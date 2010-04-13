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
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.GestureFilter;
import com.determinato.feeddroid.util.KeyUtils;
import com.determinato.feeddroid.util.GestureFilter.SimpleGestureListener;
import com.determinato.feeddroid.view.ChannelHeader;

public class PostViewActivity extends Activity implements SimpleGestureListener {
	private static final String TAG = "PostView";
	
	private static final int NEXT_POST_ID = Menu.FIRST;
	private static final int PREV_POST_ID = Menu.FIRST + 1;
	
	private static final String[] PROJECTION = new String[] {
		FeedDroid.Posts._ID, FeedDroid.Posts.CHANNEL_ID,
		FeedDroid.Posts.TITLE, FeedDroid.Posts.BODY, FeedDroid.Posts.READ,
		FeedDroid.Posts.URL, FeedDroid.Posts.STARRED	};

	private long mChannelId = -1;
	private long mPostId = -1;
	
	private Cursor mCursor;
	
	private long mPrevPostId = -1;
	private long mNextPostId = -1;
	private GestureLibrary mLibrary;
	private GestureFilter mDetector;
	private TextView mTxtStarred;
	private ContentResolver mResolver;
	private ImageButton mStarred;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_view);
		
		Uri uri = getIntent().getData();
		String postId = uri.getPathSegments().get(1);
		
		mResolver = getContentResolver();
		mCursor = mResolver.query(uri, PROJECTION, "_id=" + postId, null, "posted_on desc");

		
		if (mCursor.getCount() == 0) {
			Toast.makeText(this, "Unable to locate post", Toast.LENGTH_LONG).show();
			
			finish();
		}
		
		mCursor.moveToFirst();
		mTxtStarred = (TextView) findViewById(R.id.txt_starred);
		mStarred = (ImageButton) findViewById(R.id.star_post);
		mStarred.setBackgroundColor(Color.TRANSPARENT);
		mStarred.setOnClickListener(btn_listener);
		int starred = mCursor.getInt(mCursor.getColumnIndex(FeedDroid.Posts.STARRED));
		
		if (starred == 1) {
			mTxtStarred.setText(R.string.starred);
			mStarred.setImageResource(android.R.drawable.star_on);
		} else {
			mTxtStarred.setText(R.string.star_this);
			mStarred.setImageResource(android.R.drawable.star_off);
		}
		
		
		
		mChannelId = mCursor.getLong(mCursor.getColumnIndex(FeedDroid.Posts.CHANNEL_ID));
		mPostId = Long.parseLong(uri.getPathSegments().get(1));
		mDetector = new GestureFilter(this, this);
		mDetector.setEnabled(true);
		mDetector.setMode(GestureFilter.MODE_DYNAMIC);
		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!mLibrary.load()) {
			Log.e(TAG, "Unable to load gestures library.  Application cannot continue");
			finish();
		}


		initWithData();
		
	}
	
	
	OnClickListener btn_listener = new OnClickListener() {
		public void onClick(View v) {
			Uri uri = getIntent().getData();
			Cursor c = mResolver.query(uri, PROJECTION, "_id=" + mPostId, null, null);
			c.moveToFirst();
			int starred = c.getInt(c.getColumnIndex(FeedDroid.Posts.STARRED));
			if (starred == 0)
				starred = 1;
			else
				starred = 0;
			ContentValues values = new ContentValues();
			values.put("starred", starred);
			mResolver.update(uri, values, "_id=" + mPostId, null);
			
			if (starred == 0) {
				mTxtStarred.setText(R.string.star_this);
				mStarred.setImageResource(android.R.drawable.star_off);
			} else if (starred == 1){
				mTxtStarred.setText(R.string.starred);
				mStarred.setImageResource(android.R.drawable.star_on);
			}
			
		}
	};
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (mCursor == null || !mCursor.moveToFirst()) 
			return;
		
		ContentResolver resolver = getContentResolver();
		ContentValues values = new ContentValues();
		values.put("read", 1);
		
		resolver.update(FeedDroid.Posts.CONTENT_URI, values, 
				"_id=?", new String[] {Long.toString(mPostId)});
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
             "<html><head><style type=\"text/css\">body { background-color: #003333; color: white; } a { color: #ddf; }</style></head><body>" +
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
			switch(KeyUtils.intrepretDirection(keyCode)) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				getSiblings();
				return nextPost();
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				getSiblings();
				return prevPost();
			}		
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(KeyUtils.intrepretDirection(keyCode)) {
		case KeyEvent.KEYCODE_BACK:
			finish();
		}
		return false;
	}

	public void onDoubleTap() {
		// TODO Auto-generated method stub
		
	}

	public void onSwipe(int direction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
	
	
	
}


