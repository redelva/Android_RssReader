package com.lgq.rssreader.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.entity.SyncState;
import com.lgq.rssreader.enums.RssAction;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.enums.SyncType;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SyncStateDalHelper {
	private DBHelper.DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	public final static byte[] _writeLock = new byte[0];
	Context context;
	public SyncStateDalHelper() {
		dbHelper = new DBHelper.DatabaseHelper(ReaderApp.getAppContext());
		db = dbHelper.getWritableDatabase();
	}
	public void Close(){
		dbHelper.close();
	}
	/**
	 * 鍒ゆ柇SyncStateId鏄惁瀛樺湪
	 * 
	 * @param blogId
	 * @return
	 */
	public boolean Exist(int syncStateId) {
		String where = "SyncStateId=?";
		String[] args = {String.valueOf(syncStateId)};
		Cursor cursor = db.query(Config.DB_SYNCSTATE_TABLE, null, where, args, null,null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();
		return isExist;
	}
	
	/**
	 * 鍒ゆ柇BlogId鏄惁瀛樺湪
	 * 
	 * @param blogId
	 * @return
	 */
	public boolean Exist(String id, SyncType type) {
		String where = "";
		if(type == SyncType.Blog)
			where = "BlogOriginId=?";
		if(type == SyncType.Blog)
			where = "ChannelId=?";
		String[] args = {id};
		Cursor cursor = db.query(Config.DB_SYNCSTATE_TABLE, null, where, args, null,null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();
		return isExist;
	}
	
	/**
	 * 鍒ゆ柇id鏄惁瀛樺湪
	 * 
	 * @param id
	 * @return
	 */
	public boolean Exist(String id) {
		String where = "BlogOriginId=? or ChannelId=?";
		String[] args = {id, id};
		Cursor cursor = db.query(Config.DB_SYNCSTATE_TABLE, null, where, args, null,null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();
		return isExist;
	}
	
	/**
	 * 鑾峰彇鍓�0鏉¤褰�
	 * 
	 * @return
	 */
	public List<SyncState> GetSyncStateList() {
		//String limit = "30";
		String where = "";

		return GetSyncStateListByWhere(null, null, null);
	}
	/**
	 * 鍒嗛〉鑾峰彇
	 */
	public List<SyncState> GetSyncStateListByPage(int pageIndex, int pageSize) {
		String limit = String.valueOf((pageIndex - 1) * pageSize) + "," + String.valueOf(pageSize);
		List<SyncState> list = GetSyncStateListByWhere(limit, null, null);

		return list;
	}
	
	/**
	 * 鏍规嵁SyncStateId鑾峰彇鍗曟潯璁板綍
	 */
	public SyncState GetSyncStateEntity(int syncStateId) {
		String limit = "1";
		String where = "SyncStateId=?";
		String[] args = {String.valueOf(syncStateId)};
		List<SyncState> list = GetSyncStateListByWhere(limit, where, args);
		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
	
	/**
	 * 鏍规嵁id鑾峰彇鍗曟潯璁板綍
	 */
	public SyncState GetSyncStateEntity(String id, SyncType type) {
		String limit = "1";
		String where = "SyncStateId=?";
		String[] args = {id};
		List<SyncState> list = GetSyncStateListByWhere(limit, where, args);
		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
	
	/**
	 * 鏍规嵁where鏉′欢鑾峰彇璁板綍
	 * 
	 * @param top
	 * @param where
	 */
	public List<SyncState> GetSyncStateListByWhere(String limit, String where, String[] args) {
		List<SyncState> syncStateList = new ArrayList<SyncState>();
		String orderBy = "TimeStamp DESC";
		Cursor cursor = db.query(Config.DB_SYNCSTATE_TABLE, null, where, args, null, null, orderBy, limit);
		while (cursor != null && cursor.moveToNext()) {
			SyncState entity = new SyncState();			
			entity.SyncStateId = cursor.getInt(cursor.getColumnIndex("SyncStateId"));
			entity.BlogOriginId = cursor.getString(cursor.getColumnIndex("BlogOriginId"));
			entity.ChannelId = cursor.getString(cursor.getColumnIndex("ChannelId"));
			entity.Status = RssAction.values()[cursor.getInt(cursor.getColumnIndex("Status"))];
			entity.Tag = cursor.getString(cursor.getColumnIndex("Tag"));
			entity.TimeStamp = DateHelper.ParseDate(cursor.getString(cursor.getColumnIndex("TimeStamp")));			
			syncStateList.add(entity);
		}
		cursor.close();

		return syncStateList;
	}
	
	public void Delete(List<String> ids, SyncType type){
//		String where = "";
//		if(type == SyncType.Blog)
//			where = "BlogOriginId in (?) ";
//		if(type == SyncType.Channel)
//			where = "ChannelId in (?) ";
//		
//		StringBuffer sb = new StringBuffer();
//		
//		for(String id: ids){
//			sb.append(id + ",");
//		}
//		
//		if(sb.length() > 0)
//			sb.deleteCharAt(sb.length() - 1);
//		
//		String[] args = { sb.toString()};
//		db.delete(Config.DB_SYNCSTATE_TABLE, where, args);
		
		String where = "";
		if(type == SyncType.Blog)
			where = "BlogOriginId =?";
		if(type == SyncType.Channel)
			where = "ChannelId=?";
		
		synchronized (_writeLock) {
			db.beginTransaction();
			try {				
				for (int i = ids.size() - 1, len = 0; i >= len; i--) {
					String id = ids.get(i);					
					db.delete(Config.DB_SYNCSTATE_TABLE, where, new String[]{id});
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
	 * 鍚屾鏁版嵁
	 * 
	 * @param list
	 */
	public void SynchronyData2DB(List<SyncState> recordList) {
		List<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = 0, len = recordList.size(); i < len; i++) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("BlogOriginId", recordList.get(i).BlogOriginId);
			contentValues.put("ChannelId", recordList.get(i).ChannelId);
			contentValues.put("Status", recordList.get(i).Status.ordinal());
			contentValues.put("Tag", recordList.get(i).Tag);			
			contentValues.put("TimeStamp", DateHelper.ParseDateToString(recordList.get(i).TimeStamp));			
			list.add(contentValues);
		}
		synchronized (_writeLock) {
			db.beginTransaction();
			try {				
				for (int i = 0, len = list.size(); i < len; i++) {
					String id = "";
					String BlogOriginId=list.get(i).getAsString("BlogOriginId");
					String ChannelId=list.get(i).getAsString("ChannelId");
					
					if(BlogOriginId != null && BlogOriginId.length() == 0)
						id = BlogOriginId;
					
					if(ChannelId != null && ChannelId.length() == 0)
						id = ChannelId;
					
					boolean isExist = Exist(id);
					
					if (!isExist) {
						db.insert(Config.DB_SYNCSTATE_TABLE, null, list.get(i));
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
	
	/**
	 * 鍚屾鏁版嵁
	 * 
	 * @param list
	 */
	public void SynchronyData2DB(SyncState record) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("BlogOriginId", record.BlogOriginId);
		contentValues.put("ChannelId", record.ChannelId);
		contentValues.put("Status", record.Status.ordinal());
		contentValues.put("Tag", record.Tag);			
		contentValues.put("TimeStamp", DateHelper.ParseDateToString(record.TimeStamp));
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				String id = "";
				String BlogOriginId=record.BlogOriginId;
				String ChannelId=record.ChannelId;
				
				if(BlogOriginId != null && BlogOriginId.length() == 0)
					id = BlogOriginId;
				
				if(ChannelId != null && ChannelId.length() == 0)
					id = ChannelId;
				
				boolean isExist = Exist(id);
				
				if (!isExist) {
					db.insert(Config.DB_SYNCSTATE_TABLE, null, contentValues);
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