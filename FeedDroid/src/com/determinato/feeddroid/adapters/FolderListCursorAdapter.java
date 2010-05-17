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

/**
 * Adapter for folder list activity.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class FolderListCursorAdapter extends BaseAdapter {
	private static final String TAG = "FolderListCursorAdapter";
	private Cursor mFolderCursor;
	private Cursor mChannelCursor;
	private Context mContext;
	private static ArrayList<FolderItemDao> rows;
	
	/**
	 * Constructor.
	 * @param context Context of the List View.
	 * @param folderCursor Cursor containing folders.
	 * @param channelCursor Cursor containing channels.
	 */
	public FolderListCursorAdapter(Context context, Cursor folderCursor, Cursor channelCursor) {
		mFolderCursor = folderCursor;
		mChannelCursor = channelCursor;
		mContext = context;
		rows = new ArrayList<FolderItemDao>();
		
		
		populateRowMap();
	}
	
	/**
	 * Returns number of rows in the adapter.
	 * 
	 * @return number of rows, or 0.
	 */
	public int getCount() {
		if (rows == null)
			return 0;
		else
			return rows.size();
	}

	/**
	 * Gets View at specified position.
	 * 
	 * @param position Position of item in the list
	 * @return FolderItemDao
	 */
	public FolderItemDao getItem(int position) {
		return rows.get(position);
	}

	
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Returns view at specified position in the list.
	 * 
	 * @param position position in the list
	 * @param convertView recycled view
	 * @param parent The list view
	 * 
	 * @return the View at the specified position
	 */
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

	/**
	 * Requerys the cursors after a database refresh.
	 */
	public void refresh() {
		mFolderCursor.requery();
		mChannelCursor.requery();
	}
	
	/**
	 * Returns the list
	 * @return ArrayList of FolderItemDao objects
	 */
	public ArrayList<FolderItemDao> getRows() {
		return rows;
	}
	
	/**
	 * Populates the row map.
	 */
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
