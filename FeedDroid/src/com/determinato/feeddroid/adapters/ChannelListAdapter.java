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

public class ChannelListAdapter extends CursorAdapter implements Filterable {
	private HashMap<Long, ChannelListRow> rowMap;
	
	
	public ChannelListAdapter(Context ctx, Cursor c) {
		super(ctx, c);
		
		rowMap = new HashMap<Long, ChannelListRow>();
	}
	
	protected void updateRowMap(Cursor c, ChannelListRow row) {
		long channelId =
			c.getLong(c.getColumnIndex(FeedDroid.Channels._ID));
		rowMap.put(new Long(channelId), row);
	}
	
	@Override 
	public void bindView(View v, Context ctx, Cursor c) {
		ChannelListRow row = (ChannelListRow) v;
		row.bindView(c);
		updateRowMap(c, row);
	}
	
	@Override
	public View newView(Context ctx, Cursor c, ViewGroup parent) {
		ChannelListRow row = new ChannelListRow(ctx);
		row.bindView(c);
		updateRowMap(c, row);
		return row;
	}
	
	public ChannelListRow getViewByRowId(long id) {
		return rowMap.get(id);
	}
	
}
