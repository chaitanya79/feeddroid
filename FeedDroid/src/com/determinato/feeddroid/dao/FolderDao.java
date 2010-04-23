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
