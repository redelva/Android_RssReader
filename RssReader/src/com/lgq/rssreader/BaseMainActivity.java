package com.lgq.rssreader;

import com.lgq.rssreader.task.DownloadService;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.NetHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class BaseMainActivity extends BaseActivity {
	
	
	private void RedirectSettingActivity() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), SettingActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("fromActivity", 0);
		intent.putExtras(bundle);

		startActivity(intent);
	}
	
	private void RedirectBlogListActivity(){

		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), BlogListActivity.class);

		startActivity(intent);
	}
}
