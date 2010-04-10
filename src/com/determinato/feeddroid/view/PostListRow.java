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

package com.determinato.feeddroid.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.determinato.feeddroid.provider.FeedDroid;

public class PostListRow extends ViewGroup {
	private static final String TAG = "PostListRow";
	private static final SimpleDateFormat mDateFormatDb;
	private static final SimpleDateFormat mDateFormatToday;
	private static final SimpleDateFormat mDateFormat;
	
	private static int SUBJECT_ID = 1;
	private static int DATE_ID = 2;
	
	private TextView mSubject;
	private TextView mDate;
	private ImageView mStar;
	
	private Rect mRect;
	private Paint mGray;
	
	static {
		mDateFormatDb = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		//mDateFormatDb = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss Z");
		mDateFormatToday = new SimpleDateFormat("h:mma");
		mDateFormat = new SimpleDateFormat("MM/dd/yyyy h:mma");
	}
	
	public PostListRow(Context ctx) {
		super(ctx);
		
		mRect = new Rect();
		mGray = new Paint();
		mGray.setStyle(Paint.Style.STROKE);
		mGray.setColor(com.determinato.feeddroid.R.color.gray);
		
		mSubject = new TextView(ctx);
		mSubject.setId(SUBJECT_ID);
		
		LayoutParams subjectRules = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addView(mSubject, subjectRules);
		
		mDate = new TextView(ctx);
		mDate.setId(DATE_ID);
		//mDate.setTextColor(com.determinato.feeddroid.R.color.gray);
		
		LayoutParams dateRules = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		addView(mDate, dateRules);
		
		mStar = new ImageView(ctx);
		mStar.setImageResource(android.R.drawable.star_on);
		LayoutParams starRules = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		addView(mStar, starRules);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int subjw = mSubject.getMeasuredWidth();
		int subjh = mSubject.getMeasuredHeight();
		int datew = mDate.getMeasuredWidth();
		int dateh = mDate.getMeasuredHeight();
		int selfw = getMeasuredWidth();
		int selfh = getMeasuredHeight();
		int starh = mStar.getMeasuredHeight();
		int starw = mStar.getMeasuredWidth();
		
		mSubject.layout(0, 0, subjw, subjh);
		mDate.layout((selfw - starw) - datew, selfh - (dateh + 4), selfw, selfh - 4);
		mStar.layout(selfw - starw, selfh - (dateh + 4), selfw, selfh - 4);
		
		
		
	}
	
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		int w = View.MeasureSpec.getSize(widthSpec);
		
		mSubject.measure(widthSpec, heightSpec);
		mDate.measure(getChildMeasureSpec(widthSpec, 0, mDate.getLayoutParams().width),
				getChildMeasureSpec(widthSpec, 0, mDate.getLayoutParams().height));
		mStar.measure(getChildMeasureSpec(widthSpec, 0, mStar.getLayoutParams().width),
				getChildMeasureSpec(widthSpec, 0, mStar.getLayoutParams().height));
		
		int h;
		int lines = mSubject.getLineCount();
		
		if (lines <= 1)
			h = mSubject.getMeasuredHeight() + mDate.getMeasuredHeight();
		else {
			h = mSubject.getMeasuredHeight();
			
			float linew = mSubject.getLayout().getLineRight(lines - 1);
			
			if ((linew + 10) > (w - mDate.getMeasuredWidth()))
				h += mDate.getMeasuredHeight();
			
			if ((linew + 10) > (w - mStar.getMeasuredHeight()))
				h += mStar.getMeasuredHeight();
		}
		
		setMeasuredDimension(w, h+4);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		Rect r = mRect;
		
		getDrawingRect(r);
		canvas.drawLine(r.left, r.bottom - 1, r.right, r.bottom - 1, mGray);
		super.dispatchDraw(canvas);
	}
	
	public void bindView(Cursor cursor) {
		if (cursor.getInt(cursor.getColumnIndex(FeedDroid.Posts.READ)) != 0) {
			mSubject.setTypeface(Typeface.DEFAULT);
			mDate.setTypeface(Typeface.DEFAULT);
		} else {
			mSubject.setTypeface(Typeface.DEFAULT_BOLD);
			mDate.setTypeface(Typeface.DEFAULT_BOLD);
		}
		mSubject.setText(cursor.getString(cursor.getColumnIndex(FeedDroid.Posts.TITLE)));
		String dateStr = cursor.getString(cursor.getColumnIndex(FeedDroid.Posts.DATE));
		int starred = cursor.getInt(cursor.getColumnIndex(FeedDroid.Posts.STARRED));
		
		if (starred == 1)
			mStar.setImageResource(android.R.drawable.star_on);
		else
			mStar.setVisibility(INVISIBLE);
		
		try {
			Date date = mDateFormatDb.parse(dateStr);
			
			Calendar then = new GregorianCalendar();
			then.setTime(date);
			
			Calendar now = new GregorianCalendar();
			
			SimpleDateFormat fmt;
			
			fmt = mDateFormat;
			/*
			if (now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR))
				fmt = mDateFormatToday;
			else
				fmt = mDateFormat;
			*/
			mDate.setText(fmt.format(date));
		} catch (ParseException e) {
			Log.d(TAG, Log.getStackTraceString(e));
		}
		
	}

}
