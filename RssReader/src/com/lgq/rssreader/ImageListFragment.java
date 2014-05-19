package com.lgq.rssreader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;  
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.lgq.rssreader.R.id;
import com.lgq.rssreader.adapter.BlogAdapter;
import com.lgq.rssreader.adapter.ChannelAdapter;
import com.lgq.rssreader.adapter.ImageAdapter;
import com.lgq.rssreader.controls.PullToRefreshListView;
import com.lgq.rssreader.controls.XListView;
import com.lgq.rssreader.controls.XListView.IXListViewListener;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.dal.ImageRecordDalHelper;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.task.ImageTask;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.entity.*;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.enums.RssTab;

/**
 * A list fragment representing a list of Blogs. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link BlogContentFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ImageListFragment extends SherlockFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    
    /**
     * The fragment's data source of gallery tab
     */
    private List<ImageRecord> records;
    
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    
    /**
     * The current adapter for list.
     */
    private ListAdapter adapter;
    
    private int page;
    
    private XListView listView;
    
    private ImageView currentImage;
    
    public XListView getListView(){return listView;}
        
    public Handler myHandler = new Handler(){
        @Override  
        public void handleMessage(Message msg) {            
            
			if(getView() != null){
				records = (List<ImageRecord>)msg.obj; 
				adapter = new ImageAdapter(
                    getActivity(),
                    records,
                    listView
                    );
				listView.setAdapter(adapter);
				
				if(records.size() > 0){
					ImageRecord obj = records.get(0);        
			        
					File SDFile = android.os.Environment.getExternalStorageDirectory();
					
					Bitmap bm = BitmapFactory.decodeFile(SDFile.getAbsolutePath() + obj.StoredName);
					currentImage.setImageBitmap(bm);
				}
			}       
            super.handleMessage(msg);
        }
    };
    
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImageListFragment() {
    	
    }    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }
    
    @Override
    public void onStart(){
    	super.onStart();    	
    }
    
    @Override    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        View emptyView = inflater.inflate(R.layout.listview_empty, container, false);
        
        listView = (XListView)rootView.findViewById(id.image_list);
        currentImage = (ImageView)rootView.findViewById(id.currentimage);
        
        listView.setEmptyView(emptyView);
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> view, View v, int position, long id) {
				ImageRecord obj = (ImageRecord)adapter.getItem(position - 1);        
		        
				File SDFile = android.os.Environment.getExternalStorageDirectory();
				
				Bitmap bm = BitmapFactory.decodeFile(SDFile.getAbsolutePath() + obj.StoredName);
				currentImage.setImageBitmap(bm);
			}
        });
        
        page = 1;
        
        listView.setXListViewListener(new IXListViewListener(){

			@Override
			public void onRefresh() {				
				if(page > 1){
					page--;
					new Thread(new Runnable() {  
			            @Override  
			            public void run() {
							Message m = myHandler.obtainMessage();
				            //m.what = tab.ordinal();
				            m.obj = new ImageRecordDalHelper().GetImageRecordListByPage(page, 10);;
							myHandler.sendMessage(m);
						}
					}).start();
				}
			}

			@Override
			public void onLoadMore() {
				
				final int temp = page + 1;
				
				new Thread(new Runnable() {  
		            @Override  
		            public void run() {
				
						List<ImageRecord> records = new ImageRecordDalHelper().GetImageRecordListByPage(temp, 10);
						
						if(records.size() > 0){
							Message m = myHandler.obtainMessage();
				            //m.what = tab.ordinal();
							page = temp;
							m.obj = records;
							myHandler.sendMessage(m);
						}
		            }
				}).start();
			}
        });
        
        loadData();
		listView.setPullLoadEnable(true);
		listView.setPullRefreshEnable(true);
    	
    	return rootView;
    }
    
    public void loadData(){
    	new Thread(new Runnable() {  
            @Override  
            public void run() {
            	Message m = myHandler.obtainMessage();
	            m.what = page;
	            m.obj = new ImageRecordDalHelper().GetTopImageList();
				myHandler.sendMessage(m);
            }
        }).start();
    }
       
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	
    	if (bMenu) {
            bMenu=false;
            
        }
    	
    	return super.onContextItemSelected(item);
    }
    
    boolean bMenu=true;
    
    //�������Ĳ˵��ر�ʱ���õķ���  
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.contextmenu, (Menu) menu);
        super.onCreateContextMenu(menu, v, menuInfo);
        bMenu=true;
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
}
