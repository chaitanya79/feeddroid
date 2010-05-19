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
 * Data access object for folders.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class FolderDao implements FolderItemDao {
	private long mId;
	private long mParentId;
	private String mTitle;
	
	/**
	 * Constructor.
	 */
	public FolderDao() {
		mTitle = "";
	}
	
	/**
	 * Constructor
	 * @param id ID of folder
	 * @param parentId ID of parent folder
	 * @param title name of folder
	 */
	public FolderDao(long id, long parentId, String title) {
		mId = id;
		mParentId = parentId;
		mTitle = title;
	}
	
	/**
	 * Returns folder ID.
	 * @return long containing ID
	 */
	public long getId() {
		return mId;
	}
	
	/**
	 * Sets folder ID.
	 * @param id long containing new ID
	 */
	public void setId(long id) {
		mId = id;
	}
	
	/**
	 * Returns ID of parent folder.
	 * @return long containing ID
	 */
	public long getParentId() {
		return mParentId;
	}

	/**
	 * Sets ID of parent folder.
	 * @param id long containing new ID
	 */
	public void setParentId(long id) {
		mParentId = id;
	}
	
	/**
	 * Returns folder title.
	 * @return String containing title
	 */
	public String getTitle() {
		return mTitle;
	}
	
	/**
	 * Sets folder title.
	 * 
	 * @param title String containing new title
	 */
	public void setTitle(String title) {
		mTitle = title;
	}

}
