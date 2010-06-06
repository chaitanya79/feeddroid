package com.determinato.feeddroid.util;

import java.util.ArrayList;

/**
 * Utility functions and static variables.
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class FeedDroidUtils {

	private static boolean mNewUpdates = false;
	private static boolean mUpdating = false;
	
	/**
	 * Returns whether new updates have posted.
	 * @return true if new updates, false otherwise
	 */
	public static boolean hasUpdates() {
		return mNewUpdates;
	}
	
	/**
	 * Sets update flag.
	 * @param flag value to set update flag
	 */
	public static void setNewUpdates(boolean flag) {
		mNewUpdates = flag;
	}
	
	/**
	 * Returns whether an update is in progress.
	 * @return true if updating, false otherwise
	 */
	public static boolean isUpdating() {
		return mUpdating;
	}
	
	/**
	 * Sets flag indicating whether an update is occurring.
	 * @param flag boolean value
	 */
	public static void setUpdating(boolean flag) {
		mUpdating = flag;
	}
	
	/**
	 * Determines if the given MIME type is podcast-compatible
	 * @param mimeType String containing mimeType to test
	 */
	public static boolean isPodcast(String mimeType) {
		boolean podcast = false;
		ArrayList<String> supportedFormats = new ArrayList<String>();
		supportedFormats.add("audio/mpeg");
		supportedFormats.add("audio/x-m4a");
		supportedFormats.add("video/x-m4v");
		supportedFormats.add("video/mp4");
		
		
		if (supportedFormats.contains(mimeType))
			podcast = true;
		return podcast;
	}
	
}
