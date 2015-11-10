
package source;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.swing.JOptionPane;

class OLAPStatExperiment {

static String CR = "\r\n";

static String resultTableText = "_Result";
static String summaryTableText = "_Summary";
static String finalTableText = "_Final";
static String resultTempTText = "_TempT";
static String resultTempZText = "_TempZ";

static String technique = "olapstat";

DBSQLStatement DBS;
DBSQLConnect DBC;
int dbms;

int pInterface;
Vector pKVector;
Vector pEVector;
String pTableName;
int pAlgorithm;
int pSingleTable;
int pIndexing;
int pCalcAll;

String inputStr;

	OLAPStatExperiment (int pdbms)
	{

		dbms = pdbms;
                //NOTA: Quiere enviar un entero y el constructor esta declarado para recibir una cadena
		//DBS = new DBSQLStatement (dbms, true);
		//DBC = new DBSQLConnect (dbms, true, true);

                //EDGAR
                String database;
                switch(dbms){
                    case 1:
                        database = "postgresql";
                        break;
                    case 2:
                        database = "sqlserver";
                        break;
                    default:
                        database = "sqlserver";
                }

                //Crea el objeto para la conexión deacuerdo al manejador elegido
                DBS = new DBSQLStatement (database, true);
		DBC = new DBSQLConnect (database, true, true);
	}
	
	private void startDBConnect () { DBC.startConnection (); }
	private void stopDBConnect () { DBC.stopConnection (); }

        //FUNCION: Crea la cadena con los parámetros
	private void createInputStr ()
	{
		inputStr = "";
		
		inputStr += "interface=" + pInterface + ";";
		
		inputStr += "k=";
		for (int i = 0; i < pKVector.size (); i++)
			if (i == 0)
				inputStr += (String)pKVector.elementAt (i);
			else
				inputStr += "," + (String)pKVector.elementAt (i);
		inputStr += ";";
		
		inputStr += "e=";
		for (int i = 0; i < pEVector.size (); i++)
			if (i == 0)
				inputStr += (String)pEVector.elementAt (i);
			else
				inputStr += "," + (String)pEVector.elementAt (i);
		inputStr += ";";
				
		inputStr += "tablename=" + pTableName + ";";
		
		inputStr += "algorithm=" + pAlgorithm + ";";
		
		inputStr += "dbms=" + dbms + ";";
		
		inputStr += "SingleTable=" + pSingleTable + ";";

                //NOTA: Marca ERROR: unknown tag = Indexing
                //EDGAR
		//inputStr += "Indexing=" + pIndexing + ";";
		
		inputStr += "calcAll=" + pCalcAll + ";";	
	}
	
		// 0=1; 1=2; 2=4; 3=8; 4=12;
	private void VaryingK (PrintStream output, int start, int end, int algorithm, int index) throws IOException
	{
		boolean flag;

                //EDGAR
		pInterface = 1;
		pKVector = new Vector ();
		pEVector = new Vector ();
		//pTableName = "zmed655";
                pTableName = JOptionPane.showInputDialog(null,"Table Name: ");
		pAlgorithm = algorithm;
		pSingleTable = 1;
		pIndexing = index;
		pCalcAll = 1;
		
		// 0=1; 1=2; 2=4; 3=8; 4=12;
		/*for (int i = start; i <= end; i++)
		{
		if (i >= 0)
		{
			//pKVector.addElement ("oldyn");
		}
		if (i >= 1)
		{
			//pKVector.addElement ("sex");
		}
		if (i >= 2)
		{
			//pKVector.addElement ("hta");
			//pKVector.addElement ("diab");
		}
		if (i >= 3)
		{
			//pKVector.addElement ("hyplpd");
			//pKVector.addElement ("fhcad");
			//pKVector.addElement ("smoke");
			//pKVector.addElement ("claudi");
		}
		if (i >= 4)
		{
			//pKVector.addElement ("pangio");
			//pKVector.addElement ("pstroke");
			//pKVector.addElement ("pcarsur");
			//pKVector.addElement ("highchol");
		}
		
		//pEVector.addElement ("lad");
		//pEVector.addElement ("lcx");
		//pEVector.addElement ("lm");
		//pEVector.addElement ("rca");
                */
		createInputStr ();
		
		System.out.println (inputStr);
		output.println ("Input String: ");
		output.println (inputStr);
		output.println ("*****************************************************************");
		
		OLAPStatTest olap = new OLAPStatTest ();
		olap.RunOLAPStatTest (inputStr);
//		output.println (olap.getSqlFile ());
		
		//}
	}
	
