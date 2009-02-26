package uk.sean;

import android.app.Activity;
import android.os.Bundle;

public class Tesla extends Activity {
    /* This is the main screen, providing the playback controls. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
    }
}