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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.determinato.feeddroid.R;

/**
 * Activity to play audio/video podcast files.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class PodcastPlayerActivity extends Activity {
	private static final String TAG = "PodcastPlayerActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_player);

		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		String name = intent.getStringExtra("name");
		String type = intent.getStringExtra("type");
		
		TextView title = (TextView) findViewById(R.id.podcast_name);
		title.setText(name);
		
		if (type.startsWith("audio")) {
			ImageView image = new ImageView(this);
			image.setBackgroundDrawable(getResources().getDrawable(R.drawable.rssred256));
			image.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			
		} 
		
		VideoView player = (VideoView) findViewById(R.id.podcast_player);
		player.setKeepScreenOn(true);
		player.setVideoURI(Uri.parse(url));
		
		MediaController controller = new MediaController(this);
		player.setMediaController(controller);
		player.requestFocus();
		player.start();
		controller.show(10000);
		
		
	}
}
