package com.determinato.feeddroid.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.determinato.feeddroid.R;
import com.determinato.feeddroid.adapters.FolderListCursorAdapter;
import com.determinato.feeddroid.ads.FeedDroidAdConstants;
import com.determinato.feeddroid.ads.FeedDroidAdSenseSpec;
import com.determinato.feeddroid.dao.ChannelDao;
import com.determinato.feeddroid.dao.FolderDao;
import com.determinato.feeddroid.dao.FolderItemDao;
import com.determinato.feeddroid.provider.FeedDroid;
import com.determinato.feeddroid.view.FolderListRow;
import com.google.ads.AdSenseSpec;
import com.google.ads.GoogleAdView;
import com.google.ads.AdSenseSpec.AdType;
import com.google.ads.AdSenseSpec.ExpandDirection;

/**
 * List of folders and RSS channels for a given folder ID.
 * 
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class FolderListActivity extends ListActivity {
	private static final String TAG = "FolderListActivity";
	private static final String[] FOLDER_PROJECTION = new String[] {FeedDroid.Folders._ID, 
		FeedDroid.Folders.NAME, FeedDroid.Folders.PARENT_ID};
	private static final String[] CHANNEL_PROJECTION = new String[] {FeedDroid.Channels._ID,
		FeedDroid.Channels.TITLE, FeedDroid.Channels.URL, FeedDroid.Channels.ICON,
		FeedDroid.Channels.FOLDER_ID };

	private static final int SHOW_PREFERENCES = 1;
	private static final int SHOW_MOVE = 2;
	private static final int ADD_CHANNEL_ID = R.id.menu_new_channel;
	private static final int ADD_FOLDER_ID = R.id.menu_new_folder;
	private static final int SEARCH_ID = R.id.menu_search;
	private static final int PREFS_ID = Menu.FIRST;
	private static final int DELETE_ID = R.id.remove_channel;
	private static final int EDIT_ID = R.id.edit_channel;
	private static final int MOVE_ID = R.id.move_channel;
	private Cursor mFolderCursor;
	private Cursor mChannelCursor;
	
	private long mFolderId;
	private int mSelectedRow;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_list);
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		if (intent.getAction() == null)
			intent.setAction(Intent.ACTION_VIEW);
		
		mFolderId = Long.parseLong(uri.getPathSegments().get(1));
		
		mFolderCursor = managedQuery(FeedDroid.Folders.CONTENT_URI, 
				FOLDER_PROJECTION, "parent_id=" + mFolderId, null, null);
		mChannelCursor = managedQuery(FeedDroid.Channels.CONTENT_URI, 
				CHANNEL_PROJECTION, "folder_id=" + mFolderId, null, null);
		registerForContextMenu(getListView());
		
		FolderListCursorAdapter adapter = 
			new FolderListCursorAdapter(this, mFolderCursor, mChannelCursor);
		setListAdapter(adapter);
		
        // Set up ads
	      AdSenseSpec spec = new FeedDroidAdSenseSpec(FeedDroidAdConstants.CLIENT_ID)
	        	.setCompanyName(FeedDroidAdConstants.COMPANY_NAME)
	        	.setAppName(getString(R.string.app_name))
	        	.setChannel(FeedDroidAdConstants.CHANNEL_ID)
	        	.setAdType(AdType.TEXT_IMAGE)
	        	.setExpandDirection(ExpandDirection.TOP)
	        	.setAdTestEnabled(FeedDroidAdConstants.AD_TEST_ENABLED);
	      
	      	GoogleAdView adView = (GoogleAdView) findViewById(R.id.adview);
	      	adView.showAds(spec);		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = null;
		
		FolderItemDao data = (FolderItemDao) getListView().getItemAtPosition(position);
		
		if (data instanceof ChannelDao) {
			Long channelId = ((ChannelDao)data).getId();
			Uri uri = Uri.withAppendedPath(FeedDroid.Posts.CONTENT_URI_LIST, channelId.toString());
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		} else if (data instanceof FolderDao) {
			Long folderId = ((FolderDao)data).getId();
			Uri uri = Uri.withAppendedPath(FeedDroid.Folders.CONTENT_URI, folderId.toString());
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.channel_list_menu, menu);
		
		menu.add(0, SHOW_PREFERENCES, Menu.NONE, getString(R.string.prefs))
		.setIcon(android.R.drawable.ic_menu_preferences);
		
		MenuItem searchItem = menu.findItem(R.id.menu_search);
		searchItem.setAlphabeticShortcut(SearchManager.MENU_KEY);
		
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		
		
		switch(item.getItemId()) {
		case ADD_FOLDER_ID:
			i = new Intent(this, FolderAddActivity.class);
			i.setAction(Intent.ACTION_INSERT);
			i.setData(Uri.parse("vnd.android.cursor.dir/vnd.feeddroid.folder"));
			startActivityForResult(i, RESULT_OK);
			return true;
		case ADD_CHANNEL_ID:
			i = new Intent(this, ChannelAddActivity.class);
			i.setAction(Intent.ACTION_INSERT);
			i.setData(Uri.parse("vnd.android.cursor.dir/vnd.feeddroid.channel"));
			i.putExtra("folderId", mFolderId);
			startActivityForResult(i, RESULT_OK);
			return true;
		case PREFS_ID:
			i = new Intent(this, PreferencesActivity.class);
			startActivityForResult(i, SHOW_PREFERENCES);
			return true;
		case SEARCH_ID:
			onSearchRequested();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResume() {
		super.onResume();
		FolderListCursorAdapter adapter = 
			new FolderListCursorAdapter(this, mFolderCursor, mChannelCursor);
		setListAdapter(adapter);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.channel_list_context_menu, menu);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		FolderItemDao backing = ((FolderListRow) info.targetView).getBacking();
		if (backing instanceof FolderDao) {
			MenuItem item = menu.findItem(EDIT_ID);
			item.setVisible(false);
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		FolderItemDao backing = ((FolderListRow)info.targetView).getBacking();
		long id = backing.getId();
		switch (item.getItemId()) {
		case EDIT_ID:
			
			if (((FolderListRow)info.targetView).getItemType() == FolderListRow.CHANNEL_VIEW) {
				startActivity(new Intent(Intent.ACTION_EDIT,
						ContentUris.withAppendedId(FeedDroid.Channels.CONTENT_URI, id)));
			}
			return true;
		case DELETE_ID:
			if (((FolderListRow)info.targetView).getItemType() == FolderListRow.CHANNEL_VIEW) {
				removeChannel(id);
			} else if (((FolderListRow)info.targetView).getItemType() == FolderListRow.FOLDER_VIEW) {
				removeFolder(id);
				
				
			}
			return true;
		case MOVE_ID:
			mSelectedRow = info.position;
			showDialog(SHOW_MOVE);
			return true;

		default:
			return false;
		}
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dialog onCreateDialog(int id) {
		switch(id) {
		case SHOW_MOVE:
			LayoutInflater inflater = LayoutInflater.from(this);
			View moveDialogView = inflater.inflate(R.layout.move_item, null);
			AlertDialog.Builder moveDialog = new AlertDialog.Builder(this);
			moveDialog.setTitle(getString(R.string.move_channel));
			moveDialog.setView(moveDialogView);
			
			final Spinner folderSpinner = (Spinner) moveDialogView.findViewById(R.id.move_channel_spinner);
			Cursor c = managedQuery(FeedDroid.Folders.CONTENT_URI, 
					new String[] {FeedDroid.Folders._ID, FeedDroid.Folders.NAME}, 
					null, null, null);
			
			String[] columns = new String[] {FeedDroid.Folders.NAME};
			int[] to = new int[] {android.R.id.text1};
			
			SimpleCursorAdapter adapter = 
				new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, 
						c, columns, to);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			folderSpinner.setAdapter(adapter);
			
			moveDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					
					ContentValues values = new ContentValues();
					long folderId = folderSpinner.getSelectedItemId();
					Log.d(TAG, "selected row: " + mSelectedRow);
					FolderItemDao dao = (FolderItemDao) getListView().getItemAtPosition(mSelectedRow);
					
					
					long id = dao.getId();
					Log.d(TAG, "id: " + id);
					
					if (dao instanceof FolderDao) {
						values.put(FeedDroid.Folders.PARENT_ID, folderId);
						getContentResolver().update(FeedDroid.Folders.CONTENT_URI, values, "_id=" + id, null);
					} else {
						values.put(FeedDroid.Channels.FOLDER_ID, folderId);
						getContentResolver().update(FeedDroid.Channels.CONTENT_URI, values, "_id" + id, null);
					}
				}
			});
			
			moveDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			
			return moveDialog.create();
		}
		
		initAdapter();
		return null;
	}
	
	/**
	 * Initializes/resets List adapter.
	 */
	private void initAdapter() {
		getListView().setAdapter(new FolderListCursorAdapter(this, mFolderCursor, mChannelCursor));
	}

	/**
	 * Removes a folder from the database.
	 * 
	 * <p>If the folder contains items, they will be moved to the root folder.</p>
	 * 
	 * @param folderId ID of folder to remove
	 */
	private void removeFolder(final long folderId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to remove this folder?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ContentResolver resolver = getContentResolver();
					resolver.delete(FeedDroid.Folders.CONTENT_URI, "_id=" + folderId, null);
					
					// Move all channels in this folder to the home folder.
					ContentValues values = new ContentValues();
					values.put("folder_id", 1);
					resolver.update(FeedDroid.Channels.CONTENT_URI, values, "folder_id=" + folderId, null);
					onResume();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
		builder.create().show();
		
	}
	
	/**
	 * Removes an RSS channel from the database.
	 * 
	 * @param channelId ID of channel to remove
	 */
	private void removeChannel(final long channelId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to remove this channel?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ContentResolver resolver = getContentResolver();
					resolver.delete(FeedDroid.Posts.CONTENT_URI, "channel_id=" + channelId, null);
					resolver.delete(FeedDroid.Channels.CONTENT_URI, "_id=" + channelId, null);
					onResume();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
		builder.create().show();
	}
	
}
