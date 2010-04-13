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

package com.determinato.feeddroid.activity;

import java.util.HashMap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.parser.RssParser;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.DownloadManager;
import com.determinato.feeddroid.view.ChannelListRow;



public class ChannelListActivity extends ListActivity {
	public static final String TAG = "ChannelListActivity";
	public static final String TAG_PREFS = "FeedDroid";
	
	public static final int DELETE_ID = R.id.remove_channel;
	public static final int REFRESH_ID = Menu.FIRST + 2;
	public static final int REFRESH_ALL_ID = Menu.FIRST + 3;
	public static final int EDIT_ID = R.id.edit_channel;
	public static final int PREFS_ID = Menu.FIRST;
	public static final int SEARCH_ID = R.id.search_text;
	
	private Cursor mCursor;
	private DownloadManager mDownloadManager;
	private NotificationManager mNotificationManager;
	private SharedPreferences mPreferences;
	
	private static final String[] PROJECTION = new String[] {
		FeedDroid.Channels._ID, FeedDroid.Channels.ICON,
		FeedDroid.Channels.TITLE, FeedDroid.Channels.URL
	};
	
	private static final int SHOW_PREFERENCES = 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_list);
        
        Intent intent = getIntent();
        if (intent.getData() == null)
        	intent.setData(FeedDroid.Channels.CONTENT_URI);
        
        if (intent.getAction() == null)
        	intent.setAction(Intent.ACTION_VIEW);
        
        mCursor = managedQuery(getIntent().getData(), PROJECTION, null, null, null);
        
        ListAdapter adapter = new ChannelListAdapter(this, mCursor);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mPreferences = getSharedPreferences(PreferencesActivity.USER_PREFERENCE, MODE_PRIVATE);
        if (mPreferences.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false)) {
        	Log.d(TAG, "autoupdate set to true");
        	AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        	Intent i = new Intent("com.determinato.feeddroid.ACTION_REFRESH_RSS_ALARM");
        	PendingIntent pending = PendingIntent.getBroadcast(this, 0, i, 0);
        	long time = mPreferences.getInt(PreferencesActivity.PREF_UPDATE_FREQ, 0) * 60 * 1000;
        	Log.d(TAG, "update in " + time / 1000 + " seconds.");
        	alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time, time, pending);
        }
        	
        
    }
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.channel_list_menu, menu);
		MenuItem addItem = menu.getItem(0);
		Intent i = new Intent();
		i.setData(FeedDroid.Channels.CONTENT_URI);
		i.setAction(Intent.ACTION_INSERT);
		addItem.setIntent(i);
		menu.add(addItem.getGroupId(), SHOW_PREFERENCES, Menu.NONE, getString(R.string.prefs))
		.setIcon(android.R.drawable.ic_menu_preferences);
		
		MenuItem searchItem = menu.findItem(R.id.menu_search);
		searchItem.setAlphabeticShortcut(SearchManager.MENU_KEY);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final boolean haveItems = mCursor.getCount() > 0;
		menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
		
		if (haveItems) {
			menu.add(Menu.CATEGORY_ALTERNATIVE, REFRESH_ALL_ID, Menu.NONE, "Refresh All")
			//.setIcon(R.drawable.redo);
			.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_share));
			
		}

		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		mNotificationManager.cancel(1);
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.channel_list_context_menu, menu);
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case EDIT_ID:
			startActivity(new Intent(Intent.ACTION_EDIT, ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, info.id)));			
			return true;
		case DELETE_ID:
			removeChannel(info.id);
			return true;
		
		default:
			return false;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String action = getIntent().getAction();
		
		if (action.equals(Intent.ACTION_PICK) ||
			action.equals(Intent.ACTION_GET_CONTENT)) {
				setResult(RESULT_OK, getIntent());
		} else {
			Uri uri =
				ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI_LIST, id);
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case REFRESH_ALL_ID:
			refreshAllChannels();
			return true;
		case PREFS_ID:
			Intent i = new Intent(this, PreferencesActivity.class);
			startActivityForResult(i, SHOW_PREFERENCES);
			return true;
		case SEARCH_ID:
			onSearchRequested();
		}
		return super.onOptionsItemSelected(item);
	}

	private final void refreshAllChannels() {
 		if (mCursor.moveToFirst() == false) {
			Log.d(TAG, "Move to beginning of cursor failed.");
			return;
		}
		do {
			Log.d(TAG, "Refreshing all channels");
			refreshChannel();
			
		} while (mCursor.moveToNext() == true);

	}
	
	
	private final void refreshChannel() {
		Handler mRefreshHandler = new Handler();
		if (mDownloadManager == null) {
			mDownloadManager = new DownloadManager(mRefreshHandler);
		}	
		
		long channelId =
			mCursor.getInt(mCursor.getColumnIndex(FeedDroid.Channels._ID));
			
		String url = mCursor.getString(mCursor.getColumnIndex(FeedDroid.Channels.URL));
			
		ChannelListRow row =
			((ChannelListAdapter) getListAdapter()).getViewByRowId(channelId);
			
		if (row != null) {
			Runnable refresh = new RefreshRunnable(mRefreshHandler, row, channelId, url);
			mDownloadManager.schedule(refresh);
		}
		
	}
	
	private void removeChannel(final long channelId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to remove this channel?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ContentResolver resolver = getContentResolver();
					resolver.delete(FeedDroid.Posts.CONTENT_URI, "channel_id=" + channelId, null);
					resolver.delete(FeedDroid.Channels.CONTENT_URI, "_id=" + channelId, null);
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
		builder.create().show();
	}
	
	private static class ChannelListAdapter extends CursorAdapter implements Filterable {
		private HashMap<Long, ChannelListRow> rowMap;
		
		
		public ChannelListAdapter(Context ctx, Cursor c) {
			super(ctx, c);
			
			rowMap = new HashMap<Long, ChannelListRow>();
		}
		
		protected void updateRowMap(Cursor c, ChannelListRow row) {
			long channelId =
				c.getLong(c.getColumnIndex(FeedDroid.Channels._ID));
			rowMap.put(new Long(channelId), row);
		}
		
		@Override 
		public void bindView(View v, Context ctx, Cursor c) {
			ChannelListRow row = (ChannelListRow) v;
			row.bindView(c);
			updateRowMap(c, row);
		}
		
		@Override
		public View newView(Context ctx, Cursor c, ViewGroup parent) {
			ChannelListRow row = new ChannelListRow(ctx);
			row.bindView(c);
			updateRowMap(c, row);
			return row;
		}
		
		public ChannelListRow getViewByRowId(long id) {
			return rowMap.get(id);
		}
		
	}
	
	private class RefreshRunnable implements Runnable {
		private Handler mHandler;
		private ChannelListRow mRow;
		private long mChannelId;
		private String mUrl;
		
		public RefreshRunnable(Handler handler, ChannelListRow row, long id, String url) {
			mHandler = handler;
			mRow = row;
			mChannelId = id;
			mUrl = url;
		}
		
		public void run() {
			mHandler.post(new Runnable() {
				public void run() {
					mRow.startRefresh();
				}
			});
			
			try {
				new RssParser(getContentResolver()).syncDb(mHandler, mChannelId, mUrl);
				
			} catch (Exception e) {
				Log.e(TAG, Log.getStackTraceString(e));
				
			}
			
			mHandler.post(new Runnable() {
				public void run() {
					mRow.finishRefresh(mChannelId);
					
				}
			});
		}
	}

	
	

	
}

