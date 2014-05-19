package com.lgq.rssreader.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class ProfileTask extends AsyncTask<Profile, Void, Bitmap> {
    ImageView bmImage;
    Profile p;

    public ProfileTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(Profile... profiles) {
        p = profiles[0];
        Bitmap mIcon = null;
        try {
            InputStream in = new java.net.URL(p.Picture).openStream();
            
            //byte[] data = new byte[in.available()];
			//in.read(data,0, in.available());
			
			File SDFile = android.os.Environment.getExternalStorageDirectory();
			
			File profilePath = new File(SDFile.getAbsolutePath() + Config.PROFILE_PICTURE_LOCATION);
			
			if (!profilePath.exists()) 
				profilePath.mkdirs();
			
			File profile = new File(SDFile.getAbsolutePath() + Config.PROFILE_PICTURE_LOCATION + "profile.png");			
			
			if (!profile.exists()) {
				//profile.delete();
				profile.createNewFile();
			}
			
			FileOutputStream output = new FileOutputStream(profile);
			//loadingStream.write(data);
			int count = 0;
			byte[] buffer = new byte[4 * 1024];  
            while((count = in.read(buffer)) != -1){  
                output.write(buffer, 0, count);  
            }  
            output.flush();
			
            output.close();
			
            mIcon = BitmapFactory.decodeFile(SDFile.getAbsolutePath() + Config.PROFILE_PICTURE_LOCATION + "profile.png");
        } catch (Exception e) {
            Log.e("RssReader", e.getMessage());
            e.printStackTrace();
        }
        return mIcon;
    }

    protected void onPostExecute(Bitmap result) {
    	if(result != null){
    		bmImage.setImageBitmap(result);
    		
    		p.LocalPicture = Config.PROFILE_PICTURE_LOCATION + "profile.png";
    		
    		ReaderApp.setProfile(p);
    	}
    }
}
