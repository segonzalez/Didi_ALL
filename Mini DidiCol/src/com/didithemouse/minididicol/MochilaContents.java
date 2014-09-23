package com.didithemouse.minididicol;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

import com.didithemouse.minididicol.dragdrop.DropPanelWrapper;
import com.didithemouse.minididicol.dragdrop.ExtendedImageView;
import com.didithemouse.minididicol.dragdrop.ViewWrapper;
import com.didithemouse.minididicol.etapas.EtapaActivity.EtapaEnum;
import com.didithemouse.minididicol.network.NetManager;

public class MochilaContents {

	private DropPanelWrapper dropPanel;
	private ArrayList<ViewWrapper> items;
	private ArrayList<ViewWrapper> netItems;
	private String[] textsCorrected;
	private String[] textsEdited;
	private String[] textsOriginal;
	private String description = "";
	private String title = "";
	private String[][] argumentatorTexts;
	
	//private boolean created;
	//public boolean hasLoaded;
	
	private int kidNumber = 0;
	private String kidName = "";
	private int kidGroup = 0;
	private String dirName = "" ;
	//private String RCSdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/My SugarSync Folders/My SugarSync/RC-Write/";
	private String RCSdir = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Testminididicol/";
	
	public EtapaEnum [] etapa;
	protected boolean [] visitedPlaces;
	
	//Deshabilita log, guardado, etc. para debug.
	public static final boolean SAVING  = true;
	public static final boolean LOGGING = false;
	public static final boolean SKIP_OBJECTS = false;
	
	private static MochilaContents INSTANCE = new MochilaContents();
	private MochilaContents() {

	}
	public static MochilaContents getInstance() {
        return INSTANCE;
    }
	
	public DropPanelWrapper getDropPanel() {
		if (dropPanel == null) dropPanel = new DropPanelWrapper();
		return dropPanel;
	}
	
	public void setDropPanel(DropPanelWrapper _dropPanel) {
		dropPanel = _dropPanel;
	}

	
	public void addItem(ExtendedImageView v) {
		float vSize = Math.max(v.getHeight(), v.getWidth());
		
		float scaleFactor = CreateActivity.objectSize*1.0f/vSize; 
		v.setScaleFactor(scaleFactor);

		ViewWrapper vw = new ViewWrapper(0, 0, v, v.getEtapa());
		items.add(vw);
		vw.destroyView();
	}
	
	public void addItem(ViewWrapper v) {
		items.add(v);
	}
	
	public ArrayList<ViewWrapper> getItems()
	{
		return items;
	}
	
	public void addNetItem(ViewWrapper v) {
		netItems.add(v);
	}
	public void mergeNetItems()
	{
		items.addAll(netItems);
		ArrayList<ViewWrapper> vws = new ArrayList<ViewWrapper>();
		while(items.size()>0){
			int index=0;int min=Integer.MAX_VALUE;
			for (int i =0; i<items.size(); i++){
				ViewWrapper vw = items.get(i);
				if(vw == null) continue;
				int drawid = vw.getDrawableID();
				if(min> drawid)
				{index=i;min = drawid;}
			}
			vws.add(items.get(index));
			items.remove(index);
		}
		items = vws;
		netItems.clear();
	}
	
	public void setTextCorrected(int index, String text)
	{
		if(textsCorrected == null) textsCorrected = new String[] {"","",""};
		textsCorrected[index%3] = text;
	}
	public String getTextCorrected(int index)
	{
		if(textsCorrected == null) textsCorrected = new String[] {"","",""};
		return textsCorrected[index%3];
	}
	public void setTextEdited(int index, String text)
	{
		if(textsEdited == null) textsEdited = new String[] {"","",""};
		textsEdited[index%3] = text;
	}
	public String getTextEdited(int index)
	{
		if(textsEdited == null) textsEdited = new String[] {"","",""};
		return textsEdited[index%3];
	}
	public String getTextOriginal(int index)
	{
		if(textsOriginal == null) textsOriginal = new String[] {"","",""};
		return textsOriginal[index%3];
	}
	public void setTextOriginal(int index, String text)
	{
		if(textsOriginal == null) textsOriginal = new String[] {"","",""};
		textsOriginal[index%3] = text;
	}
	public String[] getTextsCorrected(){ return textsCorrected;}
	public String[] getTextsEdited(){ return textsEdited;}
	public String[] getTextsOriginal(){ return textsOriginal;}
	
