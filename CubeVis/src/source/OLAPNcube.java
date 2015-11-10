package source;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.font.FontRenderContext;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.BoxLayout;

/**
 *
 * @author Galia
 */
public class OLAPNcube extends JPanel  {
  
    private static final int PWIDTH = 800;   // Width Panel
    private static final int PHEIGHT = 800;  // Height Panel
    private int var_altura=800;
    private int var_ancho=1200;   
    private Panel pnlCtl;                    //pnlCentro;
    private DrawPanel pnlCentro;
    private Label  lblProj,lbl_encabezado, lbl_center,Label1,label1,label2,label3;
    private int altoDim, anchoDim;
    private int tam_rect=15; //Size of rectangles
    private int pos_xpc=15, pos_ypc=70, pos_xsc=320, pos_ysc=70, tam_pc=90, pos_px2, pos_py2;
    private int pos_xdesr=30, pos_ydesr=380, tam_desr=140;
    private int dist_relation=100+tam_pc;
    private int i = 1;
    private int[] salidaT;
    private DetailPanel pnlSur;   
    private Graphics g;
    private Ellipse2D ovalInfo;
    private Rectangle2D rectInfo;
    private String str_posicion="1", str_cp="0";
    private int valTri[][];
    private HashMap val_prin;
    private ArrayList<Integer> intList = new ArrayList<Integer>();
    private ArrayList<Integer> val_prec = new ArrayList<Integer>();
    private ArrayList<Integer> intList_int = new ArrayList<Integer>();
    private int can_dime;
    private Vector DimVectorN;
    private Color[] c = {Color.GRAY, Color.RED, Color.BLUE, Color.YELLOW, 
                 Color.GREEN, Color.MAGENTA,Color.CYAN,Color.PINK,
                 Color.ORANGE,new Color(75,0,130)};
    public Color refColors[][];
    private Color matrizColores[][]= new Color[20][21];
    private Color color_borde=Color.white;
    private Color color_font;
    private int pos_cird=215;  //Position circle total and description
    public int rect_seleccionado;
    public String valores_dibujar, valores_dibujar_dos;
    public Vector medSize, vectorInicial, vectorFinal, vectorCube, vectorInicialDim, vectorFinalDim;
    public Font fuenteEncabezado, fuenteDatos,fuenteRelaciones; 
    public int fuenteSizeEncabezado, fuenteSizeDatos,fuenteSizeRelaciones;
    
    
    private int var_segr=0;
    JButton button;
    Color co=Color.black;  //Color de relleno del ovalo
    Color str_co=Color.BLACK;
    Color[] c0 = { new Color(112,128,144),new Color(169,169,169),new Color(211,211,211),new Color(245,245,245)};  //1-Gris
    Color[] c1 = {new Color(139,0,0), new Color(178,34,34), new Color(255,0,0),new Color(255,99,71)};    //2  Rojo
    Color[] c2 = {new Color(65,105,225), new Color(135,206,250), new Color(30,144,255),new Color(70,130,180)}; //Azul
    Color[] c3 = {new Color(255,215,0), new Color(255,140,0), new Color(240,230,140),new Color(218,165,32)}; //Amarillo
    Color[] c4 = {new Color(0,128,0), new Color(50,205,50), new Color(0,100,0),new Color(144,238,144)}; //Verde       
    Color[] c6 = {new Color(0,255,255), new Color(0,206,209), new Color(127,255,212), new Color(176,224,230)};
    Color[] c5 = {new Color(255,0,255), new Color(218,112,214), new Color(199,21,133), new Color(219,112,147)};
    Color[] c7 = {new Color(139,69,19), new Color(210,105,30), new Color(244,164,96), new Color(210,180,140)};   
    String[] array_valores_dibujar ;
    String[] array_valores_dibujar_dos ;
    Rectangle2D rect;
    ArrayList<Rectangle2D> rectangleList = new ArrayList<Rectangle2D>();
     ArrayList<Rectangle2D> rectangleLista = new ArrayList<Rectangle2D>();
    JPanel pane;
    
