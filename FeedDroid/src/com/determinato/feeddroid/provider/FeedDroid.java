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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Column header and URI definitions.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class FeedDroid {
	/** Base URI authority identifier */
	public static final String AUTHORITY = "com.determinato.provider.feeddroidprovider";
	
	/** RSS channel URI and column header definitions */
	public interface Channels extends BaseColumns {
		/** Main content URI */
		public static final Uri CONTENT_URI =
			Uri.parse("content://" + AUTHORITY + "/channels");
		
		/** Default column sort order */
		public static final String DEFAULT_SORT_ORDER = "title ASC";
		
		/** Channel title column header */
		public static final String TITLE = "title";
		/** Channel URL column header */
		public static final String URL = "url";
		/** Channel Icon column header */
		public static final String ICON = "icon";
		/** Channel Icon URL column header */
		public static final String ICON_URL = "icon_url";
		/** Channel logo column header */
		public static final String LOGO = "logo";
		/** Folder id column header */
		public static final String FOLDER_ID = "folder_id";
	}
	
	/** RSS post URI and column header definitions */
	public interface Posts extends BaseColumns {
		/** Main content URI */
		public static final Uri CONTENT_URI =
			Uri.parse("content://" + AUTHORITY + "/posts");
		/** Post list content URI */
		public static final Uri CONTENT_URI_LIST =
			Uri.parse("content://" + AUTHORITY + "/postlist");
		/** Unread posts content URI */
		public static final Uri CONTENT_LIST_UNREAD =
			Uri.parse("content://" + AUTHORITY + "/unread");
		/** Default sort order */
		public static final String DEFAULT_SORT_ORDER = "posted_on DESC";
		
		/** Post id column header */
		public static final String CHANNEL_ID = "channel_id";
		/** Post read column header */
		public static final String READ = "read";
		/** Post title column header */
		public static final String TITLE = "title";
		/** Post author column header */
		public static final String AUTHOR = "author";
		/** Post URL column header */
		public static final String URL = "url";
		/** Post body column header */
		public static final String BODY = "body";
		/** Post posted on column header */
		public static final String DATE = "posted_on";
		/** Post starred column header */
		public static final String STARRED = "starred";
	}
	
	/** Folder URI and column header definitions */
	public interface Folders extends BaseColumns {
		/** Main content URI */
		public static final Uri CONTENT_URI =
			Uri.parse("content://" + AUTHORITY + "/folders");
		/** Folder name column header */
		public static final String NAME = "name";
		/** Folder parent id column header */
		public static final String PARENT_ID = "parent_id";
	}
}
