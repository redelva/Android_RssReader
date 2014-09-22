//package com.lgq.rssreader.share;
//
//public class LiveShare {
//	#region IBlogShare ³ÉÔ±
//
//    public void BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//    {
//        RssReader.Common.Helper.BuildLoginPage(page, AccountType.Live);
//
//        Helper.browserControl.Navigate(new Uri(
//            "https://login.live.com/oauth20_authorize.srf?display=touch&client_id=00000000400C6407&scope=wl.share%20wl.signin%20wl.basic&response_type=code&redirect_uri=https://login.live.com/oauth20_desktop.srf",
//            UriKind.Absolute));
//        Helper.browserControl.IsScriptEnabled = true;
//        Helper.browserControl.Navigated += (sender, eventArgs) =>            
//        {
//            if (eventArgs.Uri.ToString().Contains("https://login.live.com/oauth20_desktop.srf") && eventArgs.Uri.ToString().Contains("code="))
//            {
//                string[] rets = eventArgs.Uri.ToString().Split('?')[1].Split('&');
//                string code = rets.FirstOrDefault(s => s.Contains("code=")).Split('=')[1];
//
//                WebClient client = new WebClient();
//
//                client.Headers[HttpRequestHeader.ContentType] = "application/x-www-form-urlencoded; charset=UTF-8";
//                string param = "client_id={0}&redirect_uri={1}&client_secret={2}&code={3}&grant_type=authorization_code";
//
//                client.UploadStringAsync(new Uri("https://login.live.com/oauth20_token.srf",UriKind.Absolute), "POST",
//                    string.Format(param, apiKey, "https://login.live.com/oauth20_desktop.srf", secretKey, code));
//
//                client.UploadStringCompleted += (o, args) =>
//                {
//                    if(args.Error == null)
//                    {
//                        JObject obj = JObject.Parse(args.Result);                            
//
//                        Deployment.Current.Dispatcher.BeginInvoke(() =>
//                        {
//                            Common.Helper.RemoveBrowser();
//                            if (LoginCallBack != null)
//                            {
//                                LoginCallBack(this, new ShareEventArgs<string>(true, HttpUtility.UrlDecode(obj["access_token"].Value<string>())));
//                            }   
//                        }); 
//                    }
//                };
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
//        //client.Headers[HttpRequestHeader.Authorization] = "Bearer " + accessToken;            
//
//        string paras = "{message: \""+ RssReader.Resources.StringResources.RssReader +"\",link: \"" + blog.Link + "\",description: \""+ Common.Helper.parseHtml(blog.Description) +"\",name: \"" + HttpUtility.HtmlEncode(blog.Title) + "\"}";
//
//        client.Headers[HttpRequestHeader.ContentLength] = paras.Length.ToString();
//        client.Headers[HttpRequestHeader.ContentType] = "application/json; charset=UTF-8";
//
//        client.UploadStringAsync(new Uri("https://apis.live.net/v5.0/me/share?access_token=" + accessToken, UriKind.Absolute), "POST", paras);
//
//        client.UploadStringCompleted += (sender, args) =>
//        {
//            if(args.Error == null)
//            {
//                if (ShareCallBack != null)
//                {
//                    ShareCallBack(this, new ShareEventArgs<string>(true, string.Empty));
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