    String[] TEST = {   "Hypothesis testing of a single population mean",
                    "Hypothesis testing of a single population proportion",
                    "Hypothesis testing of the difference between two population means",
                    "Hypothesis testing of the difference between two population proportions",
                    "Hypothesis testing of a single population variance",
                    "Hypothesis testing of the ratio of two population variances",
                    "Hypothesis tests with chi-square"
};

    
    
    
     public OLAPNcube(Vector dimSize, Vector DVector, Vector vectorInicial1, Vector vectorFinal1, int testO)
     {
       
         String nombre_test=TEST[0];
         //Color Matriz. Current 10 colors 10 subcolors.
         
           //GRAY COLOR
              matrizColores[0][0]=Color.GRAY;
                matrizColores[0][1]=new Color(32,32,32);
                matrizColores[0][2]=new Color(56,56,56);
                matrizColores[0][3]=new Color(80,80,80);
                matrizColores[0][4]=new Color(104,104,104);
                matrizColores[0][5]=new Color(128,128,128);
                matrizColores[0][6]=new Color(152,152,152);
                matrizColores[0][7]=new Color(176,176,176);
                matrizColores[0][8]=new Color(192,192,192);
                matrizColores[0][9]=new Color(216,216,216);
         //RED COLOR 
              matrizColores[0][0]=Color.RED;
                matrizColores[1][1]=new Color(56,0,0);
                matrizColores[1][2]=new Color(80,0,0);
                matrizColores[1][3]=new Color(104,0,0);
                matrizColores[1][4]=new Color(128,0,0);
                matrizColores[1][5]=new Color(152,0,0);
                matrizColores[1][6]=new Color(176,0,0);
                matrizColores[1][7]=new Color(200,0,0);
                matrizColores[1][8]=new Color(224,0,0);
                matrizColores[1][9]=new Color(255,0,0);
         //BLUE COLOR       
             matrizColores[2][0]=Color.BLUE;
                matrizColores[2][1]=new Color(0,0,255);
                matrizColores[2][2]=new Color(0,0,205);
                matrizColores[2][3]=new Color(0,0,128);
                matrizColores[2][4]=new Color(0,0,171);
                matrizColores[2][5]=new Color(67,110,238);
                matrizColores[2][6]=new Color(176,196,222);
                matrizColores[2][7]=new Color(162,181,205);
                matrizColores[2][8]=new Color(198,226,255);
                matrizColores[2][9]=new Color(104,131,139);   
         //YELLOW COLOR       
             matrizColores[3][0]=Color.YELLOW;
                matrizColores[3][1]=new Color(255,255,0);
                matrizColores[3][2]=new Color(205,205,0);
                matrizColores[3][3]=new Color(128,128,0);
                matrizColores[3][4]=new Color(238,230,133);
                matrizColores[3][5]=new Color(227,207,87);
                matrizColores[3][6]=new Color(255,193,37);
                matrizColores[3][7]=new Color(184,134,11);
                matrizColores[3][8]=new Color(255,165,0);
                matrizColores[3][9]=new Color(139,90,0);   
         //GREEN COLOR       
               matrizColores[4][0]=Color.GREEN;
                matrizColores[4][1]=new Color(0,255,127);
                matrizColores[4][2]=new Color(0,238,118);
                matrizColores[4][3]=new Color(0,205,102);
                matrizColores[4][4]=new Color(0,13,69);
                matrizColores[4][5]=new Color(60,179,113);
                matrizColores[4][6]=new Color(84,255,159);
                matrizColores[4][7]=new Color(78,238,148);
                matrizColores[4][8]=new Color(67,205,128);
                matrizColores[4][9]=new Color(46,139,87);    
                 //MAGENTA COLOR       
               matrizColores[5][0]=Color.MAGENTA;
                matrizColores[5][1]=new Color(255,0,255);
                matrizColores[5][2]=new Color(238,0,238);
                matrizColores[5][3]=new Color(205,0,205);
                matrizColores[5][4]=new Color(139,0,139);
                matrizColores[5][5]=new Color(238,130,238);
                matrizColores[5][6]=new Color(139,102,139);
                matrizColores[5][7]=new Color(221,160,221);
                matrizColores[5][8]=new Color(255,131,250);
                matrizColores[5][9]=new Color(255,131,250);   
               //CYAN COLOR       
               matrizColores[6][0]=Color.cyan;
                matrizColores[6][1]=new Color(0,245,255);
                matrizColores[6][2]=new Color(0,229,238);
                matrizColores[6][3]=new Color(0,197,205);
                matrizColores[6][4]=new Color(0,134,139);
                matrizColores[6][5]=new Color(187,255,255);
                matrizColores[6][6]=new Color(150,205,205);
                matrizColores[6][7]=new Color(209,238,238);
                matrizColores[6][8]=new Color(180,205,205);
                matrizColores[6][9]=new Color(122,139,139);    
                //PINK COLOR     
                              matrizColores[7][0]=Color.cyan;
                matrizColores[7][1]=new Color(255,182,193);
                matrizColores[7][2]=new Color(238,162,173);
                matrizColores[7][3]=new Color(205,140,149);
                matrizColores[7][4]=new Color(255,192,203);
                matrizColores[7][5]=new Color(238,169,184);
                matrizColores[7][6]=new Color(219,112,147);
                matrizColores[7][7]=new Color(238,121,159);
                matrizColores[7][8]=new Color(139,71,93);
                matrizColores[7][9]=new Color(139,34,82);
                //ORANGE COLOR
                matrizColores[8][0]=Color.ORANGE;
                matrizColores[8][1]=new Color(255,69,0);
                matrizColores[8][2]=new Color(205,55,0);
                matrizColores[8][3]=new Color(139,37,0);
                matrizColores[8][4]=new Color(255,130,71);
                matrizColores[8][5]=new Color(205,104,57);
                matrizColores[8][6]=new Color(210,105,30);
                matrizColores[8][7]=new Color(255,127,36);
                matrizColores[8][8]=new Color(205,102,29);
                matrizColores[8][9]=new Color(138,54,15);
                //PURPLE COLOR
                matrizColores[9][1]=new Color(104,34,139);
                matrizColores[9][2]=new Color(154,50,205);
                matrizColores[9][3]=new Color(191,62,255);
                matrizColores[9][4]=new Color(85,26,139);
                matrizColores[9][5]=new Color(147,112,219);
                matrizColores[9][6]=new Color(159,121,238);
                matrizColores[9][7]=new Color(147,112,219);
                matrizColores[9][8]=new Color(186,85,211);
                matrizColores[9][9]=new Color(209,95,238);
                
                
         DimVectorN = DVector;
         medSize=dimSize;       
         vectorInicial =vectorInicial1;
         vectorFinal = vectorFinal1;
         fuenteSizeEncabezado=20;
         fuenteSizeDatos=14;
         fuenteSizeRelaciones=12;
         fuenteEncabezado=new Font("TimesRoman", Font.PLAIN, fuenteSizeEncabezado);
         fuenteDatos=new Font("TimesRoman", Font.PLAIN, fuenteSizeDatos);
         fuenteRelaciones=new Font("TimesRoman", Font.PLAIN, fuenteSizeRelaciones);
 
         setLayout( new GridBagLayout() );
         GridBagConstraints c = new GridBagConstraints();
         setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        
        lbl_encabezado = new Label("Dimension Size:"+dimSize.size()+ "     Test:  "+nombre_test);
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;//Celda 00
        c.gridy = 0;//Celda 00
        c.gridwidth=3;
        c.gridheight=1;
        c.weighty=0.0;
        c.weightx=0.5;
        c.anchor=GridBagConstraints.CENTER;
        add(lbl_encabezado,c);
        //pane.add(lbl_encabezado, c);
        
        /*Centro informacion */       
        /*Triangulo de Pascal para obtener ancho y alto de puntos*/          
         salidaT = PascalTriangle(dimSize.size());

         //Obteniendo el alto del elemento
           altoDim = salidaT.length;
        
        //Obteniendo el ancho del elemento
          int max = salidaT[0];
          for ( int i = 1; i <salidaT.length; i++) {
            if ( salidaT[i] > max) {
            max = salidaT[i];
            }
           }      
               
           anchoDim = max;
           //System.out.println("El alto es:"+altoDim+"El ancho es: "+anchoDim)
        /*Fin del triangulo de Pascal */
        
        /*Estableciendo el panel del centro */        
        pnlCentro = new DrawPanel(dimSize.size(),altoDim,anchoDim, salidaT);
        pnlCentro.setBackground(Color.white);
        can_dime=dimSize.size();
        c.fill = GridBagConstraints.BOTH;
        c.gridx=0;
        c.gridy=1;
        c.gridwidth = 1;
        c.gridheight=1;
        c.weighty=1;
        add(pnlCentro,c);
    
        pnlSur = new DetailPanel();
        pnlSur.setBackground(Color.WHITE);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight=1;
        c.weighty=1;
       // pane.add(pnlSur,c);
       this.add(pnlSur,c);                   
     }
    
   
//Metod to stablish nodes in each line     
    public static int[] PascalTriangle(int n)
    {
        int[] pt = new int[n+1];
        if(n == 0)
          {
            pt[0] = 1;
            return pt;
          }
        int[] ppt = PascalTriangle(n-1);
        pt[0] = pt[n] = 1;
        for(int i = 1; i < ppt.length; i++)
          {
            pt[i] = ppt[i-1] + ppt[i];
          }
        return pt;
    }   
    

    public void Predecesor(int valor, int dimensiones, int col_valprint)
    {      
      int con_valprin=col_valprint;
      int[] arr_tmp;
      int[] arr_dim = new int[50];
      int ver_comp,sal_xor;
      int verifica=0;
    
       for(int rt=0;rt<dimensiones;rt++)
          {
           ver_comp = (int)Math.pow(2, rt);
           sal_xor=(int)(valor ^ ver_comp);
      
           //System.out.println("Valor de entrada: "+valor+" Valor de comparacion: "+ver_comp+" Salida: "+sal_xor);
           
           if(sal_xor<valor)
           {
           //System.out.println("   Valor de sal_xor es:"+sal_xor);
           
             verifica=0;
             for(int rt1=0;rt1<dimensiones;rt1++)
             {
              //System.out.println("   Comparando val_xor: "+sal_xor +" con: "+(int)Math.pow(2, rt1));
              if((int)Math.pow(2, rt1)==sal_xor)
              {
              verifica=1;
              //System.out.println("   Iguales--  Verifica es:"+verifica +" Comparando val_xor: "+sal_xor +" con: "+(int)Math.pow(2, rt1));
              }
             }
              
              
              if(verifica == 1)
              {
              con_valprin++;    
              //System.out.println(" ****  Este valor es principal: "+sal_xor+"   Valor de contador: "+con_valprin);
             
              //if(!val_prin.containsValue(sal_xor))
              if(!intList.contains(sal_xor))
              {
              //System.out.println("   Valor de contador: "+con_valprin+" Sal_xor"+sal_xor);
               intList.add(sal_xor);
              
              }
               if(!val_prec.contains(sal_xor))
               {
               val_prec.add(sal_xor);
               }
              
              }
              else if(sal_xor!=0)
              {
                // System.out.println("    Llamando nuevamente a la funcion: sal_xor:"+sal_xor+" Valor de contador: "+con_valprin);
              if(!val_prec.contains(sal_xor))
               {
               val_prec.add(sal_xor);
               }
                  Predecesor(sal_xor,dimensiones,con_valprin);                  
              }               
         }
           
      }
    }
    
    
    public class DetailPanel extends JPanel implements MouseListener, MouseMotionListener
    {
    int reb,ang;
    //Variables del repaint 
    Color col_rec=Color.white;
    Color col_base=Color.white;
    String cad_relacion="";
    int var_repaint=0;
    int var_repaint_dos=0;
    
