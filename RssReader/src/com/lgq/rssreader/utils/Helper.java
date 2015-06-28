package com.lgq.rssreader.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;
import com.lgq.rssreader.R;
import com.lgq.rssreader.cache.ImageCacher;
import com.lgq.rssreader.controls.SystemBarTintManager;
import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.ImageRecordDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.entity.UnReadCount;
import com.lgq.rssreader.entity.Unread;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Helper {
	
	/**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    
    /**
     * 获得设备的屏幕高度
     *
     * @param context
     * @return
     */
    public static int getDeviceHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getHeight();
    }
    
    /**
     * 获取状态栏高度＋标题栏高度
     *
     * @param activity
     * @return
     */
    public static int getTopBarHeight(Activity activity) {
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }
	
	public static void saveHtml(Context context, boolean deleteFile)
    {
        String deviceWidth = "360";
        
        String fontFamily = "font-family:arial;";
        
        StringBuilder sb = new StringBuilder();
        
        List<File> fonts = detectFonts();
        
        for(File file : fonts){
        	sb.append("@font-face {");
        	sb.append("font-family: \"" + file.getName().substring(0, file.getName().indexOf(".")) + "\"; ");
        	sb.append("src: url('" + file.getPath() + "'); } ");
        }
        
        if(ReaderApp.getSettings().Font >= fonts.size()){        	
            fontFamily = "font-family:Droid-Sans";
        }else{
        	File currentFont = fonts.get(ReaderApp.getSettings().Font);
            fontFamily = "font-family:" + currentFont.getName().substring(0, currentFont.getName().indexOf("."));
        }        

        String html = 
    		"<html>" +
        		"<head>" + 
    				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" + 
	                "<meta name=\"viewport\" id=\"viewer\" content=\"width=" + deviceWidth + ",target-densitydpi=medium-dpi,initial-scale=1.0,minimum-scale=1.0,user-scalable=no\" />" +
                    "<script type=\"text/javascript\">" +
                        "function fontsize(size) {" +
                            "document.getElementById('body').style.fontSize = size + 'pt';" +                
                        "}" +
                        "function lineheight(height) {" +
	                        "document.getElementById('body').style.lineHeight = height + '%';" +                
	                    "}" +
                        "function backgroundColor(color) {" +
                            "document.getElementById('body').style.backgroundColor = color ;" +
                        "}" +
                        "function fontColor(color) {" +
                            "document.getElementById('body').style.color = color;" +
                        "}" +
                        "function fontFamily(family) {" +
	                        "document.getElementById('body').style.fontFamily = family;" +
	                    "}" +
                        "function moveToTop() {" +
                            "window.scrollTo(0,0);" +
                        "}" +
                        "function linkHandle() {" +
                            "var e = this.event;" + 
                            "if(e.srcElement.getAttribute('videoLink') != null)" +
                                " window.external.notifyJava('LinkHandle' + e.srcElement.getAttribute('videoLink')); " +
                            "else if(e.srcElement.href != undefined)" +
                                " window.external.notifyJava('LinkHandle' + e.srcElement.href); " +
                            "if (e && e.preventDefault) " +
                                " e.preventDefault(); " +
                            "else " +
                                " window.event.returnValue = false; " +
                            " return false; " +
                        "}" +
                        "function replaceFlash(id, html, title, enableSeperate) {" +
                            "var x = document.getElementById('msg' + id); " +
                            "if(x == undefined)" +
                                "return;" +
                            "if(html.length == 0){ " +                
                                "var y = document.getElementById('click' + id + '0'); " +
                                "y.parentNode.removeChild(y); " +
                                "x.innerHTML = title; " +
                                "x.style.color = 'red'; " +
                                "x.style.display = 'block'; " +                
                            "}" +
                            "if(html.length != 0){ " +
                                "x.style.display = 'none'; " +
                                "if(html.indexOf('____')!=-1){" +
                                    "var result = html.split('____');" +
                                    "var y = document.getElementById('click' + id + '0'); " +
                                    "if(enableSeperate == 'True' && result[0].indexOf('|')!=-1){" +
                                        "var links = result[0].split('|');" +
                                        "var y = document.getElementById('click' + id + '0'); " +
                                        "y.href = links[0]; " +
                                        "y.innerHTML = title + 0; " +
                                        "var count = links.length;" +
                                        "if(count > 20)" +
                                            "count = 20;" +
                                        "for(var i=1; i<count;i++){" +
                                            "var br = document.createElement('br'); " +                
                                            "var newLink = document.getElementById('click' + id + i.toString()); " +
                                            "newLink.href = links[i];" +                
                                            "newLink.innerHTML = title + i;" +
                                            "y.parentNode.insertBefore(br,newLink);" +                
                                        "}" +
                                        "var preview = document.createElement('img'); " +
                                        "preview.src = result[1];" +
                                        "preview.style.width = \"98%\";" +
                                        "preview.style.margin = 5;" +
                                        "y.parentNode.appendChild(preview);" +
                                        "var z = document.getElementById('flash' + id); " +
                                        "z.parentNode.removeChild(z);" +

                                    "}" +
                                    "else{" +
                                    	//Due to the limitation of tudou video should play in same UA, try to use html5 video
                                    	"if(result[0].length > 0 && (result[0].indexOf('vr.tudou') != -1 || result[0].indexOf('ku6') != -1)){" +
	                                    	"var video = document.createElement('video'); " +
	                                    	"video.src= result[0];" +
	                                    	"video.poster= result[1]; " +
	                                    	"video.style.width=\"100%\";"+
	                                    	"video.controls= \"controls\"; " +
	                                    	"video.type= \"video/mp4\"; " +
	                                    	
											"var y = document.getElementById('click' + id + '0'); " +
											"y.parentNode.appendChild(video);" + 
                                    	"}else{" +                                   	
										
	                                        "var y = document.getElementById('click' + id + '0'); " +
	                                        "y.href = result[0]; " +
	                                        "y.parentNode.style.height = 270; " + 
	                                        "y.parentNode.style.width = '100%'; " +
	                                        "y.style.left = 8; " +  
	                                        "y.style.position = 'absolute';" +
	                                        "var imgDiv = document.createElement('div'); " +
	                                        "imgDiv.style.position = 'absolute';" +                                        
	                                        "imgDiv.style.magin = 'auto';" +                                        
	                                        "imgDiv.style.height = 270;" +
											"imgDiv.style.width = '100%';" +
	
	                                        "imgDiv.setAttribute('videoLink', result[0]); " +
	                                        "var img = document.createElement('img'); " +                                        
	                                        
	                                        "if(result[1].length > 0) " +
	                                        	"img.src = result[1];" +                                        
	                                        "else{" +
	                                        	"imgDiv.style.background = \"black\";" +                                        	
	                                        "}" + 
	                                        "img.style.left = 0;" +
	                                        "img.style.bottom = 0;" +                                        
	                                        "img.style.top = 0;" +
	                                        "img.style.right = 0;" +
	                                        "img.style.width = \"100%\";" +
	                                        "img.style.position = 'absolute';" +
	                                        "img.style.margin = 'auto';" +
	                                        "img.setAttribute('videoLink', result[0]); " +
	                                        "imgDiv.appendChild(img);" +
	
	                                        "var coverDiv = document.createElement('div'); " +
	                                        "coverDiv.style.position = 'absolute';" +
	                                        "coverDiv.style.left = 0;" +
	                                        "coverDiv.style.top = 0;" +
	                                        "coverDiv.setAttribute('videoLink', result[0]); " +
	                                        "var cover = document.createElement('img'); " +
	                                        "cover.setAttribute('videoLink', result[0]); " +
	                                        "cover.style.height = \"auto\";" +
	                                        "cover.setAttribute('src', 'videostart.png'); " +
	                                        //"cover.style.width = \"" + deviceWidth + "\";" + 
	                                        "cover.style.width = '100%';" +
	                                        "coverDiv.appendChild(cover);" +
	
	                                        "y.appendChild(imgDiv);" +
	                                        "y.appendChild(coverDiv);" +
	                                        "y.style.width = \"96%\";" +
	                                        "y.style.height = \"auto\";" +
                                        
                                        "}" +
//										"window.external.notifyJava(\"y.width=\" + y.style.width + \"img.width=\" + img.style.width + \"imgDiv.width=\" + imgDiv.style.width+ \"window.width=\" + document.width);" +
                                    "}" +
                                "}" +
                            "}" +
                        "}" +
                        "function setImageSize() {" +
                            "var images = content.getElementsByTagName('img');" +
                            "for (var i = 0; i < images.length; i++) {" +
                                "images[i].removeAttribute(\"src\");" +
                                "images[i].removeAttribute(\"class\");" +
                                "images[i].removeAttribute(\"style\");" +
                                "images[i].removeAttribute(\"width\");" +
                                "images[i].removeAttribute(\"height\");" +
								"cur.style.width = '"+deviceWidth+"';" +
								"cur.style.height = 'auto';" +
                            "};" +
                        "};" +
                        "var scrollLoad = function (options) {" +
	                        "var defaults = (arguments.length == 0) ? { src: 'xSrc', time: 300} : { src: options.src || 'xSrc', time: options.time ||300}; " +
	                        "var camelize = function (s) {" +
	                            "return s.replace(/-(\\w)/g, function (strMatch, p1) {" +
	                                "return p1.toUpperCase();" +
	                            "});" +
	                        "};" +
	                        //杩斿洖娴忚鍣ㄧ殑鍙鍖哄煙浣嶇疆
	                      "this.getClient = function(){" +
	                        "var l,t,w,h;"+
	                        "l = document.documentElement.scrollLeft || document.body.scrollLeft;"+
	                        "t = document.documentElement.scrollTop || document.body.scrollTop;"+
	                        "w = document.documentElement.clientWidth;"+
	                        "h = document.documentElement.clientHeight;"+
	                        "return {'left':l,'top':t,'width':w,'height':h};"+
	                      "};"+
	                      //杩斿洖寰呭姞杞借祫婧愪綅缃�
	                      "this.getSubClient = function(p){" +
	                        "var l = 0,t = 0,w,h;"+
	                        "w = p.offsetWidth ;"+
	                        "h = p.offsetHeight;"+
	                        "while(p.offsetParent){"+
	                            "l += p.offsetLeft;"+
	                            "t += p.offsetTop;"+
	                            "p = p.offsetParent;"+
	                         "}"+
	                         "return {'left':l,'top':t,'width':w,'height':h };"+
	                        "};"+
	                        //鍒ゆ柇涓や釜鐭╁舰鏄惁鐩镐氦,杩斿洖涓�涓竷灏斿��
	                       "this.intens = function(rec1,rec2){" +
	                            "return (rec2.top > rec1.top && rec2.top < (rec1.top + document.body.clientHeight));" +
	                        "};" +
	                        "this.getStyle = function (element, property) {" +
	                            "if (arguments.length != 2) return false;" +
	                            "var value = element.style[camelize(property)];" +
	                            "if (!value) {" +
	                                "if (document.defaultView && document.defaultView.getComputedStyle) {" +
	                                    "var css = document.defaultView.getComputedStyle(element, null);" +
	                                    "value = css ? css.getPropertyValue(property) : null;" +
	                                "} else if (element.currentStyle) {" +
	                                    "value = element.currentStyle[camelize(property)];" +
	                                "}" +
	                            "}" +
	                            "return value == 'auto' ? '' : value;" +
	                        "};" +
	                        "var _init = function () {" +	                            
	                            "var rec1 = getClient();"+
	                            "docImg = document.images;" +
	                            "_len = docImg.length;" +
	                            "if (!_len) return false;" +
	                            "for (var i = 0; i < _len; i++) {" +
	                                "var attrSrc = docImg[i].getAttribute(defaults.src);" +
	                                "var o = docImg[i];" +
	                                "var tag = o.nodeName.toLowerCase();" +
	                                "if (o) {" +	                                    
	                                    "var rec2 =  getSubClient(o);"+
	                                    "if(intens(rec1,rec2)) {" +
	                                        "if (tag === \"img\" && attrSrc !== null) {" +
	                                        	"clearImage(o);"+
	                                            "loadImage(o, attrSrc," +
	                                                        "function (cur, img, cached) {" + 
	                                                            "cur.src = img.src; " +
	                                                            "if(img.width > 50)" +
	                                                                //"cur.style.width = " + deviceWidth + ";" +
	                                                                "cur.style.width = '98%';" +                                                                
	                                                                "cur.style.margin = '5px auto';" +
	                                                            "}," +
                                                            "function (cur, img) {" +
                                                                "cur.style.display = \"none\";" +
                                                            "}" +
                                                        ");" +
	                                            "resetImage(o);" +
	                                        "}" +
	                                        "o = null;" +
	                                    "}" +
	                                "}" +
	                            "};" +
	                            "window.onscroll = function () {" +
	                                "setTimeout(function () {" +
	                                    "_init();" +
	                                "}, defaults.time);" +
	                            "}" +
	                        "};" +
	                        "return _init();" +
	                    "};" +
	                    "function LoadContent(content,title,type) {" +
	                    	"window.external.notifyJava('LoadComplted'); " +
	                    	"document.getElementById('error').style.display='none';" +
		                    "document.getElementById('content').style.display='block';" +
		                    "document.getElementById('content').innerHTML=content;" +
		                    //"formatImages();" + 
		                    "scrollLoad();" +
		                    "window.scrollTo(0,0);" +
		                    "window.external.loadComplete(type); " +
		                "}" +
		                "function LoadError(errorTitle,errorContent, errorLoad) {" +	                    	
		                    "document.getElementById('content').style.display='none';" +
		                    "document.getElementById('error').style.display='block';" +
		                    "document.getElementById('errorTitle').innerHTML = errorTitle;" +
		                    "document.getElementById('errorContent').innerHTML = errorContent;" +
		                    "document.getElementById('errorLoad').innerHTML = errorLoad;" +
		                    "window.scrollTo(0,0);" +
		                    "window.external.loadComplete('content'); " +
		                "}" +
		                "var onInit = function () {"+
		                	"window.external.loadComplete('init'); " +
	                    "};"+
	                    "var loadImage = function (cur, url, callback, onerror) {"+
	                        "var img = new Image();"+
	                        "img.src = url;"+                                            
	                        "if (img.complete) {"+
	                            "callback && callback(cur, img, true);"+
	                            "return;"+
	                        "}"+
	                        "img.onload = function () {"+
	                        	"window.external.notifyJava(img.width + '-' + img.src); " +
	                            "callback && callback(cur, img, false);"+
	                            "return;"+
	                        "};"+
	                        "if (typeof onerror == \"function\") {"+
	                            "img.onerror = function () {"+
	                                "onerror && onerror(cur, img);"+
	                            "}"+
	                        "}"+
	                    "};"+
		                "var clearImage = function (img) {"+
	                        "img.removeAttribute(\"src\");"+
	                        "img.removeAttribute(\"class\");"+
	                        "img.removeAttribute(\"style\");"+
	                        "img.removeAttribute(\"width\");"+
	                        "img.removeAttribute(\"height\");"+	                        
	                        "img.src=\"Loading.gif\";" +
	                        "img.setAttribute(\"style\",\"margin:5px auto\");" +
	                    "};"+
	                    "var resetImage = function (img) {"+
	                        "img.onclick = function () {" +
	                            "var imgSrc = this.src;" +
	                            "window.external.notifyJava('SaveToMediaLibrary'+imgSrc); " +
	                            "if (e && e.preventDefault) " +
	                                " e.preventDefault(); " +
	                            "else " +
	                                " window.event.returnValue = false; " +
	                            "return false;" +
	                        "};" +
	                    "};"+
                        "var srcs = [];" +
                        "var formatImages = function () {" +
                            "srcs = [];" +
                            "var content = document.getElementById(\"content\");" +
                            "var images = content.getElementsByTagName('img');" +
                            "for (var i = 0; i < images.length; i++) {" +
                                "srcs[i] = images[i].src;" +
                                "clearImage(images[i]);" +
                                "loadImage(images[i], srcs[i]," +
	                                "function (cur, img, cached) {" +
	                                    "cur.src = img.src;" +
	                                    "if(img.width > 50){" +
	                                    	//"cur.style.width = '"+deviceWidth+"';" +
	                                    	"cur.style.width = '98%';" +
	                                    	"cur.style.height = 'auto';" +
                                    	"}" +
	                                "}," +
	                                "function (cur, img) {" +
	                                    "cur.style.display = \"none\";" +
	                                "}" +
                                ");" +
                                "resetImage(images[i]);" +
                            "}" +
                        "};" +
                    "</script>" +
                    "<style type=\"text/css\"> " + 
//	                    "@font-face { "+
//	                        "font-family: \"Hiragino\"; "+
//	                        "src: url('file:///android_asset/font/Hiragino Sans GB W3.otf'); "+
//	                    "} "+
//	                    "@font-face { "+
//	                        "font-family: \"Dreamofgirl\"; "+
//	                        "src: url('file:///android_asset/font/wryhzt.ttf'); "+
//	                    "} "+
						
                    	sb.toString() +

	                    "window,html,body{ "+
	                        //"overflow-x:hidden !important; "+
	                        //"-webkit-overflow-scrolling: touch !important;"+
	                        //"overflow: scroll !important;"+
	                        "word-wrap:break-word;" +
        					"word-break:break-all;" +
	                    "}" +
                        "body, menu, div, dl, dt, dd, ul, ol, li, h1, h2, h3, h4, h5, h6, pre, code, form, fieldset, input, textarea, p, blockquote, th, td { margin: 0; padding: 0; }" +
                        "body { background: #f3f3f3;" + fontFamily + " }" +
                        "table { border-collapse: collapse; border-spacing: 0; }" +
                        "fieldset, img { border: 0; }" +
                        "address, caption, cite, code, dfn, em, strong, th, var { font-style: normal; font-weight: normal; }" +
                        "em { font-style: italic; }" +
                        "strong { font-weight: bold; }" +
                        "ol, ul { list-style: none; }" +
                        "caption, th { text-align: left; }" +
                        "h1, h2, h3, h4, h5, h6 { font-size: 100%; font-weight: normal; }" +
                        "q:before, q:after { content: ''; }" +
                        "abbr, acronym { border: 0; }" +
                        "strike { display: inline; }" +                
                        "pre { display: block; visibility: visible; table-layout: auto; white-space:pre;} " +
                        "A:link { color: #3EC8EF; } " +
                        "A:visited { color: #3EC8EF; } " +
                        "img {text-align: center; display: block; border:0px; margin-top:2px; margin-bottom:2px;overflow:hidden;}" +
                        ".error {margin:50px; margin: auto;}" +
                		".error h1 {margin: 20px 0 0;}" +
                		".error p {margin: 10px 0; padding: 0;}" +		
                		".error a {color: #9caa6d; text-decoration:none;}" +
                		".error a:hover {color: #9caa6d; text-decoration:underline;}" +
                    "</style>" +
                "</head>" +
                "<body id=\"body\" onload='onInit()' style=\"margin:8;font-size:" + ReaderApp.getSettings().FontSize + "pt;" +
                		"color:" + ReaderApp.getSettings().FontColor.substring(3) + ";background-color:" + ReaderApp.getSettings().BackgroundColor.substring(3) + ";line-height:" + ReaderApp.getSettings().LineHeight + "%\">" +
                    "<div id=\"debug\" ></div>" +
	                "<div id=\"content\"></div>" +
	                "<div id=\"error\" onclick=\"window.external.notifyJava('reload');\" style=\"text-align: center;display:none;height:99%;\">" +
	                	"<img alt=\"閿欒鍙戠敓\" src=\"file:///android_asset/error.gif\" style=\"text-align: center;display:inline;width: 30%;margin-top:33%\"/>" +
	                	"<h1 id=\"errorTitle\"></h1>" +
	                	"<p id=\"errorContent\"></p>"+
	                	"<a id=\"errorLoad\" style=\"margin-top: 100px;\" onclick=\"window.external.notifyJava('reload');\"></a>"+
                	"</div>" +
	            "</body>" +
	        "</html>";
        
		String sDStateString = android.os.Environment.getExternalStorageState();

		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try { 
				File SDFile = android.os.Environment.getExternalStorageDirectory();
 
				File dir = new File(SDFile.getAbsolutePath() + Config.HTML_LOCATION);

				if (!dir.exists()) {
					dir.mkdirs();
				}
				
				File myFile = new File(SDFile.getAbsolutePath() + Config.HTML_LOCATION + Config.HTML_NAME);
				
				if(deleteFile){
					myFile.delete();
				}
				
                if (!myFile.exists()) {
                    myFile.createNewFile();
                }

				FileOutputStream outputStream = new FileOutputStream(myFile);				
				outputStream.write(html.getBytes("utf-8"));
				outputStream.close();
				
				File loading = new File(SDFile.getAbsolutePath() + Config.HTML_LOCATION + "Loading.gif");

				if (!loading.exists()) {
					loading.createNewFile();
				}
				
				try {
					InputStream in = context.getAssets().open("loading.gif");
					byte[] data = new byte[in.available()];
					in.read(data,0, in.available());
					
					FileOutputStream loadingStream = new FileOutputStream(loading);
					loadingStream.write(data);				
					loadingStream.close();
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				File video = new File(SDFile.getAbsolutePath() + Config.HTML_LOCATION + "videostart.png");

				if (!video.exists()) {
					video.createNewFile();
				}
				
				try {
					InputStream in = context.getAssets().open("videostart.png");
					byte[] data = new byte[in.available()];
					in.read(data,0, in.available());
					
					FileOutputStream loadingStream = new FileOutputStream(video);
					loadingStream.write(data);				
					loadingStream.close();
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}// end of try
		}
    }
	
	/**
	 * 判断服务是否后台运行
	 * 
	 * @param context
	 *            Context
	 * @param className
	 *            判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	private static boolean isServiceRun(Context mContext, String className) {
		boolean isRun = false;
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(50);
		int size = serviceList.size();
		for (int i = 0; i < size; i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRun = true;
				break;
			}
		}
		return isRun;
	}
	
	public static boolean isServiceRun(){
		return isServiceRun(ReaderApp.getAppContext(), "com.lgq.rssreader.task.DownloadService");
	}
    
    public static UnReadCount findUnreadById(Unread uc, String id){
    	for(UnReadCount u : uc.Unreads){
    		if(u.Id.equals(id)){
    			return u;
    		}    		
    	}
    	
    	return null;    	
    }
    
//    /**
//     * Detects and toggles immersive mode.
//     */
//    public static void toggleImmersiveMode(Activity activity) {
//    	// BEGIN_INCLUDE (get_current_ui_flags)
//        // The UI options currently enabled are represented by a bitfield.
//        // getSystemUiVisibility() gives us that bitfield.
//        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
//        int newUiOptions = uiOptions;
//        // END_INCLUDE (get_current_ui_flags)
//        // BEGIN_INCLUDE (toggle_ui_flags)
//        boolean isImmersiveModeEnabled =
//                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
//        if (isImmersiveModeEnabled) {
//            Log.i("RssReader", "Turning immersive mode mode off. ");
//        } else {
//            Log.i("RssReader", "Turning immersive mode mode on.");
//        }
//
//        // Navigation bar hiding:  Backwards compatible to ICS.
//        if (Build.VERSION.SDK_INT >= 14) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        }
//
//        // Status bar hiding: Backwards compatible to Jellybean
//        if (Build.VERSION.SDK_INT >= 16) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
//        }
//
//        // Immersive mode: Backward compatible to KitKat.
//        // Note that this flag doesn't do anything by itself, it only augments the behavior
//        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
//        // all three flags are being toggled together.
//        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
//        // Sticky immersive mode differs in that it makes the navigation and status bars
//        // semi-transparent, and the UI flag does not get cleared when the user interacts with
//        // the screen.
//        if (Build.VERSION.SDK_INT >= 18) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        }
//
//        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
//    }
    
	//This snippet hides the system bars.
    public static void hideSystemUI(Activity activity) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        //开启全屏模式
    	activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION                
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);    	
    	
    	//new SystemBarTintManager(activity).removeStatusBarTintEnabled(activity);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    //取消全屏模式
    public static void showSystemUI(Activity activity, int uiOptions) {
    	activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);    	
    }
    
    public static List<File> detectFonts(){
    	List<File> fonts = new ArrayList<File>();
    	
    	String sDStateString = android.os.Environment.getExternalStorageState();

		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try { 
				File SDFile = android.os.Environment.getExternalStorageDirectory();
 
				File dir = new File(SDFile.getAbsolutePath() + Config.FONTS_LOCATION);
				
				if(dir.exists())
					return Arrays.asList(dir.listFiles());
			}
			catch(Exception e){
				Log.e("RssReader", "Error at detect fonts " + e.getMessage());
			}
		}
		
		return fonts;
    }
    
    public static boolean isWIFI(){
    	ConnectivityManager connectMgr = (ConnectivityManager) ReaderApp.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    	 
    	NetworkInfo info = connectMgr.getActiveNetworkInfo();
    	return info != null && info.getType() ==  ConnectivityManager.TYPE_WIFI;
    }
    
    public static void vibrate(){
    	// 鑾峰彇Vibrate瀵硅薄  
        Vibrator vibrator = (Vibrator) ReaderApp.getAppContext().getSystemService(ReaderApp.getAppContext().VIBRATOR_SERVICE);
        
        if(ReaderApp.getSettings().EnableVibrate){
            vibrator.vibrate(1000);
            // 璁剧疆Vibrate鐨勯渿鍔ㄥ懆鏈�  
            //vibrator.vibrate(new long[]{1000,2000,3000,4000}, 0);
        }        
    }
    
    public static void sound(){
    	if(ReaderApp.getSettings().EnableSound){
    		MediaPlayer mMediaPlayer = MediaPlayer.create(ReaderApp.getAppContext(), R.raw.sound);// 寰楀埌澹伴煶璧勬簮
    		if(mMediaPlayer != null)
    			mMediaPlayer.start();// 鎾斁澹伴煶
        }
    }    
    
    public static void pulldown(){
    	if(ReaderApp.getSettings().EnableSound){
    		MediaPlayer mMediaPlayer = MediaPlayer.create(ReaderApp.getAppContext(), R.raw.pulldown);// 寰楀埌澹伴煶璧勬簮
    		if(mMediaPlayer != null)
    			mMediaPlayer.start();// 鎾斁澹伴煶
        }
    }
    
