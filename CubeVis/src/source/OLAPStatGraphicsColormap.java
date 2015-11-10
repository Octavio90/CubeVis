package source;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.lang.Math.*;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.behaviors.vp.*;

import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.geometry.Sphere;
import javax.media.j3d.BranchGroup;
import javax.vecmath.Color3f;
import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.DirectionalLight;

// import com.tornadolabs.j3dtree.*;    // for displaying the scene graph




public class OLAPStatGraphicsColormap extends JPanel
// Holds the 3D canvas where the loaded image is displayed
{
  private static final int PWIDTH = 50;   // size of panel
  private static final int PHEIGHT = 300; 

  private static int BOUNDSIZE = 100;  // larger than world

  private static Point3d USERPOSN;
    // initial user position

  private SimpleUniverse su;
  private BranchGroup sceneBG;
  private BoundingSphere bounds;   // for environment nodes

  // private Java3dTree j3dTree;   // frame to hold tree display
  
  Vector appearanceVect;

  public OLAPStatGraphicsColormap()
  // A panel holding a 3D canvas: the usual way of linking Java 3D to Swing
  {
	appearanceVect = new Vector ();
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

	// j3dTree.updateNodes( su );    // build the tree display window

  } // end of OLAPStatGraphicsColormap()



  private void createSceneGraph() 
  // initilise the scene
  { 
     Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
	Color3f specular = new Color3f(0.9f, 0.9f, 0.9f);
	Color3f white = new Color3f (1.0f, 1.0f, 1.0f);
	
    sceneBG = new BranchGroup();
	sceneBG.setCapability (BranchGroup.ALLOW_CHILDREN_EXTEND);
	
    bounds = new BoundingSphere(new Point3d(0,0,0), BOUNDSIZE);   

    lightScene();         // add the lights
    addBackground();      // add the sky
	
    QuadArray plane = new QuadArray(32, GeometryArray.COORDINATES | GeometryArray.COLOR_3 );
	Point3f[] points = new Point3f[32];
	Color3f[] cols = new Color3f[32];
	float x=0, y=0, z=0;
	int pntcnt = 0, colcnt = 0, regioncnt=63;

	for (int i = 0; i < 8; i++)
	{
		points[pntcnt] = new Point3f (x, y, z);
		pntcnt++;
		points[pntcnt] = new Point3f (x+1, y, z);
		pntcnt++;
		points[pntcnt] = new Point3f (x+1, y, z+1);
		pntcnt++;
		points[pntcnt] = new Point3f (x, y, z+1);
		pntcnt++;
		
		z += 1;
		
		cols[colcnt] = new Color3f ((float)OLAPStatGraphics.regionColors[regioncnt][0], (float)OLAPStatGraphics.regionColors[regioncnt][1], (float)OLAPStatGraphics.regionColors[regioncnt][2]);
		colcnt++;
		cols[colcnt] = new Color3f ((float)OLAPStatGraphics.regionColors[regioncnt][0], (float)OLAPStatGraphics.regionColors[regioncnt][1], (float)OLAPStatGraphics.regionColors[regioncnt][2]);
		colcnt++;
		if (regioncnt == 7)
			regioncnt -= 7;
		else
			regioncnt -= 8;
		cols[colcnt] = new Color3f ((float)OLAPStatGraphics.regionColors[regioncnt][0], (float)OLAPStatGraphics.regionColors[regioncnt][1], (float)OLAPStatGraphics.regionColors[regioncnt][2]);
		colcnt++;
		cols[colcnt] = new Color3f ((float)OLAPStatGraphics.regionColors[regioncnt][0], (float)OLAPStatGraphics.regionColors[regioncnt][1], (float)OLAPStatGraphics.regionColors[regioncnt][2]);
		colcnt++;
	}
	
    plane.setCoordinates(0, points);
    plane.setColors(0, cols);
	plane.setCapability (QuadArray.ALLOW_COLOR_WRITE);

    Appearance app = new Appearance();
    PolygonAttributes pa = new PolygonAttributes();
    pa.setCullFace(PolygonAttributes.CULL_NONE);   
    app.setPolygonAttributes(pa);
	
	sceneBG.addChild (new Shape3D(plane, app));
	
	
	// MAKE TEXT
    Vector3d pt = new Vector3d();
    pt.x = 1.2;
	pt.y = 0;
	float num = 1.0f;
    for (int i=0; i < 5; i++) {
      pt.z = i*2.0 + 0.2;
      sceneBG.addChild( makeText(pt,""+num) );   // along z-axis
	  num = num - 0.5f;
    }

    sceneBG.compile();   // fix the scene
  } // end of createSceneGraph()

   private TransformGroup makeText(Vector3d vertex, String text)
  // Create a Text2D object at the specified vertex
  {
    Text2D message = new Text2D(text, new Color3f (0.0f, 0.0f, 0.0f), "SansSerif", 80, Font.BOLD );
       // 36 point bold Sans Serif

    TransformGroup tg = new TransformGroup();
    Transform3D t3d = new Transform3D();
    t3d.setTranslation(vertex);
	t3d.setRotation (new AxisAngle4d (1.0, 0.0, 0.0, -Math.PI / 2));
    tg.setTransform(t3d);
    tg.addChild(message);
    return tg;
  } // end of getTG()

  
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
//    sceneBG.addChild(light1);

//    DirectionalLight light2 = 
//        new DirectionalLight(white, light2Direction);
//    light2.setInfluencingBounds(bounds);
//    sceneBG.addChild(light2);
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
	USERPOSN = new Point3d(1, 2, 4);
    t3d.lookAt( USERPOSN, new Point3d(1 , 0, 3.999), new Vector3d(0,1,0));
    t3d.invert();

    steerTG.setTransform(t3d);
  }  // end of initUserPosition()


} // end of OLAPStatGraphicsColormap class