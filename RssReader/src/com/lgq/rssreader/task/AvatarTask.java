package com.lgq.rssreader.task;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;

import org.json.JSONObject;

import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.utils.Helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class AvatarTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    Channel channel;

    public AvatarTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }
    
    public AvatarTask(ImageView bmImage, Channel c) {
        this.bmImage = bmImage;
        this.channel = c;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon = null;
        try {
        	String feedlyImgUrl = "http://img.feedly.com/img?url=" + URLEncoder.encode(urldisplay);
        	
            InputStream in = new java.net.URL(feedlyImgUrl).openStream();
            //mIcon = BitmapFactory.decodeStream(in);
            byte[] buf = getBytes(in);
            
            JSONObject result = new JSONObject(new String(buf));
            
            if(result.has("url")){
            	InputStream imgStream = new java.net.URL(result.getString("url")).openStream();
            	
            	byte[] imgBuf = getBytes(imgStream);
            	
            	mIcon = BitmapFactory.decodeByteArray(imgBuf, 0, imgBuf.length);
                
                if(channel != null){
                	Helper.SaveChannelIcon(mIcon, channel);            	
                }
            }
        } catch (Exception e) {
            Log.e("RssReader", e.getMessage());
            e.printStackTrace();
        }
        return mIcon;
    }

    protected void onPostExecute(Bitmap result) {
    	if(result != null){
    		//bmImage.setScaleType(ScaleType.CENTER_INSIDE);    		
    		bmImage.setImageBitmap(result);
    	}
    	else{
    		bmImage.setVisibility(View.GONE);
    	}
    }
    
    private byte[] getBytes(InputStream input)throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = 0;
        byte[] buffer = new byte[1024];

        while((len = input.read(buffer)) != -1){
            out.write(buffer, 0, len);
            out.flush();
        }
        out.close();
        return out.toByteArray();
    }
}
