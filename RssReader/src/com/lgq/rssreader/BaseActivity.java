package com.lgq.rssreader;

import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;

public class BaseActivity extends FragmentActivity {
	/**
	 * 恢复
	 */
	@Override
	protected void onResume() {
		super.onResume();
//		if (!SettingActivity.getIsAutoHorizontal(this))
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		else
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
	}
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	/**
	 * 处理按键
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_SEARCH){//搜索
			Intent intent = new Intent(BaseActivity.this,SearchActivity.class);
			intent.putExtra("isShowQuitHints", false);
			startActivity(intent);
			return true;
		}else {		
			return super.onKeyDown(keyCode, event);
		}
	}
}