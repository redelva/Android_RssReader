package com.lgq.rssreader.formatter;

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

public class SyncDescriptionFormatter extends SyncBlogFormatter
{
	@Override
    protected String LoadFromCache(Blog blog)
    {
		if (blog != null && blog.Description.length() == 0)
        {
            return "";
        }
		
        Document doc = Jsoup.parse(blog.Description);
        
        for(Element img : doc.getElementsByTag("img")){
        	if(img.hasAttr("src") && !img.attr("src").startsWith(prefix)){
        		return "";
        	}
        }
        
        return blog.Description;
    }

	@Override
	protected String Download(final Blog blog)
    {
		return blog.Description;
    }
    
	@Override
	protected String GetReadableString(String content) {
		return content;
	}
}    
