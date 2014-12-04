package com.lgq.rssreader.entity;

import java.io.Serializable;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Blog implements Serializable,Comparable<Blog> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 373299365769337131L;
		
	public String BlogId;
	public String TagId;
	public String ChannelId;
	public String Title;
	public String Description;
	public String Link;
	public Date PubDate;
	public String SubsTitle;
	public long TimeStamp;
	public boolean IsRead;
	public boolean IsStarred;
	public String OriginId;
	public boolean IsRecommend;
	public String Avatar;
	public String Content;	

//	public Blog(Parcel source) {
//		BlogId = source.readString();
//		TagId = source.readString();
//		ChannelId = source.readString();
//		Title = source.readString();
//		Description = source.readString();
//		Link = source.readString();
//		PubDate =  new Date(source.readLong());
//		SubsTitle = source.readString();
//		TimeStamp  = source.readLong();
//		IsRead = source.readInt() == 1;
//		IsStarred = source.readInt() == 1;
//		OriginId = source.readString();
//		IsRecommend = source.readInt() == 1;
//		Avatar = source.readString();
//		Content = source.readString();
//	}

	public Blog() {
		
	}

	@Override
    public boolean equals(Object t){
    	if(t == null){
    		return false;
    	}
    	
    	if(!(t instanceof Blog)){
    		return false;
    	}
    	
    	Blog tmp = (Blog)t;
    	
    	return BlogId.equals(tmp.BlogId);
    }
	
	@Override
    public int hashCode(){
    	return BlogId.hashCode();
    }
	
	@Override
	public int compareTo(Blog arg0) {
		return (int) (PubDate.getTime() - arg0.PubDate.getTime() + TimeStamp - arg0.TimeStamp);
	}
	
	

//	public static final Parcelable.Creator<Blog> CREATOR = new Creator<Blog>() {
//        @Override  
//        public Blog[] newArray(int size) {  
//            return new Blog[size];  
//        }  
//          
//        //将Parcel对象反序列化为ParcelableDate  
//        @Override  
//        public Blog createFromParcel(Parcel source) {  
//            return new Blog(source);  
//        }  
//    }; 
//	
//	@Override
//	public int describeContents() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(BlogId);
//		dest.writeString(TagId);
//		dest.writeString(ChannelId);
//		dest.writeString(Title);
//		dest.writeString(Description);
//		dest.writeString(Link);
//		dest.writeLong(PubDate.getTime());
//		dest.writeString(SubsTitle);
//		dest.writeLong(TimeStamp);
//		dest.writeInt(IsRead ? 1 : 0);
//		dest.writeInt(IsStarred ? 1 : 0);
//		dest.writeString(OriginId);
//		dest.writeInt(IsRecommend ? 1 : 0);
//		dest.writeString(Avatar);
//		dest.writeString(Content);
//	}
}
