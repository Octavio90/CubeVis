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




public class OLAPStatGraphicsHeartWrap extends JPanel
// Holds the 3D canvas where the loaded image is displayed
{
  private static final int PWIDTH = 300;   // size of panel
  private static final int PHEIGHT = 300; 

  private static int BOUNDSIZE = 100;  // larger than world
  
  private static Point3d USERPOSN;
    // initial user position

  private SimpleUniverse su;
  private BranchGroup sceneBG;
  private BoundingSphere bounds;   // for environment nodes

  // private Java3dTree j3dTree;   // frame to hold tree display
  
  Vector appearanceVect;

  public OLAPStatGraphicsHeartWrap()
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

  } // end of OLAPStatGraphicsHeartWrap()



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
	
	Material mat = new Material(white, black, white, specular, 25.0f);
    mat.setLightingEnable(true);
	Appearance[] app = new Appearance[9];
	
	appearanceVect.clear ();
	for (int i = 0; i < 9; i++)
	{
		app[i] = new Appearance ();
		app[i].setCapability (Appearance.ALLOW_MATERIAL_WRITE);
		app[i].setMaterial (mat);
	}
	appearanceVect.addElement (app);
	
	drawHeart (0.0f, 0.0f, 0.0f, app);	

    sceneBG.compile();   // fix the scene
  } // end of createSceneGraph()

  public void changeColor (int heart, String region, Color3f color)
  {
	Appearance[] app = (Appearance[])appearanceVect.elementAt (heart);
//  System.out.println ("REQUEST Change for " + region + " to " + color);
  	Material mat = new Material(color, new Color3f (0.0f, 0.0f, 0.0f), color, new Color3f (0.9f, 0.9f, 0.9f), 25.0f);
    mat.setLightingEnable(true);
	if (region.equalsIgnoreCase ("AP"))
		app[0].setMaterial(mat);
	else if (region.equalsIgnoreCase ("AL"))
		app[8].setMaterial(mat);
	else if (region.equalsIgnoreCase ("LA"))
		app[6].setMaterial(mat);		
	else if (region.equalsIgnoreCase ("IL"))
		app[2].setMaterial(mat);		
	else if (region.equalsIgnoreCase ("LI"))
		app[4].setMaterial(mat);		
	else if (region.equalsIgnoreCase ("IS_"))
		app[1].setMaterial(mat);		
	else if (region.equalsIgnoreCase ("SI"))
		app[3].setMaterial(mat);		
	else if (region.equalsIgnoreCase ("AS_"))
		app[7].setMaterial(mat);		
	else if (region.equalsIgnoreCase ("SA"))
		app[5].setMaterial(mat);		
}

