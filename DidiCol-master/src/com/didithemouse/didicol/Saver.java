package com.didithemouse.didicol;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.didithemouse.didicol.dragdrop.DropPanelWrapper;
import com.didithemouse.didicol.dragdrop.ViewWrapper;
import com.didithemouse.didicol.etapas.EtapaActivity.EtapaEnum;

//http://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/


public class Saver {
	
	public enum ActivityEnum {ETAPA,DESCRIPTION,CREATE,WRITE,KARAOKE,END, ERROR}
	
	public static void savePresentation(ActivityEnum fromActivity)
	{		
		MochilaContents mc = MochilaContents.getInstance();
		if(!MochilaContents.SAVING) return;
		
		mc.makeDirs();
		
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (Exception e) {		
			Log.d("SAVER", "FAILED TRANSFORM (1)");
			e.printStackTrace();
			return;
		}
		
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Log.d("SAVER", "FAILED TRANSFORM (2)");
			e.printStackTrace();
			return;
		}
		Document doc = db.newDocument();
		
		Element root = doc.createElement("savedFile");
		root.setAttribute("kidNumber", "" + mc.getKidNumber());
		root.setAttribute("kidName", mc.getKidName());
		root.setAttribute("kidGroup", mc.getKidGroup()+"");
		doc.appendChild(root);
		
		Element flags = doc.createElement("visitedFlags");
		for(int i=0; i<4; i++)
			flags.setAttribute("etapa"+i, mc.isVisited(i) + "");
		root.appendChild(flags);
		
		Element lastActivity = doc.createElement("lastActivity");
		lastActivity.setAttribute("last", fromActivity.name());
		root.appendChild(lastActivity);
		
		Element description = doc.createElement("description");
		description.setAttribute("desc", mc.getDescription());
		root.appendChild(description);
		
		Element title = doc.createElement("title");
		title.setAttribute("value", mc.getTitle());
		root.appendChild(title);
		
		Element mochila = doc.createElement("mochila");
		root.appendChild(mochila);
		
		for(ViewWrapper vw: mc.getItems())
		{
			Element item = doc.createElement("item");
			item.setAttribute("imageID",vw.getDrawableID()+"" );
			item.setAttribute("scaleFactor",vw.getScaleFactor()+"" );
			item.setAttribute("etapa", vw.getEtapa().name());
			mochila.appendChild(item);
		}
		
		//
		Element texts = doc.createElement("texts");
		root.appendChild(texts);
		for(int i=0; i<3; i++)
		{
			Element text = doc.createElement("text");
			text.setAttribute("index", i+"");
			text.setAttribute("valueCorrected", mc.getTextsCorrected()[i]);
			text.setAttribute("valueEdited", mc.getTextsEdited()[i]);
			text.setAttribute("valueOriginal", mc.getTextsOriginal()[i]);
			texts.appendChild(text);
		}
		//
		Element argTexts = doc.createElement("argTexts");
		root.appendChild(argTexts);
		for(int i=0; i<3; i++)
		{
			Element argText = doc.createElement("argText");
			argText.setAttribute("index", i+"");
			for(int j=0; j<3; j++)
				argText.setAttribute("value"+j, mc.getArgumentatorTexts()[i][j]);
			argTexts.appendChild(argText);
		}
		//
		
		
		Element panel = doc.createElement("panel");
		root.appendChild(panel);
			
		DropPanelWrapper dpw = mc.getDropPanel();
			//panel.setAttribute("text", dpw.getText());
			
			Element bitmap = doc.createElement("bitmap");
			boolean hasDrawn = dpw.hasDrawn();
					
			bitmap.setAttribute("hasDrawn", "" + hasDrawn);
			if (hasDrawn){
				String bitmapFilename = mc.getDirectory() + "/panelDraw_"+ ".png"; 
				bitmap.setAttribute("bitmapFilename", bitmapFilename);
				Bitmap panelBitmap = dpw.getBitmap();
				try {
			       	FileOutputStream out = new FileOutputStream(bitmapFilename);
			       	panelBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				} catch (Exception e) {
				}
			}
			panel.appendChild(bitmap);
			
			
			Element views = doc.createElement("views");
			panel.appendChild(views);
			
