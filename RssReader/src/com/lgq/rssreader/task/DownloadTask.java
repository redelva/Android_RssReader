package com.lgq.rssreader.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;
import com.lgq.rssreader.MainActivity;
import com.lgq.rssreader.R;
import com.lgq.rssreader.cache.ImageCacher;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.enums.Token;
import com.lgq.rssreader.formatter.BlogFormatter.RenderCompleteHandler;
import com.lgq.rssreader.formatter.ContentFormatter;
import com.lgq.rssreader.formatter.SyncContentFormatter;
import com.lgq.rssreader.formatter.SyncDescriptionFormatter;
import com.lgq.rssreader.parser.FeedlyParser;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.readability.Readability;
import com.lgq.rssreader.utils.NotificationHelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * ��ʼ��������
 */
public class DownloadTask extends AsyncTask<List<Channel>, Integer, Void>
{
	//֪ͨ��
	private NotificationCompat.Builder builder;
	private NotificationManager notifyManager;

	Channel channel;//�û�
	int totalCount; //��������
	String currentText="";//��ʾ����
	ImageCacher imageCacher;
	Context context;
	boolean enableContent;
	boolean enableAll;
	boolean enableDescription;

	public DownloadTask(Context context, int count, boolean enableContent, boolean enableDescription){		
		this.totalCount = count;
		this.context = context;
		this.enableContent = enableContent;
		//this.enableAll = enableAll;
		this.enableDescription = enableDescription;
		imageCacher=new ImageCacher();//����ͼƬ
		notifyManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
		
		this.builder = NotificationHelper.notify(
				context,
				R.drawable.ic_launcher,
				context.getResources().getString(R.string.download),
				context.getResources().getString(R.string.offline_notification_start_toast));
	}	
	
	@Override
	protected void onPostExecute(Void v) {
        // Removes the progress bar
		builder.setContentText(currentText).setContentTitle("").setDefaults(R.raw.ms).setVibrate(new long[]{1000}).setProgress(0, 0, false);
		notifyManager.notify(0, builder.build());
	}
	
	@Override
	protected void onPreExecute() {
		// Sets an activity indicator for an operation of indeterminate length
		builder.setProgress(0, 0, true);
		// Issues the notification
		notifyManager.notify(0, builder.build());
	}
	
	/*
	 * ������
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		boolean indeterminate = values[1] == 1;
		
		String tips = context.getResources().getString(R.string.download);
		
		tips = tips + " " + context.getResources().getString(R.string.download_complete) + String.valueOf(values[0]) + "%";
		
		if(values[0] == 0)
			builder.setContentTitle(currentText).setProgress(100, values[0], indeterminate);
		else
			builder.setContentTitle(currentText).setContentText(tips).setProgress(100, values[0], indeterminate);
        // Displays the progress bar for the first time.
        notifyManager.notify(0, builder.build());
	}

	@Override
	protected Void doInBackground(List<Channel>... channels) {

		currentText = context.getResources().getString(R.string.download_start);
		Log.i("RssReader",currentText);
		
		publishProgress(0, 1);
		
		currentText = context.getResources().getString(R.string.download_blogstart);
		
		publishProgress(0, 1);
		
		FeedlyParser parser = new FeedlyParser();
		
		Blog tmp = new Blog();
		tmp.TimeStamp = 0L;
		tmp.PubDate = new Date();
		
		int allCount = totalCount * channels[0].size();
		
		for(int i=0, len=channels[0].size(); i < len; i++){
			channel = channels[0].get(i);
			
			List<Blog> blogs = parser.syncDownload(channel, totalCount);				
			currentText = context.getResources().getString(R.string.download_blogcomplete);
			
			publishProgress(0, 1);
			
			Log.i("RssReader", currentText);
			
			if(blogs != null && blogs.size() != 0){
				
				currentText = context.getResources().getString(R.string.download_blogcomplete);
				
				publishProgress(0, 0);
				
				SyncDescriptionFormatter desc = new SyncDescriptionFormatter();
				SyncContentFormatter content = new SyncContentFormatter();
				
				int count = 0 ;
				for(Blog b : blogs){
					
					if(enableDescription){
						b.Description = desc.Render(b);
						
						b.Description = ImageCacher.DownloadHtmlImage(b, b.Description);
					}				
					
					if(enableContent){
						b.Content = content.Render(b);

						b.Content = ImageCacher.DownloadHtmlImage(b, b.Content);
					}
					
					float progress = ((float)count + totalCount * i) * 100 /allCount;
					
					publishProgress((int)progress, 0);
					
					count++;
					
					Log.i("RssReader", "The " + String.valueOf(count) + " blog complete");
				}
				
				currentText = context.getResources().getString(R.string.download_complete) + " " + context.getResources().getString(R.string.download).toLowerCase();
				
				Log.i("RssReader", currentText);
				
				publishProgress(100, 0);
				
			}else{
				currentText = context.getResources().getString(R.string.download_complete) + " " + context.getResources().getString(R.string.download).toLowerCase();
			}
			
			BlogDalHelper helper = new BlogDalHelper();
			
			helper.SynchronyData2DB(blogs);
			
			helper.Close();
		}		
		
		return null;
	}		
}
