package com.determinato.feeddroid.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.determinato.feeddroid.dao.FolderDao;
import com.determinato.feeddroid.dao.FolderItemDao;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.view.FolderListRow;

public class FolderListActivity extends ListActivity {
	private static final String TAG = "FolderListActivity";
	private static final String[] FOLDER_PROJECTION = new String[] {FeedDroid.Folders._ID, 
		FeedDroid.Folders.NAME, FeedDroid.Folders.PARENT_ID};
	private static final String[] CHANNEL_PROJECTION = new String[] {FeedDroid.Channels._ID,
		FeedDroid.Channels.TITLE, FeedDroid.Channels.URL, FeedDroid.Channels.ICON,
		FeedDroid.Channels.FOLDER_ID };

	private static final int SHOW_PREFERENCES = 1;
	private static final int ADD_CHANNEL_ID = R.id.menu_new_channel;
	private static final int ADD_FOLDER_ID = R.id.menu_new_folder;
	private static final int SEARCH_ID = R.id.menu_search;
	private static final int PREFS_ID = Menu.FIRST;
	private static final int DELETE_ID = R.id.remove_channel;
	private static final int EDIT_ID = R.id.edit_channel;
	private Cursor mFolderCursor;
	private Cursor mChannelCursor;
	
	private long mFolderId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_list);
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		if (intent.getAction() == null)
			intent.setAction(Intent.ACTION_VIEW);
		
		mFolderId = Long.parseLong(uri.getPathSegments().get(1));
		
		mFolderCursor = managedQuery(FeedDroid.Folders.CONTENT_URI, 
				FOLDER_PROJECTION, "parent_id=" + mFolderId, null, null);
		mChannelCursor = managedQuery(FeedDroid.Channels.CONTENT_URI, 
				CHANNEL_PROJECTION, "folder_id=" + mFolderId, null, null);
		registerForContextMenu(getListView());
		
		FolderListCursorAdapter adapter = 
			new FolderListCursorAdapter(this, mFolderCursor, mChannelCursor);
		setListAdapter(adapter);
		
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
			i.putExtra("folderId", mFolderId);
			startActivityForResult(i, RESULT_OK);
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
	public void onResume() {
		super.onResume();
		FolderListCursorAdapter adapter = 
			new FolderListCursorAdapter(this, mFolderCursor, mChannelCursor);
		setListAdapter(adapter);
		
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
		
		default:
			return false;
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
	
}
