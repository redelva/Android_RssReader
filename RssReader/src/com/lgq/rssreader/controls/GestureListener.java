package com.lgq.rssreader.controls;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Toast;

import com.lgq.rssreader.R;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.enums.MoveDirection;

public class GestureListener extends GestureDetector.SimpleOnGestureListener implements OnScaleGestureListener {
	int oldEv = MotionEvent.ACTION_CANCEL;
    boolean isScrolling = false;
    MoveDirection direction;
    float xDistance = 0;
    int SWIPE_THRESHOLD_DISTANCE = -1;
    IGestureListener listner;
    View target;
    
    public interface IGestureListener{
    	public void onLoad();
    	public void onLeft();
    	public void onRight();
    	public void onScale(double scale);
    }
	
	public GestureListener(IGestureListener iGestureListener, View target, int threshold) {
		this.listner = iGestureListener;
		this.target = target;
		SWIPE_THRESHOLD_DISTANCE = threshold;
	}

	@Override
    public boolean onDoubleTap(MotionEvent e) {
		
		if(listner != null){
			listner.onLoad();
		}
		
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	
    	xDistance = xDistance + distanceX;
    	    	
    	//if(Math.abs(distanceX) > 50){
    	if(Math.abs(xDistance) > SWIPE_THRESHOLD_DISTANCE){
    		if(distanceX > 0 )
        		direction = MoveDirection.Left;
        	else
        		direction = MoveDirection.Right;                		
    		
    		TranslateAnimation animation = new TranslateAnimation(target.getX() - xDistance, target.getX(),0,0);
        	
        	animation.setDuration(1000);
        	
        	target.startAnimation(animation);
        	
        	animation.start();
    		
        	isScrolling = true;
    	}
    	
    	return true;
    }
    
    public void onScrollComplete(MotionEvent e){
    	isScrolling = false;    	
    	    	    
    	Log.i("RssReader", "Scroll ends");
    	
    	if(Math.abs(xDistance) > SWIPE_THRESHOLD_DISTANCE){
    		
    		if(direction != MoveDirection.Left){
        		//012=>201    			
    			
    			listner.onLeft();
        	}else{
        		//move left 012=>120
        		listner.onRight();
        	}
    	}    	
    	
    	xDistance = 0;
    }
    
    @Override 
    public boolean onScale(ScaleGestureDetector detector) {         
        return false; 
    } 

    @Override 
    public boolean onScaleBegin(ScaleGestureDetector detector) { 
        return true; 
    } 

    @Override 
    public void onScaleEnd(ScaleGestureDetector detector) { 
        float scale = detector.getScaleFactor();
    	
    	listner.onScale(scale);
    }
}
