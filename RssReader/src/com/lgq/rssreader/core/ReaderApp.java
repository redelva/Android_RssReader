package com.lgq.rssreader.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Locale;

import org.jsoup.Connection.Base;

import com.google.gson.Gson;
import com.lgq.rssreader.MainActivity;
import com.lgq.rssreader.R;
import com.lgq.rssreader.entity.Profile;
import com.lgq.rssreader.entity.RssSettings;
import com.lgq.rssreader.enums.DownloadMode;
import com.lgq.rssreader.enums.Formatter;
import com.lgq.rssreader.enums.Theme;
import com.lgq.rssreader.enums.Token;
import com.lgq.rssreader.utils.FileHelper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.view.ViewConfiguration;
import android.widget.Toast;

public class ReaderApp extends Application{
    private static Context context;
    private static RssSettings settings;
    private Locale locale;
    private static SDCardListener fontListener;
    private static SDCardListener htmlListener;
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	Locale locale = getLocaleFromPref();
    	Locale.setDefault(locale);
    	overwriteConfigurationLocale(newConfig, locale);
    	super.onConfigurationChanged(newConfig);
    }
    
    public void setLocale() {
    	Locale locale = getLocaleFromPref();
    	Locale.setDefault(locale);
    	Configuration config = getBaseContext().getResources().getConfiguration();
    	overwriteConfigurationLocale(config, locale);
    }

    private void overwriteConfigurationLocale(Configuration config, Locale locale) {
    	config.locale = locale;
    	getBaseContext().getResources()
    			.updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
    
    private Locale getLocaleFromPref(){
    	String view_lang = ReaderApp.getPreferences().getString("view_lang", "");
    	
    	if(view_lang.equals("zh_CN"))
    		return Locale.CHINA;
    	
    	if(view_lang.equals("en"))
    		return Locale.ENGLISH;
    	
    	return Locale.getDefault();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                
            	String sDStateString = android.os.Environment.getExternalStorageState();

            	if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
        			try { 
        				File SDFile = android.os.Environment.getExternalStorageDirectory();
         
        				File dir = new File(SDFile.getAbsolutePath() + Config.ERRORLOG_LOCATION);

        				if (!dir.exists()) {
        					dir.mkdirs();
        				}
        				
        				File myFile = new File(SDFile.getAbsolutePath() + Config.ERRORLOG_LOCATION + String.valueOf(System.currentTimeMillis()) + ".txt");
        				        				
        				myFile.createNewFile();

        				FileOutputStream outputStream = new FileOutputStream(myFile);
        				StringBuilder error = new StringBuilder();
        				
        				error.append("Error msg:" + paramThrowable.getMessage() + "\r\n");
        				
        				error.append("Error throwable:" + paramThrowable.toString() + "\r\n");
        				
        				for(StackTraceElement element : paramThrowable.getStackTrace()){
        					error.append(element.toString() + "\r\n");
        				}
        				
        				if(paramThrowable.getCause() != null){
        					error.append("Caused by:" + paramThrowable.getCause().toString() + "\r\n");
            				
            				error.append("Error caused:" + paramThrowable.getCause().toString() + "\r\n");
            				
            				for(StackTraceElement element : paramThrowable.getCause().getStackTrace()){
            					error.append(element.toString() + "\r\n");
            				}
        				}
        				
        				outputStream.write(error.toString().getBytes("utf-8"));
        				outputStream.close();
        				
        				Toast.makeText(ReaderApp.getAppContext(), "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_SHORT).show();
        				
        				Intent intent = new Intent(ReaderApp.getAppContext(), MainActivity.class);  
        	            PendingIntent restartIntent = PendingIntent.getActivity(ReaderApp.getAppContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);                                                 
        	            //退出程序                                          
        	            AlarmManager mgr = (AlarmManager)ReaderApp.getAppContext().getSystemService(Context.ALARM_SERVICE);    
        	            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用   
        	            System.exit(0);
        				
        			} catch (Exception e) {
        				e.printStackTrace();
        			}// end of try
    			}
            	
//            	new AlertDialog.Builder(ReaderApp.getAppContext())
//				.setTitle(ReaderApp.getAppContext().getResources().getString(R.string.app_name)) 
//				.setMessage(ReaderApp.getAppContext().getResources().getString(R.string.error))
//			 	.setPositiveButton(ReaderApp.getAppContext().getResources().getString(R.string.com_btn_ok), new OnClickListener(){
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {						
//						System.exit(0);
//				        Intent intent = new Intent(ReaderApp.getAppContext(), MainActivity.class);  
//				        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);  
//				        startActivity(intent); 
//					}
//			 	})    				 	
//			 	.show();
            }
        });
        
        ReaderApp.context = getApplicationContext();
        
        fontListener = new SDCardListener(Config.FONTS_LOCATION);
        
        fontListener.startWatching();
        
        htmlListener = new SDCardListener(Config.HTML_LOCATION);
        
        htmlListener.startWatching();
    }
    
    public static void stopListener() {
    	fontListener.stopWatching();
    	htmlListener.stopWatching();
    }

	public static Context getAppContext() {
        return ReaderApp.context;
    }
    
    public static SharedPreferences getPreferences(){
    	return ReaderApp.getAppContext().getSharedPreferences("RssReader", 0);
    }
    
    public static Profile getProfile(){
    	String profile = ReaderApp.getPreferences().getString("Profile", "");
    	
    	if(profile.length() == 0)
    		return null;
    	
    	Gson gson = new Gson();
    	
    	return gson.fromJson(profile, Profile.class);
    }
    
    public static void setProfile(Profile p){
    	Gson gson = new Gson();
    	
    	String profile = gson.toJson(p);
    	
    	ReaderApp.getPreferences().edit().putString("Profile", profile).commit();
    }
    
    public static String getToken(Token token){
    	return ReaderApp.getPreferences().getString(token.toString(), "");    	
    }
    
    public static void setToken(Token token, String val){
    	ReaderApp.getPreferences().edit().putString(token.toString(), val).commit();    	
    }
    
    public static void saveSettings()
    {
//    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//    	
//    	if(settings != null){
//    		pref.edit().putBoolean("operation_openasread", settings.MarkAsReadWhenView).commit();
//    		pref.edit().putBoolean("oepration_defaulticon", settings.UseDefaultIcon).commit();
//    		pref.edit().putBoolean("operation_roration", settings.EnableRotation).commit();
//    		pref.edit().putBoolean("operation_confirmallread", settings.ConfirmExit).commit();
//    		pref.edit().putBoolean("operation_seperatevideo", settings.EnableSeperateClip).commit();    		
//    		
//    		pref.edit().putString("format_font", String.valueOf(settings.FontSize)).commit();
//    		pref.edit().putString("format_line", String.valueOf(settings.LineHeight)).commit();
//    		pref.edit().putString("format_type", String.valueOf(settings.Formatter.ordinal())).commit();    		
//    		
//    		pref.edit().putString("cache_numbers", String.valueOf(settings.NumPerRequest)).commit();
//    		pref.edit().putBoolean("cache_wifi", settings.SyncOnlyWifi).commit();
//    		pref.edit().putBoolean("cache_image", settings.EnableCacheImage).commit();    		
//    		
//    		pref.edit().putString("view_imgnum", String.valueOf(settings.ImgLoadNum)).commit();
//    		pref.edit().putBoolean("view_showallchannel", settings.ShowAllFeeds).commit();
//    		pref.edit().putBoolean("view_showallitem", settings.ShowAllItems).commit();    		
//    		pref.edit().putBoolean("view_noimage", settings.NoImageMode).commit();    		
//    		
//    		pref.edit().putBoolean("sync_on_start", settings.AutoSync).commit();
//    		pref.edit().putBoolean("sync_shake", settings.EnableShakeToUpdate).commit();
//    		pref.edit().putBoolean("sync_vibrate", settings.EnableVibrate).commit();
//    		pref.edit().putBoolean("sync_sound", settings.EnableSound).commit();    		
//    	}
    	
    	settings = null;
    }
    
    @SuppressLint("NewApi")
	public static RssSettings getSettings(){
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    	
    	if(settings == null){
    		settings = new RssSettings();    		
    		
    		settings.MarkAsReadWhenView = pref.getBoolean("operation_openasread", true);
    		settings.UseDefaultIcon = pref.getBoolean("oepration_defaulticon", false);
    		settings.EnableRotation = pref.getBoolean("operation_roration", false);    		
    		settings.ConfirmExit = pref.getBoolean("operation_confirmallread", true);
    		settings.EnableSeperateClip = pref.getBoolean("operation_seperatevideo", true);
    		    		
    		settings.FontSize = Integer.parseInt(pref.getString("format_fontsize", "14"));
    		settings.Font = pref.getInt("format_font", 0);
    		settings.LineHeight = Integer.parseInt(pref.getString("format_line", "150"));
    		settings.Formatter = Formatter.values()[Integer.parseInt(pref.getString("format_type", "1"))];
    		
    		settings.NumPerRequest = Integer.parseInt(pref.getString("cache_numbers", "30"));
    		settings.SyncOnlyWifi = pref.getBoolean("cache_wifi", false);
    		settings.EnableCacheImage = pref.getBoolean("cache_image", false);
    		settings.CacheSize = Integer.parseInt(pref.getString("cache_size", "200"));
    		
    		settings.ImgLoadNum = Integer.parseInt(pref.getString("view_imgnum", "10"));
    		settings.ShowAllFeeds = pref.getBoolean("view_showallchannel", true);
    		settings.ShowAllItems = pref.getBoolean("view_showallitem", true);
    		settings.NoImageMode = pref.getBoolean("view_noimage", false);
    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    			settings.FullScreen = pref.getBoolean("view_fullscreen", ViewConfiguration.get(ReaderApp.context).hasPermanentMenuKey());
    		else
    			settings.FullScreen = pref.getBoolean("view_fullscreen", false);
    		settings.BackgroundColor = pref.getString("view_backgroundcolor", "#ffffffff");
    		settings.FontColor = pref.getString("view_foregroundcolor", "#ff000000");
    		settings.Brightness = pref.getInt("view_brightness", 100);
    		
    		settings.Theme = Theme.values()[pref.getInt("view_theme", 0)];
    		
    		settings.AutoSync = pref.getBoolean("sync_on_start", true);
    		settings.EnableShakeToUpdate = pref.getBoolean("sync_shake", false);
    		settings.EnableVibrate = pref.getBoolean("sync_vibrate", true);
    		settings.EnableSound = pref.getBoolean("sync_sound", true);
    		
    		settings.DownloadPolice = DownloadMode.values()[Integer.parseInt(pref.getString("download_police", "2"))];
    		settings.DownloadOnlyWifi = pref.getBoolean("download_wifi", true);    		
    		settings.DownloadPeriod = Integer.parseInt(pref.getString("download_period", "3"));
    		settings.DownloadTime = pref.getString("download_time", "18:00");
    	}    	
    	
    	return settings;
    }
}
