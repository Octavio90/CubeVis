//import java.awt.Component;

package source;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import static javax.swing.GroupLayout.Alignment.*;

import java.io.*;
import java.util.*;

class WindowsListener2 extends WindowAdapter {
  public void windowClosing(WindowEvent event) {
    System.exit(0);
  }
}

public class OLAPInterfaceSelectDM extends JFrame 
							implements ActionListener {

	JLabel TableNameLabel;
	JTextField TableNameText;
        
        JLabel ColumnsLabel;
	DefaultListModel ColumnsAllModel, ColumnsSelectedModel;
	JList ColumnsAllList, ColumnsSelectedList;
	JScrollPane ColumnsAllScroll, ColumnsSelectedScroll;
	JButton ColumnsInsertButton, ColumnsRemoveButton, ColumnsAddButton;
	JTextField ColumnsAddText;
	
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
	
	JButton ExecuteButton;

Vector m_ColumnsVector;
Vector m_PartialCVector;
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

OLAPStatGraphics OSG;
DBSQLConnect dbConn;

    public OLAPInterfaceSelectDM(Vector d, Vector measures, Vector img, Vector pd, Vector pm, Vector pi, String tblname, int algorm, String dbmsType, int p, String outName)
	{
                m_ColumnsVector = d;
                m_PartialCVector = pd;
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
                
                //COLUMNS
		ColumnsLabel = new JLabel ("Table Columns: ");
		ColumnsAllModel = new DefaultListModel ();
			for (int i = 0; i < m_ColumnsVector.size (); i++)
				if (!m_PartialCVector.contains (m_ColumnsVector.elementAt (i)))
					ColumnsAllModel.addElement (m_ColumnsVector.elementAt (i));
		ColumnsAllList = new JList (ColumnsAllModel);
			ColumnsAllList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			ColumnsAllList.setLayoutOrientation (JList.VERTICAL);
		ColumnsAllScroll = new JScrollPane (ColumnsAllList);
			ColumnsAllScroll.setPreferredSize (new Dimension (250, 240));
		ColumnsSelectedModel = new DefaultListModel ();
			for (int i = 0; i < m_PartialCVector.size (); i++)
				ColumnsSelectedModel.addElement (m_PartialDVector.elementAt (i));
		ColumnsSelectedList = new JList (ColumnsSelectedModel);
			ColumnsSelectedList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			ColumnsSelectedList.setLayoutOrientation (JList.HORIZONTAL_WRAP);
		ColumnsSelectedScroll = new JScrollPane (ColumnsSelectedList);
			ColumnsSelectedScroll.setPreferredSize (new Dimension (250, 240));
		ColumnsInsertButton = new JButton (">>>>");
			ColumnsInsertButton.setActionCommand ("InsertDimensions");
			ColumnsInsertButton.addActionListener (this);
		ColumnsRemoveButton = new JButton ("<<<<");
			ColumnsRemoveButton.setActionCommand ("RemoveDimensions");
			ColumnsRemoveButton.addActionListener (this);        
                        
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
			DimensionsAllScroll.setPreferredSize (new Dimension (250, 120));
		DimensionsSelectedModel = new DefaultListModel ();
			for (int i = 0; i < m_PartialDVector.size (); i++)
				DimensionsSelectedModel.addElement (m_PartialDVector.elementAt (i));
		DimensionsSelectedList = new JList (DimensionsSelectedModel);
			DimensionsSelectedList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			DimensionsSelectedList.setLayoutOrientation (JList.HORIZONTAL_WRAP);
		DimensionsSelectedScroll = new JScrollPane (DimensionsSelectedList);
			DimensionsSelectedScroll.setPreferredSize (new Dimension (250, 120));
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
			MeasuresAllScroll.setPreferredSize (new Dimension (250, 120));
		MeasuresSelectedModel = new DefaultListModel ();
			for (int i = 0; i < m_PartialMVector.size (); i++)
				MeasuresSelectedModel.addElement (m_PartialMVector.elementAt (i));
		MeasuresSelectedList = new JList (MeasuresSelectedModel);
			MeasuresSelectedList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			MeasuresSelectedList.setLayoutOrientation (JList.VERTICAL);
		MeasuresSelectedScroll = new JScrollPane (MeasuresSelectedList);
			MeasuresSelectedScroll.setPreferredSize (new Dimension (250, 120));
		MeasuresInsertButton = new JButton (">>>>");
			MeasuresInsertButton.setActionCommand ("InsertMeasures");
			MeasuresInsertButton.addActionListener (this);
		MeasuresRemoveButton = new JButton ("<<<<");
			MeasuresRemoveButton.setActionCommand ("RemoveMeasures");
			MeasuresRemoveButton.addActionListener (this);
		
		
		ExecuteButton = new JButton ("Finish");
			ExecuteButton.setActionCommand ("Finish");
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
				.addComponent (ColumnsLabel)
				.addComponent (ColumnsAllScroll)
				
			)
			.addGroup (layout.createParallelGroup (LEADING)
				.addComponent (DimensionsInsertButton)
				.addComponent (DimensionsRemoveButton)
				.addComponent (MeasuresInsertButton)
				.addComponent (MeasuresRemoveButton)
			)
			.addGroup (layout.createParallelGroup (LEADING)
                                .addComponent (DimensionsLabel)
				.addComponent (DimensionsSelectedScroll)
                                .addComponent (MeasuresLabel)
				.addComponent (MeasuresSelectedScroll)
				.addComponent (ExecuteButton)
			)
		);
		
		//VERTICAL
		layout.setVerticalGroup (layout.createSequentialGroup ()
			.addGroup (layout.createParallelGroup (BASELINE)
				.addComponent (TableNameLabel)
				.addComponent (TableNameText)
			)
                        .addGroup (layout.createParallelGroup ()
                            .addComponent (ColumnsLabel)
                            .addComponent (DimensionsLabel)
                        )
                        .addGroup (layout.createParallelGroup ()
                            .addComponent (ColumnsAllScroll)
                            .addGroup (layout.createSequentialGroup ()
				.addComponent (DimensionsInsertButton)
                                .addComponent (DimensionsRemoveButton)
                                .addComponent (MeasuresInsertButton)
                                .addComponent (MeasuresRemoveButton)
                             )   
                            .addGroup (layout.createSequentialGroup ()
				.addComponent (DimensionsSelectedScroll)
                                .addComponent (MeasuresLabel)
                                .addComponent (MeasuresSelectedScroll)
                            )    
			)
			
			
			.addComponent (ExecuteButton)
			
		);

        setTitle("OLAP Statistical Tests - Segunda Prueba");
        pack();
		addWindowListener(new WindowsListener2());
