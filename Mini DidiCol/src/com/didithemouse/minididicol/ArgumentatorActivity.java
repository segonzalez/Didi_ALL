package com.didithemouse.minididicol;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.didithemouse.minididicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.minididicol.network.NetEvent;
import com.didithemouse.minididicol.network.NetManager.NetEventListener;

public class ArgumentatorActivity extends Activity {

	EditText inputText = null;
	TextView argText = null;
	Button terminar;
	MochilaContents mc = MochilaContents.getInstance();
	Spinner spinner;
	PopupMenu popup;
	public int currentIndex=0;
	
	final static int objectSize = CreateActivity.objectSize;
		
	//SELECTOR [indice] arrays
	//ej: selector[2] = {"La puntuación en el inicio"} // (nada que seleccionar)
	// selector[3] = {"es suficiente","es insuficiente"} //2 opciones
	String[][] selector;
	//indice para lo anterior, por ej, si selectedindex[3]=0, => "es suficiente"
	int [] selectedIndex;	
	//argumentatorTexts [TIPO][ParteDeLaHistoria] (ej, argumentatorTexts[puntuacion][CIERRE]
	String [][]argumentatorTexts = new String[3][3];
	
	//Guardamos donde esta lo que habla del desarrollo o fin del argumetnador
	int selectorIndexDes=0, selectorIndexCierre=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.argumentator);
		inputText = (EditText) findViewById(R.id.inputText);
		argText = (TextView) findViewById(R.id.argText_);
		
		mc.getNetManager().setReadyListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(fromClient == 0) kid1ready = true;
				else if(fromClient == 1) kid2ready = true;
				if(isWaiting && kid1ready && kid2ready)changekid();
			}
		});
		
		mc.getNetManager().setCoordListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				if(ne.cond)
					{currX=ne.i1; currY=ne.i2; spawnMenu(ne.i3);}
				else if(popupWindow != null) popupWindow.dismiss();
			}
		});
		
		inputText.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		findViewById(R.id.argEvent_).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				currX = (int)event.getRawX(); currY=(int)event.getRawY();
				Matrix translate = new Matrix();
				translate.setTranslate(v.getLeft()-50, v.getTop());
				event.transform(translate);
				argText.dispatchTouchEvent(event);
				return false;
			}
		});

		populateSelector();
		
		inputText.setText(mc.getTextEdited(0)+
				"\n\n" +
				mc.getTextEdited(1)+ 
				"\n\n" + 
				mc.getTextEdited(2));
		
		View focus = getCurrentFocus();
		if(focus != null)
		((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focus.getWindowToken(), 0);
		
		inputText.setClickable(false);
		inputText.setFocusable(false);
		inputText.setFocusableInTouchMode(false);
				
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				if(!checkForCompletion()) return;
				flag = false;
				isWaiting=true;
				mc.getNetManager().sendMessage(new NetEvent("argumentator",true) );
				terminar.setBackgroundResource(R.drawable.flechaespera);
				if(kid1ready && kid2ready)changekid();				
			}
		});	
		terminar.setVisibility(View.INVISIBLE);
		
		mc.getNetManager().setArgListener(new NetEventListener() {
			@Override
			public void run(NetEvent ne, int fromClient) {
				selectedIndex[ne.i1] = ne.i2;
				setupArgumentatorBox();
			}
		});
		
		setInstruction();
		
		setupArgumentatorBox();
	}
	
	private void setupArgumentatorBox(){
		checkForCompletion();
	    String text = "";
	    for(int i =0; i<selector.length; i++){
	    	text+= selector[i][selectedIndex[i]];
	    	if(i==selectorIndexDes || i==selectorIndexCierre)
	    		text+="\n";
	    	text+= "  ";
	    }
	    
	    SpannableString ss = new SpannableString(text);
	    for(int i=0; i<selector.length; i++){
	    	if(selector[i].length>1)
	    	setSpanOnLink(ss, i, new LinkSelector(i));
	    }
	    
	    int[][] states = new int[][] {
	    	    new int[] { android.R.attr.state_enabled}, // enabled
	    	    new int[] { android.R.attr.state_selected}, // unchecked
	    	    new int[] { android.R.attr.state_focused}  // pressed
	    	};

	    	int[] colors = new int[] {
	    	    Color.RED,
	    	    Color.GREEN,
	    	    Color.BLUE
	    	};

	    	ColorStateList myList = new ColorStateList(states, colors);
	    
	    argText.setLineSpacing(0f,1.25f);
	    argText.setText(ss);
	    argText.setLinkTextColor(myList);
	    argText.setMovementMethod(LinkMovementMethod.getInstance());
	    argText.setFocusable(true);
	    
	}


	private void setSpanOnLink(SpannableString ss, int index, ClickableSpan cs) {
        int start = 0;
        for(int i =0; i<index; i++){
        	if(i==selectorIndexDes || i==selectorIndexCierre) start+=1;
	    	start+= selector[i][selectedIndex[i]].length()+2;
	    }
        int end = start + selector[index][selectedIndex[index]].length();
        ss.setSpan(cs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
	

	PopupWindow popupWindow;
	int currX=0, currY =0;
	private void spawnMenu(int selectorIndex){
		ListView lv = new ListView(this);
		ArrayAdapter<String> saa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, selector[selectorIndex]); 
		lv.setAdapter(saa);
		
		lv.setOnItemClickListener(new MenuClick(selectorIndex));
		
		//http://www.java2s.com/Code/Android/UI/setListViewHeightBasedOnChildren.htm
        int totalHeight = 0;
        int maxWidth = 0;
        for (int i = 0; i < saa.getCount(); i++) {
            View listItem = saa.getView(i, null, lv);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
            if (listItem.getMeasuredWidth() > maxWidth) 
            	maxWidth = listItem.getMeasuredWidth();
        }
        totalHeight = totalHeight + (lv.getDividerHeight() * (lv.getCount() - 1));
		maxWidth = maxWidth + lv.getVerticalScrollbarWidth();        
		if(popupWindow == null) popupWindow = new PopupWindow(this); 
		popupWindow.dismiss();
		popupWindow.setWidth(maxWidth);
		popupWindow.setHeight(totalHeight);
		popupWindow.setContentView(lv);
		popupWindow.setFocusable(true);
		popupWindow.showAtLocation(argText, Gravity.NO_GRAVITY, currX,currY);
		
		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			
			@Override
			public void onDismiss() {
				if(currentIndex == mc.getEtapa(mc.TEXTO).ordinal()%3)
					mc.getNetManager().sendMessage(new NetEvent(0,0,0,0,0,false));;
			}
		});
		
		if(currentIndex == mc.getEtapa(mc.TEXTO).ordinal()%3)
			mc.getNetManager().sendMessage(new NetEvent(currX,currY,0,0,selectorIndex,true));;
	}
	

	boolean isWaiting = false;
	boolean kid1ready=false, kid2ready = false;
	public void changekid(){
		isWaiting=false;kid1ready=false; kid2ready=false;
		clearAndSaveArg();
		
		currentIndex++;	
		if(currentIndex == 3) {proceed();}
		else{
			setInstruction();
			populateSelector();
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
			terminar.setVisibility(View.INVISIBLE);
			setupArgumentatorBox();
		}
	}
	

	public void populateSelector(){
		int res = R.array.arg_seleccion_ort;
		if(currentIndex == 1) res = R.array.arg_seleccion_vocab;
		else if (currentIndex ==2) res = R.array.arg_seleccion_verbos;
		
		String[][] phrases_ = new String[][]{(getResources().getStringArray(res)[0]).split("\\r?\\n"),
				(getResources().getStringArray(res)[1]).split("\\r?\\n"),
				(getResources().getStringArray(res)[2]).split("\\r?\\n")};
		
		ArrayList<String[]> listselector = new ArrayList<String[]>();
		for(String[] phrases : phrases_){
			for(String phrase : phrases){
				String [] item = phrase.split("\t");
				if(item.length>1 && item[0]!= null)
				{	
					String [] item2 = new String[item.length+1];
					int min = Integer.MAX_VALUE;
					for(int i=0; i< item.length; i++){
						item2[i+1] = item[i]; if( item[i].length() < min) min=item[i].length();
					}
					item2[0] = new String(new char[Math.max(min,3)]).replace("\0", "_");
					item = item2;
				}
				if(item.length>0)
					listselector.add(item);
			}
			//Guardamos que lo que habla del inicio, desarrollo y fin por separado
			if(selectorIndexDes == 0)
				selectorIndexDes = listselector.size();
			else if (selectorIndexCierre == 0)
				selectorIndexCierre= listselector.size();
		}
		
		selector = new String[listselector.size()][];
		selectedIndex = new int[listselector.size()];
		for(int i=0; i< listselector.size(); i++){
			selector[i] = listselector.get(i); 
			selectedIndex[i] = 0;
		}
		
	}
	
	public boolean checkForCompletion(){
		int completedStuff=0;
		int toComplete =0;
		for(int i=0; i< selectedIndex.length; i++)
		{
			if(selector[i].length>1 && selectedIndex[i]!=0) completedStuff++;
			else if(selector[i].length==2) completedStuff++;
			if(selector[i].length>1 ) toComplete++;
		}
		if(toComplete == completedStuff){
			terminar.setVisibility(View.VISIBLE);
			return true;
		}
		return false;
	}
	
	public void clearAndSaveArg(){
		String textInicio="", textDes="",text = "";
	    for(int i =0; i<selector.length; i++){
	    	String current= selector[i][selectedIndex[i]];
	    	if(i== selectorIndexDes){
	    		textInicio = text+"";
	    		text="";
	    	}
	    	else if(i == selectorIndexCierre){
	    		textDes = text+"";
	    		text="";
	    	}
	    	text+= current;
	    	text+= " ";
	    	
	    }
	    argumentatorTexts[currentIndex][0] = textInicio;
	    argumentatorTexts[currentIndex][1] = textDes;
	    argumentatorTexts[currentIndex][2] = text+"";
	    for(int i =0; i<selector.length; i++){
	    	selectedIndex[i]=0;
	    }
	}
	
	TextView insTextView;
	public void setInstruction(){
		if(insTextView==null ) insTextView=((TextView)findViewById(R.id.instruction));
		String name = "";
		if(mc.getEtapa(mc.TEXTO).ordinal()%3 == currentIndex)
			name=mc.getKidName();
		else if(currentIndex == mc.getNetManager().getKidEtapa(0,mc.TEXTO).ordinal()%3)
			name = mc.getNetManager().getKidName(0);
		else if(currentIndex == mc.getNetManager().getKidEtapa(1,mc.TEXTO).ordinal()%3)
			name = mc.getNetManager().getKidName(1);	
		
		String instype = "";
		if(currentIndex==0)
			instype = getResources().getString(R.string.su_ortografia);
		else if(currentIndex==1)
			instype = getResources().getString(R.string.su_puntuacion);
		else if(currentIndex==2)
			instype = getResources().getString(R.string.la_estructura);
		insTextView.setText(String.format(getResources().getString(R.string.argInstruction),name,instype));
		
		if(mc.getEtapa(mc.TEXTO).ordinal()%3 == currentIndex){
			int filter = 0;
			if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.WEST) filter = ( 0xFF008000 );
			else if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.LIBERTY) filter = (0xFFFF4838);
			else if(mc.getEtapa(mc.OBJETOS) == EtapaEnum.BAGEL) filter = (0xFF3848FF);
			insTextView.setTextColor(filter);
		}
		else 
			insTextView.setTextColor(Color.BLACK);
	}

	public void proceed(){
		mc.getNetManager().setReadyListener(null);
		mc.getNetManager().setArgListener(null);;
		mc.getNetManager().setCoordListener(null);
		mc.setArgumentatorTexts(argumentatorTexts);
		Intent i = new Intent(getApplicationContext(), CorrectionActivity.class);
		startActivity(i);
		finish();
	}
	
	@Override
	public void onBackPressed() {}
	
	class LinkSelector extends ClickableSpan{

		int selectorIndex=0;
		public LinkSelector(int _selectorIndex){
			selectorIndex = _selectorIndex;
		}
		
		@Override
		public void onClick(View widget) {
			if(currentIndex != mc.getEtapa(mc.TEXTO).ordinal()%3) return;
			spawnMenu(selectorIndex);
		}
		
	}

	
	class MenuClick implements OnItemClickListener{
		int selectorIndex=0;
		public MenuClick(int _selectorIndex){
			selectorIndex = _selectorIndex;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View arg1, int position,
				long id) {
			if(currentIndex != mc.getEtapa(mc.TEXTO).ordinal()%3) return;
			selectedIndex[selectorIndex] = (position)%(selector[selectorIndex].length);
			mc.getNetManager().sendMessage(new NetEvent(selectorIndex,selectedIndex[selectorIndex]));
			setupArgumentatorBox();
			popupWindow.dismiss();
		}
		
	}
}
