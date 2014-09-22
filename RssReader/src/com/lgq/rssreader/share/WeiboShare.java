//package com.lgq.rssreader.share;
//
//public class WeiboShare {
//	private string refreshToken;
//    #region IBlogShare 成员
//
//    public void  BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//    {
//        SdkData.AppKey = Constants.WeiboAppKey;
//        SdkData.AppSecret = Constants.WeiboAppSecret;
//        SdkData.RedirectUri = "http://weibo.com";
//        //SdkNetEngine.AuthOption = EumAuth.OAUTH2_0;
//
//        RssReader.Common.Helper.BuildLoginPage(page, AccountType.WeiBo);
//
//        //Common.Helper.weiboControl.OAuthBack += (errCode, response) =>
//        AuthenticationView.OAuth2VerifyCompleted += (isSuccess,errCode, response) =>
//        {
//            //if (errCode == SdkErrCode.SUCCESS)
//            if (isSuccess)
//            {
//                //Common.Helper.RemoveBrowser();
//                //解析返回的Json数据
//                //JObject JosnData = JObject.Parse(response.);
//                //JToken node1 = JosnData["access_token"];                    
//                //if (null != node1 && LoginCallBack != null)
//                if (LoginCallBack != null)
//                {
//                    Deployment.Current.Dispatcher.BeginInvoke(() =>
//                    {
//                        Common.Helper.RemoveBrowser();
//                        LoginCallBack(this, new ShareEventArgs<string>(true, response.accesssToken.ToString()));
//                    });
//                }
//            }
//            else if (errCode.errCode == SdkErrCode.NET_UNUSUAL)
//            {
//                if (LoginCallBack != null)
//                {
//                    Deployment.Current.Dispatcher.BeginInvoke(() =>
//                                                                  {
//                                                                      Common.Helper.RemoveBrowser();
//                                                                      LoginCallBack(this, new ShareEventArgs<string>(new Exception(Resources.StringResources.FailedToLoginWeibo)));
//                                                                  });
//                }
//            }
//            else
//            {
//                if (LoginCallBack != null)
//                {
//                    LoginCallBack(this, new ShareEventArgs<string>(false, Resources.StringResources.OtherError));
//                }
//            }
//        };
//    }
//
//    public event LoginCompletedHandler<string> LoginCallBack;
//
//    public void  BeginShare(Blog blog, string accessToken)
//    {
//        SdkShare sdkShare = new SdkShare();
//        //{
//        //    IsPicStatus = false,// If this value is set to false,the sdk will ignore PicturePath value                                
//        //    IsShowChoosePhotoButton = true// Defalt hide applicationbar choose photo button, you can opt them visible
//        //};
//
//        //设置OAuth2.0的access_token
//        sdkShare.AccessToken = accessToken;
//        sdkShare.Message = blog.Title;
//        sdkShare.Completed += (o, args) =>
//        {
//            Deployment.Current.Dispatcher.BeginInvoke(() =>
//                                                          {
//                                                              ToastPrompt toast = new ToastPrompt();
//                                                              if (args.IsSendSuccess)
//                                                              {
//                                                                  if(ShareCallBack != null)
//                                                                      ShareCallBack(this, new ShareEventArgs<string>(true, Resources.StringResources.SucessfullySentToWeibo));
//                                                              }
//                                                              else
//                                                              {
//                                                                  if(args.Response == "AccessToken is null.")
//                                                                  {                                                                          
//                                                                      App.WeiboAccessToken = string.Empty;
//                                                                      if (ShareCallBack != null)
//                                                                          ShareCallBack(this, new ShareEventArgs<string>(false, Resources.StringResources.PleaseRelogin + " " + Resources.StringResources.SinaWeibo));
//                                                                  }
//                                                                  else
//                                                                  {
//                                                                      if (ShareCallBack != null)
//                                                                          ShareCallBack(this, new ShareEventArgs<string>(false, args.Response));   
//                                                                  }                                                                      
//                                                              }                                                                  
//                                                          });
//        };
//
//        // Active the Share page to be shown.
//        sdkShare.Show();
//    }
//
//    public event ShareCompletedHandler<string> ShareCallBack;
//
//    #endregion
//}