    //Constructor
    DetailPanel(){
    addMouseListener(this);
    addMouseMotionListener(this);
    }
    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(600, 700);     
        Color col_rec=Color.white;
      
        
        g.setColor(Color.LIGHT_GRAY);
        g.fill3DRect(10, 2, 3, 700,false);
        
        if(var_repaint ==0)
        {
        //System.out.println("Var Repaint es"+var_repaint);
        g.setColor(Color.LIGHT_GRAY);
        g.fill3DRect(10, 2, 3, 700,false);
        }
        else
        {
        g.setColor(Color.LIGHT_GRAY);
        g.fill3DRect(10, 2, 3, 700,false);
        //System.out.println("Var Repaint es"+var_repaint);
         
        if(var_repaint == 1)
        {
         g.setColor(Color.GRAY);   
         g.setFont(fuenteDatos);
         g.drawString("Current relation:"+cad_relacion, 240, pos_ypc+dist_relation);//Cadena que imprime la relacion  actual
       
         //Dibujando encabezado
         g.setFont(fuenteEncabezado);  
         g.setColor(Color.DARK_GRAY);
         g.drawString("Relations description",pos_xdesr , pos_ydesr-20); 
        }
       }
        
       reb = Integer.parseInt(str_cp);  
      //System.out.println("Valor de reb aqui:"+reb);
      
       
       if(reb == 0)
       {
         ang=360;
       }    
       else
       {
       ang=360/reb;
       }
       
        g.setColor(co);

      int j=0;
      int loga=0;
      int actual=0;
      int con_col=0;
             
             
             Iterator<Integer> i = intList.iterator();
             loga=log(Integer.parseInt(str_posicion),2);
             g.setColor(c[loga]);
             g.fillOval(pos_cird, pos_ypc, tam_pc, tam_pc);
             
             
             while( i.hasNext() ){
             actual=i.next();
             loga=log(actual,2);
             g.setColor(c[loga]);
             //g.setColor(Color.pink);
             g.fillArc(pos_cird,pos_ypc, tam_pc, tam_pc, j*ang, ang); //Descripcion_dimensiones
           
             
             j++;
             con_col++;
             System.out.println("Elementos del iterator dibujando"+ actual+"Logaritmo actual"+loga );
             }
           
             g.setColor(Color.LIGHT_GRAY);
             g.fill3DRect(5, pos_ypc+dist_relation, 590,5, true);
             g.setColor(Color.DARK_GRAY);
             g.setFont(fuenteEncabezado);
             g.drawString("Relations", 15, pos_ypc+dist_relation);
             System.out.println("Valor del iterator intList primero"+intList.size());
             
   
             //Metod to stablish the values of dumensions
             Iterator<Integer> i2 = intList.iterator();
             j=0;
             String cad_varC;
             int start_angle=360;
             vectorInicialDim =    vectorInicial;    
             vectorFinalDim = vectorFinal;
            /* VGH recorrer los elemntos del vector start and finish */
                
                for(int x=0; x < vectorInicial.size(); x++)
                {
                System.out.println("Valores de start dentro de OLAPnCUBE:"+(String)vectorInicial.elementAt(x));               
                }
                
                for(int x=0; x < vectorFinal.size(); x++)
                {
                System.out.println("Valores de finish dentro de OLAPnCUBE:"+(String)vectorFinal.elementAt(x));                
                }
             
                //Cada uno de los cuboides representa uno de los cuboides, por lo que es necesario establecer las relaciones entre 
                //los mismos
                
                
                
                
                
                //Eliminando elementos duplicados de start
                 for( int s=0; s < vectorInicial.size(); s++){
                    String elementS = (String)vectorInicial.elementAt(s);
                    //String elementF = (String)vectorInicial.elementAt(s+1);
                    for(int u=(s+1); u < vectorInicial.size(); u++){
                        if(elementS.equals((String)vectorInicial.elementAt(u))){
                            vectorInicial.remove(u);
                            //vectorFinal.remove(u);
                        }
                    }
                }   
                
                 
                //Eliminando elementos duplicados de finish
                 for( int s=0; s < vectorFinal.size(); s++){
                    String elementS = (String)vectorFinal.elementAt(s);
                    //String elementF = (String)vectorInicial.elementAt(s+1);
                    for(int u=(s+1); u < vectorFinal.size(); u++){
                        if(elementS.equals((String)vectorFinal.elementAt(u))){
                            vectorFinal.remove(u);
                            //vectorFinal.remove(u);
                        }
                    }
                }  
                 
                
                   //Elimina duplicados de start y finish
                for( int s=0; s < vectorInicial.size(); s++){
                    String elementS = (String)vectorInicial.elementAt(s);
                   // String elementF = (String)vectorFinal.elementAt(s);
                    for(int u=0; u < vectorFinal.size(); u++){
                        if(elementS.equals((String)vectorFinal.elementAt(u))){
                            //vectorInicial.remove(u);
                            vectorFinal.remove(u);
                        }
                    }
                }     
                
                int z=0;                             
                int total_relaciones=vectorInicial.size()+vectorFinal.size();
             
             
             
