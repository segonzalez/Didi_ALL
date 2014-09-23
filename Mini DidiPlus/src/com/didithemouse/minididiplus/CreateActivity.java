package com.didithemouse.minididiplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.didithemouse.minididiplus.dragdrop.DropPanel;
import com.didithemouse.minididiplus.dragdrop.DropPanelWrapper;
import com.didithemouse.minididiplus.dragdrop.ExtendedImageView;
import com.didithemouse.minididiplus.dragdrop.MyAbsoluteLayout;
import com.didithemouse.minididiplus.dragdrop.MyAbsoluteLayout.LayoutParams;
import com.didithemouse.minididiplus.dragdrop.DragController;
import com.didithemouse.minididiplus.dragdrop.DragLayer;
import com.didithemouse.minididiplus.dragdrop.ViewWrapper;
import com.didithemouse.minididiplus.etapas.EtapaActivity.EtapaEnum;

public class CreateActivity extends Activity implements OnTouchListener{

	private DragController dragController;
	private DragLayer dragLayer;
	FrameLayout fl;
	Button next;
	boolean deleting = false;
	
	MochilaContents mc = MochilaContents.getInstance();
	
	//Constants
	public final static int canvasHeight = 620;
	public final static int canvasWidth = 810;
	
	public final static int canvasHeight_mid = (int)(canvasHeight/1.619);
	public final static int canvasWidth_mid = (int)(canvasWidth/1.619);
	
	final static int canvasHeight_small = (int)(canvasHeight/2.375);
	final static int canvasWidth_small = (int)(canvasWidth/2.375);
	
	final static int topBarHeight = 50+63;
	final static int canvasSelectorWidth = 150;
	final static int noteSpiralWidth = 40;
	
	final static int objectSize = 115;
			
	private DropPanelWrapper panel = MochilaContents.getInstance().getDropPanel();
	private ArrayList<ViewWrapper> items = MochilaContents.getInstance().getItems();
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dragController = new DragController(this);
		dragController.setCreateMode(true);

		setContentView(R.layout.create);  
		dragLayer = (DragLayer) findViewById(R.id.canvas_big_draglayer);
		dragLayer.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
			
		dragLayer.setDragController(dragController);
							
		dragController.setFixedDropTarget(dragLayer);
		dragController.addDropTarget(dragLayer);
		
		updateItemsInDragLayer();
		
