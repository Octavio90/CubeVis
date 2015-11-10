package source;

import java.util.Vector;
import javax.media.j3d.*;
import javax.vecmath.*;

public class Cube extends Shape3D {
    
        String coord = "";
	QuadArray quaddy;
        
        //Color
        private float R=0,R2=0,R3=0,R4=0,R5=0,R6=0;
        private float G=0,G2=0,G3=0,G4=0,G5=0,G6=0;
        private float B=0,B2=0,B3=0,B4=0,B5=0,B6=0;

        private static float p;
        
	private static  Point3f p1;
	private static  Point3f p2; 
	private static  Point3f p3;
	private static  Point3f p4;
	private static  Point3f p5;
	private static  Point3f p6;
	private static  Point3f p7;
	private static  Point3f p8;
	


    private static Point3f[] verts;

    private TexCoord2f texCoord[] = {
        new TexCoord2f(0.0f, 1.0f),
        new TexCoord2f(1.0f, 0.0f),
        new TexCoord2f(1.0f,  0.0f),
        new TexCoord2f(0.0f,  1.0f),    
	 };
    
    final static float[][] colors = {
	{ 0, 0, 1 },
        { 0, 0, 0.2f },
        { 0, 1, 0 },
        { 0, 0.2f, 0 },
        { 0, 1, 1 },
        { 0, 1, 0.2f },
        { 0, 0.2f, 1 },
        { 1, 0, 0 },
        { 0.2f, 0, 0 },
        { 1, 0, 1 },
        { 0.2f, 0, 1 },
        { 1, 0, 0.2f },
        { 1, 1, 0 },
        { 0.2f, 1, 0 },
        { 1, 0.2f, 0 },
        { 0.5f, 0.5f, 0.5f }
    };
   /*public Color3f sidecolor[] = {
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //ATRAS
                new Color3f(0.0f, 0.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //IZQ
                new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //ARRIBA
                new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //DER
                new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //FRENTE
                new Color3f(R, G, B),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f)};
                /*
		new Color3f(0.0f, 1.0f, 1.0f),
		new Color3f(0.0f, 1.0f, 1.0f),
		new Color3f(0.0f, 1.0f, 1.0f),
		new Color3f(0.0f, 1.0f, 1.0f),				
		new Color3f(1.0f, 0.0f, 0.0f),
		new Color3f(1.0f, 0.0f, 0.0f),
		new Color3f(1.0f, 0.0f, 0.0f),
		new Color3f(1.0f, 0.0f, 0.0f),
		new Color3f(0.0f, 1.0f, 0.0f),
		new Color3f(0.0f, 1.0f, 0.0f),
		new Color3f(0.0f, 1.0f, 0.0f),
		new Color3f(0.0f, 1.0f, 0.0f),
		new Color3f(0.0f, 0.0f, 1.0f),
		new Color3f(0.0f, 0.0f, 1.0f),
		new Color3f(0.0f, 0.0f, 1.0f),
		new Color3f(0.0f, 0.0f, 1.0f),
		new Color3f(1.0f, 0.0f, 1.0f),
		new Color3f(1.0f, 0.0f, 1.0f),
		new Color3f(1.0f, 0.0f, 1.0f),
		new Color3f(1.0f, 0.0f, 1.0f)};*/

