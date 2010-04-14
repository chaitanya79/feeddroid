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

public class FeedDroid {
	public static final String AUTHORITY = "com.determinato.provider.feeddroidprovider";
	
	public interface Channels extends BaseColumns {
		public static final Uri CONTENT_URI =
			Uri.parse("content://" + AUTHORITY + "/channels");
		
		public static final String DEFAULT_SORT_ORDER = "title ASC";
		
		public static final String TITLE = "title";
		public static final String URL = "url";
		public static final String ICON = "icon";
		public static final String ICON_URL = "icon_url";
		public static final String LOGO = "logo";
		public static final String FOLDER_ID = "folder_id";
	}
	
	public interface Posts extends BaseColumns {
		public static final Uri CONTENT_URI =
			Uri.parse("content://" + AUTHORITY + "/posts");
		public static final Uri CONTENT_URI_LIST =
			Uri.parse("content://" + AUTHORITY + "/postlist");
		
		public static final String DEFAULT_SORT_ORDER = "posted_on DESC";
		
		public static final String CHANNEL_ID = "channel_id";
		public static final String READ = "read";
		public static final String TITLE = "title";
		public static final String AUTHOR = "author";
		public static final String URL = "url";
		public static final String BODY = "body";
		public static final String DATE = "posted_on";
		public static final String STARRED = "starred";
	}
	
	public interface Folders extends BaseColumns {
		public static final Uri CONTENT_URI =
			Uri.parse("content://" + AUTHORITY + "/folders");
		public static final String NAME = "name";
		public static final String PARENT_ID = "parent_id";
	}
}
