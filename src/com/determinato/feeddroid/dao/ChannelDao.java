package com.determinato.feeddroid.dao;

public class ChannelDao implements FolderItemDao {
	private Long mId;
	private Long mFolderId;
	private String mTitle;
	private String mUrl;
	private String mIcon;
	
	public long getId() {
		return mId;
	}
	
	public void setId(long id) {
		mId = id;
	}
	
	public long getFolderId() {
		return mFolderId;
	}
	
	public void setFolderId(long id) {
		mFolderId = id;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public void setUrl(String url) {
		mUrl = url;
	}
	
	public String getIcon() {
		return mIcon;
	}
	
	public void setIcon(String icon) {
		mIcon = icon;
	}
}
