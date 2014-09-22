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

public class DescriptionFormatter extends BlogFormatter
{
	@Override
    protected String LoadFromCache(Blog blog)
    {
		if (blog != null && blog.Description.length() == 0)
        {
            return "";
        }
		
        Document doc = Jsoup.parse(blog.Description);

        List<Element> embeds = doc.getElementsByTag("embed");
        
        for(Element d : doc.getElementsByTag("iframe")){
        	if(d.hasAttr("src")&&
				(
				    d.attr("src").contains("swf") ||
				    d.attr("src").contains("youku") ||
				    d.attr("src").contains("sohu") ||
				    d.attr("src").contains("tudou") ||
				    d.attr("src").contains("youtube") ||
				    d.attr("src").contains("ku6")
				)
            )
        	embeds.add(d);
        }
        
        for(Element d : doc.getElementsByTag("a")){
        	if(d.hasAttr("href")&&
				(
				    d.attr("href").contains("swf") ||
				    d.attr("href").contains("youku") ||
				    d.attr("href").contains("sohu") ||
				    d.attr("href").contains("tudou") ||
				    d.attr("href").contains("youtube") ||
				    d.attr("href").contains("ku6")
				)
            )
        	embeds.add(d);
        }

        if (embeds.size() != 0)
            return "";
        
        for(Element img : doc.getElementsByTag("img")){
        	if(img.hasAttr("src") && !img.attr("src").startsWith(prefix)){
        		return "";
        	}
        }
        
        return blog.Description;
    }

	@Override
	protected void Download(final Blog blog)
    {
		DownloadComplete.onDownload(blog, blog.Description);
    }

    private static final Object syncLock = new Object();
    
	@Override
	protected String GetReadableString(String content) {
		return content;
	}
}    
