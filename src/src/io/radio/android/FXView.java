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
	byte[] fftData;
	byte[] audioData;

	static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	static Rect bounds = new Rect();

	public FXView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FXView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FXView(Context context) {
		super(context);
	}

	public void startFx(int playerId, boolean dbGraph, boolean waveVis) {
		visualizer = new Visualizer(playerId);
		visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1] / 2);
		visualizer.setDataCaptureListener(new onAudioData(),
				Visualizer.getMaxCaptureRate(), waveVis, dbGraph);
		visualizer.setEnabled(true);

		equalizer = new Equalizer(0, playerId);
		equalizer.setEnabled(true);

	}

	public void stopFx() {
		if (visualizer != null) {
			visualizer.setEnabled(false);
			visualizer.release();
		}
		if (equalizer != null) {
			equalizer.setEnabled(false);
			equalizer.release();
		}

		fftData = null;
		audioData = null;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.getClipBounds(bounds);

		if (fftData != null) {
			paint.setColor(Color.argb(175, 255, 255, 255));
			paint.setStrokeWidth(1f);
			for (int i = 0; i < fftData.length - 1; i++) {
				canvas.drawLine(
						bounds.left + i,
						bounds.bottom,
						bounds.left + i,
						bounds.bottom - (getdB(fftData[i], fftData[i + 1]) * 2),
						paint);
			}

		}

		if (audioData != null) {
			paint.setColor(Color.argb(128, 255, 255, 255));
			paint.setStrokeWidth(3f);
			for (int i = 0; i < audioData.length - 1; i++) {
				canvas.drawLine(bounds.width() * i / (audioData.length - 1),
						bounds.height() / 2 + ((byte) (audioData[i] + 128))
								* (bounds.height() / 3) / 128, bounds.width()
								* (i + 1) / (audioData.length - 1),
						bounds.height() / 2 + ((byte) (audioData[i + 1] + 128))
								* (bounds.height() / 3) / 128, paint);
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
			audioData = waveform;
			invalidate();
		}

	}
}
