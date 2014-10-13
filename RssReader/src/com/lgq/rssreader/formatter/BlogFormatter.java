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

import com.lgq.rssreader.R;
import com.lgq.rssreader.cache.AsyncImageLoader;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.task.ImageTask;
import com.lgq.rssreader.utils.FileHelper;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.HtmlHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

   	public abstract class BlogFormatter
    {
   		public static final String prefix = "/images";
   		
   		public String BackgroundColor;
        public String FontColor;
        public boolean EnableCache;
        public boolean NoImageMode;
        
        protected abstract String LoadFromCache(Blog blog);
        protected abstract void Download(Blog blog);        
        protected abstract String GetReadableString(String content);
        
        public interface RenderCompleteHandler{
        	public void onRender(Blog sender, String content);
        }
        public interface DownloadCompleteHandler{
        	public void onDownload(Blog sender, String content);
        }
        public interface CacheCompleteHandler{
        	public void onCache(Blog sender, CacheEventArgs e);
        }
        public interface FlashCompleteHandler{
        	public void onFlash(Object sender, CacheEventArgs e);
        }
        
        protected RenderCompleteHandler RenderComplete;
        protected DownloadCompleteHandler DownloadComplete;
        protected CacheCompleteHandler CacheComplete;
        protected FlashCompleteHandler FlashComplete;        
        
        public void setRenderCompleteHandler(RenderCompleteHandler handler){
        	this.RenderComplete = handler;
        }
        
        public void setCacheCompleteHandler(CacheCompleteHandler handler){
        	this.CacheComplete = handler;
        }
        
        public void setFlashCompleteHandler(FlashCompleteHandler handler){
        	this.FlashComplete = handler;
        }

        public void Render(final Blog blog)
        {
            String content = LoadFromCache(blog);
            if (content.length() == 0)
            {
                if (DownloadComplete == null)
                {
                    DownloadComplete = new DownloadCompleteHandler(){
                    	public void onDownload(final Blog o, String e){
                    		String readable = GetReadableString(e);
                    		
                    		if(readable == null || readable.length() == 0){
                    			if (RenderComplete != null)                            
                                    RenderComplete.onRender(o, null);                            

                                if (CacheComplete != null)
                                    CacheComplete.onCache(null, new CacheEventArgs(o, null, null, -1, -1));

                                return;
                    		}
                    		
                    		if(o.Link.contains("cnbeta")){
                    			String sample = HtmlHelper.filterHtml(o.Description).substring(0,10).replace(" ", "");
                    			
                    			String puretext = HtmlHelper.trim(HtmlHelper.filterHtml(readable)).replace(" ", "");
                    			
                    			if(!puretext.contains(sample))
                    				readable = o.Description + readable;
                    		}
                    		
                    		Document doc = Jsoup.parse(readable);
                    		
                    		doc = dealLink(doc);
                    		
                    		doc = dealFlash(doc, o);
                    		
                    		doc = dealVideoLink(doc, o);
                    		
                    		doc = dealWeiphone(doc, o);
                    		
                    		doc = dealFont(doc);
                    		
                    		doc = dealStyle(doc);
                    		
                    		doc = dealImageLazyLoading(doc);
                    		
                    		if (NoImageMode)
                                doc = removeImage(doc);                    		
                    		
                    		final Element body = doc.body().clone();
                    		
                    		if (EnableCache){
                            	new Thread(){
                            		public void run(){
                            			cacheImage(body, o);
                            		}
                            	}.start();
                    		}
                    		if (RenderComplete != null)
                                RenderComplete.onRender(o, doc.body().html());
                    	}
                    };
                }
                Download(blog);
            }
            else
            {            	
            	if (RenderComplete != null)
                {
                    RenderComplete.onRender(blog, content);
                }
            }
        }
        
        private void cacheImage(Element body, Blog blog){        	
        	for(final Element node : body.getElementsByTag("img"))
            {
        		if(node.attr("src").startsWith("..")){
        			continue;
        		}
        		
                if(node.hasAttr("xSrc") && !node.attr("xSrc").startsWith("..")){
                	ImageRecord record = Helper.loadDrawable(blog, node.attr("xSrc"));
                	node.attr("xSrc", record.StoredName.replace("/rssreader", ".."));
                }
            }
        	
        	if (CacheComplete != null)
        		CacheComplete.onCache(null, new CacheEventArgs(blog, body, body, -1, -1));
        }
        
        private Document dealLink(Document doc){
        	for(Element node : doc.getElementsByTag("a"))
            {
                if (node.hasAttr("onclick"))
                    node.attr("onclick","linkHandle()");
                else
                    node.attributes().put("onclick", "linkHandle()");
            }
        	
        	return doc;
        }
        
        private Document dealFlash(Document doc, final Blog blog){
        	List<Element> embeds =  doc.getElementsByTag("embed");
            for(Element d : doc.getElementsByTag("iframe")){
                if(d.hasAttr("src") &&
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
            for (int i = 0, len=embeds.size(); i < len; i++)
            {
                Element tip = doc.createElement("div");
                Element msg = doc.createElement("div");
                //var click = doc.CreateElement("a");
                tip.appendChild(msg);
                //tip.AppendChild(click);
                msg.html("RemoveFlash");
                msg.attributes().put("id", "msg" + i);
                //msg.Attributes.Add("style", "color:red;");
                msg.attributes().put("style", "color:red;display:none;");
                //click.Attributes.Add("id", "click" + i);

                for (int j = 0; j < 20; j++)
                {
                	Element click = doc.createElement("a");
                    tip.appendChild(click);
                    //var br = doc.CreateElement("br");
                    //tip.AppendChild(br);
                    click.attributes().put("id", "click" + i + j);
                    //click.Attributes.Add("style", "display:none");
                    click.attributes().put("onclick", "linkHandle()");
                }

                if (!embeds.get(i).html().contains("youtube"))
                {
                    if (embeds.get(i).hasAttr("style"))
                    	embeds.get(i).attr("style", "display:none;");
                    else
                    	embeds.get(i).attributes().put("style", "display:none;");
                }

                if (embeds.get(i).hasAttr("id"))
                	embeds.get(i).attr("id", "flash" + i);
                else
                	embeds.get(i).attributes().put("id", "flash" + i);

                if (doc.getElementsByAttributeValue("id", "msg" + i).size() == 0)
                    embeds.get(i).before(tip);                
                
                String src = embeds.get(i).attr("src");
                parseFlash(i, blog, embeds.get(i).clone(), tip.clone(), src);                
            }
        	
        	return doc;
        }

        private Document dealVideoLink(Document doc, final Blog blog){
        	List<Element> embeds =  doc.getElementsByTag("embed");
            for(Element d : doc.getElementsByTag("iframe")){
                if(d.hasAttr("src") &&
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
        	
        	//region Video Link
            if (embeds.size() == 0 ){
            	List<Element> links = new ArrayList<Element>();
            	List<String> urls = new ArrayList<String>();
            	
            	for(Element d : doc.getElementsByTag("a")){
            		if(d.hasAttr("href") &&
                        (
                            d.attr("href").contains("youku") ||
                            d.attr("href").contains("sohu") ||
                            d.attr("href").contains("youtube") ||
                            d.attr("href").contains("ku6") ||
                            d.attr("href").contains("tudou") ||
                            d.attr("href").contains("swf")
                        )                        
                    )
            		if(!urls.contains(d.attr("href"))){
            			links.add(d);
            			urls.add(d.attr("href"));
            		}
            	}

            	for(Element p : doc.getElementsByTag("p")){
            		if(p.html().contains("youku") ||
        				p.html().contains("sohu") ||
        				p.html().contains("youtube") ||
        				p.html().contains("ku6") ||
        				p.html().contains("tudou") ||
        				p.html().contains("swf")
                    )
            		if(!urls.contains(p.html())){
            			links.add(p);
            			urls.add(p.html());
            		}
            	}

                for (int i = 0, len = links.size(); i<len; i++)
                {                    
                    Element tip = doc.createElement("div");
                    Element msg = doc.createElement("div");
                    //var click = doc.CreateElement("a");
                    tip.appendChild(msg);
                    //tip.AppendChild(click);
                    //msg.InnerHtml = Resources.StringResources.RemoveFlash;
                    msg.attributes().put("id", "msg" + i);
                    //click.Attributes.Add("id", "click" + i);
                    //click.Attributes.Add("style", "display:none");                
                    //click.Attributes.Add("onclick", "linkHandle()");

                    for (int j = 0; j < 20; j++)
                    {
                    	Element click = doc.createElement("a");
                        tip.appendChild(click);
                        click.attributes().put("id", "click" + i + j);
                        //var br = doc.CreateElement("br");
                        //tip.AppendChild(br);
                        //click.Attributes.Add("style", "display:none");
                        click.attributes().put("onclick", "linkHandle()");
                    }

                    if (links.get(i).hasAttr("id"))
                    	links.get(i).attr("id", "flash" + i);
                    else
                    	links.get(i).attributes().put("id", "flash" + i);
                    if (doc.getElementsByAttributeValue("id", "msg" + i).size() == 0)
                        links.get(i).before(tip);

                    String src = links.get(i).attr("href");
                    parseFlash(i, blog, links.get(i).clone(), tip.clone(), src);
                }
            }
        	
        	return doc;
        }
        
        private Document dealWeiphone(Document doc, final Blog blog){
        	
        	List<Element> embeds =  doc.getElementsByTag("embed");
            for(Element d : doc.getElementsByTag("iframe")){
                if(d.hasAttr("src") &&
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

            List<Element> loadings = new ArrayList<Element>();
            for(Element d :doc.getElementsByTag("p")){
            	if(d.attr("id").startsWith("weiphoneplayer")){
            		loadings.add(d);
            	}
            }        
            for (int i = 0; i < loadings.size(); i++)
            {
                Element tip = doc.createElement("div");
                Element msg = doc.createElement("div");
                //var click = doc.CreateElement("a");
                tip.appendChild(msg);
                //tip.AppendChild(click);
                msg.html("RemoveFlash");
                msg.attributes().put("id", "msg" + (i+embeds.size()));
                //msg.Attributes.Add("style", "color:red;");
                msg.attributes().put("style", "color:red;display:none;");
                //click.Attributes.Add("id", "click" + i);

                for (int j = 0; j < 20; j++)
                {
                	Element click = doc.createElement("a");
                    tip.appendChild(click);
                    //var br = doc.CreateElement("br");
                    //tip.AppendChild(br);
                    click.attributes().put("id", "click" + (i+embeds.size()) + j);
                    //click.Attributes.Add("style", "display:none");
                    click.attributes().put("onclick", "linkHandle()");
                }

                if (!loadings.get(i).html().contains("youtube"))
                {
                    if (loadings.get(i).hasAttr("style"))
                        loadings.get(i).attr("style", "display:none;");
                    else
                        loadings.get(i).attributes().put("style", "display:none;");
                }

                if (loadings.get(i).hasAttr("id"))
                    loadings.get(i).attr("id", "flash" + (i+embeds.size()));
                else
                    loadings.get(i).attributes().put("id", "flash" + (i+embeds.size()));

                if (doc.getElementsByAttributeValue("id", "msg" + (i+embeds.size())).size() == 0)
                    //doc.GetElementbyId("msg" + i).ParentNode.Remove();

                    loadings.get(i).before(tip);

                //parseFlash(i,blog, embeds[i], tip, embeds[i].Attributes["src"].Value);

                parseFlash(i, blog, loadings.get(i).clone(), tip.clone(), "weiphone");
            }
        	
        	return doc;
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
        	
        	for(Element c : doc.getElementsByAttribute("height")){
                //c.attr("height", c.attr("height").toLowerCase().replace("height", "h"));
        		c.removeAttr("height");
            }
        	
        	for(Element c : doc.getElementsByAttribute("width")){
                //c.attr("width", c.attr("width").toLowerCase().replace("width", "w"));
        		c.removeAttr("width");
            }
        	
        	for(Element c : doc.getElementsByTag("object")){
                c.attr("style", c.attr("style").toLowerCase().replace("width", "w"));
                c.attr("style", c.attr("style").toLowerCase().replace("height", "h"));
                c.attr("style", c.attr("style").toLowerCase().replace("font", "f"));
                c.attr("style", c.attr("style").toLowerCase().replace("background", "b"));
                
                for(Element param : c.children()){
                	if(param.tagName().equals("allowfullscreen")){
                		param.attr("allowfullscreen","false");
                		break;
                	}
                }
                
                c.removeAttr("width");
                c.removeAttr("style");
                c.removeAttr("height");
                c.attr("width", "350px");
                c.attr("height", "290px");
            }
        	
        	for(Element c : doc.getElementsByTag("iframe")){
        		
        		if(c.hasAttr("style")){
        			
        			if(c.attr("style").toLowerCase().contains("width")){
        				String[] attrs = c.attr("style").split(";");
                        for(String attr  : attrs){
                        	if(attr.toLowerCase().contains("width")){
                        		c.attr("style", c.attr("style").toLowerCase().replace(attr.toLowerCase(), "width:99%"));
                        	}
                        }
        			}else{
        				c.attr("style", c.attr("style") + "width:99%;");
        			}
        			
//        			if(c.attr("style").toLowerCase().contains("height")){
//        				String[] attrs = c.attr("style").split(";");
//                        for(String attr  : attrs){                        	
//                        	if(attr.toLowerCase().contains("height")){
//                        		c.attr("style", c.attr("style").toLowerCase().replace(attr.toLowerCase(), "height:100%"));
//                        	}
//                        }
//        			}else{
//        				c.attr("style", c.attr("style") + "height:100%;");
//        			}
        		}else{
        			c.attr("style", "width:100%;");
        		}
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
                	if(!img.attr("src").startsWith(prefix)){
                		img.attributes().put("xSrc", img.attr("src"));                
                        img.attr("src", "Loading.gif");
                	}
                }
                
                if (img.hasAttr("style")){
                	img.attr("style", img.attr("style") + "margin:auto;");
                }else{
                	img.attributes().put("style", "margin:auto;");
                }
            }
            
            return doc;
        }
        
        private Document removeImage(Document doc){
        	for(Element img : doc.getElementsByTag("img")){
            	img.remove();
            }
        	return doc;
        }
        
        private void parseFlash(final int cnt, final Blog blog, final Element embed, final Element tip, final String url)
        {
            if (url.contains("youku")){
                youku(cnt, blog, embed, tip, url);
            }
            else if (url.contains("youtube")){
            	youtube(cnt, blog, embed, tip, url);
            }
            else if (url.contains("sohu")){
            	sohu(cnt, blog, embed, tip, url);
            }
            else if (url.contains("weiphone")){
            	weiphone(cnt, blog, embed, tip, url);
            }
            else if (url.contains("tudou")){
            	tudou(cnt, blog, embed, tip, url);
            }
            else if (url.contains("ku6")){
            	ku6(cnt, blog, embed, tip, url);
            }
            else if (url.contains("qq")){
            	qq(cnt, blog, embed, tip, url);
            }
            else if (url.contains("56")){
            	fivesix(cnt, blog, embed, tip, url);
            }
            else{
            	
            	new Thread(){
            		public void run(){
            			if (FlashComplete != null){
                            tip.html("");                    
                            FlashComplete.onFlash(ReaderApp.getAppContext().getResources().getString(R.string.blog_videooptimize), new CacheEventArgs(blog, embed, tip, cnt, -1));
                        }
            		}
            	}.start();
            }
        }
        
        private void youku(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	int index = url.indexOf('X');
            if (index == -1)
                return;
            
            AsyncHttpClient client = new AsyncHttpClient();                
            String id = url.substring(index, index + 13);
            client.get("http://v.youku.com/player/getPlayList/VideoIDS/" + id + "/Pf/4/ctype/12/ev/1", new JsonHttpResponseHandler(){
            	@Override
            	public void onSuccess(JSONObject youku){
            		try {
						if (youku.getJSONArray("data").getJSONObject(0).getJSONObject("segs").getJSONArray("3gphd") != null)
						{							
							String ip = youku.getJSONArray("data").getJSONObject(0).getString("ip");				            
				            int h=1;
				            String q="mp4";
				            double seed = youku.getJSONArray("data").getJSONObject(0).getDouble("seed");
				            String fileid = youku.getJSONArray("data").getJSONObject(0).getJSONObject("streamfileids").getString("3gphd");
				            String f = getFileID(fileid, seed);
				            String sidAndtoken = E("becaf9be", na(youku.getJSONArray("data").getJSONObject(0).getString("ep")));
				            String sid = sidAndtoken.split("_")[0];
				            String token = sidAndtoken.split("_")[1];

				            tip.html("");
				            for(int i=0, len=youku.getJSONArray("data").getJSONObject(0).getJSONObject("segs").getJSONArray("3gphd").length();i<len;i++)
						    {
						    	JSONObject child = youku.getJSONArray("data").getJSONObject(0).getJSONObject("segs").getJSONArray("3gphd").getJSONObject(i);
						    	String k = child.getString("k");
								String l = child.getString("seconds");
						        //String k = child.getString("k");
						        String k2 = child.getString("k2");
						        //String indexFileId = fileId.Insert(9, i.ToString()).Remove(10);
						        
						        f = f.substring(0,9) + String.valueOf(i) + f.substring(10);
						        
						        String url = "/player/getFlvPath/sid/" + sid + "_" + "00" + "/st/" + q + "/fileid/" + f + "?K=" + k + "&hd=" + h + "&myp=0&ts=" + l + "&ypp=0";// +e;
					            f = HtmlHelper.UrlEncodeUpper(D(E("bf7e5f01", sid + "_" + f + "_" + token)));
					            url = url + ("&ep=" + f) + "&ctype=12&ev=1" + ("&token=" + token);
					            url += "&oip=" + ip;
					            url = "http://k.youku.com" + url;

						        if (FlashComplete != null)
						        {
						            tip.html(tip.html() + url + "|");
						        }                                
						    }				            
													    
						    tip.html(tip.html().substring(0,tip.html().length() - 1) + "____" + youku.getJSONArray("data").getJSONObject(0).getString("logo"));
						    FlashComplete.onFlash(youku.getJSONArray("data").getJSONObject(0).getString("title"), new CacheEventArgs(blog, embed, tip, cnt, 0));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                        
            	}
            	
            	public void onFailure(){
                	tip.html("");
                	FlashComplete.onFlash(ReaderApp.getAppContext().getResources().getString(R.string.blog_videooptimize), new CacheEventArgs(blog, embed, tip, cnt, -1));
                }
            });
        }

        private void youtube(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	Pattern p = Pattern.compile("(?:youtube\\.com/(?:user/.+/|(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\\.be/)([^\"&?/ ]{11})");
        	try{
        		final String group = p.matcher(url).toMatchResult().group();
                final String id = group.substring(group.length() - 11);

                final AsyncHttpClient client = new AsyncHttpClient();
                
                client.get("https://www.youtube.com/get_video_info?video_id=" + id, new AsyncHttpResponseHandler(){
                	@Override
                	public void onSuccess(String response){
                		client.get("https://www.youtube.com/get_video_info?video_id=" + id, new AsyncHttpResponseHandler(){
                        	public void onSuccess(String response){
                        		if (!response.contains("fail")){
                                    String results = HtmlHelper.unescape(response);
                                    List<String> result = processYoutube(results);
                                    tip.html(HtmlHelper.unescape(result.get(0)) + "____" + HtmlHelper.unescape(result.get(1)));
                                    if (FlashComplete != null)
                                        FlashComplete.onFlash("Youtube Video", new CacheEventArgs(blog, embed, tip, cnt, 0));
                                }
                        	}
                        });
                	}
                });
        	}catch(Exception e){
        		
        	}            
        }
        
        private void sohuSwf(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	String vid = "";
            boolean hasId = false;
            for(int i=0,len = url.split("&").length; i< len;i++){
            	if(url.split("&")[i].contains("id")){
            		hasId = true;
            		vid = url.split("&")[i];
            		break;
            	}
            }
            
            if(hasId){
                vid = vid.split("=")[1];

                AsyncHttpClient sohuClient = new AsyncHttpClient();
                sohuClient.get("http://my.tv.sohu.com/videinfo.jhtml?m=viewtv&vid=" + vid, new JsonHttpResponseHandler(){
                	public void onSuccess(final JSONObject sohu){
                		try{
                			if (!sohu.isNull("data"))
                            {	
                                tip.html("");
                                
                                final Object syncLock = new Object();
                                final int count = 0;
                                
                                String allot = sohu.getString("allot");
                				String prot = sohu.getString("prot");
                                final int len=sohu.getJSONObject("data").getJSONArray("clipsURL").length();
                                for(int i=0; i<len;i++){
                    				final String su = sohu.getJSONObject("data").getJSONArray("su").getString(i);                                	
                                	String clipsURL = sohu.getJSONObject("data").getJSONArray("clipsURL").getString(i);
                                	
                                	AsyncHttpClient real = new AsyncHttpClient();
                                	real.get("http://" + allot + "/?prot=" + prot + "&file=" + clipsURL + "&new=" + su, new AsyncHttpResponseHandler(){
                                		public void onSuccess(String url){
                                			//http://114.80.179.215/sohu/7/|1003|101.44.181.236|OeGmtuqi08gUXDD1N0SBtKRs-Oh4D17Wfwgv2Dpqrgc.|1|1|6|42
                                			
                                			String firstPart = url.split("[|]")[0];
                                			String Key = url.split("[|]")[3];
                                			
                                			String child = firstPart + su + "?key=" + Key;
                                			
                                			if (FlashComplete != null)
                                            {
                                                tip.html(tip.html() + child + "|");
                                                
                                                synchronized(syncLock){                                                	
                                                	if(tip.html().split("[|]").length == len){
                                                		try {
                                                			tip.html(tip.html().substring(0, tip.html().length() - 1) + "____" + sohu.getJSONObject("data").getString("coverImg"));
                                                			if (FlashComplete != null)															
																FlashComplete.onFlash(sohu.getJSONObject("data").getString("tvName"), new CacheEventArgs(blog, embed, tip, cnt, 0));
														} catch (JSONException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
                                                	}
                                                }
                                            }
                                		}
                                	});
                                }                                
                            }
                		}
                		catch(JSONException e){
                			e.printStackTrace();
                		}
                	}
                });                        
            }
            else
            {
                tip.html("");
                FlashComplete.onFlash(ReaderApp.getAppContext().getResources().getString(R.string.blog_videooptimize), new CacheEventArgs(blog, embed, tip, cnt, -1));                 
            }
        }
        
        private void sohuNonSwf(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	AsyncHttpClient client = new AsyncHttpClient();
        	client.get(url, new AsyncHttpResponseHandler(){
        		public void onSuccess(String result){
        			int index = result.indexOf("vid");

                    if (index == -1)
                    {
                        tip.html("");
                        FlashComplete.onFlash(ReaderApp.getAppContext().getResources().getString(R.string.blog_videooptimize), new CacheEventArgs(blog, embed, tip, cnt, -1));                            
                    }

                    int comma = result.indexOf("\"", index + 5);
                    String vid = result.substring(index + 5, comma - 5 - index);

                    AsyncHttpClient sohuClient = new AsyncHttpClient();
                    sohuClient.get("http://hot.vrs.sohu.com/vrs_flash.action?vid=" + vid, new JsonHttpResponseHandler(){
                    	public void onSuccess(JSONObject sohu){
                    		try{
                    			if (sohu.isNull("data"))
                                {
                                    tip.html("");                                    
                                    for(int i=0, len=sohu.getJSONObject("data").getJSONArray("clipsURL").length(); i< len; i++)
                                    {
                                    	String child = sohu.getJSONObject("data").getJSONArray("clipsURL").getString(i);
                                        if (FlashComplete != null)
                                        {
                                            tip.html(tip.html() + child + "|");
                                        }
                                    }
                                    tip.html(tip.html().substring(0, tip.html().length() - 1) + "____" + sohu.getJSONObject("data").getString("coverImg"));
                                    if (FlashComplete != null)
                                        FlashComplete.onFlash(sohu.getJSONObject("data").getString("tvName"), new CacheEventArgs(blog, embed, tip, cnt, 0));
                                }
                    		}catch(JSONException e){
                    			e.printStackTrace();
                    		}                    		
                    	}
                    });
        		}
        	});
        }
        
        private void sohu(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	if(url.contains("swf")){
        		sohuSwf(cnt, blog, embed, tip, url);
            }
            else{
            	sohuNonSwf(cnt, blog, embed, tip, url);
            }
        }
        
        private void weiphone(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	new Thread(){
        		public void run(){        	
		            String result = embed.attr("weiphone_src");
		
		            if(result.indexOf("swf") == -1){
		            	tip.html(HtmlHelper.unescape(result) + "____");
			            if (FlashComplete != null)
			                FlashComplete.onFlash("Weiphone", new CacheEventArgs(blog, embed, tip, cnt, 0));	
		            }else{
		            	tip.html("");
                        FlashComplete.onFlash(ReaderApp.getAppContext().getResources().getString(R.string.blog_videooptimize), new CacheEventArgs(blog, embed, tip, cnt, -1));
		            }
        		}
        	}.start();
        }
        
        private void tudou(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	AsyncHttpClient xml = new AsyncHttpClient();
        	xml.setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; MI 2S Build/JRO03L) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        	xml.get(url, new AsyncHttpResponseHandler (){
        		@Override
        		public void onFailure(Throwable t, String error){
        			String url = ""; 
        			if(t.getCause() != null){
        				if(t.getCause().getCause() != null){
        					url = t.getCause().getCause().getMessage();
        				}
        			}
        			
        			String iid = Helper.findValueInUrl(url, "iid");
        			final String title = HtmlHelper.unescape(Helper.findValueInUrl(url, "title"));
        			final String coverImg = HtmlHelper.unescape(Helper.findValueInUrl(url, "snap_pic"));
        			
        			if(iid.length() > 0){
        				
        				tip.html("http://vr.tudou.com/v2proxy/v2?it=" + iid + "&st=52&pw=____" + coverImg);
            			FlashComplete.onFlash(title, new CacheEventArgs(blog, embed, tip, cnt, 0));        				
        			}else{
        				tip.html("");
                        FlashComplete.onFlash(ReaderApp.getAppContext().getResources().getString(R.string.blog_videooptimize), new CacheEventArgs(blog, embed, tip, cnt, -1));
        			}
        			
        			Log.i("RssReader", url);
        		}
        		
        		@Override
        		public void onSuccess(String result){
        			Document doc = Jsoup.parse(result);
        			
        			Element video = null;
        			
        			for(int i=0, len=doc.getAllElements().size(); i<len; i++){
        				if(doc.getAllElements().get(i).nodeName().toLowerCase() == "video"){
        					video =doc.getAllElements().get(i) ;
        				}
        			}
        			
        			if (FlashComplete != null)
    			 	{                    
        				tip.html(video.html());
    					FlashComplete.onFlash(blog, new CacheEventArgs(blog, embed, tip, 0, 0));
			 		}
        		}        		 
        	});        	
        }
        
        private void ku6(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	
        	if(url.contains("refer")){
        		String[] segs = null;
        		segs = url.split("/");
        		int index = Arrays.asList(segs).indexOf("refer");
        		String id = segs[index + 1];
        		
        		AsyncHttpClient client = new AsyncHttpClient();
                client.get("http://v.ku6.com/fetch.htm?t=getVideo4Player&vid=" + id, new JsonHttpResponseHandler(){
                	public void onSuccess(JSONObject root){
                		try {
    	            		if (FlashComplete != null){
    	            			tip.html(root.getJSONObject("data").getString("f"));						
    	            			FlashComplete.onFlash(root.getJSONObject("data").getString("f"), new CacheEventArgs(blog, embed, tip, cnt, 0));
    	            		}
                		} catch (JSONException e) {
    						e.printStackTrace();
    					}
                	}
                });
        	}else{
        		String[] segs = null;
        		segs = embed.attr("flashvars").split("&");
        		
        		String vidUrl = "http://v.ku6vms.com/phpvms/player/forplayer" + 
        							"/vid/" + segs[0].split("=")[1] +
        							"/style/" + segs[1].split("=")[1] + 
        							"/sn/" + segs[2].split("=")[1];
        		
        		AsyncHttpClient vidClient = new AsyncHttpClient();
        		vidClient.addHeader("Referer", "http://v.ku6vms.com/player/default_0.0030.swf");
        		vidClient.post(vidUrl, new JsonHttpResponseHandler(){
                	public void onSuccess(final JSONObject vidRoot){
                		try {
                			AsyncHttpClient client = new AsyncHttpClient();
                            client.get("http://v.ku6.com/fetch.htm?t=getVideo4Player&vid=" + vidRoot.getString("ku6vid"), new JsonHttpResponseHandler(){
                            	public void onSuccess(JSONObject root){
                            		try {
                	            		if (FlashComplete != null){
                	            			tip.html(root.getJSONObject("data").getString("f") + "?stype=mp4____" + vidRoot.getString("picpath"));						
                	            			FlashComplete.onFlash(root.getJSONObject("data").getString("f"), new CacheEventArgs(blog, embed, tip, cnt, 0));
                	            		}
                            		} catch (JSONException e) {
                						e.printStackTrace();
                					}
                            	}
                            });
                		} catch (JSONException e) {
    						e.printStackTrace();
    					}
                	}
                });        		
        	}
        	
        	String[] segs = null;
        	if(url.contains("refer"))
        		segs = url.split("/");
        	else
        		segs = embed.attr("flashvars").split("&");
        	int index = Arrays.asList(segs).indexOf("refer");
        	String id = "";
        	if(index != -1)
        		id = segs[index + 1];
        	else{
        		for(String p : segs){
        			if(p.contains("vid=")){
        				id = p.replace("vid=", "");
        			}
        		}
        	}        		
            
        }
        
        private void qq(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	String[] segs = url.split("&");
        	String vid = "";
        	
        	for(String seg : segs){
        		if(seg.contains("vid")){
        			vid = seg.split("=")[1];
        		}
        	}
        	
        	if(vid.length() == 0)
        		return;

            AsyncHttpClient client = new AsyncHttpClient();
            client.get("http://vv.video.qq.com/geturl?vid=" + vid + "&otype=json&platform=1&ran=0%2E9652906153351068", new AsyncHttpResponseHandler(){
            	public void onSuccess(String root){
            		String tmp = root.replace("QZOutputJson=", "");
            		
            		JSONObject result;
					try {
						result = new JSONObject(tmp.substring(0, tmp.length() - 1));					
	            		if (FlashComplete != null){
	            			
	            			String videoUrl = result.getJSONObject("vd").getJSONArray("vi").getJSONObject(0).getString("url");
	            			
	            			tip.html(videoUrl + "____");						
	            			FlashComplete.onFlash(ReaderApp.getAppContext().getResources().getString(R.string.blog_videooptimize), new CacheEventArgs(blog, embed, tip, cnt, 0));
	            		}
            		} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            });            
        }
        
        private void fivesix(final int cnt, final Blog blog, final Element embed, final Element tip, final String url){
        	String vid = "";
        	
        	//http://www.56.com/u80/v_NjAzNjM0MDU.html
        	int start = url.indexOf("v_");
        	int end = url.indexOf(".html");
        	
        	if(start == -1 || end == -1 || start > end)
        		return;
        	
        	vid = url.substring(start + 2, end); 
        	
        	if(vid.length() == 0)
        		return;

            AsyncHttpClient client = new AsyncHttpClient();
            client.get("http://vxml.56.com/json/" + vid + "/", new JsonHttpResponseHandler(){
            	public void onSuccess(JSONObject root){            		
					try {
	            		if (FlashComplete != null){
	            			String title = root.getJSONObject("info").getString("Subject");
	            			String videoUrl = root.getJSONObject("info").getJSONArray("rfiles").getJSONObject(0).getString("url");
	            			String img = root.getJSONObject("info").getString("bimg");
	            			tip.html(videoUrl + "____" + img);						
	            			FlashComplete.onFlash(title, new CacheEventArgs(blog, embed, tip, cnt, 0));
	            		}
            		} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            });            
        }
        
        private List<String> processYoutube(String rdata)
        {
            List<String> result = new ArrayList<String>();
            String[] rdataArray = HtmlHelper.unescape(rdata).split("&");
            for (int i = 0; i < rdataArray.length; i++)
            {
                if (rdataArray[i].length() > 13)
                {
                    String r0 = rdataArray[i].substring(0, 13);
                    if (r0 == "thumbnail_url")
                    {
                        String r1 = HtmlHelper.unescape(rdataArray[i].substring(14)).replace("/default", "/hqdefault");                        
                        result.add(1, r1);
                    }
                }
                if (rdataArray[i].length() > 26)
                {
                    String r0 = rdataArray[i].substring(0, 26);
                    if (r0 == "url_encoded_fmt_stream_map")
                    {
                        String r1 = HtmlHelper.unescape(rdataArray[i].substring(0,27));
                        String[] temp1 = r1.split(",");
                        ArrayList<Integer> fmt = new ArrayList<Integer>();
                        ArrayList<String> fmt_url = new ArrayList<String>();
                        for (int j = 0; j < temp1.length; j++)
                        {
                            /*
                            temp1[j] = temp1[j].substr(4);
                            var temp2 = temp1[j].split('&itag=');
                            fmt.push(parseInt(temp2[1], 10));
                            fmt_url.push(temp2[0]);
                            */
                            String[] temp2 = temp1[j].split("&");
                            for (int jj = 0; jj < temp2.length; jj++)
                            {
                                int temp_itag = -1;
                                String temp_type = "";
                                if (temp2[jj].substring(0, 5).equals("itag="))
                                {
                                    temp_itag = Integer.valueOf(temp2[jj].substring(5));
                                    fmt.add(temp_itag);
                                }
                                else if (temp2[jj].substring(0, 4).equals("url="))
                                {
                                    fmt_url.add(temp2[jj].substring(4));
                                }
                                else if (temp2[jj].substring(0, 5).equals("type="))
                                {
                                    temp_type = '(' + HtmlHelper.unescape(temp2[jj].substring(5)) + ')';
                                }

                                //if(fmt_str[temp_itag] == 'undefined')
                                //{
                                //    fmt_str[temp_itag] = temp_type;
                                //}
                            }
                        }

                        int index = 0;
                        for(int k : fmt)
                        {
                            if (k == 18 || k == 22 || k == 37 || k == 38 || k == 82 || k == 83 || k == 84 || k == 85)
                            {
                                result.add(0, HtmlHelper.unescape(fmt_url.get(index)));
                            }
                            index++;
                        }
                    }
                }

            }
            return result;
        }

//        private void processTudou(String iid, final Blog blog, final Element embed, final Element tip)
//        {
//            AsyncHttpClient xml = new AsyncHttpClient();
//            xml.setUserAgent("Mozilla/5.0(iPad; U; CPU iPhone OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B314 Safari/531.21.10");
//            xml.get("http://v2.tudou.com/v?vn=02&st=1%2C2&it=" + iid, new AsyncHttpResponseHandler(){
//            	public void onSuccess(String response){
//            		 XElement root = XElement.Parse(eventArgs.Result);
//
//                     if (FlashComplete != null){
//                         tip.html();
//                         if(FlashComplete != null)
//                         FlashComplete.onFlash(blog, new CacheEventArgs(blog, embed, tip, 0, 0));
//                     }
//            	}
//            });           
//        }
        
        private String convertString(List<Character> array){
        	StringBuilder s = new StringBuilder();
            for(Character i : array)
                   s.append(i);
            
            return s.toString();
        }

        private String na(String a) {
            if (a == null || a.length() == 0) 
                return "";
            int c, b;
            int[] h = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1};
            int i = a.length();
            int f = 0;
            List<Character> d = new ArrayList<Character>();
            for (; f < i;) {
                do c = h[a.charAt(f++) & 255];
                while (f < i && -1 == c);
                if (-1 == c) break;
                do b = h[a.charAt(f++) & 255];
                while (f < i && -1 == b);
                if (-1 == b) break;
                d.add((char) (c << 2 | (b & 48) >> 4));
                do {
                    c = a.charAt(f++) & 255;
                    if (61 == c)
                        return convertString(d);
                    c = h[c];
                } while (f < i && -1 == c);
                if (-1 == c) break;
                d.add((char) ((b & 15) << 4 | (c & 60) >> 2));
                do {
                    b = a.charAt(f++) & 255;
                    if (61 == b)
                    	return convertString(d);
                    b = h[b];
                } while (f < i && -1 == b);
                if (-1 == b) break;
                d.add((char) ((c & 3) << 6 | b));
            }
            return convertString(d);
        }

        private String D(String a) {
            if(a == null || a.length() == 0) 
            	return "";
            String b = "";
            int d, g, h;
            int f = a.length();
            int e = 0;
            for (; e < f;) {
                d = a.charAt(e++) & 255;
                if (e == f) {
                    b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(d >> 2);
                    b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((d & 3) << 4);
                    b += "==";
                    break;
                }
                g = a.charAt(e++);
                if (e == f) {
                    b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(d >> 2);
                    b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((d & 3) << 4 | (g & 240) >> 4);
                    b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((g & 15) << 2);
                    b += "=";
                    break;
                }
                h = a.charAt(e++);
                b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(d >> 2);
                b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((d & 3) << 4 | (g & 240) >> 4);
                b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((g & 15) << 2 | (h & 192) >> 6);
                b += "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(h & 63);
            }
            return b;
        }

        private String E(String a, String c) {
        	List<Character> b = new ArrayList<Character>();
            int f = 0;            
            int h = 0;
            for (; 256 > h; h++) 
                b.add((char) h);
            for (h = 0; 256 > h; h++) {
                f = (f + b.get(h) + a.charAt(h % a.length())) % 256;
                Character i = b.get(h);
                b.set(h, b.get(f));
                b.set(f, i);
            }
            List<Character> d = new ArrayList<Character>();
            for (int q = f = h = 0; q < c.length(); q++) {
                h = (h + 1) % 256;
                f = (f + b.get(h)) % 256;
                Character i = b.get(h);
                b.set(h, b.get(f));
                b.set(f, i);
                d.add((char) (c.charAt(q) ^ b.get((b.get(h) + b.get(f)) % 256)));
            }
            return convertString(d);
        }

        private String getFileID(String fileid, double seed)
        {
            String mixed = getFileIDMixString(seed);
            String[] ids = fileid.split("\\*");
            StringBuilder realId = new StringBuilder();
            int idx;
            for (int i = 0; i < ids.length; i++)
            {
                idx = Integer.valueOf(ids[i]);
                realId.append(mixed.toCharArray()[idx]);
            }
            return realId.toString();
        }

        private String getFileIDMixString(double seed)
        {
            StringBuilder mixed = new StringBuilder();
            StringBuilder source = new StringBuilder("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/\\:._-1234567890");
            int index, len = source.length();
            for (int i = 0; i < len; ++i)
            {
                seed = (seed * 211 + 30031) % 65536;
                index = (int)Math.floor(seed / 65536 * source.length());
                mixed.append(source.toString().toCharArray()[index]);
                source.delete(index,index+ 1);
            }
            return mixed.toString();
        }        
    }

