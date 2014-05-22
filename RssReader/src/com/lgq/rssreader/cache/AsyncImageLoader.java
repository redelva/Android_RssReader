package com.lgq.rssreader.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
	private HashMap<String, SoftReference<Drawable>> imageCache;
	Context curContext;
	private BlockingQueue<Runnable> queue = null;  
    private ThreadPoolExecutor executor = null; 
	
	public AsyncImageLoader() {
		curContext = ReaderApp.getAppContext();
		imageCache = new HashMap<String, SoftReference<Drawable>>();
		queue = new LinkedBlockingQueue<Runnable>();  
        /** 
         * 线程池维护线程的最少数量2 <br> 
         * 线程池维护线程的最大数量10<br> 
         * 线程池维护线程所允许的空闲时间180秒 
         */  
        executor = new ThreadPoolExecutor(2, 10, 180, TimeUnit.SECONDS, queue);
	}

	public Drawable loadDrawable(final Context context, final String imageUrl, final ImageCallback imageCallback) {  
        if (imageCache.containsKey(imageUrl)) {  
            SoftReference<Drawable> softReference = imageCache.get(imageUrl);  
            Drawable drawable = softReference.get();  
            if (drawable != null) {  
                return drawable;  
            }  
        }  
  
        final Handler handler = new Handler() {  
            public void handleMessage(Message message) {  
                imageCallback.imageLoaded((Drawable) message.obj, imageUrl);  
            }  
        };  
  
        // 将任务添加到线程池  
        executor.execute(new Runnable() {  
            public void run() {  
                // 根据URL加载图片  
                Drawable drawable = loadImageFromUrl(context, imageUrl);  
  
                // 图片资源不为空是创建软引用  
                if (null != drawable)  
                	imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));  
  
                Message message = handler.obtainMessage(0, drawable);  
                handler.sendMessage(message);  
            }  
        });  
  
        return null;  
    }  
  
    // 网络图片先下载到本地cache目录保存，以imagUrl的图片文件名保存，如果有同名文件在cache目录就从本地加载  
    public static Drawable loadImageFromUrl(Context context, String imageUrl) {  
        Drawable drawable = null;  
  
        if (imageUrl == null)  
            return null;  
        String fileName = "";  
  
        // 获取url中图片的文件名与后缀  
        if (imageUrl != null && imageUrl.length() != 0) {  
            fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);  
        }  
  
        // 根据图片的名称创建文件（不存在：创建）  
        File file = new File(context.getCacheDir(), fileName);  
  
        // 如果在缓存中找不到指定图片则下载  
        if (!file.exists() && !file.isDirectory()) {  
            try {  
                // 从网络上下载图片并写入文件  
                FileOutputStream fos = new FileOutputStream(file);  
                InputStream is = new URL(imageUrl).openStream();  
                int data = is.read();  
                while (data != -1) {  
                    fos.write(data);  
                    data = is.read();  
                }  
                fos.close();  
                is.close();  
  
                drawable = Drawable.createFromPath(file.toString());  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        // 如果缓存中有则直接使用缓存中的图片  
        else {  
            // System.out.println(file.isDirectory() + " " + file.getName());  
            drawable = Drawable.createFromPath(file.toString());  
        }  
        return drawable;  
    }

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}

}