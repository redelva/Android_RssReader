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
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;

public class ImageAdapter extends BaseAdapter {
	private List<ImageRecord> list;
	private LayoutInflater mInflater;
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;

	public ImageAdapter(Context context, List<ImageRecord> records, ListView listView) {
		if(context != null){
			this.list = records;
			this.listView = listView;
			asyncImageLoader = new AsyncImageLoader();
			
			this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}		
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		ImageRecord entity = list.get(position);
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.image_list_item, null);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);			
		}
//		String tag = entity.GetIcon();
//		if (tag.contains("?")) {// 截断?后的字符串，避免无效图片
//			tag = tag.substring(0, tag.indexOf("?"));
//		}
//
//		viewHolder.rss_cate_icon.setTag(tag);
//		Drawable cachedImage = asyncImageLoader.loadDrawable(
//				ImageCacher.EnumImageType.RssIcon, tag, new ImageCallback() {
//					public void imageLoaded(Drawable imageDrawable, String tag) {
//						Log.i("Drawable", tag);
//						ImageView imageViewByTag = (ImageView) listView
//								.findViewWithTag(tag);
//						if (imageViewByTag != null && imageDrawable != null) {
//							imageViewByTag.setImageDrawable(imageDrawable);
//						} else {
//							try {
//								imageViewByTag
//										.setImageResource(R.drawable.sample_face);
//							} catch (Exception ex) {
//
//							}
//						}
//					}
//				});
//		if (cachedImage != null) {
//			viewHolder.rss_cate_icon.setImageDrawable(cachedImage);
//		}
		File SDFile = android.os.Environment.getExternalStorageDirectory();
		
		Bitmap bm = BitmapFactory.decodeFile(SDFile.getAbsolutePath() + entity.StoredName);		
		viewHolder.image.setImageBitmap(bm);

		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * 得到数据
	 * 
	 * @return
	 */
	public List<ImageRecord> GetData() {
		return list;
	}
	
	/**
	 * 重置数据
	 * 
	 * @return
	 */
	public void ResetData(List<ImageRecord> data) {
		list = data;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<ImageRecord> data) {
		for(ImageRecord b : data){
			if(list.contains(b))
				this.list.add(b);
		}
		Collections.sort(list,new Comparator<ImageRecord>(){
	           public int compare(ImageRecord arg0, ImageRecord arg1) {   
	               return (int)(arg1.TimeStamp.getTime() - arg0.TimeStamp.getTime());
	            }
	        }); 
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
	 * 
	 * @param list
	 */
	public void AddMoreData(List<ImageRecord> data) {
		for(ImageRecord b : data){
			if(!list.contains(b))
				this.list.add(b);
		}
		Collections.sort(list,new Comparator<ImageRecord>(){
	           public int compare(ImageRecord arg0, ImageRecord arg1) {   
	               return (int)(arg1.TimeStamp.getTime() - arg0.TimeStamp.getTime());
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
		ImageView image;		
	}	
}
