package com.lgq.rssreader.task;

import java.util.ArrayList;
import java.util.List;

import com.lgq.rssreader.R;
import com.lgq.rssreader.adapter.BlogAdapter;
import com.lgq.rssreader.adapter.ChannelAdapter;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.enums.RssTab;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewGroupCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * º”‘ÿ ˝æ›
 * 
 * @author walkingp
 * @date 2012-3-13
 */
public class PageTask<T> extends AsyncTask<Integer, Integer, List<T>> {
	private ListView listView;
	private ListAdapter adapter;
	private RssTab tab;
	private Context context;
	
	public PageTask(ListView listView, RssTab t, Context c){
		this.listView = listView;
		this.tab = t;
		this.context = c;
	}
	
	@Override
	protected List<T> doInBackground(Integer... params) {
		int type = params[0];
		int index = params[1];
		int size = params[2];
		
		List<T> data = new ArrayList<T>();
		
		if(type == 0){
			data = (List<T>) new BlogDalHelper().GetBlogList(tab, index, size);
		}
		
		if(type == 1){
			data = (List<T>) new BlogDalHelper().GetBlogList(tab, index, size);
		}
		
		return data;
	}
	@Override
	protected void onPostExecute(List<T> result) {
		if (listView.getCount() == 0 || result == null || result.size() == 0) {		
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View emptyView = inflater.inflate(R.layout.listview_empty, null);			
			listView.setEmptyView(emptyView);
		}
		adapter = null;
		
		if(result.get(0) instanceof Blog){
			adapter = new BlogAdapter(context, (List<Blog>) result, listView);
		}
		
		if(result.get(0) instanceof Channel){
			adapter = new ChannelAdapter(context, (List<Channel>) result, listView);
		}
		
		listView.setAdapter(adapter);
		listView.setSelection(1);
	}
	
	@Override
	protected void onPreExecute() {
		
	}
}