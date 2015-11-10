package source;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import java.awt.*;
import java.awt.event.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.GraphicsConfiguration;
import java.util.Vector;
import javax.swing.JPanel;

/**
 * Simple Java 3D example program to display how collision work.
 */
public class OLAPTree extends JPanel implements MouseListener, MouseMotionListener{

    private PickCanvas pickCanvas;
    private SimpleUniverse univ = null;
    private BranchGroup scene = null;
	 Cube bumper1;
	 Cube bumper2;
         Cube bumper3;
	 QuadArray struct1;
	 QuadArray struct2;
         QuadArray struct3;
    
    TransformGroup shiftview = null;

    private static final int PWIDTH = 500;   // size of panel
    private static final int PHEIGHT = 500; 

    private static int BOUNDSIZE = 1000;  // larger than world

    private static Point3d USERPOSN;
    // initial user position
    
    DBSQLConnect DBC;
    DBSQLStatement DBS;
    
    //
    private float sizeCubo;
    private float sizeCuboSpace;
    
    Vector MeasuresVect;
    Vector DimensionsVect;
    
    String m_TableName;
    String idTable;
    
    Vector start = new Vector ();
    Vector finish = new Vector ();
    Vector valuesDim;
    
    int test;    
    boolean AllColor = false;
    
    Cube picked;
    OlapGraphicsData graphic1,graphic2,graphic3;
    
    String posCube;
    int numCube;
    
    OLAPStatGraphics OSG;
    
   
    /*VGH*/
    
    String primer_dataset="Primero";
    String segundo_dataset="Segundo";
    
    
     public OLAPTree(DBSQLConnect dbConn, DBSQLStatement dbStat, Vector dimSize, Vector DVector, Vector MVector, Vector values, String tblname, String idtable, int test, OlapGraphicsData graphic, OlapGraphicsData graphic2, int numCube, String posCube, OLAPStatGraphics o,OlapGraphicsData graphic3 ) {
	// Initialize the GUI components,
	//initComponents();
        DBC = dbConn;
        DBS = dbStat;
        valuesDim = values;
        
        OSG = o;
        
        this.test = test;
        
        if(posCube.equals("")){
            this.posCube = posCube;
        }
        else{
            this.posCube = posCube+"-";
        }
        this.numCube = numCube;
        
        m_TableName = tblname;
        idTable = idtable;
        
        DimensionsVect = DVector;
	MeasuresVect = MVector;
        graphic1 = graphic;
        this.graphic2 = graphic2;
        this.graphic3 = graphic3;
        
        
        setLayout( new BorderLayout() );
        setOpaque( false );
        setPreferredSize( new Dimension(PWIDTH, PHEIGHT));

	// Create Canvas3D and SimpleUniverse; add canvas to drawing panel
	Canvas3D c = createUniverse();
	//drawingPanel.add(c, java.awt.BorderLayout.CENTER);

	// Create the content branch and add it to the universe
	scene = createSceneGraph(dimSize);
	univ.addBranchGraph(scene);
        
        //Acciones con el mouse
        pickCanvas = new PickCanvas (c, scene);
	pickCanvas.setMode (pickCanvas.BOUNDS);
	
	//VGH - Estas dos lineas son las que despliega info de las graficas
        //c.addMouseListener (this);
	//c.addMouseMotionListener (this);
    }
      
