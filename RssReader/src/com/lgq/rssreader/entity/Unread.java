package com.lgq.rssreader.entity;

import java.util.Date;
import java.util.List;

public class Unread {
    public int Max;	        
    public List<UnReadCount> Unreads;
    public static String TOKEN = "UnReadCounts";    
}
