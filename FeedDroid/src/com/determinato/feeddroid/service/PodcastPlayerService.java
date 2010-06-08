package com.determinato.feeddroid.service;

import android.app.IntentService;
import android.content.Intent;

public class PodcastPlayerService extends IntentService {
	private static final String TAG = "PodcastPlayerService";
	
	public PodcastPlayerService() {
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

}
