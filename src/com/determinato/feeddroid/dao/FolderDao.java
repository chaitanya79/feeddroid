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

public class FolderDao implements FolderItemDao {
	private long mId;
	private long mParentId;
	private String mTitle;
	
	public FolderDao() {
		mTitle = "";
	}
	
	public FolderDao(long id, long parentId, String title) {
		mId = id;
		mParentId = parentId;
		mTitle = title;
	}
	
	public long getId() {
		return mId;
	}
	
	public void setId(long id) {
		mId = id;
	}
	
	public long getParentId() {
		return mParentId;
	}
	
	public void setParentId(long id) {
		mParentId = id;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}

}
