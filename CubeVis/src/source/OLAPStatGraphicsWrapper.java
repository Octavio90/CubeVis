package source;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import javax.swing.*;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.behaviors.vp.*;
import javax.media.j3d.Group.*;
import javax.vecmath.Color3f;
import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import java.text.DecimalFormat;

import com.sun.j3d.utils.picking.*;

// import com.tornadolabs.j3dtree.*;    // for displaying the scene graph


public class OLAPStatGraphicsWrapper extends JPanel implements MouseListener, MouseMotionListener
// Holds the 3D canvas where the loaded image is displayed
{

  java.util.Date startTime, finishTime, tempTime;
  
  private PickCanvas pickCanvas;

  private static final int PWIDTH = 500;   // size of panel
  private static final int PHEIGHT = 500; 

  private static int BOUNDSIZE = 1000;  // larger than world

  private static Point3d USERPOSN;
    // initial user position

  private SimpleUniverse su;
  private BranchGroup sceneBG;
  private BoundingSphere bounds;   // for environment nodes

  // private Java3dTree j3dTree;   // frame to hold tree display

	int numDims;
	int numRows, numCols;
	float border;
	float smallL;
	float smallW;
	float bigL;
	float bigW;
	
	float startX, startZ;
	
	DBSQLConnect DBC;
	DBSQLStatement DBS;
	Vector MeasuresVect;
        //EDGAR:VIDEO
        //Vector MeasuresZVect;
	Vector DimensionsVect;
	Vector ImagesVect;
	String m_TableName;
	OLAPStatGraphicsHeartWrap heart1, heart2;
	OLAPStatGraphicsTiles pickedTile1, pickedTile2;
	OLAPStatGraphics OSG;
	OLAPStatGraphicsFloor OGF;
	
	static String binStr1;
	static String binStr2;
	static String measuresStr;
        //VIDEO
        static String measuresZStr;
	
	Vector storeAllHeartData1, storeAllHeartData2;
	int storeAllHeartDataPlace1, storeAllHeartDataPlace2;

  public OLAPStatGraphicsWrapper(DBSQLConnect dbConn, DBSQLStatement dbStat, Vector DVector, Vector MVector, Vector IVector, String tblname, int numD, int numR, int numC, float bord, float sL, float sW, float bL, float bW, OLAPStatGraphics o1, OLAPStatGraphicsHeartWrap h1, OLAPStatGraphicsHeartWrap h2)
  {
    binStr1 = "";
	binStr2 = "";
	measuresStr = "";
        measuresZStr = "";
	DBC = dbConn;
	DBS = dbStat;
	DimensionsVect = DVector;
	MeasuresVect = MVector;
	ImagesVect = IVector;
	m_TableName = tblname;
	OSG = o1;
	heart1 = h1;
	heart2 = h2;
	
	numDims = numD;
	numRows = numR;
	numCols = numC;
	border = bord;
	smallL = sL;
	smallW = sW;
	bigL = bL;
	bigW = bW;
	
	startX = -(numCols * bigL / 2);
	startZ = -(numRows * bigW / 2);
	
	pickedTile1 = null;
	pickedTile2 = null;
	
	System.out.println (smallL + "  " + smallW + "   " + bigL + "   " + bigW + "   " + startX + "   " + startZ);
	
    setLayout( new BorderLayout() );
    setOpaque( false );
    setPreferredSize( new Dimension(PWIDTH, PHEIGHT));

    GraphicsConfiguration config =
					SimpleUniverse.getPreferredConfiguration();
    Canvas3D canvas3D = new Canvas3D(config);
    add("Center", canvas3D);
    canvas3D.setFocusable(true);     // give focus to the canvas 
    canvas3D.requestFocus();

    su = new SimpleUniverse(canvas3D);

    // j3dTree = new Java3dTree();   // create a display tree for the SG

    createSceneGraph();
    initUserPosition();        // set user's viewpoint
    orbitControls(canvas3D);   // controls for moving the viewpoint
    
    su.addBranchGraph( sceneBG );

	pickCanvas = new PickCanvas (canvas3D, sceneBG);
	pickCanvas.setMode (pickCanvas.BOUNDS);
	
	canvas3D.addMouseListener (this);
	canvas3D.addMouseMotionListener (this);
	
	// j3dTree.updateNodes( su );    // build the tree display window

  } // end of OLAPStatGraphicsWrapper()
  
  public void getHeartData (String binStr, int heart)
  {
	if (binStr.length () <= 0) return;
	
	int h;
	if (heart == 1)
		h = OLAPStatGraphics.heart1Display;
	else
		h = OLAPStatGraphics.heart2Display;
		
	switch (h)
	{
		case 1:
			getAverageHeartData (binStr, heart);
			break;
		case 2:
			getSampleHeartData (binStr, heart);
			break;
		case 3:
			getAllHeartData (binStr, heart);
			break;
	}  
  }
  
  private void getAverageHeartData (String binStr, int heart)
  {
	startTime = new java.util.Date ();
	
	System.out.println ("GETTING AVERAGE HEART DATA");
		String[] names = new String [ImagesVect.size ()];
		String[] results = new String [ImagesVect.size ()];
		int len = binStr.length ();
		
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		
		Columns.addElement ("*");
		TableName.addElement (OLAPStatTest.MSVTableName);

                //EDGAR NOTA: Obtiene la última dimension
		for (int i = 0; i < DimensionsVect.size (); i++)
			//Predicate.addElement ((String)DimensionsVect.elementAt (i) + "=" + binStr.substring (len-1-i, len-1-i+1));
                        Predicate.addElement ((String)DimensionsVect.elementAt (i) + "=" + binStr.substring (i, i+1));
		
		String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		
		DBC.sendQuery (sqlStat);
//		System.out.println (sqlStat);
		
		for (int i = 0; i < ImagesVect.size (); i++)
			names[i] = "MEAN_" + (String)ImagesVect.elementAt (i);

		results = DBC.getResult (names);
//		System.out.println (sqlStat);
		if (results.length <= 0)
		{
			System.out.println ("No Results Retrieved");
			return;
		}
		
		for (int i = 0; i < names.length; i++)
		{
//			System.out.println (names[i] + " is " + results[i]);
			if (heart == 1)
				heart1.changeColor (0, names[i].substring (5), getHeartRegionColor (Double.parseDouble (results[i])));
			else if (heart == 2)
				heart2.changeColor (0, names[i].substring (5), getHeartRegionColor (Double.parseDouble (results[i])));
		}	

		finishTime = new java.util.Date ();
		long l1 = startTime.getTime();
		long l2 = finishTime.getTime();
		long difference = l2 - l1;		
		System.out.println ("Time Difference: " + difference + " milliseconds");
  }
  
  private void getSampleHeartData (String binStr, int heart)
  {
	startTime = new java.util.Date ();
	System.out.println ("GETTING SAMPLE HEART DATA");
		String[] names = new String [9];
		String[] results = new String [9];
		int len = binStr.length ();
		
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		
		Columns.addElement ("*");
		TableName.addElement (m_TableName);
		
		for (int i = 0; i < DimensionsVect.size (); i++)
			Predicate.addElement ((String)DimensionsVect.elementAt (i) + "=" + binStr.substring (len-1-i, len-1-i+1));
		
		String sqlStat = DBS.dbSample (Columns, TableName, Predicate, 1);
		
		DBC.sendQuery (sqlStat);
//		System.out.println (sqlStat);
		
		for (int i = 0; i < ImagesVect.size (); i++)
			names[i] = (String)ImagesVect.elementAt (i);

		results = DBC.getResult (names);

		if (results.length <= 0)
		{
			System.out.println ("No Results Retrieved");
			return;
		}
		
		for (int i = 0; i < names.length; i++)
		{
//			System.out.println (names[i] + " is " + results[i]);
			if (heart == 1)
				heart1.changeColor (0, names[i], getHeartRegionColor (Double.parseDouble (results[i])));
			else if (heart == 2)
				heart2.changeColor (0, names[i], getHeartRegionColor (Double.parseDouble (results[i])));
		}	
		
		finishTime = new java.util.Date ();
		long l1 = startTime.getTime();
		long l2 = finishTime.getTime();
		long difference = l2 - l1;		
		System.out.println ("Time Difference: " + difference + " milliseconds");
	}
	
  public void showThumbnails (String binStr, int heart)
  {
	startTime = new java.util.Date ();
	
	System.out.println ("GETTING Thumbnails");
		Vector appVect = new Vector ();

		String[] names = new String [9];
		String[] results = new String [9];
		int len = binStr.length ();
		
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		
		Columns.addElement ("*");
		TableName.addElement (m_TableName);
		
		for (int i = 0; i < DimensionsVect.size (); i++)
			Predicate.addElement ((String)DimensionsVect.elementAt (i) + "=" + binStr.substring (len-1-i, len-1-i+1));
		
		String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		
		DBC.sendQuery (sqlStat);
		
		for (int i = 0; i < ImagesVect.size (); i++)
			names[i] = (String)ImagesVect.elementAt (i);

		results = DBC.getResult (names);
		
		Color3f color;
		Color3f white = new Color3f (0.9f, 0.9f, 0.9f);
		Color3f black = new Color3f (0.0f, 0.0f, 0.0f);
//		Material mat;
		for (int i = 0; i < ImagesVect.size (); i++)
			names[i] = (String)ImagesVect.elementAt (i);
				
				
//			System.out.println ("Putting in Colors");
		while (results.length > 0)
		{
			Appearance[] app = new Appearance[9];
			for (int i = 0; i < 9; i++)
			{
				color = getHeartRegionColor (Double.parseDouble (results[i]));
				Material mat = new Material(color, black, color, white, 25.0f);
				mat.setLightingEnable (true);
				app[i] = new Appearance ();
				app[i].setCapability (Appearance.ALLOW_MATERIAL_WRITE);
				app[i].setMaterial (mat);
			}
			appVect.addElement (app);
			results = DBC.getResult (names);
		}
		
		if (heart == 1)
			heart1.createThumbnails (appVect);
		else
			heart2.createThumbnails (appVect);
	
		finishTime = new java.util.Date ();
		long l1 = startTime.getTime();
		long l2 = finishTime.getTime();
		long difference = l2 - l1;		
		System.out.println ("Time Difference: " + difference + " milliseconds");
	}

	
  private void getAllHeartData (String binStr, int heart)
  {
	startTime = new java.util.Date ();
	System.out.println ("GETTING ALL HEART DATA");
		if (heart == 1)
		{
			storeAllHeartData1 = new Vector ();
			storeAllHeartDataPlace1 = -1;
		}
		else
		{
			storeAllHeartData2 = new Vector ();
			storeAllHeartDataPlace2 = -1;
		}

		String[] names = new String [9];
		String[] results = new String [9];
		int len = binStr.length ();
		
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		
		Columns.addElement ("*");
		TableName.addElement (m_TableName);
		
		for (int i = 0; i < DimensionsVect.size (); i++)
			Predicate.addElement ((String)DimensionsVect.elementAt (i) + "=" + binStr.substring (len-1-i, len-1-i+1));
		
		String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		
		DBC.sendQuery (sqlStat);
//		System.out.println (sqlStat);
		
		for (int i = 0; i < ImagesVect.size (); i++)
			names[i] = (String)ImagesVect.elementAt (i);

		results = DBC.getResult (names);
		
		while (results.length > 0)
		{
			if (heart == 1)
				storeAllHeartData1.addElement (results);
			else
				storeAllHeartData2.addElement (results);
			results = DBC.getResult (names);
		}

		finishTime = new java.util.Date ();
		long l1 = startTime.getTime();
		long l2 = finishTime.getTime();
		long difference = l2 - l1;		
		System.out.println ("Time Difference: " + difference + " milliseconds");
		
		nextAllHeartImg (heart);
	}
	
	public void nextAllHeartImg (int heart)
	{
		if (heart == 1)
		{
			if (storeAllHeartData1 == null) return;
			storeAllHeartDataPlace1++;
			if (storeAllHeartDataPlace1 == storeAllHeartData1.size ())
				storeAllHeartDataPlace1 = 0;
			System.out.println ("1 - " + storeAllHeartDataPlace1);

				
			String[] res = (String[])storeAllHeartData1.elementAt (storeAllHeartDataPlace1);
	
			String[] names = new String [9];
			for (int i = 0; i < ImagesVect.size (); i++)
				names[i] = (String)ImagesVect.elementAt (i);
		
			for (int i = 0; i < names.length; i++)
			{
//				System.out.println (names[i] + " is " + results[i]);
				heart1.changeColor (0, names[i], getHeartRegionColor (Double.parseDouble (res[i])));
			}	
		}
		else
		{
			if (storeAllHeartData2 == null) return;
			storeAllHeartDataPlace2++;
			if (storeAllHeartDataPlace2 == storeAllHeartData2.size ())
				storeAllHeartDataPlace2 = 0;
			System.out.println ("2 - " + storeAllHeartDataPlace2);

			String[] res = (String[])storeAllHeartData2.elementAt (storeAllHeartDataPlace2);
	
			String[] names = new String [9];
			for (int i = 0; i < ImagesVect.size (); i++)
				names[i] = (String)ImagesVect.elementAt (i);
		
			for (int i = 0; i < names.length; i++)
			{
//				System.out.println (names[i] + " is " + results[i]);
				heart2.changeColor (0, names[i], getHeartRegionColor (Double.parseDouble (res[i])));
			}	
		}	
	}
	
	public void prevAllHeartImg (int heart)
	{
		if (heart == 1)
		{
			if (storeAllHeartData1 == null) return;
			storeAllHeartDataPlace1--;
			if (storeAllHeartDataPlace1 == -1)
				storeAllHeartDataPlace1 = storeAllHeartData1.size() - 1;
			System.out.println ("1 - " + storeAllHeartDataPlace1);
				
			String[] res = (String[])storeAllHeartData1.elementAt(storeAllHeartDataPlace1);
	
			String[] names = new String [9];
			for (int i = 0; i < ImagesVect.size (); i++)
				names[i] = (String)ImagesVect.elementAt (i);
		
			for (int i = 0; i < names.length; i++)
			{
//				System.out.println (names[i] + " is " + results[i]);
				heart1.changeColor (0, names[i], getHeartRegionColor (Double.parseDouble (res[i])));
			}	
		}
		else
		{
			if (storeAllHeartData2 == null) return;
			storeAllHeartDataPlace2--;
			if (storeAllHeartDataPlace2 == -1)
				storeAllHeartDataPlace2 = storeAllHeartData2.size() - 1;
			System.out.println ("2 - " + storeAllHeartDataPlace2);
				
			String[] res = (String[])storeAllHeartData2.elementAt(storeAllHeartDataPlace2);
	
			String[] names = new String [9];
			for (int i = 0; i < ImagesVect.size (); i++)
				names[i] = (String)ImagesVect.elementAt (i);
		
			for (int i = 0; i < names.length; i++)
			{
//				System.out.println (names[i] + " is " + results[i]);
				heart2.changeColor (0, names[i], getHeartRegionColor (Double.parseDouble (res[i])));
			}	
		}	
	
	}

  private Color3f getHeartRegionColor (double value)
  {
		int val = (int)(((value + 1.0) / 2.0) * 64);
		
		float red = (float)OLAPStatGraphics.regionColors[val][0];
		float green = (float)OLAPStatGraphics.regionColors[val][1];
		float blue = (float)OLAPStatGraphics.regionColors[val][2];
		
//		System.out.println ("Color is: (" + red + ", " + green + ", " + blue + ")");

		return (new Color3f (red,green,blue));
  }
  
  private void clearHeartData ()
  {
	String[] names = new String[9];
	
 		names[0] = "MEAN_AP";
		names[1] = "MEAN_AL";
		names[2] = "MEAN_AS_";
		names[3] = "MEAN_SA";
		names[4] = "MEAN_SI";
		names[5] = "MEAN_IS_";
		names[6] = "MEAN_IL";
		names[7] = "MEAN_LI";
		names[8] = "MEAN_LA";
		
		for (int i = 0; i < 9; i++)
		{
			heart1.changeColor (0, names[i].substring (5), new Color3f (1.0f, 1.0f, 1.0f));
			OSG.setLabel ("heart1", "None");
			heart2.changeColor (0, names[i].substring (5), new Color3f (1.0f, 1.0f, 1.0f));
			OSG.setLabel ("heart2", "None");
		}		
  }
  
  private void clearHighlightCell ()
  {
 	if (pickedTile1 != null)
		pickedTile1.setColor (new Color3f (0.0f, 0.0f, 0.0f));
	if (pickedTile2 != null)
		pickedTile2.setColor (new Color3f (0.0f, 0.0f, 0.0f));
}
  
  private void highLightCell (String bin, int tileNum, boolean findCell)
  {

	if (findCell)
	{
		long num = 0;
		long place = 1;
		int row, col;

                //EDGAR NOTA: recorrido del vector alreves
		//for (int i = bin.length()-1; i>=0; i--)
                for(int i = 0; i < bin.length(); i++)
		{
			num += place * Integer.parseInt (bin.substring (i, i+1));
			place *= 2;
                        System.out.println(num);
		}
                
		col = (int) (num / numRows);
		row = (int) (num % numRows);

        	//int cellNum = (row) * numRows + col; NOTA: Contaba demás
                
                int cellNum = (row) * numCols + col;
                
		if (tileNum == 1)
			pickedTile1 = (OLAPStatGraphicsTiles)OGF.getCellVector ().elementAt (cellNum);
		else
			pickedTile2 = (OLAPStatGraphicsTiles)OGF.getCellVector ().elementAt (cellNum);
	}
	
	if (tileNum == 1)
		pickedTile1.setColor (new Color3f (0.0f, 1.0f, 1.0f));
	else
		pickedTile2.setColor (new Color3f (0.0f, 1.0f, 1.0f));
  }

 public void mouseClicked(MouseEvent e)
{
    pickCanvas.setShapeLocation(e);
    PickResult result = pickCanvas.pickClosest();
    if (result == null) {
       System.out.println("Nothing picked");
    } else {
			clearHeartData ();
			clearHighlightCell ();
		try {
			pickedTile1 = (OLAPStatGraphicsTiles)result.getObject ();
			binStr1 = pickedTile1.getString ();
			binStr2 = "";
			getHeartData (binStr1, 1);
			highLightCell (binStr1, 1, false);
			changeLabels (binStr1, binStr2, 1);
		} catch (Exception exp) {
		    OLAPStatGraphicsConnection OSGT = (OLAPStatGraphicsConnection)result.getObject ();
			binStr1 = OSGT.getCell1 ();
			binStr2 = OSGT.getCell2 ();
			measuresStr = OSGT.getMeasures ();
			getHeartData (binStr1, 1);
			getHeartData (binStr2, 2);
			changeLabels (binStr1, binStr2, 2);
			highLightCell (binStr1, 1, true);
			highLightCell (binStr2, 2, true);
		}
    }
}

public void mouseExited (MouseEvent e) {}
public void mouseEntered (MouseEvent e) {}
public void mouseReleased (MouseEvent e) { }
public void mousePressed (MouseEvent e) { }
public void mouseDragged (MouseEvent e) { }

//protected void processMouseMotionEvent (MouseEvent e) { OSG.setDimLabel ("Mouse at: " + e.getX () + ", " + e.getY()); }
public void mouseMoved (MouseEvent e)
{
    pickCanvas.setShapeLocation(e);
    PickResult result = pickCanvas.pickClosest();
    if (result == null) {
		OSG.setLabel ("dim", " ");
    } else {
		try {
			OLAPStatGraphicsTiles tt = (OLAPStatGraphicsTiles)result.getObject ();
			String binStr = tt.getString ();
			OSG.setLabel ("dim", getSingleDesc (binStr));
		} catch (Exception exp) {
		    OLAPStatGraphicsConnection OSGT = (OLAPStatGraphicsConnection)result.getObject ();
			String binStr1 = OSGT.getCell1 ();
			String binStr2 = OSGT.getCell2 ();
			String measuresStr = OSGT.getMeasures ();
			OSG.setLabel ("dim", getDoubleDesc (binStr1, binStr2, measuresStr));
		}
    }
}

private void changeLabels (String str1, String str2, int type)
{
	if (type == 1)
	{
		OSG.setLabel ("dimShown", " ");
		OSG.setLabel ("heart1", getSingleDesc (str1));
		OSG.setLabel ("heart2", " ");
	}
	else
	{
		String s = "SHOWN: ";
		String[] ss = new String[2];
		OSG.setLabel ("dimShown", s + getSimilarDesc (str1, str2) + " Signif: " + measuresStr + " z=" + OLAPStatGraphicsWrapper.measuresZStr );
		getDifferentDesc (str1, str2, ss);
		OSG.setLabel ("heart1", ss[0]);
		OSG.setLabel ("heart2", ss[1]);	
	}
}

private String getSimilarDesc (String str1, String str2)
{
	String retStr = "";
	int len = str1.length ();
	int len2 = str2.length ();
	char c1, c2;
	if (len2 == 0) return " ";
	
	boolean flag = true;
        //EDGAR NOTA: recorrido del vector alreves
	//for (int i = DimensionsVect.size () - 1; i >= 0; i--)
        for (int i = 0; i < DimensionsVect.size(); i++)
	{
		//c1 = str1.charAt (len-1-i);
		//c2 = str2.charAt (len-1-i);
                c1 = str1.charAt (i);
		c2 = str2.charAt (i);
		if (c1 != c2) continue;
		if (flag)
		{
			retStr += (String)DimensionsVect.elementAt (i) + "=" + c1;
			flag = false;
		}
		else
			retStr += " | " + (String)DimensionsVect.elementAt (i) + "=" + c1;
	}
	return retStr;	
}

private void getDifferentDesc (String str1, String str2, String[] str)
{
	str[0] = "";
	str[1] = "";
	int len = str1.length ();
	int len2 = str2.length ();
	char c1;
	char c2 = ' ';
	boolean flag = true;
        //EDGAR NOTA: recorrido del vector alreves
        //for (int i = DimensionsVect.size () - 1; i >= 0; i--)
        for (int i = 0; i < DimensionsVect.size(); i++)
	{
		//c1 = str1.charAt (len-1-i);
                c1 = str1.charAt (i);
		if (len2 > 0)
			//c2 = str2.charAt (len-1-i);
                        c2 = str2.charAt (i);
		if (c1 == c2) continue;
		if (flag)
		{
			str[0] += (String)DimensionsVect.elementAt (i) + "=" + c1;
			str[1] += (String)DimensionsVect.elementAt (i) + "=" + c2;
			flag = false;
		}
		else
		{
			str[0] += " | " + (String)DimensionsVect.elementAt (i) + "=" + c1;
			str[1] += " | " + (String)DimensionsVect.elementAt (i) + "=" + c2;
		}
	}
}

private String getSingleDesc (String binStr)
{
	String str = "";
	int len = binStr.length ();
	boolean flag = true;
	for (int i = DimensionsVect.size () - 1; i >=0; i--)
		if (flag)
		{
			str += (String)DimensionsVect.elementAt (i) + "=" + binStr.charAt (len-1-i);
			flag = false;
		}
		else
			str += " | " + (String)DimensionsVect.elementAt (i) + "=" + binStr.charAt (len-1-i);
	return str;
}

private String getDoubleDesc (String binStr1, String binStr2, String measuresStr)
{
	String str = "", predicate="";
	int len = binStr1.length ();
	boolean flag = true;
	String c1, c2;
	//EDGAR NOTA: La lectura del arreglo esta alrevés
        //for (int i = DimensionsVect.size () - 1; i >=0; i--)
        for (int i = 0; i < DimensionsVect.size(); i++)
	{
		c1 = binStr1.substring (i,i+1);
		c2 = binStr2.substring (i,i+1);

                //VIDEO
                predicate += ((String)DimensionsVect.elementAt(i)).toLowerCase () + "1 = " + c1 + " AND " +
                             ((String)DimensionsVect.elementAt(i)).toLowerCase () + "2 = " + c2;

                if(i + 1 != DimensionsVect.size()){
                    predicate += " AND ";
                }
		
		if (flag)
		{
			flag = false;
			if (c1.equals (c2))
				str += ((String)DimensionsVect.elementAt(i)).toLowerCase () + "=" + c1;
			else
				str += ((String)DimensionsVect.elementAt(i)).toUpperCase () + "=0/1";
		}
		else
		{
			if (c1.equals (c2))
				str += " | " + ((String)DimensionsVect.elementAt(i)).toLowerCase () + "=" + c1;
			else
				str += " | " + ((String)DimensionsVect.elementAt(i)).toUpperCase () + "=0/1";
		}
	}
        
	str = str + " SIGNIF: " + measuresStr + " z=" + getMeasurez(measuresStr,predicate);
	return str;
}

  
  private void createSceneGraph() 
  // initilise the scene
  { 

  sceneBG = new BranchGroup();
	
	if ((numCols * bigL) > (numRows * bigW))
		BOUNDSIZE = (int)(numCols * bigL * 5);
	else
		BOUNDSIZE = (int)(numCols * bigW * 5);
	
    bounds = new BoundingSphere(new Point3d(0,0,0), BOUNDSIZE);   

    lightScene();         // add the lights
    addBackground();      // add the sky
	OGF = new OLAPStatGraphicsFloor (numDims, numRows, numCols, border, smallL, smallW, bigL, bigW);
    sceneBG.addChild( OGF.getBG() );  // add the floor
	
	createSigDiffs ();
	
	
	sceneBG.compile();   // fix the scene
  } // end of createSceneGraph()

  private void createSigDiffs ()
  {
		String[] names = new String [MeasuresVect.size () + 2*DimensionsVect.size ()];
                //String[] names = new String [MeasuresVect.size () * 2 + 2*DimensionsVect.size ()];
		String[] results = new String [MeasuresVect.size () + 2*DimensionsVect.size ()];
		Vector start = new Vector ();
		Vector finish = new Vector ();
		
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		
		Columns.addElement ("*");
		TableName.addElement (m_TableName + OLAPStatTest.finalTableText);
		
		String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		
		DBC.sendQuery (sqlStat);
		
		int cnt = 0;
		for (int i = 0; i < MeasuresVect.size (); i++)
		{
			//names[cnt] = "Category_" + (String)MeasuresVect.elementAt (i);
                        // EDGAR NOTA: No hay campos con Category_...
                        names[cnt] = "PValue_" + (String)MeasuresVect.elementAt (i);
                        //EDGAR:VIDEO
                        //cnt++;
                        //names2[cnt] = "ZTest_" + (String)MeasuresVect.elementAt (i);
			cnt++;
		}
		for (int i = 0; i < DimensionsVect.size (); i++)
		{
			names[cnt] = (String)DimensionsVect.elementAt(i) + "1";
			cnt++;
		}
		for (int i = 0; i < DimensionsVect.size (); i++)
		{
			names[cnt] = (String)DimensionsVect.elementAt(i) + "2";
			cnt++;
		}

		results = DBC.getResult (names);
		cnt = 0;
		String mstr = "";

		System.out.println ("RESULTS for " + m_TableName + OLAPStatTest.finalTableText + ":");
		while (results.length > 0)
		{
			cnt = 0;
			mstr = "";
			boolean flag = true;
			for (int i = 0; i < MeasuresVect.size (); i++)
				if (Integer.parseInt (results[i]) >= 3)
				{
					if (flag)
					{
						flag = false;
						mstr += (String)MeasuresVect.elementAt (i);
					}
					else
						mstr += ", " + (String)MeasuresVect.elementAt (i);
					cnt++;
				}
				
			if (cnt > 0)
			{
					int p1 = MeasuresVect.size ();
					int p2 = MeasuresVect.size () + DimensionsVect.size ();
					start.clear ();
					finish.clear ();
					for (int j = 0; j < DimensionsVect.size (); j++)
					{
						start.addElement (results[p1+j]);
						finish.addElement (results[p2+j]);
					}
					sceneBG.addChild (new OLAPStatGraphicsConnection (mstr, start, finish, (int)(((float)cnt / (float)DimensionsVect.size ()) * 2.0)+5, bigW, bigL, startX, startZ, numRows));
			}
			results = DBC.getResult (names);
		}
		System.out.println ("END OF RESULTS PRIMERO");
		
  }

  private void lightScene()
  /* One ambient light, 2 directional lights */
  {
    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

    // Set up the ambient light
    AmbientLight ambientLightNode = new AmbientLight(white);
    ambientLightNode.setInfluencingBounds(bounds);
    sceneBG.addChild(ambientLightNode);

    // Set up the directional lights
    Vector3f light1Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);
       // left, down, backwards 
    Vector3f light2Direction  = new Vector3f(1.0f, -1.0f, 1.0f);
       // right, down, forwards

    DirectionalLight light1 = 
            new DirectionalLight(white, light1Direction);
    light1.setInfluencingBounds(bounds);
    sceneBG.addChild(light1);

    DirectionalLight light2 = 
        new DirectionalLight(white, light2Direction);
    light2.setInfluencingBounds(bounds);
    sceneBG.addChild(light2);
  }  // end of lightScene()



  private void addBackground()
  // A blue sky
  { Background back = new Background();
    back.setApplicationBounds( bounds );
//    back.setColor(0.17f, 0.65f, 0.92f);    // sky colour
	back.setColor (1.0f, 1.0f, 1.0f);
    sceneBG.addChild( back );
  }  // end of addBackground()



  private void orbitControls(Canvas3D c)
  /* OrbitBehaviour allows the user to rotate around the scene, and to
     zoom in and out.  */
  {
    OrbitBehavior orbit = 
		new OrbitBehavior(c, OrbitBehavior.REVERSE_ALL);
    orbit.setSchedulingBounds(bounds);

    ViewingPlatform vp = su.getViewingPlatform();
    vp.setViewPlatformBehavior(orbit);	    
  }  // end of orbitControls()



  private void initUserPosition()
  // Set the user's initial viewpoint using lookAt()
  {
    ViewingPlatform vp = su.getViewingPlatform();
    TransformGroup steerTG = vp.getViewPlatformTransform();

    Transform3D t3d = new Transform3D();
    steerTG.getTransform(t3d);

    // args are: viewer posn, where looking, up direction
//	USERPOSN = new Point3d(0, 5, 20);
	if ((numCols * bigL) > (numRows * bigW))
		USERPOSN = new Point3d(0, numCols * bigL * 1.5, 0.001);
	else
		USERPOSN = new Point3d(0, numRows * bigW * 1.5, 0.001);
		
    t3d.lookAt( USERPOSN, new Point3d(0,0,0), new Vector3d(0,1,0));
    t3d.invert();

    steerTG.setTransform(t3d);
  }  // end of initUserPosition()

  //VIDEO
  
  private String getMeasurez(String measuresStr, String predicate){
        Vector Columns = new Vector ();
	Vector TableName = new Vector ();
	Vector Predicate = new Vector ();
	Vector OrderBy = new Vector ();
	Vector GroupBy = new Vector ();

        String[] results = new String [1];
        String[] names = new String [1];

        Columns.addElement ("Ztest_" + measuresStr);
	TableName.addElement ("zmed655_Final");
        Predicate.addElement (predicate);

        String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
        DBC.sendQuery (sqlStat);

        names[0]="ZTest_" + measuresStr;
        results = DBC.getResult (names);

        DecimalFormat df = new DecimalFormat("0.000");
        float Zabs = Math.abs(Float.valueOf(results[0]));

        this.measuresZStr = df.format(Zabs);

        return  df.format(Zabs);

  }

} // end of OLAPStatGraphicsWrapper class