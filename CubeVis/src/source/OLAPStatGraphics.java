package source;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.behaviors.vp.*;

class OLAPStatGraphics implements ActionListener, ItemListener// extends JFrame
{

static String CR = "\r\n";

static String resultTableText = "_Result";
static String summaryTableText = "_Summary";
static String finalTableText = "_Final";
static String resultTempTText = "_TempT";
static String resultTempZText = "_TempZ";
final static String colors[] = {"B", "R", "G", "Y", "W"};
final static int numColors = 5;

static String technique = "olapstat";

final static double[][] regionColors = {
		{ 0,         0,    0.5625 },
        { 0,         0,    0.6250 },
        { 0,         0,    0.6875 },
        { 0,         0,    0.7500 },
        { 0,         0,    0.8125 },
        { 0,         0,    0.8750 },
        { 0,         0,    0.9375 },
        { 0,         0,    1.0000 },
        { 0,    0.0625,    1.0000 },
        { 0,    0.1250,    1.0000 },
        { 0,    0.1875,    1.0000 },
        { 0,    0.2500,    1.0000 },
        { 0,    0.3125,    1.0000 },
        { 0,    0.3750,    1.0000 },
        { 0,    0.4375,    1.0000 },
        { 0,    0.5000,    1.0000 },
        { 0,    0.5625,    1.0000 },
        { 0,    0.6250,    1.0000 },
        { 0,    0.6875,    1.0000 },
        { 0,    0.7500,    1.0000 },
        { 0,    0.8125,    1.0000 },
        { 0,    0.8750,    1.0000 },
        { 0,    0.9375,    1.0000 },
        { 0,    1.0000,    1.0000 },
   { 0.0625,    1.0000,    0.9375 },
   { 0.1250,    1.0000,    0.8750 },
   { 0.1875,    1.0000,    0.8125 },
   { 0.2500,    1.0000,    0.7500 },
   { 0.3125,    1.0000,    0.6875 },
   { 0.3750,    1.0000,    0.6250 },
   { 0.4375,    1.0000,    0.5625 },
   { 0.5000,    1.0000,    0.5000 },
   { 0.5625,    1.0000,    0.4375 },
   { 0.6250,    1.0000,    0.3750 },
   { 0.6875,    1.0000,    0.3125 },
   { 0.7500,    1.0000,    0.2500 },
   { 0.8125,    1.0000,    0.1875 },
   { 0.8750,    1.0000,    0.1250 },
   { 0.9375,    1.0000,    0.0625 },
   { 1.0000,    1.0000,         0 },
   { 1.0000,    0.9375,         0 },
   { 1.0000,    0.8750,         0 },
   { 1.0000,    0.8125,         0 },
   { 1.0000,    0.7500,         0 },
   { 1.0000,    0.6875,         0 },
   { 1.0000,    0.6250,         0 },
   { 1.0000,    0.5625,         0 },
   { 1.0000,    0.5000,         0 },
   { 1.0000,    0.4375,         0 },
   { 1.0000,    0.3750,         0 },
   { 1.0000,    0.3125,         0 },
   { 1.0000,    0.2500,         0 },
   { 1.0000,    0.1875,         0 },
   { 1.0000,    0.1250,         0 },
   { 1.0000,    0.0625,         0 },
   { 1.0000,         0,         0 },
   { 0.9375,         0,         0 },
   { 0.8750,         0,         0 },
   { 0.8125,         0,         0 },
   { 0.7500,         0,         0 },
   { 0.6875,         0,         0 },
   { 0.6250,         0,         0 },
   { 0.5625,         0,         0 },
   { 0.5000,         0,         0 } };


//DBSQLStatement DBS;
//DBSQLConnect DBC;


int numDims;
int numCols, numRows;
float border = (float)0.1;
float smallL = (float)0.5;
float smallW = (float)1;
float bigL;
float bigW = (float)(smallW + 2 * border);

String TableName;
String idTable;


DBSQLConnect DBC;
DBSQLStatement DBS;


//OLAPStatGraphicsWrapper dimWin;
OLAPCube dimWin ;
OLAPTree dimWin2;
//OLAPHyper dimHyper;
OLAPNcube dimCube;
OLAPStatGraphicsHeartWrap heart1, heart2;
OlapGraphicsData graphic,graphic2, graphic3;
OLAPStatGraphicsColormap colormap1, colormap2;

JLabel dimLabel, dimLabel2, dimShownLabel, heart1Label, heart2Label, dimShownDimensiones;
JButton backButton1, forwardButton1, backButton2, forwardButton2;
JCheckBox thumbCheck1, thumbCheck2;
JButton sampleButton1, sampleButton2;
JRadioButton averageImgRadio1, sampleImgRadio1, allImgRadio1;
static int heart1Display=1, heart2Display=1;
JRadioButton averageImgRadio2, sampleImgRadio2, allImgRadio2;
ButtonGroup ImgButtonGroup1, ImgButtonGroup2;
JButton buttonSwitch;
GroupLayout layout;

String[] TEST = {   "Hypothesis testing of a single population mean",
                    "Hypothesis testing of a single population proportion",
                    "Hypothesis testing of the difference between two population means",
                    "Hypothesis testing of the difference between two population proportions",
                    "Hypothesis testing of a single population variance",
                    "Hypothesis testing of the ratio of two population variances",
                    "Hypothesis tests with chi-square"
};

//EDGAR
Vector dimSize;
Vector labels;
Vector values;
Vector nCubeStart, nCubeFinal;

