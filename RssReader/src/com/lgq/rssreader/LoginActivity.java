package com.lgq.rssreader;

import com.lgq.rssreader.core.ReaderApp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

public class LoginActivity extends BaseActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        LoginFragment fragment = new LoginFragment();
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.login, fragment).commit();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ) {
			if(ReaderApp.getProfile() == null)
				return false;
			else
				return super.onKeyDown(keyCode, event);
		}
		
		return super.onKeyDown(keyCode, event);
	}
}