             if(con_col == medSize.size())
             {
             System.out.println("Seleccione el que tiene todas las dimensiones");
             var_repaint =0;
             var_repaint_dos =0;  
             //Obteniendo cuantas medidas tiene cada dimension.
             int con_int=0;
             int inc_x=5;
             int ori_x=pos_xsc;
             int ori_y=pos_ysc;
             int sum_anginterno=0;
             
             int pos_py=pos_ypc+12;   //Posicion de circulo pequeno en y
             int pos_px;   //Posicion de circulo pequeno en y
             
             while( i2.hasNext() ){
             pos_px=pos_xsc+tam_pc+15;
             pos_px2=pos_px;    
             actual=i2.next();
             loga=log(actual,2);
             g.setColor(Color.BLACK);        
             //System.out.println("La dimension actual es:"+i2);
            int ang_ini=0;
             int ang_ini2=0;
            int pos_x, dist_x, cp_x;
            int pos_y, dist_y, cp_y;  
            int anc_x=90;
            int anc_y=80;
            int inc_y=0;
             pos_x=220;
             pos_y=25;             
             inc_x+=5;
              
            for(int d=0;d<(Integer)medSize.get(con_int);d++)
             {
             //Cantidad de pixeles de separacion
             cp_x=5;
             cp_y=10;    
                                 
             dist_x=d*cp_x;
             dist_y=d*cp_y;
                                  
             pos_x=ori_x+anc_x+inc_x;
             pos_y=ori_y+inc_y;
                
      
             //Draw new nodo wit information about dimension
             int ang_int=ang/(Integer)medSize.get(con_int);
             
             if(sum_anginterno < 90)  //Ubicados en el cuarto cuadrante
             {
              pos_x=ori_x+anc_x+10;
              pos_y=ori_y+dist_y+40;
             }
             else if(sum_anginterno < 180)
             {
             pos_x=ori_x;
             pos_y=ori_y+anc_x-dist_y;
             }
             
             else {
             pos_x=ori_x+200;
             pos_y=ori_y+200;
             }
             
             sum_anginterno+= ang_int;
             System.out.println("**Su medida es:"+d+"Valor de cont_int"+con_int);
             System.out.println("**Valor de angulo interno (ang_int)"+ang_int+"Valor de angulo inicial (ang_ini)"+ang_ini);
 
            /* if(con_int == 0){g.setColor(c0[d]);}
             else if(con_int == 1 ){g.setColor(c1[d]);}
             else if(con_int == 2 ){g.setColor(c2[d]);}
             else if(con_int == 3 ){g.setColor(c3[d]);}
             else if(con_int == 4 ){g.setColor(c4[d]);}
             else if(con_int == 5 ){g.setColor(c5[d]);}
             else if(con_int == 6 ){g.setColor(c6[d]);}
             else if(con_int == 7 ){g.setColor(c7[d]);}*/
             
             g.setColor(matrizColores[con_int][d+1]);
             
             if(d == (Integer)medSize.get(con_int)-1){ ang_ini=start_angle;  }
             else { ang_ini=start_angle;}
             ang_ini=start_angle;
             g.fillArc(ori_x, ori_y, 90, 90, ang_ini, -ang_int);//Nodo todasSubdimensiones
             //System.out.println("Posicion de cir peq esx es:"+pos_px+" La posicion en y es"+pos_py);
 
             g.fillOval(pos_px+12, pos_py, 17, 17); //Nodos informativos de colores
             g.setFont(fuenteDatos);
             g.setColor(Color.BLACK);
             g.drawString(Integer.toString(d), pos_px+17, pos_py+13);
             
             pos_px+=18;
             start_angle-=ang_int;
             //System.out.println("El angulo es: "+ang_int);
             inc_y+=10;
             }
             j++;
             con_int++;
             pos_py+=18;
             }
             
                int rel_x=20;
                int rel_y=pos_ypc+dist_relation+10;
                int str_relaciones=total_relaciones;
                String str_canrel=Integer.toString(str_relaciones);
                g.setFont(fuenteDatos);
                g.drawString("Cantidad de cuboides:"+str_canrel, 100, pos_ypc+dist_relation);
                for(int f=0,ii=1;f<total_relaciones;f++,ii++)
                {
                   
                    Rectangle2D r = new Rectangle2D.Double(rel_x, rel_y, tam_rect, tam_rect);
                    rectangleList.add(r);
                  //Rectangle array                  
                   g.setColor(Color.PINK);  
                   g.drawRect(rel_x, rel_y, tam_rect+5, tam_rect+5); 
                   g.fillRect(rel_x, rel_y, tam_rect, tam_rect);
                   g.setColor(Color.DARK_GRAY);
                   g.setFont(fuenteRelaciones);
                   g.drawString(Integer.toString(ii), rel_x+4, rel_y+12);
                   rel_x+=25;                 
                }
                rel_x=20;
                rel_y=pos_ypc+dist_relation+25;
                 
                for(int rd=0;rd<vectorInicialDim.size();rd++)
                 {
                   Rectangle2D red = new Rectangle2D.Double(rel_x, rel_y+10, tam_rect, tam_rect);
                   rectangleLista.add(red);
                   g.setColor(Color.BLUE); 
                   g.drawRect(rel_x, rel_y+10, tam_rect+5, tam_rect+5); 
                   g.fillRect(rel_x, rel_y+10, tam_rect, tam_rect);
                   g.setColor(Color.LIGHT_GRAY);
                   g.drawString(Integer.toString(rd), rel_x+4, rel_y+22);
                   rel_x+=25;  
                 }
                
                
                //Dibujando cuadros que establecen las relaciones entre pares de dimensiones
                
                
             } // Fin de Impresion si estamos usando el nodo con todas las dimensiones

        g.setColor(str_co);
        g.setFont(fuenteEncabezado);
        g.drawString("Information about Node actual", 300, 20);
        g.setFont(fuenteDatos);
        g.drawString("Current node:"+str_posicion, 300+5, 20+5+fuenteSizeDatos);
        g.drawString("Dimension size:"+str_cp, 300+5, 20+5+fuenteSizeDatos+fuenteSizeDatos);
   
        g.setFont(fuenteEncabezado);
        g.drawString("Information about Dimensions", 15, 20);
        g.setFont(fuenteDatos);
        g.drawString("All dimensions and name:",10+10, 20+5+fuenteSizeDatos);

       
        
        
        
        
        //Drawing detail of dimension
        
        int pos_dx=pos_xpc+tam_pc+10;
        int pos_dy=pos_ypc-10;
        
        int pos_dx2=220;
        int pos_dy2=25;
        
        int cam_pos=20;
        int anc_des=15;
        String cad_info, cad_res;
        
        int sta_ang=0;
        int can_ang=360/can_dime;
        int v,r,s;
 
        
      
        
        
        if(var_repaint == 1){
            //De acuerdo a la relacion seleccionada, es necesario establecer sus dimendiones
            //Descripción de los cuboides.
           if(var_repaint_dos == 0)
           {
            
            
            for( r=0;r<vectorInicial.size();r++)
             {
               if(r ==rect_seleccionado )
               {
               System.out.println("El rectangulo es: "+r  +"Vector Inicial");
               valores_dibujar=vectorInicial.get(r).toString();
               }                
             }
           
              //Second vector
              for( s=0;r<vectorFinal.size()+vectorInicial.size();r++,s++)
             {
              // System.out.println("Valor de r dentro del segundo for"+r+ " Valor de rect_seleccionado"+rect_seleccionado);  
               if(r ==rect_seleccionado )
               {
               System.out.println("El rectangulo es: "+r  +"Vector Final");
               valores_dibujar=vectorFinal.get(s).toString();
               }
                 
             }
              
             System.out.println("***Las relaciones a establecer son:"+valores_dibujar); 
             array_valores_dibujar=valores_dibujar.split("-");
           }
           else if(var_repaint_dos == 1 ){ //Es necesario dibujar dos cuboides con relacion de dimensiones
           
           System.out.println("***Estoy dentro de dos nodos a dibujar"); 
           System.out.println("***El rectangulo seleccionado es:"+rect_seleccionado);
          
           //Primer cuboide
            for( r=0;r<vectorInicialDim.size();r++)
             {
               if(r ==rect_seleccionado )
               {
               System.out.println("El rectangulo es: "+r  +"Vector Inicial");
               valores_dibujar=vectorInicialDim.get(r).toString();
               }                
             }
           
                   //Segundo cuboide
            for( r=0;r<vectorFinalDim.size();r++)
             {
               if(r ==rect_seleccionado )
               {
               System.out.println("El rectangulo es: "+r  +"Vector Final");
               valores_dibujar_dos=vectorFinalDim.get(r).toString();
               }                
             }
            
            System.out.println("***Las relaciones a establecer son:"+valores_dibujar); 
             array_valores_dibujar=valores_dibujar.split("-");
             array_valores_dibujar_dos=valores_dibujar_dos.split("-");
           
           
           
           }  
        }//End stablish elements of dimension
        
        
        int pos_py=pos_ydesr;   //Pos circle_y
        int pos_px; 
        int pos_py2=pos_ypc+12;
             
