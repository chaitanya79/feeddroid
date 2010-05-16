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

package com.determinato.feeddroid.provider;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.activity.HomeScreenActivity;
import com.determinato.feeddroid.activity.PostViewActivity;

public class FeedDroidWidget extends AppWidgetProvider {
	private static final String TAG = "FeedDroidWidget";
	
	public static final String FORCE_WIDGET_UPDATE =
		"com.determinato.feeddroid.FORCE_WIDGET_UPDATE";
	
	@Override
	public void onUpdate(Context ctx, AppWidgetManager manager, int[] ids) {
		updateWidget(ctx, manager, ids);
	}

	@Override
	public void onReceive(Context ctx, Intent intent) {
		super.onReceive(ctx, intent);
		
		if (intent.getAction().equals(FORCE_WIDGET_UPDATE) || 
				intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
			AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
			int[] ids = manager.getAppWidgetIds(new ComponentName(ctx, FeedDroidWidget.class));
			updateWidget(ctx, manager, ids);
		}
	}
	
	
	private void updateWidget(Context ctx, AppWidgetManager manager, int[] ids) {
		Log.d(TAG, "updateWidget called");
		ContentResolver resolver = ctx.getContentResolver();
		Cursor c = resolver.query(FeedDroid.Posts.CONTENT_URI, null, null, null, null);
		Cursor p = null;
		try {
			int postCount = c.getCount();
			Random generator = new Random(System.currentTimeMillis());
			Integer r = generator.nextInt(postCount);

			p = resolver.query(FeedDroid.Posts.CONTENT_URI, new String[] {FeedDroid.Posts.TITLE}, "_id=? and read=0",
					new String[] {r.toString()}, null);

			if (p.getCount() == 0)
				return;

			p.moveToFirst();
			final int N = ids.length;

			for (int i = 0; i < N; i++) {
				int id = ids[i];
				
				Intent intent = new Intent(ctx, PostViewActivity.class);
				intent.setData(ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI, r));
				PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, 0);

				Intent homeIntent = new Intent(ctx, HomeScreenActivity.class);
				PendingIntent homePi = PendingIntent.getActivity(ctx, 0, homeIntent, 0);
				
				RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_layout);

				views.setTextViewText(R.id.widget_article_title, p.getString(p.getColumnIndex(FeedDroid.Posts.TITLE)));
				views.setViewVisibility(R.id.widget_article_title, View.VISIBLE);
				views.setOnClickPendingIntent(R.id.widget_article_title, pi);
				views.setOnClickPendingIntent(R.id.widget_image, homePi);
				manager.updateAppWidget(id, views);
			}

		} finally {
			p.close();
			c.close();
		}


	}
}
