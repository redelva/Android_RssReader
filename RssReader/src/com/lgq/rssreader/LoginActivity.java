package com.lgq.rssreader;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.enums.Token;
import com.lgq.rssreader.parser.FeedlyParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginActivity extends BaseActivity {
	
	public LoginActivity(){
		super("#00BCD5");
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
	}
     
	private void initViews(){
        WebView view = (WebView) findViewById(R.id.page);
        
        final ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(R.string.login_title));
        mProgressDialog.setMessage(getResources().getString(R.string.login_msg));
        mProgressDialog.show();
        
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        
        view.setWebViewClient(new WebViewClient(){
//        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				Log.i("RssReader", url);
//				return true;
//			}
//        	
//        	public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm){
//        		Log.i("RssReader", host + "-" + realm);
//        	}
        	
        	public void onPageFinished (WebView view, String url){
        		Log.i("RssReader", "finished " + url);
        		
        		//if(url == Config.LOGIN_URL){
        			mProgressDialog.hide();
        		//}else{
        			if(url.contains("code=")){
        				mProgressDialog.setMessage(ReaderApp.getAppContext().getResources().getString(R.string.login_authing));
        				mProgressDialog.show();
        				
        				String params = url.substring(url.indexOf("?") + 1);
        				
        				String code = "";
        				
        				for(String p : params.split("&")){
        					if(p.contains("code")){
        						code = p.split("=")[1];
        						break;
        					}
        				}
        				
        				AsyncHttpClient client = new AsyncHttpClient();
        				
        				String actionParams = "client_id=feedly&client_secret=0XP4XQ07VVMDWBKUHTJM4WUQ&grant_type=authorization_code&" +
        						"redirect_uri=http%3A%2F%2Fwww.feedly.com%2Ffeedly.html&code=" + code;
        				
        				StringEntity se = null;
        	        	try {
        	        	  se = new StringEntity(actionParams.toString());
        	        	} catch (UnsupportedEncodingException e) {
        	        	  e.printStackTrace();
        	        	  return;
        	        	}
        	        	
        	        	client.post(null, FeedlyParser.FEEDLYLOGINURL, se, " application/x-www-form-urlencoded", new JsonHttpResponseHandler(){
        	        		
        	        		public void onSuccess(JSONObject result){
        	        			try {
									ReaderApp.setToken(Token.AccessToken, result.getString("access_token"));
									ReaderApp.setToken(Token.RefreshToken, result.getString("refresh_token"));
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
        	        			
        	        			mProgressDialog.dismiss();
        	        			        	 
        	        			Intent intent = new Intent();
    	        				setResult(Activity.RESULT_OK, intent);
    	        				
    	        				LoginActivity.this.finish();
        	        		}
        	        	});
        			}
        		//}
        	}
        });
        
        view.loadUrl(Config.LOGIN_URL);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ) {
			if(ReaderApp.getProfile() == null)
				return false;
			else
				return super.onKeyDown(keyCode, event);
		}
		
		return super.onKeyDown(keyCode, event);
	}
}
