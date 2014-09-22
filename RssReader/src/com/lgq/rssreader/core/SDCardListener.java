package com.lgq.rssreader.core;

import com.lgq.rssreader.utils.Helper;

import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

/** 
 * sd卡上的目录创建监听器 
 * @author lgq 
 * 
 */  
public class SDCardListener extends FileObserver {  
  
    public SDCardListener(String path) {  
        /* 
         * 这种构造方法是默认监听所有事件的,如果使用super(String,int)这种构造方法， 
         * 则int参数是要监听的事件类型. 
         */  
        super(Environment.getExternalStorageDirectory().toString() + path);  
    }  
  
    @Override  
    public void onEvent(int event, String path) {         
        switch(event) {  
	        case FileObserver.ALL_EVENTS:
	            //Log.d("RssReader", "path:"+ path);
	            break;  
	        case FileObserver.CREATE:
	        case FileObserver.MOVED_FROM:
	        case FileObserver.MOVED_TO:
	        case FileObserver.DELETE:
	            //Log.d("RssReader", "path:"+ path);
	            
	            Helper.saveHtml(ReaderApp.getAppContext(), true);
	            
	            break;	        
        }
    }  
}  