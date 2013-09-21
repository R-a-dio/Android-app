package io.radio.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Created by aki on 30/06/13.
 */
public class RemoteControlReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			KeyEvent ev = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (ev.getAction() == KeyEvent.ACTION_DOWN)
				switch (ev.getKeyCode()) {
				case KeyEvent.KEYCODE_MEDIA_STOP:
					RadioService.sendCommand(context, ApiUtil.REMOTEMUSICSTOP);
					break;
				case KeyEvent.KEYCODE_MEDIA_PLAY:
					RadioService.sendCommand(context, ApiUtil.REMOTEMUSICPLAY);
					break;
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					RadioService.sendCommand(context, ApiUtil.REMOTEMUSICPLAYPAUSE);
					break;
				}
		}

	}
}
