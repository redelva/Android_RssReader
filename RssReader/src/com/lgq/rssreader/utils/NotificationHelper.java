package com.lgq.rssreader.utils;

import static cn.sharesdk.framework.utils.R.getStringRes;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.sharesdk.framework.utils.UIHandler;

import com.lgq.rssreader.MainActivity;
import com.lgq.rssreader.R;
import com.lgq.rssreader.R.string;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.dal.ImageRecordDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.task.DownloadTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.opengl.Visibility;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NotificationHelper {
	
	public static void NotifyComplete(Context context, String completeText, NotificationCompat.Builder mBuilder)
	{
		NotificationManager mNotifyManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder.setContentText(completeText)
				// Removes the progress bar
				.setProgress(0,0,false);
        mNotifyManager.notify(0, mBuilder.build());
	}
	
	private static final int MSG_TOAST = 1;
	private static final int MSG_ACTION_CCALLBACK = 2;
	private static final int MSG_CANCEL_NOTIFY = 3;
	
	private static Handler.Callback callback = new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_TOAST: {
					String text = String.valueOf(msg.obj);
					Toast.makeText(ReaderApp.getAppContext(), text, Toast.LENGTH_SHORT).show();
				}
				break;
				case MSG_ACTION_CCALLBACK: {
					switch (msg.arg1) {
						case 1: {
							// 成功
							int resId = getStringRes(ReaderApp.getAppContext(), "share_completed");
							if (resId > 0) {
								showNotification(2000, ReaderApp.getAppContext().getString(resId));
							}
						}
						break;
						case 2: {
							// 失败
							String expName = msg.obj.getClass().getSimpleName();
							if ("WechatClientNotExistException".equals(expName)
									|| "WechatTimelineNotSupportedException".equals(expName)
									|| "WechatFavoriteNotSupportedException".equals(expName)) {
								int resId = getStringRes(ReaderApp.getAppContext(), "wechat_client_inavailable");
								if (resId > 0) {
									showNotification(2000, ReaderApp.getAppContext().getString(resId));
								}
							} else if ("GooglePlusClientNotExistException".equals(expName)) {
								int resId = getStringRes(ReaderApp.getAppContext(), "google_plus_client_inavailable");
								if (resId > 0) {
									showNotification(2000, ReaderApp.getAppContext().getString(resId));
								}
							} else if ("QQClientNotExistException".equals(expName)) {
								int resId = getStringRes(ReaderApp.getAppContext(), "qq_client_inavailable");
								if (resId > 0) {
									showNotification(2000, ReaderApp.getAppContext().getString(resId));
								}
							} else if ("YixinClientNotExistException".equals(expName)
									|| "YixinTimelineNotSupportedException".equals(expName)) {
								int resId = getStringRes(ReaderApp.getAppContext(), "yixin_client_inavailable");
								if (resId > 0) {
									showNotification(2000, ReaderApp.getAppContext().getString(resId));
								}
							} else {
								int resId = getStringRes(ReaderApp.getAppContext(), "share_failed");
								if (resId > 0) {
									showNotification(2000, ReaderApp.getAppContext().getString(resId));
								}
							}
						}
						break;
						case 3: {
							// 取消
							int resId = getStringRes(ReaderApp.getAppContext(), "share_canceled");
							if (resId > 0) {
								showNotification(2000, ReaderApp.getAppContext().getString(resId));
							}
						}
						break;
					}
				}
				break;
				case MSG_CANCEL_NOTIFY: {
					NotificationManager nm = (NotificationManager) msg.obj;
					if (nm != null) {
						nm.cancel(msg.arg1);
					}
				}
				break;
			}
			return false;
		}
	};
	
	// 在状态栏提示分享操作
	public static void showNotification(long cancelTime, String text) {
		try {
			Context app = ReaderApp.getAppContext().getApplicationContext();
			NotificationManager nm = (NotificationManager) app
					.getSystemService(Context.NOTIFICATION_SERVICE);
			final int id = Integer.MAX_VALUE / 13 + 1;
			nm.cancel(id);				
			
			long when = System.currentTimeMillis();
			Notification notification = new Notification(R.drawable.ic_launcher, text, when);
			PendingIntent pi = PendingIntent.getActivity(app, 0, new Intent(), 0);
			notification.setLatestEventInfo(app, ReaderApp.getAppContext().getString(R.string.app_name), text, pi);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			nm.notify(id, notification);

			if (cancelTime > 0) {
				Message msg = new Message();
				msg.what = 3;
				msg.obj = nm;
				msg.arg1 = id;
				UIHandler.sendMessageDelayed(msg, cancelTime, callback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
    * ����֪ͨ
    * @param context ������
    * @param id ֪ͨID
    * @param icon ͼ��
    * @param text ״̬������
    * @param title ֪ͨ������
    * @param content ֪ͨ������
    * @param intent
    */
    @SuppressLint("NewApi")
	public static NotificationCompat.Builder notify(Context context, int icon, String text, String title) {
    	
    	NotificationManager mNotifyManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
    	mBuilder
    		.setContentTitle(title)
    	    .setContentText(text)
    	    .setSmallIcon(R.drawable.ic_launcher);    	
    	mNotifyManager.notify(0, mBuilder.build());
    	
    	return mBuilder;
    }
    
    public static AlertDialog getDownloadDialog(final Context mContext, final Channel c, boolean showAll){
    	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		AlertDialog alertDialog = null;
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View layout = inflater.inflate(R.layout.dialog_download, null);
		
		final SeekBar seekBar = (SeekBar) layout.findViewById(R.id.seekBar);
		final CheckBox chkDescription = (CheckBox) layout.findViewById(R.id.chkDescription);
		final CheckBox chkContent = (CheckBox) layout.findViewById(R.id.chkContent);
		final View chkAllContainer = layout.findViewById(R.id.chkAllContainer);
		final CheckBox chkAll = (CheckBox)layout.findViewById(R.id.chkAll);
		final CheckBox chkLastest = (CheckBox)layout.findViewById(R.id.chkLastest);
		final TextView tvSeekBar = (TextView) layout.findViewById(R.id.tvSeekBar);
		
		if(showAll)
			chkAllContainer.setVisibility(View.VISIBLE);
		else
			chkAllContainer.setVisibility(View.GONE);
		
		DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case Dialog.BUTTON_POSITIVE ://
						if (!chkDescription.isChecked() && !chkContent.isChecked()) {
							Toast.makeText(mContext, R.string.dialog_no_select_download, Toast.LENGTH_SHORT).show();
							return;
						}
						
						if (!chkAll.isChecked() && !chkLastest.isChecked()) {
							Toast.makeText(mContext, R.string.dialog_no_select_target, Toast.LENGTH_SHORT).show();
							return;
						}
						
						int size = seekBar.getProgress();
						if (size == 0) {
							Toast.makeText(mContext,
									R.string.dialog_no_select_download,
									Toast.LENGTH_SHORT).show();
							return;
						}									
								
						DownloadTask task = new DownloadTask(
								mContext, 
								seekBar.getProgress(), 
								chkContent.isChecked(), 
								chkDescription.isChecked());
						
						List<Channel> channels;
						if(chkAll.isChecked()){
							channels = Helper.getChannels();
						}else{
							channels = new ArrayList<Channel>();
							channels.add(c);
						}
						
						task.execute(channels);
						
						break;
					case Dialog.BUTTON_NEGATIVE :// ȡ��
						break;
				}
			}
		};
		/**
		 * �϶�SeekBar�¼�
		 */
		OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int seekValue = seekBar.getProgress();
				// ֻ��ѡ��10�ı���
				int consult = seekValue / 10;
				if (seekValue < consult * 10 - 5) {
					seekValue = (consult - 1) * 10;
				} else {
					seekValue = consult * 10;
				}
				if (seekValue < 10) {
					seekValue = 10;
				}
				seekBar.setProgress(seekValue);

				String text = mContext.getString(R.string.dialog_select_nums_tips);
				text = text.replace("{0}", String.valueOf(seekValue));
				tvSeekBar.setText(text);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		};

		alertDialog = builder
				.setTitle(R.string.dialog_offline_bar_title)
				.setView(layout)
				.setPositiveButton(R.string.dialog_btn_start_download,
						clickListener)
				.setNeutralButton(R.string.no,
						clickListener).create();
		
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		// ��ǰ��������
		int seekValue = seekBar.getProgress();
		String text = mContext.getString(R.string.dialog_select_nums_tips);
		text = text.replace("{0}", String.valueOf(seekValue));
		tvSeekBar.setText(text);
		
		return alertDialog;
    }
    
    public static Dialog buildLoginDialog(Context context, String url){
    	AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle(R.string.login_msg);

        WebView wv = new WebView(context);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl(url);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        alert.setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        Dialog d = alert.setView(wv).create();        
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.FILL_PARENT;
        d.getWindow().setAttributes(lp);
        
        return d;
    }
    
    public static AlertDialog BuildDialogForClean(Context context, long size){
    	//final TextView msg = new TextView(context);					
    	//msg.setText(String.format(context.getResources().getString(R.string.cache_msg), String.valueOf(size)));
    	AlertDialog dialog = new AlertDialog.Builder(context)  
	     	.setIcon(android.R.drawable.btn_star_big_on)  
	     	//.setTitle(R.string.cache_clean)
	     	.setTitle(String.format(context.getResources().getString(R.string.cache_msg), String.valueOf(size)))
	     	//.setView(msg)				     	
	     	.setPositiveButton(R.string.yes, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.i("RssReader", "You had clicked " + which);
					new Thread(){
						@Override
						public void run() {
							//delete blog a month ago
							Calendar calendar = Calendar.getInstance();					
							calendar.add(Calendar.DATE, -3);    //得到前一个星期
							BlogDalHelper blogHelper = new BlogDalHelper();
							ImageRecordDalHelper imgHelper = new ImageRecordDalHelper();
							//List<Blog> toDeleteBlogs = blogHelper.GetBlogList(calendar.getTime());
							
							//find related imgs by blogid
							//List<ImageRecord> records = imgHelper.GetImageRecordByBlog(toDeleteBlogs);
							
							//List<Blog> toDeleteBlogs = blogHelper.GetBlogList(0.2);
							List<ImageRecord> records = imgHelper.GetImageRecordList(calendar.getTime());
							
							List<String> blogIDs = new ArrayList<String>();
							for(ImageRecord record : records){
								blogIDs.add(record.BlogId);
							}
							
							String sDStateString = android.os.Environment.getExternalStorageState();

							if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
								try {														
									File SDFile = android.os.Environment.getExternalStorageDirectory();
									
									for(ImageRecord record: records){
										File img = new File(SDFile.getAbsolutePath() + record.StoredName);
										if(img.exists()){
											img.delete();
										}
									}
									
									blogHelper.DeleteBlog(blogIDs);
									imgHelper.DeleteRecords(records);
								}
								catch(Exception e){
									Log.e("RssReader", e.getMessage());
								}
							}
							
							blogHelper.Close();
							imgHelper.Close();
						}
					}.start();
				}
			})
	     	.setNegativeButton(R.string.no,  null).create();
    	
    	return dialog;
    }
}
