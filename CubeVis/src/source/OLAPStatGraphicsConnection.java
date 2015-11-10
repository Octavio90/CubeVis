package source;
import java.awt.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.Text2D;
import javax.vecmath.*;
import java.util.*;


public class OLAPStatGraphicsConnection extends Shape3D
{
  private String myBinStr1;
  private String myBinStr2;
  private String measuresStr;
  
  private BranchGroup connectionBG;

  float bigW, bigL, startX, startZ;
  int numRows, size;

  public OLAPStatGraphicsConnection(String m, Vector s, Vector f, int sze, float bW, float bL, float sX, float sZ, int nR) 
  {
	bigW = bW;
	bigL = bL;
	startX = sX;
	startZ = sZ;
	myBinStr1 = "";
	myBinStr2 = "";
	measuresStr = m;
	numRows = nR;
	size = sze;
    for (int i = 0; i < s.size (); i++)
	{
		myBinStr1 += (String)s.elementAt (i);
		myBinStr2 += (String)f.elementAt (i);
	}
	connectionBG = new BranchGroup ();
	drawConnection (s,f);
  }    

  private void drawConnection (Vector startDims, Vector endDims)
  {
	System.out.println ("DRAWING BETWEEN " + startDims + " AND " + endDims);
	
	long num = 0;
	long place = 1;
	int row, col;

        //EDGAR NOTA: El ciclo lo recorre alreves por lo que intercambia las dimensiones
	/*
	for (int i = startDims.size ()-1; i>=0; i--)
	{
		num += place * Integer.parseInt ((String)startDims.elementAt(i));
		place *= 2;
	}*/
        for (int i = 0; i < startDims.size(); i++)
	{
		num += place * Integer.parseInt ((String)startDims.elementAt(i));
		place *= 2;
        }

	col = (int) (num / numRows);
	row = (int) (num % numRows);
	
	Point3f start = new Point3f ((float)(startX + (col+0.5) * bigL), 0.1f, (float)(startZ + (row+0.5) * bigW));
	
	System.out.println ("Start:  (" + row + ", " + col + ") with num=" + num + " and point=" + start);
	
	num = 0;
	place = 1;
	/*for (int i = endDims.size ()-1; i>=0; i--)
	{
		num += place * Integer.parseInt ((String)endDims.elementAt (i));
		place *= 2;
	}*/

        for (int i = 0; i < startDims.size(); i++)
	{
		num += place * Integer.parseInt ((String)endDims.elementAt (i));
		place *= 2;
	}
	
	col = (int) (num / numRows);
	row = (int) (num % numRows);

	Point3f end = new Point3f ((float)(startX + (col+0.5) * bigL), 0.1f, (float)(startZ + (row+0.5) * bigW));

	System.out.println ("End:  (" + row + ", " + col + ") with num=" + num + " and point=" + end);

 	Color3f blackColor = new Color3f(0.0f, 0.0f, 0.0f);
	Color3f greenColor = new Color3f (0.0f, 1.0f, 0.0f);
	Color3f specular = new Color3f(0.9f, 0.9f, 0.9f);
	
	
	float radius = (Math.abs (start.distance (end))) / 2;
	Point3f center = new Point3f (start.x + (end.x - start.x)/2, 0.01f, start.z + (end.z - start.z)/2);
	
	LineArray myLines = new LineArray (4, GeometryArray.COORDINATES);
	
	myLines.setCoordinate (0, start);
	myLines.setCoordinate (1, new Point3f (center.x, radius, center.z));
        myLines.setCoordinate (2, new Point3f (center.x, radius, center.z));
	myLines.setCoordinate (3, end);
	//EDGAR
    Appearance myAppearance = new Appearance();
    myAppearance.setLineAttributes (new LineAttributes (size, LineAttributes.PATTERN_SOLID, false));
	myAppearance.setColoringAttributes (new ColoringAttributes (greenColor, ColoringAttributes.FASTEST));

	addGeometry (myLines);
	setAppearance (myAppearance);

  }

  public String getCell1 ()
  {	return myBinStr1;  }
  
  public String getCell2 ()
  { return myBinStr2;  }
  
  public String getMeasures ()
  { return measuresStr; }

  public BranchGroup getBG()
  {  return connectionBG;  }

} // end of OLAPStatGraphicsConnection class
