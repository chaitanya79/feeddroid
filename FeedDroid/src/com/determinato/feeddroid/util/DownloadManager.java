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

package com.determinato.feeddroid.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.Handler;

public class DownloadManager {
	private static final String TAG = "DownloadManager";
	private static final int TIMEOUT = 8000;
	
	private Handler mHandler;
	
	private ConcurrentLinkedQueue<DownloadWrapper> mThreads =
		new ConcurrentLinkedQueue<DownloadWrapper>();
	
	private ConcurrentLinkedQueue<Runnable> mQueue =
		new ConcurrentLinkedQueue<Runnable>();
	
	public DownloadManager(Handler handler) {
		mHandler = handler;
	}
	
	public boolean schedule(Runnable r) {
		mQueue.add(r);
		
		if (mThreads.size() == 0) {
			mHandler.removeCallbacks(mWakeUp);
			mWakeUp.run();
			return true;
		}
		
		return false;
	}
	
	private Runnable mWakeUp = new Runnable() {
		public void run() {
			Runnable r = mQueue.poll();
			
			if (r == null) {
				// Woke up with noting to do.
				return;
			} 
			
			for (DownloadWrapper t: mThreads) {
				t.tooSlow();
			}
			
			DownloadWrapper t = new DownloadWrapper(mThreads, mHandler, r);
			mThreads.add(t);
			t.start();
			
			mHandler.removeCallbacks(this);
			mHandler.postDelayed(this, TIMEOUT);
			
			
		}
	};
	
	private class DownloadWrapper extends Thread {
		Collection<DownloadWrapper> mThreads;
		Handler mHandler;
		Runnable mWrapped;
		
		private boolean mTooSlow = false;
		
		public DownloadWrapper(Collection<DownloadWrapper> active, Handler handler, Runnable wrap) {
			mThreads = active;
			mHandler = handler;
			mWrapped = wrap;
		}
		
		public void tooSlow() {
			mTooSlow = true;
		}
		
		public void run() {
			mWrapped.run();
			
			mThreads.remove(this);
			
			if (!mTooSlow) {
				mHandler.removeCallbacks(mWakeUp);
				mHandler.post(mWakeUp);
			}
		}
	}
}
