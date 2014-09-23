package com.didithemouse.didiplus;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.didithemouse.didiplus.Saver.ActivityEnum;
import com.didithemouse.didiplus.dragdrop.DropPanelWrapper;
import com.didithemouse.didiplus.dragdrop.ViewWrapper;
import com.didithemouse.didiplus.etapas.EtapaActivity.EtapaEnum;

public class KaraokeActivity extends Activity{
	TextView inputText = null;
	Button terminar;
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

		setContentView(R.layout.karaoke);
		
		
		
		
		
		inputText = (TextView) findViewById(R.id.inputText);
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
		
		String text = "";
		for(int i=0; i<3; i++)
			text+= mc.getTextEdited(i)+"\n\n";
		inputText.setText(text);
		
		terminar = (Button) findViewById(R.id.terminar);	
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				proceed();
			}
		});
		
		inputText.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		((TextView)findViewById(R.id.instruction)).setText(getResources().getString(R.string.karaokeInstruction));
			
	}
	
	
	public void proceed(){
		Intent i = new Intent(getApplicationContext(), ArgumentatorActivity.class);
		
		startActivity(i);
		finish();
	}
	
	@Override
	public void onBackPressed() {
	}
}