package com.determinato.feeddroid.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.adapters.FolderListCursorAdapter;
import com.determinato.feeddroid.dao.ChannelDao;
import com.determinato.feeddroid.dao.FolderItemDao;
import com.determinato.feeddroid.provider.FeedDroid;
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
	
	public static final int DELETE_ID = R.id.remove_channel;
	public static final int REFRESH_ID = Menu.FIRST + 2;
	public static final int REFRESH_ALL_ID = Menu.FIRST + 3;
	public static final int EDIT_ID = R.id.edit_channel;
	public static final int PREFS_ID = Menu.FIRST;
	public static final int SEARCH_ID = R.id.menu_search;
	private static final int SHOW_PREFERENCES = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_list);
		
		Cursor folderCursor = managedQuery(
				FeedDroid.Folders.CONTENT_URI, FOLDER_PROJECTION, "parent_id=1", null, null);
		
		Cursor channelCursor = managedQuery(
				FeedDroid.Channels.CONTENT_URI, CHANNEL_PROJECTION, "folder_id=1", null, null);
		

		FolderListCursorAdapter adapter = new FolderListCursorAdapter(this, folderCursor, channelCursor);
		getListView().setAdapter(adapter);
		registerForContextMenu(getListView());
		
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		int itemType = ((FolderListRow) v).getItemType();
		Intent intent = null;
		
		FolderItemDao data = (FolderItemDao) getListView().getItemAtPosition(position);
		
		if (data instanceof ChannelDao) {
			Long channelId = ((ChannelDao)data).getId();
			Uri uri = Uri.withAppendedPath(FeedDroid.Posts.CONTENT_URI_LIST, channelId.toString());
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		
		Log.d(TAG, "menu item pressed: " + item.getItemId());
		switch(item.getItemId()) {
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
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		FolderItemDao backing = ((FolderListRow)info.targetView).getBacking();
		
		switch (item.getItemId()) {
		case EDIT_ID:
			
			if (((FolderListRow)info.targetView).getItemType() == FolderListRow.CHANNEL_VIEW) {
				long id = backing.getId();
				startActivity(new Intent(Intent.ACTION_EDIT, ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, id)));
			}
			return true;
		case DELETE_ID:
			removeChannel(info.id);
			return true;
		
		default:
			return false;
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
}
