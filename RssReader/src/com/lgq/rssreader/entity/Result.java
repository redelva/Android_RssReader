package com.lgq.rssreader.entity;

import java.io.Serializable;

public class Result implements Serializable,Comparable<Result>  {

	public boolean IsSubscribed;
	public String Title;
	public String StreamId;
	public String SubscriptCount;
	
	@Override
    public boolean equals(Object t){
    	if(t == null){
    		return false;
    	}
    	
    	if(!(t instanceof Result)){
    		return false;
    	}
    	
    	Result tmp = (Result)t;
    	
    	return StreamId.equals(tmp.StreamId);
    }
	
	@Override
    public int hashCode(){
    	return StreamId.hashCode();
    }
	
	@Override
	public int compareTo(Result arg0) {

		return Integer.valueOf(SubscriptCount) - Integer.valueOf(arg0.SubscriptCount);
	}

}
