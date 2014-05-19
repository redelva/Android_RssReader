package com.lgq.rssreader.share;

import org.json.JSONArray;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.enums.AccountType;
import com.lgq.rssreader.parser.HttpResponseHandler;
import com.lgq.rssreader.utils.Helper;
import com.lgq.rssreader.utils.HtmlHelper;
import com.lgq.rssreader.utils.ShareHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class EvernoteShare implements BlogShare<String>{
	// Change the following variable to "www.evernote.com" to access the production service
    private final String EvernoteHost = "www.evernote.com";
    private final String EDAMBaseUrl = "https://" + EvernoteHost;

    // UserStore service endpoint
    private final String UserStoreUrl = EDAMBaseUrl + "/edam/user";
    private final String NoteStoreUrl = EDAMBaseUrl + "/edam/note/shard";
	public final String EvernoteUrl = EDAMBaseUrl + "/";

    public void login(final Activity page, final String apiKey, final String secretKey, final ShareHandler<String> handler){
    	AsyncHttpClient client = new AsyncHttpClient();        

    	AsyncHttpResponseHandler response = new AsyncHttpResponseHandler(){
        	public void onSuccess(String result){
        		String loginUrl = EvernoteUrl + "OAuth.action?" + result.split("&")[0];
        		
        		ShareHelper.buildLoginPage(page, AccountType.Evernote);
        		
        		ShareHelper.browserControl.loadUrl(loginUrl);
        		
        		ShareHelper.browserControl.setWebViewClient(new WebViewClient() {
        			public void onPageFinished(WebView view, String url) {		            
	    	            super.onPageFinished(view, url);
	    	            
	    	            if (url.contains("https://api.weibo.com") && url.contains("oauth_token=")){
	    	            	int index = url.indexOf("?");
	                        String ret = url.substring(index + 1);

	                        AsyncHttpClient tokenClient = new AsyncHttpClient();

	                        AsyncHttpResponseHandler tokenHandler = new AsyncHttpResponseHandler(){
        						public void onSuccess(String result){
        							ShareHelper.removeBrowser();

	                                if (handler != null){
	                                	handler.onCallback(HtmlHelper.unescape(result), true, "");
	                                }
        						}
        						
        						public void onFailure(){
        							ShareHelper.removeBrowser();
        							if (handler != null){
        								handler.onCallback(new Exception("FailedToLoginPleaseKeepConnection"));
        							}
        						}
        					};
	                        
	                        tokenClient.get(EvernoteUrl + "oauth?oauth_consumer_key="+ apiKey +"&oauth_signature=" + 
	                        secretKey +"%26&oauth_signature_method=PLAINTEXT&oauth_timestamp="+ System.currentTimeMillis() +
	                        "&oauth_nonce="+ System.currentTimeMillis() +"&" + ret,tokenHandler);
	    	            }
	    	        }
	    		});
        	}
        	
        	public void onFailure(){						
				if (handler != null){
					handler.onCallback(new Exception("FailedToLoginPleaseKeepConnection"));
				}
			}
        };
        
        client.get(EvernoteUrl + "oauth?oauth_consumer_key=" + apiKey + "&oauth_signature=" + secretKey + "%26&oauth_signature_method=PLAINTEXT&oauth_timestamp=" + System.currentTimeMillis() + 
				"&oauth_nonce=" + System.currentTimeMillis() + "&oauth_callback=https%3A%2F%2Fapi.weibo.com%2Foauth2%2Fdefault.html", response);
    }

    public void share(Blog blog, String accessToken, final ShareHandler<String> handler){
    	
//    {            
//        String noteStoreUrl =
//            accessToken.split('&').First(s => s.Contains("edam_noteStoreUrl=")).Replace("edam_noteStoreUrl=", String.Empty);
//
//        String authToken = accessToken.Split('&').First(s => s.Contains("oauth_token=")).Replace("oauth_token=", String.Empty);
//        //String noteStoreUrl = NoteStoreUrl + "/" + accessToken.Split(':')[0].Split('=')[1];
//
//        TTransport noteStoreTransport = new THttpClient(new Uri(noteStoreUrl));
//        TProtocol noteStoreProtocol = new TBinaryProtocol(noteStoreTransport);
//        NoteStore.Client noteStore = new NoteStore.Client(noteStoreProtocol);
//
//        try
//        {
//            List<Notebook> books = noteStore.listNotebooks(authToken);
//
//            Notebook rss;
//
//            if (!books.Any(b => b.Name == Resources.StringResources.RssReader))
//            {
//                Notebook notebook = new Notebook
//                {
//                    DefaultNotebook = false,
//                    Name = Resources.StringResources.RssReader
//                };
//                rss = noteStore.createNotebook(authToken, notebook);
//            }
//            else
//            {
//                rss = books.First(b => b.Name == Resources.StringResources.RssReader);
//            }
//
//            String content = "";
//            if (!String.IsNullOrEmpty(blog.CachedContent))
//                content = blog.CachedContent;
//            else
//                content = blog.CachedDescription;
//
//            content = Helper.ConvertHtmlToEnml(content);
//
//            Note newNote = new Note
//            {
//                NotebookGuid = rss.Guid,
//                Title = blog.Title,
//                Content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
//                          "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
//                          "<en-note>" + content + "<br/>" +
//                          "</en-note>"
//            };
//        
//            Note createdNote = noteStore.createNote(authToken, newNote);
//
//            ShareCallBack(this, new ShareEventArgs<String>(true, createdNote.Title));
//        }
//        catch (TTransportException e)
//        {
//            Debug.WriteLine(e.Message);
//            ShareCallBack(this, new ShareEventArgs<String>(false, Resources.StringResources.NetworkError));
//        }
//        catch (EDAMUserException e)
//        {
//            Debug.WriteLine(e.Message);
//            ShareCallBack(this, new ShareEventArgs<String>(false, Resources.StringResources.NotFormat));
//        }
//        catch (Exception e)
//        {
//            ShareCallBack(this, new ShareEventArgs<String>(false, e.Message));
//        }
    }	
}