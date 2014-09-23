package com.didithemouse.didiplus.etapas;


import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.didithemouse.didiplus.DescriptionActivity;
import com.didithemouse.didiplus.dragdrop.DragController;
import com.didithemouse.didiplus.dragdrop.DragLayer;
import com.didithemouse.didiplus.dragdrop.DropTarget;
import com.didithemouse.didiplus.dragdrop.ExtendedImageView;
import com.didithemouse.didiplus.dragdrop.Mochila;
import com.didithemouse.didiplus.MochilaContents;
import com.didithemouse.didiplus.dragdrop.MyAbsoluteLayout.LayoutParams;
import com.didithemouse.didiplus.Principal;
import com.didithemouse.didiplus.R;
import com.didithemouse.didiplus.dragdrop.ViewWrapper;

public class EtapaActivity extends Activity implements View.OnTouchListener {

	public enum EtapaEnum {LIBERTY,BAGEL,WEST }
	protected EtapaEnum etapa;
	
	protected EtapaSurfaceView surfaceView;
	protected DragController dragController;
	protected DragLayer dragLayer;
	protected Handler handler;
	protected Button closeButton;
	protected int panelNumber;
	protected Mochila mochila;
	protected ImageView mochilaView;
	protected ExtendedImageView[] arrastrables;
	protected LayoutParams[] posiciones;
	protected final static int maxObjetos = 3;
	protected int numItems = 0;
	protected ImageView badge;
	protected ImageButton volver;
	public static boolean canGoBack;
	protected boolean canDrag=true;
	
	MochilaContents mc = MochilaContents.getInstance();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		badge = new ImageView(this.getApplicationContext());
		setContentView(R.layout.etapa);
		

		inicializarBoton();

		
		ImageView badgeX = (ImageView) findViewById(R.id.badge);
		badgeX.setVisibility(View.GONE);
		mochila = (Mochila) findViewById(R.id.backpack);
		mochilaView = (ImageView) mochila;
		dragController = new DragController(this);
		dragLayer = (DragLayer) findViewById(R.id.drag_layer);

		mochila.setDropRunnable(new Runnable(){
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.espere), Toast.LENGTH_LONG).show();
			
				mochila.setDropRunnable(null);
				canGoBack=false;
				canDrag=false;
				dragController.removeDropTarget(dragLayer);
				dragController.removeDropTarget((DropTarget)mochila);

				PropertyValuesHolder finalA = PropertyValuesHolder.ofFloat("alpha", 0.20f);
				ObjectAnimator disappear_backpack = ObjectAnimator
						.ofPropertyValuesHolder(mochilaView, finalA).setDuration(2000);
				AnimatorSet move = new AnimatorSet();
				move.play(disappear_backpack);
				move.start();

