package com.lgq.rssreader;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.lgq.rssreader.adapter.BlogAdapter;
import com.lgq.rssreader.adapter.ChannelAdapter;
import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.dal.ImageRecordDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.enums.Token;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.RssParser;
import com.lgq.rssreader.task.DownloadService;
import com.lgq.rssreader.task.ImageTask;
import com.lgq.rssreader.utils.FileHelper;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.NotificationHelper;
import com.viewpagerindicator.TabPageIndicator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * An activity representing a list of Blogs. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BlogContentActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FeedListFragment} and the item details
 * (if present) is a {@link BlogContentFragment}.
 * <p>
 * This activity also implements the required
 * {@link FeedListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MainActivity extends SlidingFragmentActivity 
        implements FeedListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;    
    public SlidingMenu mSlidingMenu;    
    private static LinkedHashMap<String, RssTab> CONTENT;
    private Channel channel;
    private Blog blog;
    public boolean isLoaded;
    
    private HashMap<Integer, Fragment> cache = new HashMap<Integer, Fragment>();   

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        isLoaded = false;

        if (findViewById(R.id.blog_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((FeedListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.blog_list))
                    .setActivateOnItemClick(true);
        }
        
        //启动线程检测html文件是否存在，检测缓存文件大小是否删除
        new Thread(){

			@Override
			public void run() {
				Helper.saveHtml(MainActivity.this, true);
				
				//try to start service
				
				SharedPreferences pref = ReaderApp.getPreferences();
				
				boolean bStartService = ReaderApp.getProfile() != null && !pref.contains("lastDownloaTime");
				
//				if(!bStartService){
//					bStartService = !Helper.isServiceRun();
//				}
				
				if(bStartService){
					Intent i = new Intent(MainActivity.this, DownloadService.class);
	                i.setAction(DownloadService.ACTION_BOOT_COMPLETED);
	                MainActivity.this.startService(i);
				}
				
				long size = FileHelper.getImageFolderSize();
				
				if(size > ReaderApp.getSettings().CacheSize){
					
					//BuildDialogForClean();
					final EditText msg = new EditText(MainActivity.this);
			    	msg.setText(String.format(MainActivity.this.getResources().getString(R.string.cache_msg), String.valueOf(size)));
			    	AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)  
				     	.setIcon(android.R.drawable.btn_star_big_on)  
				     	.setTitle(R.string.cache_clean)
				     	.setView(msg)				     	
				     	.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new Thread(){

									@Override
									public void run() {
										//delete blog a month ago
										Calendar calendar = Calendar.getInstance();					
										calendar.add(Calendar.MONTH, -1);    //得到前一个月 
										BlogDalHelper blogHelper = new BlogDalHelper();
										ImageRecordDalHelper imgHelper = new ImageRecordDalHelper();
										List<Blog> toDeleteBlogs = blogHelper.GetBlogList(calendar.getTime());
										
										//find related imgs by blogid
										List<ImageRecord> records = imgHelper.GetImageRecordByBlog(toDeleteBlogs);
										
										String sDStateString = android.os.Environment.getExternalStorageState();
		
										if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
											try {														
												File SDFile = android.os.Environment.getExternalStorageDirectory();
												
												for(ImageRecord record: records){
													File img = new File(SDFile.getAbsolutePath() + record.StoredName);
													if(img.exists()){
														img.deleteOnExit();									
													}
												}
												
												blogHelper.DeleteBlog(toDeleteBlogs);
												imgHelper.DeleteRecords(records);
											}
											catch(Exception e){
												Log.e("RssReader", e.getMessage());
											}
										}
										
										blogHelper.Close();
										imgHelper.Close();
									}
								}.start();
							}
						})
				     	.setNegativeButton(R.string.no,  null).create();		
					dialog.show();
				}
			}
        	
        }.start();        
        
        initSlidingMenu();
        
    	initIndicatorPager();
    	
    	ImageButton btnLeft =( (ImageButton) findViewById(R.id.ivTitleBtnLeft));
        
        btnLeft.setOnClickListener(new OnClickListener(){
        	@Override
            public void onClick(View v) {
        		mSlidingMenu.toggle();
            }
        });
        
        ImageButton btnRight =((ImageButton) findViewById(R.id.ivTitleBtnRight));
        
        btnRight.setOnClickListener(new OnClickListener(){
        	@Override
            public void onClick(final View v) {        		
        		ViewPager pager = (ViewPager)findViewById(R.id.pager);
        		PivotPagerAdapter pivot = (PivotPagerAdapter)pager.getAdapter();

    			FeedListFragment channel = (FeedListFragment)pivot.getItem(RssTab.Home.ordinal());
    			
    			Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);    	        
    	        v.startAnimation(anim);
    			    	        
    			channel.loadChannel();
            }
        });
        
        if(ReaderApp.getToken(Token.AccessToken).length() == 0){
        	        	
        	Intent i = new Intent(this,LoginActivity.class);

        	startActivityForResult(i, 0);
        }        
    }
    
    private void loadChannel(){
    	ViewPager pager = (ViewPager)findViewById(R.id.pager);
		PivotPagerAdapter pivot = (PivotPagerAdapter)pager.getAdapter();

		FeedListFragment channel = (FeedListFragment)pivot.getItem(RssTab.Home.ordinal());
		
		channel.loadChannel();
		
		Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);    	        
		findViewById(R.id.ivTitleBtnRight).startAnimation(anim);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case 0:
				
				Toast.makeText(ReaderApp.getAppContext(), ReaderApp.getAppContext().getResources().getString(R.string.main_loadingdata), Toast.LENGTH_SHORT).show();
				
				loadChannel();

				break;
			
			case 1:

				Log.i("RssReader", "Need to refresh count in list");
				
				boolean needUpdate = false;
				
				if(data != null)
					needUpdate = data.getBooleanExtra("NeedUpdate", false);					
				
				if(needUpdate){
					loadChannel();
				}
				
				break;
			default:
				break;
		}
	}
    
    @Override
    protected void onResume(){
    	if(ReaderApp.getPreferences().contains("reset_local")){
    		Boolean val = ReaderApp.getPreferences().getBoolean("reset_local", false);
    		
    		if(val){
    			ReaderApp.getPreferences().edit().remove("reset_local").commit();
    			restartActivity();
    		}
    	}
    	
    	if(ReaderApp.getPreferences().contains("forceRefresh")){
    		Boolean val = ReaderApp.getPreferences().getBoolean("forceRefresh", false);
    		
    		if(val){
    			ReaderApp.getPreferences().edit().remove("forceRefresh").commit();
    			
    			loadChannel();
    		}
    	}
    	
    	super.onResume();
    }
    
    private void initIndicatorPager(){
    	CONTENT = new LinkedHashMap<String, RssTab>();
    	CONTENT.put(getString(R.string.home), RssTab.Home);
    	CONTENT.put(getString(R.string.all),RssTab.All); 
		//CONTENT.put(this.getString(R.string.recommend), 
		CONTENT.put(getString(R.string.unread), RssTab.Unread);
		CONTENT.put(getString(R.string.star), RssTab.Star); 
    	CONTENT.put(getString(R.string.gallery), RssTab.Gallery);
    	
    	FragmentPagerAdapter adapter = new PivotPagerAdapter(getSupportFragmentManager());

        ViewPager pager = (ViewPager)findViewById(R.id.pager);    	
        pager.setAdapter(adapter);        

        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        
    }
    
    class PivotPagerAdapter extends FragmentPagerAdapter {
        public PivotPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	
        	Fragment frag = null;
        	        	
        	if(cache.get(position) != null)
        		return cache.get(position);        	
        	
        	RssTab[] values = new RssTab[CONTENT.size()]; 
			CONTENT.values().toArray(values);
        	
        	switch(position){
	    		case 0:	    			
	    		case 1:
	    		case 2:
	    		case 3:
	    		//case 4:
	    			FeedListFragment home = new FeedListFragment();
	    			Bundle f = new Bundle();
	    			f.putInt(FeedListFragment.STATE_TAB, values[position].ordinal());
	    			home.setArguments(f);
	    			frag = home;
	    			break;
	    		case 4:
	    			frag = new ImageListFragment();
	    			break;
	    	}
        	
        	cache.put(position, frag);
        	
            return frag;
        }
                
        @Override
        public CharSequence getPageTitle(int position) {
        	String[] keys = new String[CONTENT.keySet().size()];
        	CONTENT.keySet().toArray(keys);
            return keys[position].toUpperCase();
        }

        @Override
        public int getCount() {
          return CONTENT.size();
        }
    }
    
    public class PivotOnClickListener implements View.OnClickListener {
        private int index = 0;

        public PivotOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
        	((ViewPager)findViewById(R.id.pager)).setCurrentItem(index);
        }
    };      
    
    private void initSlidingMenu() {
        setBehindContentView(R.layout.activity_left_menu);
        FragmentTransaction mFragementTransaction = getSupportFragmentManager().beginTransaction();
        Fragment mFrag = new MenuFragment();
        mFragementTransaction.replace(R.id.leftmenu, mFrag);
        mFragementTransaction.commit();
        mSlidingMenu = getSlidingMenu();
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setBehindOffsetRes(R.dimen.SlidingMenuOffset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN); 
        //mSlidingMenu .setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setFadeEnabled( true);
        mSlidingMenu.setBehindScrollScale(0.333f);        
   }


    /**
     * Callback method from {@link FeedListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Object obj, RssTab tab) {
    	Serializable s = null;
    	    	
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putSerializable(BlogContentFragment.ARG_ITEM_ID, s);
            BlogContentFragment fragment = new BlogContentFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.blog_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
        	
        	if(obj instanceof Blog){
        		blog = (Blog)obj;
        		s = blog;
        		
        		Intent detailIntent = new Intent(this, BlogContentActivity.class);
                Bundle arguments = new Bundle();
                arguments.putSerializable(BlogContentFragment.CURRENT, s);
                arguments.putSerializable(BlogContentFragment.ARG_TAB_ID, tab);                
                detailIntent.putExtras(arguments);
                startActivity(detailIntent);
        	}
        	if(obj instanceof Channel){
        		channel = (Channel)obj;
        		s = channel;
        		
        		Intent detailIntent = new Intent(this, BlogListActivity.class);
                Bundle arguments = new Bundle();
                arguments.putSerializable(BlogListFragment.ARG_ITEM_ID, s);
                detailIntent.putExtras(arguments);
                //startActivity(detailIntent);
                startActivityForResult(detailIntent, 1);
        	}
        }
    }
    
    public boolean IsShowQuitHints=false;
    
    private static Boolean isExit = false;
    
    public static Boolean isRootFolder = true;

    private void exitBy2Click()
    {  
        Timer tExit = null;
        if(isExit == false ) 
        {  
        	isExit = true;
        	Toast.makeText(this, getResources().getString(R.string.main_exit), Toast.LENGTH_SHORT).show();  
        	tExit = new Timer(); 
        	tExit.schedule(new TimerTask() {            
	            @Override  
	            public void run() {  
	                isExit = false;             
	            }  
	        }, 2000);        
       }else{                                                                           
           finish();  
           System.exit(0);
       }     
    }
    	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ) {
			if(!isRootFolder){
				ViewPager pager = (ViewPager)findViewById(R.id.pager);
				if(pager.getCurrentItem() == RssTab.Home.ordinal()){
					PivotPagerAdapter adapter = (PivotPagerAdapter)pager.getAdapter();
					FeedListFragment fragment = (FeedListFragment)adapter.getItem(RssTab.Home.ordinal());
					if(fragment != null && fragment.getListView() != null && fragment.getListView().getAdapter() != null){
						HeaderViewListAdapter wrap = (HeaderViewListAdapter)fragment.getListView().getAdapter();
						ChannelAdapter channelAdapter = (ChannelAdapter)wrap.getWrappedAdapter();
						channelAdapter.ResetData(Helper.getChannels());
						isRootFolder = true;
					}
				}
	    	}else{
	    		exitBy2Click();
	    	}
			
			return true;
		}else if(keyCode==KeyEvent.KEYCODE_SEARCH){
			Intent intent = new Intent(MainActivity.this,SearchActivity.class);			
			startActivity(intent);
			return true;
		}else if(keyCode==KeyEvent.KEYCODE_MENU){
			mSlidingMenu.toggle();
			
			return true;
		}else {		
			return super.onKeyDown(keyCode, event);
		}
	}    	

	 private void restartActivity() {
	     Log.d("RssReader", "Restart activity");
	     Intent intent = getIntent();
	     finish();
	     startActivity(intent);
	 }
	
	public void switchContent(Fragment fragment) { 
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();    	     	        
		ft.replace(R.id.container, fragment).commit();
		getSlidingMenu().showContent();
    }
}
