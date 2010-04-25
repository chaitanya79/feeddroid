/*   
 * Copyright 2010 John R. Hicks
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.activity.HomeScreenActivity;
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
	private ServiceBinder mBinder = new ServiceBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(ALARM_ACTION);
		mPending = PendingIntent.getBroadcast(this, 0, intent, 0);
		mPreferences = getSharedPreferences(PreferencesActivity.USER_PREFERENCE, Activity.MODE_PRIVATE);
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
		Log.d(TAG, "Update serivce started");
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
			FeedDetails feed = new FeedDetails();
			feed.id = id;
			feed.url = url;
			new FeedUpdateTask().execute(feed);
		} while(c.moveToNext());
		c.close();
		if (mHasUpdates) 
			sendNotification();

		
		stopSelf();
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
		Intent appIntent = new Intent(getApplicationContext(), HomeScreenActivity.class);
		PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), titleTxt, tickerTxt, pending);
		mNotificationMgr.notify(1, notification);
	}
	
	public void updateAllChannels() {
		Cursor c = getContentResolver().query(FeedDroid.Channels.CONTENT_URI, 
				new String[] {FeedDroid.Channels._ID, FeedDroid.Channels.URL}, 
				null, null, null);
		c.moveToFirst();
		do {
			long id = c.getLong(c.getColumnIndex(FeedDroid.Channels._ID));
			String url = c.getString(c.getColumnIndex(FeedDroid.Channels.URL));
			FeedDetails feed = new FeedDetails();
			feed.id = id;
			feed.url = url;
			
			new FeedUpdateTask().execute(feed);
		} while(c.moveToNext());
		c.close();
	}
	
	public void updateChannel(long id, String url) {
		FeedDetails feed = new FeedDetails();
		feed.id = id;
		feed.url = url;
		new FeedUpdateTask().execute(feed);
	}
	
	private void parseChannelRss(final long id, final String url) {
		Handler handler = new Handler();
		DownloadManager manager = new DownloadManager(handler);
		Thread t = new Thread() {
			public void run() {
				Log.d(TAG, "Inside update thread for channel: " + id);
				Cursor p = getContentResolver().query(FeedDroid.Posts.CONTENT_URI, 
						new String[] {FeedDroid.Posts._ID}, "channel_id=" + id, null, null);
				
				int oldPostCount = p.getCount();
				
				try {
					new RssParser(getContentResolver()).syncDb(id, url);
				} catch(Exception e) {
					Log.e("RssUpdaterTask", Log.getStackTraceString(e));
				}
				if (p.requery()) {
					int newPostCount = p.getCount();
					if (newPostCount > oldPostCount) {
						sendNotification();
					}
				}
				p.close();
				
			}
		};
		manager.schedule(t);
	}
	
	public class ServiceBinder extends Binder {
		public FeedDroidUpdateService getService() {
			return FeedDroidUpdateService.this;
		}
	}
	
	private class FeedUpdateTask extends AsyncTask<FeedDetails, Void, Void> {
		@Override
		public Void doInBackground(FeedDetails... params) {
			FeedDetails feed = params[0];
						
			Cursor p = getContentResolver().query(FeedDroid.Posts.CONTENT_URI, 
					new String[] {FeedDroid.Posts._ID}, "channel_id=" + feed.id, null, null);
			
			int oldPostCount = p.getCount();
			
			try {
				new RssParser(getContentResolver()).syncDb(feed.id, feed.url);
			} catch(Exception e) {
				Log.e("RssUpdaterTask", Log.getStackTraceString(e));
			}
			if (p.requery()) {
				int newPostCount = p.getCount();
				if (newPostCount > oldPostCount) {
					sendNotification();
				}
			}
			p.close();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d(TAG, "Update finished.");
		}
		
		
	}
	
	private class FeedDetails {
		public long id;
		public String url;
	}
}