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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.determinato.feeddroid.R;

public class FeedDroidProvider extends ContentProvider {
	private static final String TAG = "FeedDroidProvider";
	private static final String DB_NAME = "feeddroid.db";
	private static final int DB_VERSION = 6;
	
	private static HashMap<String, String> CHANNEL_LIST_PROJECTION;
	private static HashMap<String, String> POST_LIST_PROJECTION;
	private static HashMap<String, String> FOLDER_LIST_PROJECTION;
	
	
	private static final int CHANNELS = 1;
	private static final int CHANNEL_ID = 2;
	private static final int POSTS = 3;
	private static final int POST_ID = 4;
	private static final int CHANNEL_POSTS = 5;
	private static final int CHANNELICON_ID = 6;
	private static final int FOLDERS = 7;
	private static final int FOLDER_ID = 8;
	
	private static final UriMatcher URL_MATCHER;
	
	private SQLiteDatabase mDb;
	
	private static class DbHelper extends SQLiteOpenHelper {
		DbHelper(Context ctx) {
			super(ctx, DB_NAME, null, DB_VERSION);
		}
		
		protected void onCreateChannels(SQLiteDatabase db) {
			String query = "CREATE TABLE channels (_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
				"title TEXT UNIQUE, url TEXT UNIQUE, " +
				"icon TEXT, icon_url TEXT, logo TEXT);";
			db.execSQL(query);
			db.execSQL("CREATE INDEX idx_folders ON channels (folder_id);");
		}
		
		protected void onCreatePosts(SQLiteDatabase db) {
			String query = "CREATE TABLE posts (_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
				"channel_id INTEGER, title TEXT UNIQUE, url TEXT UNIQUE, " +
				"posted_on DATETIME, body TEXT, author TEXT, read INTEGER(1) DEFAULT '0', " +
				"starred INTEGER(1) DEFAULT '0');";
			db.execSQL(query);
			
			// Create indexes
			db.execSQL("CREATE UNIQUE INDEX idx_post ON posts (title, url);");
			db.execSQL("CREATE INDEX idx_channel ON posts (channel_id);");
		}
		
		protected void onCreateFolders(SQLiteDatabase db) {
			String query = "CREATE TABLE folders (_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
				"name TEXT NOT NULL, parent_id INTEGER DEFAULT '0');";
			db.execSQL(query);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			onCreateChannels(db);
			onCreatePosts(db);
			onCreateFolders(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + "...");
			
			switch(oldVersion) {
			case 5:
				db.beginTransaction();
				try {
					String query = "ALTER TABLE channels ADD folder_id INTEGER DEFAULT '0'";
					db.execSQL(query);
					db.execSQL("CREATE INDEX idx_folders ON channels (folder_id);");
					query = "ALTER TABLE posts RENAME TO posts_temp";
					db.execSQL(query);
					query = "CREATE TABLE posts(_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
						"channel_id INTEGER, title TEXT UNIQUE, url TEXT UNIQUE, " + 
						"posted_on DATETIME, body TEXT, author TEXT, read INTEGER(1) DEFAULT '0', " +
						"starred INTEGER(1) DEFAULT '0');";
					db.execSQL(query);
					query = "INSERT INTO posts SELECT * FROM posts_temp;";
					db.execSQL(query);
					db.execSQL("DROP TABLE posts_temp");
					onCreateFolders(db);
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
				break;
			default:
				Log.w(TAG, "Version too old, wiping out database contents...");
				db.execSQL("DROP TABLE IF EXISTS channels");
				db.execSQL("DROP TABLE IF EXISTS posts");
				onCreate(db);
				break;
			}
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count;
		String where;
		
		switch(URL_MATCHER.match(uri)) {
		case CHANNELS:
			count = mDb.delete("channels", selection, selectionArgs);
			break;
		
		case CHANNEL_ID:
			where = "_id=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "" );
			count = mDb.delete("posts", where, selectionArgs);
			break;
			
		case POSTS:
			count = mDb.delete("posts", selection, selectionArgs);
			break;
			
		case POST_ID:
			where = "_id=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			count = mDb.delete("posts", where, selectionArgs);
			break;
			
		case FOLDERS:
			count = mDb.delete("folders", selection, selectionArgs);
			break;
			
		case FOLDER_ID:
			where = "_id=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			count = mDb.delete("folders", where, selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URL: " + uri);
		} 
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		
		switch(URL_MATCHER.match(uri)) {
		case CHANNELS:
			return "vnd.android.cursor.dir/vnd.feeddroid.channel";
		case CHANNEL_ID:
			return "vnd.android.cursor.item/vnd.feeddroid.channel";
		case CHANNELICON_ID:
			return "image/x-icon";
		case POSTS:
		case CHANNEL_POSTS:
			return "vnd.android.cursor.dir/vnd.feeddroid.post";
		case POST_ID:
			return "vnd.android.cursor.item/vnd.feeddroid.post";
		case FOLDERS:
			return "vnd.android.cursor.dir/vnd.feeddroid.folder";
		case FOLDER_ID:
			return "vnd.android.cursor.item/vnd.feeddroid.folder";
		default:
			throw new IllegalArgumentException("Unknown URL: " + uri);
		}
	}
	