	OLAPStatGraphics (String dbms, Vector DVector, Vector MVector, Vector IVector, String tblname, String idtable, int test, int numCube, String posCube)
	{
		numDims = DVector.size ();
		TableName = tblname;
                idTable = idtable;
		DBS = new DBSQLStatement (dbms, true);
		DBC = new DBSQLConnect (dbms, true, true);
		DBC.startConnection ();
		
		heart1Display = 1;
		heart2Display = 1;
                
                //Obtiene el tamaño de las dimensiones
                calcSizeDim(DVector);
                
                //tamaño dependiendo del número de dimensiones
		bigL = (float)((smallL * numDims) + 2 * border);
		calcNumRowsCols ();
		
		System.out.println ("********** OLAPStatGraphics **********");
		System.out.println ("Num Dims = " + numDims);

		JFrame frame = new JFrame ("OLAP Statistical Tests");
		frame.setPreferredSize (new Dimension (2000,700));
		frame.setMinimumSize (new Dimension (100, 100));
//		frame.setMaximumSize (new Dimension (500, 500));

		System.out.println ("NOW: " + tblname + "  " + TableName);

		heart1 = new OLAPStatGraphicsHeartWrap ();
		heart2 = new OLAPStatGraphicsHeartWrap ();
                graphic = new OlapGraphicsData("Hombre",MVector);
                graphic2 = new OlapGraphicsData("Hombre",MVector);
		//dimWin = new OLAPStatGraphicsWrapper(DBC, DBS, DVector, MVector, IVector, TableName, numDims, numRows, numCols, border, smallL, smallW, bigL, bigW, this, heart1, heart2);
                graphic3 = new OlapGraphicsData("Hombre",MVector);
                dimWin = new OLAPCube(DBC, DBS, dimSize, DVector, MVector, values, TableName, idTable, test, graphic, graphic2, numCube, posCube, this,graphic3);
                
                System.out.println("Aqui inicia todo Vane *****************");
               // dimWin2 = new OLAPTree(DBC, DBS, dimSize, DVector, MVector, values, TableName, idTable, test, graphic, graphic2, numCube, posCube, this,graphic3);
	       // dimHyper= new OLAPHyper();
                
                //Probando recorrer vector
                for(int t=0;t<values.size();t++)
                {
                System.out.println("*********Valores de values:-"+values.get(t).toString()+" -Tamano de values"+values.size());
                }
                
                 //17-Mzo. Solicitando datos de vector inicial y final para saber las relaciones
                 nCubeStart=dimWin.vectorStart();
                 nCubeFinal=dimWin.vectorFinish(); 
                 
                 buttonSwitch = new JButton("Visualizar dimensiones");
                 buttonSwitch.addActionListener(this);
                dimCube = new OLAPNcube(dimSize,DVector,nCubeStart,nCubeFinal,test);
                System.out.println("Aqui finaliza todo Vane *****************");
                colormap1 = new OLAPStatGraphicsColormap ();
		colormap2 = new OLAPStatGraphicsColormap ();
		
		dimLabel = new JLabel (" ");
			dimLabel.setFont (new Font ("Times New Roman", Font.BOLD, 12));
                        dimLabel2 = new JLabel (" ");
			dimLabel2.setFont (new Font ("Times New Roman", Font.BOLD, 12));
		dimShownLabel = new JLabel (" ");
			dimShownLabel.setFont (new Font ("Times New Roman", Font.BOLD, 15));
                dimShownDimensiones = new JLabel (" ");
			dimShownLabel.setFont (new Font ("Times New Roman", Font.BOLD, 15));        
                        
		heart1Label = new JLabel ("None");
			heart1Label.setFont (new Font ("Times New Roman", Font.BOLD, 20));
		heart2Label = new JLabel ("None");
			heart2Label.setFont (new Font ("Times New Roman", Font.BOLD, 20));
			
		thumbCheck1 = new JCheckBox ("Thumbnails");
			thumbCheck1.addItemListener (this);
			thumbCheck1.setSelected (false);
			thumbCheck1.setEnabled (false);
		thumbCheck2 = new JCheckBox ("Thumbnails");
			thumbCheck2.addItemListener (this);
			thumbCheck2.setSelected (false);
			thumbCheck2.setEnabled (false);			
		
		backButton1 = new JButton ("<<");
			backButton1.setActionCommand ("back1");
			backButton1.addActionListener (this);
			backButton1.setEnabled (false);
		forwardButton1 = new JButton (">>");
			forwardButton1.setActionCommand ("forward1");
			forwardButton1.addActionListener (this);
			forwardButton1.setEnabled (false);

		backButton2 = new JButton ("<<");
			backButton2.setActionCommand ("back2");
			backButton2.addActionListener (this);
			backButton2.setEnabled (false);
		forwardButton2 = new JButton (">>");
			forwardButton2.setActionCommand ("forward2");
			forwardButton2.addActionListener (this);
			forwardButton2.setEnabled (false);
			
		sampleButton1 = new JButton ("Sample");
			sampleButton1.setActionCommand ("sample1");
			sampleButton1.addActionListener (this);
			sampleButton1.setEnabled (false);
		sampleButton2 = new JButton ("Sample");
			sampleButton2.setActionCommand ("sample2");
			sampleButton2.addActionListener (this);
			sampleButton2.setEnabled (false);
			
			
		averageImgRadio1 = new JRadioButton ("Average Image Data");
			averageImgRadio1.setActionCommand ("averageImg1");
			averageImgRadio1.addActionListener (this);
			averageImgRadio1.setSelected (true);
		sampleImgRadio1 = new JRadioButton ("Sample Image Data");
			sampleImgRadio1.setActionCommand ("sampleImg1");
			sampleImgRadio1.addActionListener (this);
			sampleImgRadio1.setSelected (false);
		allImgRadio1 = new JRadioButton ("All Image Data");
			allImgRadio1.setActionCommand ("allImg1");
			allImgRadio1.addActionListener (this);
			allImgRadio1.setSelected (false);

		ImgButtonGroup1 = new ButtonGroup ();
			ImgButtonGroup1.add(averageImgRadio1);
			ImgButtonGroup1.add(sampleImgRadio1);
			ImgButtonGroup1.add(allImgRadio1);

			averageImgRadio2 = new JRadioButton ("Average Image Data");
			averageImgRadio2.setActionCommand ("averageImg2");
			averageImgRadio2.addActionListener (this);
			averageImgRadio2.setSelected (true);
		sampleImgRadio2 = new JRadioButton ("Sample Image Data");
			sampleImgRadio2.setActionCommand ("sampleImg2");
			sampleImgRadio2.addActionListener (this);
			sampleImgRadio2.setSelected (false);
		allImgRadio2 = new JRadioButton ("All Image Data");
			allImgRadio2.setActionCommand ("allImg2");
			allImgRadio2.addActionListener (this);
			allImgRadio2.setSelected (false);

		ImgButtonGroup2 = new ButtonGroup ();
			ImgButtonGroup2.add(averageImgRadio2);
			ImgButtonGroup2.add(sampleImgRadio2);
			ImgButtonGroup2.add(allImgRadio2);
                        
                dimShownLabel.setText(TEST[test-1] );
                dimShownDimensiones.setText(TEST[test-1] );
                if(!posCube.equals("")){
                    String[] posCube2 = posCube.split("-");
                    String posLabel = "";
                    for(int i=0; i < posCube2.length ; i++){
                        if(i > 0)
                            posLabel += ", ";
                        posLabel += DVector.elementAt(i) + ":" + posCube2[i];
                    }
                    dimLabel.setText(posLabel);
                    dimLabel2.setText(posLabel);
                }
                
                //dimShownDimensiones.setText("Mostrando las dimensiones");
                //dimShownDimensiones=dimShownLabel;
		layout = new GroupLayout (frame.getContentPane ());
		frame.getContentPane ().setLayout (layout);
		layout.setAutoCreateGaps (true);
		layout.setAutoCreateContainerGaps (true);

		// HORIZONTAL
		layout.setHorizontalGroup (layout.createSequentialGroup ()
			.addGroup (layout.createParallelGroup ()
				.addComponent (dimShownLabel)
				.addComponent (dimLabel)
				.addComponent (dimWin)
			)
			.addGroup (layout.createParallelGroup ()
				.addComponent (graphic)
				//.addComponent (heart1Label)
				//.addComponent (averageImgRadio1)
				//.addGroup (layout.createSequentialGroup ()
				//	.addComponent (sampleImgRadio1)
				//	.addComponent (sampleButton1)
				//)
				//.addGroup (layout.createSequentialGroup ()
				//	.addComponent (allImgRadio1)
				//	.addComponent (backButton1)
				//	.addComponent (forwardButton1)
				//	.addComponent (thumbCheck1)
				//)
				.addComponent (graphic2)
				//.addComponent (heart2Label)
				//.addComponent (averageImgRadio2)
				//.addGroup (layout.createSequentialGroup ()
				//	.addComponent (sampleImgRadio2)
				//	.addComponent (sampleButton2)
				//)
				//.addGroup (layout.createSequentialGroup ()
				//	.addComponent (allImgRadio2)
				//	.addComponent (backButton2)
				//	.addComponent (forwardButton2)
				//	.addComponent (thumbCheck2)
				//)
			)
			//.addGroup (layout.createParallelGroup ()
			//	.addComponent (colormap1)
			//	.addComponent (colormap2)
			//)
                        
                        /*VGH. Agregando grupo para visualizar*/
                        .addGroup (layout.createParallelGroup ()
				.addComponent (graphic3)
                        )
                        
                     .addGroup (layout.createParallelGroup()
					.addComponent (dimShownDimensiones)
					.addComponent (dimLabel2)
                                       // .addComponent (dimCube)
					.addComponent (buttonSwitch)
				)
                        
                        
		);
		
		//VERTICAL
		layout.setVerticalGroup (layout.createSequentialGroup ()
			.addGroup (layout.createParallelGroup ()
				.addGroup (layout.createSequentialGroup ()
					.addComponent (dimShownLabel)
					.addComponent (dimLabel)
					.addComponent (dimWin)
				)
				.addGroup (layout.createSequentialGroup ()
					.addGroup (layout.createParallelGroup ()
						.addComponent (graphic)
						//.addComponent (colormap1)
					)
					//.addComponent (heart1Label)
					//.addComponent (averageImgRadio1)
					//.addGroup (layout.createParallelGroup ()
					//	.addComponent (sampleImgRadio1)
					//	.addComponent (sampleButton1)
					//)
					//.addGroup (layout.createParallelGroup ()
					//	.addComponent (allImgRadio1)
					//	.addComponent (backButton1)
					//	.addComponent (forwardButton1)
					//	.addComponent (thumbCheck1)
					//)
					.addGroup (layout.createParallelGroup ()
						.addComponent (graphic2)
						//.addComponent (colormap2)
					)
					//.addComponent (heart2Label)
					//.addComponent (averageImgRadio2)
					//.addGroup (layout.createParallelGroup ()
					//	.addComponent (sampleImgRadio2)
					//	.addComponent (sampleButton2)
					//)
					//.addGroup (layout.createParallelGroup ()
					//	.addComponent (allImgRadio2)					
					//	.addComponent (backButton2)
					//	.addComponent (forwardButton2)
					//	.addComponent (thumbCheck2)
					//)
				)
                         /*VGH. Agregando grupo para visualizar*/
                        .addGroup (layout.createParallelGroup ()
				.addComponent (graphic3)
                        )
                        
                       .addGroup (layout.createSequentialGroup ()
					.addComponent (dimShownDimensiones)
					.addComponent (dimLabel2)
                                        //.addComponent (dimCube)
					.addComponent (buttonSwitch)
				)
                        
			)//Primer add group
                         
                        
		);

		
//		Container c = getContentPane();
//		c.setLayout( new BorderLayout() );
//		c.add(dimWin, BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);
	}
	
  
        
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		
		if (source == thumbCheck1)
		{
			if (thumbCheck1.isSelected ())
			{
				backButton1.setEnabled (false);
				forwardButton1.setEnabled (false);
				//dimWin.showThumbnails (dimWin.binStr1, 1);
			}
			else
			{
				backButton1.setEnabled (true);
				forwardButton1.setEnabled (true);
				//dimWin.getHeartData (dimWin.binStr1, 1);
			}
		}
		else if (source == thumbCheck2)
		{
			if (thumbCheck2.isSelected ())
			{
				backButton2.setEnabled (false);
				forwardButton2.setEnabled (false);
				//dimWin.showThumbnails (dimWin.binStr2, 2);
			}
			else
			{
				backButton2.setEnabled (true);
				forwardButton2.setEnabled (true);
				//dimWin.getHeartData (dimWin.binStr2, 2);
			}	
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		String AC = e.getActionCommand ();
System.out.println("Accion del boton es"+AC);
		if (AC.equals ("back1"))
		{
			//dimWin.prevAllHeartImg(1);
		}
		else if (AC.equals ("forward1"))
		{
			//dimWin.nextAllHeartImg(1);
		}
		else if (AC.equals ("back2"))
		{
			//dimWin.prevAllHeartImg(2);
		}
		else if (AC.equals ("forward2"))
		{
			//dimWin.nextAllHeartImg(2);
		}
		else if (AC.equals ("sample1"))
		{
			//dimWin.getHeartData (dimWin.binStr1, 1);
		}
		else if (AC.equals ("sample2"))
		{
			//dimWin.getHeartData (dimWin.binStr2, 2);
		}
		else if (AC.equals ("averageImg1"))
		{
			heart1Display = 1;
			//dimWin.getHeartData (dimWin.binStr1, 1);
			sampleButton1.setEnabled (false);
			backButton1.setEnabled (false);
			forwardButton1.setEnabled (false);
			thumbCheck1.setEnabled (false);
		}
		else if (AC.equals ("sampleImg1"))
		{
			heart1Display = 2;
			//dimWin.getHeartData (dimWin.binStr1, 1);
			sampleButton1.setEnabled (true);
			backButton1.setEnabled (false);
			forwardButton1.setEnabled (false);
			thumbCheck1.setEnabled (false);
		}
		else if (AC.equals ("allImg1"))
		{
			heart1Display = 3;
			//dimWin.getHeartData (dimWin.binStr1, 1);
			sampleButton1.setEnabled (false);
			backButton1.setEnabled (true);
			forwardButton1.setEnabled (true);
			thumbCheck1.setEnabled (true);
		}
		else if (AC.equals ("averageImg2"))
		{
			heart2Display = 1;
			//dimWin.getHeartData (dimWin.binStr2, 2);
			sampleButton2.setEnabled (false);
			backButton2.setEnabled (false);
			forwardButton2.setEnabled (false);
			thumbCheck2.setEnabled (false);
		}
		else if (AC.equals ("sampleImg2"))
		{
			heart2Display = 2;
			//dimWin.getHeartData (dimWin.binStr2, 2);
			sampleButton2.setEnabled (true);
			backButton2.setEnabled (false);
			forwardButton2.setEnabled (false);
			thumbCheck2.setEnabled (false);
		}
		else if (AC.equals ("allImg2"))
		{
			heart2Display = 3;
			//dimWin.getHeartData (dimWin.binStr2, 2);
			sampleButton2.setEnabled (false);
			backButton2.setEnabled (true);
			forwardButton2.setEnabled (true);
			thumbCheck2.setEnabled (true);
		}
		else
			System.out.println ("Unknown");
                
                
                if(AC.equals ("Visualizar dimensiones"))
                {
                  //this.dimCube.isVisible();
                  //Anadiendo el componente
                  //this.layout.
                    this.buttonSwitch.setVisible(false);
                   JPanel container = new JPanel();
                   container.setSize(1200, 800);
                   container.add(dimCube);
                    
                    JScrollPane jsp = new JScrollPane(container);
                    jsp.setSize(1200, 800);
                    
                    
                    JFrame frameCube = new JFrame("Dimensions Visualization");
                  // JScrollPane scrPane = new JScrollPane(frameCube);
                   
                   
                   frameCube.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Set up the content pane.
                   //frameCube.add(jsp);
                   frameCube.add(dimCube);
        //addComponentsToPane(frame.getContentPane());
        //Use the content pane's default BorderLayout. No need for
        //setLayout(new BorderLayout());
        //Display the window.
                   //frameCube.pack();
                   frameCube.setSize(1200,800);
                   frameCube.setVisible(true); 
                    
                }
                
    }
	
	
	public void setLabel (String lab, String text)
	{ 
		if (lab.equalsIgnoreCase ("dim"))
			dimLabel.setText (text);
		else if (lab.equalsIgnoreCase ("dimShown"))
			dimShownLabel.setText (text);
		else if (lab.equalsIgnoreCase ("heart1"))
			heart1Label.setText (text);
		else if (lab.equalsIgnoreCase ("heart2"))
			heart2Label.setText (text);
}
	
//	private void startDBConnect () { DBC.startConnection (); }
//	private void stopDBConnect () { DBC.stopConnection (); }
	
