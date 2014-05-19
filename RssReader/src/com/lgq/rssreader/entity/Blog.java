package com.lgq.rssreader.entity;

import java.io.Serializable;
import java.util.Date;

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
}
