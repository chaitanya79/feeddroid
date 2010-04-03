package com.determinato.feeddroid.util;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class GestureFilter extends SimpleOnGestureListener {
	public static final int SWIPE_UP = 1;
	public static final int SWIPE_DOWN = 2;
	public static final int SWIPE_LEFT = 3;
	public static final int SWIPE_RIGHT = 4;
	
	public static final int MODE_TRANSPARENT = 0;
	public static final int MODE_SOLID = 1;
	public static final int MODE_DYNAMIC = 2;
	
	private static final int ACTION_FAKE = -13;
	private int swipe_min_distance = 100;
	private int swipe_max_distance = 350;
	private int swipe_min_velocity = 100;
	
	private int mode = MODE_DYNAMIC;
	private boolean running = true;
	private boolean tapIndicator = false;
	
	private Activity context;
	private GestureDetector detector;
	private SimpleGestureListener listener;
	
	public GestureFilter(Activity activity, SimpleGestureListener sgl) {
		context = activity;
		detector = new GestureDetector(context, this);
		listener = sgl;
	}
	
	public void onTouchEvent(MotionEvent event) {
		if (this.running)
			return;
		
		boolean result = detector.onTouchEvent(event);
		
		if (mode == MODE_SOLID)
			event.setAction(MotionEvent.ACTION_CANCEL);
		else if (mode == MODE_DYNAMIC) {
			if (event.getAction() == ACTION_FAKE)
				event.setAction(MotionEvent.ACTION_UP);
			else if (result)
				event.setAction(MotionEvent.ACTION_CANCEL);
			else if(tapIndicator) {
				event.setAction(MotionEvent.ACTION_DOWN);
				tapIndicator = false;
			}
		}
	}
	
	public void setMode(int m) {
		mode = m;
	}
	
	public int getMode() {
		return mode;
	}
	
	public void setEnabled(boolean status) {
		running = status;
	}
	
	public void setSwipeMaxDistance(int distance) {
		swipe_max_distance = distance;
	}
	
	public void setSwipeMinDistance(int distance) {
		swipe_min_distance = distance;
	}
	
	public void setSwipeMinVelocity(int distance) {
		swipe_min_velocity = distance;
	}
	
	public int getSwipeMaxDistance() {
		return swipe_max_distance;
	}
	
	public int getSwipeMinDistance() {
		return swipe_min_distance;
	}
	
	public int getSwipeMinVelocity() {
		return swipe_min_velocity;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, 
			float velocityX, float velocityY) {
		final float xDistance = Math.abs(e1.getX() - e2.getX());
		final float yDistance = Math.abs(e1.getY() - e2.getY());
		
		if (xDistance > swipe_max_distance || yDistance > swipe_max_distance)
			return false;
		
		velocityX = Math.abs(velocityX);
		velocityY = Math.abs(velocityY);
		boolean result = false;
		
		if (velocityX > swipe_min_velocity && xDistance > swipe_min_distance) {
			if (e1.getX() > e2.getX())
				listener.onSwipe(SWIPE_LEFT);
			else
				listener.onSwipe(SWIPE_RIGHT);
			
			result = true;
		}
		
		return result;
	}
	
	
	public boolean onSingleTap(MotionEvent e) {
		tapIndicator = true;
		return false;
	}
	
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		listener.onDoubleTap();
		return true;
	}
	
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return true;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (mode == MODE_DYNAMIC) {
			e.setAction(ACTION_FAKE);
			context.dispatchTouchEvent(e);
		}
		return false;
	}
	
	public static interface SimpleGestureListener {
		void onSwipe(int direction);
		void onDoubleTap();
	}
}