	public void cloneTextsOriginal(){
		for(int i =0; i<3; i++)
		textsOriginal[i]=textsEdited[i];
	}
	public void cloneTextsCorrected(){
		for(int i =0; i<3; i++)
		textsCorrected[i]=textsEdited[i];
	}
	
	public void setDescription(String _description)
	{
		description=_description;
	}
	public String getDescription()
	{
		return description;
	}
	public void setTitle(String _title)
	{
		title=_title;
	}
	public String getTitle()
	{
		return title;
	}
	
	
	public void setArgumentatorTexts(String[][] _texts){argumentatorTexts=_texts;}
	public String[][] getArgumentatorTexts(){
		if(argumentatorTexts == null) argumentatorTexts=new String[][]{{"","",""},{"","",""},{"","",""}};
		return argumentatorTexts;}
	
	public void cleanPanels()
	{
		for(ViewWrapper wx: items)
			wx.destroyView();
		dropPanel.cleanPanel(true);
	}
	
	public void setKid(int _kidNumber,String _kidName, int _kidGroup) { 
		kidNumber = _kidNumber;
		kidName = _kidName != null? _kidName: "";
		kidGroup = _kidGroup;
		
		dirName = RCSdir +"/"+kidNumber+"/";

	}
	
	public void makeDirs(){
		if(!dirName.equals(""))
		(new File (dirName)).mkdirs();
	}
	
	public int getKidNumber(){ return kidNumber; }
	public int getKidGroup(){ return kidGroup; }
	public String getKidName(){ return kidName; }
	public String getDirectory() { return dirName; }
	
	
	public static int LECTURA=0, OBJETOS=1, TEXTO=2;
	public EtapaEnum getEtapa(int index){return etapa!=null? 
												etapa[index%3]:EtapaEnum.WEST;}
	public void setEtapas(EtapaEnum e0, EtapaEnum e1, EtapaEnum e2){
		if (etapa == null) return;
		etapa[0] = e0; etapa[1] = e1; etapa[2] = e2;
	}
	
	private final String logDirname =  RCSdir + "/log/";
	
	public String getLogDirname()
	{
		File f = new File(logDirname);
		if (!f.exists()) f.mkdirs();
		return logDirname;
	}
	
	
	public boolean kidExists(int num)
	{
		String dirnameX = RCSdir +"/"+num+"/" ;
        return (new File(dirnameX)).exists();
        	
	}
	
	public void setVisited(int i, boolean is){
		visitedPlaces[i%4] = is;
	}
	public boolean isVisited(int i){
		return visitedPlaces[i%4];
	}
	
	
	NetManager netManager = null;
	public NetManager getNetManager(){
		return netManager;
	}
	
	public void restart(Context _c)
	{
		if(netManager!= null) netManager.cleanup();
		if(_c != null){	netManager = new NetManager(_c);}
		else { netManager = null;	}

		kidNumber = 0;
		kidName = "";
		kidGroup = 0;
		dirName = "" ;
		
		if (dropPanel != null)
			dropPanel.killPanel(true);
		if (items != null)
		for(ViewWrapper wx: items)
			wx.destroyView();
		
		netItems = new ArrayList<ViewWrapper>();
		
		items = new ArrayList<ViewWrapper>();
		dropPanel = new DropPanelWrapper();
		textsCorrected = new String[] {"","",""};
		textsEdited = new String[] {"","",""};
		textsOriginal= new String[] {"","",""};
		etapa = new EtapaEnum[3];
		//created = false;
		
		description="";
		title="";
		
		visitedPlaces=new boolean[]{false,false,false,false};

	}
	
	
	
}
