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

package com.determinato.feeddroid.view;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.provider.FeedDroid;

/**
 * View containing details of an RSS feed.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class ChannelListRow extends RelativeLayout {
	private static final String TAG = "ChannelListRow";
	private ImageView mIcon;
	private TextView mName;
	private TextView mCount;
	private ProgressBar mRefresh;
	
	/**
	 * Constructor.
	 * @param ctx parent view context
	 */
	public ChannelListRow(Context ctx) {
		super(ctx);
		
		
		
		View v = LayoutInflater.from(getContext()).inflate(R.layout.channel_list_item, null, false);
		
		mIcon = (ImageView) v.findViewById(R.id.channel_icon);
		mName = (TextView) v.findViewById(R.id.channel_name);
		mCount = (TextView) v.findViewById(R.id.channel_post_count);
		mRefresh = (ProgressBar) v.findViewById(R.id.channel_refresh);
		mRefresh.setVisibility(View.GONE);
		
		
		RelativeLayout.LayoutParams rules =
			new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 75);
		
		addView(v, rules);
		
	}
	
	/**
	 * Binds data to this view.
	 * @param c database cursor
	 */
	public void bindView(Cursor c) {
		ContentResolver content = getContext().getContentResolver();
		
		long channelId =
			c.getLong(c.getColumnIndex(FeedDroid.Channels._ID));
		
		Cursor unread = content.query(ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI_LIST, channelId),
				new String[] {FeedDroid.Posts._ID }, "read=0", null, null);
		
		Typeface tf;
		
		int unreadCount = unread.getCount();
		unread.close();
		
		if (unreadCount > 0) 
			tf = Typeface.DEFAULT_BOLD;
		else
			tf = Typeface.DEFAULT;
		
		String icon = c.getString(c.getColumnIndex(FeedDroid.Channels.ICON));
		mIcon.setImageURI(Uri.parse(icon));
		
		mName.setText(c.getString(c.getColumnIndex(FeedDroid.Channels.TITLE)));
		
		mCount.setTypeface(tf);
		if (unreadCount > 0) {
			mName.setTypeface(tf, Typeface.BOLD_ITALIC);
			mCount.setText(new Integer(unreadCount).toString());
			mCount.setTypeface(tf, Typeface.BOLD_ITALIC);
		}
		else {
			mName.setTypeface(tf);
			mCount.setText("");
		}
	}
	
	/**
	 * Show busy indicator.
	 */
	public void startRefresh() {
		mCount.setVisibility(View.GONE);
		mRefresh.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Hide busy indicator and update view.
	 * @param c database cursor
	 */
	public void finishRefresh(Cursor c) {
		mRefresh.setVisibility(GONE);
		bindView(c);
		mCount.setVisibility(VISIBLE);
	}
	
	/**
	 * Update view.
	 * @param channelId id of RSS feed
	 */
	public void finishRefresh(long channelId) {
		Cursor c = getContext().getContentResolver().query
		(ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, channelId),
				new String[] {FeedDroid.Channels._ID, FeedDroid.Channels.TITLE, FeedDroid.Channels.ICON},
				null, null, null);
		if (c.getCount() < 1)
			return;
		
		c.moveToFirst();
		finishRefresh(c);
		c.close();
	}
}
