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
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.KeyUtils;
import com.determinato.feeddroid.view.ChannelHeader;
import com.determinato.feeddroid.view.PostListRow;

public class PostListActivity extends ListActivity {
	private static final String TAG = "PostListActivity";
	private static final int PREV_ID = R.id.menu_prev_channel;
	private static final int NEXT_ID = R.id.menu_next_channel;
	
	
	private static final String[] PROJECTION = new String[] {
		FeedDroid.Posts._ID, FeedDroid.Posts.CHANNEL_ID,
		FeedDroid.Posts.TITLE, FeedDroid.Posts.READ,
		FeedDroid.Posts.DATE };
	
	private Cursor mCursor;
	private long mId = -1;
	
	private long mPrevId = -1;
	private long mNextId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_list);
		
		Uri uri = getIntent().getData();
		mCursor = managedQuery(uri, PROJECTION, null, null, null);
		startManagingCursor(mCursor);
		mId = Long.parseLong(uri.getPathSegments().get(1));
		
		ListAdapter adapter = new PostListAdapter(mCursor, this);
		setListAdapter(adapter);
		initWithData();
	}
	
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
	
	@Override
	public void onStart() {
		initWithData();
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		initWithData();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
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
	
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeGroup(0);
		getSiblings();
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.post_list_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case PREV_ID:
			return prevChannel();
		case NEXT_ID:
			return nextChannel();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
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

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch(KeyUtils.intrepretDirection(keyCode)) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			getSiblings();
			return prevChannel();
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			getSiblings();
			return nextChannel();
		case KeyEvent.KEYCODE_BACK:
			finish();
		}
		
		return false;
	}
	
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
