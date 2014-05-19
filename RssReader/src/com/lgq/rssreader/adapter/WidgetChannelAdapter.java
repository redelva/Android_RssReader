package com.lgq.rssreader.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lgq.rssreader.MainActivity;
import com.lgq.rssreader.R;
import com.lgq.rssreader.adapter.ChannelAdapter.ViewHolder;
import com.lgq.rssreader.cache.AsyncImageLoader;
import com.lgq.rssreader.entity.Channel;

public class WidgetChannelAdapter extends BaseAdapter{
	private List<Channel> list;
	private LayoutInflater mInflater;
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	// 用来控制CheckBox的选中状况
    private static HashMap<Integer,Boolean> isSelected;

	public WidgetChannelAdapter(Context context, List<Channel> list, ListView listView) {
		this.list = list;
		this.listView = listView;
		asyncImageLoader = new AsyncImageLoader();
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		isSelected = new HashMap<Integer, Boolean>();
        // 初始化数据
        initDate();
    }

    // 初始化isSelected的数据
    public void initDate(){
    	isSelected.clear();
        for(int i=0; i<list.size();i++) {
            getIsSelected().put(i,false);
        }
    }
    
    public static HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer,Boolean> isSelected) {
    	WidgetChannelAdapter.isSelected = isSelected;
    }
    
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Channel entity = list.get(position);
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.channel_list_itemwithcheckbox, null);
			viewHolder.channel_icon = (ImageView) convertView.findViewById(R.id.channel_icon);
			viewHolder.channel_title = (TextView) convertView.findViewById(R.id.channel_title);
			viewHolder.channel_select = (CheckBox) convertView.findViewById(R.id.channel_select);			
		}
		viewHolder.channel_select.setChecked(false);
		viewHolder.channel_select.setClickable(false);
		viewHolder.channel_icon.setImageResource(entity.IsDirectory ? R.drawable.folder : R.drawable.rss);
		viewHolder.channel_icon.setTag(entity);		
		viewHolder.channel_title.setText(String.valueOf(entity.Title));
				
		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * 得到数据
	 * 
	 * @return
	 */
	public List<Channel> GetData() {
		return list;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<Channel> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 重置数据
	 * 
	 * @param list
	 */
	public void ResetData(List<Channel> list) {
		this.list = list;
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
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
		public CheckBox channel_select;		
	}
}
