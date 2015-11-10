package source;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.ArrayList;


public class OLAPStatGraphicsTiles extends Shape3D 
{
  private QuadArray plane;
  private String myBinStr;
  int numPoints;


  public OLAPStatGraphicsTiles(ArrayList coords, Color3f col, String binStr) 
  {
	myBinStr = binStr;
    plane = new QuadArray(coords.size(), 
				GeometryArray.COORDINATES | GeometryArray.COLOR_3 );
    createGeometry(coords, col);
    createAppearance();
  }    


  private void createGeometry(ArrayList coords, Color3f col)
  { 
    numPoints = coords.size();

    Point3f[] points = new Point3f[numPoints];
    coords.toArray( points );
    plane.setCoordinates(0, points);

    Color3f cols[] = new Color3f[numPoints];
    for(int i=0; i < numPoints; i++)
      cols[i] = col;
    plane.setColors(0, cols);
	plane.setCapability (QuadArray.ALLOW_COLOR_WRITE);

    setGeometry(plane);
  }  // end of createGeometry()

  public void setColor (Color3f col)
  {
    Color3f cols[] = new Color3f[numPoints];
    for(int i=0; i < numPoints; i++)
      cols[i] = col;
    plane.setColors(0, cols);
  }
  
  public String getString ()
  {
	return myBinStr;
  }

  private void createAppearance()
  {
    Appearance app = new Appearance();

    PolygonAttributes pa = new PolygonAttributes();
    pa.setCullFace(PolygonAttributes.CULL_NONE);   
      // so can see the OLAPStatGraphicsTiles from both sides
    app.setPolygonAttributes(pa);

    setAppearance(app);
  }  // end of createAppearance()


} // end of OLAPStatGraphicsTiles class