//        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
	//FUNCION: Interacción con la interfaz
	public void actionPerformed(ActionEvent e) {
		String AC = e.getActionCommand ();
                int[] ColAll = ColumnsAllList.getSelectedIndices();
		int[] DimAll = DimensionsAllList.getSelectedIndices ();
		int[] DimSel = DimensionsSelectedList.getSelectedIndices ();
		int[] MeaAll = MeasuresAllList.getSelectedIndices ();
		int[] MeaSel = MeasuresSelectedList.getSelectedIndices ();
		
		
		if (AC.equals ("RemoveDimensions"))
		{
			for (int i = 0; i < DimSel.length; i++)
			{
				ColumnsAllModel.addElement (DimensionsSelectedModel.elementAt (DimSel[i]-i));
				DimensionsSelectedModel.removeElementAt (DimSel[i] - i);
			}		
		}
		else if (AC.equals ("InsertDimensions"))
		{
			for (int i = 0; i < ColAll.length; i++)
			{
				DimensionsSelectedModel.addElement (ColumnsAllModel.elementAt (ColAll[i]-i));
				ColumnsAllModel.removeElementAt (ColAll[i] - i);
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
				ColumnsAllModel.addElement (MeasuresSelectedModel.elementAt (MeaSel[i]-i));
				MeasuresSelectedModel.removeElementAt (MeaSel[i] - i);
			}		
		}
		else if (AC.equals ("InsertMeasures"))
		{
			for (int i = 0; i < ColAll.length; i++)
			{
				MeasuresSelectedModel.addElement (ColumnsAllModel.elementAt (ColAll[i]-i));
				ColumnsAllModel.removeElementAt (ColAll[i] - i);
			}
		}
		else if (AC.equals ("AddMeasures"))
		{
			String tt = MeasuresAddText.getText ();
			if (tt.length () > 0)
				MeasuresAllModel.addElement (tt);
			MeasuresAddText.setText ("");
		}

		else if (AC.equals ("Finish"))
			callOLAPStatTest ();
		else if (AC.equals ("Algorithm1"))
			algorithm = 1;
		else if (AC.equals ("Algorithm2"))
			algorithm = 2;
		else if (AC.equals ("Algorithm3"))
			algorithm = 3;
		else
			System.out.println ("Unknown");
    }
	
	private void callOLAPStatTest ()
	{
		
        OLAPStatTest OT = new OLAPStatTest ();
        
	m_DimensionVector.clear ();
	for (int i = 0; i < DimensionsSelectedModel.size (); i++)
		m_DimensionVector.addElement (DimensionsSelectedModel.elementAt (i));
        java.util.Collections.sort (m_DimensionVector);
        OT.setm_FullDVector(m_DimensionVector);
        
		
	m_MeasureVector.clear ();
	for (int i = 0; i < MeasuresSelectedModel.size (); i++)
		m_MeasureVector.addElement (MeasuresSelectedModel.elementAt (i));
        java.util.Collections.sort (m_MeasureVector);
        OT.setm_FullMVector(m_MeasureVector);
        
	
	System.out.println (m_DimensionVector + "  " + m_MeasureVector);

        OT.initializeNewInputTable();
        OLAPStatTest.dbConn.stopConnection();
        this.setVisible(false);
        OT.runUserInterface();
        
	}
	
	private void callOLAPStatGraphics (String dbms)
	{
                int test = 1;
		OSG = new OLAPStatGraphics (dbms, m_PartialDVector, m_PartialMVector, m_PartialIVector, m_tblName, "", test, 0, "");
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

