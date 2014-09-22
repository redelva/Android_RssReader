//package com.lgq.rssreader.share;
//
//public class FacebookShare {
//	#region IBlogShare ≥…‘±
//
//    public void BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//    {
//        string[] extendedPermissions = new[] { "publish_stream", "offline_access" };
//
//        var oauth = new FacebookOAuthClient { AppId = apiKey };
//
//        var parameters = new Dictionary<string, object>
//        {
//            { "response_type", "token" },
//            { "display", "touch" }
//        };
//
//        if (extendedPermissions != null && extendedPermissions.Length > 0)
//        {
//            var scope = new StringBuilder();
//            scope.Append(string.Join(",", extendedPermissions));
//            parameters["scope"] = scope.ToString();
//        }
//
//        var loginUrl = oauth.GetLoginUrl(parameters);
//
//        RssReader.Common.Helper.BuildLoginPage(page, AccountType.Facebook);
//
//        Common.Helper.browserControl.Navigate(loginUrl);
//
//        Common.Helper.browserControl.Navigated += (o, args) =>
//        {                
//            FacebookOAuthResult result;
//            if (FacebookOAuthResult.TryParse(args.Uri, out result))
//            {
//                if (result.IsSuccess)
//                {                        
//                    if(LoginCallBack != null)
//                    {
//                        LoginCallBack(this, new ShareEventArgs<string>(true, result.AccessToken));
//                    }                        
//                }
//                else
//                {
//                    var errorDescription = result.ErrorDescription;
//                    var errorReason = result.ErrorReason;
//                    if(LoginCallBack != null)
//                    {
//                        Deployment.Current.Dispatcher.BeginInvoke(() =>
//                        {
//                            Common.Helper.RemoveBrowser();
//                        });
//                        LoginCallBack(this, new ShareEventArgs<string>(false, errorDescription));
//                    }                        
//                }
//            }
//        };
//        Common.Helper.browserControl.NavigationFailed += (o, args) =>
//        {
//            if (LoginCallBack != null)
//            {
//                Deployment.Current.Dispatcher.BeginInvoke(() =>
//                {
//                    Common.Helper.RemoveBrowser();
//                });
//                LoginCallBack(this, new ShareEventArgs<string>(new Exception(Resources.StringResources.FailedToLoginPleaseKeepConnection)));
//            } 
//        };
//    }
//
//    public event LoginCompletedHandler<string> LoginCallBack;
//
//    public void BeginShare(Blog blog, string accessToken)
//    {
//        var fb = new FacebookClient(accessToken);
//
//        IDictionary<string, object> param = new Dictionary<string, object>();
//        param["message"] = Resources.StringResources.FacebookTitle;
//        param["link"] = blog.Link;//"http://www.example.com/article.html";
//        //param["picture"] = "http://www.example.com/article-thumbnail.jpg";
//        param["name"] = blog.Title;//"Article Title";
//        param["caption"] = Resources.StringResources.CaptionLink;
//        param["description"] = blog.Description;//"Longer description of the link";
//        //param["actions"] = new
//        //{
//        //    name = "View on Zombo",
//        //    link = "http://www.zombo.com",
//        //};
//        //param["privacy"] = new
//        //{
//        //    value = "ALL_FRIENDS",
//        //};
//        //parameters["targeting"] = new
//        //{
//        //    countries = "US",
//        //    regions = "6,53",
//        //    locales = "6",
//        //};
//        fb.PostAsync("me/feed", param);
//        fb.PostCompleted += (sender, eventArgs) =>
//        {
//            Deployment.Current.Dispatcher.BeginInvoke(() =>
//                                                          {
//                                                              if (eventArgs.Error != null)
//                                                              {
//                                                                  if(ShareCallBack != null)
//                                                                  {
//                                                                      ShareCallBack(this, new ShareEventArgs<string>(false, eventArgs.Error.Message));
//                                                                  }                                                                       
//                                                              }
//                                                              else
//                                                              {
//                                                                  if(ShareCallBack != null)
//                                                                  {
//                                                                      ShareCallBack(this, new ShareEventArgs<string>(true, string.Empty));
//                                                                  }
//                                                              }
//                                                          });
//        };
//    }
//
//    public event ShareCompletedHandler<string> ShareCallBack;
//
//    #endregion
//}
