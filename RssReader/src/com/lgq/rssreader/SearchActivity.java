package com.lgq.rssreader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;
import com.lgq.rssreader.dal.SyncStateDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.utils.Helper;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

/**
 * An activity representing a single Blog detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link BlogContentFragment}.
 */
public class SearchActivity extends BaseActivity 
	implements FeedListFragment.Callbacks  {
	
	public SearchActivity(){
		super("#00BCD5");
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_search);

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
            
            if(arguments.containsKey(FeedListFragment.SEARCHTITLE)){
            	String key = arguments.getString(FeedListFragment.SEARCHTITLE); 
            	arguments.putInt(FeedListFragment.STATE_TAB, RssTab.Search.ordinal());
                
            	FeedListFragment fragment = new FeedListFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.search_list_container, fragment)
                        .commit();
                
                TextView title = (TextView)findViewById(R.id.searchlist_title);
                
                title.setText(key);
            }
        }
    }

	@Override
	public void onItemSelected(Object c, RssTab tab) {
		Blog blog = (Blog)c;
		
		Intent detailIntent = new Intent(this, BlogContentActivity.class);
        Bundle arguments = new Bundle();
        arguments.putSerializable(BlogContentFragment.CURRENT, blog);
        arguments.putSerializable(BlogContentFragment.ARG_TAB_ID, tab);
        arguments.putSerializable(BlogContentFragment.KEYWORD,  getIntent().getExtras().getString(FeedListFragment.SEARCHTITLE));        
        detailIntent.putExtras(arguments);
        startActivity(detailIntent);
	}

	@Override
	public void onSyncComplete(Object c, RssTab tab) {
		// TODO Auto-generated method stub
		
	}	
}
