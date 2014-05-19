package com.lgq.rssreader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.SyncStateDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.Result;
import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.utils.Helper;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An activity representing a single Blog detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link BlogContentFragment}.
 */
public class SubscribeActivity extends SherlockFragmentActivity 
	implements FeedListFragment.Callbacks  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_subscribe);

        // Show the Up button in the action bar.
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
        	
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = getIntent().getExtras();
            
            if(arguments.containsKey(FeedListFragment.SUBSCRIBETITLE)){
            	String key = arguments.getString(FeedListFragment.SUBSCRIBETITLE); 
            	arguments.putInt(FeedListFragment.STATE_TAB, RssTab.Subscribe.ordinal());
                
            	FeedListFragment fragment = new FeedListFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.subscribe_list_container, fragment)
                        .commit();
                
                TextView title = (TextView)findViewById(R.id.resultlist_title);
                
                title.setText(key);
            }
        }
    }

	@Override
	public void onItemSelected(Object c, RssTab tab) {
		Result r = (Result)c;
		
		final ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIcon(R.id.process);		        
        mProgressDialog.setMessage(getResources().getString(R.string.content_loading));
        mProgressDialog.show();
        
        FeedlyParser parser = new FeedlyParser();
        
        parser.addRss(r.StreamId, r.Title, new HttpResponseHandler(){
        	
        	@Override
        	public <Object> void onCallback(Object profile, boolean result, String msg){                                		
        		mProgressDialog.hide();
    			mProgressDialog.dismiss();
        		if(result)        			
        			Toast.makeText(SubscribeActivity.this, getResources().getString(R.string.main_subscribe), 10).show();
        		else
        			Toast.makeText(SubscribeActivity.this, (CharSequence) msg, 10).show();
        		
        		ReaderApp.getPreferences().edit().putBoolean("forceRefresh", true).commit();
        		
        		SubscribeActivity.this.onBackPressed();
        	}        	
        });
	}	
}
