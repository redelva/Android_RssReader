package com.lgq.rssreader.entity;

import java.util.Date;

public class ImageRecord {
	public int ImageRecordId;
	public String BlogId;
    public String OriginUrl;	        
    public String StoredName;
    public String Extension;
    public Date TimeStamp;
	public double Size;
	
	@Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (!(obj instanceof ImageRecord))
            return false;

        ImageRecord record = (ImageRecord) obj;

        return record.OriginUrl.equals(OriginUrl);
    }

	@Override
    public int hashCode()
    {
        return OriginUrl.hashCode();
    }
}
