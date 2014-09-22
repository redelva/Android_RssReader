package com.lgq.rssreader.share;

import android.app.Activity;

import com.lgq.rssreader.entity.Blog;

public interface BlogShare<T>{
    void login(Activity page, String apiKey, String secretKey, ShareHandler<T> handler);        
    void share(Blog blog, T token, ShareHandler<T> handler);
}

