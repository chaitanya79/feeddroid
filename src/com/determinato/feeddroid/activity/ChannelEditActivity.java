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
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.provider.FeedDroid;

public class ChannelEditActivity extends Activity {
	private EditText mUrl;
	private EditText mTitle;
	
	private Uri mUri;
	
	private Cursor mCursor;
	
	private static final String[] PROJECTION = {
		FeedDroid.Channels._ID, FeedDroid.Channels.URL,
		FeedDroid.Channels.TITLE, FeedDroid.Channels.ICON };
	
	private static final int URL_INDEX = 1;
	private static final int TITLE_INDEX = 2;
	private static final int ICON_INDEX = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUri = getIntent().getData();
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		startManagingCursor(mCursor);
		setContentView(R.layout.channel_edit);
		
		mUrl = (EditText) findViewById(R.id.channel_edit_url);
		mTitle = (EditText) findViewById(R.id.channel_edit_name);
		
		Button save = (Button) findViewById(R.id.channel_edit_save);
		save.setOnClickListener(mSaveListener);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (mCursor == null)
			return;
		
		mCursor.moveToFirst();
		mUrl.setText(mCursor.getString(URL_INDEX));
		mTitle.setText(mCursor.getString(TITLE_INDEX));
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (mCursor == null)
			return;
		
		updateProvider();
	}
	
	private void updateProvider() {
		if (mCursor == null)
			return;
		ContentValues values = new ContentValues();
		values.put("url", mUrl.getText().toString());
		values.put("title", mTitle.getText().toString());
		
		ContentResolver resolver = getContentResolver();
		resolver.update(FeedDroid.Channels.CONTENT_URI, values, null, null);
	}
	
	private OnClickListener mSaveListener = new OnClickListener() {
		public void onClick(View v) {
			updateProvider();
			
			setResult(RESULT_OK, new Intent().setData(mUri));
			finish();
		}
	};
}
