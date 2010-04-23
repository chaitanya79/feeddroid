package com.determinato.feeddroid.activity;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.adapters.FolderListCursorAdapter;
import com.determinato.feeddroid.provider.FeedDroid;

public class HomeScreenActivity extends ListActivity {
	private static final String TAG = "HomeScreenActivity";
	private static final String[] FOLDER_PROJECTION = {FeedDroid.Folders._ID, 
		FeedDroid.Folders.NAME, FeedDroid.Folders.PARENT_ID};
	private static final String[] CHANNEL_PROJECTION = {
		FeedDroid.Channels._ID, FeedDroid.Channels.TITLE, FeedDroid.Channels.URL,
		FeedDroid.Channels.ICON, FeedDroid.Channels.FOLDER_ID
	};
	
	
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
		
	}
}