    public BranchGroup createSceneGraph(Vector dimSize) {
	// Create the root of the branch graph
        
        System.out.println("Vane: Cuantas dimensiones:"+dimSize+"******");
        
        
	BranchGroup objRoot = new BranchGroup();
		  struct1 = new QuadArray(24, GeometryArray.ALLOW_COLOR_WRITE);
		  struct2 = new QuadArray(24, GeometryArray.ALLOW_COLOR_WRITE);
        // Create a Transformgroup to scale all objects so they
        // appear in the scene.
        TransformGroup objScale = new TransformGroup();
	objScale.setCapability(Group.ALLOW_CHILDREN_WRITE);
        Transform3D t3d = new Transform3D();
        t3d.setScale(0.4);
        objScale.setTransform(t3d);
        objRoot.addChild(objScale);

	// Create a bounds for the background and behaviors
	BoundingSphere bounds =
	new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

	// Set up the background
        //Color de fondo de la Aplicacion de la grafica
	Color3f bgColor = new Color3f(1.0f, 1.0f, 1.0f);
        //Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
	Background bg = new Background(bgColor);
	bg.setApplicationBounds(bounds);
	objScale.addChild(bg);

	// Create a pair of transform group nodes and initialize them to
	// identity.  Enable the TRANSFORM_WRITE capability so that
	// our behaviors can modify them at runtime.  Add them to the
	// root of the subgraph.
	TransformGroup objTrans1 = new TransformGroup();
	objTrans1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	objScale.addChild(objTrans1);
        
        //Tamaño total del cubo en la posición X
        //Número de cubos en la posición X menos uno por el tamaño que se incrementa
        int sizeY = 1;
        int sizeZ = 1;
        int sizeX = (Integer)dimSize.get(0 + (3*numCube));
        
        System.out.println("Vane: numCube"+numCube);
        System.out.println("Vane: dimensiones"+dimSize);
        //VGH This gets values from Vector 
        if (dimSize.size() - (3*numCube) >= 2)
            sizeY = (Integer)dimSize.get(1 + (3*numCube));
        if (dimSize.size() - (3*numCube) >= 3)
            sizeZ = (Integer)dimSize.get(2 + (3*numCube));
        
        System.out.println("Vane: Cubos en X"+sizeX);
        System.out.println("Vane: Cubos en y"+sizeY);
        System.out.println("Vane: Cubos en z"+sizeZ);
        
        int Vane1=(dimSize.size() - (3*numCube));
        System.out.println("Vane: Valor de dimsixe - cub"+Vane1);
        
        //VGH Only applies it has 3 dimensions
        if( (dimSize.size() - (3*numCube)) == 1 || (dimSize.size() - (3*numCube)) == 2 || (dimSize.size() - (3*numCube)) == 3)
            createSigDiffs ();
        
        //Determinar el tamaño para los cubos
        int sizeMayor=0;
        if(sizeX > sizeY)
            sizeMayor = sizeX;
        else
            sizeMayor = sizeY;
        
        if(sizeZ > sizeMayor)
            sizeMayor = sizeZ;
        
        if(sizeMayor <= 0){
            sizeCubo = 0.4f;
        }else{
            sizeCubo = (2f/sizeMayor) - 0.1f;
        }
        sizeCuboSpace = sizeCubo + 0.1f;
                
        float width = (float)((sizeX-1)*sizeCuboSpace);
        float high = (float) ((sizeY-1)*sizeCuboSpace);
        
        float posXText = (float) -sizeCubo/2.3f;//-((sizeX-1)*sizeCuboSpace)/2 + sizeCubo/8;
        float posYText = (float) (sizeCubo/2.8f) + 0.1f;//((sizeY-1)*sizeCuboSpace)/2 - sizeCubo/4;
        float posXYText = (float) (sizeCubo/1.2f) + 0.2f;
        float posZText =  -(sizeCubo/8f);
        float posPrueba=0;
        
        int contValuesX = 0;
        int contValuesY = 0;
        int contValuesZ = 0;
        Vector valuesY = new Vector();
        Vector valuesZ = new Vector();
        Vector valuesX = (Vector) valuesDim.get(0 + (3*numCube));
        if (dimSize.size() - (3*numCube) >= 2)
            valuesY = (Vector) valuesDim.get(1 + (3*numCube));
        if (dimSize.size() - (3*numCube) >= 3)
            valuesZ = (Vector) valuesDim.get(2 + (3*numCube));
        //valuesX.add("0");valuesX.add("1");valuesX.add("2");valuesX.add("3");valuesX.add("4");valuesX.add("5");valuesX.add("6");valuesX.add("7");valuesX.add("8");valuesX.add("9");valuesX.add("10");valuesX.add("11");
        //valuesY.add("0");valuesY.add("5");valuesY.add("6");valuesY.add("7");valuesY.add("4");valuesY.add("5");valuesY.add("6");valuesY.add("7");valuesY.add("8");valuesY.add("9");valuesY.add("10");valuesY.add("11");
        //valuesZ.add("0");valuesZ.add("1");valuesZ.add("2");valuesZ.add("3");valuesZ.add("4");valuesZ.add("5");valuesZ.add("6");valuesZ.add("7");valuesZ.add("8");valuesZ.add("9");valuesZ.add("10");valuesZ.add("11");
        
        float posZ = (float) 0;        
        for (int i=1; i <= sizeZ; i++){
        
            float posY = (float) ((sizeY-1)*sizeCuboSpace)/2;
            for (int j=1; j <= sizeY; j++){
                
                float posX = (float)- ((sizeX-1)*sizeCuboSpace)/2;
                
                //VGH cubos en el ejeX
                for (int k=1; k <= sizeX; k++){
                    
                    /*Vector posXYZ = new Vector();
                    posXYZ.add(valuesX.elementAt(k-1));
                    if (dimSize.size() - (3*numCube) >= 2)
                        posXYZ.add(valuesY.elementAt(j-1));
                    if (dimSize.size() - (3*numCube) >= 3)
                        posXYZ.add(valuesZ.elementAt(i-1));*/
                    
                    Cube bumper;
                    QuadArray struct;
                    String coor="";
                    
                    if (dimSize.size() - (3*numCube) >= 3)
                        coor=""+posCube+valuesX.get(k-1)+"-"+valuesY.get(j-1)+"-"+valuesZ.get(i-1);
                    else if (dimSize.size() - (3*numCube) == 2)
                        coor=""+posCube+valuesX.get(k-1)+"-"+valuesY.get(j-1);
                    else if (dimSize.size() - (3*numCube) == 1)
                        coor=""+posCube+valuesX.get(k-1);
                    
                    System.out.println("Vane Valor de coordenadas:"+coor);
                    
                    //Determina el color del cubo
                    Vector repeatIndex = new Vector();
                    
                    
                    if(start.contains(coor) || AllColor ){
                        //Si entra una vez se colorea todo para la prueba de CHI
                        if(test ==7){
                            AllColor= true;
                        }
                        repeatIndex.add(start.indexOf(coor));
                        //Búsqueda de repeticiones
                        for(int l=start.indexOf(coor); l<start.size() ;l++){
                            if((l+1) < start.size() ){
                                if(start.elementAt(l+1).equals(coor)){
                                    repeatIndex.add(l+1);
                                }
                            }
                        }
                        
                        //int relation = start.indexOf(coor);
                        //bumper = new Cube(colors[relation][0],colors[relation][1],colors[relation][2],sizeCubo/2);
                        //Si las pruebas involucran solo a una población, sel color será uniforme e igual para todas las poblaciones
                        if(test == 1 || test == 2  || test == 5 || test==7){
                            repeatIndex.clear();
                            repeatIndex.add(2);
                        }
                        bumper = new Cube(coor,repeatIndex,sizeCubo/2);
                        
                        //Elimina duplicados
                        //String relation2 = (String)finish.get(start.indexOf(coor));
                        //if(start.contains(relation2)){
                        //    finish.remove(start.indexOf(relation2));
                        //    start.remove(relation);
                        //}
                    }else if(finish.contains(coor)){
                        repeatIndex.add(finish.indexOf(coor));
                        //Búsqueda de repeticiones
                        for(int l=finish.indexOf(coor); l<finish.size() ;l++){
                            if((l+1) < finish.size() ){
                                if(finish.elementAt(l+1).equals(coor)){
                                    repeatIndex.add(l+1);
                                }
                            }
                        }
                        
                        bumper = new Cube(coor,repeatIndex,sizeCubo/2);
                    }else{
                        repeatIndex.add(15);
                        bumper = new Cube(coor,repeatIndex,sizeCubo/2);
                    }

                
                    struct = bumper.getQuadArray();
                    
                    System.out.println("cubo: " + posX +","+posY+","+posZ);
                    Group box = createBox(1.0, new Vector3d(posX, posY, posZ), (Shape3D)bumper, struct);
                    
                    
                    //Colores para las letras
                        Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);
                        Color3f sColor = new Color3f(1.0f, 1.0f, 1.0f);
                        Color3f objColor = new Color3f(0.6f, 0.6f, 0.6f);
                        
                        
                        //Crea apariencia de color para las letras 3D
                        Material m = new Material(objColor, eColor, objColor, sColor, 15.0f);
                        Appearance a = new Appearance();
                        m.setLightingEnable(true);
                        a.setMaterial(m);
                        
                        
                    //Posicion etiquetas en X
                    if(i == 1 && j == 1){
                        if(contValuesX == 0){
                            posXText = posX + ((float)(sizeX-1) * 0.1f);
                            posPrueba = posY - ((float)(sizeY-2) * 0.1f);
                            
                            if(valuesY.size() == 1 || (dimSize.size() - (3*numCube)) == 1)
                                posPrueba -= ((float)(sizeY-2) * 0.1f)*2f;
                        }
                        
                        
                        //Crea la transformación, (escala, posición)
                        Transform3D t = new Transform3D();
                        System.out.println("ejeX: " + posXText +","+posXYText+","+(posZ+ sizeCubo/8f));
                        //t.set(0.1, new Vector3d(posXText  ,posY,posZ + sizeCubo/8f));
                        System.out.println((sizeCubo/8)*sizeX);
                        
                        t.set(0.1, new Vector3d(posXText/*+ (sizeCubo/8)*sizeX*/ ,posPrueba /*- (sizeCubo/8)*sizeY*/ ,posZ + sizeCubo/8f));
                        posXText += sizeCubo/2f;
                        TransformGroup objTrans = new TransformGroup(t);
                        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                        
                        Font3D font3d = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
                        new FontExtrusion());
                        
                        Text3D textGeom = new Text3D(font3d, (String)valuesX.get(contValuesX),
                        new Point3f(0.0f, 0.0f, 0.0f));
                        contValuesX++;
                        Shape3D textShape = new Shape3D(textGeom);
                        textShape.setAppearance(a);
                        
                        TransformGroup trans = new TransformGroup();
                        trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                        MovimientoTextoEjes mov=new MovimientoTextoEjes(trans, sizeX, k, sizeCubo*5,-((sizeX-1)*sizeCuboSpace)/2 + sizeCubo/8, sizeY, j, 0f, 0f);
                        mov.setSchedulingBounds(bounds);                        
                        
                        objTrans.addChild(trans);
                        trans.addChild(textShape);
                        trans.addChild(mov);
                        
                        objRoot.addChild(objTrans);
                        
                    }
                    
                    
                    
                     //Posicion etiquetas en Y
                    if (dimSize.size() - (3*numCube) >= 2){
                    if(i == 1 && k == 1){
                        if(contValuesY == 0){
                            posYText = posY - (float)sizeY * 0.1f;
                        }
                        
                        if(valuesX.size() == 1)
                                posX -= sizeCubo/10f;
                        
                        Transform3D t = new Transform3D();
                        System.out.println("ejeY: " + (posX - sizeCubo/10f) +","+posYText+","+(posZ+ sizeCubo/8f));
                        t.set(0.1, new Vector3d(posX + (sizeX-3f)*0.1f /*posX - sizeCubo/10f*/,posYText,posZ + sizeCubo/8f));
                        posYText -= sizeCubo/2f;
                        TransformGroup objTrans = new TransformGroup(t);
                        
                        Font3D font3d = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
                        new FontExtrusion());
                        Text3D textGeom = new Text3D(font3d, (String)valuesY.get(contValuesY),
                        new Point3f(0.0f, 0.0f, 0.0f));
                        contValuesY++;
                        Shape3D textShape = new Shape3D(textGeom);
                        textShape.setAppearance(a);
                        
                        TransformGroup trans = new TransformGroup();
                        trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                        MovimientoTextoEjes mov=new MovimientoTextoEjes(trans, sizeX, k, 0f, 0f, sizeY, j, sizeCubo*5, 0f);
                        mov.setSchedulingBounds(bounds);                        
                        
                        objTrans.addChild(trans);
                        trans.addChild(textShape);
                        trans.addChild(mov);
                        
                        objRoot.addChild(objTrans);
                        
                        //objTrans.addChild(textShape);
                        //objRoot.addChild(objTrans);
                        
                    }
                    }
                    
                     //Posicion etiquetas en Z
                    if (dimSize.size() - (3*numCube) >= 3){
                    if(j == 1 && k == 1){
                        
                        Transform3D t = new Transform3D();
                        t.set(0.1, new Vector3d(posX + (sizeX-3f)*0.1f/*posX - sizeCubo/10f*/,posY - ((float)(sizeY-2) * 0.1f),posZText));
                        posZText -= sizeCubo/2f;
                        TransformGroup objTrans = new TransformGroup(t);
                        
                        Font3D font3d = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
                        new FontExtrusion());
                        Text3D textGeom = new Text3D(font3d, (String)valuesZ.get(contValuesZ),
                        new Point3f(0.0f, 0.0f, 0.0f));
                        contValuesZ++;
                        Shape3D textShape = new Shape3D(textGeom);
                        textShape.setAppearance(a);
                        
                        objTrans.addChild(textShape);
                        objRoot.addChild(objTrans);
                        
                    }
                    }
                
