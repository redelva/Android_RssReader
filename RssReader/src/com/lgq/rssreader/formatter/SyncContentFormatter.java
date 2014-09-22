package com.lgq.rssreader.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.net.Uri;
import android.util.Log;

import com.lgq.rssreader.cache.ImageCacher;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.readability.Readability;
import com.lgq.rssreader.utils.HtmlHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class SyncContentFormatter extends SyncBlogFormatter
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
                
        return blog.Content;
    }

	@Override
	protected String Download(final Blog b)
    {
		HttpGet httpRequest = new HttpGet(b.Link);			        

        HttpClient httpclient = new DefaultHttpClient();
        
        httpclient.getParams().setParameter("Content-Type","text/html;charset=UTF-8"); 
        
        HttpResponse response;
        
        String content = "";
		
        try {
			response = httpclient.execute(httpRequest);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//detect charset
				
				InputStream in = response.getEntity().getContent();
				
				//content = EntityUtils.toString(response.getEntity());
				
				StringBuffer temp = new StringBuffer();
				
				BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
				for(String tempstr = ""; (tempstr = buffer.readLine()) != null;)
					temp = temp.append(tempstr);

				buffer.close();
				in.close();
				content = temp.toString().trim();
				
				int index = content.indexOf("body");
				
				if(index == -1)
					return content;
    			
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
                    			content = new String(content.getBytes("GBK"), "UTF-8");;
                    			break;
                    		}
                    		if(p.contains("charset") && p.contains("gb2312")){
                    			content = new String(content.getBytes("gb2312"), "UTF-8");;
                    			break;
                    		}
                    	}
                    }
    			}
			}				
        } catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return content;
    }
    
	@Override
	protected String GetReadableString(String content) {
		Readability readability = Readability.Create(content);

    	return readability.Content;
	}
}    
