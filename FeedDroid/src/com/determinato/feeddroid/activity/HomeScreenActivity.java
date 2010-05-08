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

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.adapters.FolderListCursorAdapter;
import com.determinato.feeddroid.dao.ChannelDao;
import com.determinato.feeddroid.dao.FolderDao;
import com.determinato.feeddroid.dao.FolderItemDao;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.service.FeedDroidUpdateService;
import com.determinato.feeddroid.view.FolderListRow;

public class HomeScreenActivity extends ListActivity {
	private static final String TAG = "HomeScreenActivity";
	public static final String TAG_PREFS = "FeedDroid";
	private static final String[] FOLDER_PROJECTION = {FeedDroid.Folders._ID, 
		FeedDroid.Folders.NAME, FeedDroid.Folders.PARENT_ID};
	private static final String[] CHANNEL_PROJECTION = {
		FeedDroid.Channels._ID, FeedDroid.Channels.TITLE, FeedDroid.Channels.URL,
		FeedDroid.Channels.ICON, FeedDroid.Channels.FOLDER_ID
	};

	public static final int ADD_CHANNEL_ID = R.id.menu_new_channel;
	public static final int ADD_FOLDER_ID = R.id.menu_new_folder;
	public static final int DELETE_ID = R.id.remove_channel;
	public static final int REFRESH_ID = Menu.FIRST + 2;
	public static final int REFRESH_ALL_ID = Menu.FIRST + 3;
	public static final int EDIT_ID = R.id.edit_channel;
	public static final int MOVE_ID = R.id.move_channel;
	public static final int PREFS_ID = Menu.FIRST;
	public static final int SEARCH_ID = R.id.menu_search;
	public static final int FOLDER_ID = R.id.menu_new_folder;
	private static final int SHOW_PREFERENCES = 1;
	private static final int SHOW_MOVE = 2;
	
