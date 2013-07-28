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
		System.out.println("LolID: " + playerId);
		visualizer = new Visualizer(playerId);
		System.out.println("SweetWorks");
	}
	
	public void stopFx() {
		visualizer.release();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}
