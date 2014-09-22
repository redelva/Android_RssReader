package com.lgq.rssreader.task;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.lgq.rssreader.MainActivity;
import com.lgq.rssreader.R;
import com.lgq.rssreader.cache.ImageCacher;
import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.enums.DownloadMode;
import com.lgq.rssreader.enums.Token;
import com.lgq.rssreader.formatter.BlogFormatter.RenderCompleteHandler;
import com.lgq.rssreader.formatter.ContentFormatter;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.utils.Helper;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DownloadService extends Service{
	
	public static String LASTDOWNLOADTIME = "lastDownloaTime";
	
	/** action when boot completed **/
    public final static String        ACTION_BOOT_COMPLETED  = "com.lgq.rssreader.action.ACTION_BOOT_COMPLETED";
    /** action which represents need check now **/
    public final static String        ACTION_START_DOWNLOAD  = "com.lgq.rssreader.action.ACTION_START_DOWNLOAD";
    
	@SuppressWarnings("unchecked")
	protected void startDownload() {
		Channel c = new Channel();
		c.Id = "";
		List<Channel> channels = new ArrayList<Channel>();
		channels.add(c);
		
		SharedPreferences pref = ReaderApp.getPreferences();
		
		pref.edit().putLong(LASTDOWNLOADTIME, System.currentTimeMillis()).commit();
		
		DownloadTask task = new DownloadTask(ReaderApp.getAppContext(), 30, true, false);
		//DownloadTask task = new DownloadTask(ReaderApp.getAppContext(), 5, true, false);
		
		task.execute(channels);		
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Helper.acquireWakeLock(DownloadService.this);

        String action;        
        if (intent != null && (action = intent.getAction()) != null) {
            if (ACTION_BOOT_COMPLETED.equals(action)) {
            	startAlarm();
            } else 
        	if (ACTION_START_DOWNLOAD.equals(action)) {
        		
        		logAlarmService("nothing");
        		
        		boolean bDownload = false;
        		
//        		if (Helper.isServiceRun()) {
//                	logAlarmService("service is already running");
//                	bDownload = false;
//                }
        		//else 
    			if (checkPeroidTime()) {
        			logAlarmService("service had runned before, net yet reach next time, service works fine");
        			bDownload = false;
                }
        		else if (!isNetworkAvailable()) {
        			bDownload = false;
        		}
        		else{
        			bDownload = true;
        		}
        			
    			if (bDownload) {
                	startDownload();
                }
                else{
                	Toast.makeText(getApplicationContext(), "Network failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        flags = START_STICKY; 
        
        return super.onStartCommand(intent, flags, startId);        
    }
	
	/**
     * whether period time is meet the limiatation
     * 
     * @return
     */
    private boolean checkPeroidTime() {        
        Long lastDownloaTime = ReaderApp.getPreferences().getLong("lastDownloaTime", 0);
        
        if(ReaderApp.getSettings().DownloadPolice == DownloadMode.Period){			
			return lastDownloaTime + 60 * 60 * ReaderApp.getSettings().DownloadPeriod * 1000 > System.currentTimeMillis();
		}
		else if(ReaderApp.getSettings().DownloadPolice == DownloadMode.Time){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ReaderApp.getSettings().DownloadTime.substring(0,2)));
			calendar.set(Calendar.MINUTE, Integer.parseInt(ReaderApp.getSettings().DownloadTime.substring(3,5)));
			
			Boolean result = lastDownloaTime > calendar.getTimeInMillis();
			
			if(result){
				//service had runned
				return true;
			}
			else{
				result = calendar.getTimeInMillis() > System.currentTimeMillis();
							
				if(result){
					//not reach time, need to wait
					return true;
				}else{
					//already pass the time, maybe service had been killed
					return false;
				}
			}
		}
            	
        return false;
    }

	/**
     * whether network is meet the requirements
     * 
     * @return
     */
    private boolean isNetworkAvailable() {
    	ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            return false;
        }
        
        if(ReaderApp.getSettings().DownloadOnlyWifi){
        	NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetworkInfo == null || State.CONNECTED != wifiNetworkInfo.getState()) {
                return false;
            }
        }
    	
        return true;
    }

    private void startAlarm(){
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService("alarm");
                
        Intent intent = new Intent(getApplicationContext(), DownloadReceiver.class);
        intent.setAction(ACTION_START_DOWNLOAD);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        alarmManager.cancel(pendingIntent);
        
//        long intervalMilliseconds = 0L;
//        
//        long intervalPeriod = 60 * 60 * 1000 * ReaderApp.getSettings().DownloadPeriod;
//        //long intervalPeriod = 60 * 1000;
//	
//		Time time = new Time("GMT+8");
//        time.setToNow();   
//        int year = time.year;   
//        int month = time.month + 1;
//        int day = time.monthDay;
//        int minute = time.minute;   
//        int hour = time.hour;   
//        int sec = time.second;   
//        
//        Date date = null;
//        
//        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
//        try {  
//            date = format.parse(String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + ReaderApp.getSettings().DownloadTime + ":00");              
//        } catch (ParseException e) {  
//            // TODO Auto-generated catch block  
//            e.printStackTrace();  
//        }
//        
//        long intervalTime = date.getTime() - System.currentTimeMillis();
//        
//        if(intervalTime < 0 )
//        	intervalTime = intervalTime + 24 * 60 * 60 * 1000;
        
        //if(ReaderApp.getSettings().DownloadPolice == DownloadMode.All){
        //	intervalMilliseconds = intervalTime < intervalPeriod ? intervalTime : intervalPeriod;
		//}
		//else 
        
        
        
		if(ReaderApp.getSettings().DownloadPolice == DownloadMode.Period){			
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
					AlarmManager.INTERVAL_HALF_HOUR * 2 * ReaderApp.getSettings().DownloadPeriod, pendingIntent);			
		}
		else if(ReaderApp.getSettings().DownloadPolice == DownloadMode.Time){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ReaderApp.getSettings().DownloadTime.substring(0,2)));
			calendar.set(Calendar.MINUTE, Integer.parseInt(ReaderApp.getSettings().DownloadTime.substring(3,5)));
			
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
			        AlarmManager.INTERVAL_DAY, pendingIntent);
		}
    }

    private void logAlarmService(String info){
    	String sDStateString = android.os.Environment.getExternalStorageState();

    	if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try { 
				File SDFile = android.os.Environment.getExternalStorageDirectory();
 
				File dir = new File(SDFile.getAbsolutePath() + Config.LOG_LOCATION);

				if (!dir.exists()) {
					dir.mkdirs();
				}
				
				File myFile = new File(SDFile.getAbsolutePath() + Config.LOG_LOCATION + "alarm.txt");

				if(!myFile.exists())
					myFile.createNewFile();
				
				FileOutputStream outputStream = new FileOutputStream(myFile, true);
				StringBuilder error = new StringBuilder();
				
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = df.format(Calendar.getInstance().getTime());
				
				error.append("Alarm Serive fired, start download at" + time + " with " + info + " \r\n");
				
				outputStream.write(error.toString().getBytes("utf-8"));
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}// end of try
    	}
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}