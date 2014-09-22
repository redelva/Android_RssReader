package com.lgq.rssreader.utils;

import com.lgq.rssreader.enums.AccountType;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class ShareHelper {
	private static Activity parentPage;
    public static WebView browserControl;
    public static View parentControl;
    static LinearLayout.LayoutParams params;
    
	public static void buildLoginPage(final Activity page, AccountType accountType){
		parentControl = page.getWindow().getDecorView().getRootView();		
		//page.getWindow().getDecorView().findViewById(android.R.id.content);
		//parentControl = ((ViewGroup)page.findViewById(android.R.id.content)).getChildAt(0);
		 
		  
		//page.BackKeyPress += new EventHandler<System.ComponentModel.CancelEventArgs>(page_BackKeyPress);
        //page.OrientationChanged += new EventHandler<OrientationChangedEventArgs>(page_OrientationChanged);
        double w = parentControl.getWidth();
        double h = parentControl.getHeight();

        browserControl = new WebView(page);
        params = new LinearLayout.LayoutParams((int)w, (int)h);    
        params.setMargins(5, 5, 5, 5);
        browserControl.setLayoutParams(params);
        
        browserControl.setOnKeyListener(new OnKeyListener() {  
		    @Override  
		    public boolean onKey(View v, int keyCode, KeyEvent event) {  
		        // TODO Auto-generated method stub  
		        if ((keyCode == KeyEvent.KEYCODE_BACK)) {  
		            
		        	//parentPage.Content = parentControl;
		        	parentPage.setContentView(browserControl, params);
		        	
		        	return true;  
		        }  
		        return false;  
		    }
		});
         
        page.setContentView(browserControl, params);
         
        parentPage = page;
	}
	
	public static void removeBrowser(){		
		parentPage.setContentView(browserControl, params);
	}
}
