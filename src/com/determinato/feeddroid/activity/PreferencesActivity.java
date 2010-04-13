package com.determinato.feeddroid.activity;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.determinato.feeddroid.R;

public class PreferencesActivity extends Activity {
	private static final String TAG = "PreferencesActivity";
	public static final String USER_PREFERENCE = "USER_PREFERENCES";
	public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
	public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
	
	CheckBox mAutoUpdate;
	Spinner mUpdateFrequency;
	SharedPreferences mPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prefs);
		
		mPreferences = getSharedPreferences(USER_PREFERENCE, Activity.MODE_PRIVATE);
		
		final TextView txtFreq = (TextView) findViewById(R.id.txt_freq);
		mAutoUpdate = (CheckBox) findViewById(R.id.chk_auto_update);
		mUpdateFrequency = (Spinner) findViewById(R.id.frequency);
		Button okButton = (Button) findViewById(R.id.btn_ok);
		Button cancelButton = (Button) findViewById(R.id.btn_cancel);
		

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
				
				setResult(RESULT_OK);
				finish();
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				
				finish();
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
		
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	}
}
