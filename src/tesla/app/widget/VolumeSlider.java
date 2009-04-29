package tesla.app.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tesla.app.R;
import tesla.app.service.CommandService;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class VolumeSlider extends View {
	
	public static final int MAX_LEVEL = 7;
	private static final int EXTRA_MUTE_HEIGHT = 50; // This is a fudge because the mute button is slightly taller than the levels

	private byte currentLevel = 0;
	private OnVolumeLevelChangeListener listener;

	private boolean levelsCalculated = false;
	private List<LevelDrawable> levelDrawable = null;
	
	public interface OnVolumeLevelChangeListener {
		public void onLevelChanged(VolumeSlider volumeSlider, byte level);
	}
	
	private class LevelDrawable {
		public float pos;
		public byte percent;
		public int resource;
	}

	// Need all of these
	public VolumeSlider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setUp(context);
	}
	
	public VolumeSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		setUp(context);
	}
	
	public VolumeSlider(Context context) {
		super(context);
		setUp(context);
	}

	private void setUp(Context context) {
		if (context == null) context = getContext();
		if (levelDrawable == null) levelDrawable = new ArrayList<LevelDrawable>();
	}
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Calculate the level positions after widget has been drawn
		if (!levelsCalculated) {
			calculateLevels(null);
			levelsCalculated = true;
			// Set default image
			for (LevelDrawable item : levelDrawable) {
				if (item.percent >= currentLevel) {
					changeImage(item.resource);
					break;
				}
			}
		}
	}
	
	private void calculateLevels(Context context) {
		if (context == null) context = getContext();
		byte itemPerc = 100 / (MAX_LEVEL - 1);
		float itemHeight = (getHeight() - EXTRA_MUTE_HEIGHT) / MAX_LEVEL;
		float currentHeight = getHeight();
		for (int i = 0; i < MAX_LEVEL; i++) {
			LevelDrawable item = new LevelDrawable();
			currentHeight -= itemHeight;
			item.percent = (byte)(itemPerc * i);
			item.pos = currentHeight;
			switch (i) {
				case 0:
					// Adjust for extra size of mute button
					currentHeight -= EXTRA_MUTE_HEIGHT;
					item.pos = currentHeight;
					item.resource = R.drawable.vol0; 
					break;
				case 1: item.resource = R.drawable.vol1; break;
				case 2: item.resource = R.drawable.vol2; break;
				case 3: item.resource = R.drawable.vol3; break;
				case 4: item.resource = R.drawable.vol4; break;
				case 5: item.resource = R.drawable.vol5; break;
				case 6:
					// Ensure the last item is 100%
					item.percent = 100;
					item.resource = R.drawable.vol6; 
					break;
			}
			levelDrawable.add(item);
		}
	}
	
	private void changeImage(int newImage) {
		setBackgroundResource(newImage);
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		// TODO: This event is being called twice
		boolean handled = false;
		if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
			for (LevelDrawable item : levelDrawable) {
				if (event.getY() > item.pos) {
					currentLevel = item.percent;
					changeImage(item.resource);
					break;
				}
			}
			listener.onLevelChanged(this, currentLevel);
			handled = true;
		}
		return handled;
	}

	public byte getLevel() {
		return currentLevel;
	}

	public void setLevel(byte currentLevel) {
		this.currentLevel = currentLevel;
	}
	
	public void setLevel(int currentLevel) {
		this.currentLevel = (byte)currentLevel;
	}
	
	public void setOnVolumeLevelChangeListener(OnVolumeLevelChangeListener listener) {
		this.listener = listener;
	}
}
