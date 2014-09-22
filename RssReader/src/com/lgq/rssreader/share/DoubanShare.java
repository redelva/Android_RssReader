//package com.lgq.rssreader.share;
//
//public class DoubanShare {
//	#region IBlogShare 成员
//
//    private string _apiKey;
//
//    public void BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//    {
//        _apiKey = apiKey;
//        
//        Helper.BuildLoginPage(page, AccountType.Douban);
//        
//        Helper.browserControl.Navigate(new Uri(
//            string.Format("https://www.douban.com/service/auth2/auth?client_id={0}&redirect_uri={1}&response_type=code&scope=shuo_basic_r,shuo_basic_w,community_basic_note",
//            apiKey,
//            "http://www.douban.com"), UriKind.Absolute));
//        Helper.browserControl.Navigated += (sender, eventArgs) =>
//        {
//            if (eventArgs.Uri.OriginalString.StartsWith("http://www.douban.com") && eventArgs.Uri.OriginalString.Contains("code"))
//            {                    
//                int index = eventArgs.Uri.OriginalString.IndexOf("?");
//                string[] values = eventArgs.Uri.OriginalString.Substring(index + 1).Split('&');
//                if(eventArgs.Uri.OriginalString.Contains("error"))
//                {
//                    string error = values.FirstOrDefault(v => v.Contains("error")).Split('=')[1];
//                    ShareCallBack(this, new ShareEventArgs<string>(false, error));
//                }
//                else
//                {
//                    string code = values.FirstOrDefault(v => v.Contains("code")).Split('=')[1];
//
//                    WebClient client = new WebClient();
//
//                    StringBuilder sb = new StringBuilder();
//                    
//                    client.Headers[HttpRequestHeader.ContentType] = "application/x-www-form-urlencoded";
//                    sb.Append("client_id=" + apiKey);
//                    sb.Append("&client_secret=" + secretKey);
//                    sb.Append("&redirect_uri=http://www.douban.com");
//                    sb.Append("&grant_type=authorization_code");
//                    sb.Append("&code=" + code);
//
//                    client.UploadStringAsync(new Uri("https://www.douban.com/service/auth2/token", UriKind.Absolute), "POST", HttpUtility.HtmlEncode(sb.ToString()));
//
//                    client.UploadStringCompleted += (o, args) =>
//                    {
//                        if (args.Error == null)
//                        {
//                            JObject result = JObject.Parse(args.Result);
//
//                            Deployment.Current.Dispatcher.BeginInvoke(() =>
//                            {
//                                Common.Helper.RemoveBrowser();
//                                if (LoginCallBack != null)
//                                {
//                                    LoginCallBack(this, new ShareEventArgs<string>(true, HttpUtility.UrlDecode(result["access_token"].Value<string>())));
//                                }
//                            });
//                        }
//                        else
//                        {
//                            ShareCallBack(this, new ShareEventArgs<string>(false, args.Error.Message));
//                        }
//                    };   
//                }                    
//            }
//        };                
//    }
//
//    public event LoginCompletedHandler<string> LoginCallBack;
//   
//    public void BeginShare(Blog blog, string accessToken)
//    {
//        WebClient client = new WebClient();
//
//        StringBuilder sb = new StringBuilder();
//        //title	日记标题	必传，不能为空	
//        //privacy	隐私控制	为public，friend，private，分布对应公开，朋友可见，仅自己可见
//        //can_reply	是否允许回复	必传, true或者false
//        //content	日记内容, 如果含图片，使用“<图片p_pid>”伪tag引用图片, 如果含链接，使用html的链接标签格式，或者直接使用网址	必传
//        client.Headers["Authorization"] = "Bearer " + accessToken;
//        sb.Append("source=" + _apiKey);
//        sb.Append("&rec_title=" + blog.Title);
//        sb.Append("&rec_url=" + blog.Link);
//        sb.Append("&text=" + Helper.parseHtml(blog.Description));            
//
//        client.Headers[HttpRequestHeader.ContentType] = "application/x-www-form-urlencoded; charset=UTF-8";
//        client.UploadStringAsync(new Uri("https://api.douban.com/shuo/v2/statuses/"), "POST", sb.ToString());
//
//        client.UploadStringCompleted += (sender, args) =>
//        {
//            if (args.Error == null)
//            {
//                if (ShareCallBack != null)
//                {
//                    ShareCallBack(this, new ShareEventArgs<string>(true, string.Empty));
//                }
//
//                //JObject obj = JObject.Parse(args.Result);
//
//                //if (obj["id"] == null || obj["id"].Value<int>() == 0)
//                //{
//                //    if (ShareCallBack != null)
//                //    {
//                //        if (obj["error_code"].Value<int>() == 2002) //accesstoken过期                                                                
//                //            ShareCallBack(this, new ShareEventArgs<string>(false, Resources.StringResources.ReloginRenRen));
//                //        else
//                //            ShareCallBack(this, new ShareEventArgs<string>(false, obj["error_msg"].Value<string>()));
//                //    }
//                //}
//                //else
//                //{
//                //    if (ShareCallBack != null)
//                //    {
//                //        ShareCallBack(this, new ShareEventArgs<string>(true, string.Empty));
//                //    }
//                //}
//            }
//            else
//            {
//                if (LoginCallBack != null)
//                {
//                    LoginCallBack(this, new ShareEventArgs<string>(false, args.Error.Message));
//                }
//            }
//        };
//    }
//
//    public event ShareCompletedHandler<string> ShareCallBack;
//
//    #endregion
//}
