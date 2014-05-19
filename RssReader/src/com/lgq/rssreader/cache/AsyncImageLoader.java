package com.lgq.rssreader.cache;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.*;

import com.lgq.rssreader.R;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.ImageRecordDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.enums.ImageType;
import com.lgq.rssreader.utils.FileHelper;
import com.lgq.rssreader.utils.NetHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncImageLoader {
	private HashMap<ImageRecord, SoftReference<Drawable>> imageCache;
	Context curContext;
	public AsyncImageLoader() {
		curContext = ReaderApp.getAppContext();
		imageCache = new HashMap<ImageRecord, SoftReference<Drawable>>();
	}
//	/*
//	 * ֱ������ͼƬ
//	 */
//	public void loadDrawable(final Blog blog, final String imageUrl) {
//		final String folder = ImageCacher.GetImageFolder(blog);
//		final String originName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
//		final String extension = originName.split("//.")[1];
//		final String storeName = folder + UUID.randomUUID().toString();
//		
//		ImageRecord record = new ImageRecord();
//		record.Extension = extension;
//		record.OriginUrl = imageUrl;
//		record.StoredName = storeName;
//		record.TimeStamp = new Date();
//		record.Size = FileHelper.GetFileLength(storeName);
//
//		new Thread() {
//			public void run() {
//				Drawable drawable = NetHelper.loadImageFromUrlWithStore(storeName, imageUrl);
//				
//			}
//		}.start();
//	}
	/**
	 * �����ص����ز�����
	 * 
	 * @param imgType
	 * @param tag
	 * @param imageCallback
	 * @return
	 */
	public ImageRecord loadDrawable(final Blog blog, final String imageUrl) {
		if (imageUrl.trim().equals("")) {
			return null;
		}
		
//		final Handler handler = new Handler() {
//			public void handleMessage(Message message) {				
//				Object[] objs = (Object[])message.obj;
//				imageCallback.imageLoaded((Drawable)objs[0], (ImageRecord)objs[1]);
//			}
//		};
		
		final String folder = ImageCacher.GetImageFolder(blog);
		final String originName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		final String extension = originName.split("[.]").length > 1 ? originName.split("[.]")[1] : "";
		final String storeName = folder + UUID.randomUUID().toString();
		
		ImageRecord record;
		final ImageRecordDalHelper recordHelper = new ImageRecordDalHelper();
		
		if(recordHelper.Exist(imageUrl)){
			record = recordHelper.GetImageRecordEntity(imageUrl);
			File file = new File(record.StoredName);
			if (imageCache.containsKey(record)) {
				SoftReference<Drawable> softReference = imageCache.get(record);
				Drawable drawable = softReference.get();
				if (drawable != null) {
					recordHelper.SynchronyData2DB(record);
					return record;
				}
			} else if (file.exists()) {
				return record;
			}
		}else{
			Drawable drawable = NetHelper.loadImageFromUrlWithStore(storeName, imageUrl);
			
			record = new ImageRecord();
			record.Extension = extension;
			record.OriginUrl = imageUrl;
			record.BlogId = blog.BlogId;
			record.StoredName = storeName;
			record.TimeStamp = new Date();
			record.Size = FileHelper.GetFileLength(storeName);
			
			recordHelper.SynchronyData2DB(record);
			
			if (drawable != null) {
				return record;
			}
		}
		return record;		

//		new Thread() {
//			public void run() {
//				Drawable drawable = NetHelper.loadImageFromUrlWithStore(storeName, imageUrl);
//				
//				ImageRecord record = new ImageRecord();
//				record.Extension = extension;
//				record.OriginUrl = imageUrl;
//				record.StoredName = storeName;
//				record.TimeStamp = new Date();
//				record.Size = FileHelper.GetFileLength(storeName);
//				
//				recordHelper.SynchronyData2DB(record);
//				
//				if (drawable != null) {
//					imageCache.put(record, new SoftReference<Drawable>(drawable));
//					Message message = handler.obtainMessage(0, new Object[]{drawable, record});
//					handler.sendMessage(message);
//				}
//			}
//		}.start();		
	}
	
	/**
	 * �����ص����ز�����
	 * 
	 * @param imgType
	 * @param tag
	 * @param imageCallback
	 * @return
	 */
	public void loadDrawable(final String imageUrl, final ImageCallback callback) {
		if (imageUrl.trim().equals("")) {
			return;
		}
		new Thread() {
			public void run() {
				Drawable drawable = NetHelper.getBitmapFromURL(imageUrl);
				
				if(callback != null){
					callback.imageLoaded(drawable, null);
				}
			}
		}.start();
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, ImageRecord record);
	}

}