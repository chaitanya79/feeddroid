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

package com.determinato.feeddroid.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.determinato.feeddroid.provider.FeedDroid;

public class RssParser extends DefaultHandler {
	private static final String TAG = "RssParser";
	private static final int STATE_IN_ITEM = (1 << 2);
	private static final int STATE_IN_ITEM_TITLE = (1 << 3);
	private static final int STATE_IN_ITEM_LINK = (1 << 4);
	private static final int STATE_IN_ITEM_DESC = (1 << 5);
	private static final int STATE_IN_ITEM_DATE = (1 << 6);
	private static final int STATE_IN_ITEM_AUTHOR = (1 << 7);
	private static final int STATE_IN_TITLE = (1 << 8);
	private static final int STATE_MEDIA_CONTENT = (1 << 9);
	
	private static HashMap<String, Integer> mStateMap;
	
	private Handler mHandler;
	private String mRssUrl;
	private ContentResolver mResolver;
	private ChannelPost mPostBuf;
	private long mId;
	private long mFolderId;
	private int mState;

	
	
	static {
		mStateMap = new HashMap<String, Integer>();
		mStateMap.put("item", STATE_IN_ITEM);
		mStateMap.put("entry", STATE_IN_ITEM);
		mStateMap.put("title", STATE_IN_ITEM_TITLE);
		mStateMap.put("link", STATE_IN_ITEM_LINK);
		mStateMap.put("feedburner:origLink", STATE_IN_ITEM_LINK);
		mStateMap.put("description", STATE_IN_ITEM_DESC);
		mStateMap.put("summary", STATE_IN_ITEM_DESC);
		mStateMap.put("content", STATE_IN_ITEM_DESC);
		mStateMap.put("content:encoded", STATE_IN_ITEM_DESC);
		mStateMap.put("dc:date", STATE_IN_ITEM_DATE);
		mStateMap.put("updated", STATE_IN_ITEM_DATE);
		mStateMap.put("modified", STATE_IN_ITEM_DATE);
		mStateMap.put("pubDate", STATE_IN_ITEM_DATE);
		mStateMap.put("dc:author", STATE_IN_ITEM_AUTHOR);
		mStateMap.put("author", STATE_IN_ITEM_AUTHOR);
		mStateMap.put("name", STATE_IN_ITEM_AUTHOR);
		mStateMap.put("media:content", STATE_MEDIA_CONTENT);
	}
	
	public RssParser(ContentResolver resolver) {
		super();
		mResolver = resolver;
	}
	
	public long syncDb(Handler handler, long id, String rssurl) 
		throws Exception {
		mHandler = handler;
		return syncDb(id, rssurl);
	}
	
	public long syncDb(long id, String rssurl) throws Exception {
		long folderId = 1;
		return syncDb(id, folderId, rssurl);
	}
	
	public long syncDb(long id, long folderId, String rssurl) 
		throws Exception {
		mId = id;
		mFolderId = folderId;
		mRssUrl = rssurl;
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		
		reader.setContentHandler(this);
		reader.setErrorHandler(this);
		
		URL url = new URL(mRssUrl);
		
		URLConnection c = url.openConnection();
		// TODO: Is this a known user agent, or do I need to come up with my own?
		c.setRequestProperty("User-Agent", "Android/m3-rc37a");
		
		try {
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(c.getInputStream()), 65535);
			reader.parse(new InputSource(bufReader));
		} catch (NullPointerException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			Log.e(TAG, "Failed to load URL");
		}
		