        int con_int=0;
        start_angle=360;
        int ang_ini=start_angle;
        int ang_ini2=start_angle;
        int seg_nodo=290;  //Valor x del segundo nodo
        
        if(var_repaint ==1){
          g.setColor(Color.BLACK);
          g.fillOval(pos_xdesr, pos_ydesr, tam_desr, tam_desr);
          if(var_repaint_dos == 1 && var_segr == 1){
               
          g.fillOval(pos_xdesr+seg_nodo, pos_ydesr, tam_desr, tam_desr);
               
          }
          
          
        }
        for( z=0,v=1;z<can_dime;z++,v++)
        {
        pos_px=pos_xdesr+tam_desr;
        pos_px2=pos_xsc+tam_pc+15;
        cad_info="";    
        g.setColor(c[z]);
        pos_dy+=cam_pos;
        g.drawOval(pos_dx,pos_dy , anc_des, anc_des);
        g.fillOval(pos_dx,pos_dy , anc_des, anc_des);
 
        //Dibujando el ovalo principal con dimensiones
        g.fillArc(pos_xpc, pos_ypc, tam_pc, tam_pc, -can_ang*z,-can_ang ); //descripcionDimFijo
        
        //Dibujando el segundo ovalo derecho con dimensiones
        if(var_repaint == 1){
           
        g.fillArc(pos_cird, pos_ypc, tam_pc, tam_pc, -can_ang*z,-can_ang ); //Dibujando el circulo con todas las dimensiones
        //Dibujando la descripcion de las dimensiones
         
         g.setColor(c[z]);
         cad_res="("+Integer.toString((int)Math.pow(2, z))+")"; //Colores_dimensiones
         g.drawString(cad_res, pos_px+10,pos_py+12 );       
         g.fillOval(pos_px+30, pos_py, 18, 18);
            
       }
        //Procedimiento para pintar los valores internos de las dimensiones.
        //En esta parte se debe hacer el mapeo con cada uno de los vectores para establecer las relaciones
        
      
        
        
        if(var_repaint == 1){
        //{        
        int ori_x=pos_xsc;
        int ori_y=pos_ysc;
        int anc_x=90;
            cad_res="("+Integer.toString((int)Math.pow(2, z))+")"; //Colores_dimensiones
         g.drawString(cad_res, pos_xsc+tam_pc+5,pos_py2+10 );  
        //System.out.println("El valor de z es: "+z+" Su valor de esa dimension es:"+(Integer)medSize.get(z));          
       
               
        for(int d=0;d<(Integer)medSize.get(z);d++)
             {
               //En este caso es necesario dibujar un nuevo nodo con los datos de las dimensiones
             int ang_int=ang/(Integer)medSize.get(con_int);
               
             //Drawing with all colors  
             /*if(con_int == 0){g.setColor(c0[d]);}
             else if(con_int == 1 ){g.setColor(c1[d]);}
             else if(con_int == 2 ){g.setColor(c2[d]);}
             else if(con_int == 3 ){g.setColor(c3[d]);}
             else if(con_int == 4 ){g.setColor(c4[d]);}
             else if(con_int == 5 ){g.setColor(c5[d]);}
             else if(con_int == 6 ){g.setColor(c6[d]);}
             else if(con_int == 7 ){g.setColor(c7[d]);}  */
               
             g.setColor(matrizColores[con_int][d+1]);               
             g.fillArc(ori_x, ori_y, 90, 90, -ang_ini, -ang_int);  
             g.fillOval(pos_px2+12, pos_py2, 17, 17); //Nodos informativos de colores
             g.setFont(fuenteDatos);
             g.setColor(Color.BLACK);
             g.drawString(Integer.toString(d), pos_px2+17, pos_py2+13);
             pos_px2+=18;
             //End drawing with all colors  
            
             ang_int=ang/(Integer)medSize.get(z); 
             
             //Estableciendo los colores
             color_font=Color.BLACK;
            if(Integer.parseInt(array_valores_dibujar[z])==d){
                g.setColor(matrizColores[z][d+1]);
            /* if(z == 0){g.setColor(c0[d]);}
             else if(z == 1 ){g.setColor(c1[d]);}
             else if(z == 2 ){g.setColor(c2[d]);}
             else if(z == 3 ){g.setColor(c3[d]);}
             else if(z == 4 ){g.setColor(c4[d]);}
             else if(z == 5 ){g.setColor(c5[d]);}
             else if(z == 6 ){g.setColor(c6[d]);}
             else if(z == 7 ){g.setColor(c7[d]);}*/
               }
               else
               {
                   g.setColor(Color.BLACK);
                   color_font=Color.LIGHT_GRAY;
               }
 
           
            
            
           
             //Information about relations
             g.fillArc(pos_xdesr, pos_ydesr, tam_desr, tam_desr, -ang_ini, -ang_int);//Subdimensiones_descripcion
             System.out.println("El valor del angulo inicial dentro es: "+ang_ini );//descripcionRela
                         
             g.fillOval(pos_px+60, pos_py, 17, 17);  //Nodos_subdimensiones_des
             g.setColor(color_font);
             g.setFont(fuenteDatos);
             g.drawString(Integer.toString(d), pos_px+65, pos_py+13);
            
             
              
             
             
             
             ang_ini+=ang_int;
             pos_px+=18;
            
             
             
             
             
             }//Fin del for para el primer ovalo
        
 /*        if(var_repaint_dos == 1)
         {
           for(int d=0;d<(Integer)medSize.get(z);d++)
             {
               //En este caso es necesario dibujar un nuevo nodo con los datos de las dimensiones
             int ang_int=ang/(Integer)medSize.get(con_int);
               
             //Drawing with all colors  
             /*if(con_int == 0){g.setColor(c0[d]);}
             else if(con_int == 1 ){g.setColor(c1[d]);}
             else if(con_int == 2 ){g.setColor(c2[d]);}
             else if(con_int == 3 ){g.setColor(c3[d]);}
             else if(con_int == 4 ){g.setColor(c4[d]);}
             else if(con_int == 5 ){g.setColor(c5[d]);}
             else if(con_int == 6 ){g.setColor(c6[d]);}
             else if(con_int == 7 ){g.setColor(c7[d]);}  */
               
            /* g.setColor(matrizColores[con_int][d+1]);               
             g.fillArc(ori_x, ori_y, 90, 90, -ang_ini, -ang_int);  
             g.fillOval(pos_px2+12, pos_py2, 17, 17); //Nodos informativos de colores
             g.setFont(fuenteDatos);
             g.setColor(Color.BLACK);
             g.drawString(Integer.toString(d), pos_px2+17, pos_py2+13);
             pos_px2+=18;
             //End drawing with all colors  */
            
       /*      ang_int=ang/(Integer)medSize.get(z); 
             
             //Estableciendo los colores
             color_font=Color.BLACK;
            if(Integer.parseInt(array_valores_dibujar_dos[z])==d){
                g.setColor(matrizColores[z][d+1]);
            /* if(z == 0){g.setColor(c0[d]);}
             else if(z == 1 ){g.setColor(c1[d]);}
             else if(z == 2 ){g.setColor(c2[d]);}
             else if(z == 3 ){g.setColor(c3[d]);}
             else if(z == 4 ){g.setColor(c4[d]);}
             else if(z == 5 ){g.setColor(c5[d]);}
             else if(z == 6 ){g.setColor(c6[d]);}
             else if(z == 7 ){g.setColor(c7[d]);}*/
       /*        }
               else
               {
                   g.setColor(Color.BLACK);
                   color_font=Color.LIGHT_GRAY;
               }
 
           
            
            
           
             //Information about relations
             g.fillArc(pos_xdesr+seg_nodo, pos_ydesr, tam_desr, tam_desr, -ang_ini, -ang_int);//Subdimensiones_descripcion
             System.out.println("El valor del angulo inicial dentro es: "+ang_ini );//descripcionRela
                         
             g.fillOval(pos_px+60+seg_nodo, pos_py, 17, 17);  //Nodos_subdimensiones_des
             g.setColor(color_font);
             g.setFont(fuenteDatos);
             g.drawString(Integer.toString(d), pos_px+65, pos_py+13);
            
             ang_ini+=ang_int;
             pos_px+=18;
             }//Fin del for para el segundo ovalo
         
         
         
         
         }//Fin de var_repaint_dos
        */
             
        
        
        
        pos_py+=18;
        pos_py2+=18;
       
        
     
        
         }    
           if(con_col == medSize.size())
        {
        cad_res="("+Integer.toString((int)Math.pow(2, z))+")"; //Colores_dimensiones
        g.drawString(cad_res, pos_xsc+tam_pc+5,pos_dy+10 );
        }
        
        g.setColor(Color.BLACK);
        cad_info="("+Integer.toString((int)Math.pow(2, z))+")  "+DimVectorN.get(z).toString(); //Nombre_dimensiones
        g.drawString(cad_info, pos_dx+20,pos_dy+10 );
      
        
        
        
       if(var_repaint_dos == 1 && var_segr == 1)
         {
        int ori_x=pos_xsc;
        int ori_y=pos_ysc;
        int anc_x=90;
             
             
             for(int d=0;d<(Integer)medSize.get(z);d++)
             {
               //En este caso es necesario dibujar un nuevo nodo con los datos de las dimensiones
             int ang_int2=ang/(Integer)medSize.get(con_int);
               
             //Drawing with all colors  
             /*if(con_int == 0){g.setColor(c0[d]);}
             else if(con_int == 1 ){g.setColor(c1[d]);}
             else if(con_int == 2 ){g.setColor(c2[d]);}
             else if(con_int == 3 ){g.setColor(c3[d]);}
             else if(con_int == 4 ){g.setColor(c4[d]);}
             else if(con_int == 5 ){g.setColor(c5[d]);}
             else if(con_int == 6 ){g.setColor(c6[d]);}
             else if(con_int == 7 ){g.setColor(c7[d]);}  */
               
             g.setColor(matrizColores[con_int][d+1]);               
             g.fillArc(ori_x, ori_y, 90, 90, -ang_ini2, -ang_int2);  
             g.fillOval(pos_px2+12+seg_nodo, pos_py2, 17, 17); //Nodos informativos de colores
             g.setFont(fuenteDatos);
             g.setColor(Color.BLACK);
             g.drawString(Integer.toString(d), pos_px2+17+seg_nodo, pos_py2+13);
             pos_px2+=18;
             //End drawing with all colors  */
            
            ang_int2=ang/(Integer)medSize.get(z); 
             
             //Estableciendo los colores
             color_font=Color.BLACK;
            if(Integer.parseInt(array_valores_dibujar_dos[z])==d){
                g.setColor(matrizColores[z][d+1]);
            /* if(z == 0){g.setColor(c0[d]);}
             else if(z == 1 ){g.setColor(c1[d]);}
             else if(z == 2 ){g.setColor(c2[d]);}
             else if(z == 3 ){g.setColor(c3[d]);}
             else if(z == 4 ){g.setColor(c4[d]);}
             else if(z == 5 ){g.setColor(c5[d]);}
             else if(z == 6 ){g.setColor(c6[d]);}
             else if(z == 7 ){g.setColor(c7[d]);}*/
               }
               else
               {
                   g.setColor(Color.BLACK);
                   color_font=Color.LIGHT_GRAY;
               }
 
           
            
            
           
             //Information about relations
             g.fillArc(pos_xdesr+seg_nodo, pos_ydesr, tam_desr, tam_desr, -ang_ini2, -ang_int2);//Subdimensiones_descripcion
             System.out.println("El valor del angulo inicial dentro es: "+ang_ini );//descripcionRela
             
             g.fillOval(pos_px+250, pos_py-18, 17, 17);  //Nodos_subdimensiones_des
             g.setColor(color_font);
             g.setFont(fuenteDatos);
             g.drawString(Integer.toString(d), pos_px+254, pos_py-4);
             
             
             
             
        
            
             ang_ini2+=ang_int2;
             pos_px+=18;
             }//Fin del for para el segundo ovalo
         
        if(con_col == medSize.size())
        {
        cad_res="("+Integer.toString((int)Math.pow(2, z))+")"; //Colores_dimensiones
     
            
            g.drawString(cad_res, pos_xsc+tam_pc+5,pos_dy+10 );
        }
        
        g.setColor(Color.BLACK);
        cad_info="("+Integer.toString((int)Math.pow(2, z))+")  "+DimVectorN.get(z).toString(); //Nombre_dimensiones
        g.drawString(cad_info, pos_dx+20,pos_dy+10);
         
         
         }//Fin de var_repaint_dos
        
         con_int++;
        
        
        
        
        //Fin de procedimiento de pintar las primeras dimensiones
        //Dibujando ovalo secundario primeras dimensiones
  
        
        } //Fin del For completo
        
