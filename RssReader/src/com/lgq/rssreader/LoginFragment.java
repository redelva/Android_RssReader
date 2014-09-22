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
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.HttpAuthHandler;

@SuppressLint("SetJavaScriptEnabled")
public class LoginFragment extends Fragment {
	private WebView view;
	private ProgressDialog mProgressDialog;
	
	public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {        
		super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
		
        view = (WebView) rootView.findViewById(R.id.page);
        
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getActivity().getResources().getString(R.string.login_title));
        mProgressDialog.setMessage(getActivity().getResources().getString(R.string.login_msg));
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
        	        			        	 
        	        			if(getActivity() != null){
        	        				Intent intent = new Intent();
            	                    getActivity().setResult(Activity.RESULT_OK, intent);
            	                    getActivity().finish();
        	        			}
        	        		}
        	        	});
        			}
        		//}
        	}
        });
        
        view.loadUrl(Config.LOGIN_URL);        
		
		return rootView;
	}
}
