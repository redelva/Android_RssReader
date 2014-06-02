package com.lgq.rssreader.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.net.Uri;
import android.util.Log;

import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.readability.Readability;
import com.lgq.rssreader.utils.HtmlHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class ContentFormatter extends BlogFormatter
{
	
	
	@Override
    protected String LoadFromCache(Blog blog)
    {
		if (blog.Content == null){
            return "";
        }
		
        if (blog.Content.length() == 0){
            return "";
        }
        
        if (blog.Content.contains("embed")){
            return "";
        }
        
        Document doc = Jsoup.parse(blog.Content);
        
        Elements imgs = doc.getElementsByTag("img");
        
        for(int i=0, len=imgs.size(); i<len;i++){
        	if(imgs.get(i).hasAttr("src") && imgs.get(i).attr("src").startsWith(prefix))
        		return "";
        }
        return blog.Content;
    }

	@Override
	protected void Download(final Blog blog)
    {
        if (blog.Content == null || blog.Content.length() == 0 || blog.Content.contains("embed")){
            AsyncHttpClient fullClient = new AsyncHttpClient();
            //Uri link = Uri.parse(blog.Link);                
            //fullClient.addHeader("host", link.getHost());
            //fullClient.addHeader("Referer", blog.Link);            
            fullClient.get(blog.Link, new AsyncHttpResponseHandler(){
            	public void onSuccess(String content){
            		try
                    {
            			int index = content.indexOf("body");
            			
            			String body = content.substring(0, index) + "<body></body></html>"; 
            			
            			Document doc = Jsoup.parse(body);
            			            			
            			for(int j=0, length = doc.getElementsByTag("meta").size();  j < length; j++){            			
	            			Element meta = doc.getElementsByTag("meta").get(j);
	                        if (meta.outerHtml().contains("charset"))
	                        {
	                        	String[] pairs = meta.outerHtml().split(" ");
	                        	for(int i=0, len=pairs.length; i<len; i++){
	                        		String p = pairs[i].toLowerCase();
	                        		if(p.contains("charset") && p.contains("gbk")){
	                        			content = new String(content.getBytes("GBK"), "UTF-8");
	                        			break;
	                        		}
	                        		if(p.contains("charset") && p.contains("gb2312")){
	                        			content = downloadGbUrl(blog.Link);//CharHelper.change(new String(content.getBytes("utf-8"), "gb2312"), "gb2312", "UTF-8");//new String(content.getBytes("UTF-8"), "gb2312");;
	                        			break;
	                        		}
	                        	}
	                        }
            			}
                        DownloadComplete.onDownload(blog, HtmlHelper.unescape(content));
                    }
                    catch (Exception ex)
                    {
                        if (RenderComplete != null)
                            RenderComplete.onRender(blog, "");
                        if (CacheComplete != null)
                            CacheComplete.onCache(null, new CacheEventArgs(blog, null, null, -1, -1));
                    }
            	}
            	
            	@Override
            	public void onFailure(Throwable t, String error){
            		if (RenderComplete != null)
                        RenderComplete.onRender(blog, "");
                    if (CacheComplete != null)
                        CacheComplete.onCache(null, new CacheEventArgs(blog, null, null, -1, -1));
            	}
            });            
        }        
        else if (blog.Content.length() != 0)
        {
            DownloadComplete.onDownload(blog, blog.Content);
        }
    }

    private static final Object syncLock = new Object();
    
    private String downloadGbUrl(String url){
    	try{
    		URL u=new URL(url); 
        	URLConnection conn=u.openConnection(); 
        	//建立连接 
        	conn.connect();     	
        	BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream(),"gb2312")); 
        	String buf="";
        	StringBuilder content = new StringBuilder();
        	while((buf=br.readLine())!=null) 
        	{ 
        		content.append(buf); 
        	} 
        	//转换编码 
        	return new String(content.toString().getBytes("UTF-8"));
    	}catch(IOException e){
    		e.printStackTrace();
    		return "";
    	}
    }
	
	@Override
	protected String GetReadableString(String content) {
		Readability readability = Readability.Create(content);

    	return readability.Content;
	}
}    