         //System.out.println("Valor del nuevo iterator intList_int:--"+intList_int.size());        
        if(var_repaint == 1){   
             //System.out.println("Dentro de var_repaint 1 rect_seleccionado"+rect_seleccionado);         
             int pos_x=220;
             int pos_y=25;
             
             g.setColor(Color.GRAY);
             
             //Dibujando nuevamente los rectangulos
               int rel_x=20;
               int rel_y=pos_ypc+dist_relation+10;
                for(int f=0,ii=1;f<total_relaciones;f++,ii++)
                {
                  //Rectangle arrar                    
                   g.setColor(Color.ORANGE);  
                   g.drawRect(rel_x, rel_y, tam_rect+5, tam_rect+5);
                   g.fillRect(rel_x, rel_y, tam_rect, tam_rect);
                   g.setColor(Color.DARK_GRAY);
                   g.setFont(fuenteRelaciones);
                   g.drawString(Integer.toString(ii), rel_x+4, rel_y+12);
                  rel_x+=25;
                }
                
                rel_x=20;
                if(var_repaint_dos == 1){
                for(int rd=0;rd<vectorInicialDim.size();rd++)
                 {
                   //Rectangle2D red = new Rectangle2D.Double(rel_x, rel_y+37, tam_rect, tam_rect);
                   //rectangleLista.add(red);
                   g.setColor(Color.BLUE); 
                   g.drawRect(rel_x, rel_y+27, tam_rect+5, tam_rect+5); 
                   g.fillRect(rel_x, rel_y+27, tam_rect, tam_rect);
                   g.setColor(Color.LIGHT_GRAY);
                   g.drawString(Integer.toString(rd), rel_x+4, rel_y+22+15);
                   rel_x+=25;  
                 }
                
                
                }
                
                
        }
         intList.clear();
       
    }//Cierre de paint component

    public void pintarInfo(int componente)
    {
        super.paintComponent(g);
         Graphics2D g3d = (Graphics2D)g; 
         g3d.setColor(Color.black);
        g3d.setColor(Color.BLACK);
        g3d.drawOval((Integer)this.getHeight()/2, (Integer)this.getWidth()/2, 30, 30);
        g3d.setColor(Color.red);
        g3d.drawString(Integer.toString(componente), (Integer)this.getHeight()/2, (Integer)this.getWidth()/2);
        repaint();       
    }
    
