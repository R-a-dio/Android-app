package io.radio.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

public class FXView extends View {
	Visualizer visualizer;
	Equalizer equalizer;
	static Rect bounds = new Rect();
	byte[] fftData;

	static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
		visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
		visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		visualizer.setDataCaptureListener(new onAudioData(),
				Visualizer.getMaxCaptureRate(), false, true);
		visualizer.setEnabled(true);
		
		equalizer = new Equalizer(0, playerId);
		equalizer.setEnabled(true);
		
	}

	public void stopFx() {
		visualizer.setEnabled(false);
		visualizer.release();

		equalizer.setEnabled(false);
		equalizer.release();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.getClipBounds(bounds);

		paint.setColor(Color.argb(128, 255, 255, 255));
		paint.setStrokeWidth(2f);

		if (fftData != null) {
			for (int i = 0; i < fftData.length / 2; i++) {
				canvas.drawLine(
						bounds.left + i,
						bounds.bottom,
						bounds.left + i,
						bounds.bottom - (getdB(fftData[2*i], fftData[2*i + 1]) * 8),
						paint);
			}
		}
	}

	private int getdB(byte real, byte imag) {
		float mag = (real * real + imag * imag);
		int db = (int) (10 * Math.log10(mag));
		if (mag <= 0)
			db = 0;
		return db;
	}

	private class onAudioData implements Visualizer.OnDataCaptureListener {
		public void onFftDataCapture(Visualizer visualizer, byte[] fft,
				int samplingRate) {
			fftData = fft;
			invalidate();
		}

		public void onWaveFormDataCapture(Visualizer visualizer,
				byte[] waveform, int samplingRate) {
			// TODO Auto-generated method stub

		}

	}
}
