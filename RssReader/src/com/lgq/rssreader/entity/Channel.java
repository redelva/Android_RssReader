package com.lgq.rssreader.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

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
}
