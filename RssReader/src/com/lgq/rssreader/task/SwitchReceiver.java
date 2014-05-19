package com.lgq.rssreader.task;

import com.lgq.rssreader.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SwitchReceiver extends BroadcastReceiver{
	@Override
    public void onReceive(Context context, Intent intent) {             
        Intent it = new Intent(context,MainActivity.class);
        //it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//这个必须加
        context.startActivity(it);
    }
}
