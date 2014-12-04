package com.lgq.rssreader.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.lgq.rssreader.BlogContentActivity;
import com.lgq.rssreader.MainActivity;
import com.lgq.rssreader.R;
import com.lgq.rssreader.controls.XListView;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.task.RssWidgetService.RssWidgetFactory;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.HtmlHelper;

import android.animation.AnimatorSet.Builder;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MediumRssWidgetProvider extends AppWidgetProvider{
	  
	public static final String REFRESH = "com.lgq.rssreader.task.MediumRssWidgetProvider.REFRESH";
	public static final String LEFT = "com.lgq.rssreader.task.MediumRssWidgetProvider.LEFT";
	public static final String RIGHT = "com.lgq.rssreader.task.MediumRssWidgetProvider.RIGHT";
	public static final String ITEM = "com.lgq.rssreader.task.MediumRssWidgetProvider.ITEM";
	public static final String OPTION = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";
	
    @SuppressLint("NewApi")
	@Override  
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {  
//        for(int i=0, len=appWidgetIds.length;i<len;i++){
//        	buildRemoteView(context, appWidgetManager, appWidgetIds[i]);
//        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);        
    }  
      
    @SuppressLint("NewApi")
	private void buildRemoteView(Context context, AppWidgetManager appWidgetManager, int appWidgetId){    	        
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget4_2);
        
        Channel c = null;
        Blog b = null;
        
        views.setTextViewText(R.id.widget_blog_title, context.getResources().getString(R.string.app_name));
        
        SharedPreferences prefs = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0); 
        String content = prefs.getString("Widget_Channel" + appWidgetId, ""); 
        if(content.length() > 0 ){
        	c = new Gson().fromJson(content, Channel.class);
        }
        
        if(c == null)
        	return;
        
        String blogContent = prefs.getString("Widget_Blog" + appWidgetId, "");
        
        BlogDalHelper helper = new BlogDalHelper();
        
        if(blogContent.length() > 0)
        	b = new Gson().fromJson(blogContent, Blog.class);
        else{
        	b = helper.GetBlogList(c, 1, 1, false).get(0);
        	prefs.edit().putString("Widget_Blog" + appWidgetId, new Gson().toJson(b)).commit();
        }
        
        helper.Close();
    	views.setTextViewText(R.id.widget_blog_title, b.Title);
    	views.setTextViewText(R.id.widget_blog_desc, HtmlHelper.filterHtml(b.Description));
    	if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
    		views.setTextViewTextSize(R.id.widget_blog_desc, TypedValue.COMPLEX_UNIT_DIP, 20);
    	views.setTextViewText(R.id.widget_blog_pagenation, DateHelper.getDaysBeforeNow(b.PubDate));
        
        Intent toastIntent = new Intent(context, BlogContentActivity.class);        
        toastIntent.setAction(ITEM);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        toastIntent.setData(Uri.parse(toastIntent.toUri(Intent.URI_INTENT_SCHEME)));
        Bundle extras = new Bundle();
        extras.putString(BlogContentActivity.CURRENT, new Gson().toJson(b));
        if(c != null)
        	extras.putString(BlogContentActivity.CHANNEL, new Gson().toJson(c));
        toastIntent.putExtras(extras);
        PendingIntent toastPendingIntent = PendingIntent.getActivity(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.mediumlayout, toastPendingIntent);
        
        Intent refreshIntent = new Intent(context, MediumRssWidgetProvider.class);
        refreshIntent.setAction(REFRESH);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent);
        
        Intent leftIntent = new Intent(context, MediumRssWidgetProvider.class);
        leftIntent.setAction(LEFT);
        Bundle leftExtras = new Bundle();
        leftExtras.putString(LEFT + "Blog", new Gson().toJson(b));
        leftExtras.putString(LEFT + "Channel", new Gson().toJson(c));
        leftIntent.putExtras(leftExtras);
        leftIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent leftPendingIntent = PendingIntent.getBroadcast(context, 0, leftIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_left, leftPendingIntent);
        
        Intent rightIntent = new Intent(context, MediumRssWidgetProvider.class);
        rightIntent.setAction(RIGHT);
        Bundle rightExtras = new Bundle();
        rightExtras.putString(RIGHT + "Blog", new Gson().toJson(b));
        rightExtras.putString(RIGHT + "Channel", new Gson().toJson(c));
        rightIntent.putExtras(rightExtras);
        rightIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent rightPendingIntent = PendingIntent.getBroadcast(context, 0, rightIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_right, rightPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
    @SuppressLint("NewApi")
	@Override  
    public void onReceive(final Context context, Intent intent){ 
        super.onReceive(context, intent); 
        
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName cmpName = new ComponentName(context, LargeRssWidgetProvider.class);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(cmpName);
        BlogDalHelper helper = new BlogDalHelper();
        
        final int mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        
        if(intent.getAction().equals(REFRESH)){
        	
        	FeedlyParser parser = new FeedlyParser();
        	Channel c = null;
        	SharedPreferences prefs = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0); 
	        String content = prefs.getString("Widget_Channel" + mAppWidgetId, ""); 
	        if(content.length() > 0 ){
	        	c = new Gson().fromJson(content, Channel.class);	        	
	        }
	        
	        Blog tmp = new Blog();
    		tmp.TimeStamp = 0;
    		tmp.PubDate = new Date();
        	
        	parser.getRssBlog(c, tmp, ReaderApp.getSettings().NumPerRequest, new HttpResponseHandler(){
            	@Override
            	public <Blog> void onCallback(List<Blog> blogs, boolean result, String msg, boolean hasMore){
            		if(result){
            			BlogDalHelper helper = new BlogDalHelper();
            			helper.SynchronyData2DB((List<com.lgq.rssreader.entity.Blog>) blogs);
            			helper.Close();
            			Helper.sound();
            			
            			com.lgq.rssreader.entity.Blog previous = ((List<com.lgq.rssreader.entity.Blog>) blogs).get(0);
                    	
//                    	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget4_2);
//                        
//                    	views.setTextViewText(R.id.widget_blog_title, previous.Title);
//                    	views.setTextViewText(R.id.widget_blog_desc, HtmlHelper.filterHtml(previous.Description));
//                    	if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//                    		views.setTextViewTextSize(R.id.widget_blog_desc, TypedValue.COMPLEX_UNIT_DIP, 20);
//                    	views.setTextViewText(R.id.widget_blog_pagenation, DateHelper.getDaysBeforeNow(previous.PubDate));
//                        
                    	SharedPreferences.Editor editor = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0).edit();
                    	
                    	editor.putString("Widget_Blog" + mAppWidgetId, new Gson().toJson(previous)).commit();
                    	
                    	//appWidgetManager.updateAppWidget(mAppWidgetId, views);
            			
                    	buildRemoteView(context, appWidgetManager, mAppWidgetId);
            		}else{
            			Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
            		}
            	}
            });
        	
        }else if(intent.getAction().equals(OPTION)){
        	
        	buildRemoteView(context, appWidgetManager, mAppWidgetId);
        	
        }else if(intent.getAction().equals(LEFT)){
        	
//        	String content = intent.getExtras().getString(LEFT + "Blog");
//        	
//        	Blog b = (Blog) new Gson().fromJson(content, Blog.class);
//        	
//        	content = intent.getExtras().getString(LEFT + "Channel");
//        	
//        	Channel c = (Channel) new Gson().fromJson(content, Channel.class);
        	
        	Channel c = null;
        	Blog b = null;
        	
        	SharedPreferences prefs = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0); 
            String content = prefs.getString("Widget_Channel" + mAppWidgetId, ""); 
            if(content.length() > 0 ){
            	c = new Gson().fromJson(content, Channel.class);
            }
            
            String blogContent = prefs.getString("Widget_Blog" + mAppWidgetId, "");
            
            b = new Gson().fromJson(blogContent, Blog.class);
        	        	
        	Blog previous = helper.FindBlogBy(null, "", c, b, true);        	
        	
