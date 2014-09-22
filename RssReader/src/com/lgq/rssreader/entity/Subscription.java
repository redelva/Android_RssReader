package com.lgq.rssreader.entity;

import java.util.Date;
import java.util.List;

public class Subscription {
	public String Id ;
    public String Title;
    public List<Tag> Categories;
    public String SortId;
    public Date FirstItemMSEC;
    public static String TOKEN = "Subscription";
}