		// 0=1; 1=2; 2=4; 3=8; 4=16;
	private void VaryingKThyroid (PrintStream output, int start, int end, int algorithm, int index) throws IOException
	{
		boolean flag;

                //EDGAR
		pInterface = 1;
		pKVector = new Vector ();
		pEVector = new Vector ();
		pTableName = "zThyroid";
		pAlgorithm = algorithm;
		pSingleTable = 1;
		pIndexing = index;
		pCalcAll = 1;
		
		// 0=1; 1=2; 2=4; 3=8; 4=12;
		for (int i = start; i <= end; i++)
		{
		if (i >= 0)
		{
			pKVector.addElement ("age");
		}
		if (i >= 1)
		{
			pKVector.addElement ("gender");
		}
		if (i >= 2)
		{
			pKVector.addElement ("on_thyroxine");
			pKVector.addElement ("query_thyroxine");
		}
		if (i >= 3)
		{
			pKVector.addElement ("on_antithyroid_med");
			pKVector.addElement ("sick");
			pKVector.addElement ("pregnant");
			pKVector.addElement ("surgery");
		}
		if (i >= 4)
		{
			pKVector.addElement ("I131_treatment");
			pKVector.addElement ("query_hypothyroid");
			pKVector.addElement ("query_hyperthyroid");
			pKVector.addElement ("lithium");
			pKVector.addElement ("goitre");
			pKVector.addElement ("tumor");
			pKVector.addElement ("hypopituitary");
			pKVector.addElement ("psych");
			
		}
		
		pEVector.addElement ("tsh");
		pEVector.addElement ("t3");
		pEVector.addElement ("tt4");
		pEVector.addElement ("t4u");
		pEVector.addElement ("fti");
		pEVector.addElement ("tbg");

		createInputStr ();
		
		System.out.println (inputStr);
		output.println ("Input String: ");
		output.println (inputStr);
		output.println ("*****************************************************************");
		
		OLAPStatTest olap = new OLAPStatTest ();
		olap.RunOLAPStatTest (inputStr);
//		output.println (olap.getSqlFile ());
		
		}
	}

	
    public static void main(String[] args) throws IOException {
                //Obtiene el manejador de base de datos
                int dbms = Integer.parseInt(JOptionPane.showInputDialog(null,"Select DBMS\n"
                        + "1 = Postgres\n"
                        + "2 = SQLserver"));
		if (String.valueOf(dbms).length() < 1)
		{
			System.out.println ("ERROR: Experiment requires input for dbms");
                        //NOTA: No hay opción de SQL server
			//System.out.println ("1 = Teradata; 2 = PostgreSQL; 3 = Oracle; 4 = MySQL");

                        //EDGAR
                        System.out.println ("1 = Teradata; 2 = SQLserver; 3 = Oracle; 4 = MySQL");

			System.out.println ("Example: java OLAPStatExperiment 1");
			return;
		}
		
		//int dbms = Integer.parseInt (args[0]);
                
		System.out.println ("DBMS: " + dbms);
		OLAPStatExperiment OE = new OLAPStatExperiment (dbms);
		
		String tablename;
		String curTableDesc;
		String AllMeasures;
		Vector Parm = new Vector ();
		Vector All = new Vector ();
		String sql = "";
		
		try {
        PrintStream output;
        FileOutputStream outputStream;
        File outputFile;
		//String filename = "D:\\Edgar\\Mis documentos\\NetBeansProjects\\JAVA3Doriginal\\ExperimentSQLserver.sql";
        //String filename = "C:\\cubevisEXE\\ExperimentSQLserver.sql";
        String filename = "/home/scidb/CubeVis-Files/ExperimentSQLserver.sql";
		switch (dbms)
		{
			case 1:
				//filename = "ExperimentTeradata.sql";
                        filename = "ExperimentPostgreSQL.sql";
				break;
			//case 2:
			//	filename = "ExperimentPostgreSQL.sql";
			//	break;
			case 3:
				filename = "ExperimentOracle.sql";
				break;

			//case 4:
                        //EDGAR Opcion para MySQL
                        case 2:
				filename = "ExperimentSQLserver.sql";
		}
		
		outputFile = new File (filename);
		boolean nFile = outputFile.createNewFile ();
        outputStream = new FileOutputStream (outputFile);
        output = new PrintStream (outputStream);

		output.println ();

		// FOR zmed655
		// 0=1; 1=2; 2=4; 3=8; 4=12;

		OE.startDBConnect ();
		//OE.VaryingKThyroid (output, 0, 3, 1, 1);	// all Dimensions Pre Index
		OE.stopDBConnect ();
                OE.VaryingK (output, 0, 4, 1, 1);
/*
		OE.startDBConnect ();
		OE.VaryingKThyroid (output, 0, 3, 2, 1);	// all Dimensions Direct Index
		OE.stopDBConnect ();
		
		OE.startDBConnect ();
		OE.VaryingKThyroid (output, 0, 3, 1, 0);	// all Dimensions Pre no Index
		OE.stopDBConnect ();

		OE.startDBConnect ();
		OE.VaryingKThyroid (output, 0, 3, 2, 0);	// all Dimensions Direct no Index
		OE.stopDBConnect ();

		OE.startDBConnect ();
		OE.VaryingKThyroid (output, 4, 4, 1, 0);	// 12 D Pre no index
		OE.stopDBConnect ();

		OE.startDBConnect ();
		OE.VaryingKThyroid (output, 4, 4, 1, 1);	// 12 D Pre index
		OE.stopDBConnect ();

		OE.startDBConnect ();
		OE.VaryingKThyroid (output, 4, 4, 2, 0);	// 12 D Direct no index
		OE.stopDBConnect ();

		OE.startDBConnect ();
		OE.VaryingKThyroid (output, 4, 4, 2, 1);	// 12 D Direct index
		OE.stopDBConnect ();
*/
		System.out.println ("Experiment SQL placed in: " + filename);
		
		} catch (Exception e) {}
	}
}