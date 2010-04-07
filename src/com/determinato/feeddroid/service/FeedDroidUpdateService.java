package com.determinato.feeddroid.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.activity.ChannelListActivity;
import com.determinato.feeddroid.parser.RssParser;
import com.determinato.feeddroid.provider.FeedDroid;

public class FeedDroidUpdateService extends Service {
	private static final String TAG = "FeedDroidUpdateService";
	private NotificationManager mNotificationMgr;
	private RssUpdaterTask mTask;
	private ContentResolver mResolver;
	private boolean mHasUpdates;
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
			mTask.cancel(true);
		}
	}
	
	// Pre-2.0
	@Override
	public void onStart(Intent intent, int startId) {
		doStart(intent, startId);
		super.onStart(intent, startId);
		
	}

	// 2.0+
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		doStart(intent, startId);
		return super.onStartCommand(intent, flags, startId);
	}
	
	void doStart(Intent intent, int startId) {
		mResolver = getContentResolver();
		Cursor c = mResolver.query(FeedDroid.Channels.CONTENT_URI,
				new String[] {FeedDroid.Channels._ID, FeedDroid.Channels.URL},
				null, null, null);
		if (c.getCount() == 0)
			return;
		c.moveToFirst();
		do {
			RssFeed feed = new RssFeed();
			feed.id = c.getLong(c.getColumnIndex(FeedDroid.Channels._ID));
			feed.url = c.getString(c.getColumnIndex(FeedDroid.Channels.URL));
			(mTask = new RssUpdaterTask()).execute(feed);
		} while (c.moveToNext());
		
		if (mHasUpdates) {
			mNotificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification();
			int icon = R.drawable.rss_status_bar;
			String tickerTxt = getString(R.string.updates_available);
			String titleTxt = getString(R.string.app_name);
			Intent startActivity = new Intent(this, ChannelListActivity.class);
			PendingIntent pi = PendingIntent.getActivity(this, 0, startActivity, 0);
			notification.setLatestEventInfo(getApplicationContext(), titleTxt, tickerTxt, pi);
			mNotificationMgr.notify(1, notification);
			
		}
	}
	
	static void scheduleUpdates(Context ctx) {
		final Intent intent = new Intent(ctx, FeedDroidUpdateService.class);
		final PendingIntent pi = PendingIntent.getService(ctx, 0, intent, 0);
		
		final AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pi);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, pi);
	}
	
	private class RssUpdaterTask extends AsyncTask<RssFeed, Void, Void> {

		@Override
		public Void doInBackground(RssFeed... params) {
			RssFeed feed = params[0];
			Cursor p = mResolver.query(FeedDroid.Posts.CONTENT_URI, 
					new String[] {FeedDroid.Posts._ID}, "channel_id=" + feed.id, null, null);
			int oldPostCount = p.getCount();
			Handler handler = new Handler();
			try {
				new RssParser(mResolver).syncDb(handler, feed.id, feed.url);
			} catch(Exception e) {
				Log.e("RssUpdaterTask", Log.getStackTraceString(e));
			}
			if (p.requery()) {
				int newPostCount = p.getCount();
				if (newPostCount > oldPostCount) {
					mHasUpdates = true;
				}
			}
			p.close();
			return null;
		}

		
	}
	
	private class RssFeed {
		String url;
		long id;
	}
}