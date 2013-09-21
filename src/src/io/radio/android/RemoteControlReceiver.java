package io.radio.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by aki on 30/06/13.
 */
public class RemoteControlReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			/* handle media button intent here by reading contents */
			/* of EXTRA_KEY_EVENT to know which key was pressed */
		}
	}
}
