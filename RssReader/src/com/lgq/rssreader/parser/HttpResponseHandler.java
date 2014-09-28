package com.lgq.rssreader.parser;

import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.enums.RssAction;

public class HttpResponseHandler{		
	protected static final int SUCCESS_MESSAGE = 0;
    protected static final int SUCCESS_MESSAGE_MORE = 1;
    protected static final int SUCCESS_FOUR_MESSAGE = 2;    
    protected static final int SUCCESS_FOUR_MESSAGE_MORE = 3;
    protected static final int FAILURE_MESSAGE = 5;

    private Handler handler;
    
    private HandlerThread thread = new HandlerThread("自定义消息队列");    

    /**
     * Creates a new AsyncHttpResponseHandler
     */
    public HttpResponseHandler() {
    	thread.start();
    	
        // Set up a handler to post events back to the correct thread if possible
        //if(thread. != null) {
            handler = new Handler(thread.getLooper()){
                @Override
                public void handleMessage(final Message msg){
                	HttpResponseHandler.this.handleMessage(msg);
//                	new Thread(){
//                		public void run(){
//                			//HttpResponseHandler.this.handleMessage(msg);
//                		}
//                	}.start();                    
                }
            };
        //}
    }


    //
    // Callbacks to be overridden, typically anonymously
    //

    public <T> void onCallback(List<T> data, boolean result, String msg){}
    
    public <T> void onCallback(List<T> data, boolean result, String msg, boolean hasmore){}
    
    public <T> void onCallback(T data, boolean result, String msg){}
    
    public <T, K> void onCallback(List<T> tdata, List<K> kdata, boolean result, String msg){}      
    
    public <T, K> void onCallback(List<T> tdata, List<K> kdata, boolean result, String msg, boolean hasmore){}
		   
    //
    // Pre-processing of messages (executes in background threadpool thread)
    //

    protected <T> void sendSuccessMessage(List<T> data, boolean result, String msg) {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{data, result, msg}));
    }
    
    protected <T> void sendSuccessMessage(List<T> data, boolean result, String msg, boolean hasmore) {
        sendMessage(obtainMessage(SUCCESS_MESSAGE_MORE, new Object[]{data, result, msg, hasmore}));
    }
    
    protected <T, K> void sendSuccessMessage(List<T> tData, List<K> kData, boolean result, String msg) {
        sendMessage(obtainMessage(SUCCESS_FOUR_MESSAGE, new Object[]{tData, kData, result, msg}));
    }
    
    protected <T, K> void sendSuccessMessage(List<T> tData, List<K> kData, boolean result, String msg, boolean hasmore) {
        sendMessage(obtainMessage(SUCCESS_FOUR_MESSAGE_MORE, new Object[]{tData, kData, result, msg, hasmore}));
    }
        
    protected <T> void sendSuccessMessage(T data, boolean result, String msg) {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{data, result, msg}));
    }
    
    //
    // Pre-processing of messages (in original calling thread, typically the UI thread)
    //    
    
    // Methods which emulate android's Handler and Message methods
    protected <T,K> void handleMessage(Message msg) {
        Object[] response;

        switch(msg.what) {
            case SUCCESS_MESSAGE:
                response = (Object[])msg.obj;
                
                if(response[0] instanceof List)
                	onCallback((List<T>)response[0], Boolean.parseBoolean(response[1].toString()), (String)response[2]);
                else
                	onCallback((T)response[0], Boolean.parseBoolean(response[1].toString()), (String)response[2]);
                break;
            case SUCCESS_MESSAGE_MORE:
                response = (Object[])msg.obj;
                onCallback((List<T>)response[0], Boolean.parseBoolean(response[1].toString()), (String)response[2], Boolean.parseBoolean(response[3].toString()));                
                break;
            case SUCCESS_FOUR_MESSAGE:
                response = (Object[])msg.obj;
                onCallback((List<T>)response[0],(List<K>)response[1], Boolean.parseBoolean(response[2].toString()), (String)response[3]);                
                break;            
            case SUCCESS_FOUR_MESSAGE_MORE:
                response = (Object[])msg.obj;                
                onCallback((List<T>)response[0],(List<K>)response[1], Boolean.parseBoolean(response[2].toString()), (String)response[3], Boolean.parseBoolean(response[4].toString()));                
                break;
        }
    }

    protected void sendMessage(Message msg) {
        if(handler != null){
            handler.sendMessage(msg);
        } else {
            handleMessage(msg);
        }
    }

    protected Message obtainMessage(int responseMessage, Object response) {
        Message msg = null;
        if(handler != null){
            msg = this.handler.obtainMessage(responseMessage, response);
        }else{
            msg = Message.obtain();
            msg.what = responseMessage;
            msg.obj = response;
        }
        return msg;
    }

    // Interface to AsyncHttpRequest
    <T> void sendResponseMessage(List<T> data, boolean result, String msg) {
    	sendSuccessMessage(data, result, msg);
    }
    
    <T,K> void sendResponseMessage(List<T> tdata, List<K> kdata, boolean result, String msg) {
    	sendSuccessMessage(tdata, kdata, result, msg);
    }
    
    <T,K> void sendResponseMessage(List<T> tdata, List<K> kdata, boolean result, String msg, boolean hasMore) {
    	sendSuccessMessage(tdata, kdata, result, msg, hasMore);
    }
    
    <T> void sendResponseMessage(List<T> data, boolean result, String msg, boolean hasMore) {
    	sendSuccessMessage(data, result, msg, hasMore);
    }
    
    <T> void sendResponseMessage(T data, boolean result, String msg) {
    	sendSuccessMessage(data, result, msg);
    }
}