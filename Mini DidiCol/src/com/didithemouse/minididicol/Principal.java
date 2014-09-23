package com.didithemouse.minididicol;


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

import com.didithemouse.minididicol.Saver.ActivityEnum;
import com.didithemouse.minididicol.etapas.EtapaActivity;
import com.didithemouse.minididicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.minididicol.network.NetEvent;
import com.didithemouse.minididicol.network.NetManager.NetEventListener;

public class Principal extends Activity {
  
	EditText etNumber;
	EditText etName;
	EditText etGroup;
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
		etGroup = (EditText) findViewById(R.id.kidGroupNum);
		etNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_DATETIME_VARIATION_NORMAL);
		etGroup.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_DATETIME_VARIATION_NORMAL);
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
		etGroup.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				updateButtons();
				return false;
			}
		});
		
		//ENFORCE DEFAULTS
		isWaiting = false; kid1ready=false; kid2ready = false;
		loadActivity = ActivityEnum.ETAPA;
		
		//PARA TESTEAR!!!
		/*
		setCargarState(true);
		//setComenzarState(true);
		
		int randint = 100000+new Random().nextInt(100000);
		etNumber.setText(randint+"");
		if(Build.SERIAL.equals("c1607f91d8d70cf")){
			etName.setText("Tablet05");
			etNumber.setText("101");}
		else if(Build.SERIAL.equals("c1607850186e111")){
			etName.setText("Tablet06");
			etNumber.setText("100");}	
		else{
			etName.setText("Tablet10");
			etNumber.setText("102");}
		etGroup.setText("9");	*/
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
    	String textGroup = etGroup.getText().toString();
		int kidNumber = 0;
		int kidGroup = 0;
		try
		{
		   kidNumber = Integer.parseInt(textNumber);
		   kidGroup  = Integer.parseInt(textGroup );
		}
		catch (NumberFormatException ignoreException) {}
		if(MochilaContents.getInstance().kidExists(kidNumber))
		{
			setCargarState(true);
			setComenzarState(false);
			
		}
		else if (kidName == null || kidName.equals("") || kidNumber == 0 || kidGroup==0 )
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
    
    
	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	ActivityEnum loadActivity = ActivityEnum.ETAPA;
    public void proceed(){
    	//Las etapas no quuedan guardadas localmente (en el .xml), se deciden dinamicamente!
    	decidirEtapas(mc.getKidNumber(),mc.getNetManager().getKid(0),mc.getNetManager().getKid(1));
    	if(loadActivity == ActivityEnum.ETAPA){
    		Intent intent = new Intent(comenzar.getContext().getApplicationContext(), EtapaActivity.class);
			startActivity(intent);
    		finish();
    	}else if(loadActivity == ActivityEnum.KARAOKE){
    		Intent intent = new Intent(comenzar.getContext().getApplicationContext(), KaraokeActivity.class);
			startActivity(intent);
    		finish();
    	}
    }
    
    protected void cleanup() {
		mc.restart(this.getApplicationContext());
		isWaiting = false;	kid1ready=false; kid2ready = false;
		comenzarFlag = false;cargarFlag=false;
		System.gc();
    	Log.d("netconnect", "APP RESTART");
    	mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)proceed();
			}
		});	
    }
    
    public void decidirEtapas(int num, int c1, int c2){

    	if(num < c1 && num < c2)     mc.setEtapas(EtapaEnum.WEST,EtapaEnum.LIBERTY,EtapaEnum.BAGEL);
		else if (num > c1 && num>c2) mc.setEtapas(EtapaEnum.LIBERTY,EtapaEnum.BAGEL,EtapaEnum.WEST);
		else 	                     mc.setEtapas(EtapaEnum.BAGEL,EtapaEnum.WEST,EtapaEnum.LIBERTY);
		
		if    (c1 < c2 && c1 < num) mc.getNetManager().setKidEtapas(0, EtapaEnum.WEST,EtapaEnum.LIBERTY,EtapaEnum.BAGEL);
		else if (c1 > c2 && c1>num) mc.getNetManager().setKidEtapas(0, EtapaEnum.LIBERTY,EtapaEnum.BAGEL,EtapaEnum.WEST);
		else                        mc.getNetManager().setKidEtapas(0, EtapaEnum.BAGEL,EtapaEnum.WEST,EtapaEnum.LIBERTY);
								
		if(c2 < c1 && c2 < num)     mc.getNetManager().setKidEtapas(1, EtapaEnum.WEST,EtapaEnum.LIBERTY,EtapaEnum.BAGEL);
		else if (c2 > c1 && c2>num) mc.getNetManager().setKidEtapas(1, EtapaEnum.LIBERTY,EtapaEnum.BAGEL,EtapaEnum.WEST);
		else                        mc.getNetManager().setKidEtapas(1, EtapaEnum.BAGEL,EtapaEnum.WEST,EtapaEnum.LIBERTY);
			
    	Log.d("netconnect", "kid: " + num + " c1: " + c1 + " c2 " + c2+ " Etapa: "+ mc.getEtapa(mc.LECTURA).toString() );
    }
    
    
    
	public boolean comenzarFlag = false;
	public void comenzarAction() {
		if(comenzarFlag) return;
		if(!comenzarFlag) comenzarFlag = true;
		String kidName = etName.getText().toString();
		String textNumber = etNumber.getText().toString();
		String textGroup = etGroup.getText().toString();
		int kidNumber = 0;
		int kidGroup=0;
		
		try	{  kidNumber = Integer.parseInt(textNumber); 
			   kidGroup = Integer.parseInt(textGroup);
		}catch (Exception ignoreException) { comenzarFlag=false;return;}
		
		if (kidName == null || kidName.equals("")) {comenzarFlag=false;return;}
		

		if (MochilaContents.getInstance().kidExists(kidNumber))	{comenzarFlag=false;return;}
		

		MochilaContents.getInstance().setKid(kidNumber, kidName,kidGroup);
		comenzar.setText(getResources().getString(R.string.espere));
		MochilaContents.getInstance().getNetManager().searchConnect(
			//Si se conectan
			new Runnable() {
				@Override
				public void run() {
					isWaiting=true;
					if(kid1ready && kid2ready)proceed();
			}},
			//Si falla la conexion
			new Runnable() {
				@Override
				public void run() {
					cleanup();
					comenzar.setText(getResources().getString(R.string.comenzar));
					comenzarFlag = false;
					comenzar.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {comenzarAction();}
					});
			}}
		);
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.espereTablets), Toast.LENGTH_SHORT).show();
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
		
		MochilaContents.getInstance().setKid(kidNumber, "",0);

		ActivityEnum result = Saver.loadPresentation();
		if(result == ActivityEnum.KARAOKE){
			//RE-CONECTAR LAS TABLETS
			loadActivity = ActivityEnum.KARAOKE;
			MochilaContents.getInstance().getNetManager().searchConnect(
					//Si se conectan
					new Runnable() {
						@Override
						public void run() {
							isWaiting=true;
							if(kid1ready && kid2ready)proceed();
					}},
					//Si falla la conexion
					new Runnable() {
						@Override
						public void run() {
					}}
				);
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.espereTablets), Toast.LENGTH_SHORT).show();
        }else if(result == ActivityEnum.END){
			Intent intent = new Intent(cargar.getContext().getApplicationContext(), LoadActivity.class);
			startActivity(intent);
			finish();
        }
        else{cargarFlag = false;}
	}
	
	@Override
	public void onBackPressed() {
		mc.restart(null);
		finish();
		super.onBackPressed();
	}
	
}