                    //PROBANDO
                    TransformGroup trans = new TransformGroup();
                    trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                    Movimiento mov=new Movimiento(trans, width, posX, high, posY, sizeCuboSpace);
                    mov.setSchedulingBounds(bounds);                        
                    objScale.addChild(trans);
        
                    //if(k==3){
                   //if(j!=1)
                    trans.addChild(box);//***Linea que anade el cubo
                   box.addChild(mov);
                   //objRoot.addChild(trans);
                //}else{
                    //objScale.addChild(box);
                //}
                //incremento en la posición X cada que termina un ciclo
                    posX += sizeCuboSpace;
                    System.out.println("Vane , dentro de tercera iteracion k="+k);
                }
                System.out.println("Vane , terminaron los valores de X=");
                posY -= sizeCuboSpace;
            }   
            posZ -= sizeCuboSpace;
        }
        
        
        //DIMENSIONES
        Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);
                        Color3f sColor = new Color3f(1.0f, 1.0f, 1.0f);
                        Color3f objColor = new Color3f(0.6f, 0.6f, 0.6f);
                        
        Material m = new Material(objColor, eColor, objColor, sColor, 15.0f);
                        Appearance a = new Appearance();
                        m.setLightingEnable(true);
                        a.setMaterial(m);
        
        Transform3D tx = new Transform3D();
    
        tx.set(0.1, new Vector3d(0,1f,0));
        TransformGroup objTrans = new TransformGroup(tx);
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                        
        Font3D font3d = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
        new FontExtrusion());
                        
        Text3D textGeom = new Text3D(font3d, (String)DimensionsVect.elementAt(0 + (3*numCube)) ,
                        new Point3f(0.0f, 0.0f, 0.0f));
                        Shape3D textShape = new Shape3D(textGeom);
                        textShape.setAppearance(a);
                        
        objTrans.addChild(textShape);
        objRoot.addChild(objTrans);
        
        //Eje Y
        if (dimSize.size() - (3*numCube) >= 2){
        Transform3D ty = new Transform3D();
    
        ty.set(0.1, new Vector3d(-1f,0,0));
        objTrans = new TransformGroup(ty);
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                        
        font3d = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
        new FontExtrusion());
                        
        textGeom = new Text3D(font3d, (String)DimensionsVect.elementAt(1 + (3*numCube)) ,
                        new Point3f(0.0f, 0.0f, 0.0f));
        textShape = new Shape3D(textGeom);
        textShape.setAppearance(a);
                        
        objTrans.addChild(textShape);
        objRoot.addChild(objTrans);
        }
        
        //Eje Z
        if (dimSize.size() - (3*numCube) >= 3){
        Transform3D tz = new Transform3D();
    
        tz.set(0.1, new Vector3d(-0.8f,0.8f,-0.2f));
        objTrans = new TransformGroup(tz);
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                        
        font3d = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
        new FontExtrusion());
                        
        textGeom = new Text3D(font3d, (String)DimensionsVect.elementAt(2 + (3*numCube)) ,
                        new Point3f(0.0f, 0.0f, 0.0f));
        textShape = new Shape3D(textGeom);
        textShape.setAppearance(a);
                        
        objTrans.addChild(textShape);
        objRoot.addChild(objTrans);
        }
	// Create a new Behavior object that will perform the desired
	// rotation on the specified transform object and add it into
	// the scene graph.
	
	/*bumper1 = new Cube();
	bumper2 = new Cube();
        bumper3 = new Cube();
	// Now create a pair of rectangular boxes, each with a collision
	// detection behavior attached.  The behavior will highlight the
	// object when it is in a state of collision.
	struct1 = bumper1.getQuadArray();
	struct2 = bumper2.getQuadArray();
        struct3 = bumper3.getQuadArray();
	Group box1 = createBox(1.0, new Vector3d(1.5, 0.0, 0.0), (Shape3D)bumper1, struct1);
	Group box2 = createBox(1.0, new Vector3d(-1.5, 0.0, 0.0), (Shape3D)bumper2, struct2);
        Group box3 = createBox(1.0, new Vector3d(0.0, 0.0, 0.0), (Shape3D)bumper3, struct3);

	
	objScale.addChild(box1);
	objScale.addChild(box2);
        objScale.addChild(box3);*/
 
    // Have Java 3D perform optimizations on this scene graph.
        objRoot.compile();

	return objRoot;
    }

    private Group createBox(double scale, Vector3d pos, Shape3D shape, QuadArray structure) {
	// Create a transform group node to scale and position the object.
	Transform3D t = new Transform3D();
	t.set(scale, pos);
	TransformGroup objTrans = new TransformGroup(t);

	// Create a simple shape leaf node and add it to the scene graph


	
	// Create a new Behavior object that will perform the collision
	// detection on the specified object, and add it into
	// the scene graph.
	//CollisionDetector cd = new CollisionDetector(shape, structure);
	BoundingSphere bounds =
	    new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
	//cd.setSchedulingBounds(bounds);
	//cd.setEnable(true);
        objTrans.addChild(shape);
	// Add the behavior to the scene graph
	//objTrans.addChild(cd);

	return objTrans;
    }

    private Canvas3D createUniverse() {
	// Get the preferred graphics configuration for the default screen
	GraphicsConfiguration config =
	    SimpleUniverse.getPreferredConfiguration();

	// Create a Canvas3D using the preferred configuration
	Canvas3D c = new Canvas3D(config);
        
        add("Center", c);
        c.setFocusable(true);     // give focus to the canvas 
        c.requestFocus();
        
	// Create simple universe with view branch
	univ = new SimpleUniverse(c);

	// This will move the ViewPlatform back a bit so the
	// objects in the scene can be viewed.
	//univ.getViewingPlatform().setNominalViewingTransform();

        
        // This will move the ViewPlatform back a bit so the
	// objects in the scene can be viewed.
	ViewingPlatform myspot = univ.getViewingPlatform();
	myspot.setNominalViewingTransform();
	shiftview = myspot.getViewPlatformTransform();
	            OrbitBehavior orbit = new OrbitBehavior(univ.getCanvas(),
						    OrbitBehavior.REVERSE_ALL);
            BoundingSphere bounds =
                new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

            orbit.setSchedulingBounds(bounds);
            orbit.setTransFactors(0.2, 0.2);
            myspot.setViewPlatformBehavior(orbit);
        
        
	// Ensure at least 5 msec per frame (i.e., < 200Hz)
	univ.getViewer().getView().setMinimumFrameCycleTime(5);

	return c;
    }

   
   

    // ----------------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    /*private void initComponents() {
        drawingPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TickTockCollision");
        drawingPanel.setLayout(new java.awt.BorderLayout());

        drawingPanel.setPreferredSize(new java.awt.Dimension(700, 700));
        getContentPane().add(drawingPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents*/
    
    
     private void createSigDiffs ()
  { 
                //Vector de medidas + (tamaño de dimensiones x 2) 
                int sizeNames = 0;
                if(MeasuresVect.size () > 0)
                    sizeNames = MeasuresVect.size () + 2*DimensionsVect.size ();
                else
                    sizeNames = 1 + 2*DimensionsVect.size ();
                
                String[] names = new String [ sizeNames ];
                //String[] names = new String [MeasuresVect.size () * 2 + 2*DimensionsVect.size ()];
		String[] results = new String [MeasuresVect.size () + 2*DimensionsVect.size ()];
		
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		
		Columns.addElement ("*");
		TableName.addElement (m_TableName + OLAPStatTest.finalTableText + idTable);
		
		String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		
		DBC.sendQuery (sqlStat);
		
		int cnt = 0;
                
                //Agrega al vector de nombres cada medida con el prefijo PValue_
                if(MeasuresVect.size() > 0){
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
                }else{//Para la prueba PP
                        names[cnt] = "PValue";
                        cnt++;
                }
                //Agrega al vector de nombres las dimensiones con el prefijo 1
		for (int i = 0; i < DimensionsVect.size (); i++)
		{
			names[cnt] = (String)DimensionsVect.elementAt(i) + "1";
			cnt++;
		}
                //Agrega al vector de nombres las dimensiones con el prefijo 2
		for (int i = 0; i < DimensionsVect.size (); i++)
		{
			names[cnt] = (String)DimensionsVect.elementAt(i) + "2";
			cnt++;
		}

		results = DBC.getResult (names);
		cnt = 0;
		String mstr = "";

		System.out.println ("RESULTS for " + m_TableName + OLAPStatTest.finalTableText + ":");
                //Recorre los resultados y obtiene las dimensiones relacionadas
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
				
                            if (Integer.parseInt (results[0]) >= 3){
                                
                                int d1 =  DimensionsVect.size ();
                                if(MeasuresVect.size() > 0){
                                    int p1 = MeasuresVect.size ();
                                    int p2 = MeasuresVect.size () + DimensionsVect.size ();
                                    
                                    String element="";
                                    for(int i=0;i<d1;i++){
                                        element += results[p1+i];
                                        if( i+1 != d1 )
                                            element += "-";
                                    }
                                    start.addElement(element);
                                    
                                    element="";
                                    for(int i=0;i<d1;i++){
                                        element += results[p2+i];
                                        if( i+1 != d1 )
                                            element += "-";
                                    }
                                    finish.addElement(element);
                                    /*
                                    if(d1 == 1){
                                        start.addElement (posCube+results[p1+(3*numCube)]);
                                        finish.addElement (posCube+results[p2+(3*numCube)]);
                                    }
                                    else if (d1 == 2){
                                        start.addElement (posCube+results[p1+(3*numCube)] + "-" + results[p1+(3*numCube)+1]);
                                        finish.addElement (posCube+results[p2+(3*numCube)] + "-" + results[p2+(3*numCube)+1]);
                                    }else{
                                        start.addElement (posCube+results[p1+(3*numCube)] + "-" + results[p1+(3*numCube)+1] + "-" + results[p1+(3*numCube)+2]);
                                        finish.addElement (posCube+results[p2+(3*numCube)] + "-" + results[p2+(3*numCube)+1] + "-" + results[p2+(3*numCube)+2]);
                                    }*/
                                }else{
                                    int p1 = 1;
                                    int p2 = 1 + DimensionsVect.size ();
                                    
                                    String element="";
                                    for(int i=0;i<d1;i++){
                                        element += results[p1+i];
                                        if( i+1 != d1 )
                                            element += "-";
                                    }
                                    start.addElement(element);
                                    
                                    element="";
                                    for(int i=0;i<d1;i++){
                                        element += results[p2+i];
                                        if( i+1 != d1 )
                                            element += "-";
                                    }
                                    finish.addElement(element);
                                    
                                    /*if(d1 == 1){
                                        start.addElement (posCube+results[p1+(3*numCube)]);
                                        finish.addElement (posCube+results[p2+(3*numCube)]);
                                    }
                                    else if(d1 == 2){
                                        start.addElement (posCube+results[p1+(3*numCube)] + "-" + results[p1+(3*numCube)+1]);
                                        finish.addElement (posCube+results[p2+(3*numCube)] + "-" + results[p2+(3*numCube)+1]);
                                    }else{
                                        start.addElement (posCube+results[p1+(3*numCube)] + "-" + results[p1+(3*numCube)+1] + "-" + results[p1+(3*numCube)+2]);
                                        finish.addElement (posCube+results[p2+(3*numCube)] + "-" + results[p2+(3*numCube)+1] + "-" + results[p2+(3*numCube)+2]);
                                    }*/
                                }
                            }
			results = DBC.getResult (names);
		}
                
                //Elimina duplicados de start y finish
                for(int i=0; i < start.size(); i++){
                    String elementS = (String)start.elementAt(i);
                    String elementF = (String)finish.elementAt(i);
                    for(int j=(i+1); j < start.size(); j++){
                        if(elementS.equals((String)finish.elementAt(j)) && elementF.equals((String)start.elementAt(j))){
                            start.remove(j);
                            finish.remove(j);
                        }
                    }
                }
                
		System.out.println ("END OF RESULTS TREE");
  }

    public void mouseClicked(MouseEvent e) {
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        if (result == null) {
            
        }
        else{
            //Si son mas de 3 dimensiones
            if(DimensionsVect.size() > 3){
                picked = (Cube)result.getObject ();
                String coor = picked.getCoord();
                OLAPStatGraphics OSG;
                Vector img = new Vector();
                OSG = new OLAPStatGraphics ("postgresql", DimensionsVect, MeasuresVect, img, m_TableName, idTable, test, numCube + 1, coor);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        if (result == null) {
            //System.out.println("Nothing picked");
        }else{
            try{
                picked = (Cube)result.getObject ();
                String coor = picked.getCoord();
                String relation = "";
                
                //Busca si existe una relacion
                if(start.contains(coor)){
                    relation = (String)finish.get(start.indexOf(coor));
                   // graphic1.setNombrePrimerDataset("Primero");
                    graphic1.refresh(DBC, DBS, coor, relation, DimensionsVect, MeasuresVect, m_TableName, test );
                    graphic3.refreshT(DBC, DBS, coor, relation, DimensionsVect, MeasuresVect, m_TableName, test ,primer_dataset);
                    
                    
                    if(test == 1 || test == 2 || test == 5 ){
                        graphic2.removeAll();
                        graphic2.updateUI();
                    }
                    else{
                        graphic2.refresh(DBC, DBS, relation, coor, DimensionsVect, MeasuresVect, m_TableName, test );
                        graphic3.refreshT(DBC, DBS,  relation,coor, DimensionsVect, MeasuresVect, m_TableName, test ,segundo_dataset);
                    }
                }
                else if(finish.contains(coor)){
                    relation = (String)start.get(finish.indexOf(coor));
                    //graphic1.setNombrePrimerDataset("Primero");
                    graphic1.refresh(DBC, DBS, relation, coor, DimensionsVect, MeasuresVect, m_TableName, test );
                    graphic3.refreshT(DBC, DBS, relation, coor, DimensionsVect, MeasuresVect, m_TableName, test,primer_dataset );
                    if(test == 1 || test == 2 || test == 5){
                        graphic2.removeAll();
                        graphic2.updateUI();
                    }
                    else{
                        graphic2.refresh(DBC, DBS, coor, relation, DimensionsVect, MeasuresVect, m_TableName, test );
                        graphic3.refreshT(DBC, DBS, coor, relation, DimensionsVect, MeasuresVect, m_TableName, test ,segundo_dataset);
                    }
                }else{
                    //Actualiza la gráfica 1
                    graphic2.removeAll();
                    graphic2.updateUI();
                    //graphic1.setNombrePrimerDataset("Primero");

                    graphic1.refresh(DBC, DBS, coor, relation, DimensionsVect, MeasuresVect, m_TableName, test );
                    graphic3.refreshT(DBC, DBS, coor, relation, DimensionsVect, MeasuresVect, m_TableName, test,primer_dataset);
                }
                System.out.println("Coordenada"+coor);
            }catch(Exception ex){
                
            }
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    /*public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new OLAPCube().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel drawingPanel;
    // End of variables declaration//GEN-END:variables
    */






}

