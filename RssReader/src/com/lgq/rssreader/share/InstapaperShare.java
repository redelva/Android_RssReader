//package com.lgq.rssreader.share;
//
//public class InstapaperShare {
//	#region IBlogShare ≥…‘±
//
//    public void BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//    {
//        
//    }
//
//    public event LoginCompletedHandler<InstapaperAccount> LoginCallBack;
//
//    public void BeginShare(Blog blog, InstapaperAccount account)
//    {
//        WebClient client = new WebClient();
//
//        //https://readitlaterlist.com/v2/add?username=name&password=123&apikey=yourapikey&url=http://google.com&title=Google
//        //username={0}&password={1}&
//
//        string url =
//            string.Format(
//                "https://www.instapaper.com/api/add?url={2}&title={3}",
//                HttpUtility.UrlEncode(account.Load("username")),
//                HttpUtility.UrlEncode(account.Load("password")),
//                HttpUtility.UrlEncode(blog.Link),
//                HttpUtility.UrlEncode(blog.Title));
//
//        client.Headers["Host"] = "www.instapaper.com";
//        client.Headers["Authorization"] = "Basic " +
//                                          Convert.ToBase64String(
//                                              UTF8Encoding.UTF8.GetBytes(account.Load("username") + ":" +
//                                                                         account.Load("password")));
//        client.DownloadStringAsync(new Uri(url, UriKind.Absolute),"GET");            
//
//        client.DownloadStringCompleted += (sender, args) =>
//        {
//            if (args.Error == null)
//            {
//                if(args.Result == "201")
//                {
//                    if (ShareCallBack != null)
//                    {
//                        ShareCallBack(this, new ShareEventArgs<InstapaperAccount>(true, null));
//                    }
//                }
//                else
//                {
//                    if (ShareCallBack != null)
//                    {
//                        ShareCallBack(this, new ShareEventArgs<InstapaperAccount>(new Exception(args.Result)));
//                    }
//                }
//            }
//            else
//            {
//                if (args.Error.InnerException != null && 
//                    args.Error.InnerException.InnerException != null && 
//                    args.Error.InnerException.InnerException.Message.StartsWith("[net_WebHeaderInvalidControlChars]"))
//                {
//                    if (ShareCallBack != null)
//                    {
//                        ShareCallBack(this, new ShareEventArgs<InstapaperAccount>(true, null));
//                    }
//                }
//                else
//                {
//                    if (ShareCallBack != null)
//                    {
//                        ShareCallBack(this, new ShareEventArgs<InstapaperAccount>(args.Error));
//                    }   
//                }                    
//            }
//        };
//    }
//
//    public event ShareCompletedHandler<InstapaperAccount> ShareCallBack;
//
//    #endregion
//}
