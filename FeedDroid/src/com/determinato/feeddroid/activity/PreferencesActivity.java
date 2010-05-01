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


import java.io.File;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.parser.FeedParser;
import com.determinato.feeddroid.parser.GoogleReaderImporter;
import com.determinato.feeddroid.parser.OPMLParser;
import com.determinato.feeddroid.provider.FeedDroid;

public class PreferencesActivity extends Activity {
	private static final String TAG = "PreferencesActivity";
	private static final String ONBOARD_STORAGE_DIR = "/emmc/";
	private static final String SDCARD_DIR = "/sdcard/";
	private static final String IMPORT_FILENAME = "feeds.xml";
	private static final int IMPORT_DIALOG = 1;
	
	public static final String USER_PREFERENCE = "USER_PREFERENCES";
	public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
	public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
	
	CheckBox mAutoUpdate;
	Spinner mUpdateFrequency;
	SharedPreferences mPreferences;
	
	private Context mContext;
	private boolean mIsImported;
	private Intent mReturnIntent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prefs);
	
		mContext = this;
		mPreferences = getSharedPreferences(USER_PREFERENCE, Activity.MODE_PRIVATE);
		
		final TextView txtFreq = (TextView) findViewById(R.id.txt_freq);
		mAutoUpdate = (CheckBox) findViewById(R.id.chk_auto_update);
		mUpdateFrequency = (Spinner) findViewById(R.id.frequency);
		Button okButton = (Button) findViewById(R.id.btn_ok);
		Button cancelButton = (Button) findViewById(R.id.btn_cancel);
		Button importButton = (Button) findViewById(R.id.btn_greader_import);
		

		mAutoUpdate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					txtFreq.setVisibility(View.VISIBLE);
					mUpdateFrequency.setVisibility(View.VISIBLE);
				} else {
					txtFreq.setVisibility(View.INVISIBLE);
					mUpdateFrequency.setVisibility(View.INVISIBLE);
				}
				 
			}
		});

		okButton.setOnClickListener(new View.OnClickListener() {
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			private static final String ALARM_ACTION = "com.determinato.feeddroid.ACTION_REFRESH_RSS_ALARM";
			Intent i = new Intent(ALARM_ACTION);
			PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
			long time = mPreferences.getInt(PREF_UPDATE_FREQ, 15) * 60 * 1000;
			
			public void onClick(View v) {
				savePreferences();
				if (mPreferences.getBoolean(PREF_AUTO_UPDATE, false)) {
					alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time, time, pending);
				} else
					alarmManager.cancel(pending);
				
				mReturnIntent = new Intent(null, FeedDroid.Channels.CONTENT_URI);
				mReturnIntent.setData(FeedDroid.Channels.CONTENT_URI);
				mReturnIntent.setAction(Intent.ACTION_VIEW);
				mReturnIntent.putExtra("FEEDS_IMPORTED", mIsImported);
				setResult(RESULT_OK, mReturnIntent);
				finish();
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// If feeds were imported from Google Reader, we should be
				// returning that to the calling activity regardless
				// of if the page was cancelled or not.
				mReturnIntent = new Intent(null, FeedDroid.Channels.CONTENT_URI);
				mReturnIntent.setData(FeedDroid.Channels.CONTENT_URI);
				mReturnIntent.setAction(Intent.ACTION_VIEW);
				mReturnIntent.putExtra("FEEDS_IMPORTED", mIsImported);
				setResult(RESULT_OK, mReturnIntent);
				
				finish();
			}
		});

		importButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// Check both onboard storage (for devices like HTC Incredible)
				// and SD card for the import file.
				String filename = ONBOARD_STORAGE_DIR + IMPORT_FILENAME;
				File f = new File(filename);
				if (!f.exists()) {
					filename = SDCARD_DIR + IMPORT_FILENAME;
					f = new File(filename);
					if (!f.exists()) {
						Toast.makeText(mContext, "ERROR: feeds.xml doesn't exist.", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				try {
					FeedParser parser = new GoogleReaderImporter(mContext.getContentResolver());
					parser.importFeed(f);
					mIsImported = true;
				} catch (Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
					Toast.makeText(mContext, "An error occurred during the import", Toast.LENGTH_SHORT);
				}
			}
		});
		
		updateUIFromPreferences();
	}
	
	
	private void savePreferences() {
		int updateFreq = mUpdateFrequency.getSelectedItemPosition();
		boolean autoUpdate = mAutoUpdate.isChecked();

		int[] freqArray = getResources().getIntArray(R.array.update_freq_values);
		Editor editor = mPreferences.edit();
		editor.putBoolean(PREF_AUTO_UPDATE, autoUpdate);
		editor.putInt(PREF_UPDATE_FREQ, freqArray[updateFreq]);
		editor.commit();
	}
	
	private void updateUIFromPreferences() {
		boolean autoUpdate = mPreferences.getBoolean(PREF_AUTO_UPDATE, false);
		int updateFreq = mPreferences.getInt(PREF_UPDATE_FREQ, 15);
		Log.d(TAG, "updateFreq: " + updateFreq);
		TextView visibilityLbl = (TextView) findViewById(R.id.txt_freq);

		
		if (!autoUpdate) {
			visibilityLbl.setVisibility(View.INVISIBLE);
			mUpdateFrequency.setVisibility(View.INVISIBLE);
		} else {
			visibilityLbl.setVisibility(View.VISIBLE);
			mUpdateFrequency.setVisibility(View.VISIBLE);
			int frequency = 15;
			switch(updateFreq) {
			case 1:
				frequency = 0;
				break;
			case 15:
				frequency = 1;
				break;
			case 30:
				frequency = 2;
				break;
			case 45:
				frequency = 3;
				break;
			case 60:
				frequency = 4;
				break;
			case 90:
				frequency = 5;
				break;
			case 120:
				frequency = 6;
				break;
			}
			mUpdateFrequency.setSelection(frequency);
		}
		
		mAutoUpdate.setChecked(autoUpdate);
	}
	
	public class ItemListener implements OnItemSelectedListener {
		
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			Log.d(TAG, "Item selected: " + id);
		}
		
		public void onNothingSelected(AdapterView parent) {
			
		}
	}
}
