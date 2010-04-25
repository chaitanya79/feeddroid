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
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.dao.ChannelDao;
import com.determinato.feeddroid.dao.FolderDao;
import com.determinato.feeddroid.dao.FolderItemDao;
import com.determinato.feeddroid.provider.FeedDroid;

public class FolderListRow extends LinearLayout {
	public static final int FOLDER_VIEW = 1;
	public static final int CHANNEL_VIEW = 2;
	
	private ImageView mImage;
	private TextView mTitle;
	private TextView mUnread;
	private Context mContext;
	private int mItemType;
	private FolderItemDao mBacking;
	
	public FolderListRow(Context context) {
		super(context);
		mContext = context;
		View v = LayoutInflater.from(context)
			.inflate(R.layout.folder_list_row, null, false);
		
		mImage = (ImageView) v.findViewById(R.id.folder_item_icon);
		mTitle = (TextView) v.findViewById(R.id.folder_item_title);
		mUnread = (TextView) v.findViewById(R.id.folder_item_unread);
		
		LinearLayout.LayoutParams rules =
			new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 75);
		
		addView(v, rules);
	}
	
	public void bindView(FolderItemDao item) {
		mBacking = item;
		if (item instanceof FolderDao) {
			mImage.setImageDrawable(mContext.getResources()
					.getDrawable(R.drawable.folder));
			mTitle.setText(((FolderDao)item).getTitle());
			mItemType = FOLDER_VIEW;
			 
			
		} else if (item instanceof ChannelDao) {
			
			mItemType = CHANNEL_VIEW;
			mImage.setImageDrawable(mContext.getResources()
					.getDrawable(R.drawable.rssorange));
			mTitle.setText(((ChannelDao) item).getTitle());
			Integer unread = getUnreadCount((ChannelDao)item);
			if (unread > 0)
				mUnread.setText(unread.toString());
			

		}
	}

	public FolderItemDao getBacking() {
		return mBacking;
	}
	
	public void setBacking(FolderItemDao backing) {
		mBacking = backing;
	}
	
	public int getItemType() {
		return mItemType;
	}
	
	private int getUnreadCount(ChannelDao item) {
		ContentResolver resolver = mContext.getContentResolver();
		long channelId = item.getId();
		Cursor c = resolver.query(FeedDroid.Posts.CONTENT_URI, null, "channel_id=" + channelId + 
				" and read=0", null, null);
		int count = c.getCount();
		c.close();
		return count;
	}
}