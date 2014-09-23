package com.didithemouse.didicol;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.didithemouse.didicol.Saver.ActivityEnum;

public class FinalActivity extends Activity{
	private FrameLayout content = null;
	private ImageView drawing = null;
	TextView inputText = null;
	Button terminar;
	Button tabInicio, tabDesarrollo, tabFin, tabEditado,tabOriginal;
	int storyIndex=0; //0: inicio, 1:desarrollo, 2:fin
	int substoryIndex=0; //indexa páginas de inicio, desarrollo o fin
	MochilaContents mc = MochilaContents.getInstance();
	boolean mostrarOriginal=false;
	
	final static int objectSize = CreateActivity.objectSize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.presentation);
		
		mc.getNetManager().setTextListener(null);
		
		content = (FrameLayout) findViewById(R.id.dibujoCanvas);
		drawing = (ImageView) findViewById(R.id.bitmapDraw);
		content.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		//panel = MochilaContents.getInstance().getDropPanel();
		
		inputText = (TextView) findViewById(R.id.inputText);
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
		
		tabInicio     = (Button) findViewById(R.id.tabinicio);
		tabDesarrollo = (Button) findViewById(R.id.tabdesarrollo);
		tabFin		  = (Button) findViewById(R.id.tabfin);
		
		
		terminar = (Button) findViewById(R.id.terminar);	
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				
				proceed();	
				
			}
		});
		setStoryIndex(0);
		terminar.setClickable(true);	
		
		content.setBackgroundResource(0);
		
		((TextView)findViewById(R.id.instruction)).setText("¡Buen trabajo! Acá está la historia que crearon.");
		
		
		
		mostrarOriginal=false;
		tabEditado  = (Button) findViewById(R.id.tabeditado);
		tabOriginal = (Button) findViewById(R.id.taboriginal);
		tabInicio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(0);
			}
		});
		tabDesarrollo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(1);
			}
		});
		tabFin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setStoryIndex(2);
			}
		});
		tabEditado.setVisibility(View.INVISIBLE);
		tabOriginal.setVisibility(View.INVISIBLE);		
		drawing.setImageDrawable(mc.getDropPanel().getPanelView(this).getMediumBitmap());
		((TextView)findViewById(R.id.tituloNino)).setText("     \""+mc.getTitle()+"\"");
		((TextView)findViewById(R.id.tituloNino)).setTextSize(30);
	}

	 
	 public void setStoryIndex(int index)
	 {
		 //esconder/mostrar la tab segun corresponda
		 tabInicio.setBackgroundResource((index==0)? R.drawable.tabinicio : R.drawable.tabinicio_hidden);
		 tabDesarrollo.setBackgroundResource((index==1)? R.drawable.tabdesarrollo : R.drawable.tabdesarrollo_hidden);
		 tabFin.setBackgroundResource((index==2)? R.drawable.tabfin : R.drawable.tabfin_hidden);
		 
		 inputText.setText(mc.getTextCorrected(index));
		 	 
		 storyIndex=index;
		 substoryIndex=0;
	 }
		
	public void proceed(){
		mc.getNetManager().setReadyListener(null);
		Saver.savePresentation(ActivityEnum.END);
		EndingActivity.nosVemosPronto = false;
		Intent i = new Intent(getApplicationContext(), EndingActivity.class);
		
		startActivity(i);
		finish();
	}
	
	@Override
	public void onBackPressed() {
	}
}