package com.determinato.feeddroid.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.activity.ChannelListActivity;
import com.determinato.feeddroid.activity.PreferencesActivity;
import com.determinato.feeddroid.parser.RssParser;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.DownloadManager;

public class FeedDroidUpdateService extends Service {
	private static final String TAG = "FeedDroidUpdateService";
	private static final String ALARM_ACTION = "com.determinato.feeddroid.ACTION_REFRESH_RSS_ALARM";
	private NotificationManager mNotificationMgr;
	private SharedPreferences mPreferences;
	private boolean mHasUpdates;
	private PendingIntent mPending;
	private AlarmManager mAlarmManager;
	private int mUpdateFrequency;	// measured in minutes
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(ALARM_ACTION);
		mPending = PendingIntent.getBroadcast(this, 0, intent, 0);
		mPreferences = getSharedPreferences(PreferencesActivity.USER_PREFERENCE, Activity.MODE_PRIVATE);
		mUpdateFrequency = mPreferences.getInt(PreferencesActivity.PREF_UPDATE_FREQ, 15);
	}
	
	// Pre-2.0
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (mPreferences.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false))
			doStart(intent, startId);
		else {
			mAlarmManager.cancel(mPending);
			stopSelf();
		}
	}


	void doStart(Intent intent, int startId) {
		Cursor c = getContentResolver().query(FeedDroid.Channels.CONTENT_URI, 
				new String[] {FeedDroid.Channels._ID, FeedDroid.Channels.URL}, null, null, null);
		
		if (c.getCount() == 0) {
			c.close();
			return;
		}
		
		c.moveToFirst();
		
		do {
			long id = c.getLong(c.getColumnIndex(FeedDroid.Channels._ID));
			String url = c.getString(c.getColumnIndex(FeedDroid.Channels.URL));
			parseChannelRss(id, url);
		} while(c.moveToNext());
		c.close();
		if (mHasUpdates) 
			sendNotification();

		
		stopSelf();
	}
	
	private void scheduleUpdate() {
		int alarmType = AlarmManager.ELAPSED_REALTIME;
		long time = SystemClock.elapsedRealtime() + mUpdateFrequency * 60 * 1000;
		mAlarmManager.set(alarmType, time, mPending);
	}
	
	private void sendNotification() {
		mNotificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		int icon = R.drawable.rss_status_bar;
		String tickerTxt = getString(R.string.updates_available);
		String titleTxt = getString(R.string.app_name);
		Notification notification = new Notification(icon, tickerTxt, System.currentTimeMillis());
		notification.ledOffMS = 0;
		notification.ledOnMS = 1;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.flags |= Notification.DEFAULT_SOUND;
		long[] vibrate = new long[] {1000, 1000};
		notification.vibrate = vibrate;
		Intent appIntent = new Intent(getApplicationContext(), ChannelListActivity.class);
		PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), titleTxt, tickerTxt, pending);
		mNotificationMgr.notify(1, notification);
	}
	
	private void parseChannelRss(final long id, final String url) {
		Handler handler = new Handler();
		DownloadManager manager = new DownloadManager(handler);
		Thread t = new Thread() {
			public void run() {
				Cursor p = getContentResolver().query(FeedDroid.Posts.CONTENT_URI, 
						new String[] {FeedDroid.Posts._ID}, "channel_id=" + id, null, null);
				if (p.getCount() == 0) {
					
					p.close();
					return;
				}
				int oldPostCount = p.getCount();
				
				try {
					new RssParser(getContentResolver()).syncDb(id, url);
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
				
			}
		};
		manager.schedule(t);
	}
}