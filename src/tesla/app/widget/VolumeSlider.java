package tesla.app.widget;

import tesla.app.R;
import tesla.app.VolumeControl;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

public class VolumeSlider extends View {
	
	private Bitmap image;

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
		setBackgroundResource(R.drawable.vol0);
	}

	public int getLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setMax(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setLevel(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setOnSeekBarChangeListener(VolumeControl volumeControl) {
		// TODO Auto-generated method stub
		
	}
}
