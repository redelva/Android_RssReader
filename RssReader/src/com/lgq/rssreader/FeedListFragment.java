package com.lgq.rssreader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import org.json.JSONArray;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;  
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.kanak.emptylayout.EmptyLayout;
import com.lgq.rssreader.R.id;
import com.lgq.rssreader.adapter.BlogAdapter;
import com.lgq.rssreader.adapter.ResultAdapter;
import com.lgq.rssreader.adapter.ChannelAdapter;
import com.lgq.rssreader.controls.PullToRefreshListView;
import com.lgq.rssreader.controls.XListView;
import com.lgq.rssreader.controls.XListView.IXListViewListener;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.dal.SyncStateDalHelper;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.task.DownloadTask;
import com.lgq.rssreader.task.ProfileTask;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.NetHelper;
import com.lgq.rssreader.utils.NotificationHelper;
import com.lgq.rssreader.entity.*;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.enums.RssTab;

/**
 * A list fragment representing a list of Blogs. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link BlogContentFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class FeedListFragment extends SherlockFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated tab.
     */
    public static final String STATE_TAB = "tab";
    
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated tab.
     */
    public static final String SUBSCRIBETITLE = "subscribetitle";
    
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated tab.
     */
    public static final String SEARCHTITLE = "searchtitle";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = channelCallback;
    
    /**
     * The fragment's data source of Home Tab
     */
    private List<Channel> channels;
    
    /**
     * The fragment's data source of other tab
     */
    private List<Blog> blogs;
    
    /**
     * The fragment's data source of add tab
     */
    private List<Result> results;
    
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    
    /**
     * The current adapter for list.
     */
    private BaseAdapter adapter;
    
    private XListView listView;
    
    private EmptyLayout emptyLayout;      
    
    public XListView getListView(){return listView;}
    
    /**
     * The current tab which this list presents.
     */
    private RssTab tab;
    
    private String title;
    
    private int page;
        
    public static final int HOME = 0;
    public static final int ALL = 1;
    public static final int RECOMMEND = 2;
    public static final int UNREAD = 3;
    public static final int STAR = 4;
    public static final int GALLERY = 5;
    public static final int SEARCH = 6;
    public static final int SUBSCRIBE = 7;
    public static final int CLEAR = 8;
    public static final int MARKTAG = 9;  
    public static final int UNSUBSCRIBE = 10;
    
    public static final int LOCAL = 101;
    public static final int SYNC = 102;
    
    
    final String[] recommends = new String[]{
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title1),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title2),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title3),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title4),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title5),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title6),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title7),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title8),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title9),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_title10)};
	final String[] urls = new String[]{
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url1),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url2),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url3),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url4),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url5),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url6),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url7),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url8),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url9),
			ReaderApp.getAppContext().getResources().getString(R.string.recommend_url10)};
	final boolean[] chsBools = new boolean[]{false, false, false,false, false, false,false, false, false,false};

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(Object c, RssTab tab);
        
        /**
         * Callback for channels load complete.
         */
        public void onSyncComplete(Object c, RssTab tab);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks channelCallback = new Callbacks() {
        @Override
        public void onItemSelected(Object c, RssTab tab) {
        }
        @Override
        public void onSyncComplete(Object c, RssTab tab) {
        }
    };
        
    public Handler myHandler = new Handler(){
        @Override  
        public void handleMessage(Message msg) {            
            switch(msg.what){
            	case HOME:
            		if(getView() != null){
            			
            			if(msg.obj instanceof List){
            				channels = (List<Channel>)msg.obj;
            				if(channels.size() > 0){
	            				if(listView.getAdapter() == null){
	            					List<Channel> list = new ArrayList<Channel>();
	            					if(!ReaderApp.getSettings().ShowAllFeeds){	            						
	            						for (Iterator it = channels.iterator();it.hasNext();){
	            							Channel c = (Channel)it.next(); 
	            							if(c.UnreadCount > 0 )
	            								list.add(c);
	            						}
	            					}else{
	            						list = channels;
	            					}
	            					
	            					adapter = new ChannelAdapter(
	    	                                getActivity(),
	    	                                list,
	    	                                listView);
		                			listView.setAdapter(adapter);
	            					
	            				}else{
	            					HeaderViewListAdapter wrap = (HeaderViewListAdapter)listView.getAdapter();
	            					
	            					List<Channel> list = new ArrayList<Channel>();
	            					if(!ReaderApp.getSettings().ShowAllFeeds){	            						
	            						for (Iterator it = channels.iterator();it.hasNext();){
	            							Channel c = (Channel)it.next(); 
	            							if(c.UnreadCount > 0 )
	            								list.add(c);
	            						}
	            					}else{
	            						list = channels;
	            					}
	            					
	            					((ChannelAdapter)wrap.getWrappedAdapter()).ResetData(list);
	            				}
            				}else{
            					// need to recommend some feeds
            					
            					if(msg.arg1 == SYNC){
            						OnMultiChoiceClickListener multiClick = new OnMultiChoiceClickListener(){
                				    	
                						@Override
                						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                							if(isChecked){
                								FeedlyParser parser = new FeedlyParser();
                								parser.addRss(urls[which], recommends[which], null);            								
                							}
                						}
                	        		};
                					
                					AlertDialog dialog = new AlertDialog.Builder(getActivity())  
    			   	                         .setIcon(android.R.drawable.btn_star_big_on)  
    			   	                         .setTitle(getActivity().getResources().getString(R.string.recommend_add))
    			   	                         .setMultiChoiceItems(recommends, chsBools, multiClick)
    			   	                         .setPositiveButton(getActivity().getResources().getString(R.string.yes), new OnClickListener(){
    			   	                        	 	@Override
    			   		    						public void onClick(DialogInterface dialog, int which) {
    			   	                        	 		dialog.dismiss();
    			   	                        	 		dialog.cancel();
    			   	                        	 		loadChannel();			   	                        	 		
    			   		    						}
    			   		            			})
    			   	                         .setNegativeButton(getActivity().getResources().getString(R.string.no),  null).create();
    			   	                dialog.show();
            					}            					
            				}
            			}
            			
            			if(msg.obj instanceof Profile){
            				final com.lgq.rssreader.entity.Profile p = (com.lgq.rssreader.entity.Profile)msg.obj;
            				
            				MainActivity main = (MainActivity)getActivity();
            				
        	            	TextView nickName = (TextView)main.mSlidingMenu.findViewById(R.id.nickNameTextView);
        	            	final ImageView head = (ImageView)main.mSlidingMenu.findViewById(R.id.headImageView);
            				
            				if(p.LocalPicture != null && p.LocalPicture.length() != 0){
            					File SDFile = android.os.Environment.getExternalStorageDirectory();            					
            					Bitmap bm = BitmapFactory.decodeFile(SDFile.getAbsolutePath() +  p.LocalPicture);		
            					if(bm != null)
            	    				 head.setImageBitmap(bm);
            	    			 else
            	    				 new ProfileTask(head).execute(p);
            				}else{
            					new ProfileTask(head).execute(p);
            				}
        	            	
        	       		 	nickName.setText(p.FamilyName + p.GivenName);
            			}
            		}
                	break;
            	case ALL:
        		case RECOMMEND:
        		case UNREAD:
        		case SEARCH:
        			if(getView() != null){
        				blogs = (List<Blog>)msg.obj;
        				if(listView.getAdapter() == null){
	        				adapter = new BlogAdapter(
	                            getActivity(),
	                            blogs,
	                            listView
	                            );
	        				listView.setAdapter(adapter);
        				}else{
        					((BaseAdapter) adapter).notifyDataSetChanged();
        				}
        				
        				if(blogs.size() == 0){
        					emptyLayout.setEmptyMessage(getActivity().getResources().getString(R.string.list_empty_view));
        					emptyLayout.setShowEmptyButton(true);        					
        					emptyLayout.showEmpty();
        				}
        			}
        			break;
        		case STAR:
        			if(getView() != null){
        				blogs = (List<Blog>)msg.obj;
        				if(listView.getAdapter() == null){
	        				adapter = new BlogAdapter(
	                            getActivity(),
	                            blogs,
	                            listView
	                            );
	        				listView.setAdapter(adapter);
        				}else{
        					((BlogAdapter) adapter).AddMoreData(blogs);
        					
        					//adapter.notifyDataSetChanged();
        					emptyLayout.showListView();
        				}
        				
        				if(blogs.size() == 0){
        					emptyLayout.setEmptyMessage(getActivity().getResources().getString(R.string.list_empty_view));        					
        					emptyLayout.getEmptyView().setOnClickListener(new View.OnClickListener(){        						
								@Override
								public void onClick(View v) {
									loadOnlineData();
								}
        					});
        					emptyLayout.showEmpty();
        				}
        			}
                	break;
        		case SUBSCRIBE: 
        			if(getView() != null){
        				results = (List<Result>)msg.obj;
        				if(listView.getAdapter() == null){
        				adapter = new ResultAdapter(
	                            getActivity(),
	                            results,
	                            listView
	                            );
	        				listView.setAdapter(adapter);
        				}else{
        					adapter.notifyDataSetChanged();
        				}
        			}
                	break;
        		case GALLERY:
        			break;
        		case CLEAR:
//        			if(getActivity() != null){
//        				ImageButton btnRight =((ImageButton)getActivity().findViewById(R.id.ivTitleBtnRight));
//            			if(btnRight != null)
//            				btnRight.clearAnimation();
//        			}
//        			else{
//        				
//        			}
        			if(mCallbacks != null)
    					mCallbacks.onSyncComplete(null, tab);
        			break;
        		case MARKTAG:
        			if(adapter != null){
        				int index = msg.arg1;
        				Channel c = (Channel)msg.obj;
        				((ChannelAdapter) adapter).GetData().set(index, c);
						adapter.notifyDataSetChanged();
        			}
        			break;
        		case UNSUBSCRIBE:
        			if(adapter != null){
        				Channel c = (Channel)msg.obj;
        				((ChannelAdapter) adapter).GetData().remove(c);						
						adapter.notifyDataSetChanged();
        			}
        			break;
            }
            
            super.handleMessage(msg);
        }
    };
    
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FeedListFragment() {
    	
    }    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        
        // Restore the previously serialized tab.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAB)) {
            tab = RssTab.values()[savedInstanceState.getInt(STATE_TAB)];
        }
        
        if(this.getArguments().containsKey(STATE_TAB))
        	tab = RssTab.values()[this.getArguments().getInt(STATE_TAB)];
        
        if(this.getArguments().containsKey(SUBSCRIBETITLE))
        	title = this.getArguments().getString(SUBSCRIBETITLE);
        
        if(this.getArguments().containsKey(SEARCHTITLE))
        	title = this.getArguments().getString(SEARCHTITLE);
        
        page = 1;
    }   
    
    @Override    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed_list, container, false);
        //View emptyView = inflater.inflate(R.layout.listview_empty, container, false);
        
        listView = (XListView)rootView.findViewById(id.feed_list);
        
        emptyLayout = new EmptyLayout(this.getActivity(), listView);
        emptyLayout.setLoadingMessage(getActivity().getResources().getString(R.string.content_loading));
        //emptyLayout.setLoadingAnimationViewId(emptyLayout.getLoadingAnimationViewId());
        emptyLayout.setLoadingAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
        emptyLayout.showLoading();
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> view, View v, int position, long id) {
				Object obj = adapter.getItem(position - 1);        
		        mCallbacks.onItemSelected(obj, tab);
			}
        });
        
        listView.setXListViewListener(new IXListViewListener(){

			@Override
			public void onRefresh() {
				switch(tab){
		    		case Home:
		    			break;		    		
		    		//case Recommend:
		    		//case Unread:
		    			//loadData();	    			
		    			//break;
		    		case All:
		    		case Star:
		    			loadOnlineData();
		    			break;
		    		case Search:
		    		case Subscribe:
		    		case Gallery:
		    			break;
					default:
						break;
		    	}
			}

			@Override
			public void onLoadMore() {
				switch(tab){
		    		case Home:
		    			break;
		    		case All:
		    		case Recommend:
		    		case Unread:
		    		case Star:
		    		case Search:
		    			page++;
		    			loadData();	    			
		    			break;
		    		case Gallery:
		    			break;
		    		case Subscribe:
		    			break;
				}
			}
        });
        
        if(tab == RssTab.Home){
        	registerForContextMenu(listView);
        }
        
        BlogDalHelper helper = new BlogDalHelper(); 

    	switch(tab){
    		case Home:
    	        if(getActivity() != null){
//    	        	MainActivity main = (MainActivity)getActivity();
//    	        	if(!main.isLoaded){
//    	        		ImageButton v =((ImageButton) getActivity().findViewById(R.id.ivTitleBtnRight));
//    	        		Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);    	        
//    	    	        v.startAnimation(anim);
//    	        		loadChannel();
//    	        		main.isLoaded = true;
//    	        	}else{
    	        		Message m = myHandler.obtainMessage();                    				
        				m.what = RssTab.Home.ordinal();
        	            m.obj = Helper.getChannels();
        	            m.arg1 = LOCAL;
        				myHandler.sendMessage(m);
    	        	//}
    	        }
    			listView.setPullLoadEnable(false);
    			listView.setPullRefreshEnable(false);
    			break;
    		case All:
    		case Recommend:
    		case Unread:
    		case Star:
    			loadData();
    			listView.setPullLoadEnable(true);
    			listView.setPullRefreshEnable(true);
    			break;
    		case Search:
        		Message m = myHandler.obtainMessage();
	            m.what = tab.ordinal();
	            m.obj = helper.GetBlogListByKeyword(title, 1, 30);;
				myHandler.sendMessage(m);
    			break;
    		case Subscribe:
    			listView.setPullLoadEnable(false);
    			listView.setPullRefreshEnable(false);
    			FeedlyParser parser = new FeedlyParser();
    			
    			parser.searchRss(title, page, new HttpResponseHandler(){
    				@Override
    				public <T> void onCallback(List<T> data, boolean result, String msg, boolean more){
    					Message m = myHandler.obtainMessage();
    		            m.what = tab.ordinal();
    		            m.obj = data;
    					myHandler.sendMessage(m);
    				}
    			});
    			
    			break;
    		case Gallery:
    			break;    		
    	}
    	
    	helper.Close();
    	
    	return rootView;
    }
    
    public void loadData(){
    	new Thread(new Runnable() {  
            @Override  
            public void run() {
            	
            	BlogDalHelper helper = new BlogDalHelper();
            	
            	Message m = myHandler.obtainMessage();
	            m.what = tab.ordinal();
	            m.obj = helper.GetBlogList(tab, page, 30);
				myHandler.sendMessage(m);
				
				helper.Close();
            }
        }).start();
    	
    	listView.stopLoadMore();
    }
    
    public void loadOnlineData(){
    	HttpResponseHandler handler = new HttpResponseHandler(){
        	@Override
        	public <Blog> void onCallback(List<Blog> blogs, boolean result, String msg, boolean hasMore){
        		if(result){
        			
        			BlogDalHelper helper = new BlogDalHelper();
        			
        			helper.SynchronyData2DB((List<com.lgq.rssreader.entity.Blog>) blogs);

    				helper.Close();
    				
        			if(blogs.size() > 0){
        				Message m = myHandler.obtainMessage();
        	            m.what = tab.ordinal();
        	            m.obj = blogs;
        				myHandler.sendMessage(m);
        			}
        			
        			Helper.sound();
        		}else{
        			Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        		}
        		
        		listView.stopRefresh();
        	}
        };
        
        if(tab == RssTab.Star)
        	loadStar(handler);
        
        if(tab == RssTab.All)
        	loadAll(handler);    	
    }
    
    private void loadStar(HttpResponseHandler handler){
    	FeedlyParser parser = new FeedlyParser();									
		
		Blog b = new Blog();
		b.TimeStamp = 0;
		b.PubDate = new Date();
		
		parser.getFavor("global.saved", b, 30, handler);
    }
    
    private void loadAll(HttpResponseHandler handler){
    	FeedlyParser parser = new FeedlyParser();									
		
		Blog b = new Blog();
		b.TimeStamp = 0;
		b.PubDate = new Date();
		
		Channel channel = new Channel();
		channel.Id = "";
		
		parser.getRssBlog(channel, b, 30, handler);
    }
    
    public void updateChannel(){
    	new Thread(new Runnable() {  
            @Override  
            public void run() {
            	final SharedPreferences mPrefs = ReaderApp.getPreferences();
            	                    	
            	if(mPrefs.contains("Channel")){
            		Message m = myHandler.obtainMessage();                    				
    	            m.what = RssTab.Home.ordinal();
    	            m.obj = Helper.getChannels();
    				myHandler.sendMessage(m);        				
            	}
            }
    	}).start();
    }
    
    public void loadChannel(){
		new Thread(new Runnable() {  
            @Override  
            public void run() {
            	final SharedPreferences mPrefs = ReaderApp.getPreferences();
            	                    	
                try {
                	if(mPrefs.contains("Channel")){
                		List<Channel> channels = Helper.getChannels(); 
                		if(channels != null && channels.size() > 0){
                			Message m = myHandler.obtainMessage();
	        	            m.what = RssTab.Home.ordinal();
	        	            m.obj = channels;
	        	            m.arg1 = LOCAL;
	        				myHandler.sendMessage(m);
                		}
                	}
            		final FeedlyParser feedly = new FeedlyParser();
            		
            		SyncStateDalHelper stateHelper = new SyncStateDalHelper();
            		
            		List<SyncState> states = stateHelper.GetSyncStateList();
            		
            		stateHelper.Close();
            		
                    feedly.loadData(states, new HttpResponseHandler(){
                    	@Override
                    	public <Blog, SyncState> void onCallback(List<Blog> blogs, List<SyncState> states, boolean result, String msg){
                    		if(result){
                    			if(feedly.getChannels() != null){
                    				
                    				Helper.saveChannels(feedly.getChannels());
                    				
                    				Message m = myHandler.obtainMessage();                    				
                    				m.what = RssTab.Home.ordinal();
                    	            m.obj = feedly.getChannels();
                    	            m.arg1 = SYNC;
                    				myHandler.sendMessage(m);
                    				
                    				Helper.vibrate();
                    				
                    				Message c = myHandler.obtainMessage();                    				
                    				c.what = CLEAR;            	            
                    				myHandler.sendMessage(c);
                    			}
                    		}else{
                    			Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
                    		}

                    	}
                    	
                    	@Override
                    	public <T> void onCallback(List<T> data, boolean result, String msg){
                    		if(result && msg.equals(ReaderApp.getAppContext().getResources().getString(R.string.feedly_successsynctofeedly))){
                    			
                    			BlogDalHelper helper = new BlogDalHelper(); 
                    			
                    			helper.MarkAsRead((List<String>)data, true);
                    			
                    			helper.Close();
                    			
                    			Log.i("RssReader", "Finish sync from feedly count: " + data.size());
                    		}
                    	}
                    	
                    	@Override
                    	public <Profile> void onCallback(Profile profile, boolean result, String msg){                                		
                    		if(result){
                    			Message m = myHandler.obtainMessage();                    				
                    			m.what = RssTab.Home.ordinal();
                	            m.obj = profile;
                				myHandler.sendMessage(m);                            				
                    		}
                    	}
                    });
                } catch (Exception e) {  
                    // TODO Auto-generated catch block  
                    e.printStackTrace();  
                }
            }  
        }).start();
    }
    
    private void markTag(final Channel c, RssAction action){
    	FeedlyParser parser = new FeedlyParser();
    	if(action == RssAction.AllAsRead){
    		parser.markTag(c, action, new HttpResponseHandler(){
    			@Override
    			public <RssAction> void onCallback(RssAction data, boolean result, String msg){
    				if(result){
						HeaderViewListAdapter headerViewAdapter = (HeaderViewListAdapter)listView.getAdapter();
    					
    					ChannelAdapter adapter = (ChannelAdapter) headerViewAdapter.getWrappedAdapter();
    					
    					if(adapter != null){
    						int index = adapter.GetData().indexOf(c);
    						
    						if(index != -1){
    							if(c.IsDirectory){
    								for(Channel child : c.Children){
    									child.UnreadCount = 0;
    								}
    							}
    							
    							c.UnreadCount = 0;
    							
    							Message m = myHandler.obtainMessage();                    				
    							m.what = MARKTAG;
    							m.obj = c;
    							m.arg1 = index;
                				myHandler.sendMessage(m);
    							
    							BlogDalHelper helper = new BlogDalHelper();
    							helper.MarkAsRead(c, true);
    							helper.Close();
    							
    							Toast.makeText(getActivity(), ReaderApp.getAppContext().getString(R.string.feedly_successupdatestatus), Toast.LENGTH_SHORT).show();
    						}
    					}
    				}else{
    					Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
    				}
    			}
    		});
    	}
    	
    	if(action == RssAction.UnSubscribe){    		
    		parser.markTag(c, action, new HttpResponseHandler(){
    			@Override
    			public <RssAction> void onCallback(RssAction data, boolean result, String msg){
    				if(result){
//						HeaderViewListAdapter headerViewAdapter = (HeaderViewListAdapter)listView.getAdapter();
//    					
//    					ChannelAdapter adapter = (ChannelAdapter) headerViewAdapter.getWrappedAdapter();
//    					
//    					if(adapter != null){
//    						
//    						Message m = myHandler.obtainMessage();                    				
//							m.what = UNSUBSCRIBE;
//							m.obj = c;							
//            				myHandler.sendMessage(m);
    						
    						BlogDalHelper helper = new BlogDalHelper();
    						helper.DeleteBlogByChannel(c);
    						helper.Close();
    					//}
    				}else{
    					Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
    				}
    			}
    		});
    	}
    	
    	if(action == RssAction.Rename){
    		parser.markTag(c, action, new HttpResponseHandler(){
    			@Override
    			public <RssAction> void onCallback(RssAction data, boolean result, String msg){
    				if(result){
    					loadChannel();
    				}else{
    					Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
    				}
    			}
    		});
    	}
    	
    	if(action == RssAction.MoveTag){
    		parser.markTag(c, action, new HttpResponseHandler(){
    			@Override
    			public <RssAction> void onCallback(RssAction data, boolean result, String msg){
    				if(result){
    					loadChannel();
    				}else{
    					Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
    				}
    			}
    		});
    	}
    }   
    
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	
    	if (bMenu) {
            bMenu=false;
            
            if(tab == RssTab.Home){
    	        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	        
    	        Log.i("RssReader", info.id +" " + info.position);
    	        
    	        final Channel c = (Channel) listView.getAdapter().getItem((int) info.position);
    	        
    	        FeedlyParser parser = new FeedlyParser();
    	        
    	        int itemId = item.getItemId();
				if (itemId == R.id.markAllitem) {
					if(ReaderApp.getSettings().AskBeforeMarkAllAsRead){
						AlertDialog dialog = new AlertDialog.Builder(getActivity())  
					     .setIcon(android.R.drawable.btn_star_big_on)  
					     .setTitle(getActivity().getResources().getString(R.string.feed_markallasread))	                         
					     .setPositiveButton(getActivity().getResources().getString(R.string.yes), new OnClickListener(){
					    	 	@Override
								public void onClick(DialogInterface dialog, int which) {
					    	 		markTag(c, RssAction.AllAsRead);
								}
							})
					     .setNegativeButton(getActivity().getResources().getString(R.string.no),  null).create();
						
						dialog.show();
					}
					else{
						markTag(c, RssAction.AllAsRead);
					}
					return true;
//				} else if (itemId == R.id.pintostart) {
//					return true;
				} else if (itemId == R.id.setasgallery) {
					return true;
				} else if (itemId == R.id.download) {
					final Context mContext = getActivity();
					
					NotificationHelper.getDownloadDialog(mContext,c, false).show();
					
					return true;
				} else if (itemId == R.id.unsubscribe) {	
					
					final Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out);
				    animation.setAnimationListener(new AnimationListener() {
				        @Override
				        public void onAnimationStart(Animation animation) {
				        }

				        @Override
				        public void onAnimationRepeat(Animation animation) {
				        }

				        @Override
				        public void onAnimationEnd(Animation animation) {
				            markTag(c, RssAction.UnSubscribe);
				        	HeaderViewListAdapter headerViewAdapter = (HeaderViewListAdapter)listView.getAdapter();
				        	ChannelAdapter adapter = (ChannelAdapter) headerViewAdapter.getWrappedAdapter();
				        	adapter.GetData().remove(c);
				        	adapter.notifyDataSetChanged();
				        }
				    }); 
					
					int firstPosition = listView.getFirstVisiblePosition();// - listView.getHeaderViewsCount(); // This is the same as child #0
					int wantedChild = info.position - firstPosition;
					// Say, first visible position is 8, you want position 10, wantedChild will now be 2
					// So that means your view is child #2 in the ViewGroup:
					if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
					  Log.w("RssReader", "Unable to get view for desired position, because it's not being displayed on screen.");
					  return true;
					}
					// Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
					View wantedView = listView.getChildAt(wantedChild);
		    	    
					wantedView.startAnimation(animation);
					
					return true;
				} else if (itemId == R.id.moveitem) {
					// remember we need to remove the first header in listview
					final ArrayList<String> choices = new ArrayList<String>();
					List<Channel> channels = Helper.getChannels();
					for(int i= 0; i < channels.size(); i++){
						Channel t = channels.get(i);
						if(t != null && t.IsDirectory){
							choices.add(t.Title);
						}
					}
					if(Helper.findParentChannel(c) != null)
						choices.add(getActivity().getResources().getString(R.string.feed_root));
					boolean[] chsBool = new boolean[choices.size()];
					for(int i = 0; i < chsBool.length; i++){
						chsBool[i] = false;
					}
					OnMultiChoiceClickListener multiClick = new OnMultiChoiceClickListener(){
  	
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							if(isChecked){
								c.Tag = choices.get(which);
								
								
							}
						}
					};
					OnClickListener onselect = new OnClickListener() {  
					    @Override  
					    public void onClick(DialogInterface dialog, int which) {  
					    	c.Tag = choices.get(which);
					    }  
					      
					};

					AlertDialog dialog = new AlertDialog.Builder(getActivity())  
					         .setIcon(android.R.drawable.btn_star_big_on)  
					         .setTitle(getActivity().getResources().getString(R.string.feed_moveto))
					         .setMultiChoiceItems((String[]) choices.toArray(new String[0]), chsBool, multiClick)
					         //.setItems((String[]) choices.toArray(new String[0]), onselect)
					         .setPositiveButton(getActivity().getResources().getString(R.string.yes), new OnClickListener(){
					        	 	@Override
									public void onClick(DialogInterface dialog, int which) {
					        	 		markTag(c, RssAction.MoveTag);
									}
								})
					         .setNegativeButton(getActivity().getResources().getString(R.string.no),  null).create();
					dialog.show();
					return true;
				} else if (itemId == R.id.rename) {
					final EditText input = new EditText(getActivity());
					input.setId(0);
					new AlertDialog.
						Builder(getActivity()).
						setTitle(getActivity().getResources().getString(R.string.feed_rename)).
						setIcon(android.R.drawable.ic_dialog_info).
						setView(input).
						setPositiveButton(getActivity().getResources().getString(R.string.yes), new OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String value = input.getText().toString();
								
								c.Tag = value;
								
								markTag(c, RssAction.Rename);
							}
						}).
						setNegativeButton(getActivity().getResources().getString(R.string.no), null).show();
					return true;
				} else {
					return super.onContextItemSelected(item);
				}
        	}
            return super.onContextItemSelected(item);            
        } else {
            return super.onContextItemSelected(item);
        }    		
    }
    
    boolean bMenu=true;
    
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	
    	if(tab == RssTab.Home){
    		android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
            inflater.inflate(R.menu.contextmenu, (Menu) menu);
            super.onCreateContextMenu(menu, v, menuInfo);
            bMenu=true;
    	}
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        
        Log.i("RssReader","Attached need to restore tab and channel");

        mCallbacks = (Callbacks) activity;        
    }
    
//    @Override
//    public void onListItemClick(ListView listView, View view, int position, long id) {
//        super.onListItemClick(listView, view, position, id);
//        Object obj = adapter.getItem(position);        
//        mCallbacks.onItemSelected(obj);
//    }
        
    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = channelCallback;
        
        Log.i("RssReader","Detached need to save tab and channel");
    }    

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
            outState.putInt(STATE_TAB, tab.ordinal());
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        listView.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
        	listView.setItemChecked(mActivatedPosition, false);
        } else {
        	listView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
