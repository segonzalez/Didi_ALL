package com.didithemouse.didicol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

public class DrawActivity extends Activity {
	
	private FingerPaint fp;
	MochilaContents mc = MochilaContents.getInstance();
	final static int canvasHeight = CreateActivity.canvasHeight;
	final static int canvasWidth = CreateActivity.canvasWidth;

	EditText etName;
	private RelativeLayout toolbar;
	private ImageButton[] toolbar_button;
	Button terminar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(R.layout.draw);
		
		FrameLayout fl = (FrameLayout) findViewById(R.id.canvas_framelayout);
		fp= new FingerPaint(this.getApplicationContext());
		fp.setBitmap(mc.getDropPanel().getBitmap());
		fl.addView(fp);
		fp.setMinimumWidth(canvasWidth);
		fp.setMinimumHeight(canvasHeight);
		
		
		findViewById(R.id.canvas_layout).setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hideSoftKeyboard();return false;
			}
		});
		
		fp.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hideSoftKeyboard();return false;
			}
		});


		/*Aqui seteamos fingerpaint */
		toolbar = (RelativeLayout) findViewById(R.id.canvas_toolbarlayout);
		
		toolbar_button = new ImageButton[]{(ImageButton)(toolbar.getChildAt(0)),
				(ImageButton)(toolbar.getChildAt(1))};
		
		//LAPIZ
		toolbar_button[0].setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fp.setDraw();	
				changeSelectedToolbar(0);
			}
		});
		
		//Seteamos que hara el click de la GOMA
		toolbar_button[1].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fp.setErase();
				changeSelectedToolbar(1);
			    }
		});
		changeSelectedToolbar(0);
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)proceed();
			}
		});
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				if(etName.getText() == null || etName.getText().toString().equals("")) return;
				flag = false;
				isWaiting = true;
				mc.getNetManager().sendMessage(new NetEvent("draw",true) );
				terminar.setBackgroundResource(R.drawable.flechaespera);
				if(kid1ready && kid2ready)proceed();
			}
		});
		
		etName = (EditText) findViewById(R.id.kidNameText);
		etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateButtons();
				return false;
			}
		});
		updateButtons();
	}

	void changeSelectedToolbar(int index)
	{
		for (int i=0; i < toolbar_button.length; i++) 
			toolbar_button[i].setColorFilter(0);
		toolbar_button[index].setColorFilter(0xC00080FF);
	}
	
	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void proceed(){
		mc.getNetManager().setReadyListener(null);
		mc.setTitle(etName.getText().toString());
		Intent intent = new Intent(this.getApplicationContext(), FinalActivity.class);
		startActivity(intent);
		finish();
	}
	
	@Override
	public void onBackPressed() {}
	
    void hideSoftKeyboard() {
		updateButtons();
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
	        	hideSoftInputFromWindow(ib.getWindowToken(), 0);
	 }
    
    void updateButtons()
    {
    	if(etName.getText() == null || etName.getText().toString().equals("")) {
    		terminar.setVisibility(View.INVISIBLE);
    		terminar.setClickable(false);
    	}
    	else{
    		terminar.setVisibility(View.VISIBLE);
    		terminar.setClickable(true);
    	}
    }
}
