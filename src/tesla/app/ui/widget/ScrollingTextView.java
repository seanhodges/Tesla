/* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tesla.app.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.TextView;

public class ScrollingTextView extends TextView {
	
	/*
	 * Widget adapted from xiongzh's ScrollTextView:
	 * 
	 * http://bear-polka.blogspot.com/2009/01/scrolltextview-scrolling-textview-for.html
	 */
	
	private final static int SCROLL_DURATION = 40; // milliseconds before each scroll iteration
	private final static int START_POSITION = 0; // the initial X offset
	private static final int DELAY_NEXT_SCROLL = 2000; // Delay until the scroll operation repeats
	
	private Scroller scroller = null;
	
	private Handler repeatScrollingHandler = new Handler();
	private Runnable repeatScrollingRunnable = new Runnable() {
		public void run() {
			// This is a little bit naughty, but the Scroller is problematic in a Runnable
			// It piggy-backs onto the onTextChanged() callback
			setText(getText() + "");
		}
	};
	private int distance;
	private int totalDuration;
	
	public ScrollingTextView(Context context) {
		this(context, null);
	}

	public ScrollingTextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}
	
	public ScrollingTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setEllipsize(null);
		setSingleLine();
	}

	protected void onTextChanged(CharSequence text, int start, int before, int after) {
		super.onTextChanged(text, start, before, after);
		init();
	}
	
	private void init() {
		if (getWidth() > 0 && isTextOverset()) {
			setHorizontallyScrolling(true);
			distance = calculateDistance();
			totalDuration = SCROLL_DURATION * distance;
			scroller = new Scroller(this.getContext(), new LinearInterpolator());
			setScroller(scroller);
			doScroll();
		}
		else {
			setHorizontallyScrolling(false);
			setScroller(null);
		}
	}
	
	private boolean isTextOverset() {
		TextPaint tp = getPaint();
		Rect textArea = new Rect();
		String strTxt = getText().toString();
		tp.getTextBounds(strTxt, 0, strTxt.length(), textArea);
		return (textArea.width() > getWidth());
	}

	private void doScroll() {
		scroller.startScroll(START_POSITION, 0, distance, 0, totalDuration);
		repeatScrollingHandler.removeCallbacks(repeatScrollingRunnable);
		repeatScrollingHandler.postDelayed(repeatScrollingRunnable, totalDuration + DELAY_NEXT_SCROLL);
	}

	private int calculateDistance() {
		TextPaint tp = getPaint();
		Rect textArea = new Rect();
		String strTxt = getText().toString();
		tp.getTextBounds(strTxt, 0, strTxt.length(), textArea);
		return textArea.width() - getWidth();
	}
}
