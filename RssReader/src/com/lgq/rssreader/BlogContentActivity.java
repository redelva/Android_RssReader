package com.lgq.rssreader;

import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.evernote.client.android.EvernoteSession;
import com.google.gson.Gson;
import com.lgq.rssreader.MainActivity.PivotPagerAdapter;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.enums.RssTab;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ext.SatelliteMenu;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;

/**
 * An activity representing a single Blog detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link BlogContentFragment}.
 */
public class BlogContentActivity extends SherlockFragmentActivity  {
	
	/*
	 * 状态改变过的数据
	 */
	public List<Blog> Blogs;
	
	/*
	 * 状态从未读到已读或已读到未读
	 */
	public int Count;
	
	public TextView Title;
	
	private BlogContentFragment fragment;
	
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.PRODUCTION;
	
	public EvernoteSession mEvernoteSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	if(savedInstanceState != null )
        	savedInstanceState.setClassLoader(SatelliteMenu.SavedState.class.getClassLoader());
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	super.onCreate(savedInstanceState);
        		
        setContentView(R.layout.activity_blog_detail);
        
        processExtraData(null);
	}
    
    public void onNewIntent(Intent intent){
    	super.onNewIntent(intent);
    	
    	setIntent(intent);
    	
    	processExtraData(intent.getExtras());
    }
    
    private void processExtraData(Bundle savedInstanceState){
    	mEvernoteSession = EvernoteSession.getInstance(this, "redelva", "453e8a5fdee809be", EVERNOTE_SERVICE);
        ShareSDK.initSDK(this);

        Blogs = new ArrayList<Blog>();       
        
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
        fragment = new BlogContentFragment();
        fragment.setArguments(getIntent().getExtras());
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.                        
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.blog_detail_container, fragment)
                    .commit();
        }else{
        	getSupportFragmentManager().beginTransaction()
            .replace(R.id.blog_detail_container, fragment)
            .commit();
        }
    }
    
    @Override
    public void onDestroy(){
    	ShareSDK.stopSDK(this);
    	super.onDestroy();
    }
	
	@Override  
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    // Inflate the menu; this adds items to the action bar if it is present.  
		//getSupportMenuInflater().inflate(R.menu.contentmenu, menu);  
	    return true;  
	}   
        
    int oldEv = MotionEvent.ACTION_CANCEL;
    
    private String displayEvent(MotionEvent ev){
    	int events[] = {MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE,  
                MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE,  
                MotionEvent.ACTION_POINTER_DOWN,MotionEvent.ACTION_POINTER_UP,  
                MotionEvent.EDGE_TOP,MotionEvent.EDGE_BOTTOM,MotionEvent.EDGE_LEFT,MotionEvent.EDGE_RIGHT}; 
          
        String szEvents[]={"ACTION_DOWN", "ACTION_MOVE",  
        "ACTION_UP", "ACTION_MOVE", "ACTION_CANCEL", "ACTION_OUTSIDE",  
        "ACTION_POINTER_DOWN","ACTION_POINTER_UP",  
        "EDGE_TOP","EDGE_BOTTOM","EDGE_LEFT","EDGE_RIGHT"};  
        for(int i=0; i < events.length; i++)  
        {  
            if(events[i] == ev.getAction())  
            {  
                if(oldEv != ev.getAction())  
                {                      
                    //Log.i("RssReader", szEvents[i]);
                    oldEv = ev.getAction();
                	return szEvents[i];
                }  
                break;
            }  
        }
        
        return "";
    }
    
    private ArrayList<GestureOnTouchListener> onTouchListeners = new ArrayList<GestureOnTouchListener>(10);
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	for (GestureOnTouchListener listener : onTouchListeners) {
            listener.onTouch(ev);
        }
    	//displayEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
    public void registerGestureOnTouchListener(GestureOnTouchListener listener){
        onTouchListeners.add(listener);
    }
    public interface GestureOnTouchListener {
        public void onTouch(MotionEvent ev);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ) {
			
			if(fragment != null && fragment.getMenu() != null && fragment.getMenu().getVisibility() == View.GONE){
				fragment.getMenu().setVisibility(View.VISIBLE);
				fragment.getReadSetting().setVisibility(View.GONE);
				
				return true;
			}
			else{
				Intent intent = new Intent();
				Gson g = new Gson();
				StringBuilder sb = new StringBuilder();
				for(Blog b : Blogs){
					sb.append(g.toJson(b) + "____");
				}			
				
				intent.putExtra("Blogs", sb.toString());
				intent.putExtra("Count", Count);
				intent.putExtra("requestCode", 0);
				this.setResult(Activity.RESULT_OK, intent);
			}			
		}
		
		if (keyCode == KeyEvent.KEYCODE_MENU ) {
			
			if(fragment != null && fragment.getMenu() != null && fragment.getMenu().getVisibility() == View.VISIBLE){
				fragment.getMenu().toggle();
				
				return true;
			}			
		}
		
		return super.onKeyDown(keyCode, event);
    }
    
    /**
     * Called when the control returns from an activity that we launched.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (requestCode) {
        //Update UI when oauth activity returns result
        case EvernoteSession.REQUEST_CODE_OAUTH:
          if (resultCode == Activity.RESULT_OK) {
        	  if(fragment != null){
        		  fragment.ShareEvernote();
        	  }
          }
          break;
      }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                //NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
            	super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
