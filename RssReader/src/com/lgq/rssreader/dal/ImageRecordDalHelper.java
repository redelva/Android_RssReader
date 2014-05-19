package com.lgq.rssreader.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.enums.RssTab;
import com.lgq.rssreader.utils.DateHelper;
import com.lgq.rssreader.utils.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ImageRecordDalHelper {
	private DBHelper.DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	public final static byte[] _writeLock = new byte[0];
	Context context;
	public ImageRecordDalHelper() {
		dbHelper = new DBHelper.DatabaseHelper(ReaderApp.getAppContext());
		db = dbHelper.getWritableDatabase();
	}
	public void Close(){
		dbHelper.close();
	}
	/**
	 * 判断imageRecordId是否存在
	 * 
	 * @param blogId
	 * @return
	 */
	public boolean Exist(int imageRecordId) {
		String where = "ImageRecordId=?";
		String[] args = {String.valueOf(imageRecordId)};
		Cursor cursor = db.query(Config.DB_IMAGE_TABLE, null, where, args, null,null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();
		return isExist;
	}
	
	/**
	 * 判断OriginUrl是否存在
	 * 
	 * @param blogId
	 * @return
	 */
	public boolean Exist(String OriginUrl) {
		String where = "OriginUrl=?";
		String[] args = {String.valueOf(OriginUrl)};
		Cursor cursor = db.query(Config.DB_IMAGE_TABLE, null, where, args, null,null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();
		return isExist;
	}
	
	/**
	 * 删除Images
	 * 
	 * @param records
	 * @return
	 */
	public void DeleteRecords(List<ImageRecord> records) {
//		String sql = "delete from ImageRecords where ImageRecordId in (";		
//		StringBuilder sb = new StringBuilder();
//		for(ImageRecord r : records){
//			sb.append("\"" + r.ImageRecordId + "\",");
//		}
//		if(records.size() > 0)
//			sb.deleteCharAt(sb.length() - 1);
//		sql = sql + sb.toString() + ")";
//		db.execSQL(sql, null);
		
		synchronized (_writeLock) {
			db.beginTransaction();
			try {				
				for (int i = records.size() - 1, len = 0; i >= len; i--) {
					String recordId = String.valueOf(records.get(i).ImageRecordId);					
					db.delete(Config.DB_IMAGE_TABLE, "ImageRecordId=?", new String[]{recordId});
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
	 * 获取前30条记录
	 * 
	 * @return
	 */
	public List<ImageRecord> GetTopImageList() {
		String limit = "15";
		String where = "";

		return GetImageRecordListByWhere(limit, where, null);
	}
	/**
	 * 分页获取
	 */
	public List<ImageRecord> GetImageRecordListByPage(int pageIndex, int pageSize) {
		String limit = String.valueOf((pageIndex - 1) * pageSize) + "," + String.valueOf(pageSize);
		List<ImageRecord> list = GetImageRecordListByWhere(limit, null, null);

		return list;
	}
	
	/**
	 * 根据recordId获取单条记录
	 */
	public ImageRecord GetImageRecordEntity(int recordId) {
		String limit = "1";
		String where = "ImageRecordId=?";
		String[] args = {String.valueOf(recordId)};
		List<ImageRecord> list = GetImageRecordListByWhere(limit, where, args);
		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
	
	/**
	 * 根据ImageUrl获取单条记录
	 */
	public ImageRecord GetImageRecordEntity(String imageUrl) {
		String limit = "1";
		String where = "OriginUrl=?";
		String[] args = {imageUrl};
		List<ImageRecord> list = GetImageRecordListByWhere(limit, where, args);
		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
	
	/**
	 * 根据ImageUrl获取单条记录
	 */
	public List<ImageRecord> GetImageRecordByBlog(List<Blog> blogs) {		
		String where = "BlogId in (?)";
		StringBuilder ids = new StringBuilder();
		for(Blog b : blogs){
			ids.append(b.BlogId + ",");
		}
		if(ids.length() > 0 ){
			ids.deleteCharAt(ids.length() - 1);
		}
		
		String[] args = {ids.toString()};
		return GetImageRecordListByWhere(null, where, args);
		
	}
	
	/**
	 * 根据storedName获取单条记录
	 */
	public ImageRecord GetImageRecordEntityByStoreName(String storedName) {
		String limit = "1";
		String where = "StoredName=?";
		String[] args = {storedName};
		List<ImageRecord> list = GetImageRecordListByWhere(limit, where, args);
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
	public List<ImageRecord> GetImageRecordListByWhere(String limit, String where, String[] args) {
		List<ImageRecord> listImageRecord = new ArrayList<ImageRecord>();
		String orderBy = "TimeStamp DESC";
		Cursor cursor = db.query(Config.DB_IMAGE_TABLE, null, where, args, null, null, orderBy, limit);
		while (cursor != null && cursor.moveToNext()) {
			ImageRecord entity = new ImageRecord();			
			entity.ImageRecordId = cursor.getInt(cursor.getColumnIndex("ImageRecordId"));
			entity.Extension = cursor.getString(cursor.getColumnIndex("Extension"));
			entity.OriginUrl = cursor.getString(cursor.getColumnIndex("OriginUrl"));
			entity.BlogId = cursor.getString(cursor.getColumnIndex("BlogId"));
			entity.StoredName = cursor.getString(cursor.getColumnIndex("StoredName"));
			entity.Size = cursor.getDouble(cursor.getColumnIndex("Size"));
			entity.TimeStamp = DateHelper.ParseDate(cursor.getString(cursor.getColumnIndex("TimeStamp")));			
			listImageRecord.add(entity);
		}
		cursor.close();

		return listImageRecord;
	}
	
	/**
	 * 同步数据
	 * 
	 * @param list
	 */
	public void SynchronyData2DB(List<ImageRecord> recordList) {
		List<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = 0, len = recordList.size(); i < len; i++) {
			ContentValues contentValues = new ContentValues();
			//contentValues.put("ImageRecordId", recordList.get(i).ImageRecordId);
			contentValues.put("OriginUrl", recordList.get(i).OriginUrl);
			contentValues.put("Extension", recordList.get(i).Extension);
			contentValues.put("StoredName", recordList.get(i).StoredName);
			contentValues.put("BlogId", recordList.get(i).BlogId);
			contentValues.put("Size", recordList.get(i).Size);
			contentValues.put("TimeStamp", DateHelper.ParseDateToString(recordList.get(i).TimeStamp));			
			list.add(contentValues);
		}
		synchronized (_writeLock) {
			db.beginTransaction();
			try {				
				for (int i = 0, len = list.size(); i < len; i++) {
					String OriginUrl=list.get(i).getAsString("OriginUrl");
					boolean isExist = Exist(OriginUrl);
					
					if (!isExist) {
						db.insert(Config.DB_IMAGE_TABLE, null, list.get(i));
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
	 * 同步数据
	 * 
	 * @param list
	 */
	public void SynchronyData2DB(ImageRecord record) {
		ContentValues contentValues = new ContentValues();
		//contentValues.put("ImageRecordId", recordList.get(i).ImageRecordId);
		contentValues.put("OriginUrl", record.OriginUrl);
		contentValues.put("Extension", record.Extension);
		contentValues.put("StoredName", record.StoredName);
		contentValues.put("BlogId", record.BlogId);
		contentValues.put("Size", record.Size);			
		contentValues.put("TimeStamp", DateHelper.ParseDateToString(record.TimeStamp));
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				boolean isExist = Exist(record.OriginUrl);
				
				if (!isExist) {
					db.insert(Config.DB_IMAGE_TABLE, null, contentValues);
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