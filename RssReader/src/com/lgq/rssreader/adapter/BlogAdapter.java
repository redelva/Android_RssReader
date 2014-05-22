package com.lgq.rssreader.adapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lgq.rssreader.R;
import com.lgq.rssreader.cache.AsyncImageLoader;
import com.lgq.rssreader.cache.AsyncImageLoader.ImageCallback;
import com.lgq.rssreader.controls.SwipeListView;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.SyncStateDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;

public class BlogAdapter extends BaseAdapter {
	private List<Blog> list;
	private LayoutInflater mInflater;
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	private Context mContext;

	public BlogAdapter(Context context, List<Blog> blogs, ListView listView) {
		if(context != null){
			this.list = blogs;
			mContext = context;
			this.listView = listView;
			asyncImageLoader = new AsyncImageLoader();
			
			this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}		
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final Blog entity = list.get(position);
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();	
			
			if(this.listView instanceof SwipeListView){
				((SwipeListView)this.listView).closeOpenedItems();
			}
			
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.blog_list_item, null);
			viewHolder.blog_read = (ImageView) convertView.findViewById(R.id.blog_read);
			viewHolder.blog_star = (ImageView) convertView.findViewById(R.id.blog_star);
			viewHolder.blog_date = (TextView) convertView.findViewById(R.id.blog_date);
			viewHolder.blog_subtitle = (TextView) convertView.findViewById(R.id.blog_subtitle);
			viewHolder.blog_title = (TextView) convertView.findViewById(R.id.blog_title);
			viewHolder.btn_readstatus = (TextView) convertView.findViewById(R.id.btnread);
			viewHolder.btn_starstatus = (TextView) convertView.findViewById(R.id.btnstar);
			
			viewHolder.blog_readlistener = new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					RssAction action = entity.IsRead ? RssAction.AsUnread : RssAction.AsRead;
					entity.IsRead = !entity.IsRead; 
					markTag(entity, action);
					
					TextView btn = (TextView)v;
					ImageView img = (ImageView)btn.getTag(R.id.tag_first);
					TextView title = (TextView)btn.getTag(R.id.tag_second);
					
