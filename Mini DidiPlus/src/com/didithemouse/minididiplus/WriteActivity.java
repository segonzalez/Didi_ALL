package com.didithemouse.minididiplus;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.didithemouse.minididiplus.Saver.ActivityEnum;
import com.didithemouse.minididiplus.dragdrop.DropPanelWrapper;
import com.didithemouse.minididiplus.dragdrop.ExtendedImageView;
import com.didithemouse.minididiplus.dragdrop.ViewWrapper;
import com.didithemouse.minididiplus.etapas.EtapaActivity.EtapaEnum;

public class WriteActivity extends Activity {

	private FrameLayout content = null;
	private DropPanelWrapper panel = null;
	private ImageView drawing = null;
	EditText inputText = null;
	Button terminar;
	Button tabInicio, tabDesarrollo, tabFin;
	int storyIndex=0; //0: inicio, 1:desarrollo, 2:fin
	MochilaContents mc = MochilaContents.getInstance();
	TextView instruction=null;
	boolean controlTextChange=true;
	
	//Si ha tocado las tabs, para poder seguir despues de revisar
	boolean touchedA=false, touchedB=false;
		
	final static int objectSize = CreateActivity.objectSize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.write);
		
		content = (FrameLayout) findViewById(R.id.dibujoCanvas);
		drawing = (ImageView) findViewById(R.id.bitmapDraw);
		content.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		panel = MochilaContents.getInstance().getDropPanel();
		
		inputText = (EditText) findViewById(R.id.inputText);
		
		View bg = findViewById(R.id.root);
		bg.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hidekeyboard();return false;
			}
		});	
		
		
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(inputText.getText().toString().equals("") || !flag) return;
				flag = false;
				hidekeyboard();
				
				mc.setTextEdited(storyIndex,inputText.getText().toString() ); 
				if(storyIndex==2)
					setRevisar();
				else {
					controlTextChange=false;
					terminar.setVisibility(View.INVISIBLE);
					setStoryIndex(storyIndex+1);
					controlTextChange=true;
				}
				flag=true;
			}
		});
		terminar.setClickable(true);
		terminar.setVisibility(View.INVISIBLE);
		
		instruction=(TextView) findViewById(R.id.instruction);
		instruction.setClickable(false);
		instruction.setFocusable(false);
		instruction.setFocusableInTouchMode(false);
				
		tabInicio     = (Button) findViewById(R.id.tabinicio);
		tabDesarrollo = (Button) findViewById(R.id.tabdesarrollo);
		tabFin		  = (Button) findViewById(R.id.tabfin);
		
		tabInicio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(0);
			}
		});
		tabDesarrollo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				touchedA=true;
				setStoryIndex(1);
			}
		});
		tabFin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				touchedB=true;
				setStoryIndex(2);
			}
		});
		
		tabInicio.setVisibility(View.INVISIBLE);
		tabDesarrollo.setVisibility(View.INVISIBLE);
		tabFin.setVisibility(View.INVISIBLE);
		
		inputText.setCursorVisible(true);
		
		ShowContent();
		
		storyIndex = 0;
		inputText.setText(mc.getTextEdited(storyIndex));
		setStoryIndex(storyIndex);
		

		inputText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(controlTextChange)
				terminar.setVisibility(View.VISIBLE);;
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
	}
		 
	
	 boolean revisar = false;
	 public void setRevisar(){
		mc.cloneTextsOriginal();
		controlTextChange=false;
		revisar = true;
		terminar.setVisibility(View.INVISIBLE);
		setStoryIndex(0);
		terminar.setBackgroundResource(R.drawable.flecha);
		
		tabInicio.setVisibility(View.VISIBLE);
		tabDesarrollo.setVisibility(View.VISIBLE);
		tabFin.setVisibility(View.VISIBLE);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				if(!touchedA ||!touchedB) return;
				inputText.setClickable(false);
				inputText.setFocusable(false);
				inputText.setFocusableInTouchMode(false);
				
				proceed();
			}
		});
	 }
	 

	 int filterInicio=0xFFFFFFFF,filterDes=0xFFFFFFFF, filterFin=0xFFFFFFFF;
	 public void setStoryIndex(int index)
	 {
		//esconder/mostrar la tab segun corresponda
		tabInicio.setBackgroundResource((index==0)? R.drawable.tabinicio_gray : R.drawable.tabinicio_hidden_gray);
		tabDesarrollo.setBackgroundResource((index==1)? R.drawable.tabdesarrollo_gray : R.drawable.tabdesarrollo_hidden_gray);
		tabFin.setBackgroundResource((index==2)? R.drawable.tabfin_gray : R.drawable.tabfin_hidden_gray);
				 
		tabInicio.getBackground().setColorFilter(filterInicio,PorterDuff.Mode.MULTIPLY );
		tabDesarrollo.getBackground().setColorFilter(filterDes,PorterDuff.Mode.MULTIPLY );
		tabFin.getBackground().setColorFilter(filterFin,PorterDuff.Mode.MULTIPLY );
		 
		inputText.setClickable(true);
		inputText.setFocusable(true);
		inputText.setFocusableInTouchMode(true);
		inputText.setCursorVisible(true);	
		inputText.requestFocusFromTouch();
		
		String ins = "";
		if(index ==0){ins = getResources().getString(R.string.inicio);	}
		else if(index ==1){ins = getResources().getString(R.string.desarrollo);	}
		else if(index ==2){ins = getResources().getString(R.string.cierre);	}
		
		//Dependiendo de que etapa es elegimos la instruccion
		if(revisar)
			instruction.setText(getResources().getString(R.string.writeInstruction2));
		else
			instruction.setText(String.format(getResources().getString(R.string.writeInstruction),ins));
		 
		mc.setTextEdited(storyIndex,inputText.getText().toString() ); 
		inputText.setText(mc.getTextEdited(index));
		storyIndex=index;
		
		if(touchedA && touchedB) terminar.setVisibility(View.VISIBLE);
		 
	 }
	 public void ShowContent() {
	    	content.removeAllViews();
			DropPanelWrapper p1 = panel;
	    	ArrayList<ViewWrapper> wrappers = p1.getWrappers();
			for(ViewWrapper w : wrappers){
				w.destroyView();
				View iv = w.getView(getApplicationContext());
				if(iv==null) continue;
				int left=0, top=0;
				if(iv instanceof ExtendedImageView){
					ExtendedImageView img = (ExtendedImageView)iv;
					left = (int)(w.getX()*CreateActivity.canvasWidth_mid);
					top = (int)(w.getY()*CreateActivity.canvasHeight_mid);
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)(objectSize*(CreateActivity.canvasWidth_mid/810f)), (int)(objectSize*(CreateActivity.canvasWidth_mid/810f)));
					lp.leftMargin = left;
					lp.topMargin = top;
					content.addView(img, lp);
				}
			}
			drawing.setImageDrawable(p1.getPanelView(this).getMediumBitmap());
			content.addView(drawing);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			
	}
	 
	 void hidekeyboard(){
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
	        	hideSoftInputFromWindow(ib.getWindowToken(), 0);
	 }

		
	@Override
	public void onBackPressed() {}

	public void proceed(){
		mc.setTextEdited(storyIndex,inputText.getText().toString() ); 
		EndingActivity.nosVemosPronto = true;
		Saver.savePresentation(ActivityEnum.KARAOKE);
		Intent i = new Intent(getApplicationContext(), EndingActivity.class);
		startActivity(i);
		finish();
	}
}