		dragLayer.setDropRunnable(new Runnable() {
			@Override
			public void run() {
				if(itemsAreInside()){
			    	next.setVisibility(View.VISIBLE);
			    }
				else
					new Timer().schedule(new TimerTask() {					
						@Override
						public void run() {
							if(next != null)
							next.getHandler().post(new Runnable(){
								@Override
								public void run() {
									if(!deleting && next != null && itemsAreInside())
									next.setVisibility(View.VISIBLE);
								}});	}}, 500);
			}
		});
		

		
		
		
		next = (Button) findViewById(R.id.terminar);
		next.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!itemsAreInside() || !flag) return;
				flag = false;
				proceed();
			}
		});
		next.setClickable(true);
		next.setVisibility(View.INVISIBLE);

		showPanel();		
	}
	
	public void showPanel()
	{
		DropPanel dp = panel.getPanelView(this);
		
		dp.setVisibility(View.VISIBLE);
		dp.setDropRunnable(new Runnable() {
			@Override
			public void run() {
				if(itemsAreInside()){
					next.setVisibility(View.VISIBLE);
			    }
				else
				new Timer().schedule(new TimerTask() {					
					@Override
					public void run() {
						if(itemsAreInside() && next != null)next.
						getHandler().post(new Runnable(){
							@Override
							public void run() {
								if(next != null)
								next.setVisibility(View.VISIBLE);
							}});	}}, 500);
			}
		});
				

		dragLayer.addView(dp,0);
		
		DragLayer.LayoutParams lp = new LayoutParams(canvasWidth, canvasHeight,canvasSelectorWidth+noteSpiralWidth,topBarHeight);
		dragLayer.updateViewLayout(dp, lp);
		dp.setDragController(dragController);
		
		
		dragController.addDropTarget(dp);
				
		updateViewsInPanel();
		
		dragLayer.invalidate();
}
	


    @Override
	public boolean onTouch(View v, MotionEvent m) {
    	if(deleting){return true;}
    	//EVITAR Multitouch
    	if(m.getPointerCount() > 1) return true;
    	if(m.getAction()==MotionEvent.ACTION_MOVE || m.getAction()==MotionEvent.ACTION_DOWN)
    		return startDrag(v);
    	return false;
	}
    
    
	public boolean startDrag (View v)
	{
		if (! (v instanceof ExtendedImageView)) return true;
		ExtendedImageView eiv = (ExtendedImageView)v;
		Object dragInfo = v;
		dragController.startDrag (v, dragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
		return true;
	}
	
	@Override
	public void onBackPressed() {
	}
	
	public void updateViewsInPanel()
	{
		int verticalOffset=topBarHeight;
		int horizontalOffset = canvasSelectorWidth+noteSpiralWidth;
		for(ViewWrapper w : panel.getWrappers()){
			View iv = w.getView(this);
			if(! (iv instanceof ExtendedImageView)) continue;
			if(!panel.getItems(getApplicationContext()).contains(iv)){
				panel.getItems(getApplicationContext()).add(iv);
			}
			
			int left=0, top=0;
			
			ExtendedImageView img = (ExtendedImageView) iv;
			img.setScaleX(1f);
			img.setScaleY(1f);
			
			if(img.getEtapa() == EtapaEnum.WEST) img.setBackgroundResource(R.drawable.bordeverde);
			else if(img.getEtapa() == EtapaEnum.LIBERTY) img.setBackgroundResource(R.drawable.borderojo);
			else if(img.getEtapa() == EtapaEnum.BAGEL) img.setBackgroundResource(R.drawable.bordeazul);
			
				
			left = (int)(w.getX()*canvasWidth);
			top = (int)(w.getY()*canvasHeight);
			
			iv.setContentDescription("no");
			MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(objectSize,objectSize, (int)(horizontalOffset+left) , (int)(verticalOffset+top));
			if(iv.getParent() != null)((ViewGroup)iv.getParent()).removeView(iv);
			iv.setOnTouchListener(this); 
			iv.setVisibility(0);
			dragLayer.addView(iv, lp);
			items.remove(w);

		}
		
	}
	
	boolean itemsAreInside(){
		
		for(View v: panel.getItems(dragLayer.getContext())){
			if (v instanceof ExtendedImageView)
				panel.getPanelView(dragLayer.getContext()).updateObjectWrapper((ExtendedImageView)v);
		}
		if(items.size() != panel.getWrappers().size()) return false;
		for(ViewWrapper vw: panel.getWrappers())
		{
			if(vw.getX() > 1.0) return false;
		}
		return true;
	}
	
	void removePanelItems()
	{
		for(ViewWrapper vw: panel.getWrappers())
		{
			View v = vw.getView(this);
			dragLayer.removeView(v);
			items.remove(vw);
		}
	}
		
	
	void updateItemsInDragLayer()
	{
		int left=0, top=0;
		for(ViewWrapper w : items){
			View iv = w.getView(this);
			if(! (iv instanceof ExtendedImageView)) continue;
			
			ExtendedImageView img = (ExtendedImageView) iv;
			img.setScaleX(1f);
			img.setScaleY(1f);
			
			if(img.getEtapa() == EtapaEnum.WEST) img.setBackgroundResource(R.drawable.bordeverde);
			else if(img.getEtapa() == EtapaEnum.LIBERTY) img.setBackgroundResource(R.drawable.borderojo);
			else if(img.getEtapa() == EtapaEnum.BAGEL) img.setBackgroundResource(R.drawable.bordeazul);
			
			iv.setContentDescription("no");
			MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(objectSize,objectSize, (int)(canvasSelectorWidth+noteSpiralWidth+canvasWidth+left) , (int)(topBarHeight+top));
			if(iv.getParent() != null)((ViewGroup)iv.getParent()).removeView(iv);
			left = left==150 ? 0:150;
			top = left==150? top:objectSize+top;
			
			iv.setOnTouchListener(this); 
			iv.setVisibility(0);
			dragLayer.addView(iv, lp);

		}
	}
	
	public ViewWrapper getWrapperById(int id){
		for(ViewWrapper w : items)
		{
			if(w.getDrawableID() ==id) return w;
		}
		for(ViewWrapper w : panel.getWrappers())
		{
			if(w.getDrawableID() ==id) return w;
		}
		return null;
	}
	
	
	public void proceed(){
		
		deleting = true;
		dragLayer.setDropRunnable(null);
		for(View v: panel.getItems(dragLayer.getContext())){
			if (v instanceof ExtendedImageView)
				panel.getPanelView(dragLayer.getContext()).updateObjectWrapper((ExtendedImageView)v);
		}
		
		Intent i = new Intent(getApplicationContext(), WriteActivity.class);
		removePanelItems();
		startActivity(i);
		finish();
	}
}
