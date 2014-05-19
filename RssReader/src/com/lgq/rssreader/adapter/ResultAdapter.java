package com.lgq.rssreader.adapter;

import java.io.File;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lgq.rssreader.R;
import com.lgq.rssreader.cache.AsyncImageLoader;
import com.lgq.rssreader.cache.AsyncImageLoader.ImageCallback;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.entity.Result;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;

public class ResultAdapter extends BaseAdapter {
	private List<Result> list;
	private LayoutInflater mInflater;
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;

	public ResultAdapter(Context context, List<Result> records, ListView listView) {
		if(context != null){
			this.list = records;
			this.listView = listView;
			asyncImageLoader = new AsyncImageLoader();
			
			this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}		
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Result entity = list.get(position);
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.result_list_item, null);
			viewHolder.count = (TextView) convertView.findViewById(R.id.searchresult_count);
			viewHolder.title = (TextView) convertView.findViewById(R.id.searchresult_title);
		}
				
		viewHolder.count.setText(entity.SubscriptCount);
		if(!entity.IsSubscribed)
			viewHolder.title.setText(entity.Title);
		else
			viewHolder.title.setText(entity.Title + "|" + ReaderApp.getAppContext().getResources().getString(R.string.main_alreadysubscribe));
		
		if(entity.IsSubscribed){
			viewHolder.title.setTextColor(ReaderApp.getAppContext().getResources().getColor(R.color.blue));
		}

		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * �õ����
	 * 
	 * @return
	 */
	public List<Result> GetData() {
		return list;
	}
	
	/**
	 * �������
	 * 
	 * @return
	 */
	public void ResetData(List<Result> data) {
		list = data;
	}
	/**
	 * ����
	 * 
	 * @param list
	 */
	public void InsertData(List<Result> data) {
		for(Result b : data){
			if(list.contains(b))
				this.list.add(b);
		}
		Collections.sort(list,new Comparator<Result>(){
	           public int compare(Result arg0, Result arg1) {   
	               return (int)(Integer.valueOf(arg1.SubscriptCount) - Integer.valueOf(arg0.SubscriptCount));
	            }
	        }); 
		this.notifyDataSetChanged();
	}
	/**
	 * �������
	 * 
	 * @param list
	 */
	public void AddMoreData(List<Result> data) {
		for(Result b : data){
			if(!list.contains(b))
				this.list.add(b);
		}
		Collections.sort(list,new Comparator<Result>(){
	           public int compare(Result arg0, Result arg1) {   
	               return (int)(Integer.valueOf(arg1.SubscriptCount) - Integer.valueOf(arg0.SubscriptCount));
	            }
	        });
		this.notifyDataSetChanged();
	}
	
	public int getCount() {
		if (list == null) {
			return 0;
		}
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	public class ViewHolder {
		TextView count;
		TextView title;
	}	
}
