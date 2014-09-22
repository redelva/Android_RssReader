package com.lgq.rssreader.dal;

import java.util.List;

import com.lgq.rssreader.core.Config;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper {
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	public final static byte[] _writeLock = new byte[0];
	// ����ݿ�
	public void OpenDB(Context context) {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	// �ر���ݿ�
	public void Close() {
		dbHelper.close();
		if(db!=null){
			db.close();
		}
	}
		
	/**
	 * ����
	 * 
	 * @param list
	 * @param table
	 *            ����
	 */
	public void Insert(List<ContentValues> list, String tableName) {
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				db.delete(tableName, null, null);
				for (int i = 0, len = list.size(); i < len; i++)
					db.insert(tableName, null, list.get(i));
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
	public DBHelper(Context context) {
		this.dbHelper = new DatabaseHelper(context);
	}
	/**
	 * ���ڳ�ʼ����ݿ�
	 * 
	 * @author Administrator
	 * 
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper {
		// ������ݿ��ļ�
		private static final String DB_NAME = Config.DB_FILE_NAME;
		// ������ݿ�汾
		private static final int DB_VERSION = 1;
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			CreateBlogDb(db);
			Log.i("DBHelper", "����Blogs��ɹ�");
			CreateBlogView(db);
			Log.i("DBHelper", "����vBlogs��ͼ�ɹ�");
			CreateImageRecordDb(db);
			Log.i("DBHelper", "����ImageRecords��ɹ�");			
			CreateSyncStateDb(db);
			Log.i("DBHelper", "����SyncStates��ɹ�");
		}
		/**
		 * ����Blogs��
		 * 
		 * @param db
		 */
		private void CreateBlogDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [Blogs] (");			
			sb.append("[BlogId] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[ChannelId] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[TagId] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[Title] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[Description] NTEXT NOT NULL DEFAULT (''), ");			
			sb.append("[Content] NTEXT DEFAULT (''), ");
			sb.append("[Link] NVARCHAR(200), ");
			sb.append("[PubDate] DATETIME, ");
			sb.append("[SubsTitle] NVARCHAR(50) NOT NULL DEFAULT (''), ");			
			sb.append("[TimeStamp] INTEGER(16) NOT NULL DEFAULT (0), ");
			sb.append("[IsRead] BOOLEAN DEFAULT (0), ");// �Ƿ��Ѷ�
			sb.append("[IsStarred] BOOLEAN DEFAULT (0), ");// �Ƿ��ղ�
			sb.append("[IsRecommend] BOOLEAN DEFAULT (0), ");// �Ƿ��Ƽ�
			sb.append("[OriginId] NVARCHAR(200), ");
			sb.append("[Continuation] NVARCHAR(200), ");
			sb.append("[Avatar] NVARCHAR(50))");			
			
			db.execSQL(sb.toString());
		}
		
		/**
		 * ����Blogs��ͼ
		 * 
		 * @param db
		 */
		private void CreateBlogView(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE VIEW vBlogs AS SELECT A.*,");
			sb.append("(SELECT COUNT(*) FROM BLOGS WHERE TIMESTAMP>A.TIMESTAMP OR ");
			sb.append("	(TIMESTAMP=A.TIMESTAMP AND PUBDATE>A.PUBDATE) OR ");
			sb.append("	(TIMESTAMP=A.TIMESTAMP AND PUBDATE=A.PUBDATE AND ROWID > A.ROWID)");
			sb.append(") AS ID ");
			sb.append("FROM BLOGS A ORDER BY PUBDATE DESC, TIMESTAMP DESC, ROWID DESC");
			
			db.execSQL(sb.toString());
		}
		
		/**
		 * ����ImageRecord��
		 * 
		 * @param db
		 */
		private void CreateImageRecordDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [ImageRecords] (");
			sb.append("[ImageRecordId] INTEGER PRIMARY KEY AUTOINCREMENT , ");
			sb.append("[BlogId] NVARCHAR(50), ");
			sb.append("[OriginUrl] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[StoredName] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[Extension] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[TimeStamp] DATETIME, ");
			sb.append("[Size] Double )");

			db.execSQL(sb.toString());
		}
		/**
		 * ����SyncState��
		 * 
		 * @param db
		 */
		private void CreateSyncStateDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [SyncStates] (");
			sb.append("[SyncStateId] INTEGER PRIMARY KEY AUTOINCREMENT , ");
			sb.append("[BlogOriginId] NVARCHAR(50), ");
			sb.append("[ChannelId] NVARCHAR(50), ");
			sb.append("[Status] int NOT NULL, ");
			sb.append("[TimeStamp] DATETIME, ");
			sb.append("[Tag] NVARCHAR(100))");

			db.execSQL(sb.toString());
		}		
		/**
		 * ��������CommentList��
		 * 
		 * @param db
		 */
		private void CreateCommentDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [CommentList] (");
			sb.append("[CommentId] INTEGER NOT NULL DEFAULT (0), ");
			sb.append("[PostUserUrl] NVARCHAR(200) NOT NULL DEFAULT (''), ");
			sb.append("[PostUserName] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[Content] NTEXT NOT NULL DEFAULT (''), ");
			sb.append("[ContentId] INTEGER NOT NULL DEFAULT (0), ");
			sb.append("[CommentType] INTEGER DEFAULT (0), ");
			sb.append("[AddTime] DATETIME);");
			db.execSQL(sb.toString());
		}
		/**
		 * �������Ĳ���RssList��
		 * 
		 * @param db
		 */
		private void CreateRssListDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [RssList] (");
			sb.append("[RssId] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,");
			sb.append("[Title] NVARCHAR(50) NOT NULL DEFAULT (''),");
			sb.append("[Link] NVARCHAR(500) NOT NULL DEFAULT (''), ");
			sb.append("[Description] NVARCHAR(500) DEFAULT (''),");
			sb.append("[AddTime] DATETIME DEFAULT (date('now')), ");
			sb.append("[OrderNum] INTEGER DEFAULT (0),");
			sb.append("[RssNum] INTEGER DEFAULT (0),");
			sb.append("[Guid] NVARCHAR(500),");
			sb.append("[IsCnblogs] BOOLEAN DEFAULT (0),");
			sb.append("[Image] NVARCHAR(200) DEFAULT (''),");
			sb.append("[Updated] DATETIME DEFAULT (date('now')),");
			sb.append("[Author] NVARCHAR(50) DEFAULT (''),");
			sb.append("[CateId] INTEGER,");
			sb.append("[CateName] NVARCHAR DEFAULT (''),");
			sb.append("[IsActive] BOOLEAN DEFAULT (1));");
			sb.append(");");
			db.execSQL(sb.toString());
		}
		/**
		 * ������������RssItem��
		 * 
		 * @param db
		 */
		private void CreateRssItemDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [RssItem] (");
			sb.append("[Id] INTEGER PRIMARY KEY AUTOINCREMENT,");
			sb.append("[Title] NVARCHAR(200) DEFAULT (''),");
			sb.append("[Link] NVARCHAR(200) DEFAULT (''),");
			sb.append("[Description] NTEXT DEFAULT (''),");
			sb.append("[Category] NVARCHAR(50),");
			sb.append("[Author] NVARCHAR(50) DEFAULT (''),");
			sb.append("[AddDate] DATETIME,");
			sb.append("[IsReaded] BOOLEAN DEFAULT (0),");
			sb.append("[IsDigg] BOOLEAN DEFAULT (0));");
			db.execSQL(sb.toString());
		}
		/**
		 * �����ղر�FavList
		 * @param db
		 */
		private void CreateFavListDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [FavList] (");
			sb.append("[FavId] INTEGER PRIMARY KEY AUTOINCREMENT,");
			sb.append("[AddTime] DATETIME NOT NULL DEFAULT (date('now')), ");
			sb.append("[ContentType] INTEGER NOT NULL DEFAULT (0),");
			sb.append("[ContentId] INTEGER NOT NULL DEFAULT (0));");
			db.execSQL(sb.toString());
		}
		/**
		 * ���°汾ʱ���±�
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			DropTable(db);
			onCreate(db);
			Log.e("User", "onUpgrade");
		}
		/**
		 * ɾ���
		 * 
		 * @param db
		 */
		private void DropTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("DROP TABLE IF EXISTS " + Config.DB_BLOG_TABLE + ";");
			sb.append("DROP TABLE IF EXISTS " + Config.DB_IMAGE_TABLE + ";");
			sb.append("DROP TABLE IF EXISTS " + Config.DB_SYNCSTATE_TABLE + ";");
//			sb.append("DROP TABLE IF EXISTS " + Config.DB_RSSLIST_TABLE + ";");
//			sb.append("DROP TABLE IF EXISTS " + Config.DB_RSSITEM_TABLE + ";");
//			sb.append("DROP TABLE IF EXISTS " + Config.DB_FAV_TABLE + ";");
			db.execSQL(sb.toString());
		}
		/**
		 * �����ݱ?�����������ݣ�
		 * @param db
		 */
		public static void ClearData(Context context){
			DatabaseHelper dbHelper = new DBHelper.DatabaseHelper(context);
			SQLiteDatabase db=dbHelper.getWritableDatabase();
			StringBuilder sb=new StringBuilder();
			sb.append("DELETE FROM Blogs;");//��ղ��ͱ�
			sb.append("DELETE FROM ImageRecords;");//��ղ��ͱ�
			sb.append("DELETE FROM SyncStates;");//��ղ��ͱ�
			//sb.append("DELETE FROM NewsList WHERE IsFull=0;");//������ű�
			//sb.append("DELETE FROM CommentList;");//������۱�
			//sb.append("DELETE FROM RssItem;");//��ն������±�
			db.execSQL(sb.toString());
		}
	}
}