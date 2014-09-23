package com.didithemouse.didiplus;


import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.didithemouse.didiplus.Saver.ActivityEnum;
import com.didithemouse.didiplus.etapas.EtapaActivity;
import com.didithemouse.didiplus.etapas.EtapaActivity.EtapaEnum;

public class Principal extends Activity {
  
	EditText etNumber;
	EditText etName;
	Button cargar;
	Button comenzar;
	MochilaContents mc = MochilaContents.getInstance();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.principal);

		cleanup();
		
		View vcomenzar = findViewById(R.id.iniciars);
		etNumber = (EditText) findViewById(R.id.numKidText);
		etName = ((EditText) findViewById(R.id.kidNameText));
		etNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_DATETIME_VARIATION_NORMAL);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			
		View bg = findViewById(R.id.root);
		bg.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
				hideSoftKeyboard();
				updateButtons();
				return false;
			}
		});		
		
		comenzar = (Button) vcomenzar;
		comenzar.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		comenzar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {comenzarAction();}
		});
		setComenzarState(false);

		
		cargar = (Button) findViewById(R.id.load);
		cargar.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		cargar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {cargarAction();}
		});
		setCargarState(false);
		
		
		etNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateButtons();
				return false;
			}
		});
		etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateButtons();
				return false;
			}
		});
				
		//PARA TESTEAR!!!
		//setCargarState(true);
		/*
		setComenzarState(true);
		
		int randint = 100000+new Random().nextInt(100000);
		etNumber.setText(randint+"");
			etName.setText("RandKid");*/
    }
    
    void hideSoftKeyboard() {
		 View ib = getCurrentFocus();
		 if(ib != null)
			((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).
	        	hideSoftInputFromWindow(ib.getWindowToken(), 0);
	 }
    
    void updateButtons()
    {
    	if( cargar == null ||  comenzar == null) return;
    	String textNumber = etNumber.getText().toString();
    	String kidName = etName.getText().toString();
		int kidNumber = 0;
		try
		{
		   kidNumber = Integer.parseInt(textNumber);
		}
		catch (NumberFormatException ignoreException) {}
		if(MochilaContents.getInstance().kidExists(kidNumber))
		{
			setCargarState(true);
			setComenzarState(false);
			
		}
		else if (kidName == null || kidName.equals("") || kidNumber == 0  )
		{
			setCargarState(false);
			setComenzarState(false);
		}
		else
		{
			setCargarState(false);
			setComenzarState(true);
		}
    }
    
    void setCargarState(boolean isActive)
    {
    	cargar.setClickable(isActive);
		cargar.setFocusable(isActive);
		cargar.setTextColor(isActive? Color.WHITE:Color.DKGRAY);
    }
    void setComenzarState(boolean isActive)
    {
    	comenzar.setClickable(isActive);
    	comenzar.setFocusable(isActive);
    	comenzar.setTextColor(isActive? Color.WHITE:Color.DKGRAY);
    }
        
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    };
    
    @Override
    protected void onPause() {
        super.onPause();

    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
        
    protected void cleanup() {
    	mc.restart();
		comenzarFlag = false;cargarFlag=false;
		System.gc();
    	Log.d("netconnect", "APP RESTART");
    }
        
    
	public boolean comenzarFlag = false;
	public void comenzarAction() {
		if(comenzarFlag) return;
		if(!comenzarFlag) comenzarFlag = true;
		String kidName = etName.getText().toString();
		String textNumber = etNumber.getText().toString();
		int kidNumber = 0;
		
		try	{  kidNumber = Integer.parseInt(textNumber); 
		}catch (Exception ignoreException) { comenzarFlag=false;return;}
		
		if (kidName == null || kidName.equals("")) {comenzarFlag=false;return;}
		

		if (MochilaContents.getInstance().kidExists(kidNumber))	{comenzarFlag=false;return;}
		

		MochilaContents.getInstance().setKid(kidNumber, kidName);
    	Intent intent = new Intent(comenzar.getContext().getApplicationContext(), EtapaActivity.class);
		startActivity(intent);
    	finish();
	}
	
	public boolean cargarFlag = false;
	public void cargarAction() {
		if(cargarFlag) return;
		if(!cargarFlag) cargarFlag = true;
		
		String textNumber = etNumber.getText().toString();
		int kidNumber = 0;
		try
		{
		   kidNumber = Integer.parseInt(textNumber);
		}
		catch (NumberFormatException ignoreException) { cargarFlag=false; return;}
		
		MochilaContents.getInstance().setKid(kidNumber, "");

		ActivityEnum result = Saver.loadPresentation();
		if(result == ActivityEnum.KARAOKE){
			Intent intent = new Intent(comenzar.getContext().getApplicationContext(), KaraokeActivity.class);
			startActivity(intent);
    		finish();
		}else if(result == ActivityEnum.END){
			Intent intent = new Intent(cargar.getContext().getApplicationContext(), LoadActivity.class);
			startActivity(intent);
			finish();
        }
        else{cargarFlag = false;}
	}
	
	@Override
	public void onBackPressed() {
		mc.restart();
		finish();
		super.onBackPressed();
	}
	
}