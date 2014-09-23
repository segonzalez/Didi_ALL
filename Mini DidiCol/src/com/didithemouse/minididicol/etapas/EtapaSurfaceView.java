package com.didithemouse.minididicol.etapas;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class EtapaSurfaceView extends GLSurfaceView {

	public EtapaRenderer mRenderer;
	protected float mPreviousX;
	protected float mPreviousY;
	protected GestureDetector gd;
	protected EtapaActivity activity;
	protected Context context;
	protected Handler handler;

	public EtapaSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gd = new GestureDetector(context, new EtapaGestureListener(this));
		this.context = context;
		// set the mRenderer member
		setupRenderer();
	}

	public void setupRenderer() {
		this.handler = new Handler();
		mRenderer = new EtapaRenderer(this.context, this,
				this.handler);
		setRenderer(mRenderer);

		// Always render the view (scroller).
		// TODO: revisar como hacerlo solo when there is a change
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	public void showObjects() {
		activity.showObjects();
	}
	
	public void showMouseHole()
	{
	    activity.showMouseHole();
	}
	public void hideMouseHole()
	{
	    activity.hideMouseHole();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
	    if (mRenderer.zoomedOut || mRenderer.endStory) {

			return false;
			// activity.finishAndReturn();
		}
		if (gd.onTouchEvent(e))
			return true;
		else
			return false;
	}

	public void setActivity(EtapaActivity etapaActivity) {
		this.activity = etapaActivity;
	}

	public void setHandler(Handler h) {
		this.handler = h;
	}

	public void move(float distanceX, float distanceY) {
		mRenderer.movement_x = -distanceX * 0.007f;
		mRenderer.movement_y = distanceY * 0.007f;
		requestRender();
	}
}