    public int log(int x, int base)
    {
    return (int) (Math.log(x) / Math.log(base));
    }

        public void mouseClicked(MouseEvent e) {       
         System.out.println("Estoy dando clic en este panel");    
            
        for (Rectangle2D re : rectangleList) {
        if(re.contains(e.getX(), e.getY()))
        {
        System.out.println("Seleccionado"+re.toString()+"Indice de array rectangle: "+rectangleList.indexOf(re) );      
        rect_seleccionado=rectangleList.indexOf(re);
        col_rec=Color.BLACK;
        
        //Color de relleno del circulo base
        col_base=Color.black;
        
        //Cadena con informacion de relacion
        cad_relacion="Relation:"+rectangleList.indexOf(re);
        
        //Variable repaint
        var_repaint=1;
        var_repaint_dos=0;
       var_segr=0;
        //con_col=medSize.size();
        
        str_cp=str_cp;
        
        co=Color.PINK;
        
        intList_int=intList_int;
        c=c;
      
        repaint();
          } 
         }
        
        //Rectangulo azul de dimensiones
        for (Rectangle2D red : rectangleLista) {
        if(red.contains(e.getX(), e.getY()))
        {
       System.out.println("Seleccionado segundo"+red.toString()+"Indice de array rectangle segundo: "+rectangleLista.indexOf(red) ); 
            
            rect_seleccionado=rectangleLista.indexOf(red);
        col_rec=Color.BLACK;
        
        //Color de relleno del circulo base
        col_base=Color.black;
        
        //Cadena con informacion de relacion
        cad_relacion="Relation:"+rectangleLista.indexOf(red);
        
        //Variable repaint
        var_repaint=1;
        var_repaint_dos=1;
        //con_col=medSize.size();
        var_segr=1;
        
        str_cp=str_cp;
        
        co=Color.PINK;
        
        intList_int=intList_int;
        c=c;
      
        repaint();
            
            
        }
        }//Fin del for del segundo rectangulo
        
        }
        
        
        
        
        
        
        
