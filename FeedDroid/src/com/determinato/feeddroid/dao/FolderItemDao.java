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
 * Interface to represent an RSS entity (either a folder or channel).
 * @author John R. Hicks <john@determinato.com>
 *
 */
public interface FolderItemDao {
	/** 
	 * Returns ID.
	 * @return long containing ID
	 */
	long getId();
	
	/**
	 * Returns title.
	 * @return String containing title
	 */
	String getTitle();
}
