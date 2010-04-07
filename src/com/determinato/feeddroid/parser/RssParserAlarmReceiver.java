package com.determinato.feeddroid.parser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.determinato.feeddroid.service.FeedDroidUpdateService;

public class RssParserAlarmReceiver extends BroadcastReceiver {
	public static final String ACTION_REFRESH_CHANNELS =
		"com.determinato.feeddroid.ACTION_REFRESH_CHANNELS";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startIntent = new Intent(context, FeedDroidUpdateService.class);
		context.startService(startIntent);
	}

}
