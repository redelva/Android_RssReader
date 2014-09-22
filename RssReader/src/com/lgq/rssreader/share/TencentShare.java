//package com.lgq.rssreader.share;
//
//public class TencentShare {
//	 #region IBlogShare ³ÉÔ±
//
//     public void BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//     {
//         TencentWeiboSDK.OAuthConfigruation.APP_KEY = apiKey;
//         TencentWeiboSDK.OAuthConfigruation.APP_SECRET = secretKey;
//         RssReader.Common.Helper.BuildLoginPage(page, AccountType.Tencent);
//
//         Common.Helper.tencentControl.OAuthLogin(callback =>
//         {
//             if (callback.Succeed)
//             {                    
//                 if (null != callback.Data && LoginCallBack != null)
//                 {
//                     Deployment.Current.Dispatcher.BeginInvoke(() =>
//                     {
//                         Common.Helper.RemoveBrowser();
//                         LoginCallBack(this, new ShareEventArgs<AccessToken>(true, callback.Data));
//                     });
//                 }
//             }                
//             else
//             {
//                 if (LoginCallBack != null)
//                 {
//                     LoginCallBack(this, new ShareEventArgs<AccessToken>(callback.InnerException));
//                 }
//             }
//         });
//     }
//
//     public event LoginCompletedHandler<AccessToken> LoginCallBack;
//
//     public void BeginShare(Blog blog, AccessToken accessToken)
//     {
//         TencentWeiboSDK.Services.TService service = new TService(accessToken);
//         ServiceArgument argument = new ServiceArgument();
//         argument.Content = blog.Title;
//         argument.ContentType = ContentType.Text;
//         argument.Format = DataFormat.Json;            
//         service.Add(argument,callback =>
//         {
//             Deployment.Current.Dispatcher.BeginInvoke(() =>
//             {
//                 ToastPrompt toast = new ToastPrompt();
//                 if (callback.Succeed)
//                 {
//                     if (ShareCallBack != null)
//                         ShareCallBack(this, new ShareEventArgs<AccessToken>(true, accessToken));
//                 }
//                 else
//                 {                        
//                     if (ShareCallBack != null)
//                         ShareCallBack(this, new ShareEventArgs<AccessToken>(callback.InnerException));
//                     
//                 }
//             });
//         });
//     }
//
//     public event ShareCompletedHandler<AccessToken> ShareCallBack;
//
//     #endregion
//}
