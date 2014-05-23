package com.lgq.rssreader.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lgq.rssreader.MainActivity;
import com.lgq.rssreader.R;
import com.lgq.rssreader.cache.AsyncImageLoader;
import com.lgq.rssreader.cache.AsyncImageLoader.ImageCallback;
import com.lgq.rssreader.cache.ImageCacher;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.*;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.task.ImageTask;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.NetHelper;

public class ChannelAdapter extends BaseAdapter {
	private List<Channel> list;
	private LayoutInflater mInflater;
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;

	public ChannelAdapter(Context context, List<Channel> list, ListView listView) {
		if(!ReaderApp.getSettings().ShowAllFeeds){
			this.list = new ArrayList<Channel>();
			for (Iterator it = list.iterator();it.hasNext();){
				Channel c = (Channel)it.next(); 
				if(c.UnreadCount > 0 )
					this.list.add(c);
			}
		}else{
			this.list = list;
		}		
		this.listView = listView;
		asyncImageLoader = new AsyncImageLoader();
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Channel entity = list.get(position);
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.channel_list_item, null);
			viewHolder.channel_icon = (ImageView) convertView.findViewById(R.id.channel_icon);
			viewHolder.channel_title = (TextView) convertView.findViewById(R.id.channel_title);
			viewHolder.channel_count = (TextView) convertView.findViewById(R.id.channel_count);			
		}

		viewHolder.channel_count.setText(String.valueOf(entity.UnreadCount));		
		
		if(!ReaderApp.getSettings().UseDefaultIcon)		
			viewHolder.channel_icon.setImageResource(entity.IsDirectory ? R.drawable.folder : R.drawable.rss);
		else{
			if(entity.IsDirectory)
				viewHolder.channel_icon.setImageResource(R.drawable.folder);
			else{
				if(!Helper.isChannelIconSaved(entity)){
					
					viewHolder.channel_icon.setImageResource(R.drawable.rss);
					
					String url = "https://www.google.com/s2/favicons?alt=feed&domain=" + entity.Id.replace("feed/", "");
					
					final ImageView icon = viewHolder.channel_icon;
					
					ImageTask task = new ImageTask(icon, entity);
					
					task.execute(url);
				}else{
					String sd = Environment.getExternalStorageDirectory().toString();				    
					
					Bitmap bm = BitmapFactory.decodeFile(sd + "/rssreader/profile/" + entity.Folder + ".png");
					
					viewHolder.channel_icon.setImageBitmap(bm);
				}
			}
		}
		viewHolder.channel_icon.setTag(entity);
		if(entity.IsDirectory){
			viewHolder.channel_icon.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					Channel c = (Channel)view.getTag();
					
					if(c != null && c.IsDirectory){
		        		ResetData(c.Children);
		        		MainActivity.isRootFolder = false;
		        	}
				}
			});
		}
		
		viewHolder.channel_title.setText(String.valueOf(entity.Title));

		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * 锟矫碉拷锟斤拷锟�
	 * 
	 * @return
	 */
	public List<Channel> GetData() {
		return list;
	}
	/**
	 * 锟斤拷锟斤拷
	 * 
	 * @param list
	 */
	public void InsertData(List<Channel> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 锟斤拷锟斤拷锟斤拷锟�
	 * 
	 * @param list
	 */
	public void ResetData(List<Channel> list) {
		this.list = list;
		this.notifyDataSetChanged();
	}
	/**
	 * 锟斤拷锟斤拷锟斤拷锟�
	 * 
	 * @param list
	 */
	public void AddMoreData(List<Channel> list) {
		this.list.addAll(list);
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
		ImageView channel_icon;
		TextView channel_title;
		TextView channel_count;		
	}	
}
