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

import com.determinato.feeddroid.provider.FeedDroid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChannelHeader extends LinearLayout {
	private static final int paddingTop = 2;
	private static final int paddingBottom = 6;
	
	private ImageView mLogo;
	private ImageView mIcon;
	private TextView mLogoText;
	
	private Rect mRect;
	private Paint mGray;
	private Paint mBlack1;
	private Paint mBlack2;
	
	public ChannelHeader(Context ctx) {
		super(ctx);
		init(ctx);
	}
	
	
	public ChannelHeader(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init(ctx);
	}
	
	private void init(Context ctx) {
		mRect = new Rect();
		mGray = new Paint();
		mGray.setStyle(Paint.Style.STROKE);
		mGray.setColor(0xff9c9e9c);
		
		mBlack1 = new Paint();
		mBlack1.setStyle(Paint.Style.STROKE);
		mBlack1.setColor(0xbb000000);
		
		mBlack2 = new Paint();
		mBlack2.setStyle(Paint.Style.STROKE);
		mBlack2.setColor(0x33000000);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		Rect r = mRect;
		
		getDrawingRect(r);
		canvas.drawLine(r.left, r.bottom - 4, r.right, r.bottom - 4, mGray);
		canvas.drawLine(r.left, r.bottom - 3, r.right, r.bottom - 3, mGray);
		canvas.drawLine(r.left, r.bottom - 2, r.right, r.bottom - 2, mBlack1);
		canvas.drawLine(r.left, r.bottom - 1, r.right, r.bottom - 1, mBlack2);
		
		super.dispatchDraw(canvas);
	}
	
	public void setLogo(String channelName, String iconData, String logoData) {
		if (mIcon == null) {
			mIcon = new ImageView(getContext());
			mIcon.setPadding(10, paddingTop, 0, paddingBottom);
			
			LayoutParams iconRules = new LayoutParams(16 + 10, 16 + paddingTop + paddingBottom);
			iconRules.gravity = Gravity.CENTER_VERTICAL;
			addView(mIcon, iconRules);
		}
		
		if (mLogoText == null) {
			mLogoText = new TextView(getContext());
			mLogoText.setPadding(6, paddingTop, 0, paddingBottom);
			mLogoText.setTypeface(Typeface.DEFAULT_BOLD);
			mLogoText.setMaxLines(1);
			addView(mLogoText, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
		
		mLogoText.setText(channelName);
	}
	
	public void setLogo(Cursor c) {
		setLogo(c.getString(c.getColumnIndex(FeedDroid.Channels.TITLE)),
				c.getString(c.getColumnIndex(FeedDroid.Channels.ICON)),
				c.getString(c.getColumnIndex(FeedDroid.Channels.LOGO)));
	}
	
}
