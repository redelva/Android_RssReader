package com.lgq.rssreader.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;


public class WebViewEx extends android.webkit.WebView {

	public WebViewEx(Context context) {
		super(context);		
	}
	
	public WebViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}

	public WebViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

	    if (event.getAction() == MotionEvent.ACTION_DOWN){
	        int temp_ScrollY = getScrollY();
	        scrollTo(getScrollX(), getScrollY() + 1);
	        scrollTo(getScrollX(), temp_ScrollY);

	    }

	    return super.onTouchEvent(event);
	}
	
	public void applyAfterMoveFix() { 
		onScrollChanged(getScrollX(), getScrollY(), getScrollX(), getScrollY()); 
	}

	public void setWebViewClient(WebViewClient webViewClient) {
		// TODO Auto-generated method stub
		super.setWebViewClient(webViewClient);
	}
}
