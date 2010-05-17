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
package com.determinato.feeddroid.adapters;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;

import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.view.ChannelListRow;

/**
 * Adapter for RSS channel list.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class ChannelListAdapter extends CursorAdapter implements Filterable {
	private HashMap<Long, ChannelListRow> rowMap;
	
	/**
	 * Constructor.
	 * @param ctx Context of ListView
	 * @param c Cursor containing data to display.
	 */
	public ChannelListAdapter(Context ctx, Cursor c) {
		super(ctx, c);
		
		rowMap = new HashMap<Long, ChannelListRow>();
	}
	
	/**
	 * Updates row in list.
	 * @param c Cursor containing data
	 * @param row View to update
	 */
	protected void updateRowMap(Cursor c, ChannelListRow row) {
		long channelId =
			c.getLong(c.getColumnIndex(FeedDroid.Channels._ID));
		rowMap.put(new Long(channelId), row);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override 
	public void bindView(View v, Context ctx, Cursor c) {
		ChannelListRow row = (ChannelListRow) v;
		row.bindView(c);
		updateRowMap(c, row);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public View newView(Context ctx, Cursor c, ViewGroup parent) {
		ChannelListRow row = new ChannelListRow(ctx);
		row.bindView(c);
		updateRowMap(c, row);
		return row;
	}
	
	/**
	 * Find row by its ID.
	 * @param id ID to locate
	 * @return View of data for selected ID.
	 */
	public ChannelListRow getViewByRowId(long id) {
		return rowMap.get(id);
	}
	
}
