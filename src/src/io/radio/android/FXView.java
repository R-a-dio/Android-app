package io.radio.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

public class FXView extends View {
	Visualizer visualizer;
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
		visualizer.setCaptureSize(visualizer.getCaptureSizeRange()[1]);
		visualizer.setDataCaptureListener(new onAudioData(),
				Visualizer.getMaxCaptureRate(), false, true);
		visualizer.setEnabled(true);
	}

	public void stopFx() {
		visualizer.setEnabled(false);
		visualizer.release();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.getClipBounds(bounds);

		paint.setColor(Color.argb(128, 255, 255, 255));
		paint.setStrokeWidth(10f);

		if (fftData != null) {
			byte rfk = fftData[0];
			byte ifk = fftData[1];
			float mag = (rfk * rfk + ifk * ifk);
			int db = (int) (10 * Math.log10(mag));
			// System.out.println("Mag: " + mag + " - " + "dB: " + db);
			canvas.drawLine(bounds.centerX(), bounds.bottom, bounds.centerX(),
					bounds.bottom - (db * 10), paint);
		}
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
