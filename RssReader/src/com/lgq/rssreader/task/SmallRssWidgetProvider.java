package com.lgq.rssreader.task;

import java.util.Calendar;

import com.google.gson.Gson;
import com.lgq.rssreader.BlogContentActivity;
import com.lgq.rssreader.BlogListActivity;
import com.lgq.rssreader.MainActivity;
import com.lgq.rssreader.R;
import com.lgq.rssreader.controls.XListView;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.task.RssWidgetService.RssWidgetFactory;
import com.lgq.rssreader.utils.DateHelper;

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
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SmallRssWidgetProvider extends AppWidgetProvider{
	  
	public static final String REFRESH = "com.lgq.rssreader.task.SmallRssWidgetProvider.REFRESH";
	public static final String LEFT = "com.lgq.rssreader.task.SmallRssWidgetProvider.LEFT";
	public static final String RIGHT = "com.lgq.rssreader.task.SmallRssWidgetProvider.RIGHT";
	public static final String ITEM = "com.lgq.rssreader.task.SmallRssWidgetProvider.ITEM";
	public static final String OPTION = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";
	
    @SuppressLint("NewApi")
	@Override  
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {  
//        for(int i=0, len=appWidgetIds.length;i<len;i++){
//        	buildRemoteView(context, appWidgetManager, appWidgetIds[i]);
//        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);        
    }  
      
    private void buildRemoteView(Context context, AppWidgetManager appWidgetManager, int appWidgetId){    	        
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget1_1);
        Channel c = null;
        
        SharedPreferences prefs = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0); 
        String content = prefs.getString("Widget_Channel" + appWidgetId, ""); 
        if(content.length() > 0 ){
        	c = new Gson().fromJson(content, Channel.class);
        	views.setTextViewText(R.id.widget_channel_title,  c.Title);
        	views.setTextViewText(R.id.widget_channel_count,  String.valueOf(c.UnreadCount));
        }
        
        Intent toastIntent = new Intent(context, BlogListActivity.class);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        toastIntent.setData(Uri.parse(toastIntent.toUri(Intent.URI_INTENT_SCHEME)));
        
        Bundle extras = new Bundle();
        if(c != null)
        	extras.putString(BlogListActivity.ARG_ITEM_ID, new Gson().toJson(c));
        toastIntent.putExtras(extras);
        
        PendingIntent toastPendingIntent = PendingIntent.getActivity(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.smalllayout, toastPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
    @Override  
    public void onReceive(Context context, Intent intent){ 
        super.onReceive(context, intent); 
        
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName cmpName = new ComponentName(context, MediumRssWidgetProvider.class);
        
        int mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        
        if(intent.getAction().equals(REFRESH)){
        
        	
        	
        }else if(intent.getAction().equals(OPTION)){
        	
        	buildRemoteView(context, appWidgetManager, mAppWidgetId);
        	
        }
    } 
}
