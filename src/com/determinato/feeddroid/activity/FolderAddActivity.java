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
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.provider.FeedDroid;


public class FolderAddActivity extends Activity {
	private static final String TAG = "FolderAddActivity";
	
	private EditText mFolderName;
	private Spinner mParentFolder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_add);
		
		mFolderName = (EditText) findViewById(R.id.folder_name);
		mParentFolder = (Spinner) findViewById(R.id.parent_folder);
		Button addBtn = (Button) findViewById(R.id.btn_add);
		addBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				ContentValues values = new ContentValues();
				values.put("name", mFolderName.getText().toString());
				values.put("parent_id", mParentFolder.getSelectedItemId());
				getContentResolver().insert(FeedDroid.Folders.CONTENT_URI, values);
				finish();
			}
		});
		
		Cursor c = managedQuery(FeedDroid.Folders.CONTENT_URI, 
				new String[] {FeedDroid.Folders._ID, FeedDroid.Folders.NAME}, 
				null, null, null);
		
		String[] columns = new String[] {FeedDroid.Folders.NAME};
		int[] to = new int[] {android.R.id.text1};
		
		SimpleCursorAdapter adapter = 
			new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, 
					c, columns, to);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mParentFolder.setAdapter(adapter);
		
	}
}
