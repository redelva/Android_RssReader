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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        	
        	SystemBarTintManager manager = new SystemBarTintManager(this);
        	manager.setStatusBarTintEnabled(true);
        	//manager.setStatusBarTintResource(R.drawable.translucent_status_bar);
        	manager.setStatusBarTintColor(Color.parseColor("#2DBD60"));
        	manager.setNavigationBarTintEnabled(true);
        	manager.setNavigationBarTintColor(Color.parseColor("#2DBD60"));
        }
	}	
}