				move.addListener(new AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {}
					@Override
					public void onAnimationRepeat(Animator animation) {	}
					@Override
					public void onAnimationEnd(Animator animation) {
						postTutorial();}
					@Override
					public void onAnimationCancel(Animator animation) {}
				});


			}});

		ImageView backpack = (ImageView) findViewById(R.id.backpack_intro);
		backpack.setVisibility(View.GONE);

		for(EtapaEnum etapax: EtapaEnum.values())
		{
			if(mc.isVisited(etapax.ordinal()%3)) continue;
			int badgeDrawable = (EtapaConstants.SMALL_BADGE_DRAWABLE[etapax.ordinal()%3]);
			ExtendedImageView badge_eim = new ExtendedImageView(getApplicationContext(),badgeDrawable,1, etapax);
			badge_eim.setImageResource(badgeDrawable);
			badge_eim.setContentDescription("no");
			badge_eim.setVisibility(ImageView.VISIBLE);
			dragLayer.addView(badge_eim);
			badge_eim.setOnTouchListener(this);
			dragLayer.updateViewLayout(badge_eim, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 390+etapax.ordinal()*200, 310));
		}
		dragLayer.setDragController(dragController);
		dragController.addDropTarget(dragLayer);


		mochilaView.setContentDescription("no");
		dragController.addDropTarget((DropTarget) findViewById(R.id.backpack));

		DragLayer.LayoutParams lp1 = new LayoutParams(179, 205, 0, 540);
		dragLayer.updateViewLayout(mochilaView, lp1);
		canDrag=true;

	}

	protected void postTutorial() {

		ArrayList<ViewWrapper> items = MochilaContents.getInstance().getItems();
		etapa = items.get(items.size()-1).getEtapa();
		MochilaContents.getInstance().getItems().remove(items.size()-1);
		dragLayer.removeAllViews();
		dragLayer.addView(mochilaView);	
		
		surfaceView = new EtapaSurfaceView(this, etapa);
		surfaceView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.FILL_PARENT));
		surfaceView.setActivity(this);
		surfaceView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		surfaceView.setVisibility(View.VISIBLE);
		//encima del texview que dice espere por favor
		((FrameLayout)findViewById(R.id.overlay)).addView(surfaceView, 0);
		

		mochila = (Mochila) findViewById(R.id.backpack);

		mochila.setDropRunnable(new Runnable(){
			@Override
			public void run() {
				checkObjects(); }});


		/*** SETUP DE LAS VISTAS ***/
		setupViews();
		mochilaView.setAlpha(1f);
		mochilaView.setVisibility(View.VISIBLE);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Pausamos el thread de rendering.
		// TODO: de-allocate objects para liberar memoria
		if(surfaceView != null)
		surfaceView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Resumimos el thread suspendido
		// TODO: si se hizo la de-allocation, re-allocar los objetos
		if(surfaceView != null)
		surfaceView.onResume();
	}

	public void showObjects() {
		//Animation appear = AnimationUtils.loadAnimation(this,
		//		R.animator.appear_long);
		canDrag=true;
		dragLayer.setVisibility(View.VISIBLE);
		//dragLayer.startAnimation(appear);
		if(MochilaContents.SKIP_OBJECTS)
		{
			volver.setClickable(true);
			volver.setVisibility(View.VISIBLE);
		}
		else
		{
			volver.setVisibility(View.INVISIBLE);
		}
	}

	boolean hole_shown = false;

	public void showMouseHole() {
		if (!hole_shown) {
			hole_shown = true;
			View mousehole = findViewById(R.id.mousehole);
			mousehole.setVisibility(View.VISIBLE);
			mousehole.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					View mousehole = findViewById(R.id.mousehole);
					mousehole.setVisibility(View.GONE);
					mousehole.setClickable(false);
					mousehole.setAlpha(0);
					surfaceView.mRenderer.collectItems = true;
					setOverlay();
				}
			});
		}
	}

	public void hideMouseHole() {
		if(hole_shown) {
			hole_shown = false;
			View mousehole = findViewById(R.id.mousehole);
			mousehole.setVisibility(View.INVISIBLE);
			mousehole.setClickable(false);
		}
	}
	
	public Handler getHandler() {
		return this.handler;
	}

	int drawables[];
	protected void setupViews() {
		DragController dragController = this.dragController;
		dragLayer = (DragLayer) findViewById(R.id.drag_layer);
		dragLayer.setVisibility(View.INVISIBLE);
		dragLayer.setDragController(dragController);
		dragController.addDropTarget(dragLayer);

		mochilaView = (ImageView) findViewById(R.id.backpack);
		arrastrables = new ExtendedImageView[maxObjetos];
		posiciones = new LayoutParams[maxObjetos];

		setObjects();

		for (int i = 0; i < drawables.length && i < maxObjetos; i++) {
			if (posiciones[i] != null) {
				arrastrables[i] = new ExtendedImageView(this.getApplicationContext(),drawables[i],1,etapa);
				arrastrables[i].setImageResource(drawables[i]);
				arrastrables[i].setVisibility(ImageView.VISIBLE);
				dragLayer.addView(arrastrables[i]);
				arrastrables[i].setContentDescription("no");
				arrastrables[i].setOnTouchListener(this);
				
				dragLayer.updateViewLayout(arrastrables[i], posiciones[i]);
			}
		}

		dragLayer.removeView(mochilaView);
		dragLayer.addView(mochilaView);
		mochilaView.setContentDescription("no");
		dragController.addDropTarget((DropTarget) findViewById(R.id.backpack));

		DragLayer.LayoutParams lp1 = new LayoutParams(179, 205, 0, 540);
		dragLayer.updateViewLayout(mochilaView, lp1);
		volver.setClickable(false);

	}
	
	public boolean startDrag(View v) {
		Object dragInfo = v;
		dragController.startDrag(v, dragLayer, dragInfo,
				DragController.DRAG_ACTION_MOVE);
		return true;
	}

	public void toast(String msg) {
	}

	@Override
	public boolean onTouch(View v, MotionEvent m) {
		//EVITAR Multitouch
    	if(m.getPointerCount() > 1) return true;
    	if(!canDrag) return true;
    	
		if (m.getAction() == MotionEvent.ACTION_MOVE
				|| m.getAction() == MotionEvent.ACTION_DOWN)
			return startDrag(v);
		return false;
	}
	
	protected void checkObjects()
	{
 		numItems++;
		int counter = 0;
		// -1 porque son solo 2
		for (int i = 0; i < maxObjetos-1; i++)
		{if (arrastrables[i] == null) counter++;}
		if (numItems >= maxObjetos - counter-1)
		{
			volver.setVisibility(View.VISIBLE);
			volver.setColorFilter(0);
			volver.setClickable(true);
			mochila.setDropRunnable(null);
		}
	}

	void setObjects() {
		//PONER EN EL ORDEN ANTERIOR !!
		drawables = EtapaConstants.STAGE_OBJECTS[etapa.ordinal()%4];
        
		int[] posArray = EtapaConstants.STAGE_OBJECTS_COORDS[etapa.ordinal()%4];
		for(int i=0; i< drawables.length; i++)
				posiciones[i] = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 
						posArray[2*i], posArray[2*i+1]);
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(surfaceView != null){
			this.surfaceView.setVisibility(View.GONE);
			this.surfaceView.destroyDrawingCache();
			this.surfaceView.mRenderer.finish();
			this.surfaceView = null;
		}
		
		dragController = null;
		dragLayer = null;
		handler = null;
		closeButton = null;
		mochila = null;
		mochilaView = null;
		arrastrables = null;
		posiciones = null;
		badge = null;
		volver = null;
		System.gc();
	}

	protected void inicializarBoton() {
		volver = (ImageButton) findViewById(R.id.volver);
		volver.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				proceed();
			}
		});
	}
	
	protected void setOverlay()
	{
		//if(etapa.equals(EtapaEnum.EMPIRE)){
		//	FrameLayout overlay = (FrameLayout) findViewById(R.id.overlay);
		//	overlay.setForeground(getResources().getDrawable(R.drawable.empirestate_overlay));
		//}
	}
	@Override
	public void onBackPressed() {
		if(!canGoBack) return;
		Intent intent = new Intent(this.getApplicationContext(), Principal.class);
		startActivity(intent);
		finish();
	}
	
	public void proceed(){
		MochilaContents.getInstance().cleanPanels();
		Intent intent = new Intent(this.getApplicationContext(), DescriptionActivity.class);
		mc.setVisited(etapa.ordinal()%3, true);
		mc.addEtapa(etapa);
		startActivity(intent);
		finish();
	}

}
