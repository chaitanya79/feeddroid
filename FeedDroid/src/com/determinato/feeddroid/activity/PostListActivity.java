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

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.KeyUtils;
import com.determinato.feeddroid.view.ChannelHeader;
import com.determinato.feeddroid.view.PostListRow;

/**
 * Activity to list posts for a given RSS feed.
 * 
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class PostListActivity extends ListActivity {
	private static final String TAG = "PostListActivity";
	private static final int MARK_ALL_READ_ID = R.id.menu_mark_all_read;
	
	private static final String[] PROJECTION = new String[] {
		FeedDroid.Posts._ID, FeedDroid.Posts.CHANNEL_ID,
		FeedDroid.Posts.TITLE, FeedDroid.Posts.READ,
		FeedDroid.Posts.DATE, FeedDroid.Posts.STARRED };
	
	private Cursor mCursor;
	private long mId = -1;
	
	private long mPrevId = -1;
	private long mNextId = -1;
	private boolean showAll = false;
	private Context mContext = this;
	private RadioButton unread;
	private RadioButton all;
	private RadioButton starred;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_list);

		unread = (RadioButton) findViewById(R.id.show_unread);
		all = (RadioButton) findViewById(R.id.show_all);
		starred = (RadioButton) findViewById(R.id.show_starred);
		
		unread.setOnClickListener(radio_listener);
		all.setOnClickListener(radio_listener);
		starred.setOnClickListener(radio_listener);
		
		Uri uri = getIntent().getData();
		
		mCursor = managedQuery(uri, PROJECTION, "read=0", null, "posted_on desc");
		
		startManagingCursor(mCursor);
		mId = Long.parseLong(uri.getPathSegments().get(1));
		
		ListAdapter adapter = new PostListAdapter(mCursor, this);
		setListAdapter(adapter);
		initWithData();
	}
	
	/**
	 * Initializes channel name header.
	 */
	private void initWithData() {
		long channelId = Long.parseLong(getIntent().getData().getPathSegments().get(1));
		
		ContentResolver resolver = getContentResolver();
		Cursor cChannel = resolver.query(ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, channelId),
				new String[] { FeedDroid.Channels.LOGO, FeedDroid.Channels.ICON, FeedDroid.Channels.TITLE },
				null, null, null);
			
		
	
		cChannel.moveToFirst();
		ChannelHeader head = (ChannelHeader) findViewById(R.id.postListHead);
		head.setLogo(cChannel);
			
		cChannel.close();
		
	}
	
	/**
	 * Radio button listener for All/Unread/Starred.
	 */
	private OnClickListener radio_listener = new OnClickListener() {
		public void onClick(View v) {
			ListAdapter adapter = null;
			RadioButton rb = (RadioButton) v;
			
			switch(rb.getId()) {
			case R.id.show_all:
				mCursor = managedQuery(getIntent().getData(), PROJECTION, null, null, null);
				break;
			case R.id.show_starred:
				Log.d(TAG, "show_starred clicked");
				mCursor = managedQuery(getIntent().getData(), PROJECTION, "starred=1", null, null);
				break;
			default:
				mCursor = managedQuery(getIntent().getData(), PROJECTION, "read=0", null, null);
				break;

			}
				
			adapter = new PostListAdapter(mCursor, mContext);
			getListView().setAdapter(adapter);
			getListView().invalidate();
			
		}
	};
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart() {
		initWithData();
		super.onStart();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResume() {
		super.onResume();
		initWithData();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "List item clicked: " + id);
		Uri uri = 
			ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI, id);
		String action = getIntent().getAction();
		
		if (action.equals(Intent.ACTION_PICK) ||
				action.equals(Intent.ACTION_GET_CONTENT)) {
			Intent i = new Intent();
			i.setData(uri);
			setResult(RESULT_OK, i);
		} else {
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeGroup(0);
		getSiblings();
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.post_list_menu, menu);
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case MARK_ALL_READ_ID:
			markAllPostsRead();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Marks all posts for the current RSS channel as read.
	 */
	private void markAllPostsRead() {
		ContentValues values = new ContentValues();
		values.put("read", 1);
		getContentResolver().update(FeedDroid.Posts.CONTENT_URI, values, "channel_id=" + mId, null);
		finish();
		
	}
	
	/**
	 * Gets child folders and channels of the current item.
	 */
	private void getSiblings() {
		if (mNextId >= 0 && mPrevId >= 0)
			return;
		
		Cursor cChannelList = getContentResolver().query(
				FeedDroid.Channels.CONTENT_URI,
				new String[] {FeedDroid.Channels._ID }, null, null, null);
		
		if (cChannelList.moveToFirst()) {
			long lastId = -1;
			
			for (cChannelList.moveToFirst(); cChannelList.isLast(); cChannelList.moveToNext()) {
				long thisId = cChannelList.getLong(0);
				
				if (thisId == mId)
					break;
				
				lastId = thisId;
			}
			
			if (mPrevId < 0)
				mPrevId = lastId;
			
			if (mNextId < 0) {
				if (cChannelList.isLast() == false) {
					cChannelList.moveToNext();
					mNextId = cChannelList.getLong(0);
				}
			}
			
			cChannelList.close();
		}
	}
	
	private void moveTo(long id) {
		Uri uri =
			ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI_LIST, id);
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);
		
		finish();
	}
	
	private boolean prevChannel() {
		if (mPrevId < 0)
			return false;
		
		moveTo(mPrevId);
		return true;
	}
	
	private boolean nextChannel() {
		if (mNextId < 0) 
			return false;
		
		moveTo(mNextId);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch(KeyUtils.intrepretDirection(keyCode)) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			getSiblings();
			return prevChannel();
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			getSiblings();
			return nextChannel();
		
		}
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(KeyUtils.intrepretDirection(keyCode)) {
		case KeyEvent.KEYCODE_BACK:
			finish();
		}
		return false;
	}
	
	/**
	 * 
	 * List adapter
	 *
	 */
	private static class PostListAdapter extends CursorAdapter implements Filterable {
		public PostListAdapter(Cursor c, Context ctx) {
			super(ctx, c);
		}
		
		@Override
		public void bindView(View view, Context ctx, Cursor c) {
			((PostListRow)view).bindView(c);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			PostListRow post = new PostListRow(context);
			post.bindView(cursor);
			return post;
		}
		
		
	}
}
