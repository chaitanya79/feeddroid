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

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.text.TextUtils;
import android.util.Log;

import com.determinato.feeddroid.provider.FeedDroid;

/**
 * Class to handle parsing of OPML XML files, 
 * which Google Reader exports its feeds in.
 * 
 * @author jhicks
 *
 */
public class OPMLParser extends DefaultHandler implements FeedParser {
	private static final String TAG = "OPMLParser";
	
	private ContentResolver mResolver;
	private ArrayList<ImportedFeed> mFeeds;
	
	public OPMLParser(ContentResolver resolver) {
		mResolver = resolver;
		mFeeds = new ArrayList<ImportedFeed>();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		if (localName.equals("outline")) {
			ImportedFeed feed = new ImportedFeed();
			feed.text = attributes.getValue("text");
			feed.title = attributes.getValue("title");
			feed.type = attributes.getValue("type");	
			feed.xmlUrl = attributes.getValue("xmlUrl");
			feed.htmlUrl = attributes.getValue("htmlUrl");
			mFeeds.add(feed);
		}
		
	}
	
	public void importFeed(File opmlFile) 
		throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		reader.setContentHandler(this);
		reader.setErrorHandler(this);
		
		reader.parse(new InputSource(new FileReader(opmlFile)));
		syncDb();
	}
	
	private void syncDb() throws Exception {
		for (ImportedFeed feed : mFeeds) {
			// TODO add code for folder support
			
			if (!TextUtils.isEmpty(feed.xmlUrl)) {
				ContentValues values = new ContentValues();
				values.put("title", feed.title);
				values.put("url", feed.xmlUrl);
				
				try {
					mResolver.insert(FeedDroid.Channels.CONTENT_URI, values);
				} catch(SQLiteConstraintException e) {
					Log.e(TAG, "Ignoring duplicate feed: " + feed.xmlUrl);
				}
			}
		}
	}
	
	public class ImportedFeed {
		public String text;
		public String title;
		public String type;
		public String xmlUrl;
		public String htmlUrl;
	}
}
