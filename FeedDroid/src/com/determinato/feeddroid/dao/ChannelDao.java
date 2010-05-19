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
package com.determinato.feeddroid.dao;

/**
 * Data access object for RSS channel.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class ChannelDao implements FolderItemDao {
	private Long mId;
	private Long mFolderId;
	private String mTitle;
	private String mUrl;
	private String mIcon;
	
	/**
	 * Returns channel ID.
	 * 
	 * @return long containing ID
	 */
	public long getId() {
		return mId;
	}
	
	/**
	 * Sets channel ID.
	 * @param id long containing new ID
	 */
	public void setId(long id) {
		mId = id;
	}
	
	/**
	 * Returns ID of channel's containing folder.
	 * @return long containing ID
	 */
	public long getFolderId() {
		return mFolderId;
	}
	
	/**
	 * Sets ID of channel's containing folder.
	 * @param id long containing folder ID
	 */
	public void setFolderId(long id) {
		mFolderId = id;
	}
	
	/**
	 * Returns channel title.
	 * @return String containing channel title
	 * 
	 */
	public String getTitle() {
		return mTitle;
	}
	
	/**
	 * Sets channel title.
	 * @param title String containing new title
	 */
	public void setTitle(String title) {
		mTitle = title;
	}
	
	/**
	 * Returns URL of channel.
	 * @return String containing URL
	 */
	public String getUrl() {
		return mUrl;
	}
	
	/**
	 * Sets channel URL.
	 * @param url String containing new URL
	 */
	public void setUrl(String url) {
		mUrl = url;
	}
	
	/**
	 * Returns filename of channel's icon.
	 * @return String containing filename
	 */
	public String getIcon() {
		return mIcon;
	}
	
	/**
	 * Sets filename of channel icon.
	 * @param icon String containing name
	 */
	public void setIcon(String icon) {
		mIcon = icon;
	}
}
