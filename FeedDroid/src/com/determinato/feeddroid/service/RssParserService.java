package com.determinato.feeddroid.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.determinato.feeddroid.parser.RssParser;

/**
 * Service to update RSS feeds in the background.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class RssParserService extends IntentService {
	private static final String TAG = "RssParserService";


	/**
	 * Constructor.
	 */
	public RssParserService() {
		super(TAG);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		long id = intent.getLongExtra("id", -1);
		String url = intent.getStringExtra("url");
		
		if (id == -1 || TextUtils.isEmpty(url))
			return;
		
		try {
			new RssParser(getContentResolver()).syncDb(id, url);
			
		} catch (Exception e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
	}

}
