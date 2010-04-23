package com.determinato.feeddroid.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.determinato.feeddroid.dao.ChannelDao;
import com.determinato.feeddroid.dao.FolderDao;
import com.determinato.feeddroid.dao.FolderItemDao;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.view.FolderListRow;

public class FolderListCursorAdapter extends BaseAdapter {
	private Cursor mFolderCursor;
	private Cursor mChannelCursor;
	private Context mContext;
	private ArrayList<FolderItemDao> rows;
	
	public FolderListCursorAdapter(Context context, Cursor folderCursor, Cursor channelCursor) {
		mFolderCursor = folderCursor;
		mChannelCursor = channelCursor;
		mContext = context;
		rows = new ArrayList<FolderItemDao>();
		
		populateRowMap();
	}
	
	public int getCount() {
		return rows.size();
	}

	public Object getItem(int position) {
		return rows.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		FolderListRow row = null;
		
		if (convertView == null) {
			row = new FolderListRow(mContext);
		} else 
			row = (FolderListRow) convertView;
		
		FolderItemDao item = rows.get(position);
		row.bindView(item);
		return row;
	}

	private void populateRowMap() {
		if (mFolderCursor.getCount() > 0) {
			mFolderCursor.moveToFirst();
			do {
				FolderDao folder = new FolderDao();
				folder.setId(mFolderCursor.getLong(
						mFolderCursor.getColumnIndex(FeedDroid.Folders._ID)));
				folder.setParentId(mFolderCursor.getLong(
						mFolderCursor.getColumnIndex(FeedDroid.Folders.PARENT_ID)));
				folder.setTitle(mFolderCursor.getString(
						mFolderCursor.getColumnIndex(FeedDroid.Folders.NAME)));
				rows.add(folder);
			} while (mFolderCursor.moveToNext());
		}
		
		if (mChannelCursor.getCount() > 0) {
			mChannelCursor.moveToFirst();
			do {
				ChannelDao channel = new ChannelDao();
				channel.setId(mChannelCursor.getLong(
						mChannelCursor.getColumnIndex(FeedDroid.Channels._ID)));
				channel.setFolderId(mChannelCursor.getLong(
						mChannelCursor.getColumnIndex(FeedDroid.Channels.FOLDER_ID)));
				channel.setTitle(mChannelCursor.getString(
						mChannelCursor.getColumnIndex(FeedDroid.Channels.TITLE)));
				channel.setUrl(mChannelCursor.getString(
						mChannelCursor.getColumnIndex(FeedDroid.Channels.URL)));
				channel.setIcon(mChannelCursor.getString(
						mChannelCursor.getColumnIndex(FeedDroid.Channels.ICON)));
				
				rows.add(channel);
			} while (mChannelCursor.moveToNext());
		}
	}
}
