package com.didithemouse.minididicol;


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

import com.didithemouse.minididicol.Saver.ActivityEnum;
import com.didithemouse.minididicol.dragdrop.DropPanelWrapper;
import com.didithemouse.minididicol.dragdrop.ViewWrapper;
import com.didithemouse.minididicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.minididicol.network.NetEvent;
import com.didithemouse.minididicol.network.NetManager.NetEventListener;

public class KaraokeActivity extends Activity{
	private FrameLayout content = null;
	private DropPanelWrapper panel;
	private ImageView drawing = null;
	TextView inputText = null;
	Button terminar;
	Button tabInicio, tabDesarrollo, tabFin, tabEditado,tabOriginal;
	int storyIndex=0; //0: inicio, 1:desarrollo, 2:fin
	int fixedStoryIndex0, fixedStoryIndex1, fixedStoryIndex2=0;
	MochilaContents mc = MochilaContents.getInstance();
	
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
		
		panel = MochilaContents.getInstance().getDropPanel();
		
		inputText = (TextView) findViewById(R.id.inputText);
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
		
		tabInicio     = (Button) findViewById(R.id.tabinicio);
		tabDesarrollo = (Button) findViewById(R.id.tabdesarrollo);
		tabFin		  = (Button) findViewById(R.id.tabfin);
		
		for(ViewWrapper w :panel.getWrappers())
		{
			if(w.getEtapa() != mc.getEtapa(mc.LECTURA)) continue;
			if(w.getX() <0.33) fixedStoryIndex0 = 0;
			else if(w.getX()>0.33 && w.getX()<0.66) fixedStoryIndex0 = 1;
			else if(w.getX()>0.66) fixedStoryIndex0 = 2;
			break;
		}
		for(ViewWrapper w :panel.getWrappers())
		{
			if(w.getEtapa() != mc.getNetManager().getKidEtapa(0,mc.LECTURA)) continue;
			if(w.getX() <0.33) fixedStoryIndex1 = 0;
			else if(w.getX()>0.33 && w.getX()<0.66) fixedStoryIndex1 = 1;
			else if(w.getX()>0.66) fixedStoryIndex1 = 2;
			break;
		}
		for(ViewWrapper w :panel.getWrappers())
		{
			if(w.getEtapa() != mc.getNetManager().getKidEtapa(1,mc.LECTURA)) continue;
			if(w.getX() <0.33) fixedStoryIndex2 = 0;
			else if(w.getX()>0.33 && w.getX()<0.66) fixedStoryIndex2 = 1;
			else if(w.getX()>0.66) fixedStoryIndex2 = 2;
			break;
		}
		
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				changetab();
			}
		});
		
		terminar = (Button) findViewById(R.id.terminar);	
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				
				changetab();
				mc.getNetManager().sendMessage(new NetEvent("pres",true));
				
			}
		});
		setStoryIndex(0);
		if(fixedStoryIndex0 == storyIndex){
			terminar.setClickable(true);
		}
		else{
			terminar.setBackgroundResource(R.drawable.flechaespera);
			terminar.setClickable(false);
		}		
		
		content.setBackgroundResource(0);
		setInstruction();
		
		
		
		tabEditado  = (Button) findViewById(R.id.tabeditado);
		tabOriginal = (Button) findViewById(R.id.taboriginal);
		tabEditado.setVisibility(View.INVISIBLE);
		tabOriginal.setVisibility(View.INVISIBLE);			
	}
	

	 
	 public void setStoryIndex(int index)
	 {
		 //esconder/mostrar la tab segun corresponda
		 tabInicio.setBackgroundResource((index==0)? R.drawable.tabinicio : R.drawable.tabinicio_hidden);
		 tabDesarrollo.setBackgroundResource((index==1)? R.drawable.tabdesarrollo : R.drawable.tabdesarrollo_hidden);
		 tabFin.setBackgroundResource((index==2)? R.drawable.tabfin : R.drawable.tabfin_hidden);
		 
		 inputText.setText(mc.getTextEdited(index));
		 
		 if(index == 0) {drawing.setImageResource(R.drawable.fondoinicio);}
		 else if (index ==1) {drawing.setImageResource(R.drawable.fondochina);}
		 else if (index ==2) {drawing.setImageResource(R.drawable.fondoconey);}
		 
		 storyIndex=index;
	 }
		
	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void changetab(){
		storyIndex = storyIndex+1;
		if(storyIndex == 2){
			mc.getNetManager().setReadyListener(new NetEventListener() {
				@Override
				public void run(NetEvent ne, int fromClient) {
					proceed();
				}
			});
			terminar.setOnClickListener(new View.OnClickListener() {
				boolean flag = true;
				public void onClick(View v) {
					if(!flag) return;
					flag = false;
					
					mc.getNetManager().sendMessage(new NetEvent("write",true) );
					proceed();	
				}
			});
		}
		if(fixedStoryIndex0 == storyIndex){
			terminar.setClickable(true);
			terminar.setBackgroundResource(R.drawable.flecha);
		}
		else{
			terminar.setBackgroundResource(R.drawable.flechaespera);
			terminar.setClickable(false);
		}
		setStoryIndex(storyIndex);
		setInstruction();
		
	}
	
	public void setInstruction(){
		String name = "";
		EtapaEnum etapaFilter = EtapaEnum.WEST;
		int filter = 0xFFFFFFFF;
		if(fixedStoryIndex0==storyIndex){
			name=mc.getKidName();
			etapaFilter = mc.getEtapa(mc.OBJETOS);
		}
		else if(fixedStoryIndex1 == storyIndex)
		{	
			name = mc.getNetManager().getKidName(0);
			etapaFilter = mc.getNetManager().getKidEtapa(0, mc.OBJETOS);
		}
		else if(fixedStoryIndex2 == storyIndex)
		{	
			name = mc.getNetManager().getKidName(1);	
			etapaFilter = mc.getNetManager().getKidEtapa(1, mc.OBJETOS);
		}
		((TextView)findViewById(R.id.instruction)).setText(String.format(getResources().getString(R.string.karaokeInstruction),name));
		if(etapaFilter == EtapaEnum.WEST) filter = ( 0xFF008000 );
		else if(etapaFilter == EtapaEnum.LIBERTY) filter = (0xFFFF4838);
		else if(etapaFilter == EtapaEnum.BAGEL) filter = (0xFF3848FF);
		inputText.setTextColor(filter);
	}
	
	public void proceed(){
		mc.getNetManager().setReadyListener(null);
		Intent i = new Intent(getApplicationContext(), ArgumentatorActivity.class);
		
		startActivity(i);
		finish();
	}
	
	@Override
	public void onBackPressed() {
	}
}