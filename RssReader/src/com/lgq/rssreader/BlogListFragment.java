package com.lgq.rssreader;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.kanak.emptylayout.EmptyLayout;
import com.lgq.rssreader.adapter.BlogAdapter;
import com.lgq.rssreader.adapter.BlogAdapter.ViewHolder;
import com.lgq.rssreader.adapter.ChannelAdapter;
import com.lgq.rssreader.controls.BaseSwipeListViewListener;
import com.lgq.rssreader.controls.PullToRefreshListView;
import com.lgq.rssreader.controls.PullToRefreshListView.OnRefreshListener;
import com.lgq.rssreader.controls.SwipeListView;
import com.lgq.rssreader.controls.SwipeListViewListener;
import com.lgq.rssreader.controls.XListView;
import com.lgq.rssreader.controls.XListView.IXListViewListener;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;

/**
 * A list fragment representing a list of Blogs. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link BlogContentFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class BlogListFragment extends Fragment implements IXListViewListener {
//implements BlogContentFragment.Callbacks
	
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
    private SwipeListView listView;
    
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
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = blogCallback;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(Blog previous, Blog current, Blog next);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks blogCallback = new Callbacks() {
        @Override
        public void onItemSelected(Blog p, Blog c, Blog n) {
        }
    };
    
    public static final int LOADDATA = 1;
    public static final int UPDATESTATE = 2;
    public static final int UPDATECOUNT = 3;       
    public static final int EMPTY = 4;
    public static final int ONLOAD = 5;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BlogListFragment() {
    }
    
    public Handler myHandler = new Handler(){
        @Override  
        public void handleMessage(Message msg) {            
            switch(msg.what){
	            case LOADDATA :
	            	if(adapter == null){
	            		data = (List<Blog>) msg.obj;
	            		adapter = new BlogAdapter(
	                            getActivity(),
	                            data,
	                            listView);
	            		listView.setAdapter(adapter);	            		
	            	}else{
	            		List<Blog> blogs = ((List<Blog>) msg.obj);
	            		
	            		if(msg.arg1 == 0){
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
	            		else{
	            			data = blogs;
	            		}
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
	                            getActivity(),
	                            data,
	                            listView);
	            		listView.setAdapter(adapter);	            		
	            	}
	            	
	            	if(blogs.size() == 0){
	            		emptyLayout.setEmptyMessage(getActivity().getResources().getString(R.string.list_empty_view));
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
	                emptyLayout.setEmptyMessage(getActivity().getResources().getString(R.string.list_empty_view));
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
            }
            
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        if(getArguments().containsKey(ARG_ITEM_ID)){
        	channel = (Channel)getArguments().get(ARG_ITEM_ID);
        	
        	new Thread(new Runnable() {  
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
            }).start();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog_list, container, false); 
        
        listView = (SwipeListView)view.findViewById(R.id.blog_list);
        listView.setPullLoadEnable(true);
        listView.setPullRefreshEnable(true);
        listView.setXListViewListener(this);
        
        emptyLayout = new EmptyLayout(this.getActivity(), listView);
        emptyLayout.setLoadingMessage(getActivity().getResources().getString(R.string.content_loading));
        //emptyLayout.setLoadingAnimationViewId(emptyLayout.getLoadingAnimationViewId());
        emptyLayout.setLoadingAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
        emptyLayout.showLoading();
        
        title = (TextView)view.findViewById(R.id.bloglist_channel_title);
        title.setText(channel.Title + "-" + channel.UnreadCount);
        title.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v) {
        		listView.setSelection(0);
			}
        });
        
        if(channel.LastRefreshTime != null)
        	listView.setRefreshTime(DateHelper.DateToChineseString(channel.LastRefreshTime));
        else
        	listView.setRefreshTime(DateHelper.DateToChineseString(channel.LastUpdateTime));
    	
    	if (Build.VERSION.SDK_INT >= 11) {
    		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        
    	listView.setSwipeListViewListener(new BaseSwipeListViewListener() {
    		@Override
			public void onClickFrontView(int position) {
    			ListAdapter adapter = (ListAdapter) listView.getAdapter();
				mCallbacks.onItemSelected(
						position-1 >=0 ? (Blog)adapter.getItem(position-1) : null,
						(Blog)adapter.getItem(position),
						position+1 < adapter.getCount() ? (Blog)adapter.getItem(position+1) : null);
			}    		
		});

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = blogCallback;
    }    

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
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
    	listView.setRefreshTime(DateHelper.getDaysBeforeNow(channel.LastRefreshTime) + ReaderApp.getAppContext().getResources().getString(R.string.list_refreshtime));
	}
    
    private void onEmpty(){
    	final FeedlyParser feedly = new FeedlyParser();             
        
		Blog tmp = new Blog();
		tmp.TimeStamp = 0;
		tmp.PubDate = new Date();
		
        feedly.getRssBlog(channel, tmp, 30, new HttpResponseHandler(){
        	@Override
        	public <Blog> void onCallback(List<Blog> blogs, boolean result, String msg, boolean hasMore, int page){
        		if(result){
        			BlogDalHelper helper = new BlogDalHelper();
        			
        			helper.SynchronyData2DB((List<com.lgq.rssreader.entity.Blog>) blogs);
        			
        			helper.Close();
        			
        			if(blogs.size() > 0){
        				Message m = myHandler.obtainMessage();
        	            m.what = LOADDATA;
        	            m.obj = blogs;
        	            m.arg1 = 0;
        				myHandler.sendMessage(m);
        			}
        			
        			Helper.sound();
        		}else{
        			Message m = myHandler.obtainMessage();
    	            m.what = EMPTY;
    	            m.obj = blogs;
    	            m.arg1 = 0;
    				myHandler.sendMessage(m);
        			Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
        		}
        	}
        });
    }
    
	@Override
	public void onRefresh() {
		Blog b = (Blog)adapter.getItem(0);
		
		FeedlyParser parser = new FeedlyParser();
		
		parser.getRssBlog(channel, b, ReaderApp.getSettings().NumPerRequest, new HttpResponseHandler(){
        	@Override
        	public <Blog> void onCallback(final List<Blog> blogs, boolean result, String msg, boolean hasMore, int page){
        		if(result){
        			new Thread(){
        				public void run(){
        					BlogDalHelper helper = new BlogDalHelper();
                			helper.SynchronyData2DB((List<com.lgq.rssreader.entity.Blog>) blogs);
                			helper.Close();   
        				}
        			}.start();
        			
        			//only first page show in UI thread
        			if(blogs.size() > 0 && page == 0){
        				Message m = myHandler.obtainMessage();
        	            m.what = LOADDATA;
        	            m.obj = blogs;
        	            m.arg1 = hasMore ? 1 : 0;        	            
        				myHandler.sendMessage(m);
        			}
        			
        			if(hasMore){
        				Toast.makeText(ReaderApp.getAppContext(), ReaderApp.getAppContext().getResources().getString(R.string.list_loadingmore), Toast.LENGTH_SHORT).show();
        			}else{        				
        				Message m = myHandler.obtainMessage();
        	            m.what = ONLOAD;        	                    	           
        				myHandler.sendMessage(m);        				
        				Helper.sound();
        			}
        		}else{
        			Message m = myHandler.obtainMessage();
    	            m.what = ONLOAD;        	                    	           
    				myHandler.sendMessage(m);
        			Toast.makeText(ReaderApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();        			
        		}
        	}
		}, 0);
	}

	@Override
	public void onLoadMore() {
		
		new Thread(){
			public void run(){
			final BlogDalHelper helper = new BlogDalHelper();
			
			pageIndex = pageIndex + 1;
			
			List<Blog> blogs = helper.GetBlogList(channel, pageIndex, ReaderApp.getSettings().NumPerRequest, ReaderApp.getSettings().ShowAllItems);
			
			if(blogs.size()>0){
				Message m = myHandler.obtainMessage();                    				
	            m.what = LOADDATA;
	            m.obj = blogs;
	            m.arg1 = 0;
				myHandler.sendMessage(m);
				//getActivity().runOnUiThread(new Runnable(){public void run(){onLoad();}});
				
				helper.Close();
				
			}else{
				Blog b = (Blog)adapter.getItem(adapter.getCount() - 1);
				
				b.TimeStamp = -b.TimeStamp; 
				
				FeedlyParser parser = new FeedlyParser();
				
				parser.getRssBlog(channel, b, ReaderApp.getSettings().NumPerRequest, new HttpResponseHandler(){
		        	@Override
		        	public <Blog> void onCallback(final List<Blog> blogs, boolean result, String msg, boolean hasMore, int page){
		        		if(result){
		        			
		        			new Thread(){
		        				public void run(){
		        					helper.SynchronyData2DB((List<com.lgq.rssreader.entity.Blog>) blogs);
				        			helper.Close();
		        				}
		        			}.start();	        			
		        			
		        			if(blogs.size() > 0){
		        				Message m = myHandler.obtainMessage();                    				
		        	            m.what = LOADDATA;
		        	            m.obj = blogs;
		        	            m.arg1 = 0;
		        				myHandler.sendMessage(m);
		        			}
		        			
		        			if(hasMore){
		        				Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.list_loadingmore), Toast.LENGTH_SHORT).show();
		        			}
		        			else{
		        				Helper.sound();
		        			}
		        		}else{
		        			Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();        			
		        		}
		        		
		        		//onLoad();
		        	}
				});
			}
		}}.start();
		
		//onLoad();
	}
}
