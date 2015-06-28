package com.lgq.rssreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.kanak.emptylayout.EmptyLayout;
import com.lgq.rssreader.adapter.BlogAdapter;
import com.lgq.rssreader.adapter.RecyclerBlogAdapter;
import com.lgq.rssreader.adapter.RecyclerItemClickListener;
import com.lgq.rssreader.controls.SwipeMenu;
import com.lgq.rssreader.controls.SwipeMenuAdapter;
import com.lgq.rssreader.controls.SwipeMenuCreator;
import com.lgq.rssreader.controls.SwipeMenuItem;
import com.lgq.rssreader.controls.SwipeMenuListView;
import com.lgq.rssreader.controls.SwipeMenuView;
import com.lgq.rssreader.controls.SystemBarTintManager;
import com.lgq.rssreader.controls.XListView.IXListViewListener;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.dal.SyncStateDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
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
public class BlogListActivity extends BaseActivity implements IXListViewListener {
	
	private boolean needUpdate;

	/**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    
    /**
     * The fragment's listview, which support pull down to refresh
     * clicks.
     */
    private SwipeMenuListView listView;
    //private RecyclerView listView;
    
    private SwipeRefreshLayout mSwipeRefreshLayout;
    
    /**
     * The fragment's title
     * 
     */
    private TextView title;
    
    /**
     * The listview adapter
     */
    private BlogAdapter adapter;
    
    /**
     * The listview page index
     */
    private int pageIndex = 1;
    
    /**
     * The blog list
     */
    private List<Blog> data;
    
    /**
     * Empty view for listview
     */
    private EmptyLayout emptyLayout;
    
    /**
     * The Channel which this fragment presents
     */
    private Channel channel;
    
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    
    public static final int LOADDATA = 1;
    public static final int UPDATESTATE = 2;
    public static final int UPDATECOUNT = 3;       
    public static final int EMPTY = 4;
    public static final int ONLOAD = 5;
    public static final int SCROLLING = 6;
    
    public Handler myHandler = new Handler(){
        @Override  
        public void handleMessage(Message msg) {            
            switch(msg.what){
	            case LOADDATA :
	            	if(adapter == null){
	            		data = (List<Blog>) msg.obj;
	            		adapter = new BlogAdapter(
	                            BlogListActivity.this,
	                            data,
	                            listView);
	            		listView.setAdapter(adapter);
	            	}else{
	            		List<Blog> blogs = ((List<Blog>) msg.obj);
	            		
	            		for(Blog b : blogs){
	            			if(!data.contains(b)){
	            				data.add(b);
	            			}
	            		}
	            		Collections.sort(data, new Comparator<Blog>(){
	         	           public int compare(Blog arg0, Blog arg1) {   
	         	               return (int)(arg1.TimeStamp - arg0.TimeStamp);
	         	            }
	         	        }); 
	            	}
	            		            	
	            	adapter.notifyDataSetChanged();
	            	
	            	onLoad();
	            	
	            	break;
	            case UPDATESTATE:
	            	List<Blog> blogs = (List<Blog>)msg.obj;
	            	if(data == null){
	            		data = blogs;
	            	}else{
		            	for(Blog b : blogs){
		            		int index = data.indexOf(b);
		            		if(index != -1)
		            			data.set(index, b);
		            	}
	            	}
	            	Collections.sort(data, new Comparator<Blog>(){
	         	           public int compare(Blog arg0, Blog arg1) {   
	         	               return (int)(arg1.TimeStamp - arg0.TimeStamp);
	         	            }
	         	        });
	            	if(adapter == null){
	            		adapter = new BlogAdapter(
	                            BlogListActivity.this,
	                            data,
	                            listView);
	            		listView.setAdapter(adapter);	            		
	            	}
	            	
	            	if(blogs.size() == 0){
	            		emptyLayout.setEmptyMessage(BlogListActivity.this.getResources().getString(R.string.list_empty_view));
	            		emptyLayout.getEmptyView().setOnClickListener(new View.OnClickListener(){        						
							@Override
							public void onClick(View v) {
								onRefresh();
							}
    					});        					
    					emptyLayout.showEmpty();
	            	}
	            	
	            	adapter.notifyDataSetChanged();
	            	break;
	            case UPDATECOUNT:	            	
	                title.setText(channel.Title + "-" + String.valueOf(msg.obj));
	            	break;
	            case EMPTY:
	                emptyLayout.setEmptyMessage(ReaderApp.getAppContext().getResources().getString(R.string.list_empty_view));	                
            		emptyLayout.getEmptyView().setOnClickListener(new View.OnClickListener(){        						
						@Override
						public void onClick(View v) {
							onEmpty();
						}
					});        					
					emptyLayout.showEmpty();
	            	break;
	            case ONLOAD:
	                onLoad();
	            	break;
	            case SCROLLING:
	            	if(adapter != null)
	            		adapter.clearPosition(-1);
	            	break;
            }
            
            super.handleMessage(msg);
        }
    };
	
