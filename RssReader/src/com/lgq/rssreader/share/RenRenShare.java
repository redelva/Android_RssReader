//package com.lgq.rssreader.share;
//
//public class RenRenShare {
//	 #region IBlogShare 成员
//
//     private string _secretKey;
//
//     public void BeginLogin(PhoneApplicationPage page, string apiKey, string secretKey)
//     {
//         _secretKey = secretKey;
//	        RenRenAPI api = new RenRenAPI(apiKey, secretKey);
//         Helper.BuildLoginPage(page,AccountType.RenRen);
//         List<string> scope = new List<string> { "publish_blog" };
//         api.Login(page, scope, (sender, eventArgs) =>
//                                     {
//                                         if(eventArgs.Result)
//                                         {
//                                             RenRenSDKData data = (RenRenSDKData) sender;
//                                             Deployment.Current.Dispatcher.BeginInvoke(() =>
//                                             {
//                                                 Common.Helper.RemoveBrowser();
//                                                 if (LoginCallBack != null)
//                                                 {
//                                                     LoginCallBack(this, new ShareEventArgs<string>(true, HttpUtility.UrlDecode(data.AccessToken)));
//                                                 }   
//                                             });                                                 
//                                         }
//                                         else
//                                         {
//                                             if (LoginCallBack != null)
//                                             {
//                                                 LoginCallBack(this, new ShareEventArgs<string>(false, eventArgs.Data));
//                                             } 
//                                         }
//                                     });
//     }
//
//     public event LoginCompletedHandler<string> LoginCallBack;
//
//     private string SignRenRen(IOrderedEnumerable<KeyValuePair<string, string>> param, string secretKey)
//     {
//         StringBuilder sb = new StringBuilder();
//         foreach (KeyValuePair<string, string> pair in param)
//         {
//             sb.Append(pair.Key + "=" + pair.Value);
//         }
//         sb.Append(secretKey);
//
//         return MD5.GetMd5String(sb.ToString());
//     }
//
//     public void  BeginShare(Blog blog, string accessToken)
//     {
//	        IDictionary<string, string> param = new Dictionary<string, string>();
//
//         //Required	Name	Type	Description
//         //required	sig	string	签名认证。是用当次请求的所有参数计算出来的值。点击此处查看详细算法
//         //method	string	blog.addBlog
//         //v	string	API的版本号，固定值为1.0
//         //title	string	日志的标题
//         //content	string	日志的内容
//         //alternative	api_key	string	申请应用时分配的api_key，调用接口时候代表应用的唯一身份
//         //session_key	string	当前用户的session_key
//         //access_token	string	OAuth2.0验证授权后获得的token。当传入此参数时，api_key和session_key可以不用传入。
//         //optional	format	string	返回值的格式。请指定为JSON或者XML，推荐使用JSON，缺省值为XML
//         //visable	int	日志的隐私设置，可用值有99(所有人可见)1(仅好友可见)4(需要密码)-1(仅自己可见),错传或没传,默认为99
//         //password	string	用户设置的密码
//
//         param.Add("method", "blog.addBlog");
//         param.Add("v","1.0");
//         param.Add("title", blog.Title);
//         param.Add("content", string.IsNullOrEmpty(blog.Content) ? blog.Description : blog.Content);
//         param.Add("url", blog.Link);            
//         param.Add("format", "JSON");
//         param.Add("access_token", accessToken);            
//
//         param.Add("sig", SignRenRen(param.OrderBy(p => p.Key),_secretKey));
//
//         WebClient client = new WebClient();
//
//         StringBuilder sb = new StringBuilder();
//
//         foreach (KeyValuePair<string, string> pair in param)
//         {
//             sb.Append(pair.Key + "=" + HttpUtility.UrlEncode(pair.Value) + "&");
//         }
//
//         sb.Remove(sb.Length - 1, 1);
//
//         client.Headers[HttpRequestHeader.ContentType] = "application/x-www-form-urlencoded; charset=UTF-8";
//         client.UploadStringAsync(new Uri("http://api.renren.com/restserver.do"), "POST", sb.ToString());            
//
//         client.UploadStringCompleted += (sender, args) =>
//         {
//             if(args.Error == null)
//             {
//                 JObject obj = JObject.Parse(args.Result);
//
//                 if (obj["id"] == null || obj["id"].Value<int>() == 0)
//                 {
//                     if(ShareCallBack != null)
//                     {   
//                         if(obj["error_code"].Value<int>() == 2002) //accesstoken过期                                                                
//                             ShareCallBack(this, new ShareEventArgs<string>(false, Resources.StringResources.ReloginRenRen));                                                                
//                         else
//                             ShareCallBack(this, new ShareEventArgs<string>(false, obj["error_msg"].Value<string>()));
//                     }
//                 }
//                 else
//                 {
//                     if(ShareCallBack != null)
//                     {
//                         ShareCallBack(this, new ShareEventArgs<string>(true, string.Empty));
//                     }
//                 }
//             }
//             else
//             {
//                 if (LoginCallBack != null)
//                 {
//                     LoginCallBack(this, new ShareEventArgs<string>(false, args.Error.Message));
//                 }
//             }
//         };
//     }
//
//     public event ShareCompletedHandler<string> ShareCallBack;
//
//     #endregion
//}
