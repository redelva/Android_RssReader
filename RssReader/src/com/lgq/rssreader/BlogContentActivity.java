package com.lgq.rssreader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;
import com.google.gson.Gson;
import com.lgq.rssreader.MainActivity.PivotPagerAdapter;
import com.lgq.rssreader.R.drawable;
import com.lgq.rssreader.controls.GestureListener;
import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ManualResetEvent;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.dal.ImageRecordDalHelper;
import com.lgq.rssreader.dal.SyncStateDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.enums.Theme;
import com.lgq.rssreader.formatter.CacheEventArgs;
import com.lgq.rssreader.formatter.ContentFormatter;
import com.lgq.rssreader.formatter.DescriptionFormatter;
import com.lgq.rssreader.formatter.BlogFormatter.CacheCompleteHandler;
import com.lgq.rssreader.formatter.BlogFormatter.FlashCompleteHandler;
import com.lgq.rssreader.formatter.BlogFormatter.RenderCompleteHandler;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.HtmlHelper;
import com.lgq.rssreader.utils.NotificationHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ext.SatelliteMenu;
import android.view.ext.SatelliteMenuItem;
import android.view.ext.SatelliteMenu.SateliteClickedListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
	
	/**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";    
    public static final String CURRENT = "current";    
    public static final String CHANNEL = "channel";
    public static final String KEYWORD = "keyword";
    
    /**
     * This is usede to present iframe.
     */
    private boolean isPageLoaded;
    
    private ContentFormatter content;
    private DescriptionFormatter desc;
    
    GestureListener simpleListner;
    GestureDetector gesture;
    ScaleGestureDetector scaleDetector; 

    /**
     * The blog this fragment is presenting.
     */    
    private Blog current;    
    
    /**
     * Read Settings.
     */
    private View readSetting;
    
    public View getReadSetting(){return readSetting;}
    
    /**
     * The fragment argument representing the rss tab that this fragment
     * represents.
     */
    public static final String ARG_TAB_ID = "tsstab";
    
    /**
     * The fragment argument representing the uioptions
     */
    public static final String UI_OPTIONS = "uioptions";
    
    /**
     * The data source of this fragment represents.
     */
    public RssTab from;
    
    /**
     * The Channel this fragment is presenting.
     */
    private Channel channel;
    
    private ImageView processImage;
    private TextView processMsg;
    private View processContainer;
    private ProgressDialog mProgressDialog;
    
    /**
     * The view container
     */
    private List<View> views;
    
    /**
     * Identify the IMMERSIVE mode status
     */
    private boolean immersiveMode;
        
    /**
     * Identify the initial status
     */
    private int uiOptions;
    
    /**
     * The WebView of this fragment to present.
     */
    private View view;
    private WebView browser;
    private TextView blogTitle;
    private SatelliteMenu menu;
    //private BlogDalHelper helper;
    
    public SatelliteMenu getMenu(){return menu;}
    
    private ManualResetEvent loadEvent;
    private ManualResetEvent jsEvent;
	
	public TextView Title;		
	
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.PRODUCTION;
	
	public EvernoteSession mEvernoteSession;
	
 	private static final int CONTENT = 1;
 	private static final int DESC = 2;
 	private static final int FLASH = 3;
 	private static final int SHARE = 4;
 	private static final int SHAKE = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {    	
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);    	
    	super.onCreate(savedInstanceState);
        		
        setContentView(R.layout.activity_blog_detail);
        initData();
        initViews();        
        initFormatter();
    }
    
    private void initData(){
    	processExtraData(null);
    	
    	if(getIntent().getExtras().containsKey(CURRENT)){
			if(getIntent().getExtras().get(CURRENT) instanceof String){
				current = new Gson().fromJson(getIntent().getExtras().getString(CURRENT), Blog.class);
			}
			if(getIntent().getExtras().get(CURRENT) instanceof Blog){
				current = (Blog)getIntent().getExtras().get(CURRENT);
			}
		}
		if(getIntent().getExtras().containsKey(CHANNEL)){    		
			if(getIntent().getExtras().get(CHANNEL) instanceof String){
				channel = new Gson().fromJson(getIntent().getExtras().getString(CHANNEL), Channel.class);
			}
			if(getIntent().getExtras().get(CHANNEL) instanceof Channel){
				channel = (Channel)getIntent().getExtras().get(CHANNEL);
			}
		}
		
		if(getIntent().getExtras().containsKey(ARG_TAB_ID))
			from = (RssTab)getIntent().getExtras().get(ARG_TAB_ID);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
    	super.onRestoreInstanceState(savedInstanceState);
    	//restore from savedInstanceState
        if(savedInstanceState != null && savedInstanceState.containsKey(CURRENT))
        	current = (Blog) savedInstanceState.getSerializable(CURRENT);
        
        if(savedInstanceState != null && savedInstanceState.containsKey(CHANNEL))
        	channel = (Channel) savedInstanceState.getSerializable(CHANNEL);
        
        if(savedInstanceState != null && savedInstanceState.containsKey(ARG_TAB_ID)){
        	from = (RssTab) savedInstanceState.getSerializable(ARG_TAB_ID);
        }    
	}
    
    @SuppressLint("NewApi") 
    private void initViews(){    	
    	this.registerGestureOnTouchListener(new BlogContentActivity.GestureOnTouchListener() {
            @Override
            public void onTouch(MotionEvent ev) {
            	
                gesture.onTouchEvent(ev);
                scaleDetector.onTouchEvent(ev);
                
                //if(isScrolling && ev.getAction() == MotionEvent.ACTION_UP){
                if(ev.getAction() == MotionEvent.ACTION_UP){
                	
                	simpleListner.onScrollComplete(ev);
                }
            }
        });
    	
    	menu = (SatelliteMenu)findViewById(R.id.pathmenu);
        initPathMenu();
        
        if(ReaderApp.getSettings().FullScreen){
        	//Helper.toggleImmersiveMode(getActivity());
        	uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        	Helper.hideSystemUI(this);
        	immersiveMode = true;
        }

        browser = (WebView)findViewById(R.id.browser);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.addJavascriptInterface(this, "external");
        browser.getSettings().setPluginState(PluginState.ON); 

        view = findViewById(R.id.content);
        
        simpleListner = new GestureListener(new com.lgq.rssreader.controls.GestureListener.IGestureListener() {
    		
    		@Override
    		public void onRight() {
    			Blog tmp = GetNext(current);
    			
    			if(tmp != null){
    				jsEvent.reset();
    				current = tmp;
        			new Thread(){public void run(){desc.Render(current);}}.start();
        			showProcess("");
        		}
    			else{
    				Toast.makeText(BlogContentActivity.this, BlogContentActivity.this.getResources().getString(R.string.content_nonextitem), Toast.LENGTH_SHORT).show();    				
    			}
    		}
    		
    		public void onUp(){
    			//int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();    	        
    	        //boolean isImmersiveModeEnabled =((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
    			
    	        if(ReaderApp.getSettings().FullScreen && immersiveMode){
    	        	//Helper.toggleImmersiveMode(getActivity());
    	        	Helper.showSystemUI(BlogContentActivity.this, uiOptions);
    	        	immersiveMode = false;
    	        }
    		}
    		
    		@Override
    		public void onDown(){
    			//int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();    	        
    	        //boolean isImmersiveModeEnabled =((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
    	        
    	        if(ReaderApp.getSettings().FullScreen && !immersiveMode){
    	        	//Helper.toggleImmersiveMode(getActivity());
    	        	
    	        	Helper.hideSystemUI(BlogContentActivity.this);
    	        	
    	        	immersiveMode = true;
    	        }
    		}
    		
    		@Override
    		public void onDoubleTap() {
    			if(readSetting != null && readSetting.getVisibility() == View.GONE){
	    			jsEvent.reset();
	    			
	    			initProgressDialog(true);
	    	        
	    	        new Thread(){public void run(){content.Render(current);}}.start();
    			}
    		}
    		
    		@Override
    		public void onLeft() {
    			Blog tmp = GetPrevious(current);
    			if(tmp != null){
    				jsEvent.reset();
    				current = tmp;    			
        			new Thread(){public void run(){desc.Render(current);}}.start();
        			showProcess("");
        		}
    			else{
    				Toast.makeText(BlogContentActivity.this, BlogContentActivity.this.getResources().getString(R.string.content_nopreviousitem), Toast.LENGTH_SHORT).show();
    			}
    		}
    		
    		@Override
    		public void onScale(double scale) {
    			if(scale == 1.0)
    				return;
    			
    			Log.i("RssReader", "on scale:" + scale);
    			
    			int size = (int) (ReaderApp.getSettings().FontSize * scale);
    			
    			//ReaderApp.getSettings().FontSize = size;
    			
    			Editor e = PreferenceManager.getDefaultSharedPreferences(ReaderApp.getAppContext()).edit();
    			
    			e.putString("format_fontsize", String.valueOf(size)).commit();
    			
    			ReaderApp.saveSettings();
    			
    			Helper.saveHtml(BlogContentActivity.this, true);
    			
    			browser.loadUrl("javascript: fontsize(" + size + ")");
    			
    		}
        }, 
        view, 
        (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.33),
        (int) (getWindowManager().getDefaultDisplay().getHeight() * 0.33));
        
        gesture = new GestureDetector(this, simpleListner);
        
        scaleDetector= new ScaleGestureDetector(this, simpleListner);
                
        blogTitle = (TextView)findViewById(R.id.blog_content_title);
        
        readSetting = findViewById(R.id.readSetting);
        initReadSetting();
        
        //blogTitle = (TextView)getActivity().findViewById(R.id.blog_content_title);
        blogTitle.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v) {
        		browser.loadUrl("javascript: moveToTop()");
			}
        });
        
        //rootView.setBackgroundColor(Color.BLACK);
        processImage = (ImageView)findViewById(R.id.process);    	
        processMsg = (TextView)findViewById(R.id.blog_content_msg);
        processContainer = findViewById(R.id.processContainer);
        showProcess(getResources().getString(R.string.content_loading));
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //if ( 0 != (getActivity().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) ) {
                WebView.setWebContentsDebuggingEnabled(true);
            //}
        }
        
        isPageLoaded = false;
        
        browser.setWebViewClient(new WebViewClient() {
			// Load opened URL in the application instead of standard browser
			// application
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.i("RssReader", url);
				return true;
			}
			
			public void onPageFinished(WebView view, String url) {		            
	            super.onPageFinished(view, url);
	            
	            if(url.equals("file://" + android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + 
	            		Config.HTML_LOCATION + Config.HTML_NAME) && !isPageLoaded){
	            	isPageLoaded = true;
	            	new Thread(){public void run(){desc.Render(current);}}.start();
	            }
	        }
		});
        
        browser.setOnLongClickListener(new OnLongClickListener(){	
			@Override
			public boolean onLongClick(View v) {
				
				Uri name = Uri.parse(HtmlHelper.unescape(current.Link));
    			Intent intent = new Intent(Intent.ACTION_VIEW, name);
    	        
    	        startActivity(intent);
						            	
				return true;
			}       
        });
        
        if (current != null) {	        	
        	blogTitle.setText(current.Title);
        	
        	markAsRead(current);
        	
            browser.loadUrl("file://" + android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + 
            		Config.HTML_LOCATION + Config.HTML_NAME);
        }else{
        	browser.loadDataWithBaseURL(null,"<html><h1>Empty<h1><html>","text/html", "utf-8", null);
        }
    }
    
    private void initFormatter(){
    	content = new ContentFormatter();
        content.EnableCache = ReaderApp.getSettings().EnableCacheImage;
        content.NoImageMode = ReaderApp.getSettings().NoImageMode;
        content.setCacheCompleteHandler(new CacheCompleteHandler(){
			public void onCache(Blog b, final CacheEventArgs args){
				if(args.Cache != null && args.Cache != null){
					BlogDalHelper helper = new BlogDalHelper();
					helper.SynchronyContent2DB(args.Blog.BlogId, args.Cache.outerHtml());
					helper.Close();
				}
			}
		});
		content.setRenderCompleteHandler(new RenderCompleteHandler(){
			public void onRender(Blog b, final String Content){
				b.Content = Content;				
				
    			int index = Blogs.indexOf(b);
    			
    			if(index == -1)
    				Blogs.add(b);
    			else
    				Blogs.get(index).Content = Content;
				
	    		BlogDalHelper helper = new BlogDalHelper();
				helper.SynchronyContent2DB(b.BlogId, Content);
				helper.Close();
				if(b.BlogId.equals(current.BlogId))
				{
					int what = CONTENT;
					
					try {
						loadEvent.waitOne();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Message m = myHandler.obtainMessage();
		            m.what = what;
		            m.obj = Content;
					myHandler.sendMessage(m);
				}				
			}
		});
		
		content.setFlashCompleteHandler(new FlashCompleteHandler(){
			@Override
			public void onFlash(final Object sender, final CacheEventArgs cacheArgs) {
								
				int what = FLASH;
				
				try {
					jsEvent.waitOne();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Message m = myHandler.obtainMessage();
	            m.what = what;
	            m.obj = cacheArgs;
	            Bundle b = new Bundle();
	            b.putString("title", sender.toString());
	            m.setData(b);
				myHandler.sendMessage(m);
			}
		});
    
		desc = new DescriptionFormatter();
		desc.EnableCache = ReaderApp.getSettings().EnableCacheImage;
        desc.NoImageMode = ReaderApp.getSettings().NoImageMode;
		desc.setCacheCompleteHandler(new CacheCompleteHandler(){
			public void onCache(Blog b, final CacheEventArgs args){
				if(args.Blog != null && args.Cache != null){
					BlogDalHelper helper = new BlogDalHelper();				
					helper.SynchronyDescription2DB(args.Blog.BlogId, args.Cache.outerHtml());
					helper.Close();
				}				
			}		
		});
		desc.setRenderCompleteHandler(new RenderCompleteHandler(){
			public void onRender(Blog b, final String Content){
				
				b.Description = Content;
				
				int index = Blogs.indexOf(b);
    			
    			if(index == -1)
    				Blogs.add(b);
    			else
    				Blogs.get(index).Description = Content;
	    			
				BlogDalHelper helper = new BlogDalHelper();
				helper.SynchronyDescription2DB(b.BlogId, Content);
				helper.Close();

				if(b.BlogId.equals(current.BlogId))
				{
					int what = DESC;
					
					try {
						Log.i("RssReader", "Waiting for page load complete");
						
						loadEvent.waitOne();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
					Message m = myHandler.obtainMessage();
		            m.what = what;
		            m.obj = Content;
					myHandler.sendMessage(m);
				}
			}
		});
		
		desc.setFlashCompleteHandler(new FlashCompleteHandler(){
			@Override
			public void onFlash(final Object sender, final CacheEventArgs cacheArgs) {				
				int what = FLASH;
				
				try {
					jsEvent.waitOne();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Message m = myHandler.obtainMessage();
	            m.what = what;
	            m.obj = cacheArgs;
	            Bundle b = new Bundle();
	            b.putString("title", sender.toString());
	            m.setData(b);
				myHandler.sendMessage(m);
			}
		});
    }
    
    protected void hideProcess(){    	
    	runOnUiThread(new Runnable(){
			public void run(){
				processImage.clearAnimation();
		    	processMsg.setText("");
		    	processImage.setVisibility(View.GONE);
		    	processMsg.setVisibility(View.GONE);
		    	processContainer.setVisibility(View.GONE);
			}
		});
    }
    
    protected void hideProgressbar(){    	
    	runOnUiThread(new Runnable(){
			public void run(){
				if(mProgressDialog == null)
					initProgressDialog(false);
				
				mProgressDialog.hide();
				mProgressDialog.dismiss();
			}
		});
    }
    
    protected void showProcess(final String msg){
    	runOnUiThread(new Runnable(){
			public void run(){
		    	processMsg.setText(msg);
		    	processMsg.setVisibility(View.GONE);
		    	processImage.setVisibility(View.VISIBLE);
		    	processContainer.setVisibility(View.VISIBLE);
		    	processImage.setAnimation(AnimationUtils.loadAnimation(BlogContentActivity.this, R.anim.rotate));
			}
		});
    }
    
    @JavascriptInterface
    public void notifyJava(String args){
    	Log.i("RssReader", args);
    	
    	String url = "";
    	if(args.startsWith("LinkHandle")){
    		url = args.replace("LinkHandle", "");
    		
    		if(url.contains("mp4") || url.contains("f4v") || url.contains("flv")){
    			Intent intent = new Intent(Intent.ACTION_VIEW);
    	        String type = "video/mp4";
    	        Uri name = Uri.parse(HtmlHelper.unescape(url));
    	        intent.setDataAndType(name, type);
    	        
    	        startActivity(intent);
    		}    		
    		else{
    			Uri name = Uri.parse(HtmlHelper.unescape(url));
    			Intent intent = new Intent(Intent.ACTION_VIEW, name);
    	        
    	        startActivity(intent);
    		}
    	}
    	
    	if(args.startsWith("SaveToMediaLibrary")){
    		url = args.replace("SaveToMediaLibrary", "");
    		
    		URL aURL;
			try {
				aURL = new URL(url);
				URLConnection conn = aURL.openConnection();
	    		conn.connect();
	    		InputStream is = conn.getInputStream();
	    		BufferedInputStream bis = new BufferedInputStream(is);
	    		Bitmap bm = BitmapFactory.decodeStream(bis);
	    		
	    		ContentResolver cr = getContentResolver();
	    		MediaStore.Images.Media.insertImage(cr, bm, "RssReader", "this is a Photo from RssReader");
	    		
	    		Toast.makeText(this, getResources().getString(R.string.content_savetolibrary), Toast.LENGTH_SHORT).show();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	}
    	
    	if(args.equals("reload")){
    		new Thread(){public void run(){desc.Render(current);}}.start();
    	}
    }
    
    @JavascriptInterface
    public void loadComplete(String args){
    	Log.i("RssReader", "Page Load " + args);
    	
    	if(args.equals("init")){
    		loadEvent.set();
    	}
    	else if(args.equals("content")){
    		hideProgressbar();
    		
    		if(!ReaderApp.getSettings().MenuShake){    			
    			
    			Editor e = PreferenceManager.getDefaultSharedPreferences(ReaderApp.getAppContext()).edit();
    			
    			e.putBoolean("MenuShake", true).commit();
    			
    			ReaderApp.saveSettings();
    			
    			Message m = myHandler.obtainMessage();
	            m.what = SHAKE;	            
				myHandler.sendMessage(m);
    		}
    	}
    	else if(args.equals("description")){
    		hideProcess();
    		if(!ReaderApp.getSettings().MenuShake){
    			Editor e = PreferenceManager.getDefaultSharedPreferences(ReaderApp.getAppContext()).edit();
    			
    			e.putBoolean("MenuShake", true).commit();
    			
    			ReaderApp.saveSettings();
    			
    			Message m = myHandler.obtainMessage();
	            m.what = SHAKE;	            
				myHandler.sendMessage(m);
    		}
    	}
    }
    
    private void initReadSetting(){
    	final View container = findViewById(R.id.content);
    	
    	LinearLayout readfonts = (LinearLayout)findViewById(R.id.readfonts);
    	
    	List<File> fonts = Helper.detectFonts();
    	final List<TextView> texts = new ArrayList<TextView>();
    	
    	double weight = 1.0/(fonts.size() + 1);
    	int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (fonts.size() + 1) * 45, getResources().getDisplayMetrics());
    	int minHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());   
    	readfonts.getLayoutParams().height = height > minHeight ? height : minHeight;
    	
    	readfonts.requestLayout();
    	
    	for(File f : fonts){
    		TextView text = new TextView(container.getContext());
    		text.setBackgroundResource(R.drawable.textviewbg);
    		text.setTextColor(getResources().getColor(R.color.white));
    		text.setText(f.getName().substring(0, f.getName().indexOf(".")));
    		text.setGravity(Gravity.CENTER);
    		
    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
    		lp.weight = (float) weight;
    		lp.setMargins(2,2,2,2);
    		text.setLayoutParams(lp);
    		
    		readfonts.addView(text);
    		texts.add(text);
    	}
    	
    	if(fonts.size()>0){
    		TextView text = new TextView(container.getContext());
    		text.setBackgroundResource(R.drawable.textviewbg);
    		text.setTextColor(getResources().getColor(R.color.white));
    		text.setText("Droid Sans");
    		text.setGravity(Gravity.CENTER);
    		
    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
    		lp.weight = (float) weight;
    		lp.setMargins(2,2,2,2);
    		text.setLayoutParams(lp);
    		
    		readfonts.addView(text);
    		texts.add(text);
    	}else{
    		TextView text = new TextView(container.getContext());
    		text.setBackgroundResource(R.drawable.textviewbg);
    		text.setTextColor(getResources().getColor(R.color.white));
    		text.setText(R.string.blog_fontmsg);
    		text.setGravity(Gravity.CENTER);
    		
    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
    		lp.weight = (float) weight;
    		lp.setMargins(2,2,2,2);
    		text.setLayoutParams(lp);
    		
    		readfonts.addView(text);
    		texts.add(text);
    	}
    	
    	int currentFont = ReaderApp.getSettings().Font >= texts.size() ? 0 : ReaderApp.getSettings().Font; 
    	texts.get(currentFont).setText(texts.get(currentFont).getText() + " √");
    	
    	if(currentFont == texts.size() - 1 || Helper.detectFonts().size() == 0){
			blogTitle.setTypeface(null);
		}else{
			Typeface typeFace = Typeface.createFromFile(Helper.detectFonts().get(currentFont));
			blogTitle.setTypeface(typeFace);
		}
    	
    	OnClickListener familyListner = new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if(texts.size() == 1){
					return;
				}
				
				String family = "";
				int chosen = -1;
				
				int index = 0;
				for(TextView view : texts){
					if(v.equals(view)){
						family = view.getText().toString().replace(" √", "");						
						view.setText(family + " √");
						chosen = index;
					}else{						
						view.setText(view.getText().toString().replace(" √", ""));
					}
					index++;
				}
				
				if(chosen == texts.size() - 1 || Helper.detectFonts().size() == 0){
					blogTitle.setTypeface(null);
				}else{
					Typeface typeFace = Typeface.createFromFile(Helper.detectFonts().get(chosen));
					blogTitle.setTypeface(typeFace);
				}
				
				Editor e = PreferenceManager.getDefaultSharedPreferences(ReaderApp.getAppContext()).edit();
    			    			
    			e.putInt("format_font", chosen).commit();    			
    			
    			ReaderApp.saveSettings();
    			
    			Helper.saveHtml(BlogContentActivity.this, true);
								
				browser.loadUrl("javascript: fontFamily('" + family + "')");
			}
		};
    	
		for(TextView view: texts){
			view.setOnClickListener(familyListner);
		}	
    	    	
    	final TextView setting_defaultcolor = (TextView)findViewById(R.id.setting_defaultcolor);
    	final TextView setting_lightcolor = (TextView)findViewById(R.id.setting_lightcolor);
    	final TextView setting_darkcolor = (TextView)findViewById(R.id.setting_darkcolor);
    	
    	if(ReaderApp.getSettings().Theme == Theme.Default){
    		setting_defaultcolor.setText("√");
    		setting_lightcolor.setText("");
    		setting_darkcolor.setText("");
    		container.setBackgroundColor(getResources().getColor(R.color.readdefault));  
    		((RelativeLayout)container.getParent()).setBackgroundColor(getResources().getColor(R.color.readdefault));
    		blogTitle.setBackgroundColor(getResources().getColor(R.color.readdefault));
    		blogTitle.setTextColor(getResources().getColor(R.color.readfontdefault));    		
    	}else if(ReaderApp.getSettings().Theme == Theme.Light){
    		setting_defaultcolor.setText("");
    		setting_lightcolor.setText("√");
    		setting_darkcolor.setText("");
    		container.setBackgroundColor(getResources().getColor(R.color.readlight));
    		((RelativeLayout)container.getParent()).setBackgroundColor(getResources().getColor(R.color.readlight));
    		blogTitle.setBackgroundColor(getResources().getColor(R.color.readlight));
    		blogTitle.setTextColor(getResources().getColor(R.color.readfontlight));
    	}else if(ReaderApp.getSettings().Theme == Theme.Dark){
    		setting_defaultcolor.setText("");
    		setting_lightcolor.setText("");
    		setting_darkcolor.setText("√");
    		container.setBackgroundColor(getResources().getColor(R.color.readdark));
    		((RelativeLayout)container.getParent()).setBackgroundColor(getResources().getColor(R.color.readdark));
    		blogTitle.setBackgroundColor(getResources().getColor(R.color.readdark));
    		blogTitle.setTextColor(getResources().getColor(R.color.readfontdark));
    	}
    
    	
    	OnClickListener colorListner = new OnClickListener(){

			@Override
			public void onClick(View v) {
				String backgroundcolor = "";
				String foregroundcolor = "";
				Theme theme = Theme.Default;
				
				if(v.equals(setting_defaultcolor)){
					backgroundcolor = getResources().getString(R.color.readdefault);
					foregroundcolor = getResources().getString(R.color.readfontdefault);
					theme = Theme.Default;
					setting_defaultcolor.setText("√");
					setting_lightcolor.setText("");
					setting_darkcolor.setText("");
					
					((RelativeLayout)container.getParent()).setBackgroundColor(getResources().getColor(R.color.readdefault));
					container.setBackgroundColor(getResources().getColor(R.color.readdefault));    		
		    		blogTitle.setBackgroundColor(getResources().getColor(R.color.readdefault));
		    		blogTitle.setTextColor(getResources().getColor(R.color.readfontdefault));
				}else if(v.equals(setting_lightcolor)){
					backgroundcolor = getResources().getString(R.color.readlight);
					foregroundcolor = getResources().getString(R.color.readfontlight);
					theme = Theme.Light;
					setting_defaultcolor.setText("");
					setting_lightcolor.setText("√");
					setting_darkcolor.setText("");
					
					((RelativeLayout)container.getParent()).setBackgroundColor(getResources().getColor(R.color.readlight));
					container.setBackgroundColor(getResources().getColor(R.color.readlight));    		
		    		blogTitle.setBackgroundColor(getResources().getColor(R.color.readlight));
		    		blogTitle.setTextColor(getResources().getColor(R.color.readfontlight));
				}else if(v.equals(setting_darkcolor)){
					backgroundcolor = getResources().getString(R.color.readdark);
					foregroundcolor = getResources().getString(R.color.readfontdark);
					theme = Theme.Dark;
					setting_defaultcolor.setText("");
					setting_lightcolor.setText("");
					setting_darkcolor.setText("√");
					
					((RelativeLayout)container.getParent()).setBackgroundColor(getResources().getColor(R.color.readdark));
					container.setBackgroundColor(getResources().getColor(R.color.readdark));    		
		    		blogTitle.setBackgroundColor(getResources().getColor(R.color.readdark));
		    		blogTitle.setTextColor(getResources().getColor(R.color.readfontdark));
		    		
		    		mProgressDialog = new ProgressDialog(BlogContentActivity.this, R.style.DarkAlertDialogStyle);
		    		
				}
				
				Editor e = PreferenceManager.getDefaultSharedPreferences(ReaderApp.getAppContext()).edit();
    			
    			e.putString("view_backgroundcolor", backgroundcolor).commit();
    			e.putString("view_foregroundcolor", foregroundcolor).commit();
    			e.putInt("view_theme", theme.ordinal()).commit();    			
    			
    			ReaderApp.saveSettings();
    			
    			Helper.saveHtml(BlogContentActivity.this, true);
				
				backgroundcolor = backgroundcolor.substring(3);
				foregroundcolor = foregroundcolor.substring(3);
				
				browser.loadUrl("javascript: backgroundColor('" + backgroundcolor + "')");
				browser.loadUrl("javascript: fontColor('" + foregroundcolor + "')");
				
				initProgressDialog(false);
			}
		};
    	
		setting_defaultcolor.setOnClickListener(colorListner);
		setting_lightcolor.setOnClickListener(colorListner);
		setting_darkcolor.setOnClickListener(colorListner);    	
    	
    	final ImageView setting_bigfont = (ImageView)findViewById(R.id.setting_bigfont);
    	final ImageView setting_smallfont = (ImageView)findViewById(R.id.setting_smallfont);
    	
    	OnClickListener fontListner = new OnClickListener(){

			@Override
			public void onClick(View v) {
				int size = ReaderApp.getSettings().FontSize;
				
				if(v.equals(setting_bigfont))
					size = size + 1;
				
				if(v.equals(setting_smallfont))
					size = size - 1;
    			
				if(size <= 0)
					size = 1;
    			//ReaderApp.getSettings().FontSize = size;
    			
    			Editor e = PreferenceManager.getDefaultSharedPreferences(ReaderApp.getAppContext()).edit();
    			
    			e.putString("format_fontsize", String.valueOf(size)).commit();
    			
    			ReaderApp.saveSettings();
    			
    			Helper.saveHtml(BlogContentActivity.this, true);
    			
    			browser.loadUrl("javascript: fontsize(" + size + ")");
			}
		};
		
		setting_bigfont.setOnClickListener(fontListner);
		setting_smallfont.setOnClickListener(fontListner);
    	
    	final ImageView setting_bigheight = (ImageView)findViewById(R.id.setting_bigheight);
    	final ImageView setting_smallheight = (ImageView)findViewById(R.id.setting_smallheight);
    	
    	OnClickListener heightListner = new OnClickListener(){

			@Override
			public void onClick(View v) {
				int height = ReaderApp.getSettings().LineHeight;
				
				if(v.equals(setting_bigheight))
					height = height + 10;
				
				if(v.equals(setting_smallheight))
					height = height - 10;
    			
				if(height <= 60)
					height = 60;
    			//ReaderApp.getSettings().FontSize = size;
    			
    			Editor e = PreferenceManager.getDefaultSharedPreferences(ReaderApp.getAppContext()).edit();
    			
    			e.putString("format_line", String.valueOf(height)).commit();
    			
    			ReaderApp.saveSettings();
    			
    			Helper.saveHtml(BlogContentActivity.this, true);
    			
    			browser.loadUrl("javascript: lineheight(" + height + ")");
			}
		};
		
		setting_bigheight.setOnClickListener(heightListner);
		setting_smallheight.setOnClickListener(heightListner);
    	
    	final ImageView setting_lower = (ImageView)findViewById(R.id.setting_lower);
    	final ImageView setting_lighter = (ImageView)findViewById(R.id.setting_lighter);
    	
    	OnClickListener brightListner = new OnClickListener(){

			@Override
			public void onClick(View v) {
				int bright = 100;
				
				if(v.equals(setting_lower))
					bright = bright - 10;
				
				if(v.equals(setting_lighter))
					bright = bright + 10;
    			
				if(bright <= 30)
					bright = 30;
				
				if(bright >= 250)
					bright = 250;
    			
				WindowManager.LayoutParams layoutParams = getWindow().getAttributes(); 
		        layoutParams.screenBrightness = bright/255.0f;
		        getWindow().setAttributes(layoutParams);
			}
		};
		
		setting_lower.setOnClickListener(brightListner);
		setting_lighter.setOnClickListener(brightListner);
		
		final Button setting_close = (Button)findViewById(R.id.setting_close);
		
		setting_close.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				menu.setVisibility(View.VISIBLE);
				readSetting.setVisibility(View.GONE);
			}			
		});
    }
    
    private void initPathMenu(){
    	final float scale = getResources().getDisplayMetrics().density;          
    	menu.setSatelliteDistance((int) (200 * scale + 0.5f));    	
    	
		menu.setMainImage(R.drawable.main);
		menu.setTotalSpacingDegree(90);
		menu.setCloseItemsOnClick(true);
		menu.setExpandDuration(500);
    	
    	List<SatelliteMenuItem> items = new ArrayList<SatelliteMenuItem>();
    	
    	if(current.IsStarred)
    		items.add(new SatelliteMenuItem(0, R.drawable.fav));
    	else
    		items.add(new SatelliteMenuItem(0, R.drawable.addfav));
    	if(current.IsRead)
    		items.add(new SatelliteMenuItem(1, R.drawable.unread));
    	else
    		items.add(new SatelliteMenuItem(1, R.drawable.read));
    	        
        items.add(new SatelliteMenuItem(2, R.drawable.share));
        items.add(new SatelliteMenuItem(3, R.drawable.reload));
        items.add(new SatelliteMenuItem(4, R.drawable.full));
        items.add(new SatelliteMenuItem(5, R.drawable.settings));
    	        
        menu.addItems(items);        
        
        menu.setOnItemClickedListener(new SateliteClickedListener() {
			
			public void eventOccured(int id) {
				
				switch(id){
					case 0:
						
						if(!current.IsStarred)
		        			menu.getMenuItems().get(id).setImgDrawable(ReaderApp.getAppContext().getResources().getDrawable(R.drawable.fav));		        		
		        		else
		        			menu.getMenuItems().get(id).setImgDrawable(ReaderApp.getAppContext().getResources().getDrawable(R.drawable.addfav));
						
						new Thread(){
							public void run(){
								current.IsStarred = !current.IsStarred;
				        		
								if(current.IsStarred)
									markTag(current, RssAction.AsStar);
								else
									markTag(current, RssAction.AsUnstar);
								
								int index = Blogs.indexOf(current);
			        			
			        			if(index == -1)
			        				Blogs.add(current);
			        			else
			        				Blogs.get(index).IsStarred = current.IsStarred;
				        		
				        		BlogDalHelper helper = new BlogDalHelper();
				        		helper.MarkAsStar(current.BlogId, current.IsStarred);
				        		helper.Close();
							}
						}.start();					
								        		
						break;
					case 1: 
						
						if(!current.IsRead)
		        			menu.getMenuItems().get(id).setImgDrawable(ReaderApp.getAppContext().getResources().getDrawable(R.drawable.read));
		        		else
		        			menu.getMenuItems().get(id).setImgDrawable(ReaderApp.getAppContext().getResources().getDrawable(R.drawable.unread));
						
						new Thread(){
							public void run(){
								current.IsRead = !current.IsRead;
								
								if(current.IsRead)
									markTag(current, RssAction.AsRead);
								else
									markTag(current, RssAction.AsUnread);
				        				        		
								int index = Blogs.indexOf(current);
			        			
			        			if(current.IsRead)
			        				Count++;
								else
									Count--;
			        			
			        			if(index == -1)
			        				Blogs.add(current);
			        			else
			        				Blogs.get(index).IsRead = current.IsRead;
				        		
				        		BlogDalHelper helper = new BlogDalHelper();
				        		helper.MarkAsRead(current.BlogId, current.IsRead);
				        		helper.Close();
							}
						}.start();
								        		
						break;						
					case 2: 
						
						new Thread(){
							public void run(){
								final OnekeyShare oks = new OnekeyShare();

								// 令编辑页面显示为Dialog模式
								oks.setDialogMode();

								// 在自动授权时可以禁用SSO方式
								oks.disableSSOWhenAuthorize();

								oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
								if(ReaderApp.getProfile() != null)
									oks.setAddress(ReaderApp.getProfile().Email);
								oks.setTitle(current.Title);
								oks.setTitleUrl(current.Link);
								oks.setText(HtmlHelper.HtmlToText(HtmlHelper.filterHtml(current.Content != null && current.Content.length() > 0 ? current.Content : current.Description)));								
								oks.setUrl(current.Link);
								oks.setVenueName("RssReader");
								oks.setVenueDescription("RssReader offers better experience!");
								oks.setUrl(current.Link);								
								oks.setSite(getString(R.string.app_name));
								oks.setSiteUrl(current.Link);
								oks.setSilent(true);
								
								String sDStateString = android.os.Environment.getExternalStorageState();

								if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
									try {
										File SDFile = android.os.Environment.getExternalStorageDirectory();
										List<Blog> tmp = new ArrayList<Blog>();
										tmp.add(current);
										List<ImageRecord> records = new ImageRecordDalHelper().GetImageRecordByBlog(tmp);
										if(records != null && !records.isEmpty())
											oks.setImagePath(SDFile.getAbsolutePath() + Config.IMAGES_LOCATION + records.get(0).StoredName);
									} catch (Exception e) {
										e.printStackTrace();
									}// end of try
								}
								
								Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo_evernote);								
								String label = "Evernote";
								OnClickListener listener = new OnClickListener() {
							        public void onClick(View v) {							        	
							        	if(!mEvernoteSession.isLoggedIn())
							        		mEvernoteSession.authenticate(BlogContentActivity.this);
							        	else
							        		ShareEvernote();
						                oks.finish();
							        }
								};
								oks.setCustomerLogo(logo, label, 6, listener);
								
								Bitmap copyLogo = BitmapFactory.decodeResource(getResources(), R.drawable.logo_copy);								
								String copyLabel = ReaderApp.getAppContext().getString(android.R.string.copy);
								OnClickListener copyListener = new OnClickListener() {
							        public void onClick(View v) {
							        	ClipboardManager clipboard = (ClipboardManager)ReaderApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
							        	
							        	ClipData clip = ClipData.newPlainText("RssReader", HtmlHelper.HtmlToText(HtmlHelper.filterHtml(current.Content != null ? current.Content : current.Description)));
							        	
							        	clipboard.setPrimaryClip(clip);
							        	
							        	Toast.makeText(ReaderApp.getAppContext(), ReaderApp.getAppContext().getString(R.string.content_addtoclipbroad), Toast.LENGTH_SHORT).show();
							        	
						                oks.finish();
							        }
								};
								oks.setCustomerLogo(copyLogo, copyLabel, -1, copyListener);
								
								oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback(){

									@Override
									public void onShare(Platform platform, ShareParams paramsToShare) {
										if("SinaWeibo".equals(platform.getName())){
											paramsToShare.text = current.Title;								
										}
										else if ("Twitter".equals(platform.getName())) {
											paramsToShare.text = current.Title;
										}
										else if ("ShortMessage".equals(platform.getName())) {
											paramsToShare.text = current.Title;
										}
										else if ("Instagram".equals(platform.getName())) {
											paramsToShare.text = current.Title;
										}
										else if ("GooglePlus".equals(platform.getName())) {
											paramsToShare.text = HtmlHelper.filterHtml(current.Content != null && current.Content.length() > 0 ? current.Content : current.Description);
										}
										else if ("Email".equals(platform.getName())) {
											paramsToShare.text = HtmlHelper.filterHtml(current.Content != null && current.Content.length() > 0 ? current.Content : current.Description);
										}
										else if ("QQ".equals(platform.getName())) {
											paramsToShare.text = HtmlHelper.filterHtml(current.Content != null && current.Content.length() > 0 ? current.Content : current.Description);
										}
										else if ("Evernote".equals(platform.getName())) {
											paramsToShare.text = HtmlHelper.ConvertHtmlToEnml(current.Content != null && current.Content.length() > 0 ? current.Content : current.Description);									
										}
										else if ("Wechat".equals(platform.getName())) {
											paramsToShare.text = HtmlHelper.HtmlToText(HtmlHelper.filterHtml(current.Content != null && current.Content.length() > 0 ? current.Content : current.Description));									
										}										
									}
								});
								
								Message m = myHandler.obtainMessage();
					            m.what = SHARE;
					            m.obj = oks;
								
					            //myHandler.sendMessage(m);
				            	oks.show(BlogContentActivity.this);
							}
						}.start();
						
						break;
					case 3: 
						new Thread(){public void run(){desc.Render(current);}}.start();
						break;
					case 4: 
						
						if(!ReaderApp.getSettings().FullScreen){
							Helper.hideSystemUI(BlogContentActivity.this);
							
							menu.getMenuItems().get(id).setImgDrawable(ReaderApp.getAppContext().getResources().getDrawable(R.drawable.small));
							
							Toast.makeText(BlogContentActivity.this, ReaderApp.getAppContext().getString(R.string.content_immersiveon), Toast.LENGTH_SHORT).show();
						}
		    			else{
		    				
		    				menu.getMenuItems().get(id).setImgDrawable(ReaderApp.getAppContext().getResources().getDrawable(R.drawable.full));
		    				
		    				Helper.showSystemUI(BlogContentActivity.this, uiOptions);
		    				Toast.makeText(BlogContentActivity.this, ReaderApp.getAppContext().getString(R.string.content_immersiveoff), Toast.LENGTH_SHORT).show();
		    			}
						
						new Thread(){
							public void run(){
								ReaderApp.getSettings().FullScreen = !ReaderApp.getSettings().FullScreen;
								
								Editor e = PreferenceManager.getDefaultSharedPreferences(ReaderApp.getAppContext()).edit();
				    			
				    			e.putBoolean("view_fullscreen", ReaderApp.getSettings().FullScreen).commit();
				    			
				    			ReaderApp.saveSettings();
							}
						}.start();						
		    				
						break;
					case 5: 
						
						if(readSetting.getVisibility() == View.GONE){
							readSetting.setVisibility(View.VISIBLE);
							menu.setVisibility(View.GONE);
						}
						else{
							readSetting.setVisibility(View.GONE);
							menu.setVisibility(View.VISIBLE);
						}
						break;
				}
			}
		});
    }
    
    private void initProgressDialog(boolean isShowDialog){
    	if(ReaderApp.getSettings().Theme == Theme.Dark)
			mProgressDialog = new ProgressDialog(BlogContentActivity.this, R.style.DialogDarkTheme);
		if(ReaderApp.getSettings().Theme == Theme.Light)
			mProgressDialog = new ProgressDialog(BlogContentActivity.this, R.style.DialogLightTheme);
		if(ReaderApp.getSettings().Theme == Theme.Default)
			mProgressDialog = new ProgressDialog(BlogContentActivity.this);
		
		mProgressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mProgressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIcon(R.id.process);		        
        mProgressDialog.setMessage(BlogContentActivity.this.getResources().getString(R.string.content_loading) + "...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminateDrawable(BlogContentActivity.this.getResources().getDrawable(drawable.spinner));
        if(isShowDialog)
        	mProgressDialog.show();
        
        mProgressDialog.getWindow().getDecorView().setSystemUiVisibility(BlogContentActivity.this.getWindow().getDecorView().getSystemUiVisibility());
		//Clear the not focusable flag from the window
        mProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	if(immersiveMode){
    		immersiveMode = false;
        	Helper.showSystemUI(BlogContentActivity.this, uiOptions);
    	}    	
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CURRENT, current);
        outState.putSerializable(CHANNEL, channel);
        outState.putSerializable(ARG_TAB_ID, from);
        outState.putSerializable(UI_OPTIONS, uiOptions);
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
                
        loadEvent = new ManualResetEvent(false);
    	jsEvent = new ManualResetEvent(false);
    }
    
    private Handler myHandler = new Handler(){
    	@Override  
        public void handleMessage(Message msg) {
    		String Content = "";
    		if(msg.obj instanceof String)
    			 Content = (String)msg.obj;
    		
    		CacheEventArgs cacheArgs = null;
    		String title = ""; 
    		if(msg.obj instanceof CacheEventArgs){
    			cacheArgs = (CacheEventArgs)msg.obj;
    			title = msg.getData().getString("title");
    		}
    		
    		//WebView view = null;
    		//TextView blogTitle = null;
    		if(browser != null){
    			switch(msg.what){
	    			case CONTENT:
	    			case DESC:
	    				
	    				Log.i("RssReader", "Render content in js");
	    				
	    				//check current blog is still the parsed blog
	    				if(cacheArgs != null && cacheArgs.Blog.BlogId != current.BlogId)
	    					return;
	    				
	    				if(Content.length() != 0)
	    					browser.loadUrl("javascript: LoadContent('" + HtmlHelper.trim(Content) + "','','" + (msg.what == CONTENT ? "content" : "description") + "')");
		            	else
		            		browser.loadUrl("javascript: LoadError('" + ReaderApp.getAppContext().getResources().getString(R.string.content_errortitle) + "','" + 
		            				ReaderApp.getAppContext().getResources().getString(R.string.content_errorcontent) + "','" + 
		            				ReaderApp.getAppContext().getResources().getString(R.string.content_errorload) + "','" +
		            				(msg.what == CONTENT ? "content" : "description") + "')");
	    				    				
	    				blogTitle.setText(current.Title);
	    				
	    				//((BlogContentActivity)getActivity()).Title.setText(current.Title);
		            	jsEvent.set();
	    				break;
	    			case FLASH:
	    				
	    				if(cacheArgs != null && cacheArgs.Blog.BlogId == current.BlogId){
	    					if (cacheArgs.Total != -1)
		    					browser.loadUrl("javascript: replaceFlash('" + String.valueOf(cacheArgs.CompleteIndex) + "','" + cacheArgs.Cache.html().replace("'", "\"") + "','" + (ReaderApp.getAppContext().getResources().getString(R.string.blog_clicktoview) + " " + title.replace("'", "\"")) + "','True')");
			                else
			                	browser.loadUrl("javascript: replaceFlash('" + String.valueOf(cacheArgs.CompleteIndex) + "','" + cacheArgs.Cache.html().replace("'", "\"") + "','" + title.replace("'", "\"") + "','True')");
	    				}
	    				break;
	    			case SHARE:
	    				OnekeyShare oks = (OnekeyShare)msg.obj;
	    				
	    				oks.show(BlogContentActivity.this);
	    				
	    				break;
	    			case SHAKE:
	    				
	    				Animation shake = AnimationUtils.loadAnimation(BlogContentActivity.this, R.anim.shake);  
	        			menu.startAnimation(shake);
	    				
	    				break;
	    		}
	    		
	    		markAsRead(current);
    		}
    	}
    };
    
    public Blog GetNext(Blog b){    
    	String keyword = "";
		if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(KEYWORD))
			keyword = getIntent().getExtras().getString(KEYWORD);
		
		BlogDalHelper helper = new BlogDalHelper();
		Blog t = helper.FindBlogBy(from, keyword,channel, b, false);
		helper.Close();
		return t;
    }
    
    public Blog GetPrevious(Blog b){
    	String keyword = "";
		if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(KEYWORD))
			keyword = getIntent().getExtras().getString(KEYWORD);
		
		BlogDalHelper helper = new BlogDalHelper();
    	Blog t = helper.FindBlogBy(from, keyword, channel, b, true);
    	helper.Close();
    	return t;
    }
    
    private void markAsRead(final Blog b){
    	// read from setting before mark as read
    	if(ReaderApp.getSettings().MarkAsReadWhenView){
    		if(!b.IsRead){
        		b.IsRead = true;
        		
        		Count++;
    			
    			int index = Blogs.indexOf(b);
    			
    			if(index == -1)
    				Blogs.add(b);
    			else
    				Blogs.get(index).IsRead = true;
        		
        		//markTag(b, RssAction.AsRead);
        		BlogDalHelper helper = new BlogDalHelper();        		
        		helper.MarkAsRead(b.BlogId, b.IsRead);
        		helper.Close();        		
        	}
    	}
    }
    
    @Override
    public void onStop(){
    	if(Blogs != null && Blogs.size() > 0){
			final List<String> blogIDs = new ArrayList<String>();
			
			for(Blog b : Blogs){				
				blogIDs.add(b.BlogId);				
			}
			
			final FeedlyParser feedly = new FeedlyParser();
			
			feedly.batchMarkTag(blogIDs, RssAction.AsRead, new HttpResponseHandler(){
	        	@Override
	        	public <RssAction> void onCallback(RssAction action, boolean result, String msg){
	        		if(!result){
	        			Log.i("RssReader", msg);
	        			
	        			SyncStateDalHelper helper = new SyncStateDalHelper();
	        			
	        			List<SyncState> states = new ArrayList<SyncState>();
	        			
	        			for(Blog b : Blogs){
	            			SyncState s = new SyncState();
	            			
	            			s.BlogOriginId = b.BlogId;
	            			s.Status = (com.lgq.rssreader.enums.RssAction) action;
	            			s.TimeStamp = new Date();
	            			
	            			states.add(s);
	        			}
	        			helper.SynchronyData2DB(states);
	        			helper.Close();
	        		}
	        	}
	        });
		}
    	
    	super.onStop();
    }
    
    public void ShareEvernote(){
    	NotificationHelper.showNotification(2000, getResources().getString(R.string.sharing));
    	
    	final Note note = new Note();
        note.setTitle(current.Title);

        note.setContent(EvernoteUtil.NOTE_PREFIX + HtmlHelper.ConvertHtmlToEnml(current.Content != null ? current.Content : current.Description) + EvernoteUtil.NOTE_SUFFIX);
        
        try {
        	mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>(){
        		@Override
				public void onSuccess(List<Notebook> data) {
        			String rssreader = null;
        			for(Notebook book : data){
        				if(book.getName().toLowerCase().equals(ReaderApp.getAppContext().getResources().getString(R.string.app_name).toLowerCase())){
        					rssreader = book.getGuid();
        					break;
        				}
        			}
				
        			if(rssreader == null){
        				Notebook notebook = new Notebook();
        				notebook.setName(getResources().getString(R.string.app_name));
        				
        				try {
							mEvernoteSession.getClientFactory().createNoteStoreClient().createNotebook(notebook, new OnClientCallback<Notebook>() {
								@Override
							    public void onSuccess(Notebook data) {
									try {
										note.setNotebookGuid(data.getGuid());
										mEvernoteSession.getClientFactory().createNoteStoreClient().createNote(note, new OnClientCallback<Note>() {
										    @Override
										    public void onSuccess(Note data) {
										    	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_completed));
										    }

										    @Override
										    public void onException(Exception exception) {
										    	Log.e("RssReader", "Error saving note", exception);
										    	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
										    }
										});
									} catch (TTransportException e) {
										Log.e("RssReader", "Error saving note", e);
								    	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
									}
							    }

							    @Override
							    public void onException(Exception exception) {
							    	Log.e("RssReader", "Error saving note", exception);
							    	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
							    }
							});
						} catch (TTransportException e) {
							Log.e("RssReader", "Error creating notebook", e);
				        	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
						}
        			}else{
        				try {
        					note.setNotebookGuid(rssreader);
							mEvernoteSession.getClientFactory().createNoteStoreClient().createNote(note, new OnClientCallback<Note>() {
							    @Override
							    public void onSuccess(Note data) {
							    	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_completed));
							    }

							    @Override
							    public void onException(Exception exception) {
							    	Log.e("RssReader", "Error saving note", exception);
							    	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
							    }
							  });
						} catch (TTransportException e) {
							Log.e("RssReader", "Error share note", e);
				        	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
						}
        			}
				}

				@Override
				public void onException(Exception exception) {
					Log.e("RssReader", "Error loading notebook", exception);
		        	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
				}});
        } catch (TTransportException exception) {
        	Log.e("RssReader", "Error creating notestore", exception);
        	//Toast.makeText(ReaderApp.getAppContext(), R.string.share_failed, Toast.LENGTH_LONG).show();
        	NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
        }catch(Exception e){
    		Log.e("RssReader", "Error creating notestore", e);
            //Toast.makeText(ReaderApp.getAppContext(), R.string.share_failed, Toast.LENGTH_LONG).show();
    		NotificationHelper.showNotification(2000, ReaderApp.getAppContext().getResources().getString(R.string.share_failed));
        }
    }
    
    private void markTag(final Blog b, RssAction action){
    	final FeedlyParser feedly = new FeedlyParser();
		
		feedly.markTag(b, action, new HttpResponseHandler(){
        	@Override
        	public <T> void onCallback(T action, boolean result, String msg){
        		if(!result){
        			Log.i("RssReader", msg);
        			
        			Toast.makeText(BlogContentActivity.this, msg, Toast.LENGTH_SHORT).show();
        			
        			SyncStateDalHelper helper = new SyncStateDalHelper();
        			
        			List<SyncState> states = new ArrayList<SyncState>();
        			        			
        			SyncState s = new SyncState();
        			
        			s.BlogOriginId = b.BlogId;
        			s.Status = (com.lgq.rssreader.enums.RssAction) action;
        			s.TimeStamp = new Date();
        			
        			states.add(s);
        			
        			helper.SynchronyData2DB(states);
        			helper.Close();
        		}
        	}
        });
    }
    
    @Override
    public void onDestroy(){
    	ShareSDK.stopSDK(this);
    	super.onDestroy();
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
			
			if(getMenu() != null && getMenu().getVisibility() == View.GONE){
				getMenu().setVisibility(View.VISIBLE);
				getReadSetting().setVisibility(View.GONE);
				
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
			
			if(getMenu() != null && getMenu().getVisibility() == View.VISIBLE){
				getMenu().toggle();
				
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
        	  ShareEvernote();        	  
          }
          break;
      }
    }        
}
