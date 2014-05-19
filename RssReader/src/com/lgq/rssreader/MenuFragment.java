package com.lgq.rssreader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.DBHelper;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.Profile;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.task.DownloadService;
import com.lgq.rssreader.task.DownloadTask;
import com.lgq.rssreader.task.ImageTask;
import com.lgq.rssreader.task.ProfileTask;
import com.lgq.rssreader.utils.FileHelper;
import com.lgq.rssreader.utils.NetHelper;
import com.lgq.rssreader.utils.NotificationHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
 
public class MenuFragment extends Fragment implements OnClickListener{
		
	MainActivity mainActivity;
	View google, download, settings,search,add,logout;
	TextView nickName;
 
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
    }
     
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	 View view = inflater.inflate(R.layout.fragment_left_menu, container, false);
    	 
    	 google = view.findViewById(R.id.google);
    	 google.setOnClickListener(this);
    	 
    	 download = view.findViewById(R.id.download);
    	 download.setOnClickListener(this);
    	 
    	 settings = view.findViewById(R.id.settings);
    	 settings.setOnClickListener(this);
    	 
    	 search = view.findViewById(R.id.search);
    	 search.setOnClickListener(this);
    	 
    	 logout = view.findViewById(R.id.logout);
    	 logout.setOnClickListener(this);
    	 
    	 add = view.findViewById(R.id.add);
    	 add.setOnClickListener(this);
    	 
    	 nickName = (TextView)view.findViewById(R.id.nickNameTextView);
    	 final ImageView head = (ImageView)view.findViewById(R.id.headImageView);
    	 final Profile p = ReaderApp.getProfile();
    	 if(p != null){
    		 //new ImageTask((ImageView)view.findViewById(R.id.headImageView)).execute(p.Picture);
				
    		 if(p.LocalPicture != null && p.LocalPicture.length() != 0){
    			 File SDFile = android.os.Environment.getExternalStorageDirectory();
    			 
    			 Bitmap bm = BitmapFactory.decodeFile(SDFile.getAbsolutePath() +  p.LocalPicture);
    			 
    			 if(bm != null)
    				 head.setImageBitmap(bm);
    			 else
    				 new ProfileTask(head).execute(p);
    		 }else{
    			 //new ImageTask(head).execute(p.Picture);
    			 new ProfileTask(head).execute(p);
    			 
    		 }
    		 
    		 nickName.setText(p.FamilyName + p.GivenName);
    	 }
    	 
         return view;
    }
	
	//set the selected status
	private void setSelected(int id){
		google.setSelected(false);
		download.setSelected(false);
		settings.setSelected(false);
		search.setSelected(false);
		add.setSelected(false);
		logout.setSelected(false);
		
		mainActivity = (MainActivity)getActivity();
		
		if (id == R.id.google) {
			google.setSelected(true);
		} else if (id == R.id.download) {
			
			mainActivity.getSlidingMenu().toggle();
			
			final Context mContext = getActivity();
			
			Channel c = new Channel();
			c.Id = "";
			
			NotificationHelper.getDownloadDialog(mContext, c, true).show();
			
			download.setSelected(true);
		} else if (id == R.id.settings) {
			settings.setSelected(true);
			Intent i = new Intent(getActivity(),SettingActivity.class);
			startActivity(i);
			
		} else if (id == R.id.search) {
			
			mainActivity.getSlidingMenu().toggle();
			
			final EditText et = new EditText(getActivity());
			new AlertDialog.Builder(getActivity()).setTitle( getActivity().getResources().getString(R.string.main_search))
			.setIcon(android.R.drawable.ic_dialog_info).setView(et)
			.setPositiveButton( getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					
					Intent i = new Intent(getActivity(),SearchActivity.class);
					i.putExtra(FeedListFragment.SEARCHTITLE, et.getText().toString());
					startActivity(i);
				}
			}).setNegativeButton( getActivity().getResources().getString(R.string.no), null).show();
			
			search.setSelected(true);			
		} else if (id == R.id.add) {
			mainActivity.getSlidingMenu().toggle();
			
			final EditText et = new EditText(getActivity());
			new AlertDialog.Builder(getActivity()).setTitle( getActivity().getResources().getString(R.string.main_add))
				.setIcon(android.R.drawable.ic_dialog_info).setView(et)
				.setPositiveButton( getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
						Intent i = new Intent(getActivity(),SubscribeActivity.class);
						
						Bundle b = new Bundle();
						b.putString(FeedListFragment.SUBSCRIBETITLE, et.getText().toString());
						i.putExtras(b);
						
						startActivity(i);
						
					}
				}).setNegativeButton( getActivity().getResources().getString(R.string.no), null).show();
		} else if (id == R.id.logout) {
			//final TextView et = new TextView(getActivity());
			//et.setText(R.string.logout_msg);
			new AlertDialog.Builder(getActivity())
			.setTitle(getActivity().getResources().getString(R.string.logout_msg))
			.setIcon(android.R.drawable.ic_dialog_info)
			//.setView(et)
			.setPositiveButton( getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					final ProgressDialog mProgressDialog = new ProgressDialog(getActivity());
			        mProgressDialog.setTitle(getActivity().getResources().getString(R.string.logout));
			        mProgressDialog.setMessage(getActivity().getResources().getString(R.string.logouting_msg));
			        mProgressDialog.setCancelable(false);
			        mProgressDialog.setCanceledOnTouchOutside(false);
			        mProgressDialog.show();
			        
			        new Thread(){
			        	@Override
			        	public void run(){
			        		//step 1 : delete sd card file 
			        		String sDStateString = android.os.Environment.getExternalStorageState();
			        		
			        		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			        			try { 
			        				File SDFile = android.os.Environment.getExternalStorageDirectory();

			        				FileHelper.DeleteFile(SDFile.getAbsolutePath() + Config.ROOT_LOCATION);
			        			}
			        			catch(Exception e){
			        				e.printStackTrace();
			        			}
			        		}
			        		
			        		//step 2: delete sqlite db
			        		DBHelper.DatabaseHelper.ClearData(mainActivity);
			        		
			        		//step 3: delete preference
			        		ReaderApp.getPreferences().edit().clear().commit();
			        		
			        		
		        			Intent intent = mainActivity.getIntent();		
		        			mainActivity.finish();
		        			startActivity(intent);
			        		
			        	}
			        }.start();
				}
			}).setNegativeButton( getActivity().getResources().getString(R.string.no), null).show();			
		} else {
			
		}
	}	
	
	@Override
    public void onClick(View v) {
		setSelected(v.getId());
    }
        
    private void switchFragment(Fragment fragment) {
        if (getActivity() == null)
            return;    
        MainActivity ra = (MainActivity) getActivity();
        ra.switchContent(fragment);               
    }
}