	public BlogListActivity(){
		super(R.drawable.translucent_status_bar);
	} 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_list);
        
        initViews();
    }
     
    private void initViews(){
        if(getIntent().getExtras().containsKey(ARG_ITEM_ID)){
        	channel = (Channel)getIntent().getExtras().get(ARG_ITEM_ID);
        	
        	new Thread(){  
                @Override  
                public void run() {  
                    try {
                    	
                    	final BlogDalHelper helper = new BlogDalHelper();
                    	
                    	List<Blog> data = helper.GetBlogList(channel, 1, ReaderApp.getSettings().NumPerRequest, ReaderApp.getSettings().ShowAllItems);
                    	
                    	helper.Close();
                    	
                    	if(data.size() > 0){
                    		Message m = myHandler.obtainMessage();                    				
            	            m.what = LOADDATA;
            	            m.obj = data;
            	            m.arg1 = 0;
            				myHandler.sendMessage(m);            				
                    	}else{
                    		onEmpty();
                    	}
                    } catch (Exception e) {
                        e.printStackTrace();  
                    }
                }  
        	}.start();
        }        
        
        LinearLayout fragment_blog_list_layout = (LinearLayout)findViewById(R.id.fragment_blog_list_layout);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        	fragment_blog_list_layout.setPadding(0, Helper.getStatusBarHeight(), 0, 0);
        }        
        
        listView = (SwipeMenuListView)findViewById(R.id.blog_list);
        
        SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu, int position) {
				
				Blog entity = (Blog)adapter.getItem(position);
				
											
				// create "open" item
				SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
				// set item background
				openItem.setBackground(new ColorDrawable(Color.rgb(0x2D, 0xBD, 0x69)));
				// set item width
				openItem.setWidth(dp2px(90));
				// set item icon
				//openItem.setIcon(R.drawable.fav);
//				if(!entity.IsStarred){
//					openItem.setIcon(R.drawable.fav);
//					openItem.setTitle("Fav");
//				}
//    			else{
//    				openItem.setIcon(R.drawable.unstar);
//    				openItem.setTitle("Unstar");
//    			}
				// add to menu
				menu.addMenuItem(openItem);
				
				SwipeMenuItem seerateItem = new SwipeMenuItem(getApplicationContext());
				// set item background
				seerateItem.setBackground(R.color.white);
				// set item width
				seerateItem.setWidth(dp2px(1));
				// add to menu
				menu.addMenuItem(seerateItem);


				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getApplicationContext());
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0x2D, 0xBD, 0x69)));
				// set item width
				deleteItem.setWidth(dp2px(90));
				// set item icon
				//deleteItem.setIcon(R.drawable.read);
//				if(!entity.IsRead){
//					deleteItem.setIcon(R.drawable.read);
//					deleteItem.setTitle("read");
//				}
//    			else{
//    				deleteItem.setIcon(R.drawable.unread);
//    				deleteItem.setTitle("unread");
//    			}
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		// set creator
		listView.setMenuCreator(creator);
        
        //listView = (RecyclerView)findViewById(R.id.blog_list);
        
        //listView.setHasFixedSize(true);

        //final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //listView.setLayoutManager(layoutManager);
        
        //mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        
        //mSwipeRefreshLayout.setColorSchemeResources(R.color.green);
        
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {  
//            @Override
//            public void onRefresh() {
//                // Refresh items
//                refreshItems();
//            }
//        });
        
        listView.setPullLoadEnable(true);
        listView.setPullRefreshEnable(true);
        listView.setXListViewListener(this);
        
//        emptyLayout = new EmptyLayout(this, listView);
//        emptyLayout.setLoadingMessage(getResources().getString(R.string.content_loading));
//        //emptyLayout.setLoadingAnimationViewId(emptyLayout.getLoadingAnimationViewId());
//        //emptyLayout.setLoadingAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
//        emptyLayout.showLoading();
        
        title = (TextView)findViewById(R.id.bloglist_channel_title);
        title.setText(channel.Title + "-" + channel.UnreadCount);
        title.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v) {
        		//listView.setSelection(0);
        		listView.smoothScrollToPosition(0);        		
			}
        });
        
