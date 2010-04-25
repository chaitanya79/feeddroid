package com.determinato.feeddroid.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.adapters.FolderListCursorAdapter;
import com.determinato.feeddroid.dao.ChannelDao;
import com.determinato.feeddroid.dao.FolderDao;
import com.determinato.feeddroid.dao.FolderItemDao;
import com.determinato.feeddroid.provider.FeedDroid;

public class FolderListActivity extends ListActivity {
	private static final String TAG = "FolderListActivity";
	private static final String[] FOLDER_PROJECTION = new String[] {FeedDroid.Folders._ID, 
		FeedDroid.Folders.NAME, FeedDroid.Folders.PARENT_ID};
	private static final String[] CHANNEL_PROJECTION = new String[] {FeedDroid.Channels._ID,
		FeedDroid.Channels.TITLE, FeedDroid.Channels.URL, FeedDroid.Channels.ICON,
		FeedDroid.Channels.FOLDER_ID };
	
	private Cursor mFolderCursor;
	private Cursor mChannelCursor;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_list);
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		if (intent.getAction() == null)
			intent.setAction(Intent.ACTION_VIEW);
		
		long folderId = Long.parseLong(uri.getPathSegments().get(1));
		
		mFolderCursor = managedQuery(FeedDroid.Folders.CONTENT_URI, 
				FOLDER_PROJECTION, "parent_id=" + folderId, null, null);
		mChannelCursor = managedQuery(FeedDroid.Channels.CONTENT_URI, 
				CHANNEL_PROJECTION, "folder_id=" + folderId, null, null);
		
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
	public void onResume() {
		super.onResume();
		mFolderCursor.requery();
		mChannelCursor.requery();
		FolderListCursorAdapter adapter = new FolderListCursorAdapter(this, mFolderCursor, mChannelCursor);
		getListView().setAdapter(adapter);

	}
}
