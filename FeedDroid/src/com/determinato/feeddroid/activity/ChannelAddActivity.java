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

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.parser.RssParser;
import com.determinato.feeddroid.provider.FeedDroid;

public class ChannelAddActivity extends Activity {
	private static final String TAG = "ChannelAdd";
	
	private EditText mUrl;
	private Context mContext = this;
	protected ProgressDialog mBusy;
	final Handler mHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_add);
		
		mUrl = (EditText) findViewById(R.id.url);
		
		Button add = (Button) findViewById(R.id.add);
		add.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addChannel();
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	private static URL getDefaultFavicon(String url) {
		try {
			URL orig = new URL(url);
			URL iconUrl = new URL(orig.getProtocol(), orig.getHost(), orig.getPort(), "/favicon.ico");
			return iconUrl;
		} catch (MalformedURLException e) {
			Log.d(TAG, Log.getStackTraceString(e));
			return null;
		}
	}
	
	private void addChannel() {
		
		final String url = mUrl.getText().toString();
		
		mBusy = ProgressDialog.show(ChannelAddActivity.this, "Downloading", 
				"Accessing XML feed...", true, false);
		
		Thread t = new Thread() {
			public void run() {
				try {
					
					RssParser refresh = new RssParser(getContentResolver());
					
					final long id = refresh.syncDb(null, -1, url);
					
					if (id >= 0) {
						URL iconUrl = getDefaultFavicon(url);
						refresh.updateFavicon(id, iconUrl);
					}
					
					mHandler.post(new Runnable() {
						public void run() {
							mBusy.dismiss();
							Uri uri = ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, id);
							getIntent().setData(uri);
							
							setResult(RESULT_OK, getIntent());
							finish(); 
						}
					});
				} catch (Exception e) {
					
					final String errMsg = e.getMessage();
					final String fullErrMsg = e.toString();
					mHandler.post(new Runnable() {
						public void run() {
							mBusy.dismiss();
							String errstr = ((fullErrMsg != null) ? fullErrMsg : errMsg);
							
							AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
							builder.setTitle("Feed error");
							builder.setMessage("An error was encountered while accessing the feed");
							
							builder.setCancelable(true);
							builder.create().show();
						}
					});
				}
			}
		};
		t.start();
	}
}