//    public static void showSystemUI(View view){
//    	view.setSystemUiVisibility(
//            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//    }
//    
//    public static void hideSystemUI(View view){
//    	view.setSystemUiVisibility(
//    			View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//    }
    
    public static Channel findChannelById(List<Channel> channels, String id){
    	for(Channel c : channels){
    		if(c.Id.equals(id)){
    			return c;
    		}else{
    			if(c.IsDirectory){
    				for(Channel child : c.Children){
    		    		if(child.Id.equals(id)){
    		    			return child;
    		    		}
    				}
    			}
    		}
    	}
    	
    	return null;    	
    }
    
    public static Channel findParentChannel(Channel c){
    	List<Channel> channels = getChannels();
    	
    	for(Channel ch : channels){
    		if(ch.Children != null && ch.Children.contains(c)){
    			return ch;
    		}
    	}
    	
    	return null;    	
    }
    
    public static List<Channel> getChannels(){
    	Gson gson = new Gson();
	    String json = ReaderApp.getAppContext().getSharedPreferences("RssReader", 0).getString("Channel", "");
		String[] data = json.split("____");
		
		List<Channel> channels = new ArrayList<Channel>(); 
		
		for(String obj : data){			
			Channel c = gson.fromJson(obj, Channel.class);
			if(c != null)
				channels.add(c);
		}

		return channels;		
    }
    
    public static void updateChannels(String channelId, Integer count){
    	Gson gson = new Gson();
	    String json = ReaderApp.getAppContext().getSharedPreferences("RssReader", 0).getString("Channel", "");
		String[] data = json.split("____");
		
		List<Channel> channels = new ArrayList<Channel>(); 
		
		for(String obj : data){			
			Channel c = gson.fromJson(obj, Channel.class);
			
			if(c.Id.equals(channelId)){
				c.UnreadCount = count;				
			}			
			else if(c.IsDirectory){
				for(Channel child : c.Children){
					if(child.Id.equals(channelId)){
						child.UnreadCount = count;
						break;
					}
				}
			}
			channels.add(c);
		}

		saveChannels(channels);
    }    
    
    public static void updateChannels(String channelId, Date lastRefreshTime){
    	Gson gson = new Gson();
	    String json = ReaderApp.getAppContext().getSharedPreferences("RssReader", 0).getString("Channel", "");
		String[] data = json.split("____");
		
		List<Channel> channels = new ArrayList<Channel>(); 
		
		for(String obj : data){			
			Channel c = gson.fromJson(obj, Channel.class);
			
			if(c.Id.equals(channelId)){
				c.LastRefreshTime = lastRefreshTime;				
			}			
			else if(c.IsDirectory){
				for(Channel child : c.Children){
					if(child.Id.equals(channelId)){
						child.LastRefreshTime = lastRefreshTime;
						break;
					}
				}
			}
			channels.add(c);
		}

		saveChannels(channels);
    }
    
    public static void updateChannels(String channelId, boolean icon){
    	Gson gson = new Gson();
	    String json = ReaderApp.getAppContext().getSharedPreferences("RssReader", 0).getString("Channel", "");
		String[] data = json.split("____");
		
		List<Channel> channels = new ArrayList<Channel>(); 
		
		for(String obj : data){			
			Channel c = gson.fromJson(obj, Channel.class);
			
			if(c.Id.equals(channelId)){
				c.FavIcon = icon;				
			}			
			else if(c.IsDirectory){
				for(Channel child : c.Children){
					if(child.Id.equals(channelId)){
						child.FavIcon = icon;
						break;
					}
				}
			}
			channels.add(c);
		}

		saveChannels(channels);
    }   
    
    public static void saveChannels(List<Channel> channels){
    	Gson gson = new Gson();
    	
    	StringBuilder sb = new StringBuilder();
    	
    	for(Channel c : channels){
    		sb.append(gson.toJson(c) + "____");
    	}
    			
		ReaderApp.getAppContext().getSharedPreferences("RssReader", 0).edit().putString("Channel", sb.toString()).commit();
    }       
    
    public static String findValueInUrl(String url, String key){
    	String[] params = url.split("&");
    	
    	String value = "";
    	
    	for(String p : params){
			if(p.contains(key)){
				try {
					value = URLDecoder.decode(p.split("=")[1],"utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}        					
			}
		}
    	
    	return value;
    }
	
    public static String FindShortPath(Blog blog)
    {    	
    	List<Channel> channels = getChannels();
        Channel target = null;
        Channel parent = null;
        for(Channel c : channels){
        	if(c.Id == blog.BlogId){
        		target = c;
        		break;
        	}else{
        		if(c.Children != null){
        			for(Channel child : c.Children){
        				if(c.Id == blog.BlogId){
        					target = child;
        					parent = c;
        	        		break;
        	        	}
        			}
        		}
        	}
        }        
        if (target != null){
        	if(parent == null)
        		return target.Folder;
        	else
        		return parent.Folder + "/" + target.SortId;
        }
        else
            return "Recommend";
    }
    
	public static Drawable GetUrlDrawable(String url){
		try{			
			URL aryURI=new URL(url);
			URLConnection conn=aryURI.openConnection();
			InputStream is=conn.getInputStream();
			Bitmap bmp=BitmapFactory.decodeStream(is);
			return new BitmapDrawable(bmp);
		}catch(Exception e){
			Log.e("ERROR", "urlImage2Drawable failed with image url at " + url, e);
			return null;
		}
	}
	
	public static ImageRecord loadDrawable(final Blog blog, final String imageUrl) {
		if (imageUrl.trim().equals("")) {
			return null;
		}		
		
		final String folder = ImageCacher.GetImageFolder(blog);
		final String originName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		final String extension = originName.split("[.]").length > 1 ? originName.split("[.]")[1] : "";
		final String storeName = folder + UUID.randomUUID().toString();
		
		ImageRecord record;
		final ImageRecordDalHelper recordHelper = new ImageRecordDalHelper();
		
		if(recordHelper.Exist(imageUrl)){
			record = recordHelper.GetImageRecordEntity(imageUrl);
			File file = new File(record.StoredName);
			if (file.exists()) {
				recordHelper.Close();
				return record;
			}
		}else{
			Drawable drawable = NetHelper.loadImageFromUrlWithStore(storeName, imageUrl);
			
			record = new ImageRecord();
			record.Extension = extension;
			record.OriginUrl = imageUrl;
			record.BlogId = blog.BlogId;
			record.StoredName = storeName;
			record.TimeStamp = new Date();
			record.Size = FileHelper.GetFileLength(storeName);
			
			recordHelper.SynchronyData2DB(record);
			
			if (drawable != null) {
				recordHelper.Close();
				return record;
			}
		}
		recordHelper.Close();
		return record;	
	}
	
	public static Bitmap GetBitmap(String imageUrl){   
	    Bitmap mBitmap = null;   
	    try {   
	      URL url = new URL(imageUrl);   
	      URLConnection conn=url.openConnection();
	      InputStream is = conn.getInputStream();   
	      mBitmap = BitmapFactory.decodeStream(is);   
	    } catch (MalformedURLException e) {   
	      e.printStackTrace();   
	    } catch (IOException e) {   
	      e.printStackTrace();   
	    }   
	    return mBitmap;   
	}
	
	public static Bitmap DrawableToBitmap(Drawable drawable) {  
	    try {  
	        Bitmap bitmap = Bitmap  
	                .createBitmap(  
	                        drawable.getIntrinsicWidth(),  
	                        drawable.getIntrinsicHeight(),  
	                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
	                                : Bitmap.Config.RGB_565);  
	        Canvas canvas = new Canvas(bitmap);  
	        // canvas.setBitmap(bitmap);  
	        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable  
	                .getIntrinsicHeight());  
	        drawable.draw(canvas);  

	        return bitmap;  
	    } catch (OutOfMemoryError e) {  
	        e.printStackTrace();  
	        return null;  
	    }  
	} 

    public static void QuitHintDialog(final Context context){
    	new AlertDialog.Builder(context)
    	.setMessage(R.string.sys_ask_quit_app)
    	.setTitle(R.string.com_dialog_title_quit)
    	.setIcon(R.drawable.ic_launcher)
    	.setPositiveButton(R.string.com_btn_ok,new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try{
					((Activity)context).finish();
				}catch(Exception e){
					Log.e("RssReader",e.getMessage());
				}
			}
		})
		.setNegativeButton(R.string.com_btn_cancel, new DialogInterface.OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
    }
	
    public static void SaveChannelIcon(Bitmap b, Channel c) {
    	String path = Environment.getExternalStorageDirectory().toString();
    	OutputStream fOut = null;
    	File file = new File(path + "/rssreader/profile/", c.Folder + ".png");    	
    		
    	try {    		
    		if(!file.exists())
        		file.createNewFile();
			fOut = new FileOutputStream(file);
			b.compress(Bitmap.CompressFormat.PNG, 85, fOut);
	    	fOut.flush();
	    	fOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static boolean isChannelIconSaved(Channel c) {
    	String path = Environment.getExternalStorageDirectory().toString();
    	OutputStream fOut = null;
    	File file = new File(path + "/rssreader/profile/", c.Folder + ".png");    	
    		
    	return file.exists();        		
    }
    
	public static int GetVersionCode(final Context con) {
		int version = 1;
		PackageManager packageManager = con.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(con.getPackageName(), 0);
			version = packageInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}
	
	public static String GetVersionName(final Context context){
		String versionName = "1.0.0";
		PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			versionName = packageInfo.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}
	
	private static PowerManager.WakeLock wakeLock;
	
    /**
     * 鍞ら啋鏈嶅姟
     */
    public static void acquireWakeLock(Context context){
    	if(wakeLock!=null){
            return;
        }
        PowerManager powerManager = (PowerManager)(context.getSystemService(Context.POWER_SERVICE));
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.cnblogs.download.OfflineService");
        wakeLock.acquire();
    }
    
    /**
     * 閲婃斁鍞ら啋鏈嶅姟锛岃繑鍥炰紤鐪犵姸鎬�
     */
    public static void releaseWakeLock(){
        if(wakeLock!=null && wakeLock.isHeld()){
            wakeLock.release();
            wakeLock = null;
        }
    }
    
    public static void switchLanguage(Locale locale) {
        Resources resources = ReaderApp.getAppContext().getResources();// 鑾峰緱res璧勬簮瀵硅薄
        Configuration config = resources.getConfiguration();// 鑾峰緱璁剧疆瀵硅薄
        DisplayMetrics dm = resources.getDisplayMetrics();// 鑾峰緱灞忓箷鍙傛暟锛氫富瑕佹槸鍒嗚鲸鐜囷紝鍍忕礌绛夈��
        config.locale = locale; // 绠�浣撲腑鏂�
        resources.updateConfiguration(config, dm);
    }

	/**
	 *鍒ゆ柇瀛楃鏄惁鏄腑鏂�    
	 *@param 杈撳叆瀛楃    
    **/
    public static boolean IsChineseLetter(char input)
    {
        int code = (int)input;
        int chfrom = 19968;
        int chend = 40959;
        //int chfrom = Convert.ToInt32("4e00", 16);    //鑼冨洿锛�0x4e00锝�0x9fff锛夎浆鎹㈡垚int锛坈hfrom锝瀋hend锛�
        //int chend = Convert.ToInt32("9fff", 16);
        if (code >= chfrom && code <= chend)
        {
            return true;     //褰揷ode鍦ㄤ腑鏂囪寖鍥村唴杩斿洖true
        }
        else
        {
            return false;    //褰揷ode涓嶅湪涓枃鑼冨洿鍐呰繑鍥瀎alse
        }
    }

	// 获取手机状态栏高度
    public static int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = ReaderApp.getAppContext().getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    // 获取ActionBar的高度
    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))// 如果资源是存在的、有效的
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }
}
