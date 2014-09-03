package com.lgq.rssreader.entity;

import java.util.Locale;

import android.graphics.Color;

import com.lgq.rssreader.enums.DownloadMode;
import com.lgq.rssreader.enums.Formatter;
import com.lgq.rssreader.enums.Theme;

public class RssSettings
{
    public RssSettings()
    {
//    	if (Locale.getDefault().getCountry() == "zh-CN")
//        {
//            EnableSinaWeibo = true;
//            EnableRenRen = true;
//            EnableTecentWeibo = true;
//            EnableEvernote = true;
//            EnableDouban = true;
//            EnableTwitter = false;
//            EnableFacebook = false;
//            EnableInstapaper = false;
//            EnablePocket = false;
//        }
//        else
//        {
//            EnableSinaWeibo = false;
//            EnableRenRen = false;
//            EnableTecentWeibo = false;
//            EnableEvernote = true;
//            EnableDouban = false;
//            EnableTwitter = true;
//            EnableFacebook = true;
//            EnableInstapaper = true;
//            EnablePocket = true;
//        }
    }

    public String FontColor;
    public String BackgroundColor;
    public int Brightness = 100;
    public Theme Theme = com.lgq.rssreader.enums.Theme.Default;
    public int Font = -1;
    public int NumPerRequest = 30;
    public Formatter Formatter = com.lgq.rssreader.enums.Formatter.Description;
    public int FontSize = 14;//int.Parse(Common.Helper.GetDescription<FontSize>(FontSize.Small));        
    public boolean AutoSync = true;
    public boolean ShowAllFeeds = true;
    public boolean ShowAllItems = false;
    public boolean UseDefaultIcon = true;
    public boolean EnableSyncOnStart = true;
    public boolean EnableSeperateClip = true;    
    public boolean MarkAsReadWhenView = true;
    public boolean EnableShakeToUpdate = false;
    public int ShakeSpeed = 50;    
    public boolean ConfirmExit = false;
    public boolean AskBeforeMarkAllAsRead = true;
    public boolean FitImageToWidth = true;    
    public boolean EnableVibrate = true;
    public boolean EnableSound = true;
    public Channel PicFolder;
    public boolean NoImageMode = false;
    public boolean FullScreen = false;
    public boolean EnableCacheImage = true;
    public boolean EnableRotation = false;
    //public Theme Theme = Theme.Default;
    public int ImgLoadNum = 10;
    public boolean EnbaleShakeToUpdate = false;    
    public boolean SyncOnlyWifi = true;
    public int LineHeight = 150;
    
    public DownloadMode DownloadPolice = com.lgq.rssreader.enums.DownloadMode.Period;
    public boolean DownloadOnlyWifi = true;    
    public int DownloadPeriod = 3;
    public String DownloadTime = "18:00";
    public int CacheSize = 400;
    
    
//    public boolean EnableSinaWeibo = true;
//    public boolean EnableRenRen = true;
//    public boolean EnableTwitter = true;
//    public boolean EnableLive = true;
//    public boolean EnableTecentWeibo = false;
//    public boolean EnableNFC = true;
//    public boolean EnableEvernote = true;
//    public boolean EnableDouban = false;
//    public boolean EnableFacebook = false;
//    public boolean EnableInstapaper = false;
//    public boolean EnablePocket = false;    
}