	private String getIconFilename(long channelId) {
		return "channel" + channelId + ".ico";
	}
	
	private String getIconPath(long channelId) {
		return getContext().getFileStreamPath(getIconFilename(channelId)).getAbsolutePath();
	}
	
	private void copyDefaultIcon(String path) 
		throws FileNotFoundException, IOException{
		FileOutputStream out = new FileOutputStream(path);
		
		InputStream ico =  
			getContext().getResources().openRawResource(R.drawable.rssorange);
		
		byte[] buf = new byte[1024];
		int n;
		
		while ((n = ico.read(buf)) != -1)
			out.write(buf, 0, n);
		
		ico.close();
		out.close();
	}

	public ParcelFileDescriptor openFile(Uri uri, String mode)
		throws FileNotFoundException {
		switch(URL_MATCHER.match(uri)) {
		case CHANNELICON_ID:
			long id = Long.valueOf(uri.getPathSegments().get(1));
			String path = getIconPath(id);
			
			if (mode.equals("rw") == true) {
				FileOutputStream foo = getContext().openFileOutput(getIconFilename(id), 0);
				
				try { foo.write(new byte[] {'t'}); foo.close(); }
				catch (Exception e) {}
			}
			
			File file = new File(path);
			int modeInt;
			
			if (mode.equals("rw") == true) {
				modeInt = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_TRUNCATE;
			} else {
				modeInt = ParcelFileDescriptor.MODE_READ_ONLY;
				
				if (file.exists() == false) {
					try {
						copyDefaultIcon(path);
					} catch (IOException e) {
						Log.d(TAG, "Unable to create default feed icon", e);
						return null;
					}
				}
			}
			
			return ParcelFileDescriptor.open(file, modeInt);
		default:
			throw new IllegalArgumentException("Unknown URL: " + uri);
		}
	}
	
	private long insertChannels(ContentValues values) {
		Resources r = Resources.getSystem();
		
		if (values.containsKey(FeedDroid.Channels.TITLE) == false)
			values.put(FeedDroid.Channels.TITLE, r.getString(android.R.string.untitled));
		
		long id = mDb.insert("channels", "title", values);
		
		
		if (values.containsKey(FeedDroid.Channels.ICON) == false) {
			Uri iconUri;
			
			iconUri = FeedDroid.Channels.CONTENT_URI.buildUpon()
				.appendPath(String.valueOf(id))
				.appendPath("icon")
				.build();
			
			ContentValues update = new ContentValues();
			update.put(FeedDroid.Channels.ICON, iconUri.toString());
			mDb.update("channels", update, "_id=" + id, null);
		}
		
		return id;
	}
	
	private long insertPosts(ContentValues values) {
		return mDb.insert("posts", "title", values);
	}
	
	private long insertFolders(ContentValues values) {
		return mDb.insert("folders", "name", values);
	}
	
	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		long rowId;
		ContentValues values;
		
		if (initialValues != null)
			values = new ContentValues(initialValues);
		else
			values = new ContentValues();
		
		Uri uri;
		
