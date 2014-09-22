package com.lgq.rssreader.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.lgq.rssreader.BlogContentActivity;
import com.lgq.rssreader.BlogContentFragment;
import com.lgq.rssreader.BlogListFragment;
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
import com.lgq.rssreader.utils.Helper;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class LargeRssWidgetProvider extends AppWidgetProvider{
	  
	public static final String REFRESH = "com.lgq.rssreader.task.RssWidgetProvider.REFRESH";
	public static final String LEFT = "com.lgq.rssreader.task.RssWidgetProvider.LEFT";
	public static final String RIGHT = "com.lgq.rssreader.task.RssWidgetProvider.RIGHT";
	public static final String ITEM = "com.lgq.rssreader.task.RssWidgetProvider.ITEM";
	public static final String OPTION = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";
	
    @SuppressLint("NewApi")
	@Override  
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {  
        for(int i=0, len=appWidgetIds.length;i<len;i++){
        	buildRemoteView(context, appWidgetManager, appWidgetIds[i]);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);        
    }  
      
    private void buildRemoteView(Context context, AppWidgetManager appWidgetManager, int appWidgetId){    	        
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget4_4);
        views.setTextViewText(R.id.widget_blog_title, context.getResources().getString(R.string.app_name));
        
        Intent toastIntent = new Intent(context, BlogContentActivity.class);        
        toastIntent.setAction(ITEM);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        toastIntent.setData(Uri.parse(toastIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getActivity(context, 0, toastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.listView1, toastPendingIntent);
                    
        Intent intent=new Intent(context, RssWidgetService.class);
    	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);    	
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));        
        views.setRemoteAdapter(appWidgetId, R.id.listView1, intent);
        
        Intent refreshIntent = new Intent(context, LargeRssWidgetProvider.class);
        refreshIntent.setAction(REFRESH);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent);
        
        Intent leftIntent = new Intent(context, LargeRssWidgetProvider.class);
        leftIntent.setAction(LEFT);
        leftIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent leftPendingIntent = PendingIntent.getBroadcast(context, 0, leftIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_left, leftPendingIntent);
        
        Intent rightIntent = new Intent(context, LargeRssWidgetProvider.class);
        rightIntent.setAction(RIGHT);
        rightIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent rightPendingIntent = PendingIntent.getBroadcast(context, 0, rightIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_right, rightPendingIntent);

        //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView1);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }   
    
    @Override  
    public void onReceive(final Context context, Intent intent){ 
        super.onReceive(context, intent); 
        
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName cmpName = new ComponentName(context, LargeRssWidgetProvider.class);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(cmpName);
        
        int mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        
        if(intent.getAction().equals(REFRESH)){ 
        	
        	Toast.makeText(context, context.getResources().getString(R.string.content_loading), Toast.LENGTH_SHORT).show();
        	
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
            			RssWidgetFactory.CurrentPage = 1;
                		
                		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.listView1);
            		}else{
            			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            		}
            	}
            });
        	
        }else if(intent.getAction().equals(OPTION)){
        	
        }else if(intent.getAction().equals(LEFT)){
        	//buildRemoteView(context, appWidgetManager, mAppWidgetId);
        	
        	if(RssWidgetFactory.CurrentPage > 0){
        		RssWidgetFactory.CurrentPage = RssWidgetFactory.CurrentPage - 1;
        		
        		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.listView1);
        	}
        	
        }else if(intent.getAction().equals(RIGHT)){
        	//buildRemoteView(context, appWidgetManager, mAppWidgetId);
        	
        	RssWidgetFactory.CurrentPage = RssWidgetFactory.CurrentPage + 1;        	 
              
        	appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.listView1);
        	        	
        }else if(intent.getAction().equals(ITEM)){

        }
        
    } 
}
