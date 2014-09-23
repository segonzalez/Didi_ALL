package com.didithemouse.didicol;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.didicol.network.NetEvent;
import com.didithemouse.didicol.network.NetManager.NetEventListener;

public class CorrectionActivity extends Activity {

	EditText inputText = null;
	TextView argText = null;
	Button terminar;
	MochilaContents mc = MochilaContents.getInstance();
	public int storyIndex=0;
	
	String [][]argTexts = mc.getArgumentatorTexts();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.correction);
		inputText = (EditText) findViewById(R.id.inputText);
		argText = (TextView) findViewById(R.id.argText_);
		argTexts = mc.getArgumentatorTexts();
		
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)changekid();
			}
		});
						
		View bg = findViewById(R.id.root);
		bg.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hidekeyboard();return false;
			}
		});	
		
		inputText.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		inputText.setCursorVisible(true);

		mc.cloneTextsCorrected();
		inputText.setText(mc.getTextCorrected(0));
		inputText.addTextChangedListener(new TextWatcher() {
			boolean flag = false;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(flag|| ignoreNextTextChange || storyIndex != mc.getEtapa(mc.TEXTO).ordinal()%3) return;
				flag=false;
				mc.getNetManager().sendMessage(new NetEvent(before,s.subSequence(start, start+count).toString(),start));
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		if(storyIndex != mc.getEtapa(mc.TEXTO).ordinal()%3){
			hidekeyboard();
			inputText.setFocusable(false);
			inputText.setFocusableInTouchMode(false);		
		 }
				
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				isWaiting=true;
				mc.getNetManager().sendMessage(new NetEvent("argumentator",true) );
				terminar.setBackgroundResource(R.drawable.flechaespera);
				if(kid1ready && kid2ready)changekid();				
			}
		});	
		
		mc.getNetManager().setTextListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				
				if(ne.i2 >=0){
					inputText.getText().insert(ne.i2, ne.message);
					if(ne.message.equals("")){
						if(ne.i2>=ne.i1-1)inputText.getText().delete(ne.i2-ne.i1+1, ne.i2+1);
						else inputText.getText().delete(0, ne.i1);
					}
					
				}
				else{
					mc.setTextCorrected(ne.i1,ne.message);
					setStoryIndex(-ne.i2-1, true);
				}
			}
		});
		
		setInstruction();
		
		String atext = "";
		for(int i=0; i<3; i++){
			atext += argTexts[i][storyIndex%3];
			atext += "\n\n";
		}
		argText.setText(atext);
		
		setStoryIndex(0, true);
		
	}
	
	
	 boolean ignoreNextTextChange = false;
	 
	 int filterInicio=0xFFFFFFFF,filterDes=0xFFFFFFFF, filterFin=0xFFFFFFFF;
	 public void setStoryIndex(int index, boolean isEvent)
	 {
		 mc.setTextCorrected(storyIndex,inputText.getText().toString() );
		 if(index >= 3) {storyIndex++; return;} 
		 
		 inputText.requestFocusFromTouch();
		 
		 ignoreNextTextChange=true;
		 inputText.setText(mc.getTextCorrected(index));
		 ignoreNextTextChange=false;
		 storyIndex=index;
		 
	 }
	

	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void changekid(){
		isWaiting=false;kid1ready=false; kid2ready=false;

		setStoryIndex(storyIndex+1, true);
		if(storyIndex == mc.getEtapa(mc.TEXTO).ordinal()%3){
			inputText.setFocusable(true);
			inputText.setFocusableInTouchMode(true);	
			inputText.requestFocusFromTouch();
			showkeyboard();
		 }
		 else{
			hidekeyboard();
			inputText.setFocusable(false);
			inputText.setFocusableInTouchMode(false);
		 }
		
		if(storyIndex == 3) {proceed();}
		else{
			setInstruction();
			
			String atext = "";
			for(int i=0; i<3; i++){
				atext+= argTexts[i][storyIndex%3]+ "\n\n";
			}
			argText.setText(atext);
			
			setStoryIndex(storyIndex, true);
			terminar.setBackgroundResource(R.drawable.flecha);
			terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
					if(!flag) return;
					flag = false;
				
					isWaiting = true;
					mc.getNetManager().sendMessage(new NetEvent("argumentator",true) );
					terminar.setBackgroundResource(R.drawable.flechaespera);
					if(kid1ready && kid2ready)changekid();				
				}
			});
		}
	}
	
	TextView insTextView;
	public void setInstruction(){
		filterInicio=filterFin=filterDes=0xFFFFFFFF;
		if(insTextView==null ) insTextView=((TextView)findViewById(R.id.instruction));
		String name = "";
		if(mc.getEtapa(mc.TEXTO).ordinal()%3 == storyIndex)
			name=mc.getKidName();
		else if(storyIndex == mc.getNetManager().getKidEtapa(0,mc.TEXTO).ordinal()%3)
			name = mc.getNetManager().getKidName(0);
		else if(storyIndex == mc.getNetManager().getKidEtapa(1,mc.TEXTO).ordinal()%3)
			name = mc.getNetManager().getKidName(1);	
		
		String instype = "";
		if(storyIndex==0)
			instype = getResources().getString(R.string.inicio);
		else if(storyIndex==1)
			instype = getResources().getString(R.string.desarrollo);
		else if(storyIndex==2)
			instype = getResources().getString(R.string.cierre);
		insTextView.setText(String.format(getResources().getString(R.string.corrInstruction),name,instype));
		
		int filter = 0xFFFFFFFF;
		if(mc.getEtapa(mc.TEXTO).ordinal()%3 == storyIndex){
			
			if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.WEST) filter = ( 0xFF008000 );
			else if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.LIBERTY) filter = (0xFFFF4838);
			else if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.BAGEL) filter = (0xFF3848FF);
			insTextView.setTextColor(filter);
			
			
		}
		else 
			{insTextView.setTextColor(Color.BLACK);}
		if(storyIndex==0) filterInicio=filter;
		else if(storyIndex==1) filterDes=filter;
		else if(storyIndex==2) filterFin=filter;
	}

	public void proceed(){
		//mc.setTextCorrected(storyIndex,inputText.getText().toString() );
		
		mc.getNetManager().setReadyListener(null);
		mc.getNetManager().setArgListener(null);;
		Intent i = new Intent(getApplicationContext(), DrawActivity.class);
		startActivity(i);
		finish();
	}
	
	
	@Override
	public void onBackPressed() {}
	
	 void hidekeyboard(){
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
	        	hideSoftInputFromWindow(ib.getWindowToken(), 0);
	 }
	 void showkeyboard(){
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
			showSoftInput(ib, InputMethodManager.SHOW_IMPLICIT);
	 }
}