	private void calcNumRowsCols ()
	{
		if (numDims % 2 == 0)
		{
			int temp = numDims / 2;
			numRows = (int)Math.pow (2, temp);
			numCols = numRows;
		}
		else
		{
			int temp = (numDims + 1) / 2;
			numRows = (int)Math.pow (2, temp);
			temp = (numDims - 1) / 2;
			numCols = (int)Math.pow (2, temp);
		}	
	}
        
        //EDGAR
        //Obtiene el tamaño de las dimensiones, valores diferentes
        private void calcSizeDim(Vector DVector){
            dimSize = new Vector();
            values = new Vector();
            Vector valuesi = new Vector();
                    
            String sql = "";
            String[] num;
            String dim = "";
            int size;
            
            String[] cols = new String[1];
            
            
            for(int i=0;i<DVector.size();i++){
                dim = DVector.get(i).toString();
                cols[0] = dim;
               
                System.out.println(dim);
                
                sql = DBS.dbSelectSize(TableName,dim);
                DBC.sendQuery( sql );
                num = DBC.getResult(cols);
                size = 0;
                
                while(num.length != 0){
                    System.out.println((String)num[0]);
                    valuesi.add((String)num[0]);
                    size ++;
                    num = DBC.getResult(cols);
                }
                values.add((Vector)valuesi);
                valuesi = new Vector();
                
                
                dimSize.add(size);
            }
        }
	
}