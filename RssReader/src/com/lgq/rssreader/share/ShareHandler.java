package com.lgq.rssreader.share;

public class ShareHandler<T> {
	public <T> void onCallback(T data, boolean result, String msg){}

	public void onCallback(Exception exception) {}
}
