package com.lgq.rssreader.formatter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.lgq.rssreader.cache.AsyncImageLoader;
import com.lgq.rssreader.cache.AsyncImageLoader.ImageCallback;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.task.ImageTask;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.HtmlHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

public abstract class SyncBlogFormatter
{
	public static final String prefix = "../images";
	
	public String BackgroundColor;
    public String FontColor;
    public boolean EnableCache;
    public boolean NoImageMode;
    
    protected abstract String LoadFromCache(Blog blog);
    protected abstract String Download(Blog blog);        
    protected abstract String GetReadableString(String content);
    
    public String Render(final Blog blog)
    {
        String content = LoadFromCache(blog);
        if (content.length() == 0)
        {
            String e = Download(blog);
            
        	String readable = GetReadableString(e);
                		
    		if(readable == null || readable.length() == 0){
    			return "";
    		}
    		
    		if(blog.Link.contains("cnbeta")){
    			String sample = HtmlHelper.filterHtml(blog.Description).substring(0,10).replace(" ", "");
    			
    			String puretext = HtmlHelper.trim(HtmlHelper.filterHtml(readable)).replace(" ", "");
    			
    			if(!puretext.contains(sample))
    				readable = blog.Description + readable;
    		}
    		
    		Document doc = Jsoup.parse(readable);
    		
    		doc = dealFont(doc);
    		
    		doc = dealStyle(doc);
    		
    		doc = dealImageLazyLoading(doc);
    		
    		return doc.body().html();
        }
        
        return content;
    }
    
    
    
    private Document dealFont(Document doc){
    	Elements fonts = doc.getElementsByTag("font");

        for (int i=0, len=fonts.size(); i < len; i++){
        	Element spanFont = doc.createElement("span");

            spanFont.html(fonts.get(i).html());

            fonts.get(i).before(spanFont);

            fonts.get(i).remove();
        }
        
        return doc;
    }
    
    private Document dealStyle(Document doc){
    	for(Element c : doc.getElementsByAttribute("style")){
            c.attr("style", c.attr("style").toLowerCase().replace("width", "w"));
            c.attr("style", c.attr("style").toLowerCase().replace("height", "h"));
            c.attr("style", c.attr("style").toLowerCase().replace("font", "f"));
            c.attr("style", c.attr("style").toLowerCase().replace("background", "b"));
        }
    	
    	return doc;
    }
    
    private Document dealImageLazyLoading(Document doc){
    	List<Element> imgs = new ArrayList<Element>();        
        for(Element d : doc.getElementsByTag("img")){
        	
        	if (d.hasAttr("width"))
                d.removeAttr("width");
            if (d.hasAttr("height"))
            	d.removeAttr("height");
        	
        	if(d.hasAttr("src") &&
    			(!d.hasAttr("xSrc") || !d.attr("xSrc").contains(prefix))
			)
        	imgs.add(d);
        }
        for(Element img : imgs)
        {            
            if (!img.hasAttr("xSrc") && img.hasAttr("src"))
            {
                img.attributes().put("xSrc", img.attr("src"));                
                img.attr("src", "Loading.gif");
            }
            
            if (img.hasAttr("style")){
            	img.attr("style", img.attr("style") + "margin:auto;");
            }else{
            	img.attributes().put("style", "margin:auto;");
            }
        }
        
        return doc;
    }
}

