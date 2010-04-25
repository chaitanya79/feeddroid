package com.determinato.feeddroid.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.determinato.feeddroid.service.FeedDroidUpdateService;

public class AlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "AlarmReceiver";
	public static final String ACTION_REFRESH_RSS_ALARM =
		"com.determinato.feeddroid.ACTION_REFRESH_RSS_ALARM";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Alarm received");
		
		Intent service = new Intent(context, FeedDroidUpdateService.class);
		context.startService(service);

	}

}
