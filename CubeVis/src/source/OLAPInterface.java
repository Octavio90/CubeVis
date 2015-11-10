//import java.awt.Component;
package source;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import static javax.swing.GroupLayout.Alignment.*;

import java.io.*;
import java.util.*;

class WindowsListener extends WindowAdapter {
  public void windowClosing(WindowEvent event) {
    System.exit(0);
  }
}

public class OLAPInterface extends JFrame 
							implements ActionListener {

	JLabel TableNameLabel;
	JTextField TableNameText;
	
	JLabel DimensionsLabel;
	DefaultListModel DimensionsAllModel, DimensionsSelectedModel;
	JList DimensionsAllList, DimensionsSelectedList;
	JScrollPane DimensionsAllScroll, DimensionsSelectedScroll;
	JButton DimensionsInsertButton, DimensionsRemoveButton, DimensionsAddButton;
	JTextField DimensionsAddText;
	
	JLabel MeasuresLabel;
	DefaultListModel MeasuresAllModel, MeasuresSelectedModel;
	JList MeasuresAllList, MeasuresSelectedList;
	JScrollPane MeasuresAllScroll, MeasuresSelectedScroll;
	JButton MeasuresInsertButton, MeasuresRemoveButton, MeasuresAddButton;
	JTextField MeasuresAddText;
	
	JLabel ImageLabel;
	DefaultListModel ImageAllModel, ImageSelectedModel;
	JList ImageAllList, ImageSelectedList;
	JScrollPane ImageAllScroll, ImageSelectedScroll;
	JButton ImageInsertButton, ImageRemoveButton;
	
	
	JLabel DBMSLabel;
	JComboBox DBMSCombo;
	JLabel PThresholdLabel;
	JTextField PThresholdText;
	JLabel OutfileLabel;
	JTextField OutfileText;
	
	ButtonGroup AlgorithmGroup;
	JRadioButton Algorithm1Radio;
	JRadioButton Algorithm2Radio;
	JRadioButton Algorithm3Radio;
        
        ButtonGroup TestGroup;
        JRadioButton Test1Radio;
        JRadioButton Test2Radio;
        JRadioButton Test3Radio;
        JRadioButton Test4Radio;
        JRadioButton Test5Radio;
        JRadioButton Test6Radio;
        JRadioButton Test7Radio;
        JLabel meanLabel;
        JTextField meanValue;
        
        JLabel proportionLabel;
        JTextField proportionValue;
        
        JLabel varianceLabel;
        JTextField varianceValue;
	
	JButton ExecuteButton;

Vector m_DimensionVector;
Vector m_PartialDVector;
Vector m_MeasureVector;
Vector m_PartialMVector;
Vector m_ImageVector;
Vector m_PartialIVector;
String m_tblName;
int pThreshold;
String sqlFileName;
String dbms;
int algorithm;
int test = 1;

String parameter = "";

OLAPStatGraphics OSG;
DBSQLConnect dbConn;




    public OLAPInterface(Vector d, Vector measures, Vector img, Vector pd, Vector pm, Vector pi, String tblname, int algorm, String dbmsType, int p, String outName)
	{
		m_DimensionVector = d;
		m_PartialDVector = pd;
		m_MeasureVector = measures;
		m_PartialMVector = pm;
		m_ImageVector = img;
		m_PartialIVector = pi;
		m_tblName = tblname;
		pThreshold = p;
		sqlFileName = outName;
		dbms=dbmsType;
		algorithm = algorm;

		createGUI ();
	}

        //FUNCION crea la interfaz donde se seleccionan dimensiones, medidas e imágenes
	private void createGUI () {
		TableNameLabel = new JLabel ("Table Name: ");
		TableNameText = new JTextField (m_tblName);
			TableNameText.setMinimumSize (new Dimension (100, 10));

		//DIMENSIONS
		DimensionsLabel = new JLabel ("Cube Dimensions: ");
		DimensionsAllModel = new DefaultListModel ();
			for (int i = 0; i < m_DimensionVector.size (); i++)
				if (!m_PartialDVector.contains (m_DimensionVector.elementAt (i)))
					DimensionsAllModel.addElement (m_DimensionVector.elementAt (i));
		DimensionsAllList = new JList (DimensionsAllModel);
			DimensionsAllList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			DimensionsAllList.setLayoutOrientation (JList.HORIZONTAL_WRAP);
		DimensionsAllScroll = new JScrollPane (DimensionsAllList);
			DimensionsAllScroll.setPreferredSize (new Dimension (250, 80));
		DimensionsSelectedModel = new DefaultListModel ();
			for (int i = 0; i < m_PartialDVector.size (); i++)
				DimensionsSelectedModel.addElement (m_PartialDVector.elementAt (i));
		DimensionsSelectedList = new JList (DimensionsSelectedModel);
			DimensionsSelectedList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			DimensionsSelectedList.setLayoutOrientation (JList.HORIZONTAL_WRAP);
		DimensionsSelectedScroll = new JScrollPane (DimensionsSelectedList);
			DimensionsSelectedScroll.setPreferredSize (new Dimension (250, 80));
		DimensionsInsertButton = new JButton (">>>>");
			DimensionsInsertButton.setActionCommand ("InsertDimensions");
			DimensionsInsertButton.addActionListener (this);
		DimensionsRemoveButton = new JButton ("<<<<");
			DimensionsRemoveButton.setActionCommand ("RemoveDimensions");
			DimensionsRemoveButton.addActionListener (this);
		
		// MEASURES
		MeasuresLabel = new JLabel ("Cube Measures: ");
		MeasuresAllModel = new DefaultListModel ();
			for (int i = 0; i < m_MeasureVector.size (); i++)
				if (!m_PartialMVector.contains (m_MeasureVector.elementAt (i)))
					MeasuresAllModel.addElement (m_MeasureVector.elementAt (i));
		MeasuresAllList = new JList (MeasuresAllModel);
			MeasuresAllList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			MeasuresAllList.setLayoutOrientation (JList.VERTICAL);
		MeasuresAllScroll = new JScrollPane (MeasuresAllList);
			MeasuresAllScroll.setPreferredSize (new Dimension (250, 80));
		MeasuresSelectedModel = new DefaultListModel ();
			for (int i = 0; i < m_PartialMVector.size (); i++)
				MeasuresSelectedModel.addElement (m_PartialMVector.elementAt (i));
		MeasuresSelectedList = new JList (MeasuresSelectedModel);
			MeasuresSelectedList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			MeasuresSelectedList.setLayoutOrientation (JList.VERTICAL);
		MeasuresSelectedScroll = new JScrollPane (MeasuresSelectedList);
			MeasuresSelectedScroll.setPreferredSize (new Dimension (250, 80));
		MeasuresInsertButton = new JButton (">>>>");
			MeasuresInsertButton.setActionCommand ("InsertMeasures");
			MeasuresInsertButton.addActionListener (this);
		MeasuresRemoveButton = new JButton ("<<<<");
			MeasuresRemoveButton.setActionCommand ("RemoveMeasures");
			MeasuresRemoveButton.addActionListener (this);
		
		// Image Data
		ImageLabel = new JLabel ("Statistical Test: ");
                Test1Radio = new JRadioButton ("Hypothesis testing of a single population mean");
			Test1Radio.setActionCommand ("Test1");
			Test1Radio.addActionListener (this);
			Test1Radio.setSelected (true);
                Test2Radio = new JRadioButton ("Hypothesis testing of a single population proportion");
			Test2Radio.setActionCommand ("Test2");
			Test2Radio.addActionListener (this);
                Test5Radio = new JRadioButton ("Hypothesis testing of a single population variance");
			Test5Radio.setActionCommand ("Test5");
			Test5Radio.addActionListener (this);
                Test3Radio = new JRadioButton ("Hypothesis testing of the difference between two population means");
			Test3Radio.setActionCommand ("Test3");
			Test3Radio.addActionListener (this);
                Test4Radio = new JRadioButton ("Hypothesis testing of the difference between two population proportions");
			Test4Radio.setActionCommand ("Test4");
			Test4Radio.addActionListener (this);
                Test6Radio = new JRadioButton ("Hypothesis testing of the ratio of two population variances");
			Test6Radio.setActionCommand ("Test6");
			Test6Radio.addActionListener (this);
                Test7Radio = new JRadioButton ("Hypothesis tests with chi-square");
			Test7Radio.setActionCommand ("Test7");
			Test7Radio.addActionListener (this);
                
                TestGroup = new ButtonGroup ();
			TestGroup.add(Test1Radio);
			TestGroup.add(Test2Radio);
                        TestGroup.add(Test5Radio);
			TestGroup.add(Test3Radio);
                        TestGroup.add(Test4Radio);
                        TestGroup.add(Test6Radio);
                        TestGroup.add(Test7Radio);
                        
                meanLabel = new JLabel ("µ =");
                meanValue = new JTextField ();
                meanValue.setMaximumSize(new Dimension (50, 10));
                proportionLabel = new JLabel ("p =");
                proportionValue = new JTextField ();
                proportionValue.setMaximumSize(new Dimension (50, 10));
                varianceLabel = new JLabel ("σ2 =");
                varianceValue = new JTextField ();
                varianceValue.setMaximumSize(new Dimension (50, 10));
                        
		Algorithm2Radio = new JRadioButton ("Direct Compute Method");
			Algorithm2Radio.setActionCommand ("Algorithm2");
			Algorithm2Radio.addActionListener (this);
		Algorithm3Radio = new JRadioButton ("Auto Compute Method");
			Algorithm3Radio.setActionCommand ("Algorithm3");
			Algorithm3Radio.addActionListener (this);
                
                
		ImageAllModel = new DefaultListModel ();
			for (int i = 0; i < m_ImageVector.size (); i++)
				if (!m_PartialIVector.contains (m_ImageVector.elementAt (i)))
					ImageAllModel.addElement (m_ImageVector.elementAt (i));
		ImageAllList = new JList (ImageAllModel);
			ImageAllList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			ImageAllList.setLayoutOrientation (JList.VERTICAL);
		ImageAllScroll = new JScrollPane (ImageAllList);
			ImageAllScroll.setPreferredSize (new Dimension (250, 80));
		ImageSelectedModel = new DefaultListModel ();
			for (int i = 0; i < m_PartialIVector.size(); i++)
				ImageSelectedModel.addElement (m_PartialIVector.elementAt (i));
		ImageSelectedList = new JList (ImageSelectedModel);
			ImageSelectedList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			ImageSelectedList.setLayoutOrientation (JList.VERTICAL);
		ImageSelectedScroll = new JScrollPane (ImageSelectedList);
			ImageSelectedScroll.setPreferredSize (new Dimension (250, 80));
		ImageInsertButton = new JButton (">>>>");
			ImageInsertButton.setActionCommand ("InsertImage");
			ImageInsertButton.addActionListener (this);
		ImageRemoveButton = new JButton ("<<<<");
			ImageRemoveButton.setActionCommand ("RemoveImage");
			ImageRemoveButton.addActionListener (this);
		
		//OTHER OPTIONS
		DBMSLabel = new JLabel ("DBMS: ");
			String[] DBMSstr = {"sqlserver","postgresql"};
		DBMSCombo = new JComboBox (DBMSstr);
			
                        DBMSCombo.setActionCommand("selectDBMS");
                        DBMSCombo.addActionListener(this);
                        DBMSCombo.setSelectedItem(dbms);
		PThresholdLabel = new JLabel ("PThreshold: ");
		PThresholdText = new JTextField (new Integer(pThreshold).toString ());
		OutfileLabel = new JLabel ("Outfile: ");
		OutfileText = new JTextField (sqlFileName);
		
		Algorithm1Radio = new JRadioButton ("Pre-Computed Method");
			Algorithm1Radio.setActionCommand ("Algorithm1");
			Algorithm1Radio.addActionListener (this);
			Algorithm1Radio.setSelected (true);
		Algorithm2Radio = new JRadioButton ("Direct Compute Method");
			Algorithm2Radio.setActionCommand ("Algorithm2");
			Algorithm2Radio.addActionListener (this);
		Algorithm3Radio = new JRadioButton ("Auto Compute Method");
			Algorithm3Radio.setActionCommand ("Algorithm3");
			Algorithm3Radio.addActionListener (this);
		if (algorithm == 1)
		{
			Algorithm1Radio.setSelected (true);
			Algorithm2Radio.setSelected (false);
			Algorithm3Radio.setSelected (false);
		}
		else if (algorithm == 2)
		{
			Algorithm1Radio.setSelected (false);
			Algorithm2Radio.setSelected (true);
			Algorithm3Radio.setSelected (false);
		}
		else
		{
			Algorithm1Radio.setSelected (false);
			Algorithm2Radio.setSelected (false);
			Algorithm3Radio.setSelected (true);
		}


		AlgorithmGroup = new ButtonGroup ();
			AlgorithmGroup.add(Algorithm1Radio);
			AlgorithmGroup.add(Algorithm2Radio);
			AlgorithmGroup.add(Algorithm3Radio);
		
		ExecuteButton = new JButton ("Execute");
			ExecuteButton.setActionCommand ("Execute");
			ExecuteButton.addActionListener (this);
			ExecuteButton.setMinimumSize (new Dimension (100, 75));
		
		
		GroupLayout layout = new GroupLayout (getContentPane ());
		getContentPane ().setLayout (layout);
		layout.setAutoCreateGaps (true);
		layout.setAutoCreateContainerGaps (true);
		
		// HORIZONTAL
		layout.setHorizontalGroup (layout.createSequentialGroup ()
			.addGroup (layout.createParallelGroup (LEADING)
				.addGroup (layout.createSequentialGroup ()
					.addComponent (TableNameLabel)
					.addComponent (TableNameText)
				)
				.addComponent (DimensionsLabel)
				.addComponent (DimensionsAllScroll)
				.addComponent (MeasuresLabel)
				.addComponent (MeasuresAllScroll)
				.addComponent (ImageLabel)
				.addGroup (layout.createParallelGroup ()
						.addComponent (Test1Radio)
						.addComponent (Test2Radio)
                                                .addComponent (Test5Radio)
						.addComponent (Test3Radio)
                                                .addComponent (Test4Radio)
                                                .addComponent (Test6Radio)
                                                .addComponent (Test7Radio)
					)

				.addGroup (layout.createSequentialGroup ()
					.addComponent (DBMSLabel)
					.addComponent (DBMSCombo)
				)
//				.addGroup (layout.createSequentialGroup ()
//					.addComponent (PThresholdLabel)
//					.addComponent (PThresholdText)
//				)
				.addGroup (layout.createSequentialGroup ()
					.addComponent (OutfileLabel)
					.addComponent (OutfileText)
				)
			)
			.addGroup (layout.createParallelGroup (LEADING)
				.addComponent (DimensionsInsertButton)
				.addComponent (DimensionsRemoveButton)
				.addComponent (MeasuresInsertButton)
				.addComponent (MeasuresRemoveButton)
				.addComponent (meanLabel)
				.addComponent (proportionLabel)
                                .addComponent (varianceLabel)
			)
			.addGroup (layout.createParallelGroup ()
				.addComponent (DimensionsSelectedScroll)
				.addComponent (MeasuresSelectedScroll)
				.addComponent (meanValue)
                                .addComponent (proportionValue)
                                .addComponent (varianceValue)
				.addGroup (layout.createSequentialGroup ()
					.addGroup (layout.createParallelGroup ()
						.addComponent (Algorithm1Radio)
						.addComponent (Algorithm2Radio)
						.addComponent (Algorithm3Radio)
					)
					.addComponent (ExecuteButton)
				)
			)
		);
		
		//VERTICAL
		layout.setVerticalGroup (layout.createSequentialGroup ()
			.addGroup (layout.createParallelGroup (BASELINE)
				.addComponent (TableNameLabel)
				.addComponent (TableNameText)
			)
			.addComponent (DimensionsLabel)
			.addGroup (layout.createParallelGroup ()
				.addComponent (DimensionsAllScroll)
				.addGroup (layout.createSequentialGroup ()
					.addComponent (DimensionsInsertButton)
					.addComponent (DimensionsRemoveButton)
				)
				.addComponent (DimensionsSelectedScroll)
			)
			.addComponent (MeasuresLabel)
			.addGroup (layout.createParallelGroup ()
				.addComponent (MeasuresAllScroll)
				.addGroup (layout.createSequentialGroup ()
					.addComponent (MeasuresInsertButton)
					.addComponent (MeasuresRemoveButton)
				)
				.addComponent (MeasuresSelectedScroll)
			)
			.addComponent (ImageLabel)
			.addGroup (layout.createParallelGroup ()
				.addGroup (layout.createSequentialGroup ()
						.addComponent (Test1Radio)
						.addComponent (Test2Radio)
                                                .addComponent (Test5Radio)
						.addComponent (Test3Radio)
                                                .addComponent (Test4Radio)
                                                .addComponent (Test6Radio)
                                                .addComponent (Test7Radio)
					)
				.addGroup (layout.createSequentialGroup ()
					.addComponent (meanLabel)
					.addComponent (proportionLabel)
                                        .addComponent (varianceLabel)
				)
                                .addGroup (layout.createSequentialGroup ()
					.addComponent (meanValue)
					.addComponent (proportionValue)
                                        .addComponent (varianceValue)
				)
				
			)
			.addGroup (layout.createParallelGroup ()
				.addGroup (layout.createSequentialGroup ()
					.addGroup (layout.createParallelGroup (BASELINE)
						.addComponent (DBMSLabel)
						.addComponent (DBMSCombo)
					)
//					.addGroup (layout.createParallelGroup (BASELINE)
//						.addComponent (PThresholdLabel)
//						.addComponent (PThresholdText)
//					)
					.addGroup (layout.createParallelGroup (BASELINE)
						.addComponent (OutfileLabel)
						.addComponent (OutfileText)
					)
				)
				.addGroup (layout.createSequentialGroup ()
					.addComponent (Algorithm1Radio)
					.addComponent (Algorithm2Radio)
					.addComponent (Algorithm3Radio)
				)
				.addComponent (ExecuteButton)
			)
			
		);

        setTitle("OLAP Statistical Tests" );
        pack();
		addWindowListener(new WindowsListener());
//        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
	//FUNCION: Interacción con la interfaz
	public void actionPerformed(ActionEvent e) {
		String AC = e.getActionCommand ();
		int[] DimAll = DimensionsAllList.getSelectedIndices ();
		int[] DimSel = DimensionsSelectedList.getSelectedIndices ();
		int[] MeaAll = MeasuresAllList.getSelectedIndices ();
		int[] MeaSel = MeasuresSelectedList.getSelectedIndices ();
		int[] ImgAll = ImageAllList.getSelectedIndices ();
		int[] ImgSel = ImageSelectedList.getSelectedIndices ();
		
		if (AC.equals ("RemoveDimensions"))
		{
			for (int i = 0; i < DimSel.length; i++)
			{
				DimensionsAllModel.addElement (DimensionsSelectedModel.elementAt (DimSel[i]-i));
				DimensionsSelectedModel.removeElementAt (DimSel[i] - i);
			}		
		}
		else if (AC.equals ("InsertDimensions"))
		{
			for (int i = 0; i < DimAll.length; i++)
			{
				DimensionsSelectedModel.addElement (DimensionsAllModel.elementAt (DimAll[i]-i));
				DimensionsAllModel.removeElementAt (DimAll[i] - i);
			}
		}
		else if (AC.equals ("AddDimensions"))
		{
			String tt = DimensionsAddText.getText ();
			if (tt.length () > 0)
				DimensionsAllModel.addElement (tt);
			DimensionsAddText.setText ("");
		}	
		else if (AC.equals ("RemoveMeasures"))
		{
			for (int i = 0; i < MeaSel.length; i++)
			{
				MeasuresAllModel.addElement (MeasuresSelectedModel.elementAt (MeaSel[i]-i));
				MeasuresSelectedModel.removeElementAt (MeaSel[i] - i);
			}		
		}
		else if (AC.equals ("InsertMeasures"))
		{
			for (int i = 0; i < MeaAll.length; i++)
			{
				MeasuresSelectedModel.addElement (MeasuresAllModel.elementAt (MeaAll[i]-i));
				MeasuresAllModel.removeElementAt (MeaAll[i] - i);
			}
		}
		else if (AC.equals ("AddMeasures"))
		{
			String tt = MeasuresAddText.getText ();
			if (tt.length () > 0)
				MeasuresAllModel.addElement (tt);
			MeasuresAddText.setText ("");
		}
		else if (AC.equals ("RemoveImage"))
		{
			for (int i = 0; i < ImgSel.length; i++)
			{
				ImageAllModel.addElement (ImageSelectedModel.elementAt (ImgSel[i]-i));
				ImageSelectedModel.removeElementAt (ImgSel[i]-i);
			}
		}
		else if (AC.equals ("InsertImage"))
		{
			for (int i = 0; i < ImgAll.length; i++)
			{
				ImageSelectedModel.addElement (ImageAllModel.elementAt (ImgAll[i]-i));
				ImageAllModel.removeElementAt (ImgAll[i]-i);
			}
		}
		else if (AC.equals ("Execute"))
			callOLAPStatTest ();
		else if (AC.equals ("Algorithm1"))
			algorithm = 1;
		else if (AC.equals ("Algorithm2"))
			algorithm = 2;
		else if (AC.equals ("Algorithm3"))
			algorithm = 3;
                else if (AC.equals ("Test1"))
                        test = 1;
                else if (AC.equals ("Test2"))
                        test = 2;
                else if (AC.equals ("Test3"))
                        test = 3;
                else if (AC.equals ("Test4"))
                        test = 4;
                else if (AC.equals ("Test5"))
                        test = 5;
                else if (AC.equals ("Test6"))
                        test = 6;
                else if (AC.equals ("Test7"))
                        test = 7;
                else if (AC.equals("selectDBMS"))
                        dbms = (String)DBMSCombo.getSelectedItem();
		else
			System.out.println ("Unknown");
    }
	
	private void callOLAPStatTest ()
	{
            
            if(test == 1)
                parameter = meanValue.getText();
            else if (test == 2)
                parameter = proportionValue.getText();
            else if (test == 5)
                parameter = varianceValue.getText();
            
	m_DimensionVector.clear ();
	for (int i = 0; i < DimensionsAllModel.size (); i++)
		m_DimensionVector.addElement (DimensionsAllModel.elementAt (i));
	m_PartialDVector.clear ();
	for (int i = 0; i < DimensionsSelectedModel.size (); i++)
	{
		m_DimensionVector.addElement (DimensionsSelectedModel.elementAt (i));
		m_PartialDVector.addElement (DimensionsSelectedModel.elementAt (i));
	}
	
	m_MeasureVector.clear ();
	for (int i = 0; i < MeasuresAllModel.size (); i++)
		m_MeasureVector.addElement (MeasuresAllModel.elementAt (i));
	m_PartialMVector.clear ();
	for (int i = 0; i < MeasuresSelectedModel.size (); i++)
	{
		m_MeasureVector.addElement (MeasuresSelectedModel.elementAt (i));
		m_PartialMVector.addElement (MeasuresSelectedModel.elementAt (i));
	}
	
	m_ImageVector.clear ();
	for (int i = 0; i < ImageAllModel.size (); i++)
		m_ImageVector.addElement (ImageAllModel.elementAt (i));
	m_PartialIVector.clear ();
	for (int i = 0; i < ImageSelectedModel.size (); i++)
	{
		m_ImageVector.addElement (ImageSelectedModel.elementAt (i));
		m_PartialIVector.addElement (ImageSelectedModel.elementAt (i));
	}
	System.out.println (m_ImageVector + "  " + m_PartialIVector);
	m_tblName = getTableName ();
	pThreshold = getPThreshold ();
	sqlFileName = getOutfile ();
	dbms = getDBMS ();
	algorithm = getAlgorithm ();
	
        
        if(test==7 && m_PartialDVector.size() > 2){
        //Calculo de combinaciones
        int n = m_PartialDVector.size();
        int r = 2;
        int n_r = n-r;
        
        int fact_r = 2*1;
        int fact_n = n;
        int fact_n_r = n_r;
        for(int i=1; i < n; i++){
            fact_n = fact_n * (n - i) ;
        }
        for(int i=1; i < n_r; i++){
            fact_n_r = fact_n_r * (n_r - i) ;
        }
        
        int repeticiones = fact_n/(fact_r * fact_n_r);
        
        int cont=1;        
        int cont_ant=1;     
        for(int i=0; i<repeticiones; i++){
            Vector combinacion = new Vector();
            if(cont == n){
                cont_ant++;
                cont = cont_ant;
            }
            combinacion.add(m_PartialDVector.elementAt(cont_ant-1));
            combinacion.add(m_PartialDVector.elementAt(cont));
            OLAPStatTest OT = new OLAPStatTest ();
            OT.RunFromInterface (m_DimensionVector, combinacion, m_MeasureVector, m_PartialMVector, m_ImageVector, m_PartialIVector, m_tblName, combinacion.elementAt(0).toString() + combinacion.elementAt(1).toString(), algorithm, test, parameter, dbms, pThreshold, sqlFileName);
            callOLAPStatGraphics (dbms,combinacion,combinacion.elementAt(0).toString() + combinacion.elementAt(1).toString());
            cont++;
        }
        }
        else{
            OLAPStatTest OT = new OLAPStatTest ();
            OT.RunFromInterface (m_DimensionVector, m_PartialDVector, m_MeasureVector, m_PartialMVector, m_ImageVector, m_PartialIVector, m_tblName, "",algorithm, test, parameter, dbms, pThreshold, sqlFileName);
                    
            callOLAPStatGraphics (dbms);
        }
        
	}
	
	private void callOLAPStatGraphics (String dbms)
	{
		OSG = new OLAPStatGraphics (dbms, m_PartialDVector, m_PartialMVector, m_PartialIVector, m_tblName,"", test, 0, "");
	}

        private void callOLAPStatGraphics (String dbms, Vector combinacion, String idtable)
	{
		OSG = new OLAPStatGraphics (dbms, combinacion, m_PartialMVector, m_PartialIVector, m_tblName ,idtable, test, 0, "");
	}
        
	private String getTableName ()
	{
		return TableNameText.getText ();
	}
	
	private int getPThreshold ()
	{
		return Integer.parseInt (PThresholdText.getText ());
	}
	
	private String getDBMS ()
	{
		return (String)DBMSCombo.getSelectedItem();
	}
	
	private String getOutfile ()
	{
		return OutfileText.getText ();
	}
	
	private int getAlgorithm ()
	{
		return algorithm;
	}
/*
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(
                                  "javax.swing.plaf.metal.MetalLookAndFeel");
                                //  "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                                //UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                new OLAPInterface().setVisible(true);
            }
        });
    }
*/
}


