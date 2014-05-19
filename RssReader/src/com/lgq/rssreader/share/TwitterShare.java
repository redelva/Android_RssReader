//package com.lgq.rssreader.share;
//
//public class TwitterShare {
//	private const string twitterApiUrl = "https://api.twitter.com/oauth/authorize";
//    private const string twitterRequestTokenUrl = "https://api.twitter.com/oauth/request_token";
//    private const string twitterAccessTokenUrl = "https://api.twitter.com/oauth/access_token/";
//    private const string twitterPostUrl = "https://api.twitter.com/1/statuses/update.json";
//
//    #region IBlogShare ≥…‘±        
//
//    public void BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//    {            
//        WebClient client = new WebClient();
//
//        IDictionary<string, string> param = new Dictionary<string, string>();
//
//        param.Add("oauth_callback", "www.twitter.com");
//        param.Add("oauth_consumer_key", HttpUtility.UrlEncode(Constants.TwitterApiKey));
//        param.Add("oauth_nonce", HttpUtility.UrlEncode(Helper.ConvertDateTimeToStamp(DateTime.Now).ToString()));            
//        param.Add("oauth_signature_method",HttpUtility.UrlEncode("HMAC-SHA1"));
//        param.Add("oauth_timestamp", HttpUtility.UrlEncode(Helper.ConvertDateTimeToStamp(DateTime.Now).ToString()));
//        param.Add("oauth_version", HttpUtility.UrlEncode("1.0"));
//
//        param.Add("oauth_signature", HttpUtility.UrlEncode(SignTwitter("POST", twitterRequestTokenUrl, param.OrderBy(p => p.Key), Constants.TwitterApiSecret + "&")));
//
//        //oauth_consumer_key="cChZNFj6T5R0TigYB9yd1w",
//        //oauth_nonce="ea9ec8429b68d6b77cd5600adbbb0456",
//        //oauth_signature="F1Li3tvehgcraF8DMJ7OyxO4w9Y%3D",
//        //oauth_signature_method="HMAC-SHA1",
//        //oauth_timestamp="1318467427",
//        //oauth_version="1.0"
//
//        StringBuilder sb = new StringBuilder();
//        foreach (KeyValuePair<string, string> pair in param)
//        {
//            sb.Append(pair.Key + "=" + pair.Value + "&");
//        }
//
//        sb.Remove(sb.Length - 1, 1);
//
//        //Common.Helper.BuildLoginPage(page, AccountType.Twitter);
//
//        //Helper.browserControl.Navigate(new Uri(twitterRequestTokenUrl + "?" + sb.ToString()));               
//
//        client.UploadStringAsync(new Uri(twitterRequestTokenUrl),"POST", sb.ToString());            
//
//        client.UploadStringCompleted += (sender, args) =>
//        {
//            if(args.Error == null)
//            {
//                //oauth_token=opknaEaCfXb3QYn4RPDHYT5QmMyDXSHj7iCrqNl5Q&
//                //oauth_token_secret=ejocIHGBLfhev5lclZHZTk8v2FF1UNPyVYH3SPuclyw&
//                //oauth_callback_confirmed=true
//                string[] tokens = args.Result.Split('&');
//                string oauth_token = tokens[0].Split('=')[1];
//                string oauth_token_secret = tokens[1].Split('=')[1];
//                                                              
//                Common.Helper.BuildLoginPage(page, AccountType.Twitter);
//
//                Helper.browserControl.Navigate(new Uri(twitterApiUrl + "?oauth_token=" + oauth_token));
//
//                Helper.browserControl.Navigating += (o, eventArgs) =>
//                {
//                    string returnBackUri = eventArgs.Uri.ToString();
//                    int index = returnBackUri.IndexOf("www.twitter.com");
//                    if(index != -1)
//                    {
//                        Deployment.Current.Dispatcher.BeginInvoke(() =>
//                                                                    {
//                                                                        Common.Helper.RemoveBrowser();
//                                                                    });
//                                                                                                     
//                        string tokenString = returnBackUri.Substring(index);
//
//                        int questionMark = tokenString.IndexOf('?');
//
//                        string query = tokenString.Substring(questionMark);
//
//                        if(!query.Contains("denied"))
//                        {
//                            string token = query.Split('&')[0].Split('=')[1];
//                            string oauth_verifier = query.Split('&')[1].Split('=')[1];
//
//                            WebClient accessTokenClient = new WebClient();
//
//                            IDictionary<string, string> accessTokenParam = new Dictionary<string, string>();
//
//                            accessTokenParam.Add("oauth_consumer_key", HttpUtility.UrlEncode(Constants.TwitterApiKey));
//                            accessTokenParam.Add("oauth_nonce", HttpUtility.UrlEncode(Helper.ConvertDateTimeToStamp(DateTime.Now).ToString()));
//                            accessTokenParam.Add("oauth_signature_method", HttpUtility.UrlEncode("HMAC-SHA1"));
//                            accessTokenParam.Add("oauth_timestamp", HttpUtility.UrlEncode(Helper.ConvertDateTimeToStamp(DateTime.Now).ToString()));
//                            accessTokenParam.Add("oauth_token", HttpUtility.UrlEncode(token));
//                            accessTokenParam.Add("oauth_version", HttpUtility.UrlEncode("1.0"));
//
//                            accessTokenParam.Add("oauth_signature", HttpUtility.UrlEncode(SignTwitter("POST", twitterAccessTokenUrl, accessTokenParam.OrderBy(p => p.Key), Constants.TwitterApiSecret + "&" + oauth_token_secret)));
//
//                            StringBuilder accessTokenBuilder = new StringBuilder();
//                            foreach (KeyValuePair<string, string> pair in accessTokenParam)
//                            {
//                                accessTokenBuilder.Append(pair.Key + "=" + pair.Value + "&");
//                            }
//
//                            accessTokenBuilder.Remove(accessTokenBuilder.Length - 1, 1);
//
//                            accessTokenClient.UploadStringAsync(new Uri(twitterAccessTokenUrl), "POST", accessTokenBuilder.ToString());
//
//                            accessTokenClient.UploadStringCompleted += (sender1, completedEventArgs) =>
//                            {
//                                if (completedEventArgs.Error == null)
//                                {
//                                    string result = completedEventArgs.Result;
//                                    //oauth_token=280556231-lTJadMPvNPeOBZGuIhzOGyDDJ449XcqL6o7BPhuc&
//                                    //   oauth_token_secret=mKH9gRNq8sx3uEGaaQ1OSo8GYRD0ZKXdChycsKwA&
//                                    //   user_id=280556231&
//                                    //   screen_name=luguoqing                                                                                     
//
//                                    string accessToken = result.Split('&')[0].Split('=')[1];
//                                    string oauth_accesstoken_secret = result.Split('&')[1].Split('=')[1];
//                                    string user_id = result.Split('&')[2].Split('=')[1];
//                                    string screen_name = result.Split('&')[3].Split('=')[1];
//
//                                    TwitterAccount account = new TwitterAccount();
//                                    account.Save("AccessToken", accessToken);
//                                    account.Save("oauth_token_secret", oauth_accesstoken_secret);
//                                    account.Save("user_id", user_id);
//                                    account.Save("screen_name", screen_name);
//
//                                    if (LoginCallBack != null)
//                                    {
//                                        LoginCallBack(this, new ShareEventArgs<TwitterAccount>(true, account));
//                                    }
//                                }
//                                else
//                                {
//                                    if (LoginCallBack != null)
//                                    {
//                                        LoginCallBack(this, new ShareEventArgs<TwitterAccount>(new Exception(Resources.StringResources.FailedToLoginPleaseKeepConnection)));
//                                    }
//                                }
//                            };
//                        }
//                        else
//                        {
//                            if (LoginCallBack != null)
//                            {
//                                LoginCallBack(this, new ShareEventArgs<TwitterAccount>(new Exception(Resources.StringResources.PleaseAuthorizedToAccessTwitter)));
//                            }
//                        }                            
//                    }
//                    //else
//                    //{
//                    //    if (LoginCallBack != null && eventArgs.Uri.Host == "")
//                    //    {
//                    //        LoginCallBack(this, new ShareEventArgs<TwitterAccount>(false, null));
//                    //    }
//                    //}   
//                };
//            }
//            else
//            {
//                if(LoginCallBack != null)
//                {
//                    LoginCallBack(this, new ShareEventArgs<TwitterAccount>(new Exception(Resources.StringResources.FailedToLoginPleaseKeepConnection)));
//                }
//            }                                  
//        };        
//    }
//
//    private string SignTwitter(string httpMethod, string url, IOrderedEnumerable<KeyValuePair<string, string>> param, string secretKey)
//    {
//        StringBuilder sb = new StringBuilder();
//        sb.Append(httpMethod.ToUpper() + "&");
//        sb.Append(UrlEncode(url) + "&");
//        foreach (KeyValuePair<string, string> pair in param)
//        {
//            sb.Append(UrlEncode(pair.Key + "=" + pair.Value + "&"));
//        }
//        sb.Remove(sb.Length - 3, 3);
//
//        System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();
//
//        byte[] keyByte = encoding.GetBytes(secretKey);
//
//        HMACSHA1 method = new HMACSHA1(keyByte);
//
//        byte[] message = encoding.GetBytes((sb.ToString()));
//
//        return Convert.ToBase64String(method.ComputeHash(message), 0, method.ComputeHash(message).Length);
//
//        //return encoding.GetString(method.ComputeHash(message));
//    }
//
//    protected string unreservedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~";
//
//    /// <summary>
//    /// This is a different Url Encode implementation since the default .NET one outputs the percent encoding in lower case.
//    /// While this is not a problem with the percent encoding spec, it is used in upper case throughout OAuth
//    /// </summary>
//    /// <param name="value">The value to Url encode</param>
//    /// <returns>Returns a Url encoded string</returns>
//    protected string UrlEncode(string value)
//    {
//        StringBuilder result = new StringBuilder();
//
//        foreach (char symbol in value)
//        {
//            if (unreservedChars.IndexOf(symbol) != -1)
//            {
//                result.Append(symbol);
//            }
//            else
//            {
//                result.Append('%' + String.Format("{0:X2}", (int)symbol));
//            }
//        }
//
//        return result.ToString();
//    }
//    
//    public event LoginCompletedHandler<TwitterAccount> LoginCallBack;
//
//    public void BeginShare(Blog blog, TwitterAccount account)
//    {                        
//        WebClient client = new WebClient();
//
//        IDictionary<string, string> param = new Dictionary<string, string>();
//
//        param.Add("status", HttpUtility.UrlEncode(blog.Title).ToUpper());
//        //param.Add("include_entities", "true");
//        param.Add("oauth_consumer_key", HttpUtility.UrlEncode(Constants.TwitterApiKey));
//        param.Add("oauth_nonce", HttpUtility.UrlEncode(Helper.ConvertDateTimeToStamp(DateTime.Now).ToString()));            
//        param.Add("oauth_signature_method",HttpUtility.UrlEncode("HMAC-SHA1"));
//        param.Add("oauth_timestamp", HttpUtility.UrlEncode(Helper.ConvertDateTimeToStamp(DateTime.Now).ToString()));
//        param.Add("oauth_version", HttpUtility.UrlEncode("1.0"));            
//        param.Add("oauth_token", HttpUtility.UrlEncode(account.Load("AccessToken")));            
//
//        param.Add("oauth_signature", HttpUtility.UrlEncode(SignTwitter("POST", twitterPostUrl, param.OrderBy(p => p.Key), Constants.TwitterApiSecret + "&" + account.Load("oauth_token_secret"))));
//       
//        StringBuilder sb = new StringBuilder();
//        foreach (KeyValuePair<string, string> pair in param)
//        {
//            sb.Append(pair.Key + "=" + pair.Value + "&");
//        }
//
//        sb.Remove(sb.Length - 1, 1);            
//     
//        client.UploadStringAsync(new Uri(twitterPostUrl),"POST", sb.ToString());            
//
//        client.UploadStringCompleted += (sender, args) =>
//                                            {
//                                                if(args.Error == null)
//                                                {
//                                                    if (ShareCallBack != null)
//                                                    {
//                                                        ShareCallBack(this, new ShareEventArgs<TwitterAccount>(true, account));
//                                                    }
//                                                }
//                                                else
//                                                {
//                                                    if(ShareCallBack != null)
//                                                    {
//                                                        ShareCallBack(this, new ShareEventArgs<TwitterAccount>(new Exception(args.Error.Message)));
//                                                    }
//                                                }
//                                            };
//    }
//
//    public event ShareCompletedHandler<TwitterAccount> ShareCallBack;
//}
