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

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.ads.FeedDroidAdConstants;
import com.determinato.feeddroid.ads.FeedDroidAdSenseSpec;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.util.KeyUtils;
import com.determinato.feeddroid.view.ChannelHeader;
import com.google.ads.AdSenseSpec;
import com.google.ads.GoogleAdView;
import com.google.ads.AdSenseSpec.AdType;
import com.google.ads.AdSenseSpec.ExpandDirection;

/**
 * Activity to view a RSS post.
 * 
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class PostViewActivity extends Activity {
	private static final String TAG = "PostView";
	
	private static final int NEXT_POST_ID = Menu.FIRST;
	private static final int PREV_POST_ID = Menu.FIRST + 1;
	
	private static final String[] PROJECTION = new String[] {
		FeedDroid.Posts._ID, FeedDroid.Posts.CHANNEL_ID,
		FeedDroid.Posts.TITLE, FeedDroid.Posts.BODY, FeedDroid.Posts.READ,
		FeedDroid.Posts.URL, FeedDroid.Posts.STARRED, FeedDroid.Posts.PODCAST_URL, FeedDroid.Posts.PODCAST_MIME_TYPE};

	private long mChannelId = -1;
	private long mPostId = -1;
	
	private Cursor mCursor;
	private Context mContext;
	private long mPrevPostId = -1;
	private long mNextPostId = -1;
	private TextView mTxtStarred;
	private ContentResolver mResolver;
	private ImageButton mStarred;
	
	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_view);
		
		mContext = this;
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
		initWithData();
		
        // Set up ads
	      AdSenseSpec spec = new FeedDroidAdSenseSpec(FeedDroidAdConstants.CLIENT_ID)
	        	.setCompanyName(FeedDroidAdConstants.COMPANY_NAME)
	        	.setAppName(getString(R.string.app_name))
	        	.setChannel(FeedDroidAdConstants.CHANNEL_ID)
	        	.setAdType(AdType.TEXT_IMAGE)
	        	.setExpandDirection(ExpandDirection.TOP);
	      
	      	GoogleAdView adView = (GoogleAdView) findViewById(R.id.adview);
	      	adView.showAds(spec);		
	}
	
	/**
	 * Listener for starred button.
	 */
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
	
	/**
	 * {@inheritDoc}
	 */
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
	
	/**
	 * Initializes data for the view.
	 */
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
		
		// Podcast support: If post contains a URL to a podcast, enable option to download
		// the podcast mp3 and play it in the Media Player.
		mCursor.requery();
		mCursor.moveToFirst();
		final String podcastUrl = mCursor.getString(mCursor.getColumnIndex(FeedDroid.Posts.PODCAST_URL));
		final String podcastName = mCursor.getString(mCursor.getColumnIndex(FeedDroid.Posts.TITLE));
		final String podcastMimeType = mCursor.getString(mCursor.getColumnIndex(FeedDroid.Posts.PODCAST_MIME_TYPE));
		
		Button listenBtn = (Button) findViewById(R.id.btnListen);
		Button downloadBtn = (Button) findViewById(R.id.btnDownload);

		if (!TextUtils.isEmpty(podcastUrl)) {
			
			listenBtn.setEnabled(true);
			listenBtn.setOnClickListener(new View.OnClickListener() {
				
				
				public void onClick(View v) {
					Intent player = new Intent(mContext, PodcastPlayerActivity.class);
					player.putExtra("url", podcastUrl);
					player.putExtra("name", podcastName);
					player.putExtra("type", podcastMimeType);
					startActivity(player);
				}
			});
		} else {
			listenBtn.setVisibility(View.INVISIBLE);
			downloadBtn.setVisibility(View.INVISIBLE);
		}

	}
	
	/**
	 * Gets body of post from the database and appends a "Read more..." link.
	 * @return body of post
	 */
	private String getBody() {
		String body = mCursor.getString(mCursor.getColumnIndex(FeedDroid.Posts.BODY));
		String url = mCursor.getString(mCursor.getColumnIndex(FeedDroid.Posts.URL));
		
		if (body == null) 
			body = "";
		
		
		if (!hasMoreLink(body, url))
			body += "<p><a href=\"" + url + "\">Read more...</a></p>";

		
		return body;
	}
	
	/**
	 * Determines if a post has a "Read More" link
	 * @param body RSS post body
	 * @param url RSS post URL
	 * @return true if link exists, false otherwise
	 */
	private boolean hasMoreLink(String body, String url) {
		int urlPos;

		if ((body == null) || (urlPos = body.indexOf(url)) <= 0)
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
	
	/**
	 * Gets children.
	 */
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
	
/*	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeGroup(0);
		getSiblings();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.post_view_menu, menu);
		return true;
	}
*/	
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
	
	/**
	 * {@inheritDoc}
	 */
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
	
	/**
	 * {@inheritDoc}
	 */
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
	 * {@inheritDoc}
	 */
	@Override
	protected void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
	
	
	
}


