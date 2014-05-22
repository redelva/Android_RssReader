package com.lgq.rssreader.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lgq.rssreader.cache.AsyncImageLoader.ImageCallback;
import com.lgq.rssreader.core.Config;
import com.lgq.rssreader.core.ReaderApp;
import com.lgq.rssreader.dal.BlogDalHelper;
import com.lgq.rssreader.dal.ImageRecordDalHelper;
import com.lgq.rssreader.entity.Blog;
import com.lgq.rssreader.entity.Channel;
import com.lgq.rssreader.entity.ImageRecord;
import com.lgq.rssreader.enums.ImageType;
import com.lgq.rssreader.utils.Helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ImageCacher {
	private Context context;
	public ImageCacher(Context context) {
		this.context = context;
	}
	public ImageCacher() {

	}
	
	/**
	 * �õ�ͼƬ��ַ�ļ���
	 * 
	 * @param imageType
	 * @return
	 */
	public static String GetImageFolder(Blog blog) {
		String folder = Config.IMAGES_LOCATION;
		
		List<Channel> channels = Helper.getChannels();

		Channel target = null;
		Channel parent = null;
		
		for(Channel c : channels){
			if(c.Id.equals(blog.ChannelId) || c.Id.equals(blog.TagId)){
				target = c;
				break;
			}
		}
		
		if(target == null){
			for(Channel c : channels){
				if(c.IsDirectory && c.Children != null && c.Children.size() != 0){
					for(Channel child : c.Children){
						if(child.Title.equals(blog.SubsTitle)){
							parent = c;
							target = child;
							break;
						}
					}
				}				
			}
		}
		
		if(target == null ){
			
			Log.e("RssReader", blog.Title + " " + blog.ChannelId + " " + blog.TagId + " " + blog.SubsTitle);
			
			return folder;
		}
		
		if(parent != null)
			folder = folder + parent.Folder + "/" + target.Folder + "/";
		else
			folder = folder + target.Folder + "/";		
		
		return folder;
	}
	
	static final Pattern patternImgSrc = Pattern.compile("<img(.+?)src=\"(.+?)\"(.+?)>");
	
	static final Pattern patternImgxSrc = Pattern.compile("<img(.+?)xsrc=\"(.+?)\"(.+?)>");
	
	/**
	 * �õ�html�е�ͼƬ��ַ
	 * 
	 * @param html
	 * @return
	 */
	private static List<String> GetImagesList(String html) {
		List<String> listSrc = new ArrayList<String>();
		Matcher m = patternImgSrc.matcher(html);
		while (m.find()) {
			if(!m.group(2).equals("Loading.gif"))
				listSrc.add(m.group(2));
		}
		
		Matcher xm = patternImgxSrc.matcher(html);
		while (xm.find()) {
			listSrc.add(xm.group(2));
		}

		return listSrc;
	}
	
	/**
	 * �õ���ͼƬ��ַ������·����
	 * 
	 * @param imgType
	 * @param imageUrl
	 * @return
	 */
	private static String GetLocalImgSrc(Blog b, String src) {
		String folder = GetImageFolder(b);
		
		String localFile = new ImageRecordDalHelper().GetImageRecordEntity(src).StoredName;

		return "file:///mnt" + folder + localFile;
	}
	
	/**
	 * ����html�е�ͼƬ
	 * 
	 * @param imgType
	 * @param html
	 */
	public static void DownloadHtmlImage(final Blog b) {		
		List<String> listSrc = GetImagesList(b.Content);
		for (String src : listSrc) {
			ImageRecord record = Helper.loadDrawable(b, src);

			b.Content.replace(record.OriginUrl, record.StoredName.replace("/rssreader", ".."));
			new BlogDalHelper().SynchronyContent2DB(b.BlogId, b.Content);
		}
	}
	
	/**
	 * ����html�е�ͼƬ
	 * 
	 * @param b
	 * @param content
	 */
	public static String DownloadHtmlImage(final Blog b, String content) {		
		List<String> listSrc = GetImagesList(content);
		for (String src : listSrc) {
			ImageRecord record = Helper.loadDrawable(b, src);

			content = content.replace(record.OriginUrl, record.StoredName.replace("/rssreader", ".."));			
		}
		
		return content;
	}
	
	/**
	 * �õ���ʽ�����html
	 * 
	 * @param imgType
	 * @param html
	 * @return
	 */
	public static String FormatLocalHtmlWithImg(Blog blog) {
		List<String> listSrc = GetImagesList(blog.Content);
		for (String src : listSrc) {
			String newSrc = GetLocalImgSrc(blog, src);
			blog.Content = blog.Content.replace(src, newSrc);
		}

		return blog.Content;
	}
}