					if(entity.IsRead){
						img.setVisibility(View.VISIBLE);
						img.setImageResource(R.drawable.keepread);
						title.setTextColor(Color.GRAY);
						btn.setText(R.string.blog_setunread);
						Drawable drawable = mContext.getResources().getDrawable(R.drawable.setunread);
						drawable.setBounds(btn.getCompoundDrawables()[0].getBounds());
						btn.setCompoundDrawables(drawable, null, null, null);
					}
					else{
						title.setTextColor(Color.BLACK);
						img.setVisibility(View.GONE);
						btn.setText(R.string.blog_setread);
						Drawable drawable = mContext.getResources().getDrawable(R.drawable.setread);
						drawable.setBounds(btn.getCompoundDrawables()[0].getBounds());
						btn.setCompoundDrawables(drawable, null, null, null);
					}					
				}
			};
			viewHolder.blog_starlistener = new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					RssAction action = entity.IsStarred ? RssAction.AsUnstar : RssAction.AsUnstar;
					entity.IsStarred = !entity.IsStarred;
					markTag(entity, action);
					
					TextView btn = (TextView)v;
					ImageView img = (ImageView)btn.getTag();
					
					if(entity.IsStarred){
						img.setVisibility(View.VISIBLE);
						img.setImageResource(R.drawable.star);
						btn.setText(R.string.blog_setunstar);
						Drawable drawable = mContext.getResources().getDrawable(R.drawable.setstar);
						drawable.setBounds(btn.getCompoundDrawables()[0].getBounds());
						btn.setCompoundDrawables(drawable, null, null, null);
					}
					else{
						img.setVisibility(View.GONE);
						btn.setText(R.string.blog_setstar);
						btn.setCompoundDrawables(mContext.getResources().getDrawable(R.drawable.setunstar), null, null, null);
						Drawable drawable = mContext.getResources().getDrawable(R.drawable.setunstar);
						drawable.setBounds(btn.getCompoundDrawables()[0].getBounds());
						btn.setCompoundDrawables(drawable, null, null, null);
					}					
				}
			};
			
			viewHolder.btn_readstatus.setTag(R.id.tag_first, viewHolder.blog_read);
			viewHolder.btn_readstatus.setTag(R.id.tag_second, viewHolder.blog_title);
			viewHolder.btn_starstatus.setTag(viewHolder.blog_star);
			viewHolder.btn_readstatus.setOnClickListener(viewHolder.blog_readlistener);
			viewHolder.btn_starstatus.setOnClickListener(viewHolder.blog_starlistener);
		}

		viewHolder.blog_date.setText(DateHelper.getDaysBeforeNow(entity.PubDate).toString());
		
		if(entity.IsRead){
			viewHolder.blog_read.setVisibility(View.VISIBLE);
			viewHolder.blog_read.setImageResource(R.drawable.keepread);
			viewHolder.blog_title.setTextColor(Color.GRAY);
			viewHolder.btn_readstatus.setText(R.string.blog_setunread);
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.setunread);
			drawable.setBounds(viewHolder.btn_readstatus.getCompoundDrawables()[0].getBounds());
			viewHolder.btn_readstatus.setCompoundDrawables(drawable, null, null, null);			
		}
		else{
			viewHolder.blog_title.setTextColor(Color.BLACK);
			viewHolder.blog_read.setVisibility(View.GONE);
			viewHolder.btn_readstatus.setText(R.string.blog_setread);
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.setread);
			drawable.setBounds(viewHolder.btn_readstatus.getCompoundDrawables()[0].getBounds());
			viewHolder.btn_readstatus.setCompoundDrawables(drawable, null, null, null);		
		}
		if(entity.IsStarred){
			viewHolder.blog_star.setVisibility(View.VISIBLE);
			viewHolder.blog_star.setImageResource(R.drawable.star);
			viewHolder.btn_starstatus.setText(R.string.blog_setunstar);
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.setunstar);
			drawable.setBounds(viewHolder.btn_starstatus.getCompoundDrawables()[0].getBounds());
			viewHolder.btn_starstatus.setCompoundDrawables(drawable, null, null, null);
		}
		else{
			viewHolder.blog_star.setVisibility(View.GONE);
			viewHolder.btn_starstatus.setText(R.string.blog_setstar);
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.setstar);
			drawable.setBounds(viewHolder.btn_starstatus.getCompoundDrawables()[0].getBounds());
			viewHolder.btn_starstatus.setCompoundDrawables(drawable, null, null, null);
		}
		viewHolder.blog_title.setText(String.valueOf(entity.Title));
		viewHolder.blog_subtitle.setText(String.valueOf(entity.SubsTitle));

		convertView.setTag(viewHolder);
		return convertView;
	}
	
	private void markTag(final Blog b, RssAction action){
    	final FeedlyParser feedly = new FeedlyParser();
		
		feedly.markTag(b, action, new HttpResponseHandler(){
        	@Override
        	public <RssAction> void onCallback(RssAction action, boolean result, String msg){
        		if(!result){
        			Log.i("RssReader", msg);
        			
        			SyncStateDalHelper helper = new SyncStateDalHelper();
        			
        			List<SyncState> states = new ArrayList<SyncState>();
        			        			
        			SyncState s = new SyncState();
        			
        			s.BlogOriginId = b.BlogId;
        			s.Status = (com.lgq.rssreader.enums.RssAction) action;
        			s.TimeStamp = new Date();
        			
        			states.add(s);
        			
        			helper.SynchronyData2DB(states);
        		}else{
        			Toast.makeText(ReaderApp.getAppContext(), msg, 10).show();
        		}
        	}
        });
    }   

	/**
	 * �õ����
	 * 
	 * @return
	 */
	public List<Blog> GetData() {
		return list;
	}
	
	/**
	 * �������
	 * 
	 * @return
	 */
	public void ResetData(List<Blog> data) {
		list = data;
	}
	/**
	 * ����
	 * 
	 * @param list
	 */
	public void InsertData(List<Blog> data) {
		for(Blog b : data){
			if(list.contains(b))
				this.list.add(b);
		}
		Collections.sort(list,new Comparator<Blog>(){
	           public int compare(Blog arg0, Blog arg1) {   
	        	   return (int) (arg1.PubDate.getTime() - arg0.PubDate.getTime() + arg1.TimeStamp - arg0.TimeStamp);
	            }
	        }); 
		this.notifyDataSetChanged();
	}
	/**
	 * �������
	 * 
	 * @param list
	 */
	public void AddMoreData(List<Blog> data) {
		for(Blog b : data){
			if(!list.contains(b))
				this.list.add(b);
		}
		Collections.sort(list,new Comparator<Blog>(){
	           public int compare(Blog arg0, Blog arg1) {   
	        	   return (int) (arg1.PubDate.getTime() - arg0.PubDate.getTime() + arg1.TimeStamp - arg0.TimeStamp);
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
		public ImageView blog_star;
		public ImageView blog_read;
		TextView btn_starstatus;
		TextView btn_readstatus;		
		TextView blog_title;
		TextView blog_subtitle;
		TextView blog_date;
		View.OnClickListener blog_readlistener;
		View.OnClickListener blog_starlistener;
	}	
}
