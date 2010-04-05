package com.determinato.feeddroid.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.determinato.feeddroid.parser.RssParser;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.DownloadManager;

public class FeedDroidUpdateService extends Service {
	private static final String TAG = "FeedDroidUpdateService";
	private static final String[] CHANNEL_PROJECTION = new String[] {
		FeedDroid.Channels._ID, FeedDroid.Channels.TITLE,
		FeedDroid.Channels.URL, FeedDroid.Channels.ICON,
		FeedDroid.Channels.ICON_URL	};
	private static final String[] POST_PROJECTION = new String[] {
		FeedDroid.Posts._ID, FeedDroid.Posts.CHANNEL_ID,
		FeedDroid.Posts.TITLE, FeedDroid.Posts.BODY, FeedDroid.Posts.READ,
		FeedDroid.Posts.URL, FeedDroid.Posts.STARRED	};
	
	private DownloadManager mDownloadManager;
	
	@Override
	public void onCreate() {
		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent i, int startId) {
		
	}
	
	private void updateAllFeeds() {
		ContentResolver resolver = getContentResolver();
		Handler handler = new Handler();
		
		Intent i = new Intent();
		if (i.getData() == null)
			i.setData(FeedDroid.Channels.CONTENT_URI);
		if (i.getAction() == null)
			i.setAction(Intent.ACTION_VIEW);
		
		Cursor c = resolver.query(i.getData(), CHANNEL_PROJECTION, 
				null, null, null);
		
		if (!c.moveToFirst()) // no channels to update
			return;
		
		do {
			if (mDownloadManager == null)
				mDownloadManager = new DownloadManager(handler);
			
			long channelId = c.getLong(c.getColumnIndex(FeedDroid.Channels._ID));
			String url = c.getString(c.getColumnIndex(FeedDroid.Channels.URL));
			Runnable parserThread = new RssParserThread(handler, channelId, url);
			mDownloadManager.schedule(parserThread);
		} while(c.moveToNext());
		
		c.close();
	}
	
	private class RssParserThread implements Runnable {
		private static final String TAG = "RssParserThread";
		private Handler mHandler;
		private long mChannelId;
		private String mUrl;
		
		RssParserThread(Handler handler, long channelId, String url) {
			mHandler = handler;
			mChannelId = channelId;
			mUrl = url;
		}
		
		public void run() {
			try {
				new RssParser(getContentResolver()).syncDb(mHandler, mChannelId, mUrl);
			} catch(Exception e) {
				Log.e(TAG, Log.getStackTraceString(e));
				// TODO send a notification here
			}
		}
	}
}