//        listView.addOnItemTouchListener(
//    	    new RecyclerItemClickListener(getBaseContext(), new RecyclerItemClickListener.OnItemClickListener() {
//    	        @Override public void onItemClick(View view, int position) {
//    	        	RecyclerBlogAdapter adapter = (RecyclerBlogAdapter) listView.getAdapter();
//        			
//        			Intent detailIntent = new Intent(BlogListActivity.this, BlogContentActivity.class);
//        	        Bundle arguments = new Bundle();
//        	        
//    	        	arguments.putSerializable(BlogContentActivity.CURRENT, (Blog)adapter.getItem(position));        
//        	        arguments.putSerializable(BlogContentActivity.CHANNEL, (Channel)getIntent().getExtras().get(BlogListActivity.ARG_ITEM_ID));
//        	        detailIntent.putExtras(arguments);
//        	        startActivityForResult(detailIntent, 0);    	        	
//	        	}
//    	    })
//		);
//        
//        listView.setOnScrollListener(new RecyclerView.OnScrollListener() 
//        {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx,int dy)
//            {
//                super.onScrolled(recyclerView, dx, dy); 
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView,int newState) 
//            {
//                int totalItemCount = layoutManager.getItemCount();
//                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
//
//                if (totalItemCount> 1) 
//                {
//                    if (lastVisibleItem >= totalItemCount - 1) 
//                    {
//                        // End has been reached
//                        // do something 
//                    	onLoadMore();
//                    }
//                }          
//            }
//        });  
                
        if(channel.LastRefreshTime != null)
        	listView.setRefreshTime(DateHelper.DateToChineseString(channel.LastRefreshTime));
        else
        	listView.setRefreshTime(DateHelper.DateToChineseString(channel.LastUpdateTime));
    	
    	if (Build.VERSION.SDK_INT >= 11) {
    		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);    		
        }        
    	
    	listView.setOnSwipeListener(new com.lgq.rssreader.controls.SwipeMenuListView.OnSwipeListener() {
			
			@Override
			public void onSwipeStart(SwipeMenuView menuView, int position) {
				// swipe start				
				
				//View view = listView.getChildAt(position);
				Blog entity = (Blog)adapter.getItem(position - listView.getHeaderViewsCount());
				
				//Toast.makeText(getApplicationContext(), entity.Title, Toast.LENGTH_SHORT).show();
				
				if(entity != null){
					//SwipeMenuView menuView = (SwipeMenuView)view.getTag();
					
					SwipeMenu menu = menuView.getMenu();
					
					if(menu != null){
						SwipeMenuItem openItem = menu.getMenuItem(0);
						SwipeMenuItem deleteItem = menu.getMenuItem(2);
						
						if(!entity.IsStarred){
							openItem.setIcon(R.drawable.fav);
							openItem.setTitle("Fav");
						}
		    			else{
		    				openItem.setIcon(R.drawable.unstar);
		    				openItem.setTitle("Unstar");
		    			}
						
						if(!entity.IsRead){
							deleteItem.setIcon(R.drawable.read);
							deleteItem.setTitle("read");
						}
		    			else{
		    				deleteItem.setIcon(R.drawable.unread);
		    				deleteItem.setTitle("unread");
		    			}
												
						menuView.setMenu(menu);						
					}					
				}						
			}
			
			@Override
			public void onSwipeEnd(SwipeMenuView menuView, int position) {
				// swipe end
			}
		});
    	
    	// step 2. listener item click event
		listView.setOnMenuItemClickListener(new com.lgq.rssreader.controls.SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenuView menuView, int index) {
				Blog entity = (Blog)adapter.getItem(position);
				RssAction action;
				
				//Toast.makeText(getApplicationContext(), entity.Title, Toast.LENGTH_SHORT).show();
				
				SwipeMenu menu = menuView.getMenu();
				
				if(menu != null){
					SwipeMenuItem openItem = menu.getMenuItem(0);
					SwipeMenuItem deleteItem = menu.getMenuItem(2);
										
					switch (index) 
					{
						case 0:
							
							Log.i("RssReader","设置收藏未收藏");
			    			
			    			if(menu == null) 
			    				return false;;
			    			
			    			
			    			action = deleteItem.getTitle().equals("Fav") ? RssAction.AsUnstar : RssAction.AsStar;
			    			//RssAction action = title.getCurrentTextColor() == Color.GRAY ? RssAction.AsUnread : RssAction.AsRead;
							//entity.IsRead = title.getCurrentTextColor() == Color.BLACK;
			    			if(action.equals(RssAction.AsStar)){
			    				openItem.setIcon(R.drawable.fav);
								openItem.setTitle("Fav");
								entity.IsStarred = true;
			    			}
			    			else{
			    				openItem.setIcon(R.drawable.unstar);
			    				openItem.setTitle("Unstar");
			    				entity.IsStarred = false;
			    			}
							markTag(entity, action);					
							
							adapter.notifyDataSetChanged();
							
							break;
						case 2:
							Log.i("RssReader","设置已读未读");
			    			
			    			if(menu == null) 
			    				return false;;
			    				
			    				//Toast.makeText(getApplicationContext(), "before" + entity.IsRead, Toast.LENGTH_SHORT).show();
			    			
			    			action = deleteItem.getTitle().equals("read") ? RssAction.AsUnread : RssAction.AsRead;
			    			//RssAction action = title.getCurrentTextColor() == Color.GRAY ? RssAction.AsUnread : RssAction.AsRead;
							//entity.IsRead = title.getCurrentTextColor() == Color.BLACK;
			    			if(entity.IsRead){
			    				deleteItem.setIcon(R.drawable.read);
								deleteItem.setTitle("read");
		    				}		    			
			    			else{
			    				deleteItem.setIcon(R.drawable.unread);
								deleteItem.setTitle("unread");								
			    			}
			    			
							markTag(entity, action);
							
							entity.IsRead = !entity.IsRead;
							
							adapter.notifyDataSetChanged();
							break;
					}
					
					//Toast.makeText(getApplicationContext(), "after" + entity.IsRead, Toast.LENGTH_SHORT).show();
					
					menuView.setMenu(menu);
				}
				return false;
			}
		});
        
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListAdapter adapter = (ListAdapter) listView.getAdapter();
    			
    			Intent detailIntent = new Intent(BlogListActivity.this, BlogContentActivity.class);
    	        Bundle arguments = new Bundle();        
    	        
	        	arguments.putSerializable(BlogContentActivity.CURRENT, (Blog)adapter.getItem(position));        
    	        arguments.putSerializable(BlogContentActivity.CHANNEL, (Channel)getIntent().getExtras().get(BlogListActivity.ARG_ITEM_ID));
    	        detailIntent.putExtras(arguments);
    	        startActivityForResult(detailIntent, 0);
				
			}});
