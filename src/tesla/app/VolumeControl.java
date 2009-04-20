package tesla.app;

import tesla.app.command.Command;
import tesla.app.command.CommandFactory;
import tesla.app.connect.ConnectionException;
import tesla.app.connect.ConnectionOptions;
import tesla.app.connect.FakeConnection;
import tesla.app.connect.IConnection;
import tesla.app.connect.SSHConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SeekBar;

public class VolumeControl extends Activity implements OnSeekBarChangeListener {
	
	private IConnection connection;
	private SeekBar volumeSlider;
	
    /* This is the volume control. */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.volume_control);
        
        volumeSlider = (SeekBar)this.findViewById(R.id.volume);
        volumeSlider.setOnSeekBarChangeListener(this);
        
        // TODO: Bind to service
    }

	public void onProgressChanged(SeekBar seekBar, int progress,
		boolean fromTouch) {
		int level = ((SeekBar) volumeSlider).getProgress();
		Command command = CommandFactory.instance().getCommand(Command.VOL_CHANGE);
		command.addArg(new Float((float)level / 100));
		
		// TODO: Send command to service
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}
}