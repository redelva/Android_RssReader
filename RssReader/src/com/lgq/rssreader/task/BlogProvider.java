package com.lgq.rssreader.task;

import com.lgq.rssreader.dal.BlogDalHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class BlogProvider extends ContentProvider {

	private static final UriMatcher matcher;  
    private BlogDalHelper helper;      
      
    private static final String AUTHORITY = "com.lgq.rssreader.BlogProvider";  
    private static final int PERSON_ALL = 0;  
    private static final int PERSON_ONE = 1;  
      
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.scott.person";  
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.scott.person";  
      
    //数据改变后立即重新查询  
    private static final Uri NOTIFY_URI = Uri.parse("content://" + AUTHORITY + "/persons");  
      
    static {  
        matcher = new UriMatcher(UriMatcher.NO_MATCH);  
          
        matcher.addURI(AUTHORITY, "persons", PERSON_ALL);   //匹配记录集合  
        matcher.addURI(AUTHORITY, "persons/#", PERSON_ONE); //匹配单条记录  
    }
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