			for(ViewWrapper vw : dpw.getWrappers())
			{
				Element view = doc.createElement("view");
				
				int drawID = vw.getDrawableID();
				boolean isImage = (drawID != -1);
				view.setAttribute("type", isImage? "image":"text");
				
				if (isImage)
				{
					Element image = doc.createElement("image");
					image.setAttribute("imageID", drawID+"");
					image.setAttribute("scaleFactor", vw.getScaleFactor() + "");
					image.setAttribute("etapa",vw.getEtapa().name());
					view.appendChild(image);
				}
				else
				{
					Element text = doc.createElement("text");
					text.setAttribute("drawText", vw.getText());
					view.appendChild(text);
				}
				
				
				Element coords = doc.createElement("coords");
				coords.setAttribute("x1", vw.getX()+"");
				coords.setAttribute("y1", vw.getY()+"");
				coords.setAttribute("x2", 0.0+"");
				coords.setAttribute("y2", 0.0+"");
				view.appendChild(coords);
							
				views.appendChild(view);
								
			}
			
		
		
		File file = new File(mc.getDirectory()+ "/saveFile.xml");
		file.delete();
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(mc.getDirectory()+ "/saveFile.xml");
		}catch (Exception e) {return;}
		
		Result output = new StreamResult(out);
		Source input = new DOMSource(doc);

		try {
			transformer.transform(input, output);
			out.close();
		} catch (TransformerException e) {
			Log.d("SAVER", "FAILED TRANSFORM (3)");
			e.printStackTrace();
			return;
		} catch(Exception e) {return;}
	}
	
	
	public static ActivityEnum loadPresentation()
	{
		MochilaContents mc = MochilaContents.getInstance();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return ActivityEnum.ERROR;
		}
		
		Document doc;
		File file = new File(mc.getDirectory()+ "/saveFile.xml");
		if(!file.exists()) return ActivityEnum.ERROR;
		try {
			doc = db.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
			return ActivityEnum.ERROR;
		}
		
		doc.getDocumentElement().normalize();
		Element rootNode = doc.getDocumentElement();
		String kidName = rootNode.getAttribute("kidName");
		int kidNumber = parseInt(rootNode.getAttribute("kidNumber"));
		int kidGroup = parseInt(rootNode.getAttribute("kidGroup"));
		mc.setKid(kidNumber, kidName, kidGroup);
		
		Element visitedFlags = getChildrenFromElement(rootNode,"visitedFlags").get(0);
		for(int i=0; i<4; i++){
			mc.setVisited(i, parseBool(visitedFlags.getAttribute("etapa")));
		}
		
		Element lastActivity = getChildrenFromElement(rootNode,"lastActivity").get(0);
		String last = lastActivity.getAttribute("last");
		
		Element description = getChildrenFromElement(rootNode,"description").get(0);
		String desc = description.getAttribute("desc");
		mc.setDescription(desc);
		
		Element title = getChildrenFromElement(rootNode,"title").get(0);
		String titleV = title.getAttribute("value");
		mc.setTitle(titleV);
		
		Element mochilaElement = getChildrenFromElement(rootNode,"mochila").get(0);
		List<Element> itemElement = getChildrenFromElement(mochilaElement,"item");
		for (Element item : itemElement)
		{
			int id = parseInt(item.getAttribute("imageID"));
			float scale = parseFloat(item.getAttribute("scaleFactor"));
			EtapaEnum etapa = EtapaEnum.valueOf(item.getAttribute("etapa"));
			mc.addItem(new ViewWrapper(0, 0,  id,scale,etapa));
		}
		
		Element texts = getChildrenFromElement(rootNode,"texts").get(0);
		List<Element> textElement = getChildrenFromElement(texts,"text");
		
		for(Element text: textElement)
		{
			int id = parseInt(text.getAttribute("index"));
			String valueCorrected = text.getAttribute("valueCorrected");
			String valueEdited = text.getAttribute("valueEdited");
			String valueOriginal = text.getAttribute("valueOriginal");
			mc.setTextEdited(id, valueEdited);
			mc.setTextOriginal(id, valueOriginal);
			mc.setTextCorrected(id, valueCorrected);
		}
		//
		Element argTexts = getChildrenFromElement(rootNode,"argTexts").get(0);
		List<Element> argElement = getChildrenFromElement(argTexts,"argText");
		for(Element argText: argElement)
		{
			int id = parseInt(argText.getAttribute("index"));
			for(int j=0; j<3;j++){
				String value = argText.getAttribute("value"+j);
				mc.getArgumentatorTexts()[id][j] = value;
			}
		}
		
		///
		
		
		Element panelElement = getChildrenFromElement(rootNode,"panel").get(0);

		
			DropPanelWrapper dpw = new DropPanelWrapper();
			
			//dpw.setText(panelElement.getAttribute("text"));
						
			Element bitmapElement = getChildrenFromElement(panelElement,"bitmap").get(0);
			boolean hasDrawn = parseBool(bitmapElement.getAttribute("hasDrawn"));
			
			
			if(hasDrawn)
			{
				String filename = bitmapElement.getAttribute("bitmapFilename");
				filename = new File(filename).getAbsolutePath();
				
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inMutable = true;
				Bitmap b = BitmapFactory.decodeFile(filename,opts);
				dpw.setBitmap(b);
			}
			
			Element viewsElement = getChildrenFromElement(panelElement,"views").get(0);
			List<Element> viewElement = getChildrenFromElement(viewsElement,"view");
			ArrayList<ViewWrapper> items = new ArrayList<ViewWrapper>();
			
			for(Element view : viewElement)
			{
				Element coords = getChildrenFromElement(view,"coords").get(0);
				
				double x1 = parseDouble(coords.getAttribute("x1"));
				double y1 = parseDouble(coords.getAttribute("y1"));
				
				if("image".equals(view.getAttribute("type")))
				{
					Element image = getChildrenFromElement(view,"image").get(0);
					int drawID = parseInt(image.getAttribute("imageID"));
					float scaleFactor = parseFloat(image.getAttribute("scaleFactor"));
					EtapaEnum etapa = EtapaEnum.valueOf(image.getAttribute("etapa"));
					items.add(new ViewWrapper(x1, y1, drawID,scaleFactor,etapa));
				}
				
			}
			dpw.setWrappers(items);
			
		mc.setDropPanel(dpw);
		try{
			return ActivityEnum.valueOf(last);
		}catch(Exception e)
		{
			return ActivityEnum.ERROR;
		}
	}
	
	static List<Element> getChildrenFromElement(Element e, String tag) {
	    List<Element> result = new LinkedList<Element>();
	    NodeList nl = e.getElementsByTagName(tag);
	    for (int i = 0; i < nl.getLength(); ++i) {
	        if (nl.item(i).getNodeType() == Node.ELEMENT_NODE)
	            result.add((Element) nl.item(i));
	    }
	    return result;
	}
	
	static int parseInt (String s)
	{
		int res = 0;
		try{ res = Integer.parseInt(s); } catch(Exception e) {}		
		return res;
	}
	
	static float parseFloat (String s)
	{
		float res = 0.0f;
		try{ res = Float.parseFloat(s); } catch(Exception e) {}		
		return res;
	}
	
	static double parseDouble (String s)
	{
		double res = 0.0;
		try{ res = Double.parseDouble(s); } catch(Exception e) {}		
		return res;
	}
	
	static boolean parseBool (String s)
	{
		return "true".equals(s);
	}
	
}
