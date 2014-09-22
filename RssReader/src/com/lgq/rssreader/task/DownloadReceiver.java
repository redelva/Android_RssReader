package com.lgq.rssreader.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {    	
    	//������ʱ��
        //DownloadService.startAlarm(context);
    	
    	String action;
        if (intent != null && (action = intent.getAction()) != null) {
            if (DownloadService.ACTION_BOOT_COMPLETED.equals(action)) {
                Log.i("RssReader", "boot complete receive");
                Intent i = new Intent(context, DownloadService.class);
                i.setAction(DownloadService.ACTION_BOOT_COMPLETED);
                context.startService(i);
            } else if (DownloadService.ACTION_START_DOWNLOAD.equals(action)) {
            	Log.i("RssReader", "receive check action");
                Intent i = new Intent(context, DownloadService.class);
                i.setAction(DownloadService.ACTION_START_DOWNLOAD);
                context.startService(i);
            }
        }
    }	
}
