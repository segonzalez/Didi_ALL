package com.didithemouse.didiplus;

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

import com.didithemouse.didiplus.etapas.EtapaActivity.EtapaEnum;

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
						
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				changekid();				
			}
		});	
				
		setInstruction();
		
		String atext = "";
		for(int i=0; i<3; i++){
			atext += argTexts[i][storyIndex%3];
			atext += "\n\n";
		}
		argText.setText(atext);
		
		setStoryIndex(0);
		
	}
	
	
	 boolean ignoreNextTextChange = false;
	 
	 public void setStoryIndex(int index)
	 {
		 mc.setTextCorrected(storyIndex,inputText.getText().toString() );
		 if(index >= 3) {storyIndex++; return;}
		 //esconder/mostrar la tab segun corresponda
		 
		 inputText.requestFocusFromTouch();
		 
		 ignoreNextTextChange=true;
		 inputText.setText(mc.getTextCorrected(index));
		 ignoreNextTextChange=false;
		 storyIndex=index;
		 
	 }
	

	public void changekid(){

		setStoryIndex(storyIndex+1);
		inputText.setFocusable(true);
		inputText.setFocusableInTouchMode(true);	
		inputText.requestFocusFromTouch();
		showkeyboard();
		
		if(storyIndex == 3) {proceed();}
		else{
			setInstruction();
			
			String atext = "";
			for(int i=0; i<3; i++){
				atext+= argTexts[i][storyIndex%3]+ "\n\n";
			}
			argText.setText(atext);
			
			setStoryIndex(storyIndex);
			terminar.setBackgroundResource(R.drawable.flecha);
			terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
					if(!flag) return;
					flag = false;
				
					changekid();				
				}
			});
		}
	}
	
	TextView insTextView;
	public void setInstruction(){
		insTextView = (TextView)findViewById(R.id.instruction);
		String instype = "";
		if(storyIndex==0)
			instype = getResources().getString(R.string.inicio);
		else if(storyIndex==1)
			instype = getResources().getString(R.string.desarrollo);
		else if(storyIndex==2)
			instype = getResources().getString(R.string.cierre);
		insTextView.setText(String.format(getResources().getString(R.string.corrInstruction),instype));
		
	}

	public void proceed(){
		//mc.setTextCorrected(storyIndex,inputText.getText().toString() );
		
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
