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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.util.Log;

import com.determinato.feeddroid.provider.FeedDroid;

public class GoogleReaderImporter implements FeedParser {
	private static final String TAG = "GoogleReaderImporter";
	
	private static final int ROOT_FOLDER = 1;
	
	private ContentResolver mResolver;
	private long parentFolder = ROOT_FOLDER;
	private long previousParent = ROOT_FOLDER;
	
	public GoogleReaderImporter(ContentResolver resolver) {
		mResolver = resolver;
	}
	
	public void importFeed(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		
		Element root = doc.getDocumentElement();
		NodeList bodyList = root.getElementsByTagName("body");
		Node body = bodyList.item(0);
		NodeList children = body.getChildNodes();
		processChildren(children);
		
	}

	private void processFolder(Node node) {
		NamedNodeMap atts = node.getAttributes();
		ContentValues values = new ContentValues();
		values.put("name", atts.getNamedItem("title").getNodeValue());
		values.put("parent_id", parentFolder);
		Uri uri = null;
		try {
			uri = mResolver.insert(FeedDroid.Folders.CONTENT_URI, values);
		} catch (SQLiteConstraintException e) {
			Log.d(TAG, "ignoring duplicate folder");
		}
		previousParent = parentFolder;
		parentFolder = new Long(uri.getPathSegments().get(1));
		NodeList children = node.getChildNodes();
		processChildren(children);
		parentFolder = previousParent;
	}
	
	private void processChildren(NodeList children) {
		
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String name = child.getNodeName();
			
			if (name.equalsIgnoreCase("outline")) {
				NamedNodeMap atts = child.getAttributes();
				
				if (atts.getNamedItem("xmlUrl") == null) {
					processFolder(child);
				} else {
					ContentValues values = new ContentValues();
					values.put("title", atts.getNamedItem("title").getNodeValue());
					values.put("url", atts.getNamedItem("xmlUrl").getNodeValue());
					values.put("folder_id", parentFolder);
					
					try {
						mResolver.insert(FeedDroid.Channels.CONTENT_URI, values);
					} catch (SQLiteConstraintException e) {
						Log.d(TAG, "ignoring duplicate channel");
					}
				}
			}
		}
		
	}
}
