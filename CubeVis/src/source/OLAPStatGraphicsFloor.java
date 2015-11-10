package source;

import java.awt.*;
import java.util.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.Text2D;
import javax.vecmath.*;
import java.util.ArrayList;


public class OLAPStatGraphicsFloor
{
  private final static int FLOOR_LEN = 20;  // should be even

  // colours for floor, etc
  private final static Color3f[] colors = {new Color3f(0.0f, 0.0f, 0.0f), new Color3f(0.0f, 0.5f, 1.0f), new Color3f(0.8f, 0.1f, 0.2f), new Color3f(0.0f, 0.5f, 0.1f), new Color3f(0.0f, 1.0f, 1.0f), new Color3f(1.0f, 1.0f, 1.0f) };
  private final static int numColors = 6;

  private BranchGroup floorBG;

	int numDims;
	int numRows, numCols;
	float border;
	float smallL;
	float smallW;
	float bigL;
	float bigW;
	float startX, startZ;
	
	Vector cellVect;
	
  public OLAPStatGraphicsFloor(int numD, int numR, int numC, float bord, float sL, float sW, float bL, float bW)
  {
	cellVect = new Vector ();
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
	
	System.out.println (smallL + "  " + smallW + "   " + bigL + "   " + bigW + "   " + startX + "   " + startZ);
	
	ArrayList coords = new ArrayList ();
    floorBG = new BranchGroup();

	for (int lp1 = 0; lp1 < numRows; lp1++)
	{
		for (int lp2 = 0; lp2 < numCols; lp2++)
		{
			OLAPStatGraphicsTiles OSGT = new OLAPStatGraphicsTiles(getBigCoords (lp1, lp2), colors[0], generateBinary (lp1, lp2));
			OSGT.setPickable (true);
			cellVect.addElement (OSGT);
			floorBG.addChild(OSGT);
			createSmallTiles (lp1, lp2);
		}
	}

//    labelAxes();
  }  // end of OLAPStatGraphicsFloor()
  
  private String generateBinary (int rows, int cols)
  {
		int num = cols * numRows + rows;
		String temp = Integer.toBinaryString (num);
		int len = temp.length ();

		for (int k = 0; k < numDims - len; k++)
		{
			temp = "0" + temp;
		}
		return temp;
	}
  
	private void createSmallTiles (int rows, int cols)
	{
		int tnum, cnt = 0;
		String binStr = generateBinary (rows, cols);
		
		for (int k = 0; k < binStr.length (); k++)
		{
			tnum = Integer.parseInt (binStr.substring (k, k+1)) + 1;
			if (tnum >= numColors) tnum = numColors - 1;
			OLAPStatGraphicsTiles OSGT = new OLAPStatGraphicsTiles (getSmallCoords (rows, cols, cnt), colors[tnum], binStr);
			OSGT.setPickable (false);
			floorBG.addChild ( OSGT );
			cnt++;
		}
	}
	
	private ArrayList getSmallCoords (int rows, int cols, int num)
	{
		ArrayList coords = new ArrayList ();
		Point3f p1 = new Point3f (startX + cols * bigL + border + num * smallL, 0.01f, startZ + rows * bigW + border);
		Point3f p2 = new Point3f (startX + cols * bigL + border + num * smallL + smallL, 0.01f, startZ + rows * bigW + border);
		Point3f p3 = new Point3f (startX + cols * bigL + border + num * smallL + smallL, 0.01f, startZ + rows * bigW + border + smallW);
		Point3f p4 = new Point3f (startX + cols * bigL + border + num * smallL, 0.01f, startZ + rows * bigW + border + smallW);

		coords.add(p1); coords.add(p2); 
		coords.add(p3); coords.add(p4);
		return coords;
	
	}
		
  
	private ArrayList getBigCoords (int rows, int cols)
	{
		ArrayList coords = new ArrayList ();
		Point3f p1 = new Point3f (startX + cols * bigL, 0.0f, startZ + rows * bigW);
		Point3f p2 = new Point3f (startX + cols * bigL + bigL, 0.0f, startZ + rows * bigW);
		Point3f p3 = new Point3f (startX + cols * bigL + bigL, 0.0f, startZ + rows * bigW + bigW);
		Point3f p4 = new Point3f (startX + cols * bigL, 0.0f, startZ + rows * bigW + bigW);

		coords.add(p1); coords.add(p2); 
		coords.add(p3); coords.add(p4);
		return coords;
	}


  private void labelAxes()
  // Place numbers along the X- and Z-axes at the integer positions
  {
    Vector3d pt = new Vector3d();
    for (int i=-FLOOR_LEN/2; i <= FLOOR_LEN/2; i++) {
      pt.x = i;
      floorBG.addChild( makeText(pt,""+i) );   // along x-axis
    }

    pt.x = 0;
    for (int i=-FLOOR_LEN/2; i <= FLOOR_LEN/2; i++) {
      pt.z = i;
      floorBG.addChild( makeText(pt,""+i) );   // along z-axis
    }
  }  // end of labelAxes()


  private TransformGroup makeText(Vector3d vertex, String text)
  // Create a Text2D object at the specified vertex
  {
    Text2D message = new Text2D(text, colors[1], "SansSerif", 36, Font.BOLD );
       // 36 point bold Sans Serif

    TransformGroup tg = new TransformGroup();
    Transform3D t3d = new Transform3D();
    t3d.setTranslation(vertex);
    tg.setTransform(t3d);
    tg.addChild(message);
    return tg;
  } // end of getTG()


  public BranchGroup getBG()
  {  return floorBG;  }
  
  public Vector getCellVector ()
  { return cellVect; }


}  // end of OLAPStatGraphicsFloor class

