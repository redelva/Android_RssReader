package com.lgq.rssreader.entity;

public class Tag {
	public String Id ;
    public String Label;
    public String SortId;
    public static String TOKEN = "Tags";
    
    @Override
    public boolean equals(Object t){
    	if(t == null){
    		return false;
    	}
    	
    	if(!(t instanceof Tag)){
    		return false;
    	}
    	
    	Tag tmp = (Tag)t;
    	
    	return Id.equals(tmp.Id) && Label.equals(tmp.Label);
    }
    
    @Override
    public int hashCode(){
    	return Id.hashCode();
    }
}
