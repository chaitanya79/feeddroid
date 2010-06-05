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
import android.os.IBinder;
import android.util.Log;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.activity.HomeScreenActivity;
import com.determinato.feeddroid.activity.PreferencesActivity;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.FeedDroidUtils;

/**
 * Service called by a system Alarm to update RSS feeds in the background.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class FeedDroidUpdateService extends Service {
	private static final String TAG = "FeedDroidUpdateService";
	private static final String ALARM_ACTION = "com.determinato.feeddroid.ACTION_REFRESH_RSS_ALARM";
	private NotificationManager mNotificationMgr;
	private SharedPreferences mPreferences;
	private PendingIntent mPending;
	private AlarmManager mAlarmManager;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate() {
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(ALARM_ACTION);
		mPending = PendingIntent.getBroadcast(this, 0, intent, 0);
		mPreferences = getSharedPreferences(PreferencesActivity.USER_PREFERENCE, Activity.MODE_PRIVATE);
		
	}
	
	// Pre-2.0
	/**
	 * {@inheritDoc}
	 */
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


	/**
	 * Starts the service.
	 * @param intent
	 * @param startId
	 */
	void doStart(Intent intent, int startId) {
		Log.d(TAG, "Update serivce started");
		if (FeedDroidUtils.isUpdating())
			return;
		
		FeedDroidUtils.setUpdating(true);
		FeedDroidUtils.setNewUpdates(false);
		String[] projection = new String[] {FeedDroid.Channels._ID, FeedDroid.Channels.URL};
		Cursor cCursor = getContentResolver().query(FeedDroid.Channels.CONTENT_URI, 
				projection, null, null, null);

		try {
			cCursor.moveToFirst();
		
			do {
				long id = cCursor.getLong(cCursor.getColumnIndex(FeedDroid.Channels._ID));
				String url = cCursor.getString(cCursor.getColumnIndex(FeedDroid.Channels.URL));
				Intent updateService = new Intent(this, RssParserService.class);
				updateService.putExtra("id", id);
				updateService.putExtra("url", url);
				startService(updateService);
			} while(cCursor.moveToNext());
		} finally {
			cCursor.close();
		}
			
		if (FeedDroidUtils.hasUpdates()) 
			sendNotification();

		FeedDroidUtils.setUpdating(false);
		stopSelf();
	}
	
	/**
	 * Sends a notification of new RSS posts to the NotificationManager.
	 */
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


}