        public void mousePressed(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void mouseReleased(MouseEvent e) {
           // throw new UnsupportedOperationException("Not supported yet.");
        }

        public void mouseEntered(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void mouseExited(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void mouseDragged(MouseEvent e) {
            // new UnsupportedOperationException("Not supported yet.");
        }

        public void mouseMoved(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    
    }
 
//Clase para dibujar el panel
public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener{

    int DIST_altura;
    int DIST_ancho;
    int MAX_ROW ;
    int MAX_COL;
    int i = 1,y,x;
    int[] salidaT1;
    int anc_oval;
    int alt_oval;
    int dimensiones;
    Color color1_deg,color2_deg;
    HashMap pos_x,pos_y,mapPosi,mapPosiOrd, val_prin1;
    int can_vertices,ver_actual, ver_comp,a,co1,sal_xor,counter,con1;
    String pru_xor;
    int val_act, val_act1, val_com2, val_xor;
    String str_xor;
    Ellipse2D oval;
    ArrayList<Ellipse2D> ellipseList = new ArrayList<Ellipse2D>();
    
    
    //Constructor
    DrawPanel(int dim, int altodim, int anchodim, int[] salidaT){
      MAX_ROW = altodim +1 ; //Cantidad de Filas
      MAX_COL = anchodim ;   //Cantidad de columnas
      salidaT1 = salidaT;    //Vector con valores
      dimensiones = dim;     //Cantidad de dimensiones
      color1_deg=Color.white;
      color2_deg=Color.gray;
      y=0;
      anc_oval=30;
      alt_oval=30;
      addMouseListener(this);
      addMouseMotionListener(this);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        DIST_altura=(int)var_altura-200;
        DIST_ancho=(int)var_ancho/2;
        //this.setSize(408, 400);
        this.setSize(DIST_ancho, DIST_altura);
        //this.setSize((int)this.getSize().getWidth()/2, (int)this.getSize().getHeight());
        System.out.println("Tamano Ancho"+this.getSize().getWidth());
        System.out.println("Tamano Alto"+this.getSize().getHeight());
        
        DIST_altura = (int)this.getSize().getHeight()/MAX_ROW;
        DIST_ancho = (int)this.getSize().getWidth()/MAX_COL;
        
        // Dibujar puntos cuadricula
        int x = 0;
        for (int row = 1; row < MAX_ROW; row++) {
            x = row * DIST_altura;
            for (int col = 1; col < MAX_COL; col++) {
                int y = col * DIST_ancho;
                g.setColor(Color.blue);
                g.drawLine(x, y, x, y + 1);
            }
        }
    
       /*Estableciendo la estructura de nodos */
               
       //System.out.println("El valor de la salida es:"+this.salidaT1.length);                
       pos_x = new HashMap(); //Posiciones de x
       pos_y = new HashMap(); //Posiciones de y          
    
       //Guardando informacion de vertices adyacentes
       can_vertices = (int)Math.pow(2, dimensiones);
       mapPosi = new HashMap(); //Hashmap que guarda el orden de los vertices
       mapPosi.put(0, 0);  //Valor inicial es 0.0
  
       for(int rs=0,ps=1;rs<mapPosi.size();rs++)
       {
        //Obteniendo el elemento
         ver_actual=(Integer)mapPosi.get(rs);
  
         //Cada valor se va a comparar para ver digito separador
          for(int rt=0;rt<dimensiones;rt++)
          {
           ver_comp = (int)Math.pow(2, rt);
           sal_xor=(int)(ver_actual ^ ver_comp);
     
           if(!mapPosi.containsValue(sal_xor))
             {
               mapPosi.put(ps,sal_xor);
               ps++;
             }
          }   
      }
  
  //Codigo para iterar el hashMap
    Iterator do1 = mapPosi.entrySet().iterator();
    while (do1.hasNext()) {
      Map.Entry e = (Map.Entry)do1.next();
      //System.out.println("LlaveOK es: --"+e.getKey() + "-- " + e.getValue());
    }


    //Estableciendo las posiciones de los nodos.     
      for (int row = 1,pos=0, val=0; row <= this.salidaT1.length; row++,pos++)
      {      
        y = (row * DIST_altura);
        if(salidaT1[pos]==1)
        {
          x= (int)(this.getSize().getWidth()/2);       
          pos_x.put(mapPosi.get(val),x);
          pos_y.put(mapPosi.get(val),y);        
          val++;
        }
        else
        {   
          DIST_ancho=(int)(this.getSize().getWidth()/(salidaT1[pos] + 1)); ;
          
            for(int col1=1;col1<=salidaT1[pos];col1++)
            {
                x = col1*DIST_ancho;
                pos_x.put(mapPosi.get(val),x);
                pos_y.put(mapPosi.get(val),y);
                val++;
            }
        }//Fin del else
      }//Fin del for
    /*Fin de establecer los puntos */
    
    Graphics2D g2d = (Graphics2D)g; 
    g2d.setColor(Color.black);
    
    //Estableciendo los enlaces
    for(int j=1,k=0;j<=can_vertices;j++,k++)
    {
        //Obtener los valores adyacentes
        val_act=k;
        System.out.println("*** Valor actual es:" +val_act);
        for(int m=1;m<=can_vertices;m++)
        {
          if(m > val_act) //Para no comparar con anteriores
          {
            //Aplicar XOR
              val_act1=val_act;  //Valor actual
              val_com2 = m;      //Valor del vertice
              val_xor=(int)(val_act1 ^ val_com2);
              
              //Convertir a string binario , ya que los que difieran en 1 bit son adyacentes              
              str_xor=Integer.toBinaryString(val_xor);
              counter = 0;
                for( int n=0; n<str_xor.length(); n++ ) {
                 if( str_xor.charAt(n) == '1' ) {
                  counter++;
                  } 
                 }
                            
              if((counter == 1) && (val_act < can_vertices) && (val_com2 < can_vertices ) ) 
              {
              //Dibujamos la linea del origen al destino ya que son adyacentes
                  //System.out.println(" Vertices son: "+can_vertices+" Resultado de XOR de (val_act1): "+val_act1+" (val_com2): " +val_com2 + "es (val_xor): "+val_xor +" Binario es: "+str_xor + " Cantidad de (1): "+counter );
                  g.drawLine((Integer)pos_x.get(val_act), (Integer)pos_y.get(val_act), (Integer)pos_x.get(val_com2), (Integer)pos_y.get(val_com2));
              }
          }
        }
    }
   /*Fin de establecer los enlaces */     
    
         
      
      g2d.setColor(color_borde);
      int prec=0;
      Iterator<Integer> i = val_prec.iterator();
      while( i.hasNext() ){
          prec=i.next();
          
      g2d.fillOval((Integer)pos_x.get(prec)-(anc_oval/2)-3,(Integer)pos_y.get(prec)-(alt_oval/2)-3,anc_oval+6,alt_oval+6);
          //System.out.println("--Elementos que preceden"+ i.next() );
      }
    
    
    
    //Dibujando los nodos sobre los enlaces
      Font exFont = new Font("TimesRoman",Font.PLAIN,10);
      g2d.setFont(exFont);
    
     //Dibujando los nodos y los numeros, **OJO El mapPosi no esta ordenado en sus indices
      //Colorear los nodos que son dimensiones
       int logb=0;
      
      for(int o=0;o<can_vertices;o++)
      {         
        for(int p=0;p<dimensiones;p++)
        {
          
         int potencia=(int)Math.pow(2, p);
         int posicion=(Integer)mapPosi.get(o);
            
         System.out.println("***Dimension:"+p+"  Potencia:"+(int)Math.pow(2, p)+"  Valor de mapPosi"+mapPosi.get(o));   
        // if((int)Math.pow(2, p)==mapPosi.get(o))
         if(potencia==posicion)
         {
             System.out.println("Aqui entre potencia igual a mapPosi");
         g2d.setColor(c[p]);
         break;
         }
         else{
          g2d.setColor(Color.BLACK);
         }      
        }
          
      oval = (new Ellipse2D.Double((Integer)pos_x.get(mapPosi.get(o))-(anc_oval/2),(Integer)pos_y.get(mapPosi.get(o))-(alt_oval/2),anc_oval,alt_oval));
      g2d.fill(oval);
      g2d.setColor(Color.WHITE);
      //g2d.drawOval((Integer)pos_x.get(mapPosi.get(o))-(anc_oval/2),(Integer)pos_y.get(mapPosi.get(o))-(alt_oval/2),anc_oval+2,alt_oval+2);
      ellipseList.add(oval);
      //System.out.println("Datos del ovalo: "+o+"Valor de x: "+pos_x.get(mapPosi.get(o)).toString()+" Valor de y:"+pos_y.get(mapPosi.get(o)).toString() + " Key de MapPosi: "+o+" Valor de MapPosi: " + mapPosi.get(o));
      
      g2d.setColor(Color.BLACK);
      g2d.drawString(mapPosi.get(o).toString(),(Integer)pos_x.get(mapPosi.get(o))+(anc_oval/2),(Integer)pos_y.get(mapPosi.get(o))-(alt_oval/2));
    }
          
     val_prec.clear();
      
     //Iterar el array list
  for (Ellipse2D e : ellipseList)
  {
  System.out.println("Indice de:"+ellipseList.indexOf(e)+" MapPosi: "+mapPosi.get(ellipseList.indexOf(e))); 
  }
  }//Fin del paint
    
    
        public void mousePressed(MouseEvent evento)
    {
    //System.out.println("Se hizo clic en: "+evento.getX()+"--"+evento.getY());
    }
    
        public void mouseDragged(MouseEvent evento)
    {
    //System.out.println("Se hizo clic en: "+evento.getX()+"--"+evento.getY());
    }
    
        public void mouseMoved(MouseEvent evento)
    {
    //System.out.println("Se hizo clic en: "+evento.getX()+"--"+evento.getY());
    }
    
        public void mouseReleased(MouseEvent evento)
    {
    //System.out.println("Se hizo clic en: "+evento.getX()+"--"+evento.getY());
    }
    
                public void mouseEntered(MouseEvent evento)
    {
    //System.out.println("Se hizo clic en: "+evento.getX()+"--"+evento.getY());
    }
        
    
    public void mouseClicked(MouseEvent evento)
    {
        //Establecer un metodo que me indique el ovalo que estoy seleccionando   
        for (Ellipse2D e : ellipseList) {
        if(e.contains(evento.getX(), evento.getY()))
        {
        System.out.println("Seleccionado"+e.toString()+"Indice de array ellipse: "+ellipseList.indexOf(e) + " Posicion:"+mapPosi.get(ellipseList.indexOf(e)));
        //Establecer el metodo que indica que va a cambiar la informacion del nodo actual
        
        co = Color.BLUE;
        str_posicion=Integer.toString((Integer)(mapPosi.get(ellipseList.indexOf(e))));
        str_co=Color.black;
       
       //03Enero. Buscar el ovalo seleccionado para saber las particiones
        int val=0;
        for(int i=0,o=0;i<salidaT.length;i++)
        {
          for(int v=0;v<salidaT[i];v++)
          {
            {
            //System.out.println("El valor de mapPosi:"+ (Integer)(mapPosi.get(o))  +"Se encuntra en"+i);
            if((Integer)(mapPosi.get(ellipseList.indexOf(e))) == (Integer)(mapPosi.get(o)))
            {
            //System.out.println("Toy aqui: "+i);
            val=i;
            }         
          }
            o++;
          }
        }
        str_cp=Integer.toString(val);
        
       
        //Limpiando el mapa        
        //03-Enero. Definiendo predecesores del nodo actual
        //System.out.println("Cantidad de dimensiones antes:"+dimensiones);
        Predecesor((Integer)(mapPosi.get(ellipseList.indexOf(e))),dimensiones,0);
        
      Iterator<Integer> i = val_prec.iterator();
      while( i.hasNext() ){
      System.out.println("--Elementos que preceden"+ i.next() );
      }
      
      //La lista val_prec, contiene los datos de aquellos nodos que preceden el nodo actual.
      color_borde=Color.MAGENTA;
      pnlCentro.repaint();
      pnlSur.repaint();
        break;
        }
         }//Fin del for
         
        //Buscando Informacion pero de los rectangulos de las relaciones 
       for (Rectangle2D r : rectangleList) {
        if(r.contains(evento.getX(), evento.getY()))
        {
        //System.out.println("Seleccionado"+r.toString()+"Indice de array rectangle: "+rectangleList.indexOf(r) + " Posicion:"+mapPosi.get(rectangleList.indexOf(r)));
         
        }
         }//Fin del segundo for
    }
    
   public void mouseExited(MouseEvent evento)
    {
    //System.out.println("Se hizo clic en: "+evento.getX()+"--"+evento.getY());
    }
    
   
   public int get_ovalo(int x, int y)
   {
   return 0;
   }
}

} //Fin de la clase OLAPNcube
