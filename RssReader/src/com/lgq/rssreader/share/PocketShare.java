package com.lgq.rssreader.share;
//package com.lgq.rssreader.parser;
//
//import com.lgq.rssreader.share.Blog;
//import com.lgq.rssreader.share.PhoneApplicationPage;
//import com.lgq.rssreader.share.ShareEventArgs;
//import com.lgq.rssreader.share.Uri;
//import com.lgq.rssreader.share.WebClient;
//import com.lgq.rssreader.share.event;
//import com.lgq.rssreader.share.region;
//import com.lgq.rssreader.share.string;
//import com.lgq.rssreader.share.var;
//
//public class PocketShare {
//	private string _secretKey;
//    #region IBlogShare ³ÉÔ±
//
//    public void BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//    {
//        WebClient client = new WebClient();
//
//        string url = "https://getpocket.com/v3/oauth/request";
//
//        string data = "consumer_key=" + apiKey + "&redirect_uri=https://api.weibo.com/oauth2/default.html";
//        client.Headers[HttpRequestHeader.Host] = "getpocket.com";
//        client.Headers[HttpRequestHeader.ContentType] = "application/x-www-form-urlencoded; charset=UTF-8";
//        client.Headers["X-Accept"] = "application/x-www-form-urlencoded";
//        client.Headers[HttpRequestHeader.ContentLength] = data.Length.ToString();
//
//        client.UploadStringAsync(new Uri(url, UriKind.Absolute), "POST", data);
//
//        client.UploadStringCompleted += (sender, args) =>
//        {
//            if (args.Error == null)
//            {
//                var loginUrl = "https://getpocket.com/auth/authorize?request_token=" + args.Result.Split('=')[1] + "&redirect_uri=https://api.weibo.com/oauth2/default.html";
//
//                RssReader.Common.Helper.BuildLoginPage(page, AccountType.Pocket);
//
//                Common.Helper.browserControl.Navigate(new Uri(loginUrl));
//
//                Common.Helper.browserControl.Navigated += (obj, eventArgs) =>
//                {
//                    if (eventArgs.Uri.Host == "api.weibo.com")
//                    {
//                        WebClient tokenClient = new WebClient();
//
//                        tokenClient.Headers[HttpRequestHeader.Host] = "getpocket.com";
//                        tokenClient.Headers[HttpRequestHeader.ContentType] = "application/x-www-form-urlencoded; charset=UTF-8";
//                        tokenClient.Headers["X-Accept"] = "application/x-www-form-urlencoded";
//                        string tokenData = "consumer_key=" + apiKey + "&" + args.Result;
//                        tokenClient.Headers[HttpRequestHeader.ContentLength] = tokenData.Length.ToString();
//
//                        tokenClient.UploadStringAsync(new Uri("https://getpocket.com/v3/oauth/authorize", UriKind.Absolute), "POST", tokenData);
//
//                        tokenClient.UploadStringCompleted += (o, e) =>
//                        {
//                            if (e.Error == null)
//                            {
//                                //access_token=5678defg-5678-defg-5678-defg56&username=pocketuser
//
//                                if (LoginCallBack != null)
//                                {
//                                    LoginCallBack(this, new ShareEventArgs<string>(true, e.Result.Split('&')[0].Split('=')[1]));
//                                }
//                            }
//                            else
//                            {
//                                if (LoginCallBack != null)
//                                {
//                                    LoginCallBack(this, new ShareEventArgs<string>(false, e.Error.Message));
//                                }
//                            }
//
//                            Deployment.Current.Dispatcher.BeginInvoke(() =>
//                            {
//                                Common.Helper.RemoveBrowser();
//                            });
//                        };
//                    }
//                };                    
//            }
//            else
//            {
//                if (LoginCallBack != null)
//                {
//                    Deployment.Current.Dispatcher.BeginInvoke(() =>
//                    {
//                        Common.Helper.RemoveBrowser();
//                    });
//                    LoginCallBack(this, new ShareEventArgs<string>(false, args.Error.Message));
//                }
//            }
//        };
//    }
//
//    public event LoginCompletedHandler<string> LoginCallBack;
//
//    public void BeginShare(Blog blog, string token)
//    {
//        WebClient client = new WebClient();
//
//        string url = "https://getpocket.com/v3/add";            
//
//        //{"url":"http:\/\/pocket.co\/s8Kga",
//        //"title":"iTeaching: The New Pedagogy (How the iPad is Inspiring Better Ways of 
//        //Teaching)",
//        //"time":1346976937,
//        //"consumer_key":"1234-abcd1234abcd1234abcd1234",
//        //"access_token":"5678defg-5678-defg-5678-defg56"}
//        StringBuilder sb = new StringBuilder();
//        sb.Append("{\"url\":\"" + blog.Link + "\",");
//        sb.Append("\"title\":\"" + blog.Title + "\",");
//        sb.Append("\"time\":\"" + Helper.ConvertDateTimeToStamp(DateTime.Now) + "\",");
//        sb.Append("\"consumer_key\":\"" + Constants.PocketSecret + "\",");
//        sb.Append("\"access_token\":\"" + token + "\"}");
//
//        client.Headers[HttpRequestHeader.Host] = "getpocket.com";
//        client.Headers[HttpRequestHeader.ContentType] = "application/json; charset=UTF-8";
//        client.Headers["X-Accept"] = "application/json";
//        client.Headers[HttpRequestHeader.ContentLength] = sb.Length.ToString();
//
//        client.UploadStringAsync(new Uri(url, UriKind.Absolute), "POST", sb.ToString());
//
//        client.UploadStringCompleted += (sender, args) =>
//        {
//            if(args.Error == null)
//            {
//                if (ShareCallBack != null)
//                {
//                    ShareCallBack(this, new ShareEventArgs<string>(true, null));
//                }                     
//            }
//            else
//            {
//                if (ShareCallBack != null)
//                {
//                    ShareCallBack(this, new ShareEventArgs<string>(false, args.Error.Message));
//                }  
//            }
//        };
//    }
//
//    public event ShareCompletedHandler<string> ShareCallBack;
//
//    #endregion
//}