		return mId;
	}
	
	public boolean updateFavicon(long id, String iconUrl) 
		throws MalformedURLException {
		return updateFavicon(id, new URL(iconUrl));
	}
	
	public boolean updateFavicon(long id, URL iconUrl) {
		InputStream in = null;
		OutputStream out = null;
		
		boolean r = false;
		
		Uri iconUri = FeedDroid.Channels.CONTENT_URI
			.buildUpon()
			.appendPath(String.valueOf(id))
			.appendPath("icon")
			.build();
		
		try {
			in = iconUrl.openStream();
			
			out = mResolver.openOutputStream(iconUri);
			
			byte[] b = new byte[1024];
			
			int n;
			
			while ((n = in.read(b)) != -1)
				out.write(b, 0, n);
			
			r = true;
		} catch (Exception e) {
			Log.d(TAG, Log.getStackTraceString(e));
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {}
		}
		
		return r;
	}

	public void startElement(String uri, String name, String qName, Attributes attrs) {
		
		if (mId == -1 &&
				name.equals("title") && (mState & STATE_IN_ITEM) == 0) {
			mState |= STATE_IN_TITLE;
			return;
		}
		

		Integer state = mStateMap.get(name);
		
		if (state != null) {
			mState |= state.intValue();
			
			if (state.intValue() == STATE_IN_ITEM)
				mPostBuf = new ChannelPost();
			else if ((mState & STATE_IN_ITEM) != 0 && state.intValue() == STATE_IN_ITEM_LINK) {
				String href = attrs.getValue("href");
			
				if (href != null)
					mPostBuf.link = href;
			}
		}
	}
	
	public void endElement(String uri, String name, String qName) {
		Integer state = mStateMap.get(name);
		
		if (state != null) {
			mState &= ~(state.intValue());
			
			if (state.intValue() == STATE_IN_ITEM) {
				if (mId == -1) {
					Log.d(TAG, "</item> found before feed title.");
					return;
				}

				ContentValues values = new ContentValues();
					
				values.put(FeedDroid.Posts.CHANNEL_ID, mId);
				values.put(FeedDroid.Posts.TITLE, mPostBuf.title);
				values.put(FeedDroid.Posts.URL, mPostBuf.link);
				values.put(FeedDroid.Posts.AUTHOR, mPostBuf.author);
				values.put(FeedDroid.Posts.DATE, mPostBuf.getDate());
				values.put(FeedDroid.Posts.BODY, reEncodeHtml(mPostBuf.desc));
					
				try {
					mResolver.insert(FeedDroid.Posts.CONTENT_URI, values);
				} catch (SQLException e) {
					// Eating the exception since it's likely due to a duplicate post.
				}
			}
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		// Are we in the Channel or in a Post?
		if ((mId == -1) && (mState & STATE_IN_TITLE) != 0) {
			
			ContentValues values = new ContentValues();
			
			values.put(FeedDroid.Channels.TITLE, new String(ch, start, length));
			values.put(FeedDroid.Channels.URL, mRssUrl);
			values.put(FeedDroid.Channels.FOLDER_ID, mFolderId);
			
			Uri added = mResolver.insert(FeedDroid.Channels.CONTENT_URI, values);
			
			mId = Long.parseLong(added.getPathSegments().get(1));
			
			mState &= ~STATE_IN_TITLE;
			
			return;
		}
		
		if ((mState & STATE_IN_ITEM) == 0)
			return;
		
		StringBuilder str = new StringBuilder();
		switch(mState) {
		case STATE_IN_ITEM | STATE_IN_ITEM_TITLE:
			str.append(new String(ch, start, length).trim());
			if (mPostBuf.title == null)
				mPostBuf.title = str.toString();
			else
				mPostBuf.title += str.toString();
			break;
		case STATE_IN_ITEM | STATE_IN_ITEM_DESC:
			str.append(new String(ch, start, length).trim());
			if (mPostBuf.desc == null)
				mPostBuf.desc = str.toString();
			else
				mPostBuf.desc += str.toString();
			break;
		case STATE_IN_ITEM | STATE_IN_ITEM_LINK:
			mPostBuf.link = new String(ch, start, length).trim();
			break;
		case STATE_IN_ITEM | STATE_IN_ITEM_DATE:
			mPostBuf.setDate(new String(ch, start, length).trim());
			break;
		case STATE_IN_ITEM | STATE_IN_ITEM_AUTHOR:
			mPostBuf.author = new String(ch, start, length).trim();
			break;
		default:
		}
	}
	
	private String reEncodeHtml(String str) {
		StringBuilder builder = new StringBuilder();
		String[] sources = new String[] {"&gt;", "&lt;", "&amp;"};
		String[] dests = new String[] {">", "<", "&"};
		builder.append(TextUtils.replace(str, sources, dests));
		return builder.toString();
	}
	
	private class ChannelPost {
		public String title;
		public Date date;
		public String desc;
		public String link;
		public String author;
		
		public ChannelPost() {}
		
		private String strDate;
		public void setDate(String str) {
			
			
			try {
			
				date = DateUtils.parseDate(str);
			}
			catch (DateParseException e) {
				Log.i(TAG, "Unable to parse date.  Defaulting to system.");
				date = new Date();
			}
			if (date == null)
				date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			strDate = dateFormat.format(date);
			
		}
		
		public String getDate() {
			return strDate;
		}
	}

	@Override
	public void endDocument() throws SAXException {
		Log.d(TAG, "Parsing of " + mRssUrl + " finished.");
		super.endDocument();
	}

	@Override
	public void startDocument() throws SAXException {
		Log.d(TAG, "Parsing RSS XML: " + mRssUrl);
		super.startDocument();
	}
	
}
