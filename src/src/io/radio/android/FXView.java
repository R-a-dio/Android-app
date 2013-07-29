package io.radio.android;

import android.content.Context;
import android.graphics.Canvas;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

public class FXView extends View {
	Visualizer visualizer;

	public FXView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FXView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FXView(Context context) {
		super(context);
	}
	
	public void startFx(int playerId) {
		visualizer = new Visualizer(playerId);
		//visualizer.setCaptureSize(size);
		//visualizer.setDataCaptureListener(listener, rate, waveform, fft);
		visualizer.setEnabled(true);
	}

	public void stopFx() {
		visualizer.setEnabled(false);
		visualizer.release();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	private class onAudioData implements Visualizer.OnDataCaptureListener
    {

		public void onFftDataCapture(Visualizer visualizer, byte[] fft,
				int samplingRate) {
			// TODO Auto-generated method stub
			
		}

		public void onWaveFormDataCapture(Visualizer visualizer,
				byte[] waveform, int samplingRate) {
			// TODO Auto-generated method stub
			
		}
		
    }
}