//        	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget4_2);
//            
//        	views.setTextViewText(R.id.widget_blog_title, previous.Title);
//        	views.setTextViewText(R.id.widget_blog_desc, HtmlHelper.filterHtml(previous.Description));
//        	if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//        		views.setTextViewTextSize(R.id.widget_blog_desc, TypedValue.COMPLEX_UNIT_DIP, 20);
//        	views.setTextViewText(R.id.widget_blog_pagenation, DateHelper.getDaysBeforeNow(previous.PubDate));
            
        	SharedPreferences.Editor editor = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0).edit();
        	
        	editor.putString("Widget_Blog" + mAppWidgetId, new Gson().toJson(previous)).commit();
        	
        	buildRemoteView(context, appWidgetManager, mAppWidgetId);
        	//appWidgetManager.updateAppWidget(mAppWidgetId, views);
        	
        }else if(intent.getAction().equals(RIGHT)){
        	
//        	String content = intent.getExtras().getString(RIGHT + "Blog");
//        	
//        	Blog b = (Blog) new Gson().fromJson(content, Blog.class);
//        	
//        	content = intent.getExtras().getString(RIGHT + "Channel");
//        	
//        	Channel c = (Channel) new Gson().fromJson(content, Channel.class);
        	
        	Channel c = null;
        	Blog b = null;
        	
        	SharedPreferences prefs = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0); 
            String content = prefs.getString("Widget_Channel" + mAppWidgetId, ""); 
            if(content.length() > 0 ){
            	c = new Gson().fromJson(content, Channel.class);
            }
            
            String blogContent = prefs.getString("Widget_Blog" + mAppWidgetId, "");
            
            b = new Gson().fromJson(blogContent, Blog.class);
        	        	
        	Blog next = helper.FindBlogBy(null, "", c, b, false);        	
        	
//        	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget4_2);
//            
//        	views.setTextViewText(R.id.widget_blog_title, next.Title);
//        	views.setTextViewText(R.id.widget_blog_desc, HtmlHelper.filterHtml(next.Description));
//        	if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//        		views.setTextViewTextSize(R.id.widget_blog_desc, TypedValue.COMPLEX_UNIT_DIP, 20);
//        	views.setTextViewText(R.id.widget_blog_pagenation, DateHelper.getDaysBeforeNow(next.PubDate));
//            
        	SharedPreferences.Editor editor = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0).edit();
        	
        	editor.putString("Widget_Blog" + mAppWidgetId, new Gson().toJson(next)).commit();
        	
        	//appWidgetManager.updateAppWidget(mAppWidgetId, views);
        	
        	buildRemoteView(context, appWidgetManager, mAppWidgetId);
        	        	
        }else if(intent.getAction().equals(ITEM)){

        }        
        
        helper.Close();
    } 
}
