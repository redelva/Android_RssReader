package com.lgq.rssreader.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.lgq.rssreader.R;
import com.lgq.rssreader.core.ReaderApp;

public class DateHelper {
	/**
	 * 时间间隔计算
	 * 
	 */
	public static String getDaysBeforeNow(Date date) {
		long sysTime = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		long ymdhms = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(date));
		String strYear = ReaderApp.getAppContext().getString(R.string.year);
		String strMonth = ReaderApp.getAppContext().getString(R.string.month);
		String strDay = ReaderApp.getAppContext().getString(R.string.day);
		String strHour = ReaderApp.getAppContext().getString(R.string.hour);
		String strMinute = ReaderApp.getAppContext().getString(R.string.min);
		try {
			if (ymdhms == 0) {
				return "";
			}
			long between = (sysTime / 10000000000L) - (ymdhms / 10000000000L);
			if (between > 0) {
				return between + strYear;
			}
			between = (sysTime / 100000000L) - (ymdhms / 100000000L);
			if (between > 0) {
				return between + strMonth;
			}
			between = (sysTime / 1000000L) - (ymdhms / 1000000L);
			if (between > 0) {
				return between + strDay;
			}
			between = (sysTime / 10000) - (ymdhms / 10000);
			if (between > 0) {
				return between + strHour;
			}
			between = (sysTime / 100) - (ymdhms / 100);
			if (between > 0) {
				return between + strMinute;
			}
			return "1" + strMinute;
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * 将时间转换为中文
	 * @param datetime
	 * @return
	 */
	public static String DateToChineseString(Date datetime){
		Date today=new Date();
		long   seconds   =   (today.getTime()-   datetime.getTime())/1000; 

		long year=	seconds/(24*60*60*30*12);// 相差年数
		long   month  =   seconds/(24*60*60*30);//相差月数
		long   date   =   seconds/(24*60*60);     //相差的天数 
		long   hour   =   (seconds-date*24*60*60)/(60*60);//相差的小时数 
		long   minute   =   (seconds-date*24*60*60-hour*60*60)/(60);//相差的分钟数 
		long   second   =   (seconds-date*24*60*60-hour*60*60-minute*60);//相差的秒数 

		if(year>0){
			return year + ReaderApp.getAppContext().getString(R.string.year);
		}
		if(month>0){
			return month + ReaderApp.getAppContext().getString(R.string.month);
		}
		if(date>0){
			return date + ReaderApp.getAppContext().getString(R.string.day);
		}
		if(hour>0){
			return hour + ReaderApp.getAppContext().getString(R.string.hour);
		}
		if(minute>0){
			return minute + ReaderApp.getAppContext().getString(R.string.min);
		}
		if(second>0){
			return second + ReaderApp.getAppContext().getString(R.string.second);
		}
		return "未知时间";
	}
	
	public static Date ConvertStampToDateTime(long unixStamp)
    {
        return new Date(unixStamp);
    }

    public static long ConvertDateTimeToStamp(Date time)
    {
        return time.getTime()/1000;
    }

    public static long ConvertDateTimeToStampMilliSeconds(Date time)
    {
        return time.getTime();
    }

    public static Date ConvertMsecStampToDateTime(long mSecStamp)
    {        
        Date time = new Date(mSecStamp * 1000);
        return time;
    }
    
    public static Date ParseDate(String str){
		SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
		Date addTime = null;
		try {
			addTime = dateFormat.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return addTime;
	}
	
	public static String ParseDateToString(Date date){
		return ParseDateToString(date,"yyyy-MM-dd HH:mm:ss");
	}
	
	public static String ParseDateToString(Date date,String format){
		SimpleDateFormat dateFormat =new SimpleDateFormat(format);

		return dateFormat.format(date);
	}
	
	public static Date ParseUTCDate(String str){
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.CHINA);
		try {
			Date date = formatter.parse(str);

			return date;
		} catch (ParseException e) {			
			try{
				SimpleDateFormat formatter2=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.CHINA);
				Date date2 = formatter2.parse(str);

				return date2;
			}catch(ParseException ex){
				return null;
			}
		}		
	}
}