	private Cursor mFolderCursor;
	private Cursor mChannelCursor;
	private FolderListCursorAdapter adapter;
	private NotificationManager mNotificationManager;
	private SharedPreferences mPreferences;
	private ContentResolver mResolver;
	private FeedDroidUpdateService mServiceBinder;
	private int mSelectedRow;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_list);
		
		mFolderCursor = managedQuery(
				FeedDroid.Folders.CONTENT_URI, FOLDER_PROJECTION, "parent_id=1", null, null);
		
		mChannelCursor = managedQuery(
				FeedDroid.Channels.CONTENT_URI, CHANNEL_PROJECTION, "folder_id=1", null, null);
		
		registerForContextMenu(getListView());
		mResolver = getContentResolver();
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
	public void onResume() {
		super.onResume();
		mNotificationManager.cancel(1);
		mFolderCursor.requery();
		mChannelCursor.requery();
		
		Intent bindIntent = new Intent(this, FeedDroidUpdateService.class);
		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
		
		initAdapter();

	}
	
	private void initAdapter() {
		getListView().setAdapter(new FolderListCursorAdapter(this, mFolderCursor, mChannelCursor));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = null;
		
		FolderItemDao data = (FolderItemDao) getListView().getItemAtPosition(position);
		
		if (data instanceof ChannelDao) {
			Long channelId = ((ChannelDao)data).getId();
			Uri uri = Uri.withAppendedPath(FeedDroid.Posts.CONTENT_URI_LIST, channelId.toString());
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		} else if (data instanceof FolderDao) {
			Long folderId = ((FolderDao)data).getId();
			Uri uri = Uri.withAppendedPath(FeedDroid.Folders.CONTENT_URI, folderId.toString());
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}
		
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
		
		menu.add(Menu.CATEGORY_ALTERNATIVE, REFRESH_ALL_ID, Menu.NONE, "Refresh All")
			.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_share));
		

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.channel_list_menu, menu);

		menu.add(0, SHOW_PREFERENCES, Menu.NONE, getString(R.string.prefs))
		.setIcon(android.R.drawable.ic_menu_preferences);
		
		MenuItem searchItem = menu.findItem(R.id.menu_search);
		searchItem.setAlphabeticShortcut(SearchManager.MENU_KEY);
		
		return true;
	}
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		
		
		switch(item.getItemId()) {
		case ADD_FOLDER_ID:
			i = new Intent(this, FolderAddActivity.class);
			i.setAction(Intent.ACTION_INSERT);
			i.setData(Uri.parse("vnd.android.cursor.dir/vnd.feeddroid.folder"));
			startActivityForResult(i, RESULT_OK);
			return true;
		case ADD_CHANNEL_ID:
			i = new Intent(this, ChannelAddActivity.class);
			i.setAction(Intent.ACTION_INSERT);
			i.setData(Uri.parse("vnd.android.cursor.dir/vnd.feeddroid.channel"));
			i.putExtra("folderId", 1L);
			startActivityForResult(i, RESULT_OK);
			return true;
		case REFRESH_ALL_ID:
			updateAllChannels();
			return true;
		case PREFS_ID:
			i = new Intent(this, PreferencesActivity.class);
			startActivityForResult(i, SHOW_PREFERENCES);
			return true;
		case SEARCH_ID:
			onSearchRequested();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.channel_list_context_menu, menu);
	
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		FolderItemDao backing = ((FolderListRow) info.targetView).getBacking();
		if (backing instanceof FolderDao) {
			MenuItem item = menu.findItem(EDIT_ID);
			item.setVisible(false);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		FolderItemDao backing = ((FolderListRow)info.targetView).getBacking();
		long id = backing.getId();
		switch (item.getItemId()) {
		case EDIT_ID:
			
			if (((FolderListRow)info.targetView).getItemType() == FolderListRow.CHANNEL_VIEW) {
				startActivity(new Intent(Intent.ACTION_EDIT,
						ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, id)));
			}
			return true;
		case DELETE_ID:
			if (((FolderListRow)info.targetView).getItemType() == FolderListRow.CHANNEL_VIEW) {
				removeChannel(id);
			} else if (((FolderListRow)info.targetView).getItemType() == FolderListRow.FOLDER_VIEW) {
				removeFolder(id);
			}
			return true;
		case MOVE_ID:
			mSelectedRow = info.position;
			showDialog(SHOW_MOVE);
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		switch(id) {
		case SHOW_MOVE:
			LayoutInflater inflater = LayoutInflater.from(this);
			View moveDialogView = inflater.inflate(R.layout.move_item, null);
			AlertDialog.Builder moveDialog = new AlertDialog.Builder(this);
			moveDialog.setTitle(getString(R.string.move_channel));
			moveDialog.setView(moveDialogView);
			
			final Spinner folderSpinner = (Spinner) moveDialogView.findViewById(R.id.move_channel_spinner);
			Cursor c = managedQuery(FeedDroid.Folders.CONTENT_URI, 
					new String[] {FeedDroid.Folders._ID, FeedDroid.Folders.NAME}, 
					null, null, null);
			
			String[] columns = new String[] {FeedDroid.Folders.NAME};
			int[] to = new int[] {android.R.id.text1};
			
			SimpleCursorAdapter adapter = 
				new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, 
						c, columns, to);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			folderSpinner.setAdapter(adapter);
			
			moveDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					ContentValues values = new ContentValues();
					long folderId = folderSpinner.getSelectedItemId();
					Log.d(TAG, "selected row: " + mSelectedRow);
					FolderItemDao dao = (FolderItemDao) getListView().getItemAtPosition(mSelectedRow);
					
					
					long id = dao.getId();
					Log.d(TAG, "id: " + id);
					
					if (dao instanceof FolderDao) {
						values.put(FeedDroid.Folders.PARENT_ID, folderId);
						getContentResolver().update(FeedDroid.Folders.CONTENT_URI, values, "_id=" + id, null);
					} else {
						values.put(FeedDroid.Channels.FOLDER_ID, folderId);
						getContentResolver().update(FeedDroid.Channels.CONTENT_URI, values, "_id" + id, null);
					}
				}
			});
			
			moveDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			
			return moveDialog.create();
		}
		
		initAdapter();
		return null;
	}
	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			switch(requestCode) {
			case SHOW_PREFERENCES:
				mFolderCursor.requery();
				mChannelCursor.requery();
		        adapter = new FolderListCursorAdapter(this, mFolderCursor, mChannelCursor);
		        setListAdapter(adapter);
				
				break;
			}
		}
	}
	private void removeFolder(final long folderId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to remove this folder?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ContentResolver resolver = getContentResolver();
					resolver.delete(FeedDroid.Folders.CONTENT_URI, "_id=" + folderId, null);
					
					// Move all channels in this folder to the home folder.
					ContentValues values = new ContentValues();
					values.put("folder_id", 1);
					resolver.update(FeedDroid.Channels.CONTENT_URI, values, "folder_id=" + folderId, null);
					onResume();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
		builder.create().show();
		
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
					onResume();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
		builder.create().show();
	}
	
	private void updateAllChannels() {
		mServiceBinder.updateAllChannels();
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mServiceBinder = ((FeedDroidUpdateService.ServiceBinder) service).getService();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mServiceBinder = null;
		}
	};


	@Override
	protected void onStop() {
		unbindService(mConnection);
		super.onStop();
	}
}
