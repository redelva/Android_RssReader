package com.lgq.rssreader.parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.lgq.rssreader.R;
import com.lgq.rssreader.dal.SyncStateDalHelper;
import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.Profile;
import com.lgq.rssreader.entity.Result;
import com.lgq.rssreader.entity.Subscription;
import com.lgq.rssreader.entity.Tag;
import com.lgq.rssreader.entity.Unread;
import com.lgq.rssreader.entity.UnReadCount;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.enums.SyncType;
import com.lgq.rssreader.enums.Token;
import com.lgq.rssreader.formatter.BlogFormatter;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.HtmlHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class FeedlyParser extends RssParser {
	public FeedlyParser()
	{		
        this.Preferences = ReaderApp.getPreferences();
        this.editor = Preferences.edit();
	}
	
	//Const Title
    private static final String RECOMMENDTITLE = "pop/topic/top/language/";
    public static final String READLISTTITLE = "user/-/state/com.google/reading-list";
    public static final String STARREDTTITLE = "user/-/state/com.google/starred";    

    //Feedly Urls and Login Auth
    private static final String AUTH_PARAMS = "accountType=HOSTED_OR_GOOGLE&Email={0}&Passwd={1}&service=reader&source=mobilescroll";
    private static final String FEEDLY_AUTH_PARAMS = "code={0}&client_id=feedly&client_secret=0XP4XQ07VVMDWBKUHTJM4WUQ&redirect_uri=http%3A%2F%2Fdev.feedly.com%2Ffeedly.html&grant_type=authorization_code";
    private static final String LOGINURL = "https://www.google.com/accounts/ClientLogin";
    public static final String FEEDLYLOGINURL = "http://cloud.feedly.com/v3/auth/token";    

    private static final String SUBSCRIPTIONURL = "http://cloud.feedly.com/v3/subscriptions?ct=feedly.desktop";
    private static final String PROFILEURL = "http://cloud.feedly.com/v3/profile";
    private static final String TAGURL = "http://cloud.feedly.com/v3/tags?ct=feedly.desktop";
    private static final String UNREADURL = "http://cloud.feedly.com/v3/markers/counts?ct=feedly.desktop";        
    private static final String EDITTAGURL = "http://cloud.feedly.com/v3/markers?ck=1371868985337&ct=feedly.desktop";

    private static final String EDITSUBSCRIPTIONURL = "https://cloud.feedly.com/reader/api/0/subscription/edit?client=scroll";
    private static final String RENAMETAGURL = "https://cloud.feedly.com/reader/api/0/rename-tag?client=scroll";
    private static final String MARKALLASREADURL = "https://cloud.feedly.com/reader/api/0/mark-all-as-read?client=scroll";
    private static final String DISABLETAGURL = "https://cloud.feedly.com/reader/api/0/disable-tag?client=scroll";
    private static final String ADDSUBSURL = "https://cloud.feedly.com/reader/api/0/subscription/quickadd";
    private static final String ADDSEARCHRESULTURL = "https://cloud.feedly.com/reader/api/0/subscription/edit?source=FEED_FINDER_SEARCH_RESULT&client=scroll";
    private static final String SEARCHSUBSURL = "https://cloud.feedly.com/reader/directory/search?q={0}&ck={1}&client=scroll&start={2}";
    private static final String SYNCUNREADURL = "https://cloud.feedly.com/reader/atom/user/-/state/com.google/read?n=1000";
    private static final String SORTLISTURL = "https://cloud.feedly.com/reader/api/0/preference/stream/list?output=json";
    
    // Tags, Subscription, SortOrder and Unread
    private List<Tag> Tags;
    private List<Subscription> Subscriptions;    
    private List<String> SortList;    
    private Unread Unreads;
    private List<Channel> channels;
    
    private SharedPreferences.Editor editor; 
    
    @Override
	public List<Channel> getChannels() {
		// TODO Auto-generated method stub
		//lazy load data
        if (channels != null && channels.size() > 0)
            return channels;
        if (Tags != null && Unreads != null && Subscriptions != null && SortList != null)
        {
        	List<Channel> obj = new ArrayList<Channel>();
        	for(Tag t: Tags){
        		Channel c = new Channel();
        		c.Id = t.Id;
        		c.Title = t.Label;
        		c.SortId = t.SortId;
        		c.IsDirectory = true;
        		c.Children = new ArrayList<Channel>();
        		
        		UnReadCount uc = Helper.findUnreadById(Unreads, t.Id);
        		
        		if(uc != null){
        			c.LastUpdateTime = uc.NewestItemStamp;
        			c.UnreadCount = uc.Count;
        		}else{
        			c.LastUpdateTime = new Date();
        			c.UnreadCount = 0;
        		}
        		
        		obj.add(c);
        	}
        	            
            for(Subscription s : Subscriptions)
            {
            	Channel c = new Channel();
        		c.Id = s.Id;
        		c.Title = s.Title;
        		c.SortId = s.SortId;
        		c.IsDirectory = false;
        		c.Children = new ArrayList<Channel>();
        		
        		UnReadCount uc = Helper.findUnreadById(Unreads, s.Id);
        		
        		if(uc != null){
        			c.LastUpdateTime = uc.NewestItemStamp;
        			c.UnreadCount = uc.Count;
        		}else{
        			c.LastUpdateTime = new Date();
        			c.UnreadCount = 0;
        		}

                if (s.Categories.size() != 0)
                {
                	c.IsDirectory = false;
                    for(Tag t : s.Categories)
                    {
                    	Channel d = Helper.findChannelById(obj, t.Id);
                        if (d != null)
                        {
                            d.Children.add(c);
                        }
                    }
                }
                else
                {                	
                    obj.add(c);
                }
            }
            
            Integer i = 0;
            for(Channel displayObj : obj)
            {
                displayObj.Folder = i.toString();
                displayObj.SortId = i.toString();
                i++;
                if (displayObj.Children != null)
                {
                    for(Channel display : displayObj.Children)
                    {
                        display.Folder = i.toString();
                        display.SortId = i.toString();
                        i++;
                    }
                }
            }

            //new add rss feeds will not dispear in sortlist
            ArrayList<Channel> newFeeds = new ArrayList<Channel>(); 
            for(Channel c : obj){
            	if(!SortList.contains(c.Title))
            		newFeeds.add(c);
            }
                        
            channels = new ArrayList<Channel>();
            for(String sortId : SortList)
            {
            	for(Channel c : obj){
            		if (c.Title.equals(sortId)){
            			channels.add(c);
            			break;
            		}
            	}
            }
            
            channels.addAll(newFeeds);            
        }
        return channels;
	}
    
	@Override
	public void connect(String username, String password) {
		// TODO Auto-generated method stub
		
	}
	
	public void refreshToken(final HttpResponseHandler handler){
		AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");        
        
        String params = "client_id=feedly&client_secret=0XP4XQ07VVMDWBKUHTJM4WUQ&" +
        		"grant_type=refresh_token&refresh_token=" + ReaderApp.getToken(Token.RefreshToken);
        
        StringEntity se = null;
    	try {
    	  se = new StringEntity(params.toString(),"UTF-8");
    	} catch (UnsupportedEncodingException e) {
    	  e.printStackTrace();
    	  return;
    	}
    	
    	client.post(null, FEEDLYLOGINURL, se, "application/x-www-form-urlencoded", new JsonHttpResponseHandler(){
        	public void onFailure(Throwable t, String error){
        		String result = "";
        		if(error != null){
        			result = error;
        		}else{
        			result = t.getCause().getMessage();
        		}
        		
        		Log.e("RssReader", result);
        		handler.onCallback("", false, result);
        	}
        	
        	public void onSuccess(JSONObject result){
        		try {
					ReaderApp.setToken(Token.AccessToken, result.getString("access_token"));
					
					handler.onCallback(result.getString("access_token"), true, "");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					handler.onCallback("", false, e.getMessage());
				}
        	}
        });
	}

	@Override
	public void loadData(List<SyncState> states, HttpResponseHandler handler) {
		getSubsciptions(handler);
        getUnReadCount(handler);
        getSortList(handler);
        getProfile(handler);        
        syncFromFeedly(handler);
        syncToFeedly(states,handler);
    }

    private void getProfile(final HttpResponseHandler handler) {
    	AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
    	
        client.get(PROFILEURL, new JsonHttpResponseHandler(){
        	public void onFailure(Throwable e, String errorResponse){
        		String error = "";
        		
        		if(errorResponse == null)
        			error = e.getCause().getMessage();
        		else
        			error = errorResponse;
        		
        		if (error.contains("expire"))
       		 	{
        			//now token is expired, we need to relogin        			
        			refreshToken(new HttpResponseHandler(){
        				public void onCallback(String token, boolean result, String msg){
        					if(result){
        						getProfile(handler);
        					}else{
        						if (handler != null)
        	        			{
        	        				handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetprofile));
        	        			}
        					}
        				}
        			});
       		 	}
        		else
        		{
        			if (handler != null)
        			{
        				handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetprofile));
        			}
        		}
        		
        		Log.e("RssReader", error);
        	}
        	
        	public void onSuccess(JSONObject result){
        		try
	            {
        			Profile p = new Profile();
        			
        			p.Email = result.getString("email");
        			p.FamilyName = result.getString("familyName");
        			p.Gender = result.has("gender") ? result.getString("gender") : "";
        			p.GivenName = result.getString("givenName");
        			p.Google = result.getString("google");
        			p.Id = result.getString("id");
        			p.Locale = result.getString("locale");
        			p.Picture = result.getString("picture") + "?sz=420";
        			p.Reader = result.has("reader") ?result.getString("reader") : "";
        			p.Wave = result.getString("wave");
        			
        			ReaderApp.setProfile(p);
        			
        			if (handler != null)
	                {
	            		handler.sendResponseMessage(p, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successtogetprofile));
	                }
        			
	            }catch(JSONException e){	            	
	            	e.printStackTrace();
	            	
	                Log.i("RssReader", "Error Happen Profile" + e.getMessage());
	            }
        	}
        });
	}

	private void syncToFeedly(List<SyncState> unsync, HttpResponseHandler handler)
    {
		if(unsync == null || unsync.size() == 0)
			return ;
		
        //seperate blog and channel type first
		final List<String> readblogs = new ArrayList<String>();
		final List<String> unreadblogs = new ArrayList<String>();
		final List<String> starblogs = new ArrayList<String>();
		final List<String> unstarblogs = new ArrayList<String>();
		final List<SyncState> channels = new ArrayList<SyncState>();
		
		for(SyncState state : unsync){
			if(state.BlogOriginId != null && state.BlogOriginId.length() > 0){
				
				if(state.Status == RssAction.AsRead)
					readblogs.add(state.BlogOriginId);
				
				if(state.Status == RssAction.AsUnread)
					unreadblogs.add(state.BlogOriginId);
				
				if(state.Status == RssAction.AsStar)
					starblogs.add(state.BlogOriginId);
				
				if(state.Status == RssAction.AsUnstar)
					unstarblogs.add(state.BlogOriginId);
			}
			
			if(state.ChannelId != null && state.ChannelId.length() > 0){
				channels.add(state);
			}
		}
		
		//use batchMarkTag for blog
		
		batchMarkTag(readblogs, RssAction.AsRead, new HttpResponseHandler(){
        	@Override
        	public <RssAction> void onCallback(RssAction action, boolean result, String msg){
        		if(result){
        			Log.i("RssReader", msg);
        			
        			SyncStateDalHelper helper = new SyncStateDalHelper();
        			
        			helper.Delete(readblogs, SyncType.Blog);
        		}
        	}
        });
		
		batchMarkTag(unreadblogs, RssAction.AsUnread, new HttpResponseHandler(){
        	@Override
        	public <RssAction> void onCallback(RssAction action, boolean result, String msg){
        		if(result){
        			Log.i("RssReader", msg);
        			
        			SyncStateDalHelper helper = new SyncStateDalHelper();
        			
        			helper.Delete(unreadblogs, SyncType.Blog);
        		}
        	}
        });
		
		batchMarkTag(starblogs, RssAction.AsStar, new HttpResponseHandler(){
        	@Override
        	public <RssAction> void onCallback(RssAction action, boolean result, String msg){
        		if(result){
        			Log.i("RssReader", msg);
        			
        			SyncStateDalHelper helper = new SyncStateDalHelper();
        			
        			helper.Delete(starblogs, SyncType.Blog);
        		}
        	}
        });
		
		batchMarkTag(unstarblogs, RssAction.AsUnstar, new HttpResponseHandler(){
        	@Override
        	public <RssAction> void onCallback(RssAction action, boolean result, String msg){
        		if(result){
        			Log.i("RssReader", msg);
        			
        			SyncStateDalHelper helper = new SyncStateDalHelper();
        			
        			helper.Delete(unstarblogs, SyncType.Blog);
        		}
        	}
        });
		
		//single for channel
		
		
    }
    
    private void getSubsciptions(final HttpResponseHandler handler){
    	AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
    	
        client.get(SUBSCRIPTIONURL + "&ck=" + System.currentTimeMillis(), new JsonHttpResponseHandler(){
        	@Override
        	public void onFailure(Throwable e, String errorResponse){
        		String error = "";
        		
        		if(errorResponse == null)
        			error = e.getCause().getMessage();
        		else
        			error = errorResponse;
        		
        		if (error.contains("expire"))
       		 	{
        			//now token is expired, we need to relogin        			
        			refreshToken(new HttpResponseHandler(){
        				public void onCallback(String token, boolean result, String msg){
        					if(result){
        						getSubsciptions(handler);
        					}else{
        						if (handler != null)
        	        			{
        	        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogettags));
        	        			}
        					}
        				}
        			});
       		 	}
        		else
        		{
        			if (handler != null)
        			{
        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogettags));
        			}
        		}
        		
        		Log.e("RssReader", error);
        	}
        	
        	public void onSuccess(JSONArray items){
        		try
	            {
        			List<Tag> tags = new ArrayList<Tag>();
	                for(int i=0; i < items.length(); i++)
	                {
	                	if(items.getJSONObject(i).has("categories"))
	                	{
	                		Tag t = new Tag();
		                    JSONArray categories = items.getJSONObject(i).getJSONArray("categories");
		                    
		                    JSONObject node = null;
		                    for(int j=0; j< categories.length();j++)
		                    {
		                    	if(categories.getJSONObject(j).get("id") != null)
		                    	{
		                    		node = categories.getJSONObject(j);
		                    		t.Id = node.getString("id");
			                        t.Label = node.getString("label");
					                        
			                        if (!tags.contains(t))
			                            tags.add(t);
		                    	}
		                    }		                    
	                	}
	                }
	
	                Tags = tags;
	
	                Log.i("RssReader", "Finish tags");
	                	                
	                if (handler != null)
	                {
	                	handler.sendResponseMessage(null, null, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successtogettags));
	                }
	                
	                List<Subscription> subs = new ArrayList<Subscription>();
	
	                for(int i=0; i< items.length();i++)
	                {
	                    Subscription s = new Subscription();
                    	
	                    JSONObject subscription = items.getJSONObject(i);
	                    
	                    s.Id = subscription.getString("id");
	                    s.Title = subscription.getString("title");
	                    s.Categories = new ArrayList<Tag>();
	                    if (subscription.has("categories") && subscription.getJSONArray("categories").length() > 0)
	                    {
	                    	JSONArray categories = subscription.getJSONArray("categories");
	                        for(int j=0; j< categories.length(); j++)
	                        {
	                        	JSONObject category = categories.getJSONObject(j);
	                            Tag t = new Tag();
	                            t.Id = category.getString("id");
	                            t.Label = category.getString("label");                                     
	                            s.Categories.add(t);
	                        }                                
	                    }
	                    
	                    if (subscription.has("updated"))
	                        s.FirstItemMSEC = new Date(subscription.getLong("updated"));
	                    else
	                        s.FirstItemMSEC = new Date();
	
	                    subs.add(s);
	                }

	                Subscriptions = subs;                        
	
	                if (handler != null)
	                {
	                	handler.sendResponseMessage(null, null, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successtogetsubscriptions));
	                }
	                	                
	                Log.i("RssReader", "Finish Subscriptions");
	            }
	            catch (JSONException e)
	            {
	            	if (handler != null)
	                {
	            		handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetsubscriptions));
	                }
	            	
	            	e.printStackTrace();
	            	
	                Log.i("RssReader", "Error Happen Subscriptions" + e.getMessage());
	            }
        	}
        });
    }
    
    public void getCount(final String feedId, final HttpResponseHandler handler){
    	AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
    	//client.get(UNREADURL + "&ck=" + System.currentTimeMillis() + "&steamId=" + HtmlHelper.UrlEncodeUpper(feedId), new JsonHttpResponseHandler(){
        client.get(UNREADURL + "&ck=" + System.currentTimeMillis() + "&steamId=" + URLEncoder.encode(feedId), new JsonHttpResponseHandler(){
    		@Override
    		public void onFailure(Throwable e, String errorResponse){
        		String error = "";
        		
        		if(errorResponse == null)
        			error = e.getCause().getMessage();
        		else
        			error = errorResponse;
        		
        		if (error.contains("expire"))
       		 	{
        			//now token is expired, we need to relogin        			
        			refreshToken(new HttpResponseHandler(){
        				public void onCallback(String token, boolean result, String msg){
        					if(result){
        						getCount(feedId, handler);
        					}else{
        						if (handler != null)
        	        			{
        	        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetunreadcounts));
        	        			}
        					}
        				}
        			});
       		 	}
        		else
        		{
        			if (handler != null)
        			{
        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetunreadcounts));
        			}
        		}
        		
        		Log.e("RssReader", error);
        	}
    		
    		public void onSuccess(JSONObject root){
    			try
                {
    				for(int i=0, len=root.getJSONArray("unreadcounts").length(); i<len;i++){
    					JSONObject obj = root.getJSONArray("unreadcounts").getJSONObject(i);
    					
    					if(obj.getString("id").equals(feedId)){
    						int count = obj.getInt("count");                       

    	                    if (handler != null)
    	                    {
    	                    	handler.sendResponseMessage(count, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successtogetunreadcounts));
    	                    }
    	                    
    	                    return;
    					}
    				}
    				
    				if (handler != null)
                    {
                    	handler.sendResponseMessage(-1, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetunreadcounts));
                    }


                    Log.i("RssReader","Finish unreadcount");
                }
                catch (Exception ex)
                {
                    if (handler != null)
                    {
                    	handler.sendResponseMessage(0, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetunreadcounts));
                    }
                    Log.i("RssReader","Error Happen unreadcount" + ex.getMessage());
                }
    		}
    	});
    }
    
    private void getUnReadCount(final HttpResponseHandler handler){
    	AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
    	client.get(UNREADURL + "&ck=" + System.currentTimeMillis(), new JsonHttpResponseHandler(){
    		@Override
    		public void onFailure(Throwable e, String errorResponse){
        		String error = "";
        		
        		if(errorResponse == null)
        			error = e.getCause().getMessage();
        		else
        			error = errorResponse;
        		
        		if (error.contains("expire"))
       		 	{
        			//now token is expired, we need to relogin        			
        			refreshToken(new HttpResponseHandler(){
        				public void onCallback(String token, boolean result, String msg){
        					if(result){
        						getUnReadCount(handler);
        					}else{
        						if (handler != null)
        	        			{
        	        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetunreadcounts));
        	        			}
        					}
        				}
        			});
       		 	}
        		else
        		{
        			if (handler != null)
        			{
        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetunreadcounts));
        			}
        		}
        		
        		Log.e("RssReader", error);
        	}
    		
    		public void onSuccess(JSONObject root){
    			try
                {
    				Unread unRead = new Unread();
                    //unRead.Max = root["max"].Value<int>();

                    unRead.Unreads = new ArrayList<UnReadCount>();
                    
                    JSONArray unreadcounts = root.getJSONArray("unreadcounts");
                    
                    for(int i=0; i< unreadcounts.length();i++)
                    {
                    	JSONObject count = unreadcounts.getJSONObject(i);
                    	
                        UnReadCount u = new UnReadCount();
                        u.Id = count.getString("id");
                        u.Count = count.getInt("count");
                        u.NewestItemStamp = new Date(count.getLong("updated"));

                        unRead.Unreads.add(u);
                    }

                    Unreads = unRead;                       

                    if (handler != null)
                    {
                    	handler.sendResponseMessage(null, null, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successtogetunreadcounts));
                    }

                    Log.i("RssReader","Finish unreadcount");
                }
                catch (Exception ex)
                {
                    if (handler != null)
                    {
                    	handler.sendResponseMessage(null, null, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetunreadcounts));
                    }
                    Log.i("RssReader","Error Happen unreadcount" + ex.getMessage());
                }
    		}
    	});
    }
    
    private void getSortList(final HttpResponseHandler handler){
    	AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
    	client.get("http://cloud.feedly.com/v3/preferences?ct=feedly.desktop&ck=" + System.currentTimeMillis(), new JsonHttpResponseHandler(){
    		@Override
    		public void onFailure(Throwable e, String errorResponse){
        		String error = "";
        		
        		if(errorResponse == null)
        			error = e.getCause().getMessage();
        		else
        			error = errorResponse;
        		
        		if (error.contains("expire"))
       		 	{
        			//now token is expired, we need to relogin        			
        			refreshToken(new HttpResponseHandler(){
        				public void onCallback(String token, boolean result, String msg){
        					if(result){
        						getSortList(handler);
        					}else{
        						if (handler != null)
        	        			{
        	        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetsortorder));
        	        			}
        					}
        				}
        			});
       		 	}
        		else
        		{
        			if (handler != null)
        			{
        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetsortorder));
        			}
        		}
        		
        		Log.e("RssReader", error);
        	}
    		
        	public void onSuccess(JSONObject result){
        		try{
        			if(result.has("categoriesOrdering"))
                    {
        				JSONArray orders = new JSONArray(result.getString("categoriesOrdering"));        				        			
                        
                        SortList = new ArrayList<String>();
                        
                        int length = orders.length();
                        for(int i = 0; i<length; i++){
                        	SortList.add(orders.getString(i));
                        }                		
                    }
                    else
                    {
                        SortList = new ArrayList<String>();
                    }

                    if (handler != null)
                    {
                    	handler.sendResponseMessage(null, null, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successtogetsortorder));
                    }

                    Log.i("RssReader", "Finish Sort List");
        		}
        		catch(Exception e){
        			if (handler != null)
                    {
        				handler.sendResponseMessage(null, null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetsortorder));
                    }

                    Log.i("RssReader", "Error Sort List" + e.getMessage());
        		}
        	}
    	});
    }

    private void syncFromFeedly(final HttpResponseHandler handler)
    {
        String url = "http://cloud.feedly.com/v3/markers/reads";
        
        Long lastTimeSync = Preferences.getLong("LastTimeSyncUnread", System.currentTimeMillis() - 24 * 60 *60 *1000);        
        
    	url = url + "?newerThan=" + String.valueOf(lastTimeSync);
        
        AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
        
        client.get(url, new JsonHttpResponseHandler(){
        	@Override
        	public void onFailure(Throwable e, String errorResponse){
        		String error = "";
        		
        		if(errorResponse == null)
        			error = e.getCause().getMessage();
        		else
        			error = errorResponse;
        		
        		if (error.contains("expire"))
       		 	{
        			//now token is expired, we need to relogin        			
        			refreshToken(new HttpResponseHandler(){
        				public void onCallback(String token, boolean result, String msg){
        					if(result){
        						syncFromFeedly(handler);
        					}else{
//        						if (handler != null)
//        	        			{
//        							handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedsynctofeedly));
//        	        			}
        					}
        				}
        			});
       		 	}
//        		else
//        		{
//        			if (handler != null)
//        			{
//        				handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedsynctofeedly));
//        			}
//        		}
        		
        		Log.e("RssReader", error);
        	}
        	
        	public void onSuccess(JSONObject data){        		
				try {
					List<String> entryIds = new ArrayList<String>();
					
					for(int i=0, len=data.getJSONArray("entries").length(); i< len; i++){						
						entryIds.add(data.getJSONArray("entries").getString(i));
					}
					
					editor.putLong("LastTimeSyncUnread", System.currentTimeMillis()).commit();
					
					if(handler != null && entryIds.size() > 0)
						handler.sendResponseMessage(entryIds, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successsynctofeedly));
					
					Log.i("RssReader", "Sync from feedly complete");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(handler != null)
						handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedsynctofeedly));
				}
        	}
        });
	}
    
    public void batchMarkTag(final List<String> blogs, final RssAction action, final HttpResponseHandler handler) {
    	String actionParams = "";	
		
        String method = "";
        String url = "";
        if (action == RssAction.AsStar)
        {
            url = "http://cloud.feedly.com/v3/tags/user%2F" + ReaderApp.getProfile().Id + "%2Ftag%2Fglobal.saved?ct=feedly.desktop";
            method = "PUT";
            
            StringBuffer sb = new StringBuffer();
            for(String blog : blogs){
            	sb.append("\"" + blog + "\",");
            }
            
            if(sb.length() > 0 )
            	sb.deleteCharAt(sb.length() - 1);
            
            actionParams = "{\"entryId\":[" + sb.toString() + "]}";            
        }
        else if (action == RssAction.AsRead)
        {
        	StringBuffer sb = new StringBuffer();
            for(String blog : blogs){
            	sb.append("\"" + blog + "\",");
            }
            
            if(sb.length() > 0 )
            	sb.deleteCharAt(sb.length() - 1);
        	
            actionParams = "{\"action\":\"markAsRead\",\"type\":\"entries\",\"entryIds\":[" + sb.toString() + "]}";        	
            method = "POST";
            url = "http://cloud.feedly.com/v3/markers?ct=feedly.desktop";
        }            
        else if (action == RssAction.AsUnread)
        {
        	StringBuffer sb = new StringBuffer();
            for(String blog : blogs){
            	sb.append("\"" + blog + "\",");
            }
            
            if(sb.length() > 0 )
            	sb.deleteCharAt(sb.length() - 1);
        	
            actionParams = "{\"action\":\"keepUnread\",\"type\":\"entries\",\"entryIds\":[" + sb.toString() + "]}";        	
            method = "POST";
            url = "http://cloud.feedly.com/v3/markers?ct=feedly.desktop";
        }
        
        url = url + "&ck=" + System.currentTimeMillis();
		
        AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
        
        AsyncHttpResponseHandler async = new  AsyncHttpResponseHandler(){
        	public void onSuccess(String response){
        		if (handler != null)
                {
        			handler.sendResponseMessage(action, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successupdatestatus));
                }
        	}
        	
        	 public void onFailure(Throwable e, String response) {
                 // Response failed :(
        		 
        		 String error = "";
        		 if(response == null){
        			 error = e.getCause().getMessage();
        		 }else{
        			 error = response;
        		 }
        		         		 
         		
        		 if (error.contains("expire")){
        			 //now token is expired, we need to relogin        			
        			 refreshToken(new HttpResponseHandler(){
        				 public void onCallback(String token, boolean result, String msg){
        					 if(result){
        						 batchMarkTag(blogs, action, handler);
        					 }else{
        						 if (handler != null){
        							 handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedupdatestatus));
        						 }
        					 }
        				 }
        			 });
		 		 }
        		 else
        		 {
        			 if (handler != null)
        			 {
        				 handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedupdatestatus));
        			 }
        		 }
				
        		 Log.i("RssReader", error);
        	 }
        };
        
        if(actionParams.length() == 0)
        {        	
        	client.addHeader("Content-Type","application/json");
			client.addHeader("Content-Length","0");
			client.delete(url,async);
        }
        else
        {
        	StringEntity se = null;
        	try {
        		se = new StringEntity(actionParams.toString(),"UTF-8");
        	} catch (UnsupportedEncodingException e) {
        	  e.printStackTrace();
        	  return;
        	}
        	
        	client.post(null, url, se, "application/json",async);
        }
    }

	@Override
	public void markTag(final Blog blog, final RssAction action, final HttpResponseHandler handler) {		
		String actionParams = "";	
		
        String method = "";
        String url = "";
        if (action == RssAction.AsUnstar)
        {
            try {//cloud.
				url = "http://feedly.com/v3/tags/user%2F" + ReaderApp.getProfile().Id + "%2Ftag%2Fglobal.saved/" + URLEncoder.encode(blog.BlogId, "UTF-8") + "?ct=feedly.desktop";
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            method = "DELETE";
        }            
        else if (action == RssAction.AsStar)
        {//cloud.
            url = "http://feedly.com/v3/tags/user%2F" + ReaderApp.getProfile().Id + "%2Ftag%2Fglobal.saved?ct=feedly.desktop";
            method = "PUT";
            actionParams = "{\"entryId\":\"" + blog.BlogId + "\"}";
        }
        else if (action == RssAction.AsRead)
        {//cloud.
            actionParams = "{\"action\":\"markAsRead\",\"type\":\"entries\",\"entryIds\":[\"" + blog.BlogId + "\"]}";        	
            method = "POST";
            url = "http://feedly.com/v3/markers?ct=feedly.desktop";
        }            
        else if (action == RssAction.AsUnread)
        {//cloud.
            actionParams = "{\"action\":\"keepUnread\",\"type\":\"entries\",\"entryIds\":[\"" + blog.BlogId + "\"]}";        	
            method = "POST";
            url = "http://feedly.com/v3/markers?ct=feedly.desktop";
        }
        
        url = url + "&ck=" + System.currentTimeMillis();
		
        AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
        
        AsyncHttpResponseHandler async = new  AsyncHttpResponseHandler(){
        	public void onSuccess(String response){
        		if (handler != null)
                {
        			handler.sendResponseMessage(action, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successupdatestatus));
                }
        	}
        	
        	 public void onFailure(Throwable e, String response) {
                 // Response failed :(
        		 
        		 String error = "";
        		 if(response == null){
        			 error = e.getCause().getMessage();
        		 }else{
        			 error = response;
        		 }
        		         		 
         		
        		 if (error.contains("expire")){
        			 //now token is expired, we need to relogin        			
        			 refreshToken(new HttpResponseHandler(){
        				 public void onCallback(String token, boolean result, String msg){
        					 if(result){
        						 markTag(blog, action, handler);
        					 }else{
        						 if (handler != null){
        							 handler.sendResponseMessage(action, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedupdatestatus));
        						 }
        					 }
        				 }
        			 });
		 		 }
        		 else
        		 {
        			 if (handler != null)
        			 {
        				 handler.sendResponseMessage(action, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedupdatestatus));
        			 }
        		 }
				
        		 Log.i("RssReader", error);
        	 }
        };
        
        if(method.equals("DELETE"))
        {        	
        	client.addHeader("Content-Type","application/json");
			client.addHeader("Content-Length","0");
			client.delete(url,async);
        }
        else if(method.equals("POST"))
        {
        	StringEntity se = null;
        	try {
        		se = new StringEntity(actionParams.toString(),"UTF-8");
        	} catch (UnsupportedEncodingException e) {
        	  e.printStackTrace();
        	  return;
        	}
        	
        	client.post(null, url, se, "application/json",async);
        }
        else if(method.equals("PUT"))
        {
        	StringEntity se = null;
        	try {
        		se = new StringEntity(actionParams.toString(),"UTF-8");
        	} catch (UnsupportedEncodingException e) {
        	  e.printStackTrace();
        	  return;
        	}
        	
        	client.put(null, url, se, "application/json",async);
        }
	}

	@Override
	public void markTag(final Channel channel, final RssAction action, final HttpResponseHandler handler) {
		// TODO Auto-generated method stub
		String actionParams = "";
        String url = "";
        String method = "";

        if (action == RssAction.UnSubscribe)
        {
            try {
				url = "https://cloud.feedly.com/v3/subscriptions/" + URLEncoder.encode(channel.Id,"UTF-8") + "?ck=" + System.currentTimeMillis() + "&ct=feedly.desktop&cv=17.1.614";
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            actionParams = "";
            method = "DELETE";
        }

        if (action == RssAction.RemoveTag)
        {
            //http://cloud.feedly.com/v3/subscriptions/feed%2Fhttp%3A%2F%2Ffeeds2.feedburner.com%2Fcnbeta-full?ck=1371959857730&ct=feedly.desktop
            //DELETE
            try {
				url = "https://cloud.feedly.com/v3/subscriptions/" + URLEncoder.encode(channel.Id,"UTF-8") + "?ck="+ System.currentTimeMillis() +"&ct=feedly.desktop";
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            actionParams = "";
            method = "DELETE";
        }

        if (action == RssAction.MoveTag)
        {
            //{"id":"feed/http://feeds2.feedburner.com/cnbeta-full","title":"cnBetaå…¨æ–‡ç‰ˆ","categories":[{"id":"user/d1ef3938-a54b-4404-9d9d-f97842124281/category/test","label":"test"}]}:
            //{"id":"feed/http://www.cnbeta.com/backend.php","title":"cnBeta.COM","categories":[{"id":"user/d1ef3938-a54b-4404-9d9d-f97842124281/category/Test","label":"Test"}]}
        	String newTitle = String.valueOf(channel.Tag);
        	if(newTitle.equals("Root"))
        		actionParams = "{\"id\":\"" + TextUtils.htmlEncode(channel.Id) + "\",\"title\":\"" + TextUtils.htmlEncode(channel.Title) + "\",\"categories\":[]}";
        	else
        		actionParams = "{\"id\":\"" + TextUtils.htmlEncode(channel.Id) + "\",\"title\":\"" + TextUtils.htmlEncode(channel.Title) + "\",\"categories\":[{\"id\":\"user/" + ReaderApp.getProfile().Id + "/category/" + newTitle + "\",\"label\":\"" + newTitle + "\"}]}";
            url = "http://cloud.feedly.com/v3/subscriptions?ct=feedly.desktop";
            method = "POST";
        }

        if (action == RssAction.Rename)
        {
        	String newTitle = String.valueOf(channel.Tag);
            actionParams = "{\"id\":\"" + channel.Id + "\",\"title\":\"" + newTitle + "\",\"categories\":[]}";
            url = "http://cloud.feedly.com/v3/subscriptions?ct=feedly.desktop";
            method = "POST";
        }

        if (action == RssAction.AllAsRead)
        {
            if(channel.IsDirectory)
            {
                //{"action":"markAsRead","type":"categories","categoryIds":["user/d1ef3938-a54b-4404-9d9d-f97842124281/category/æŒ‡å¯¼"],"asOf":1371898725006}
                actionParams = "{\"action\":\"markAsRead\",\"type\":\"categories\",\"categoryIds\":[\"user/" + ReaderApp.getProfile().Id + "/category/" + TextUtils.htmlEncode(channel.Title) + "\"],\"asOf\":" + System.currentTimeMillis() + "}";
                url = "http://cloud.feedly.com/v3/markers?ct=feedly.desktop";
            }
            else
            {
                //{"action":"markAsRead","type":"feeds","feedIds":["feed/http://www.wpdang.com/feed"],"asOf":1371917583346}
                actionParams = "{\"action\":\"markAsRead\",\"type\":\"feeds\",\"feedIds\":[\"" + channel.Id +"\"],\"asOf\":" + System.currentTimeMillis() + "}";
                url = "http://cloud.feedly.com/v3/markers?ct=feedly.desktop";
            }
            method = "POST";
        }
        
        url = url + "&ck=" + System.currentTimeMillis();
		
        AsyncHttpClient client = new AsyncHttpClient();
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
        
        final AsyncHttpResponseHandler async = new  AsyncHttpResponseHandler(){
        	public void onSuccess(String response){
        		if (handler != null)
                {
        			handler.sendResponseMessage(action, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successupdatestatus));
                }
        	}
        	
        	public void onFailure(Throwable e, String response) {
        		String error = "";
       		 	if(response == null){
       		 		error = e.getCause().getMessage();
       		 	}else{
       		 		error = response;
       		 	}
       		         		 
        		
       		 	if (error.contains("expire")){
       		 		//now token is expired, we need to relogin        			
       		 		refreshToken(new HttpResponseHandler(){
       		 			public void onCallback(String token, boolean result, String msg){
       		 				if(result){
       		 					markTag(channel, action, handler);
       		 				}else{
       		 					if (handler != null){
       		 						handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedupdatestatus));
       		 					}
       		 				}
       		 			}
       		 		});
		 		}
       		 	else
       		 	{
       		 		if (handler != null)
       		 		{
       		 			handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedupdatestatus));
       		 		}
       		 	}
				
       		 	Log.i("RssReader", error);
            }
        };
        
        if(actionParams.length() == 0)
        {        	
        	client.addHeader("Content-Type","application/json");
			client.addHeader("Content-Length","0");
			client.delete(url,async);
        }
        else
        {
        	StringEntity se = null;
        	try {
        	  se = new StringEntity(actionParams.toString(),"UTF-8");
        	} catch (UnsupportedEncodingException e) {
        	  e.printStackTrace();
        	  return;
        	}
        	
        	client.post(null, url, se, "application/json",async);
        }
	}

	@Override
	public void getRssBlog(final Channel channel, final Blog blog, final int count, final HttpResponseHandler handler) { 
		String url = "";
		try {		
	        if(channel.Id.length() > 0)
				url = "https://cloud.feedly.com/v3/streams/contents?streamId=" + URLEncoder.encode(channel.Id, "UTF-8");			
			else
	            url = "https://cloud.feedly.com/v3/streams/contents?streamId=" + URLEncoder.encode("user/" + ReaderApp.getProfile().Id + "/category/global.all", "UTF-8");		
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        
        if (blog.TimeStamp > 0)
        {
            if (Preferences.contains("UP" + channel.Id) && Preferences.getString("UP" + channel.Id,"").length() > 0)
            {
                url = url + "&count=" + count + "&continuation=" + Preferences.getString("UP" + channel.Id, "") + "&newerThan=" + blog.TimeStamp;
            }
            else
            {
                url = url + "&count=" + count;
            }
        }
        else if (blog.TimeStamp < 0)
        {
            if (Preferences.contains("DOWN" + blog.ChannelId) && Preferences.getString("DOWN" + blog.ChannelId,"").length() > 0)
            {
                url = url + "&count=" + count + "&continuation=" + Preferences.getString("DOWN" + blog.ChannelId,"");
            }
            else
            {
                url = url + "&count=" + count;
            }        	
        }
        else if (blog.TimeStamp == 0)
        {
            url = url + "&count=" + count;
        }

        url = url + "&ct=feedly.desktop&unreadOnly=false&ranked=newest&ck=" + System.currentTimeMillis();
        
        AsyncHttpClient client = new AsyncHttpClient();
        
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
        
        Log.i("RssReader", "Start get rss blog request at: " + new Date());
        
        client.get(url, new AsyncHttpResponseHandler(){
        	@Override
        	public void onFailure(Throwable e, String errorResponse){
        		String error = "";
        		
        		if(errorResponse == null)
        			error = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        		else
        			error = errorResponse;
        		
        		if (error.contains("expire"))
       		 	{
        			//now token is expired, we need to relogin        			
        			refreshToken(new HttpResponseHandler(){
        				public void onCallback(String token, boolean result, String msg){
        					if(result){
        						getRssBlog(channel, blog, count, handler);
        					}else{
        						if (handler != null)
        	        			{
        	        				handler.sendResponseMessage(new ArrayList<Blog>(), false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetblogs), false);
        	        			}
        					}
        				}
        			});
       		 	}
        		else
        		{
        			if (handler != null)
        			{
        				handler.sendResponseMessage(new ArrayList<Blog>(), false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetblogs), false);
        			}
        		}
        		
    			Log.i("RssReader", error);
        	}
        	
        	@Override
        	public void onSuccess(String data){
        		//get blogs and clean duplicate blogs
        		
        		Log.i("RssReader", "Finish get rss blog request at " + new Date());        		
        		
        		try{
        			JSONObject result = new JSONObject(data);
        			
        			String continuation = !result.has("continuation")
                           ? ""
                           : result.getString("continuation");
				    if (blog.TimeStamp > 0)
				    {
				        //get lastest
				        editor.putString("UP" + channel.Id, continuation).commit();
				    }
				    else if (blog.TimeStamp <= 0)
				    {
				        //get older
				    	editor.putString("DOWN" + channel.Id, continuation).commit();
				    }
				
				    List<Blog> blogs = new ArrayList<Blog>();
				    JSONArray array = result.getJSONArray("items");
				    JSONObject item = null;
				    for(int i = 0; i < array.length(); i++)
					{
				     	item = array.getJSONObject(i);
							
						Blog b = new Blog();
					
						b.TagId = "";
						b.Content = "";
						if (item.has("categories"))
						{
							JSONObject obj = null;
							JSONArray categories = item.getJSONArray("categories");
							for(int j = 0; j < categories.length(); j++)
							{    						
								obj = categories.getJSONObject(j);
								
								if(obj.getString("id").contains("category"))
								{
									b.TagId = obj.getString("id");
									break;
								}
							}
							//b.TagId = item.get("categories").Children().First(c => c["id"].Value<String>().Contains("category"))["id"].Value<String>();
						}
						b.BlogId = item.getString("id");
						b.ChannelId = item.getJSONObject("origin").getString("streamId");
						
						if(item.has("title"))
							b.Title = HtmlHelper.unescape(item.getString("title"));
						else
							b.Title = HtmlHelper.unescape(item.getJSONObject("origin").getString("title"));
						if (item.has("summary"))
							b.Description = HtmlHelper.unescape(item.getJSONObject("summary").getString("content"));
						else
							b.Description = HtmlHelper.unescape(item.getJSONObject("content").getString("content"));
						if (item.has("alternate")){
							int alt = item.getJSONArray("alternate").length();
							for(int j=0; j<alt; j++){
								if(item.getJSONArray("alternate").getJSONObject(j).has("href"))
									b.Link = item.getJSONArray("alternate").getJSONObject(j).getString("href");
								if(item.getJSONArray("alternate").getJSONObject(j).has("originId"))
									b.Link = item.getString("originId");
								
								if(b.Link.length() > 0)
									break;
							}
						}
							
						//remove cnbeta ad
						if (b.Link.contains("cnbeta.com"))
						{
							int index = b.Description.indexOf("<img");
							if (index != -1)
								b.Description = b.Description.substring(0, index);
						}
			
						b.PubDate = new Date(item.getLong("published"));
						b.SubsTitle = item.getJSONObject("origin").getString("title");
						b.TimeStamp = item.getLong("crawled");
						b.IsRead = !item.getBoolean("unread");
						b.Avatar = "";
			
						if (item.has("tags"))
						{
							JSONObject obj = null;
							JSONArray tags = item.getJSONArray("tags");
							for(int j = 0; j< tags.length();j++)
							{
								if (tags.getString(j).contains("saved"))
									b.IsStarred = true;
							}
						}
						
						b.OriginId = item.getString("id");
						b.IsRecommend = false;						
			
						blogs.add(b);
					}
				    
				    //deal with long time no updates
	                boolean hasMore = true;
	                for(Blog b : blogs){
	                	long between=(b.PubDate.getTime()- blog.PubDate.getTime())/1000;
	                	
						if(between > 0L)
							hasMore = hasMore && true;
						else
							hasMore = hasMore && false;
					}
	                
	                if(blog.TimeStamp <= 0L)
	                	hasMore = false;
	                
	                if(hasMore)
	                {	                    
	                    if (handler != null)
	                    {
	                    	handler.sendResponseMessage(blogs, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_waitformoreblogs), true);
	                    }
	                    getRssBlog(channel, blog, count, handler);
	                }
	                else
	                {
	                    if(blog.TimeStamp > 0)
	                        editor.putString("UP" + channel.Id, "").commit();

	                    if (handler != null)
	                    {
	                    	handler.sendResponseMessage(blogs, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successtogetblogs), false);
	                    }
	                }
        		}
        		catch(Exception json){
        			json.printStackTrace();
        			if (handler != null)
        				handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedtogetblogs));                    
        		}
        	}
        });
	}

	@Override
	public void getFavor(String tag, Blog b, int count, HttpResponseHandler handler) {
		Channel c = new Channel();
		c.Id = "user/" + ReaderApp.getProfile().Id + "/tag/" + tag;
				
		getRssBlog(c, b, count, handler);
	}

	@Override
	public void addRss(final String rssUrl, String searchResultTitle, final HttpResponseHandler handler) {
		String actionParams = "";

		AsyncHttpClient client = new AsyncHttpClient();
        
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));        
        
        actionParams = "{\"id\":\"" + rssUrl + "\",\"title\":\"" + searchResultTitle + "\",\"categories\":[]}";
        
        String url = "http://cloud.feedly.com/v3/subscriptions?ck=" + System.currentTimeMillis() + "&ct=feedly.desktop&cv=16.0.548";
        
        ByteArrayEntity entity = null;
		try {
			entity = new ByteArrayEntity(actionParams.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        
    	client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler(){
    		@Override
    		public void onFailure(Throwable e, String response) {
    			// Response failed :(
       		 
       		 	String error = "";
       		 	if(response == null){
       		 		error = e.getCause().getMessage();
	       		 }else{
	       			 error = response;
	       		 }
	       		 
	       		 if (error.contains("expire"))
	       		 {
                    //now token is expired, we need to relogin
                    if (handler != null)
                    {
                   	 	handler.sendResponseMessage(rssUrl, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_sessionexpire));
                    }
                }
                else
                {
                    if (handler != null)
                    {
                    	handler.sendResponseMessage(rssUrl, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedaddsubscription));
                    }
                }
            }                
    		
    		@Override
    		public void onSuccess(String data){
    			if(handler != null)
    				handler.sendResponseMessage(rssUrl, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successaddsubscription));    			
    		}
        });
	}

	@Override
	public void assignFolder(final Channel folder, Channel single, final HttpResponseHandler handler) {
		String actionParams = "";
        // Means new folder
        if (folder.SortId.length() != 0)
        {
            //new folder
            actionParams = "{\"id\":\"" + single.Id + "\",\"title\":\"" + single.Title + "\",\"categories\":[{\"id\":\"user/" + ReaderApp.getProfile().Id + "/category/" + folder.Title + "\",\"label\":\"" + folder.Title + "\"}]}";
        }
        if (folder.Title.length() == 0)
        {
            actionParams = "{\"id\":\"" + single.Id + "\",\"title\":\"" + single.Title + "\",\"categories\":[]}";
        }
        AsyncHttpClient client = new AsyncHttpClient();
        
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
        
        StringEntity se = null;
    	try {
    		se = new StringEntity(actionParams.toString(),"UTF-8");
    	} catch (UnsupportedEncodingException e) {
    	  e.printStackTrace();
    	  return;
    	}
    	
    	String url = "http://cloud.feedly.com/v3/subscriptions?ct=feedly.desktop&ck=" + System.currentTimeMillis();
    	client.post(null, url, se, "application/json", new AsyncHttpResponseHandler(){
    		@Override
    		public void onFailure(Throwable t, String result){
    			String error = "";
    			if(result == null)
    				error = t.getCause().getMessage();
    			else
    				error = result;
    			
    			Log.i("RssReader", error);
    			
    			if (error.contains("expire"))//now token is expired, we need to relogin
                    handler.sendResponseMessage(false, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_sessionexpire));
                else
                	handler.sendResponseMessage(false, false, String.format(ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedmovefolder), folder.Title));
    		}
    		
    		@Override
    		public void onSuccess(String reponse){
    			handler.sendResponseMessage(true, true, String.format(ReaderApp.getAppContext().getResources().getString(R.string.feedly_successmovefolder), folder.Title));
    		}
    	});
	}

	@Override
	public void searchRss(String key, int page, final HttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
        
    	client.addHeader("Host", "cloud.feedly.com");
        client.addHeader("Accept-Charset", "utf8");
        client.addHeader("Referer", "http://cloud.feedly.com/");
        client.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));

        String url = "";
		try {
			url = "http://www.feedly.com/v3/search/feeds?q=" + URLEncoder.encode(key, "UTF-8") + "&n=20&d=true&ck=" + System.currentTimeMillis();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        client.get(url, new JsonHttpResponseHandler(){
        	@Override
        	public void onFailure(Throwable t, String result){
        		String error = "";
    			if(result == null)
    				error = t.getCause().getMessage();
    			else
    				error = result;
    			
    			Log.i("RssReader", error);
    			if(error.contains("session"))
        			handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_sessionexpire), false);
                else
                	handler.sendResponseMessage(null, false, ReaderApp.getAppContext().getResources().getString(R.string.feedly_failedsearch), false);
        	}
        	
        	@Override
        	public void onSuccess(JSONObject obj){
                List<Result> results = new ArrayList<Result>();

                try{
	                JSONObject r = null;
	                for(int i=0, len = obj.getJSONArray("results").length(); i<len;i++){
	                	r = obj.getJSONArray("results").getJSONObject(i);
	                	Result result = new Result();
	
	                    result.IsSubscribed = false;
	                    result.Title = r.getString("title");
	                    result.StreamId = r.getString("feedId");
	                    result.SubscriptCount = r.getString("subscribers");
	                    results.add(result);
	                }
	                
	                handler.sendResponseMessage(results, true, ReaderApp.getAppContext().getResources().getString(R.string.feedly_successsearch), false);
                }catch(JSONException je){
                	je.printStackTrace();
                }
        	}
        });
	}

	@Override
	public void asyncDownload(Channel c, int count, HttpResponseHandler handler) {
	
	}
	
	/*
	 * This method is used in DownloadTask, it works in background and should be synchronous
	 * So we don't use AsyncHttpClient to make http request, we use default HttpClient instead. 
	 */
	@Override
	public List<Blog> syncDownload(Channel c, int count) {
		String url = "";
		try {
	        if(c.Id.length() > 0)
				url = "https://cloud.feedly.com/v3/streams/contents?streamId=" + URLEncoder.encode(c.Id, "UTF-8");			
			else
	            url = "https://cloud.feedly.com/v3/streams/contents?streamId=" + URLEncoder.encode("user/" + ReaderApp.getProfile().Id + "/category/global.all", "UTF-8");		
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        
        url = url + "&count=" + count + "&ct=feedly.desktop&unreadOnly=false&ranked=newest&ck=" + System.currentTimeMillis();
        
        HttpGet httpRequest = new HttpGet(url);
        
        httpRequest.addHeader("Host", "cloud.feedly.com");
        httpRequest.addHeader("Accept-Charset", "utf8");
        httpRequest.addHeader("Referer", "http://cloud.feedly.com/");
        httpRequest.addHeader("X-Feedly-Access-Token", ReaderApp.getToken(Token.AccessToken));
        //取得HttpClient对象  
        HttpClient httpclient = new DefaultHttpClient();
        
        HttpResponse response;
        
        List<Blog> blogs = new ArrayList<Blog>();
		
        try {
			response = httpclient.execute(httpRequest);
		        
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	        	//取得返回的字符串  
	        	String data = EntityUtils.toString(response.getEntity());
	    		
				JSONObject result = new JSONObject(data);
						    
			    JSONArray array = result.getJSONArray("items");
			    JSONObject item = null;
			    for(int i = 0; i < array.length(); i++)
				{
			     	item = array.getJSONObject(i);
						
					Blog b = new Blog();
				
					b.TagId = "";
					b.Content = "";
					if (item.has("categories"))
					{
						JSONObject obj = null;
						JSONArray categories = item.getJSONArray("categories");
						for(int j = 0; j < categories.length(); j++)
						{    						
							obj = categories.getJSONObject(j);
							
							if(obj.getString("id").contains("category"))
							{
								b.TagId = obj.getString("id");
								break;
							}
						}
						//b.TagId = item.get("categories").Children().First(c => c["id"].Value<String>().Contains("category"))["id"].Value<String>();
					}
					b.BlogId = item.getString("id");
					b.ChannelId = item.getJSONObject("origin").getString("streamId");
					b.Title = HtmlHelper.unescape(item.getString("title"));
					if (item.has("summary"))
						b.Description = HtmlHelper.unescape(item.getJSONObject("summary").getString("content"));
					else
						b.Description = HtmlHelper.unescape(item.getJSONObject("content").getString("content"));
					if (item.has("alternate")){
						int alt = item.getJSONArray("alternate").length();
						for(int j=0; j<alt; j++){
							if(item.getJSONArray("alternate").getJSONObject(j).has("href"))
								b.Link = item.getJSONArray("alternate").getJSONObject(j).getString("href");
							if(item.getJSONArray("alternate").getJSONObject(j).has("originId"))
								b.Link = item.getString("originId");
							
							if(b.Link.length() > 0)
								break;
						}
					}
						
					//remove cnbeta ad
					if (b.Link.contains("cnbeta.com"))
					{
						int index = b.Description.indexOf("<img");
						if (index != -1)
							b.Description = b.Description.substring(0, index);
					}
		
					b.PubDate = new Date(item.getLong("published"));
					b.SubsTitle = item.getJSONObject("origin").getString("title");
					b.TimeStamp = item.getLong("crawled");
					b.IsRead = !item.getBoolean("unread");
					b.Avatar = "";
		
					if (item.has("tags"))
					{
						JSONObject obj = null;
						JSONArray tags = item.getJSONArray("tags");
						for(int j = 0; j< tags.length();j++)
						{
							if (tags.getString(j).contains("saved"))
								b.IsStarred = true;
						}
					}
					
					b.OriginId = item.getString("id");
					b.IsRecommend = false;						
		
					blogs.add(b);
				}
			    
			    
			}
		}
		catch(JSONException json){
			json.printStackTrace();
		}
    	catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    	}
		catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return blogs;
	}		
}