		if (URL_MATCHER.match(url) == CHANNELS) {
			rowId = insertChannels(values);
			uri = ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, rowId);
		} else if (URL_MATCHER.match(url) == POSTS) {
			rowId = insertPosts(values);
			uri = ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI, rowId);
		} else if (URL_MATCHER.match(url) == FOLDERS) {
			rowId = insertFolders(values);
			uri = ContentUris.withAppendedId(FeedDroid.Folders.CONTENT_URI, rowId);
		} else {
			throw new IllegalArgumentException("Unknown URL: " + url);
		}
		
		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		throw new SQLException("Failed to insert row into " + url);
	}

	@Override
	public boolean onCreate() {
		DbHelper helper = new DbHelper(getContext());
		try {
			mDb = helper.getWritableDatabase();
		} catch (SQLiteException e) {
			mDb = helper.getReadableDatabase();
		}
		
		return (mDb == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String defaultSort = null;
		String groupBy = null;
		String having = null;
		
		switch(URL_MATCHER.match(uri)) {
		case CHANNELS:
			qb.setTables("channels");
			qb.setProjectionMap(CHANNEL_LIST_PROJECTION);
			defaultSort = FeedDroid.Channels.DEFAULT_SORT_ORDER;
			break;
			
		case CHANNEL_ID:
			qb.setTables("channels");
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
		
		case POSTS:
			qb.setTables("posts");
			qb.setProjectionMap(POST_LIST_PROJECTION);
			groupBy = "_id";
			having = "COUNT(title) = 1";
			defaultSort = FeedDroid.Posts.DEFAULT_SORT_ORDER;
			break;
			
		case CHANNEL_POSTS:
			qb.setTables("posts");
			qb.appendWhere("channel_id=" + uri.getPathSegments().get(1));
			qb.setProjectionMap(POST_LIST_PROJECTION);
			defaultSort = FeedDroid.Posts.DEFAULT_SORT_ORDER;
			break;
			
		case POST_ID:
			qb.setTables("posts");
			groupBy = "_id";
			having = "COUNT(title) = 1";
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
			
		case FOLDERS:
			qb.setTables("folders");
			qb.setProjectionMap(FOLDER_LIST_PROJECTION);
			break;
			
		case FOLDER_ID:
			qb.setTables("folders");
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URL: " + uri);
		}
		
		String orderBy;
		
		if (TextUtils.isEmpty(sortOrder))
			orderBy = defaultSort;
		else
			orderBy = sortOrder;
		
		Cursor c = qb.query(mDb, projection, selection, selectionArgs, groupBy, having, orderBy);
		//Log.d(TAG, DatabaseUtils.dumpCursorToString(c));
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count;
		String where;
		
		switch(URL_MATCHER.match(uri)) {
		case CHANNELS:
			count = mDb.update("channels", values, selection, selectionArgs);
			break;
			
		case CHANNEL_ID:
			where = "_id=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			count = mDb.update("channels", values, where, selectionArgs);
			break;
			
		case POSTS:
			count = mDb.update("posts", values, selection, selectionArgs);
			break;
			
		case POST_ID:
			where = "_id=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			count = mDb.update("posts", values, where, selectionArgs);
			break;
		
		case FOLDERS:
			count = mDb.update("folders", values, selection, selectionArgs);
			break;
			
		case FOLDER_ID:
			where = "_id=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
			count = mDb.update("folders", values, where, selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URL: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URL_MATCHER.addURI(FeedDroid.AUTHORITY, "channels", CHANNELS);
		URL_MATCHER.addURI(FeedDroid.AUTHORITY, "channels/#", CHANNEL_ID);
		URL_MATCHER.addURI(FeedDroid.AUTHORITY, "channels/#/icon", CHANNELICON_ID);
		URL_MATCHER.addURI(FeedDroid.AUTHORITY, "posts", POSTS);
		URL_MATCHER.addURI(FeedDroid.AUTHORITY, "posts/#", POST_ID);
		URL_MATCHER.addURI(FeedDroid.AUTHORITY, "postlist/#", CHANNEL_POSTS);
		URL_MATCHER.addURI(FeedDroid.AUTHORITY, "folders", FOLDERS);
		URL_MATCHER.addURI(FeedDroid.AUTHORITY, "folders/#", FOLDER_ID);
		
		CHANNEL_LIST_PROJECTION = new HashMap<String, String>();
		CHANNEL_LIST_PROJECTION.put(FeedDroid.Channels._ID, "_id");
		CHANNEL_LIST_PROJECTION.put(FeedDroid.Channels.TITLE, "title");
		CHANNEL_LIST_PROJECTION.put(FeedDroid.Channels.URL, "url");
		CHANNEL_LIST_PROJECTION.put(FeedDroid.Channels.ICON, "icon");
		CHANNEL_LIST_PROJECTION.put(FeedDroid.Channels.LOGO, "logo");
		
		POST_LIST_PROJECTION = new HashMap<String, String>();
		POST_LIST_PROJECTION.put(FeedDroid.Posts._ID, "_id");
		POST_LIST_PROJECTION.put(FeedDroid.Posts.CHANNEL_ID, "channel_id");
		POST_LIST_PROJECTION.put(FeedDroid.Posts.READ, "read");
		POST_LIST_PROJECTION.put(FeedDroid.Posts.TITLE, "title");
		POST_LIST_PROJECTION.put(FeedDroid.Posts.URL, "url");
		POST_LIST_PROJECTION.put(FeedDroid.Posts.AUTHOR, "author");
		POST_LIST_PROJECTION.put(FeedDroid.Posts.DATE, "posted_on");
		POST_LIST_PROJECTION.put(FeedDroid.Posts.BODY, "body");
		POST_LIST_PROJECTION.put(FeedDroid.Posts.STARRED, "starred");
		
		FOLDER_LIST_PROJECTION = new HashMap<String, String>();
		FOLDER_LIST_PROJECTION.put(FeedDroid.Folders.NAME, "name");
		FOLDER_LIST_PROJECTION.put(FeedDroid.Folders.PARENT_ID, "parent_id");
	}
}
