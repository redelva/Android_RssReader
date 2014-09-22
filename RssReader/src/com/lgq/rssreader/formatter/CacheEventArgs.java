package com.lgq.rssreader.formatter;
import org.jsoup.nodes.Element;
import com.lgq.rssreader.entity.Blog;

public class CacheEventArgs
{
    public CacheEventArgs(Blog b, Element origin, Element cache, int completeIndex, int total)
    {
        Blog = b;
        Origin = origin;
        Cache = cache;
        Total = total;
        CompleteIndex = completeIndex;
    }

    public Blog Blog;
    public Element Origin;
    public Element Cache;    
    public int CompleteIndex;
    public int Total;
}