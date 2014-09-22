package com.lgq.rssreader.parser;

import java.util.List;

import android.content.SharedPreferences;

import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.entity.Result;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.formatter.BlogFormatter;

public abstract class RssParser {	
	public SharedPreferences Preferences;
	public abstract List<Channel> getChannels();
	public abstract void connect(String username, String password);
	public abstract void loadData(List<SyncState> states, HttpResponseHandler handler);
	public abstract void markTag(Blog blog, RssAction action, HttpResponseHandler handler);
	public abstract void markTag(Channel displayObj, RssAction action, HttpResponseHandler handler);
	public abstract void getRssBlog(Channel channel, Blog blog, int count, HttpResponseHandler handler);	
	public abstract void getFavor(String tag, Blog blog, int count, HttpResponseHandler handler);
	public abstract void addRss(String rssUrl, String searchResultTitle, HttpResponseHandler handler);
	public abstract void assignFolder(Channel folder, Channel single, HttpResponseHandler handler);
	public abstract void searchRss(String key, int page, HttpResponseHandler handler);
	public abstract void asyncDownload(Channel c, int count, HttpResponseHandler handler);    
	public abstract List<Blog> syncDownload(Channel c, int count);
}
