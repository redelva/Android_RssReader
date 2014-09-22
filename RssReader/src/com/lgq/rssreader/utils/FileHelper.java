package com.lgq.rssreader.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.lgq.rssreader.core.Config;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class FileHelper {
	/**
	 * �����ļ���
	 * 
	 * @param dirName
	 */
	public static void MakeDir(String dirName) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File destDir = new File(dirName);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
		}
	}
	
	/**
	 * �ļ����Ƿ����
	 * 
	 * @param dirName
	 */
	public static boolean DirectoryExists(String dirName) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File destDir = new File(dirName);
			return destDir.exists();				
		}
		return false;
	}

	/**
	 * ɾ���·�����ʾ���ļ���Ŀ¼�� ����·�����ʾһ��Ŀ¼�������ɾ��Ŀ¼�µ������ٽ�Ŀ¼ɾ�����Ըò�������ԭ���Եġ�
	 * ���Ŀ¼�л���Ŀ¼�������ݹ鶯����
	 * 
	 * @param filePath
	 *            Ҫɾ���ļ���Ŀ¼��·����
	 * @return ���ҽ����ɹ�ɾ���ļ���Ŀ¼ʱ������ true�����򷵻� false��
	 */
	public static boolean DeleteFile(String filePath) {
		File file = new File(filePath);
		if (file.listFiles() == null)
			return true;
		else {
			File[] files = file.listFiles();
			for (File deleteFile : files) {
				if (deleteFile.isDirectory())
					DeleteAllFile(deleteFile);
				else
					deleteFile.delete();
			}
		}
		return true;
	}
	/**
	 * ɾ��ȫ���ļ�
	 * 
	 * @param file
	 * @return
	 */
	private static boolean DeleteAllFile(File file) {
		File[] files = file.listFiles();
		for (File deleteFile : files) {
			if (deleteFile.isDirectory()) {
				// ������ļ��У���ݹ�ɾ��������ļ�����ɾ����ļ���
				if (!DeleteAllFile(deleteFile)) {
					// ���ʧ���򷵻�
					return false;
				}
			} else {
				if (!deleteFile.delete()) {
					// ���ʧ���򷵻�
					return false;
				}
			}
		}
		return file.delete();
	}
	/**
	 * �õ���ݿ��ļ�·��
	 * @return
	 */
	public static String GetDbFileAbsolutePath(){
		String dbPath="/data/data/" + Config.APP_PACKAGE_NAME + "/databases/" + Config.DB_FILE_NAME;
		return dbPath;
	}
	/**
	 * ��ȡ�ļ���С
	 * @param filePath
	 * @return
	 */
	public static long GetFileLength(String filePath){
		File file=new File(filePath);
		return file.length();
	}
	/**
	 * ��ȡ�ļ��д�С
	 * @param dirPath
	 * @return
	 */
	public static long GetPathLength(String dirPath){
		File dir=new File(dirPath);
		return getDirSize(dir);
	}
	
	private static long getDirectorySize(File directory, long blockSize) {
        File[] files = directory.listFiles();
        if (files != null) {
            // space used by directory itself 
            long size = directory.length();

            for (File file : files) {
                if (file.isDirectory()) {
                    // space used by subdirectory
                    size += getDirectorySize(file, blockSize);
                } else {
                    // file size need to rounded up to full block sizes
                    // (not a perfect function, it adds additional block to 0 sized files
                    // and file who perfectly fill their blocks) 
                    size += (file.length() / blockSize + 1) * blockSize;
                }
            }
            return size;
        } else {
            return 0;
        }
    }
    
    @SuppressLint("NewApi")
	private static long getDirectorySize(File directory) {
        StatFs statFs = new StatFs(directory.getAbsolutePath());
        long blockSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
        } else {
            blockSize = statFs.getBlockSize();
        }

        return getDirectorySize(directory, blockSize);
    }
    
    public static long getImageFolderSize(){
    	String sDStateString = android.os.Environment.getExternalStorageState();

		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try { 
				File SDFile = android.os.Environment.getExternalStorageDirectory();
 
				File dir = new File(SDFile.getAbsolutePath() + Config.IMAGES_LOCATION);

				if (dir.exists()) {
					return getDirectorySize(dir) / (1024 * 1024);
				}
			}
			catch(Exception e){
				Log.e("RssReader", e.getMessage());
			}
		}
		
		return 0;
    }
	
	/**
	 * ��ȡ�ļ��д�С
	 * @param dir
	 * @return
	 */
	private static long getDirSize(File dir) {  
	    if (dir == null) {  
	        return 0;  
	    }  
	    if (!dir.isDirectory()) {  
	        return 0;  
	    }  
	    long dirSize = 0;  
	    File[] files = dir.listFiles();  
	    for (File file : files) {  
	        if (file.isFile()) {  
	            dirSize += file.length();  
	        } else if (file.isDirectory()) {  
	            dirSize += file.length();  
	            dirSize += getDirSize(file); // �������Ŀ¼��ͨ��ݹ���ü���ͳ��  
	        }  
	    }  
	    return dirSize;  
	} 
	
	/**
	 * ���ֳ�����ת��ΪKB/MB
	 * @param size
	 * @return
	 */
	public static String GetFileSize(long size){
		int kbSize=(int)size/1024;
		if(kbSize>1024){
			float mbSize=kbSize/1024;
			DecimalFormat formator=new DecimalFormat( "##,###,###.## ");
			return formator.format(mbSize) + "M";
		}
		return kbSize + "K";
	}
		
	public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {

        }
    }
}