    //public Cube(float colorR, float colorG, float colorB, float size) {
    public Cube(String coor, Vector colors, float size) {
        /*if(coor.size() == 3){
            coord = "" + coor.elementAt(0) + "-" + coor.elementAt(1) + "-" + coor.elementAt(2);
        }
        else if(coor.size() == 2){
            coord = "" + coor.elementAt(0) + "-" + coor.elementAt(1);
        }
        else if(coor.size() == 1){
            coord = "" + coor.elementAt(0);
        }*/
        coord = coor;
	int i;
        
        if(colors.size() == 1){
            R = R2 = R3 = R4 = R5 = R6 = this.colors[(Integer)colors.elementAt(0)][0];
            G = G2 = G3 = G4 = G5 = G6 = this.colors[(Integer)colors.elementAt(0)][1];
            B = B2 = B3 = B4 = B5 = B6 = this.colors[(Integer)colors.elementAt(0)][2];
        }else
        if(colors.size() == 2){
            R = R2 = R3 = this.colors[(Integer)colors.elementAt(0)][0];
            G = G2 = G3 = this.colors[(Integer)colors.elementAt(0)][1];
            B = B2 = B3 = this.colors[(Integer)colors.elementAt(0)][2];
            R4 = R5 = R6 = this.colors[(Integer)colors.elementAt(1)][0];
            G4 = G5 = G6 = this.colors[(Integer)colors.elementAt(1)][1];
            B4 = B5 = B6 = this.colors[(Integer)colors.elementAt(1)][2];
        }else if(colors.size() == 3){
            R = R2 = this.colors[(Integer)colors.elementAt(0)][0];
            G = G2 = this.colors[(Integer)colors.elementAt(0)][1];
            B = B2 = this.colors[(Integer)colors.elementAt(0)][2];
            R3 = R4 = this.colors[(Integer)colors.elementAt(1)][0];
            G3 = G4 = this.colors[(Integer)colors.elementAt(1)][1];
            B3 = B4 = this.colors[(Integer)colors.elementAt(1)][2];
            R5 = R6 = this.colors[(Integer)colors.elementAt(2)][0];
            G5 = G6 = this.colors[(Integer)colors.elementAt(2)][1];
            B5 = B6 = this.colors[(Integer)colors.elementAt(2)][2];
        }else if(colors.size() == 4){
            R2 = this.colors[(Integer)colors.elementAt(0)][0];
            G2 = this.colors[(Integer)colors.elementAt(0)][1];
            B2 = this.colors[(Integer)colors.elementAt(0)][2];
            R3 = this.colors[(Integer)colors.elementAt(1)][0];
            G3 = this.colors[(Integer)colors.elementAt(1)][1];
            B3 = this.colors[(Integer)colors.elementAt(1)][2];
            R5 = this.colors[(Integer)colors.elementAt(2)][0];
            G5 = this.colors[(Integer)colors.elementAt(2)][1];
            B5 = this.colors[(Integer)colors.elementAt(2)][2];
            R6 = this.colors[(Integer)colors.elementAt(3)][0];
            G6 = this.colors[(Integer)colors.elementAt(3)][1];
            B6 = this.colors[(Integer)colors.elementAt(3)][2];
        }
        
        p = size;
        System.out.println("Colores"+ R + " " + G + " " + B);
        
        p1 = new Point3f(-p, -p, -p);
	p2 = new Point3f(p, -p, -p);
	p3 = new Point3f(-p, -p, p);
	p4 = new Point3f(-p, p, -p);
	p5 = new Point3f(p, p, -p);
	p6 = new Point3f(-p, p, p);
	p7 = new Point3f(p, -p, p);
	p8 = new Point3f(p, p, p);
        
        verts = new Point3f[]{
	p1, p2, p7,	p3,   // negative y
	p1, p4, p5, p2,	// negative z
	p1, p3, p6, p4,	// negative x
	p8, p5, p4, p6,	// positive y
	p8, p7, p2, p5,	// positive x
	p8, p6, p3, p7,	// positive z
        };
        
        Color3f sidecolor[] = {
                //ABAJO
		new Color3f(R, G, B),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //ATRAS
                new Color3f(R2, G2, B2),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //IZQ
                new Color3f(R3, G3, B3),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //ARRIBA
                new Color3f(R4, G4, B4),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //DER
                new Color3f(R5, G5, B5),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
                
                //FRENTE
                new Color3f(R6, G6, B6),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f),
		new Color3f(1.0f, 1.0f, 1.0f)};
        
        
        
	quaddy = new QuadArray(24, GeometryArray.COORDINATES 
	 |	GeometryArray.NORMALS | GeometryArray.COLOR_3 | GeometryArray.ALLOW_COLOR_WRITE
 | GeometryArray.TEXTURE_COORDINATE_2);
	quaddy.setCoordinates(0, verts);
       for (i = 0; i < 4; i++) {
            quaddy.setTextureCoordinate(0, i, texCoord[i%4]);
        }
 	setAppearance(new Appearance());
   quaddy.setColors(0, sidecolor);
for (int x = 0; x < 4; x++)
		{
			quaddy.setNormal(x, new Vector3f(0f, -1f, 0f));
	    }
for (int x = 4; x < 8; x++)
		{
			quaddy.setNormal(x, new Vector3f(0f, 0f, -1f));
	    }
for (int x = 8; x < 12; x++)
		{
			quaddy.setNormal(x, new Vector3f(-1f, 0f, 0f));
	    }
for (int x = 12; x < 16; x++)
		{
			quaddy.setNormal(x, new Vector3f(0f, 1f, 0f));
	    }
for (int x = 16; x < 20; x++)
		{
			quaddy.setNormal(x, new Vector3f(1f, 0f, 0f));
	    }
for (int x = 20; x < 24; x++)
		{
			quaddy.setNormal(x, new Vector3f(0f, 0f, 1f));
	    }		 		 		 		 
	this.setGeometry(quaddy);
	//this.setAppearance(new Appearance());
    }//Fin de Cubo
	 QuadArray getQuadArray()
	 {
	 		return quaddy;
	}
         
         String getCoord(){
             return coord;
         }

}
