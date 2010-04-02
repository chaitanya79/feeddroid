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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.KeyUtils;
import com.determinato.feeddroid.view.ChannelHeader;

public class PostViewActivity extends Activity {
	private static final String TAG = "PostView";
	
	private static final int NEXT_POST_ID = Menu.FIRST;
	private static final int PREV_POST_ID = Menu.FIRST + 1;
	
	private static final String[] PROJECTION = new String[] {
		FeedDroid.Posts._ID, FeedDroid.Posts.CHANNEL_ID,
		FeedDroid.Posts.TITLE, FeedDroid.Posts.BODY, FeedDroid.Posts.READ,
		FeedDroid.Posts.URL	};

	private long mChannelId = -1;
	private long mPostId = -1;
	
	private Cursor mCursor;
	
	private long mPrevPostId = -1;
	private long mNextPostId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_view);
		
		Uri uri = getIntent().getData();
		
		mCursor = managedQuery(uri, PROJECTION, null, null, null);
		
		if (mCursor == null || !mCursor.moveToFirst())
			finish();
		
		mChannelId = mCursor.getLong(mCursor.getColumnIndex(FeedDroid.Posts.CHANNEL_ID));
		mPostId = Long.parseLong(uri.getPathSegments().get(1));
		
		initWithData();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (mCursor == null || !mCursor.moveToFirst()) 
			return;
		
		ContentResolver resolver = getContentResolver();
		
		//resolver.update(FeedDroid.Posts.CONTENT_URI, null, FeedDroid.Posts.READ + "=1", null);
	}
	
	public void initWithData() {
		ContentResolver resolver = getContentResolver();
		
		Cursor cChannel = resolver.query(ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, mChannelId),
				new String[] { FeedDroid.Channels.ICON, FeedDroid.Channels.LOGO, FeedDroid.Channels.TITLE}, null, null, null);
		
		if (cChannel.getCount() != 1)
			return;
		
		cChannel.moveToFirst();
		
		ChannelHeader head = (ChannelHeader) findViewById(R.id.postViewHead);
		head.setLogo(cChannel);
		
		cChannel.close();
		
		TextView postTitle = (TextView) findViewById(R.id.postTitle);
		postTitle.setText(mCursor.getString(mCursor.getColumnIndex(FeedDroid.Posts.TITLE)));
		
		WebView postText = (WebView)findViewById(R.id.postText);
		
		 String html =
             "<html><head><style type=\"text/css\">body { background-color: #201c19; color: white; } a { color: #ddf; }</style></head><body>" +
             getBody() +
             "</body></html>";

		
		postText.loadData(html, "text/html", "utf-8");
	}
	
	private String getBody() {
		String body = mCursor.getString(mCursor.getColumnIndex(FeedDroid.Posts.BODY));
		String url = mCursor.getString(mCursor.getColumnIndex(FeedDroid.Posts.URL));
		
		if (!hasMoreLink(body, url))
			body += "<p><a href=\"" + url + "\">Read more...</a></p>";

		
		return body;
	}
	
	private boolean hasMoreLink(String body, String url) {
		int urlPos;
		
		if ((urlPos = body.indexOf(url)) <= 0)
			return false;
		
		try {
			
			if (body.charAt(urlPos - 1) != '>')
				return false;
			
			if (body.charAt(urlPos + url.length() + 1) != '<')
				return false;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}
	
	private void getSiblings() {
		if (mNextPostId < 0 || mPrevPostId < 0) {
			Cursor cPostList = getContentResolver().query(ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI_LIST, mChannelId),
					new String[] {FeedDroid.Posts._ID}, null, null, null);
			cPostList.moveToFirst();
			
			int indexId = cPostList.getColumnIndex(FeedDroid.Posts._ID);
			
			long lastId = -1;
			
			for (cPostList.moveToFirst(); cPostList.isLast(); cPostList.moveToNext()) {
				long thisId = cPostList.getLong(indexId);
				
				if (thisId == mPostId)
					break;
				lastId = thisId;
			}
			
			if (mNextPostId < 0)
				mNextPostId = lastId;
			
			if (mPrevPostId < 0) {
				if (!cPostList.isLast()) {
					cPostList.moveToNext();
					mPrevPostId = cPostList.getLong(indexId);
				}
			}
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeGroup(0);
		getSiblings();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.post_view_menu, menu);
		return true;
	}
	
	private void moveTo(long id) {
		Intent i = new Intent(Intent.ACTION_VIEW,
				ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI, id));
		startActivity(i);
		finish();
	}
	
	private boolean prevPost() {
		if (mPrevPostId < 0)
			return false;
		moveTo(mPrevPostId);
		return true;
	}
	
	private boolean nextPost() {
		if (mNextPostId < 0)
			return false;
		moveTo(mNextPostId);
		return true;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case PREV_POST_ID:
			return prevPost();
		case NEXT_POST_ID:
			return nextPost();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ((keyCode != KeyEvent.KEYCODE_DPAD_LEFT) &&
				(keyCode != KeyEvent.KEYCODE_DPAD_RIGHT)) {
			switch(KeyUtils.intrepretDirection(keyCode)) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				getSiblings();
				return nextPost();
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				getSiblings();
				return prevPost();
			case KeyEvent.KEYCODE_BACK:
				finish();
			}
		}
		return false;
	}
	
}


