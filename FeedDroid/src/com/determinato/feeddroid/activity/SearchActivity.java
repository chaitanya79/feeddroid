package com.determinato.feeddroid.activity;


import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.provider.FeedDroid;

public class SearchActivity extends ListActivity {
	private static final String TAG = "SearchActivity";
	private TextView mTextView;
	private ListView mList;
	private ArrayList<SearchResult> mSearchResults;
	private ContentResolver mResolver;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.search);
		mSearchResults = new ArrayList<SearchResult>();
		mResolver = getContentResolver();
		final Intent intent = getIntent();
		final String searchAction = intent.getAction();
	
		if (Intent.ACTION_SEARCH.equals(searchAction)) {
			String searchTerms = intent.getStringExtra(SearchManager.QUERY);
			doSearch(searchTerms);
		}
	}
	
	private void doSearch(String query) {
		String[] projection = new String[] {
			FeedDroid.Posts._ID, FeedDroid.Posts.CHANNEL_ID,
			FeedDroid.Posts.TITLE, FeedDroid.Posts.BODY
		};
		
		Cursor c = mResolver.query(FeedDroid.Posts.CONTENT_URI,
				projection, "title LIKE '%" + query + "%' OR body LIKE '%" + query + "%'",
				null, null);
		if (c.getCount() == 0)
			return;
		
		c.moveToFirst();
		do {
			SearchResult result = new SearchResult();
			result.id = c.getLong(c.getColumnIndex(FeedDroid.Posts._ID));
			result.channel_id = c.getLong(c.getColumnIndex(FeedDroid.Posts.CHANNEL_ID));
			result.title = c.getString(c.getColumnIndex(FeedDroid.Posts.TITLE));
			result.body = c.getString(c.getColumnIndex(FeedDroid.Posts.BODY));
			
			// Search all the other results for the same post ID and if so, don't add it.
			boolean isAdded = false;
			
			for (SearchResult subResult: mSearchResults) {
				if (result.id == subResult.id)
					isAdded = true;
			}
			
			if (!isAdded)
				mSearchResults.add(result);
		} while(c.moveToNext());
		getListView().setAdapter(new SearchResultAdapter(this, android.R.layout.simple_list_item_1,
				mSearchResults));
	}
	
	
	class SearchResultAdapter extends ArrayAdapter<SearchResult>  {
		private List<SearchResult> mResults;
		private Context mContext;
		
		SearchResultAdapter(Context ctx, int resourceId, List<SearchResult> results) {
			super(ctx, resourceId, results);
			mResults = results;
			mContext = ctx;
		}

		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = null;
			
			if (convertView != null)
				view = (TextView) convertView;
			else {
				view = new TextView(mContext);
				view.setTypeface(Typeface.DEFAULT_BOLD);
			}
			
			SearchResult result = mResults.get(position);
			
			view.setText(result.title);
			return view;
		}

	}

	
	class SearchResult {
		public long id;
		public long channel_id;
		public String title;
		public String body;
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		SearchResult result = mSearchResults.get(position);
		
		Uri uri =
			ContentUris.withAppendedId(FeedDroid.Posts.CONTENT_URI, result.id);
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);
		
	}
}
