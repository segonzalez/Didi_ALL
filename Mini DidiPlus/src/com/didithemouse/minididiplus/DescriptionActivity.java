package com.didithemouse.minididiplus;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;

import com.didithemouse.minididiplus.Saver.ActivityEnum;
import com.didithemouse.minididiplus.dragdrop.ExtendedImageView;
import com.didithemouse.minididiplus.dragdrop.ViewWrapper;
import com.didithemouse.minididiplus.etapas.EtapaActivity;

public class DescriptionActivity extends Activity {

	private FrameLayout letraCanvas = null;
	EditText inputText = null;
	TextView instruction=null;
	Button terminar;
	MochilaContents mc = MochilaContents.getInstance();
	
	final static int objectSize = CreateActivity.objectSize;
	boolean controlTextChange=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.description);
		
		View bg = findViewById(R.id.root);
		bg.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hidekeyboard();return false;
			}
		});	
		
		inputText = (EditText) findViewById(R.id.inputText);
		instruction=(TextView) findViewById(R.id.instruction);
		instruction.setFocusable(false);
		instruction.setFocusableInTouchMode(false);

		inputText.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		letraCanvas =(FrameLayout) findViewById(R.id.letraCanvas);
		
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(inputText.getText().toString().equals("") || !flag) return;
				flag = false;
				finishEdit();
			}
		});
		terminar.setClickable(true);
		terminar.setVisibility(View.INVISIBLE);
		
		inputText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(controlTextChange)
				{
					terminar.setVisibility(View.VISIBLE);;
					controlTextChange=false;
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
				
	}
	
	public void finishEdit()
	{

		MochilaContents.getInstance().addDescription(inputText.getText().toString());
		if(!mc.isEverythingVisited()){
			Intent i = new Intent(getApplicationContext(), EtapaActivity.class);
			startActivity(i);
			finish();
		}
		else
		{
		
			ShowContent();

			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		
			instruction.setText(getResources().getString(R.string.descInstruction2));
			
			String descriptions = "";
			for(int i=0; i<3; i++){
				descriptions += mc.getDescription(i) +"\n\n";
			}
			
			inputText.setText(descriptions);
			
			inputText.setFocusable(false);
			inputText.setFocusableInTouchMode(false);
						
			terminar.setOnClickListener(new View.OnClickListener() {
				boolean flag = true;
				public void onClick(View v) {
					if(!flag) return;
					flag = false;
					proceed();				
				}
			});
		}
	}
	
	 public void ShowContent() {
	    	ArrayList<ViewWrapper> wrappers = mc.getItems();

			int left=200;
			for(ViewWrapper w : wrappers){
				w.destroyView();
				View iv = w.getView(getApplicationContext());
				if(iv==null) continue;
				if(iv instanceof ExtendedImageView){
					ExtendedImageView img = (ExtendedImageView)iv;
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(objectSize, objectSize);
					lp.leftMargin = left;
					lp.topMargin = 350;
					left+=130;
					letraCanvas.addView(img, lp);
				}
			}
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
		Intent i = new Intent(getApplicationContext(), CreateActivity.class);
		startActivity(i);
		finish();
	}
}
