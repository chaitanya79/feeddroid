package com.determinato.feeddroid.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.activity.ChannelListActivity;
import com.determinato.feeddroid.parser.RssParser;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.DownloadManager;

public class FeedDroidUpdateService extends Service {
	private static final String TAG = "FeedDroidUpdateService";
	private static final String[] CHANNEL_PROJECTION = new String[] { FeedDroid.Channels._ID, 
		FeedDroid.Channels.TITLE, FeedDroid.Channels.URL, FeedDroid.Channels.ICON,
		FeedDroid.Channels.ICON_URL	};
	private static final String[] POST_PROJECTION = new String[] { FeedDroid.Posts._ID,
		FeedDroid.Posts.CHANNEL_ID, FeedDroid.Posts.TITLE, FeedDroid.Posts.BODY, FeedDroid.Posts.URL,
		FeedDroid.Posts.DATE, FeedDroid.Posts.URL, FeedDroid.Posts.READ, FeedDroid.Posts.STARRED };

	private static final int NOTIFICATION_NEW_POSTS = 1;
	private static final int NOTIFICATION_ERROR = 2;
	
	private NotificationManager mNotificationMgr;
	private final ScheduledExecutorService mScheduler =
		Executors.newScheduledThreadPool(1);
	private ContentResolver mResolver;
	private DownloadManager mDownloadManager;

	@Override
	public void onCreate() {
		mNotificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mResolver = getContentResolver();
		parseRss();
	}
	
	@Override
	public void onDestroy() {
		mNotificationMgr.cancelAll();
	
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void parseRss() {
		
		
	}
	
	private void sendNotification(String tickerText, String noticeTitle, String noticeText) {
		Log.d(TAG, "sending notification");
		Context ctx = getApplicationContext();
		int icon = R.drawable.rss_status_bar;
		long when = System.currentTimeMillis();
		
		Notification notification = new Notification(icon, tickerText, when);
		Intent i = new Intent(this, ChannelListActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
		notification.setLatestEventInfo(ctx, noticeTitle, noticeText, contentIntent);
		mNotificationMgr.notify(1, notification);
		
	}
}
