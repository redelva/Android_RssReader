package com.lgq.rssreader.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.lgq.rssreader.BlogContentActivity;
import com.lgq.rssreader.BlogContentFragment;
import com.lgq.rssreader.BlogListFragment;
import com.lgq.rssreader.R;
import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.Profile;
import com.lgq.rssreader.utils.DateHelper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public class RssWidgetService extends RemoteViewsService{
	private static final boolean DB = true;  
    private static final String TAG = "RssReader";  
  
    @Override  
    public RemoteViewsFactory onGetViewFactory(Intent intent) {  
        log("onGetViewFactory, intent=" + intent);
        
        int mAppWidgetId = 0;
        
        Bundle extras = intent.getExtras();
        if (extras != null) {  
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }        
        
        return new RssWidgetFactory(getApplicationContext(), intent, mAppWidgetId);  
    }  
  
    public static class RssWidgetFactory implements RemoteViewsService.RemoteViewsFactory {  
  
        private Context mContext;
        
        private Channel c;
  
        private List<Blog> mBlogs;
        
        private int mAppWidgetId;
        
        public static int CurrentPage = 1;
        
        // ����  
        public RssWidgetFactory(Context context, Intent intent, int appWidgetId) {  
            log("MyWidgetFactory");  
            mContext = context;
            
            mAppWidgetId = appWidgetId;           
            
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget4_4);
            
            SharedPreferences prefs = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0); 
	        String content = prefs.getString("Widget_Channel" + mAppWidgetId, ""); 
	        if(content.length() > 0 ){
	        	c = new Gson().fromJson(content, Channel.class);
	        	views.setTextViewText(R.id.widget_blog_title, c.Title);
	        }
	        
	        views.setTextViewText(R.id.widget_blog_pagenation, String.format(mContext.getResources().getString(R.string.widget_page), CurrentPage));	        
	        views.setRemoteAdapter(appWidgetId, R.id.listView1, intent);
	        
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);                        
        }  
  
        @Override  
        public int getCount() {  
            log("getCount");  
            return mBlogs.size();  
        }  
  
        @Override  
        public long getItemId(int position) {  
            log("getItemId");  
            return position;  
        }  
  
        // �ڵ���getViewAt�Ĺ���У���ʾһ��LoadingView��  
        // ���return null����ô������һ��Ĭ�ϵ�loadingView  
        @Override  
        public RemoteViews getLoadingView() {  
            log("getLoadingView");  
            return null;  
        }  
  
        @Override  
        public RemoteViews getViewAt(int position) {  
            log("getViewAt, position=" + position);  
            if (position < 0 || position >= getCount()) {  
                return null;  
            }
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_blog_list_item);  
            views.setTextViewText(R.id.widget_blog_subtitle, mBlogs.get(position).SubsTitle);
            if(mBlogs.get(position).IsStarred)
            	views.setImageViewResource(R.id.widget_blog_star, R.drawable.star);
            else
            	views.setViewVisibility(R.id.widget_blog_star, View.GONE);
            if(mBlogs.get(position).IsRead){
            	views.setImageViewResource(R.id.widget_blog_read, R.drawable.keepread);
            	views.setTextColor(R.id.widget_blog_title, mContext.getResources().getColor(R.color.gray));
            }
            else{
            	views.setImageViewResource(R.id.widget_blog_read, View.GONE);
            	views.setTextColor(R.id.widget_blog_title, mContext.getResources().getColor(R.color.black));
            }
            views.setTextViewText(R.id.widget_blog_date, DateHelper.getDaysBeforeNow(mBlogs.get(position).PubDate));
            views.setTextViewText(R.id.widget_blog_title, mBlogs.get(position).Title);
            
            Bundle extras = new Bundle();
            extras.putInt("EXTRA_ITEM", position);
            extras.putString(BlogContentFragment.CURRENT, new Gson().toJson(mBlogs.get(position)));
            if(c != null)
            	extras.putString(BlogContentFragment.CHANNEL, new Gson().toJson(c));
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            // Make it possible to distinguish the individual on-click
            // action of a given item
            views.setOnClickFillInIntent(R.id.widget_blog_list_item, fillInIntent);
            
            return views;  
        }  
  
        @Override  
        public int getViewTypeCount() {  
            log("getViewTypeCount");  
            return 1;  
        }  
  
        @Override  
        public boolean hasStableIds() {  
            log("hasStableIds");  
            return true;  
        }  
  
        @Override  
        public void onCreate() {  
            log("onCreate");  
        }  
  
        @Override  
        public void onDataSetChanged() {  
            log("onDataSetChanged");
            
            if(c != null)
            	mBlogs = new BlogDalHelper().GetBlogList(c, CurrentPage, ReaderApp.getSettings().NumPerRequest, ReaderApp.getSettings().ShowAllItems);
            else
            	mBlogs = new BlogDalHelper().GetTopBlogList();
        }  
  
        @Override  
        public void onDestroy() {  
            log("onDestroy");  
        }  
    }  
  
    private static void log(String log) {  
        if (DB)  
            Log.d(TAG, log);  
    }  
}