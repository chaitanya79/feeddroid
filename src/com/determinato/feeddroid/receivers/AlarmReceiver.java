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
package com.determinato.feeddroid.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.determinato.feeddroid.service.FeedDroidUpdateService;

public class AlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "AlarmReceiver";
	public static final String ACTION_REFRESH_RSS_ALARM =
		"com.determinato.feeddroid.ACTION_REFRESH_RSS_ALARM";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Alarm received");
		String action = intent.getAction();
		
		Intent service = new Intent(context, FeedDroidUpdateService.class);
		context.startService(service);

	}

}
