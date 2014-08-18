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
    float yDistance = 0;
    int SWIPE_THRESHOLD_XDISTANCE = -1;
    int SWIPE_THRESHOLD_YDISTANCE = -1;
    IGestureListener listner;
    View target;
    
    public interface IGestureListener{
    	public void onDoubleTap();
    	public void onLeft();
    	public void onRight();
    	public void onDown();
    	public void onUp();
    	public void onScale(double scale);
    }
	
	public GestureListener(IGestureListener iGestureListener, View target, int xThreshold, int yThreshold) {
		this.listner = iGestureListener;
		this.target = target;
		SWIPE_THRESHOLD_XDISTANCE = xThreshold;
		SWIPE_THRESHOLD_YDISTANCE = yThreshold;
	}

	@Override
    public boolean onDoubleTap(MotionEvent e) {
		
		if(listner != null){
			listner.onDoubleTap();
		}
		
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	
    	xDistance = xDistance + distanceX;
    	yDistance = yDistance + distanceY;
    	    	
    	//if(Math.abs(distanceX) > 50){
    	if(Math.abs(xDistance) > SWIPE_THRESHOLD_XDISTANCE && Math.abs(yDistance) < SWIPE_THRESHOLD_YDISTANCE){
    		if(distanceX > 0 )
        		direction = MoveDirection.Left;
        	else
        		direction = MoveDirection.Right;                		
    		
    		TranslateAnimation animation = new TranslateAnimation(target.getX() - xDistance, target.getX(),0,0);
        	
        	animation.setDuration(1000);
        	
        	target.startAnimation(animation);
        	
        	animation.start();
    		
        	isScrolling = true;
        	
        	return true;
    	}
    	
    	if(Math.abs(xDistance) < SWIPE_THRESHOLD_XDISTANCE && Math.abs(yDistance) > SWIPE_THRESHOLD_YDISTANCE){
    		
    		if(distanceY > 0 )
        		direction = MoveDirection.Down;
        	else
        		direction = MoveDirection.Up;
    		
        	isScrolling = true;
        	
        	return true;
    	}
    	
    	
    	return true;
    }
        
    public void onScrollComplete(MotionEvent e){
    	isScrolling = false;    	
    	    	    
    	Log.i("RssReader", "Scroll ends");
    	
    	if(Math.abs(xDistance) > SWIPE_THRESHOLD_XDISTANCE){    		
    		if(direction == MoveDirection.Right){
        		listner.onLeft();
        	}
    		if(direction == MoveDirection.Left){
        		listner.onRight();
        	}    		
    	}
    	
		if(Math.abs(yDistance) > SWIPE_THRESHOLD_YDISTANCE){
			if(direction == MoveDirection.Down){
	    		listner.onDown();
	    	}			
    	}
		
		if(e.getAction() == MotionEvent.ACTION_UP && Math.abs(yDistance) * 2 > SWIPE_THRESHOLD_YDISTANCE){
			if(direction == MoveDirection.Up){
	    		listner.onUp();
	    	}
		}
    	
    	xDistance = 0;
    	yDistance = 0;
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
