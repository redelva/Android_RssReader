package com.lgq.rssreader.entity;

import java.util.Date;

import com.lgq.rssreader.enums.RssAction;

public class SyncState {
	public int SyncStateId;	        
    public String BlogOriginId;        	        
    public String ChannelId;        
    public RssAction Status;	        
    public String Tag;
    public Date TimeStamp;
}