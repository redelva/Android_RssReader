package com.lgq.rssreader;

import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.lgq.rssreader.controls.SystemBarTintManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends FragmentActivity {
	
	private String m_color;
	private int m_resId;
	
	public BaseActivity(){
		m_color = "#00BCD5";
		m_resId = 0;
	}
	
	public BaseActivity(String Color){
		if(Color != null && Color.length() == 7)
			m_color = Color;
		else
			m_color = "#00BCD5";
	}
	
	public BaseActivity(int resId){
		m_resId = resId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        	
        	SystemBarTintManager manager = new SystemBarTintManager(this);
        	manager.setStatusBarTintEnabled(true);
        	if(m_resId == 0)
        		manager.setStatusBarTintColor(Color.parseColor(m_color));
        	else
        		manager.setStatusBarTintResource(m_resId);
        	manager.setNavigationBarTintEnabled(true);
        	manager.setNavigationBarTintResource(R.drawable.transparent_bg);
        }
        
        super.onCreate(savedInstanceState);
	}	
}