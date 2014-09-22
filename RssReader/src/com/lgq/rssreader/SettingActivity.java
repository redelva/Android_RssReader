package com.lgq.rssreader;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
//import com.lgq.rssreader.controls.TimePickerDialog.TimePickerDialogListener;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.enums.DownloadMode;
import com.lgq.rssreader.task.DownloadReceiver;
import com.lgq.rssreader.utils.Helper;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

@SuppressWarnings("deprecation")
public class SettingActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener, OnTimeChangedListener{
	
	TimePicker tp;
	public final static String        ACTION_START_DOWNLOAD  = "com.lgq.rssreader.action.ACTION_START_DOWNLOAD";
		
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);
        
        Map<String, ?> prefs = this.getPreferenceManager().getSharedPreferences().getAll();
        for (String preferenceName : prefs.keySet()) {
            Preference p = this.findPreference(preferenceName);
            if (p != null) {
                Object value = prefs.get(preferenceName);
                
                p.setOnPreferenceChangeListener(this);
                p.setOnPreferenceClickListener(this);
            }
        }
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
				
		return false;
	}
	
	private void restartActivity() {
		Intent intent = getIntent();		
		finish();
		startActivityForResult(intent,2);
	}

	@Override
	public boolean onPreferenceChange(Preference p, Object o) {
		
		Log.i("RssReader", p.getKey() + " " + o.toString());
		
		
		
		Editor e = this.getPreferenceManager().getSharedPreferences().edit();
		
		if(o instanceof Boolean){
			e.putBoolean(p.getKey(), (Boolean) o).commit();
		}
		
		if(o instanceof String){
			
			if(!p.getKey().equals("download_time"))				
				e.putString(p.getKey(), (String) o).commit();			
		}
		
		if(p.getKey().equals("download_police")){
			
			AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService("alarm");
            
	        Intent intent = new Intent(getApplicationContext(), DownloadReceiver.class);
	        intent.setAction(ACTION_START_DOWNLOAD);

	        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
	        alarmManager.cancel(pendingIntent);
			
			if(Integer.parseInt(o.toString()) == DownloadMode.Period.ordinal()){
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
						AlarmManager.INTERVAL_HALF_HOUR * 2 * ReaderApp.getSettings().DownloadPeriod, pendingIntent);
				
			}
			else if(Integer.parseInt(o.toString()) == DownloadMode.Time.ordinal()){					
				// Set the alarm to start at 8:30 a.m.
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ReaderApp.getSettings().DownloadTime.substring(0,2)));
				calendar.set(Calendar.MINUTE, Integer.parseInt(ReaderApp.getSettings().DownloadTime.substring(3,5)));
				
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				        AlarmManager.INTERVAL_DAY, pendingIntent);
			}
		}
		
		ReaderApp.saveSettings();
		
		if(p.getKey().equals("view_lang")){
			
			ReaderApp.getPreferences().edit().putString("view_lang",(String)o).commit();
			ReaderApp.getPreferences().edit().putBoolean("reset_local", true).commit();
			((ReaderApp) getApplication()).setLocale();
			restartActivity();
			
//			if(((String)o).equals("zh-CN"))
//				Helper.switchLanguage( Locale.CHINESE);
//			if(((String)o).equals("en"))
//				Helper.switchLanguage(Locale.ENGLISH);
//			
//			 finish();
//			 //Intent i = new Intent("com.lgq.rssreader.task.language");
//             //sendBroadcast(i);
//			 Intent intent = new Intent();
//             intent.setClass(SettingActivity.this,SettingActivity.class);
//             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//             startActivity(intent);
		}
		
		return true;
	}

	@Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

        if(view.getId() == R.id.preference_downloadTime) {
        	Log.i("STATIC", "hour "+hourOfDay+"minute "+minute);
        	
        	Editor e = this.getPreferenceManager().getSharedPreferences().edit();
        	
        	e.putString("download_time", hourOfDay + ":" + minute).commit();
        }
    }
		
	@Override
	public boolean onPreferenceClick(Preference preference) {

        if(preference.getKey().equals("download_time")){
              
            Dialog dialog=((EditTextPreference)preference).getDialog();  

            tp = (TimePicker)dialog.findViewById(R.id.preference_downloadTime);  
  
            String time = this.getPreferenceManager().getSharedPreferences().getString(preference.getKey(), "17:00");
            
            tp.setCurrentHour(Integer.parseInt(time.split(":")[0]));
            tp.setCurrentMinute(Integer.parseInt(time.split(":")[1]));
            
            tp.setOnTimeChangedListener(this);
            
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } 
		
		return false;
	}
}