//    		
//    		@Override 
//    		public void onRightAutoClose(int position, View view){
//    			Log.i("RssReader","设置已读未读");
//    			
//    			if(view == null) return;
//    			
//    			Blog entity = (Blog)adapter.getItem(position- listView.getHeaderViewsCount());
//    			
//    			TextView btn = (TextView)view.findViewById(R.id.btnread);
//				ImageView img = (ImageView)btn.getTag(R.id.tag_first);
//				TextView title = (TextView)btn.getTag(R.id.tag_second);
//    			
//    			//RssAction action = entity.IsRead ? RssAction.AsUnread : RssAction.AsRead;
//    			RssAction action = title.getCurrentTextColor() == Color.GRAY ? RssAction.AsUnread : RssAction.AsRead;
//				entity.IsRead = title.getCurrentTextColor() == Color.BLACK; 
//				markTag(entity, action);
//				
////				if(entity.IsRead){
////					img.setVisibility(View.VISIBLE);
////					img.setImageResource(R.drawable.keepread);
////					title.setTextColor(Color.GRAY);
////					//btn.setText(R.string.blog_setunread);
////					btn.setText(R.string.empty);
////					Drawable drawable = ReaderApp.getAppContext().getResources().getDrawable(R.drawable.setunread);
////					drawable.setBounds(btn.getCompoundDrawables()[0].getBounds());
////					btn.setCompoundDrawables(drawable, null, null, null);
////				}
////				else{
////					title.setTextColor(Color.BLACK);
////					img.setVisibility(View.GONE);
////					//btn.setText(R.string.blog_setread);
////					btn.setText(R.string.empty);
////					Drawable drawable = ReaderApp.getAppContext().getResources().getDrawable(R.drawable.setread);
////					drawable.setBounds(btn.getCompoundDrawables()[0].getBounds());
////					btn.setCompoundDrawables(drawable, null, null, null);
////				}
//				
//				adapter.notifyDataSetChanged();
//				
//    		}
//    		
//    		@Override 
//    		public void onLeftAutoClose(int position, View view){
//    			Log.i("RssReader","设置收藏相关");
//    			
//    			if(view == null) return;
//    			
//    			Blog entity = (Blog)adapter.getItem(position - listView.getHeaderViewsCount());
//    			TextView btn = (TextView)view.findViewById(R.id.btnstar);
//				ImageView img = (ImageView)btn.getTag();
//    			
//    			//RssAction action = entity.IsStarred ? RssAction.AsUnstar : RssAction.AsStar;
//				RssAction action = img.getVisibility() == View.VISIBLE ? RssAction.AsUnstar : RssAction.AsStar;
//				entity.IsStarred = img.getVisibility() == View.GONE;//!entity.IsStarred;
//				markTag(entity, action);
//				
////				if(entity.IsStarred){
////					img.setVisibility(View.VISIBLE);
////					img.setImageResource(R.drawable.star);
////					//btn.setText(R.string.blog_setunstar);
////					btn.setText(R.string.empty);
////					Drawable drawable = ReaderApp.getAppContext().getResources().getDrawable(R.drawable.setstar);
////					drawable.setBounds(btn.getCompoundDrawables()[0].getBounds());
////					btn.setCompoundDrawables(drawable, null, null, null);
////				}
////				else{
////					img.setVisibility(View.GONE);					
////					//btn.setText(R.string.blog_setstar);
////					btn.setText(R.string.empty);					
////					Drawable drawable = ReaderApp.getAppContext().getResources().getDrawable(R.drawable.setunstar);
////					drawable.setBounds(btn.getCompoundDrawables()[0].getBounds());
////					btn.setCompoundDrawables(drawable, null, null, null);
////				}
//				
//				adapter.notifyDataSetChanged();
//    		}
		//});
    }
    
    private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}

    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ) {
			Intent i = new Intent();
			i.putExtra("NeedUpdate", needUpdate);
			this.setResult(Activity.RESULT_OK, i);
						
		}
		
		return super.onKeyDown(keyCode, event);
    }
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (channel != null) {
            // Serialize and persist the activated item position.
            outState.putSerializable(BlogListActivity.ARG_ITEM_ID, channel);
        }
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (resultCode)
		{
			case RESULT_OK:
				String gson = data.getStringExtra("Blogs");
				Gson g = new Gson();
				
				final List<Blog> blogs = new ArrayList<Blog>();
				final List<String> blogIDs = new ArrayList<String>();
				
				for(String s : gson.split("____")){
					if(s != null && s.length() > 0){
						Blog b = g.fromJson(s, Blog.class);
						blogs.add(b);
						blogIDs.add(b.BlogId);
					}
				}
				
				final FeedlyParser feedly = new FeedlyParser();
				
				final int count = data.getIntExtra("Count", 0);
	            
	            if(blogs.size() > 0){
					new Thread(){
						public void run(){
							Message m = myHandler.obtainMessage();
	        	            m.what = BlogListActivity.UPDATECOUNT;
	        	            m.obj = channel.UnreadCount - count > 0 ? channel.UnreadCount - count : 0 ;//blogs.size();
	        	            myHandler.sendMessage(m);
	        	            
	        	            Message msg = myHandler.obtainMessage();
				            msg.what = BlogListActivity.UPDATESTATE;
				            msg.obj = blogs;
				            myHandler.sendMessage(msg);
	        	            
	        	            needUpdate = true;
	        	            			        	            
	        	            channel.UnreadCount = channel.UnreadCount - count > 0 ? channel.UnreadCount - count : 0;
							
							if(channel.IsDirectory){
								//need to update sub channel unread count
								Hashtable<String, Integer> groups = new Hashtable<String, Integer>(); 
								for(Blog b : blogs){									
									if(groups.containsKey(b.ChannelId))
										groups.put(b.ChannelId, groups.get(b.ChannelId) + 1);
									else
										groups.put(b.ChannelId, 1);
								}
								
								for(Iterator<String> it = groups.keySet().iterator(); it.hasNext(); ){ 
							        String id = it.next(); 
							        Integer count = groups.get(id);
							        
									for(Channel child : channel.Children){
										if(child.Id.equals(id)){
											child.UnreadCount = child.UnreadCount - count;
											break;
										}
									}
								}
							}
							
							Helper.updateChannels(channel.Id, channel.UnreadCount);
						}
					}.start();	            	
	            }
				break;
			default:
				break;
		}
	}
		
    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        listView.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
        	listView.setItemChecked(mActivatedPosition, false);
        } else {
        	listView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void onLoad() {
    	listView.stopRefresh();
    	listView.stopLoadMore();
    	channel.LastRefreshTime = new Date();
    	Helper.updateChannels(channel.Id, channel.LastRefreshTime);
    	//mSwipeRefreshLayout.setRefreshing(false);
    	listView.setRefreshTime(DateHelper.getDaysBeforeNow(channel.LastRefreshTime) + ReaderApp.getAppContext().getResources().getString(R.string.list_refreshtime));
	}
    
    private void markTag(final Blog b, RssAction action){
    	final FeedlyParser feedly = new FeedlyParser();
		
		feedly.markTag(b, action, new HttpResponseHandler(){
        	@Override
        	public <T> void onCallback(T action, boolean result, String msg){
				if(!result){
        			Log.i("RssReader", msg);
        			
        			SyncStateDalHelper helper = new SyncStateDalHelper();
        			
        			List<SyncState> states = new ArrayList<SyncState>();
        			        			
        			SyncState s = new SyncState();
        			
        			s.BlogOriginId = b.BlogId;
        			
        			s.Status = (com.lgq.rssreader.enums.RssAction) action;
        			s.TimeStamp = new Date();
        			
        			states.add(s);
        			
        			helper.SynchronyData2DB(states);
        			
        			helper.Close();
        			
        			Toast.makeText(ReaderApp.getAppContext(), R.string.feedly_failedupdatestatus, Toast.LENGTH_SHORT).show();
        		}else{
        			BlogDalHelper bHelper = new BlogDalHelper();
        			if(action == RssAction.AsRead)
        				bHelper.MarkAsRead(b.BlogId, true);
        			if(action == RssAction.AsUnread)
        				bHelper.MarkAsRead(b.BlogId, false);
        			if(action == RssAction.AsStar)
        				bHelper.MarkAsStar(b.BlogId, true);
        			if(action == RssAction.AsUnstar)
        				bHelper.MarkAsStar(b.BlogId, false);        			
        			
        			bHelper.Close();
        			
        			Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
        		}
        	}
        });
    } 
    
    private void onEmpty(){
    	final FeedlyParser feedly = new FeedlyParser();             
        
		Blog tmp = new Blog();
		tmp.TimeStamp = 0;
		tmp.PubDate = new Date();
		
        feedly.getRssBlog(channel, tmp, 30, new HttpResponseHandler(){
        	@Override
        	public <Blog> void onCallback(List<Blog> blogs, boolean result, String msg, boolean hasMore){
        		if(result){
        			BlogDalHelper helper = new BlogDalHelper();
        			
        			helper.SynchronyData2DB((List<com.lgq.rssreader.entity.Blog>) blogs);
        			
        			helper.Close();
        			
        			if(blogs.size() > 0){
        				Message m = myHandler.obtainMessage();
        	            m.what = LOADDATA;
        	            m.obj = blogs;
        				myHandler.sendMessage(m);
        			}
        			
        			Helper.sound();
        		}else{
        			Message m = myHandler.obtainMessage();
    	            m.what = EMPTY;
    	            m.obj = blogs;    	            
    				myHandler.sendMessage(m);
        			Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
        		}
        	}
        });
    }
    
	@Override
	public void onRefresh() {
		Blog b = (Blog)adapter.getItem(0);
		
		//listView.setSelection(0);
		
		Helper.pulldown();
		
		FeedlyParser parser = new FeedlyParser();
		
		parser.getRssBlog(channel, b, ReaderApp.getSettings().NumPerRequest, new HttpResponseHandler(){
        	@Override
        	public <Blog> void onCallback(final List<Blog> blogs, boolean result, String msg, boolean hasMore){
        		if(result){
        			BlogDalHelper helper = new BlogDalHelper();
        			helper.SynchronyData2DB((List<com.lgq.rssreader.entity.Blog>) blogs);
        			helper.Close();
        			
        			if(hasMore){
        				Toast.makeText(ReaderApp.getAppContext(), ReaderApp.getAppContext().getResources().getString(R.string.list_loadingmore), Toast.LENGTH_SHORT).show();
        			}else{        				
        				Message m = myHandler.obtainMessage();
        	            m.what = ONLOAD;        	            
        				myHandler.sendMessage(m);        				
        				Helper.sound();
        				
        				Message s = myHandler.obtainMessage();
        	            s.what = SCROLLING;        	            
        				myHandler.sendMessage(s);
        			}
        			
        			//only first page show in UI thread
        			if(blogs.size() > 0){
        				Message m = myHandler.obtainMessage();
        	            m.what = LOADDATA;
        	            m.obj = blogs;
        				myHandler.sendMessage(m);
        			}
        			
        		}else{
        			Message m = myHandler.obtainMessage();
    	            m.what = ONLOAD;        	                    	           
    				myHandler.sendMessage(m);
        			Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();        			
        		}
        	}
		});
	}

	@Override
	public void onLoadMore() {
		
		new Thread(){
			public void run(){
			final BlogDalHelper helper = new BlogDalHelper();
			
			pageIndex = pageIndex + 1;
			
			List<Blog> blogs = helper.GetBlogList(channel, pageIndex, ReaderApp.getSettings().NumPerRequest, ReaderApp.getSettings().ShowAllItems);
			helper.Close();
			
			if(blogs.size()>0){
				Message m = myHandler.obtainMessage();                    				
	            m.what = LOADDATA;
	            m.obj = blogs;	            
				myHandler.sendMessage(m);
				
				Message s = myHandler.obtainMessage();
	            s.what = SCROLLING;        	            
				myHandler.sendMessage(s);
				
			}else{
				Blog b = (Blog)adapter.getItem(adapter.getCount() - 1);
				
				b.TimeStamp = -b.TimeStamp; 
				
				FeedlyParser parser = new FeedlyParser();
				
				parser.getRssBlog(channel, b, ReaderApp.getSettings().NumPerRequest, new HttpResponseHandler(){
		        	@Override
		        	public <Blog> void onCallback(final List<Blog> blogs, boolean result, String msg, boolean hasMore){
		        		if(result){
		        			
		        			final BlogDalHelper save = new BlogDalHelper();
        					save.SynchronyData2DB((List<com.lgq.rssreader.entity.Blog>) blogs);
        					save.Close();
		        			
		        			if(hasMore){
		        				Toast.makeText(BlogListActivity.this, getResources().getString(R.string.list_loadingmore), Toast.LENGTH_SHORT).show();
		        			}
		        			else{
		        				Message s = myHandler.obtainMessage();
		        	            s.what = SCROLLING;        	            
		        				myHandler.sendMessage(s);
		        				Helper.sound();
		        			}
		        			
		        			if(blogs.size() > 0){
		        				Message m = myHandler.obtainMessage();                    				
		        	            m.what = LOADDATA;
		        	            m.obj = blogs;		        	            
		        				myHandler.sendMessage(m);
		        			}
		        			
		        		}else{ 
		        			Toast.makeText(BlogListActivity.this, msg, Toast.LENGTH_SHORT).show();        			
		        		}
		        		
		        		//onLoad();
		        	}
				});
			}
		}}.start();
		
		//onLoad();
	}
}
