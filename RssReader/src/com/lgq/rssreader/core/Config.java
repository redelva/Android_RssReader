package com.lgq.rssreader.core;

public class Config {
	public static final String LOCATION_PREFIX = "/Android/data/com.lgq.rssreader";
	public static final String IMAGES_LOCATION = "/rssreader/images/";
	public static final String PROFILE_PICTURE_LOCATION = "/rssreader/profile/";
	public static final String IMAGES_PREFIX = "../images/";
	public static final String HTML_LOCATION = "/rssreader/html/";
	public static final String ROOT_LOCATION = "/rssreader/";
	public static final String ERRORLOG_LOCATION = "/rssreader/error/";
	public static final String LOG_LOCATION = "/rssreader/log/";
	public static final String FONTS_LOCATION = "/rssreader/fonts/";
	public static final String HTML_NAME = "sample.1.6.html";
	
	public static final String DB_BLOG_TABLE = "Blogs";
	public static final String DB_BLOG_VIEW = "vBlogs";
	public static final String DB_IMAGE_TABLE = "ImageRecords";
	public static final String DB_SYNCSTATE_TABLE = "SyncStates";
	public static final String DB_FILE_NAME="rssreader_db";
	public static final String APP_PACKAGE_NAME="com.lgq.rssreader";
	
	public static final String LOGIN_URL = "http://feedly.com/v3/auth/auth?client_id=feedly&redirect_uri=http%3A%2F%2Ffeedly.com%2Ffeedly.html&scope=https%3A%2F%2Fcloud.feedly.com%2Fsubscriptions&response_type=code&migrate=false&ck=1412952055218&ct=feedly.desktop&windowsLiveOAuthActive=true&facebookOAuthActive=true&twitterOAuthActive=true&cv=24.0.861&mode=login";
	//public static final String LOGIN_URL = "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=534890559860-r6gn7e3agcpiriehe63dkeus0tpl5i4i.apps.googleusercontent.com&redirect_uri=https%3A%2F%2Fcloud.feedly.com%2Fv3%2Fauth%2Fcallback&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email&state=AQAAz0N7ImkiOiJmZWVkbHkiLCJwIjoiR29vZ2xlIiwiciI6Imh0dHA6Ly9jbG91ZC5mZWVkbHkuY29tL2ZlZWRseS5odG1sIn0&approval_prompt=force";
	//public static String Auth = "AQAAPB57ImEiOiJmZWVkbHkiLCJ0IjoxLCJpIjoiZDFlZjM5MzgtYTU0Yi00NDA0LTlkOWQtZjk3ODQyMTI0MjgxIiwicCI6MSwidiI6InByb2R1Y3Rpb24iLCJ4Ijoic3RhbmRhcmQiLCJlIjoxMzgxNjcwMzM3MDAyfQ";//Feedly auth token
	//public static String Sid = "d1ef3938-a54b-4404-9d9d-f97842124281";//Feedly user id
	public static final String SETTING_INFOS = "Settings";
}
