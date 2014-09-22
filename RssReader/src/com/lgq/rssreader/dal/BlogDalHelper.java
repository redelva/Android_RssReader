package com.lgq.rssreader.dal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class BlogDalHelper {
	private DBHelper.DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	public final static byte[] _writeLock = new byte[0];
	Context context;
	public BlogDalHelper() {
		dbHelper = new DBHelper.DatabaseHelper(ReaderApp.getAppContext());
		db = dbHelper.getWritableDatabase();
	}
	public void Close(){
		dbHelper.close();
	}
	/**
	 * 判断blogId是否存在
	 * 
	 * @param blogId
	 * @return
	 */
	private boolean Exist(String blogId) {
		String where = "BlogId=?";
		String[] args = {String.valueOf(blogId)};
		Cursor cursor = db.query(Config.DB_BLOG_TABLE, null, where, args, null,null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();
		return isExist;
	}
	
	public List<Blog> GetBlogList(Date until) {		
		String where = "datetime(PubDate) <?";
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000");
		String[] args = { dateFm.format(until) };//datetime(LogTime)=datetime('2011-08-18 16:38:32.000') 			
		return GetBlogListByWhere(null, where, args );
	}
	
	public List<Blog> GetBlogList(double percentage) {
		
		String sql = "select count(1) from imagerecords";
		Cursor cursor = db.rawQuery(sql, null);
		int count = 0;
		while (cursor != null && cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		
		String limit = "limit " + (int)(count * percentage);		 		
		return GetBlogListByWhere(limit, null, null);
	}
	
	/**
	 * 根据Channel获取对应Blogs
	 * 
	 * @return
	 */
	public List<Blog> GetBlogList(Channel c, int pageIndex, int pageSize, boolean allItems) {
		String limit = String.valueOf((pageIndex - 1) * pageSize) + "," + String.valueOf(pageSize);
		String where = "";		
		String[] args = {c.Id};
		if(!c.IsDirectory){
			where = "ChannelId=?";			
		}else{
			where = "TagId=?";
		}
		
		if(!allItems)
			where = where + " and IsRead = 0";
		
		return GetBlogListByWhere(limit, where, args );
	}
	
	public int FindBlogIndex(RssTab from, String keyword, Channel channel, Blog current, boolean previous) {
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql = "select count(1) from blogs where 1=1 ";
		
			sql = sql + " and pubdate>='" + dateFm.format(current.PubDate) + "' and timestamp>=" + current.TimeStamp + " and blogid>'" + current.BlogId + "'";		
		
		if(from == null){
			if(!channel.IsDirectory)
				sql = sql + " AND channelid='" + channel.Id + "'";
			else
				sql = sql + " AND tagid='" + channel.Id + "'";
		}else if(from == RssTab.Search){
			sql = sql + " AND Description LIKE '%" + keyword + "%' or Content LIKE '%" + keyword + "%' or Title LIKE '%" + keyword + "%'";
		}else{
    		switch(from){
    			case All:    				
					break;
    			case Recommend:
					sql = sql + " AND IsRecommend = 1";		    					
					break;
    			case Star:
					sql = sql + " AND IsStarred = 1";		    					
					break;
    			case Unread:
					sql = sql + " AND IsRead = 0";		    					 				
					break;
    		}
    	}
    	
		String orderby = "PUBDATE DESC, TIMESTAMP DESC, BLOGID DESC";
		
		sql = sql + " order by " + orderby;
		
		Log.i("RssReader", sql);
		
    	SQLiteStatement s = db.compileStatement(sql);

  		long count = s.simpleQueryForLong();
  		
  		return (int) count;
	}
	
	/**
	 * 根据条件查找上下条
	 * 
	 * @return
	 */
	public Blog FindBlogBy(RssTab from, String keyword, Channel channel, Blog current, boolean previous) {
//		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		
//		int index = FindBlogIndex(from, keyword, channel, current, previous);
//		
//		String sql = "select * from Blogs ";
//		
//		String where = "";
//		String offset = "";
//		if(previous){		
//			where = "pubdate>'" + dateFm.format(current.PubDate) + "' or " +
//					"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp>" + current.TimeStamp + ") or " +
//					"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid>'" + current.BlogId + "')";
//			offset= String.valueOf(index - 1);
//		}
//		else{			
//			where = "pubdate<'" + dateFm.format(current.PubDate) + "' or " +
//					"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp<" + current.TimeStamp + ") or " +
//					"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid<'" + current.BlogId + "')";
//			offset= "0";
//		}
//		String limit = "1";		 
//		
//		if(from == null){
//			if(!channel.IsDirectory){
//				if(previous)
//					where = "(pubdate>'" + dateFm.format(current.PubDate) + "' or " +
//							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp>" + current.TimeStamp + ") or " +
//							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid>'" + current.BlogId + "')) and ChannelId='" + current.ChannelId + "'";
//				else
//					where = "(pubdate<'" + dateFm.format(current.PubDate) + "' or " +
//							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp<" + current.TimeStamp + ") or " +
//							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid<'" + current.BlogId + "')) and ChannelId='" + current.ChannelId + "'";
//			}else{
//				if(previous)
//					where = "(pubdate>'" + dateFm.format(current.PubDate) + "' or " +
//							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp>" + current.TimeStamp + ") or " +
//							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid>'" + current.BlogId + "')) and TagId='" + current.TagId + "'";
//				else
//					where = "(pubdate<'" + dateFm.format(current.PubDate) + "' or " +
//							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp<" + current.TimeStamp + ") or " +
//							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid<'" + current.BlogId + "')) and TagId='" + current.TagId + "'";
//			}			
//    	}
//    	else if(from == RssTab.Search){    		
//    		if(previous)
//				where = "(pubdate>'" + dateFm.format(current.PubDate) + "' or " +
//						"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp>" + current.TimeStamp + ") or " +
//						"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid>'" + current.BlogId + "')) AND " +
//						"Description LIKE ? or Content LIKE ? or Title LIKE ?";
//			else
//				where = "(pubdate<'" + dateFm.format(current.PubDate) + "' or " +
//						"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp<" + current.TimeStamp + ") or " +
//						"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid<'" + current.BlogId + "')) AND " +
//						"Description LIKE ? or Content LIKE ? or Title LIKE ?";
//    	}
//    	else{
//    		switch(from){
//    			case All:
//    				if(previous)
//    					where = "pubdate>'" + dateFm.format(current.PubDate) + "' or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp>" + current.TimeStamp + ") or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid>'" + current.BlogId + "')";
//    				else
//    					where = "pubdate<'" + dateFm.format(current.PubDate) + "' or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp<" + current.TimeStamp + ") or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid<'" + current.BlogId + "')";    				
//    				break;
//    			case Recommend:
//    				if(previous)
//    					where = "(pubdate>'" + dateFm.format(current.PubDate) + "' or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp>" + current.TimeStamp + ") or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid>'" + current.BlogId + "')) AND IsRecommend = 1";
//    				else
//    					where = "(pubdate<'" + dateFm.format(current.PubDate) + "' or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp<" + current.TimeStamp + ") or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid<'" + current.BlogId + "')) AND IsRecommend = 1";    				
//    				break;
//    			case Star:
//    				if(previous)
//    					where = "(pubdate>'" + dateFm.format(current.PubDate) + "' or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp>" + current.TimeStamp + ") or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid>'" + current.BlogId + "')) AND IsStarred = 1";
//    				else
//    					where = "(pubdate<'" + dateFm.format(current.PubDate) + "' or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp<" + current.TimeStamp + ") or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid<'" + current.BlogId + "')) AND IsStarred = 1";    				
//    				break;
//    			case Unread:
//    				if(previous)
//    					where = "(pubdate>'" + dateFm.format(current.PubDate) + "' or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp>" + current.TimeStamp + ") or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid>'" + current.BlogId + "')) AND IsRead = 0";
//    				else
//    					where = "(pubdate<'" + dateFm.format(current.PubDate) + "' or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp<" + current.TimeStamp + ") or " +
//    							"(pubdate = '" + dateFm.format(current.PubDate) + "' and timestamp=" + current.TimeStamp + " and blogid<'" + current.BlogId + "')) AND IsRead = 0";    				
//    				break;
//    		}
//    	}		
//		
//		String orderby = "PUBDATE DESC, TIMESTAMP DESC, BLOGID DESC";
		
		SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String sql = "select * from Blogs ";
		
		String where = "";
		String orderby = "";
		if(previous){		
			//where = "blogid>'" + current.BlogId + "'";
			//orderby = "BLOGID ASC, PUBDATE ASC, TIMESTAMP ASC";
			where = "blogid >'" + current.BlogId + "' and timestamp>=" + current.TimeStamp;
			orderby = "TIMESTAMP ASC, PUBDATE ASC, BLOGID ASC";
		}
		else{			
			//where = "blogid<'" + current.BlogId + "'";
			//orderby = "BLOGID DESC, PUBDATE DESC, TIMESTAMP DESC";
			where = "blogid <'" + current.BlogId + "' and timestamp<=" + current.TimeStamp;
			orderby = "TIMESTAMP DESC, PUBDATE DESC, BLOGID DESC";
		}
		String limit = "1";
		
		if(from == null){
			if(!channel.IsDirectory){
				where = where + " and ChannelId='" + current.ChannelId + "'";
			}else{
				where = where + " and TagId='" + current.TagId + "'";
			}			
    	}
    	else if(from == RssTab.Search){
    		where = where + " AND Description LIKE '" + keyword + "' or Content LIKE '" + keyword + "' or Title LIKE '" + keyword + "'";
    	}
    	else{
    		
    		if(previous){
				where = "pubdate>'" + dateFm.format(current.PubDate) + "'";
				orderby = "timestamp ASC, pubdate ASC ";
			}
			else{			
				where = "pubdate<'" + dateFm.format(current.PubDate) + "'";
				orderby = "timestamp DESC, pubdate DESC";
			}
    		
    		switch(from){
    			case All:    				
    				break;
    			case Recommend:    				
    				where = where + " AND IsRecommend = 1";    				
    				break;
    			case Star:    				
    				where = where + " AND IsStarred = 1";    				
    				break;
    			case Unread:    				
    				where = where + " AND IsRead = 0";
    				break;
    		}
    	}
				
		List<Blog> list = new ArrayList<Blog>();
		
		Log.i("RssReader", sql + " where " + where + " order by " + orderby + " limit 1 ");
				
		//Cursor cursor = db.rawQuery("select * from Blogs where " + where + " order by " + orderby + " limit 1 offset " + offset, args);
		Cursor cursor = db.rawQuery(sql + " where " + where + " order by " + orderby + " limit 1 ", null);
		while (cursor != null && cursor.moveToNext()) {
			Blog entity = new Blog();
			entity.TagId = cursor.getString(cursor.getColumnIndex("TagId"));
			entity.ChannelId = cursor.getString(cursor.getColumnIndex("ChannelId"));
			entity.BlogId = cursor.getString(cursor.getColumnIndex("BlogId"));
			entity.Title = cursor.getString(cursor.getColumnIndex("Title"));
			entity.Description = cursor.getString(cursor.getColumnIndex("Description"));
			entity.Content = cursor.getString(cursor.getColumnIndex("Content"));
			entity.Link = cursor.getString(cursor.getColumnIndex("Link"));
			entity.PubDate = DateHelper.ParseDate(cursor.getString(cursor.getColumnIndex("PubDate")));
			entity.SubsTitle = cursor.getString(cursor.getColumnIndex("SubsTitle"));
			entity.TimeStamp = cursor.getLong(cursor.getColumnIndex("TimeStamp"));
			entity.IsRead = cursor.getString(cursor.getColumnIndex("IsRead")).equals("1");
			entity.IsStarred = cursor.getString(cursor.getColumnIndex("IsStarred")).equals("1");;
			entity.IsRecommend = cursor.getString(cursor.getColumnIndex("IsRecommend")).equals("1");;
			entity.OriginId = cursor.getString(cursor.getColumnIndex("TagId"));
			entity.Avatar = cursor.getString(cursor.getColumnIndex("TagId"));			
			list.add(entity);
		}
		cursor.close();
		
		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
	
	/**
	 * 根据key word获取对应Blogs
	 * 
	 * @return
	 */
	public List<Blog> GetBlogListByKeyword(String keyword, int pageIndex, int pageSize) {
		String limit = String.valueOf((pageIndex - 1) * pageSize) + "," + String.valueOf(pageSize);
		String where = "";
		String[] args = {"%" + keyword + "%"};
		where = "Description LIKE ? or Content LIKE ? or Title LIKE ?";
		return GetBlogListByWhere(limit, where, args );
	}
	
	/**
	 * 根据Tab获取对应Blogs
	 * 
	 * @return
	 */
	public List<Blog> GetBlogList(RssTab t, int pageIndex, int pageSize) {
		String limit = String.valueOf((pageIndex - 1) * pageSize) + "," + String.valueOf(pageSize);
		String where = "";
		String[] args = new String[1];
		
		List<Blog> blogs = GetTopBlogList();
		
		switch(t){
			case All:
				blogs = GetBlogListByPage(pageIndex, pageSize);
				break;
			case Recommend:
				where = "IsRecommend = ?";
				args[0] = "1";
				blogs = GetBlogListByWhere(limit, where, args);
				break;
			case Star:
				where = "IsStarred = ?";
				args[0] = "1";
				blogs = GetBlogListByWhere(limit, where, args);
				break;
			case Unread:
				where = "IsRead = ?";
				args[0] = "0";
				blogs = GetBlogListByWhere(limit, where, args);
				break;
		}
		
		return blogs;
	}
	/**
	 * 获取前30条记录
	 * 
	 * @return
	 */
	public List<Blog> GetTopBlogList() {
		String limit = "30";
		String where = "";

		return GetBlogListByWhere(limit, where, null);
	}
	/**
	 * 分页获取
	 */
	public List<Blog> GetBlogListByPage(int pageIndex, int pageSize) {
		String limit = String.valueOf((pageIndex - 1) * pageSize) + "," + String.valueOf(pageSize);
		List<Blog> list = GetBlogListByWhere(limit, null, null);

		return list;
	}

	/**
	 * 根据BlogId获取单条记录
	 */
	public Blog GetBlogEntity(String blogId) {
		String limit = "1";
		String where = "BlogId=?";
		String[] args = {String.valueOf(blogId)};
		List<Blog> list = GetBlogListByWhere(limit, where, args);
		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
	/**
	 * 根据where条件获取记录
	 * 
	 * @param top
	 * @param where
	 */
	public List<Blog> GetBlogListByWhere(String limit, String where, String[] args) {
		List<Blog> listBlog = new ArrayList<Blog>();
		//String orderBy = "PUBDATE DESC, TIMESTAMP DESC, BLOGID DESC";
		String orderBy = "TIMESTAMP DESC, PUBDATE DESC, BLOGID DESC";
		Cursor cursor = db.query(Config.DB_BLOG_TABLE, null, where, args, null, null, orderBy, limit);
		while (cursor != null && cursor.moveToNext()) {
			Blog entity = new Blog();			
			entity.TagId = cursor.getString(cursor.getColumnIndex("TagId"));
			entity.ChannelId = cursor.getString(cursor.getColumnIndex("ChannelId"));
			entity.BlogId = cursor.getString(cursor.getColumnIndex("BlogId"));
			entity.Title = cursor.getString(cursor.getColumnIndex("Title"));
			entity.Description = cursor.getString(cursor.getColumnIndex("Description"));
			entity.Content = cursor.getString(cursor.getColumnIndex("Content"));
			entity.Link = cursor.getString(cursor.getColumnIndex("Link"));
			entity.PubDate = DateHelper.ParseDate(cursor.getString(cursor.getColumnIndex("PubDate")));
			entity.SubsTitle = cursor.getString(cursor.getColumnIndex("SubsTitle"));
			entity.TimeStamp = cursor.getLong(cursor.getColumnIndex("TimeStamp"));
			entity.IsRead = cursor.getString(cursor.getColumnIndex("IsRead")).equals("1");
			entity.IsStarred = cursor.getString(cursor.getColumnIndex("IsStarred")).equals("1");;
			entity.IsRecommend = cursor.getString(cursor.getColumnIndex("IsRecommend")).equals("1");;
			entity.OriginId = cursor.getString(cursor.getColumnIndex("TagId"));
			entity.Avatar = cursor.getString(cursor.getColumnIndex("TagId"));			
			listBlog.add(entity);
		}
		cursor.close();

		return listBlog;
	}
	/**
	 * 根据blogId获取是否收藏
	 * 
	 * @param blogId
	 * @return
	 */
	public boolean GetIsStarred(String blogId) {
		Blog entity = GetBlogEntity(blogId);
		if (entity != null) {
			return entity.IsStarred;
		}
		return false;
	}
	/**
	 * 设置blogId为收藏
	 * 
	 * @param blogId
	 */
	public void MarkAsStar(String blogId, boolean b) {
		String sql = "";
		if(b)
			sql = "update Blogs set IsStarred=1 where BlogId=?";
		else
			sql = "update Blogs set IsStarred=0 where BlogId=?";
		String[] args = {String.valueOf(blogId)};
		db.execSQL(sql, args);
	}
	/**
	 * 根据blogId获取是否已读
	 * 
	 * @param blogId
	 * @return
	 */
	public boolean GetIsRead(String blogId) {
		Blog entity = GetBlogEntity(blogId);
		if (entity != null) {
			return entity.IsRead;
		}
		return false;
	}
	/**
	 * 设置blogId为已读
	 * 
	 * @param blogId
	 */
	public void MarkAsRead(String blogId, boolean b) {
		String sql = "";
		if(b)
			sql = "update Blogs set IsRead=1 where BlogId=?";
		else
			sql = "update Blogs set IsRead=0 where BlogId=?";
		String[] args = {String.valueOf(blogId)};
		db.execSQL(sql, args);
	}
	
	/**
	 * 设置blogId为已读
	 * 
	 * @param blogId
	 */
	public void MarkAsRead(List<String> blogIds, boolean b) {
//		String sql = "";
//		if(b)
//			sql = "update Blogs set IsRead=1 where BlogId in (";
//		else
//			sql = "update Blogs set IsRead=0 where BlogId in (";
//		
//		StringBuilder sb = new StringBuilder();
//		
//		for(String id : blogIds){
//			sb.append("\"" + id + "\",");
//		}
//		
//		if(sb.length() > 0)
//			sb.deleteCharAt(sb.length() - 1);
//		
//		sql = sql + sb.toString() + ")";
//				
//		dbHelper.getWritableDatabase().execSQL(sql, null);
		
		synchronized (_writeLock) {
			db.beginTransaction();
			try {				
				for (int i = blogIds.size() - 1, len = 0; i >= len; i--) {
					String blogId = blogIds.get(i);
					ContentValues contentValues = new ContentValues();			
					contentValues.put("IsRead", b ? 1 : 0);
					db.update(Config.DB_BLOG_TABLE, contentValues, "BlogId=?", new String[]{blogId});
				}
				db.setTransactionSuccessful();
			} 
			catch(Exception e){
				e.printStackTrace();
			}finally {
				db.endTransaction();
			}
		}
	}
	
	/**
	 * 设置blogId为已读
	 * 
	 * @param blogId
	 */
	public void MarkAsRead(Channel c, boolean b) {
		String sql = "";
		
		if(c.IsDirectory){
			if(b)
				sql = "update Blogs set IsRead=1 where TagId = ?";
			else
				sql = "update Blogs set IsRead=0 where TagId = ?";
		}else{
			if(b)
				sql = "update Blogs set IsRead=1 where ChannelId = ?";
			else
				sql = "update Blogs set IsRead=0 where ChannelId = ?";
		}
		
		String[] args = {c.Id};
		db.execSQL(sql, args);
	}
	
	/**
	 * 同步正文
	 * 
	 * @param blogId
	 * @param blogContent
	 */
	public void SynchronyContent2DB(String blogId, String blogContent) {
		if (blogContent == null || blogContent.equals("")) {
			return;
		}
		String sql = "update Blogs set Content=? where BlogId=?";
		String[] args = {blogContent, blogId};
		db.execSQL(sql, args);
	}
	
	/**
	 * 删除Channel
	 * 
	 * @param blogId
	 * @param blogContent
	 */
	public void DeleteBlogByChannel(Channel c) {
		String sql = "";
		String[] args = new String[1];
		if(c.IsDirectory){		
			sql = "delete from Blogs where TagId=?";
			args[0] = c.Id;
		}else{
			sql = "delete from Blogs where ChannelId=?";
			args[0] = c.Id;
		}
		db.execSQL(sql, args);
	}
	
	/**
	 * 删除Blogs
	 * 
	 * @param blogs
	 */
	public void DeleteBlog(ArrayList<Blog> blogs) {
//		String sql = "delete from Blogs where BlogId in (";
//		String[] args = new String[1];
//		StringBuilder sb = new StringBuilder();
//		for(Blog b : blogs){
//			sb.append("\"" + b.BlogId + "\",");
//		}
//		if(blogs.size() > 0)
//			sb.deleteCharAt(sb.length() - 1);
//				
//		sql = sql + sb.toString() + ")";
//		db.execSQL(sql, null);
		
		synchronized (_writeLock) {
			db.beginTransaction();
			try {				
				for (int i = blogs.size() - 1, len = 0; i >= len; i--) {
					String blogId = blogs.get(i).BlogId;					
					db.delete(Config.DB_BLOG_TABLE, "BlogId=?", new String[]{blogId});
				}
				db.setTransactionSuccessful();
			} 
			catch(Exception e){
				e.printStackTrace();
			}finally {
				db.endTransaction();
			}
		}
	}
	
	/**
	 * 删除Blogs
	 * 
	 * @param blogs
	 */
	public void DeleteBlog(List<String> blogIDs) {		
		synchronized (_writeLock) {
			db.beginTransaction();
			try {				
				for (int i = blogIDs.size() - 1, len = 0; i >= len; i--) {
					String blogId = blogIDs.get(i);					
					db.delete(Config.DB_BLOG_TABLE, "BlogId=?", new String[]{blogId});
				}
				db.setTransactionSuccessful();
			} 
			catch(Exception e){
				e.printStackTrace();
			}finally {
				db.endTransaction();
			}
		}
	}
	
	/**
	 * 同步摘要
	 * 
	 * @param blogId
	 * @param blogContent
	 */
	public void SynchronyDescription2DB(String blogId, String description) {
		if (description== null || description.equals("")) {
			return;
		}
		String sql = "update Blogs set Description=? where BlogId=?";
		String[] args = {description, blogId};
		db.execSQL(sql, args);
	}
	
	/**
	 * 同步数据
	 * 
	 * @param list
	 */
	public void SynchronyData2DB(List<Blog> blogs) {
		List<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = blogs.size() - 1, len = 0; i >= len; i--) {
			ContentValues contentValues = new ContentValues();			
			contentValues.put("BlogId", blogs.get(i).BlogId);
			contentValues.put("TagId", blogs.get(i).TagId);
			contentValues.put("ChannelId", blogs.get(i).ChannelId);
			contentValues.put("Title", blogs.get(i).Title);
			contentValues.put("Description", blogs.get(i).Description);
			contentValues.put("Content", blogs.get(i).Content);
			contentValues.put("Link", blogs.get(i).Link);
			contentValues.put("PubDate", DateHelper.ParseDateToString(blogs.get(i).PubDate));
			contentValues.put("SubsTitle", blogs.get(i).SubsTitle);
			contentValues.put("TimeStamp", blogs.get(i).TimeStamp);
			contentValues.put("IsRead", blogs.get(i).IsRead);
			contentValues.put("IsStarred", blogs.get(i).IsStarred);
			contentValues.put("IsRecommend", blogs.get(i).IsRecommend);
			contentValues.put("OriginId", blogs.get(i).OriginId);
			contentValues.put("Avatar", blogs.get(i).Avatar);			

			list.add(contentValues);
		}
		synchronized (_writeLock) {
			db.beginTransaction();
			try {				
				for (int i = blogs.size() - 1, len = 0; i >= len; i--) {
					String blogId=list.get(i).getAsString("BlogId");
					boolean isExist = Exist(blogId);
					
					if (!isExist) {
						db.insert(Config.DB_BLOG_TABLE, null, list.get(i));
					}else{
						db.update(Config.DB_BLOG_TABLE, list.get(i),"BlogId=?", new String[]{blogId});
					}
				}
				db.setTransactionSuccessful();
			} 
			catch(Exception e){
				e.printStackTrace();
			}finally {
				db.endTransaction();
			}
		}
	}
	

}