package com.lgq.rssreader.entity;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.ParcelableSpan;

public class Channel implements Serializable,Comparable<Channel> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8836207717484134876L;

	public static String TOKEN = "Channel";
    
    public String Id;
    public String Title;
    public String SortId;
    public Date LastUpdateTime;
    public Date LastRefreshTime;
    public int UnreadCount;
    public boolean IsDirectory;
    public String Folder;
    public boolean FavIcon;
    public ArrayList<Channel> Children;
    public Object Tag;

    public Channel(Channel obj)
    {
        Id = obj.Id;
        Title = obj.Title;
        SortId = obj.SortId;
        LastUpdateTime = obj.LastUpdateTime;
        LastRefreshTime = obj.LastRefreshTime;        
        UnreadCount = obj.UnreadCount;
        IsDirectory = obj.IsDirectory;
        Folder = obj.Folder;        
        FavIcon = obj.FavIcon;
        Children = new ArrayList<Channel>();
        for(Channel child : obj.Children)
        {
            Children.add(child);
        }
        Tag = new Object();
    }

	public Channel() {
		// TODO Auto-generated constructor stub
	}
//	
//	public Channel(Parcel source) {
//		Id = source.readString();
//		Title = source.readString();
//		SortId = source.readString();
//		LastUpdateTime = new Date(source.readLong());
//		LastRefreshTime = new Date(source.readLong());
//		UnreadCount = source.readInt();
//		IsDirectory = source.readInt() == 1;
//	    Folder = source.readString();
//	    FavIcon = source.readInt() == 1;
//	    Children = (ArrayList<Channel>)source.readArrayList(Channel.class.getClassLoader());
//	    Object[] objs= ((Object[])source.readArray(Object.class.getClassLoader()));
//	    Tag = objs[0];
//	}

	@Override
    public boolean equals(Object t){
    	if(t == null){
    		return false;
    	}
    	
    	if(!(t instanceof Channel)){
    		return false;
    	}
    	
    	Channel tmp = (Channel)t;
    	
    	return Id.equals(tmp.Id);
    }
	
	@Override
    public int hashCode(){
    	return Id.hashCode();
    }
	
	@Override
	public int compareTo(Channel arg0) {
		return UnreadCount - arg0.UnreadCount;
	}

//	@Override
//	public int describeContents() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(Id);
//		dest.writeString(Title);
//		dest.writeString(SortId);
//		dest.writeLong(LastUpdateTime.getTime());
//		dest.writeLong(LastRefreshTime.getTime());
//		dest.writeInt(UnreadCount);
//		dest.writeInt(IsDirectory ? 1 : 0);
//	    dest.writeString(Folder);
//	    dest.writeInt(FavIcon ? 1: 0);
//	    dest.writeList(Children);
//	    dest.writeArray(new Object[]{Tag});
//	}
//	
//	//实例化静态内部对象CREATOR实现接口Parcelable.Creator  
//    public static final Parcelable.Creator<Channel> CREATOR = new Creator<Channel>() {  
//          
//        @Override  
//        public Channel[] newArray(int size) {  
//            return new Channel[size];  
//        }  
//          
//        //将Parcel对象反序列化为ParcelableDate  
//        @Override  
//        public Channel createFromParcel(Parcel source) {  
//            return new Channel(source);  
//        }  
//    }; 
}