public void createThumbnails (Vector appVect)
{
	int num = appVect.size ();
	int cols = (int)Math.sqrt (num);
	int rows = (int)((float)num / (float) cols + 0.5);
	
	Appearance[] app;
//	sceneBG.removeAllChildren();
//	sceneBG = new BranchGroup ();
//	sceneBG.setCapability (BranchGroup.ALLOW_CHILDREN_WRITE);
	
    bounds = new BoundingSphere(new Point3d(0,0,0), BOUNDSIZE);

//    lightScene();         // add the lights
//    addBackground();      // add the sky

//	appearanceVect.clear ();
	
	int cnt = 0;
	int sx = 0, sy = 0, sz = 0;
	for (int i = 0; i < rows; i++)
	{
		for (int j = 0; j < cols; j++)
		{
			app = (Appearance[])appVect.elementAt (cnt);
			appearanceVect.addElement (app);
			drawHeart2 (sx, sy, sz, app);
			sx += 5;
			cnt++;
		}
		sz += 5;
		sx = 0;
	}
	
//	sceneBG.compile ();
}
  
  private void drawHeart (float sx, float sy, float sz, Appearance[] app)
  {
    // Create the blue appearance node
	float x, y, z, r;

    BranchGroup tBG = new BranchGroup();
	tBG.setCapability (BranchGroup.ALLOW_DETACH);

	x = sx+0f; y = sy+0.04f; z = sz+0f; r = 1;
	drawSphere (tBG, x, y, z, r, app[0]);
	sceneBG.addChild (makeText (new Vector3d (sx+0,sy+1.1,sz+0), "AP"));
	
	x = sx-0.2f; y = sy-0.1f; z = sz+0.2f; r = 1;
	drawSphere (tBG, x, y, z, r, app[1]);
	sceneBG.addChild (makeText (new Vector3d (sx-2,sy+0,sz+1.5), "IS_"));

	x = sx+0.2f; y = sy-0.1f; z = sz+0.2f; r = 1;
	drawSphere (tBG, x, y, z, r, app[2]);
	sceneBG.addChild (makeText (new Vector3d (sx+2,sy+0,sz+1.5), "IL"));

	x = sx-0.32f; y = sy-0.1f; z = sz+0.1f; r = 1;
	drawSphere (tBG, x, y, z, r, app[3]);
	sceneBG.addChild (makeText (new Vector3d (sx-2.5,sy+0,sz+0.5), "SI"));

	x = sx+0.32f; y = sy-0.1f; z = sz+0.1f; r = 1;
	drawSphere (tBG, x, y, z, r, app[4]);
	sceneBG.addChild (makeText (new Vector3d (sx+2.5,sy+0,sz+0.5), "LI"));

	x = sx-0.32f; y = sy-0.1f; z = sz-0.1f; r = 1;
	drawSphere (tBG, x, y, z, r, app[5]);
	sceneBG.addChild (makeText (new Vector3d (sx-2.5,sy+0,sz-0.5), "SA"));

	x = sx+0.32f; y = sy-0.1f; z = sz-0.1f; r = 1;
	drawSphere (tBG, x, y, z, r, app[6]);
	sceneBG.addChild (makeText (new Vector3d (sx+2.5,sy+0,sz-0.5), "LA"));

	x = sx-0.2f; y = sy-0.1f; z = sz-0.2f; r = 1;
	drawSphere (tBG, x, y, z, r, app[7]);
	sceneBG.addChild (makeText (new Vector3d (sx-2,sy+0,sz-1.5), "AS_"));

	x = sx+0.2f; y = sy-0.1f; z = sz-0.2f; r = 1;
	drawSphere (tBG, x, y, z, r, app[8]);
	sceneBG.addChild (makeText (new Vector3d (sx+2,sy+0,sz-1.5), "AL"));

	sceneBG.addChild (tBG);
  }

  private void drawHeart2 (float sx, float sy, float sz, Appearance[] app)
  {
    // Create the blue appearance node
	float x, y, z, r;
        
        BranchGroup tBG = new BranchGroup();
	tBG.setCapability (BranchGroup.ALLOW_DETACH);

        System.out.println("Aqui");
        
	x = sx+0f; y = sy+0.04f; z = sz+0f; r = 1;
	drawSphere (tBG, x, y, z, r, app[0]);
	//tBG.addChild (makeText (new Vector3d (sx+0,sy+1.1,sz+0), "AP"));

	x = sx-0.2f; y = sy-0.1f; z = sz+0.2f; r = 1;
	drawSphere (tBG, x, y, z, r, app[1]);
	//tBG.addChild (makeText (new Vector3d (sx-2,sy+0,sz+1.5), "IS_"));

	x = sx+0.2f; y = sy-0.1f; z = sz+0.2f; r = 1;
	drawSphere (tBG, x, y, z, r, app[2]);
	//tBG.addChild (makeText (new Vector3d (sx+2,sy+0,sz+1.5), "IL"));

	x = sx-0.32f; y = sy-0.1f; z = sz+0.1f; r = 1;
	drawSphere (tBG, x, y, z, r, app[3]);
	//tBG.addChild (makeText (new Vector3d (sx-2.5,sy+0,sz+0.5), "SI"));

	x = sx+0.32f; y = sy-0.1f; z = sz+0.1f; r = 1;
	drawSphere (tBG, x, y, z, r, app[4]);
	//tBG.addChild (makeText (new Vector3d (sx+2.5,sy+0,sz+0.5), "LI"));

	x = sx-0.32f; y = sy-0.1f; z = sz-0.1f; r = 1;
	drawSphere (tBG, x, y, z, r, app[5]);
	//tBG.addChild (makeText (new Vector3d (sx-2.5,sy+0,sz-0.5), "SA"));

	x = sx+0.32f; y = sy-0.1f; z = sz-0.1f; r = 1;
	drawSphere (tBG, x, y, z, r, app[6]);
	//tBG.addChild (makeText (new Vector3d (sx+2.5,sy+0,sz-0.5), "LA"));

	x = sx-0.2f; y = sy-0.1f; z = sz-0.2f; r = 1;
	drawSphere (tBG, x, y, z, r, app[7]);
	//tBG.addChild (makeText (new Vector3d (sx-2,sy+0,sz-1.5), "AS_"));

	x = sx+0.2f; y = sy-0.1f; z = sz-0.2f; r = 1;
	drawSphere (tBG, x, y, z, r, app[8]);
	//tBG.addChild (makeText (new Vector3d (sx+2,sy+0,sz-1.5), "AL"));

	sceneBG.addChild (tBG);
  }

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
  
  
  private void drawSphere(BranchGroup BG, float x, float y, float z, float r, Appearance app)
  // A shiny blue sphere located at (0,4,0)
  {
    Transform3D t3d = new Transform3D();
    t3d.set( new Vector3f(x,y,z)); 
    TransformGroup tg = new TransformGroup(t3d);
	Sphere sp = new Sphere (r, 1, 100);
//	sp.setCapability (Sphere.ALLOW_APPEARANCE_WRITE);
	sp.setAppearance (app);
    tg.addChild(sp);   // set its radius and appearance
    BG.addChild(tg);
  }  // end of floatingSphere()
  
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
	USERPOSN = new Point3d(0, 10, 0.1);
    t3d.lookAt( USERPOSN, new Point3d(0,0,0), new Vector3d(0,1,0));
    t3d.invert();

    steerTG.setTransform(t3d);
  }  // end of initUserPosition()


} // end of OLAPStatGraphicsHeartWrap class