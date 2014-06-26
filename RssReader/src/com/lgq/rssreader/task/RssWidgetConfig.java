package com.lgq.rssreader.task;

import com.google.gson.Gson;
import com.lgq.rssreader.R;
import com.lgq.rssreader.adapter.ChannelAdapter;
import com.lgq.rssreader.adapter.WidgetChannelAdapter;
import com.lgq.rssreader.adapter.WidgetChannelAdapter.ViewHolder;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.utils.Helper;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

public class RssWidgetConfig extends Activity{
	int mAppWidgetId;
	private Channel c;
    
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        // TODO Auto-generated method stub  
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);
          
        Log.i("RssReader"," on WidgetConf ... ");  
          
        setResult(RESULT_CANCELED);  
          
        // Find the widget id from the intent.  
        Intent intent = getIntent();  
        Bundle extras = intent.getExtras();  
        if (extras != null) {  
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);  
        }  
  
        // If they gave us an intent without the widget id, just bail.  
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {  
            finish();  
        }
        
        Button btnOk = (Button)findViewById(R.id.widget_ok);
        
        btnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// return OK  
		        Intent resultValue = new Intent();  
		        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);  		        
		        
		        SharedPreferences.Editor prefs = ReaderApp.getAppContext().getSharedPreferences("Widget_Channel", 0).edit(); 
		        prefs.putString("Widget_Channel" + mAppWidgetId, new Gson().toJson(c)); 
		        prefs.commit(); 
		          
		        setResult(RESULT_OK, resultValue);
		        
		        finish();  
			}
        });
        
        final ListView listView = (ListView)findViewById(R.id.widget_channels);
        
        if(listView.getAdapter() == null){
			WidgetChannelAdapter adapter = new WidgetChannelAdapter(
                    this,
                    Helper.getChannels(),
                    listView);
			listView.setAdapter(adapter);	                			
		}else{
			HeaderViewListAdapter wrap = (HeaderViewListAdapter)listView.getAdapter();
			((ChannelAdapter)wrap.getWrappedAdapter()).ResetData(Helper.getChannels());
		}
        
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	ViewHolder holder = (ViewHolder) view.getTag();
                holder.channel_select.toggle();
                
                if(WidgetChannelAdapter.getIsSelected().containsValue(true)){
                	Integer[] data = new Integer[WidgetChannelAdapter.getIsSelected().size()];
                	
                	int lastPosition = 0;
                	for(int i =0; i< data.length;i++){
                		if(WidgetChannelAdapter.getIsSelected().get(i)){
                			lastPosition = i;
                			break;
                		}
                	}                	
                	                	
                	View wantedView = listView.getChildAt(lastPosition);
                	
                	ViewHolder previousHolder = (ViewHolder)wantedView.getTag();
                	
                	previousHolder.channel_select.setChecked(false);
                	WidgetChannelAdapter.getIsSelected().put(lastPosition, false);
                }
                
                if(holder.channel_select.isChecked()){
                	WidgetChannelAdapter.getIsSelected().put(position, holder.channel_select.isChecked());
                
                	c = (Channel)parent.getItemAtPosition(position);
                
                	Toast.makeText(view.getContext(), ReaderApp.getAppContext().getResources().getString(R.string.widget_youhadchosed) + c.Title, Toast.LENGTH_SHORT).show();
            	}                
            }
        });
    }
}
