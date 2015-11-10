/*-
=========================================
OLAPStatTest.java
Performs Statistical tests on Cubes using Database
Zhibo Chen
Started: October 2006
Revised: (Revisions for EACH date are listed in order of importance)
2007-01-15 -	Changed input style from each argument group separated by a semicolon and space to just a semicolon.
			This create one long string which includes all the arguments for a particular input.
2007-01-25 - 	Changed the order of the functions and altered some function names to comply with agreement on how variables
			should be used.  Also created a CreateTable helper function.
2007-02-04 -	Changing insert into, create table, and other sql code to work with Teradata
2007-02-05 - 	Major Change to structure of program.  Created DBStatement class to handle all SQL commands.
			DBStatement can be extended and can work with multiple databases.
2007-02-12 -	1) All generated code now uses DBStatement.  Results table was not using this scheme before.
			2) GroupBy Columns now appear first in selects, allowing for the use of primary keys/indexes
			3) OrderBy deleted from tables.  Was used for user purposes, not needed
			4) Added pThreshold paramater to allow use to control the population threshold of Z-test (defaults to 25)
			5) Added parameter called outfile to allow user ability to pick filename of output sql code
			6) aggregate parameter now called measure
			7) Changed RETURN to CR
2007-02-15 -	Created debug.txt
2007-02-20 -	Altered program to work with JDBC in DBConnect class
2007-03-09 -	Major alteration of the Program
			1) Includes a connection to the interface class for userInterface
			2) Added option for algorithm
				a) 1 = pre-compute table to the finest granularity and then aggregate everything from there
				b) 2 = don't pre-compute.  Aggregate directly from the main data table
			3) For interface, don't run all possible sets.  Only run the set chosen by the user
			4) For non-interface, run all possible sets.
2007-03-10 -	Added function for determining name of tables
			1) Lookup table added.  Now works as a file LookupTable.dat.  Will evetually change to something stored in db
			2) Naming convention is as follows: tablename followed by "D" then the colums (changed into numbers according to lookuptable) then "M" followed by measures (also numbers according to lookuptable)
2007-03-12 -	1) Completed overhaul of program
			2) Program now based on a different flow, with interface being the important deciding factor.  Interface side is now completed.
2007-03-20 -	1) Added user input variable for SingleTable (Single or Multiple)
			2) Removed original lookup idea. Now using TableInfoLookup table inside database. Also will prompt user for columns for first-time tables.
			3) Creates a results and summary table for use with single table idea.
2007-03-24 -	1) Finished insertion of results into summary and result tables
			2) Changed names of DBStatement to DBSQLStatement and DBConnect to DBSQLConnect
			3) Added DBSQLUtil class for general helper functions
=========================================
NOTES
need to create the table structure variable
*/

package source;
import java.io.*;
import java.util.*;
import java.sql.*;
import javax.swing.*;

public class OLAPStatTest
{

// 1 = TeraData
// 2 = PostgreSQL
// 3 = Oracle
// 4 = MySQL	
static String dbmsType;
boolean debugFlag = false;
boolean precomputeFlag = true;
boolean resultSumFlag = true;
boolean connectFlag = true;
boolean checkFlag = true;
boolean printFlag = true;
boolean saveFileFlag = true;

boolean keepTimeFlag = true;

String parameterStr;
String expParm;

static DBSQLStatement dbFunctions;
static DBSQLConnect dbConn;

java.util.Date startTime, finishTime, tempTime;

final String CR = "\r\n";

// FOR flags
// flags=option1,option2..etc   (debug, resultsum, precompute, connect, checkresult, print, savefile) add no in front of each for false
// fulld=option1,option2,..etc to manually enter dimensions
// fulle=option1,option2,etc to manually enter measurse
// fullf=option1,option2,etc to manually enter images
// expStr=option1    to enter in parameter string

final String ERRORMSG = CR + "Syntax: [tags=option1,option2,...];" + CR + CR + "\t interface=option;  1=yes, 0=no   (default=0)" + CR + "\t k=col1,col2,col3...;   (default=dim1)" + CR + "\t e=aggr1,aggr2,aggr3,...;    (default=meas1)" + CR + "\t tablename=option;    (default=T)" + CR + "\t algorithm=option;  1=precompute, 2=dataset  (default=1)" + CR + "\t dbms=option;  1=Teradata, 2=PostgreSQL, 3=Oracle, 4=MySQL (default=1)" + CR + "\t pThreshold=option    (default=25)" + CR + "\t calcAll=option;  1=true, 0=false  (default=0)" + CR + "\t outfile=option;   (default=olapst.sql)" + CR + CR + "Optimizations" + CR + "SingleTable=option; 1=true, 0=false (default=1)" + CR + CR + "Example: java OLAPStatTest interface=1;k=A,B,C;e=M1,M2,M3;tablename=tbl1;algorithm=1;dbms=4;pThreshold=20;calcAll=1;outfile=results.sql;SingleTable=1;";

// Parameters
String SQLextension = ".sql";

String sqlFile;
String sqlFileName = "olapstat.sql";
PrintStream sqlOutput;
FileOutputStream sqlOutputStream;
File sqlOutputFile;
		
String debugFile;
String debugFileName = "debug.txt";
PrintStream debugOutput;
FileOutputStream debugOutputStream;
File debugOutputFile;

String msgStr;

final String technique = "olapstat";

static final String resultTableText = "_Result";
static final String summaryTableText = "_Summary";
static final String finalTableText = "_Final";
static final String resultTempTText = "_TempT";
static final String resultTempZText = "_TempZ";
static final String MSVTableName = "tempMSV";
static final String CompareStatementsTableName = "tempCompare";

// column types, leave spaces around string
final String dimType= " real ";					// all available precision as opposed to decimal(10,2)
final String idType=  " int ";					// for segment id, cutoff
final String countType= " int ";				// for count(*)
final String floatFormat= "(format '999.99')";

static Vector m_FullCVector;
static Vector m_FullDVector;
static Vector m_FullMVector;
static Vector m_FullIVector;
static String m_tblName;
String idTable;
int m_uniqueID;

//static double[] m_ZValues = {1.645, 1.96, 2.575};
static double[] m_ZValues = {1.88,1.96,2.04};
static double[] chi_ZValues = {4.709,5.024,5.412};
//static String[] m_ZValuesString = {"More than 0.1", "Between 0.1 and 0.05", "Between 0.05 and 0.01", "Less than 0.01" };
static String[] m_ZValuesString = {"0", "1", "2", "3" };
int pThreshold;
boolean userInterface;
int algorithm;
int test;
String parameter;
boolean calcAll;

//Optimization flags
boolean SingleTable;

// FOR INTERFACE
Vector m_PartialDVector;
Vector m_PartialMVector;
Vector m_PartialIVector;

//for tablenames
Vector LookupList;

/* for work with DBSQLStatement */
// CreateTable
Vector ColName;
Vector ColType;
Vector PrimeKey;
static String TblName = "";
String SelectStatement = "";

//Insert
Vector Values;

// Select
Vector Columns;
Vector TableName;
Vector Predicate;
Vector OrderBy;
Vector GroupBy;

//EDGAR
Vector dimSize;
Vector values;


int numTables;

double[] gl1 = {0.0000393,  0.0100,  0.0717,  0.207,  0.412,  0.676,  0.989,  1.344,  1.735,  2.156, 
                    2.603,   3.074,   3.565,  4.075,  4.601,  5.142,  5.697,  6.265,  6.844,  7.434,
                    8.034,   8.643,   9.260,  9.886, 10.520, 11.160, 11.808, 12.461, 13.121, 13.787,
                   14.458,  15.134,  15.815, 16.501, 17.192, 17.192, 17.192, 17.192, 17.192, 20.707,
                   20.707,  20.707,  20.707, 20.707, 24.311, 24.311, 24.311, 24.311, 24.311, 27.991,
                   27.991,  27.991,  27.991, 27.991, 27.991, 27.991, 27.991, 27.991, 27.991, 35.535,
                   35.535,  35.535,  35.535, 35.535, 35.535, 35.535, 35.535, 35.535, 35.535, 43.275,
                   43.275,  43.275,  43.275, 43.275, 43.275, 43.275, 43.275, 43.275, 43.275, 51.172,
                   51.172,  51.172,  51.172, 51.172, 51.172, 51.172, 51.172, 51.172, 51.172, 59.196,
                   59.196,  59.196,  59.196, 59.196, 59.196, 59.196, 59.196, 59.196, 59.196, 67.328};

double[] gl2 = {   7.879,  10.597,  12.838,  14.860,  16.750,  18.548,  20.278,  21.955,  23.589,  25.188,
                  26.757,  28.300,  29.819,  31.319,  32.801,  34.267,  35.718,  37.156,  38.582,  39.997,
                  41.401,  42.796,  44.181,  45.558,  46.928,  48.290,  49.645,  50.993,  52.336,  53.672,
                  55.003,  56.328,  57.648,  58.964,  60.275,  60.275,  60.275,  60.275,  60.275,  66.766,
                  66.766,  66.766,  66.766,  66.766,  66.766,  66.766,  66.766,  66.766,  66.766,  73.166,
                  73.166,  73.166,  73.166,  73.166,  73.166,  73.166,  73.166,  73.166,  73.166,  79.490,
                  79.490,  79.490,  79.490,  79.490,  79.490,  79.490,  79.490,  79.490,  79.490,  91.952,
                  91.952,  91.952,  91.952,  91.952,  91.952,  91.952,  91.952,  91.952,  91.952, 104.215,
                  104.215, 104.215, 104.215, 104.215, 104.215, 104.215, 104.215, 104.215, 104.215,116.321,
                  116.321, 116.321, 116.321, 116.321, 116.321, 116.321, 116.321, 116.321, 116.321,128.299,
                  128.299, 128.299, 128.299, 128.299, 128.299, 128.299, 128.299, 128.299, 128.299,140.169};

double[][] gl0v = new double[150][121];
double[][] gl005v = new double[150][121];
double[][] gl995v = new double[150][121];

        
	OLAPStatTest ()
	{
            /************ VALORES PARA PRUEBA BILATERAL 0.995*************/
                double[] vtemp24 =                                          
                  /*24*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  2.97,   2.92,   2.87,   2.83,   2.79,   2.76,   2.73,
                            2.73,   2.73,   2.73,   2.73,   2.73,   2.73,   2.73,   2.73,   2.73,   2.50,
                            2.50,   2.50,   2.50,   2.50,   2.50,   2.50,   2.50,   2.50,   2.50,   2.50,
                            2.50,   2.50,   2.50,   2.50,   2.50,   2.50,   2.50,   2.50,   2.50,   2.29,
                            2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,
                            2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,
                            2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,
                            2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,
                            2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,
                            2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.29,   2.09,
                            1.90
                        };
                gl995v[23]=gl995v[24]=gl995v[25]=gl995v[26]=gl995v[27]=gl995v[28]=vtemp24;
                 double[] vtemp30 =                                       
                  /*30*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  2.87,   2.82,   2.77,   2.73,   2.69,   2.66,   2.63,
                            2.63,   2.63,   2.63,   2.63,   2.63,   2.63,   2.63,   2.63,   2.63,   2.40,
                            2.40,   2.40,   2.40,   2.40,   2.40,   2.40,   2.40,   2.40,   2.40,   2.40,
                            2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,
                            2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,
                            2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,
                            2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,
                            2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,
                            2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,
                            2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   2.19,   1.98,
                            1.79
                        };
                 gl995v[29]=gl995v[30]=gl995v[31]=gl995v[32]=gl995v[33]=gl995v[34]=gl995v[35]=gl995v[36]=gl995v[37]=gl995v[38]=vtemp30;
                 double[] vtemp40 =                                       
                  /*40*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  2.77,   2.72,   2.67,   2.63,   2.59,   2.56,   2.52,
                            2.52,   2.52,   2.52,   2.52,   2.52,   2.52,   2.52,   2.52,   2.52,   2.30,
                            2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,
                            2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.08,
                            2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,
                            2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,
                            2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,
                            2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,
                            2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,
                            2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   2.08,   1.87,
                            1.67
                  };
                 gl995v[39]=gl995v[40]=gl995v[41]=gl995v[42]=gl995v[43]=gl995v[44]=gl995v[45]=gl995v[46]=gl995v[47]=gl995v[48]=vtemp40;
                 gl995v[49]=gl995v[50]=gl995v[51]=gl995v[52]=gl995v[53]=gl995v[54]=gl995v[55]=gl995v[56]=gl995v[57]=gl995v[58]=vtemp40;
                 double[] vtemp60 =                                       
                  /*60*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  2.66,   2.61,   2.56,   2.52,   2.48,   2.45,   2.42,
                            2.42,   2.42,   2.42,   2.42,   2.42,   2.42,   2.42,   2.42,   2.42,   2.18,
                            2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,
                            2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   1.96,
                            1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,
                            1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,
                            1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,
                            1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,
                            1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,
                            1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.96,   1.75,
                            1.53
                  };
                 gl995v[59]=gl995v[60]=gl995v[61]=gl995v[62]=gl995v[63]=gl995v[64]=gl995v[65]=gl995v[66]=gl995v[67]=gl995v[68]=vtemp60;
                 gl995v[69]=gl995v[70]=gl995v[71]=gl995v[72]=gl995v[73]=gl995v[74]=gl995v[75]=gl995v[76]=gl995v[77]=gl995v[78]=vtemp60;
                 gl995v[79]=gl995v[80]=gl995v[81]=gl995v[82]=gl995v[83]=gl995v[84]=gl995v[85]=gl995v[86]=gl995v[87]=gl995v[88]=vtemp60;
                 gl995v[89]=gl995v[90]=gl995v[91]=gl995v[92]=gl995v[93]=gl995v[94]=gl995v[95]=gl995v[96]=gl995v[97]=gl995v[98]=vtemp60;
                 gl995v[99]=gl995v[100]=gl995v[101]=gl995v[102]=gl995v[103]=gl995v[104]=gl995v[105]=gl995v[106]=gl995v[107]=gl995v[108]=vtemp60;
                 gl995v[109]=gl995v[110]=gl995v[111]=gl995v[112]=gl995v[113]=gl995v[114]=gl995v[115]=gl995v[116]=gl995v[117]=gl995v[118]=vtemp60;
                 double[] vtemp120 =                                       
                  /*120*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  2.55,   2.50,   2.45,   2.41,   2.37,   2.33,   2.30,
                            2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.30,   2.06,
                            2.06,   2.06,   2.06,   2.06,   2.06,   2.06,   2.06,   2.06,   2.06,   2.06,
                            2.06,   2.06,   2.06,   2.06,   2.06,   2.06,   2.06,   2.06,   2.06,   1.83,
                            1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,
                            1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,
                            1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,
                            1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,
                            1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,
                            1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.83,   1.61,
                            1.36
                  };
                 gl995v[119]=gl995v[120]=gl995v[121]=gl995v[122]=gl995v[123]=gl995v[124]=gl995v[125]=gl995v[126]=gl995v[127]=gl995v[128]=vtemp120;
                 gl995v[129]=gl995v[130]=gl995v[131]=gl995v[132]=gl995v[133]=gl995v[134]=gl995v[135]=gl995v[136]=gl995v[137]=gl995v[138]=vtemp120;
                 gl995v[139]=gl995v[140]=gl995v[141]=gl995v[142]=gl995v[143]=gl995v[144]=gl995v[145]=gl995v[146]=gl995v[147]=gl995v[148]=vtemp120;
                 double[] vtemp150 =                                       
                  /*150*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  2.43,   2.38,   2.33,   2.29,   2.25,   2.21,   2.18,
                            2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   2.18,   1.93,
                            1.93,   1.93,   1.93,   1.93,   1.93,   1.93,   1.93,   1.93,   1.93,   1.93,
                            1.93,   1.93,   1.93,   1.93,   1.93,   1.93,   1.93,   1.93,   1.93,   1.69,
                            1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,
                            1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,
                            1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,
                            1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,
                            1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,
                            1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.69,   1.43,
                            1.00
                  };
                 
                 gl995v[149]=vtemp150;
                 
                /************ VALORES PARA PRUEBA BILATERAL 0.005*************/
                double[] vtemp25 =                                          
                  /*25*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0.343,   0.345,   0.347,   0.349,   0.351,   0.353,   0.355,
                            0.355,  0.355,  0.355,  0.355,  0.362,  0.362,  0.362,  0.362,  0.362,  0.368,
                            0.368,  0.368,  0.368,  0.368,  0.373,  0.373,  0.373,  0.373,  0.373,  0.377,
                            0.377,  0.377,  0.377,  0.377,  0.380,  0.380,  0.380,  0.380,  0.380,  0.383,
                            0.383,  0.383,  0.383,  0.383,  0.386,  0.386,  0.386,  0.386,  0.386,  0.388,
                            0.388,  0.388,  0.388,  0.388,  0.390,  0.390,  0.390,  0.390,  0.390,  0.392,
                            0.392,  0.392,  0.392,  0.392,  0.393,  0.393,  0.393,  0.393,  0.393,  0.395,
                            0.395,  0.395,  0.395,  0.395,  0.396,  0.396,  0.396,  0.396,  0.396,  0.397,
                            0.397,  0.397,  0.397,  0.397,  0.397,  0.397,  0.397,  0.397,  0.397,  0.397,
                            0.397,  0.397,  0.397,  0.397,  0.397,  0.397,  0.397,  0.397,  0.397,  0.397,
                            0.416};
                            
                      
                gl005v[24]=gl005v[25]=gl005v[26]=gl005v[27]=gl005v[28]=vtemp25;
                 double[] vtemp302 =                                       
                  /*30*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0.367, 0.369,   0.372, 0.374,   0.376,  0.379,  0.381,
                            0.381,  0.381,  0.381,  0.381,  0.389,  0.389,  0.389,  0.389,  0.389,  0.396,
                            0.396,  0.396,  0.396,  0.396,  0.402,  0.402,  0.402,  0.402,  0.402,  0.407,
                            0.407,  0.407,  0.407,  0.407,  0.411,  0.411,  0.411,  0.411,  0.411,  0.414,
                            0.414,  0.414,  0.414,  0.414,  0.417,  0.417,  0.417,  0.417,  0.417,  0.420,
                            0.420,  0.420,  0.420,  0.420,  0.422,  0.422,  0.422,  0.422,  0.422,  0.424,
                            0.424,  0.424,  0.424,  0.424,  0.426,  0.426,  0.426,  0.426,  0.426,  0.428,
                            0.428,  0.428,  0.428,  0.428,  0.429,  0.429,  0.429,  0.429,  0.429,  0.430,
                            0.430,  0.430,  0.430,  0.430,  0.430,  0.430,  0.430,  0.430,  0.430,  0.430,
                            0.430,  0.430,  0.430,  0.430,  0.430,  0.430,  0.430,  0.430,  0.430,  0.430,
                            0.453};
                            
                 gl005v[29]=gl005v[30]=gl005v[31]=gl005v[32]=gl005v[33]=vtemp302;
                 double[] vtemp35 =                                       
                  /*35*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0.385,   0.388,   0.391,   0.393,   0.396,   0.398,   0.401,
                            0.401,  0.401,  0.401,  0.401,  0.410,  0.410,  0.410,  0.410,  0.410,  0.418,
                            0.418,  0.418,  0.418,  0.418,  0.424,  0.424,  0.424,  0.424,  0.424,  0.430,
                            0.430,  0.430,  0.430,  0.430,  0.434,  0.434,  0.434,  0.434,  0.434,  0.438,
                            0.438,  0.438,  0.438,  0.438,  0.442,  0.442,  0.442,  0.442,  0.442,  0.445,
                            0.445,  0.445,  0.445,  0.445,  0.447,  0.447,  0.447,  0.447,  0.447,  0.450,
                            0.450,  0.450,  0.450,  0.450,  0.452,  0.452,  0.452,  0.452,  0.452,  0.454,
                            0.454,  0.454,  0.454,  0.454,  0.455,  0.455,  0.455,  0.455,  0.455,  0.457,
                            0.457,  0.457,  0.457,  0.457,  0.457,  0.457,  0.457,  0.457,  0.457,  0.457,
                            0.457,  0.457,  0.457,  0.457,  0.457,  0.457,  0.457,  0.457,  0.457,  0.457,
                            0.484
                  };
                 gl005v[34]=gl005v[35]=gl005v[36]=gl005v[37]=gl005v[38]=vtemp35;
                 
                 double[] vtemp402 =                                       
                  /*40*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0.400,   0.403,   0.406,   0.409,   0.411,   0.414,   0.416,
                            0.416,  0.416,  0.416,  0.416,  0.427,  0.427,  0.427,  0.427,  0.427,  0.436,
                            0.436,  0.436,  0.436,  0.436,  0.443,  0.443,  0.443,  0.443,  0.443,  0.449,
                            0.449,  0.449,  0.449,  0.449,  0.454,  0.454,  0.454,  0.454,  0.454,  0.458,
                            0.458,  0.458,  0.458,  0.458,  0.462,  0.462,  0.462,  0.462,  0.462,  0.465,
                            0.465,  0.465,  0.465,  0.465,  0.468,  0.468,  0.468,  0.468,  0.468,  0.471,
                            0.471,  0.471,  0.471,  0.471,  0.473,  0.473,  0.473,  0.473,  0.473,  0.475,
                            0.475,  0.475,  0.475,  0.475,  0.477,  0.477,  0.477,  0.477,  0.477,  0.479,
                            0.479,  0.479,  0.479,  0.479,  0.479,  0.479,  0.479,  0.479,  0.479,  0.479,
                            0.479,  0.479,  0.479,  0.479,  0.479,  0.479,  0.479,  0.479,  0.479,  0.479,
                            0.509
                  };
                 gl005v[39]=gl005v[40]=gl005v[41]=gl005v[42]=gl005v[43]=gl005v[44]=gl005v[45]=gl005v[46]=gl005v[47]=gl005v[48]=vtemp402;
                 
                 double[] vtemp50 =                                       
                  /*50*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0.421,   0.425,   0.428,   0.432,   0.435,   0.437,   0.440,
                            0.440,  0.440,  0.440,  0.440,  0.452,  0.452,  0.452,  0.452,  0.452,  0.462,
                            0.462,  0.462,  0.462,  0.462,  0.470,  0.470,  0.470,  0.470,  0.470,  0.477,
                            0.477,  0.477,  0.477,  0.477,  0.483,  0.483,  0.483,  0.483,  0.483,  0.488,
                            0.488,  0.488,  0.488,  0.488,  0.492,  0.492,  0.492,  0.492,  0.492,  0.496,
                            0.496,  0.496,  0.496,  0.496,  0.500,  0.500,  0.500,  0.500,  0.500,  0.503,
                            0.503,  0.503,  0.503,  0.503,  0.506,  0.506,  0.506,  0.506,  0.506,  0.508,
                            0.508,  0.508,  0.508,  0.508,  0.510,  0.510,  0.510,  0.510,  0.510,  0.512,
                            0.512,  0.512,  0.512,  0.512,  0.512,  0.512,  0.512,  0.512,  0.512,  0.512,
                            0.512,  0.512,  0.512,  0.512,  0.512,  0.512,  0.512,  0.512,  0.512,  0.512,
                            0.549
                            
                  };
                 gl005v[49]=gl005v[50]=gl005v[51]=gl005v[52]=gl005v[53]=gl005v[54]=gl005v[55]=gl005v[56]=gl005v[57]=gl005v[58]=vtemp50;
                 
                 double[] vtemp602 =                                       
                  /*50*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0.437,   0.441,   0.444,   0.448,   0.451,   0.454,   0.457,
                            0.457,  0.457,  0.457,  0.457,  0.470,  0.470,  0.470,  0.470,  0.470,  0.481,
                            0.481,  0.481,  0.481,  0.481,  0.490,  0.490,  0.490,  0.490,  0.490,  0.498,
                            0.498,  0.498,  0.498,  0.498,  0.504,  0.504,  0.504,  0.504,  0.504,  0.510,
                            0.510,  0.510,  0.510,  0.510,  0.515,  0.515,  0.515,  0.515,  0.515,  0.519,
                            0.519,  0.519,  0.519,  0.519,  0.523,  0.523,  0.523,  0.523,  0.523,  0.526,
                            0.526,  0.526,  0.526,  0.526,  0.530,  0.530,  0.530,  0.530,  0.530,  0.532,
                            0.532,  0.532,  0.532,  0.532,  0.535,  0.535,  0.535,  0.535,  0.535,  0.537,
                            0.537,  0.537,  0.537,  0.537,  0.537,  0.537,  0.537,  0.537,  0.537,  0.537,
                            0.537,  0.537,  0.537,  0.537,  0.537,  0.537,  0.537,  0.537,  0.537,  0.537,
                            0.580
                  };
                 
                 gl005v[59]=gl005v[60]=gl005v[61]=gl005v[62]=gl005v[63]=gl005v[64]=gl005v[65]=gl005v[66]=gl005v[67]=gl005v[68]=vtemp602;
                 gl005v[69]=gl005v[70]=gl005v[71]=gl005v[72]=gl005v[73]=gl005v[74]=gl005v[75]=gl005v[76]=gl005v[77]=gl005v[78]=vtemp602;
                 gl005v[79]=gl005v[80]=gl005v[81]=gl005v[82]=gl005v[83]=gl005v[84]=gl005v[85]=gl005v[86]=gl005v[87]=gl005v[88]=vtemp602;
                 gl005v[89]=gl005v[90]=gl005v[91]=gl005v[92]=gl005v[93]=gl005v[94]=gl005v[95]=gl005v[96]=gl005v[97]=gl005v[98]=vtemp602;
                 gl005v[99]=gl005v[100]=gl005v[101]=gl005v[102]=gl005v[103]=gl005v[104]=gl005v[105]=gl005v[106]=gl005v[107]=gl005v[108]=vtemp602;
                 gl005v[109]=gl005v[110]=gl005v[111]=gl005v[112]=gl005v[113]=gl005v[114]=gl005v[115]=gl005v[116]=gl005v[117]=gl005v[118]=vtemp602;
                 
                 double[] vtemp1202 =                                       
                  /*120*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0.479,  0.483,  0.488,  0.492,  0.496,  0.500,  0.504,
                            0.504,  0.504,  0.504,  0.504,  0.521,  0.521,  0.521,  0.521,  0.521,  0.534,
                            0.534,  0.534,  0.534,  0.534,  0.546,  0.546,  0.546,  0.546,  0.546,  0.556,
                            0.556,  0.556,  0.556,  0.556,  0.565,  0.565,  0.565,  0.565,  0.565,  0.572,
                            0.572,  0.572,  0.572,  0.572,  0.579,  0.579,  0.579,  0.579,  0.579,  0.585,
                            0.585,  0.585,  0.585,  0.585,  0.591,  0.591,  0.591,  0.591,  0.591,  0.596,
                            0.596,  0.596,  0.596,  0.596,  0.600,  0.600,  0.600,  0.600,  0.600,  0.604,
                            0.604,  0.604,  0.604,  0.604,  0.608,  0.608,  0.608,  0.608,  0.608,  0.611,
                            0.611,  0.611,  0.611,  0.611,  0.611,  0.611,  0.611,  0.611,  0.611,  0.611,
                            0.611,  0.611,  0.611,  0.611,  0.611,  0.611,  0.611,  0.611,  0.611,  0.611,
                            0.677
                  };
                 gl005v[119]=gl005v[120]=gl005v[121]=gl005v[122]=gl005v[123]=gl005v[124]=gl005v[125]=gl005v[126]=gl005v[127]=gl005v[128]=vtemp1202;
                 gl005v[129]=gl005v[130]=gl005v[131]=gl005v[132]=gl005v[133]=gl005v[134]=gl005v[135]=gl005v[136]=gl005v[137]=gl005v[138]=vtemp1202;
                 gl005v[139]=gl005v[140]=gl005v[141]=gl005v[142]=gl005v[143]=gl005v[144]=gl005v[145]=gl005v[146]=gl005v[147]=gl005v[148]=vtemp1202;
                 double[] vtemp1502 =                                       
                  /*150*/{   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
                            0,  0,  0,  0.488,  0.493,  0.497,  0.502,  0.506,  0.510,  0.514,
                            0.514,  0.514,  0.514,  0.514,  0.532,  0.532,  0.532,  0.532,  0.532,  0.546,
                            0.546,  0.546,  0.546,  0.546,  0.559,  0.559,  0.559,  0.559,  0.559,  0.569,
                            0.569,  0.569,  0.569,  0.569,  0.579,  0.579,  0.579,  0.579,  0.579,  0.587,
                            0.587,  0.587,  0.587,  0.587,  0.594,  0.594,  0.594,  0.594,  0.594,  0.601,
                            0.601,  0.601,  0.601,  0.601,  0.606,  0.606,  0.606,  0.606,  0.606,  0.612,
                            0.612,  0.612,  0.612,  0.612,  0.617,  0.617,  0.617,  0.617,  0.617,  0.621,
                            0.621,  0.621,  0.621,  0.621,  0.625,  0.625,  0.625,  0.625,  0.625,  0.629,
                            0.629,  0.629,  0.629,  0.629,  0.629,  0.629,  0.629,  0.629,  0.629,  0.629,
                            0.629,  0.629,  0.629,  0.629,  0.629,  0.629,  0.629,  0.629,  0.629,  0.629,
                            0.702
                  }; 
                 gl005v[149]=vtemp1502;
                 
                 
		//dbmsType = "sqlserver1"; // 1 = TeraData; 2 = PostgreSQL; 3 = Oracle; 4 = MySQL
                m_FullCVector = new Vector ();
		m_FullDVector = new Vector ();
		m_FullMVector = new Vector ();
		m_FullIVector = new Vector ();
		m_PartialDVector = new Vector ();
		m_PartialMVector = new Vector ();
		m_PartialIVector = new Vector ();
		pThreshold = 25;
		//m_tblName = "zmed655";
		m_uniqueID = 0;
		userInterface = false;
		algorithm = 1;
		sqlFile = "";
		debugFile = "";
		SingleTable = true;
		expParm = "";
		calcAll = false;
		numTables = 0;

		try {
			sqlOutputFile = new File (sqlFileName);
			boolean nFile = sqlOutputFile.createNewFile ();
			sqlOutputStream = new FileOutputStream (sqlOutputFile);
			sqlOutput = new PrintStream (sqlOutputStream);

			debugOutputFile = new File (debugFileName);
			nFile = debugOutputFile.createNewFile ();
			debugOutputStream = new FileOutputStream (debugOutputFile, true);
			debugOutput = new PrintStream (debugOutputStream);
		} catch (Exception e) { }
	}
	
	/*	Function to place values into the appropriate variable
	*	Parameters:
	*		String name - identifier
	*		String value - value of the identifer
	*	Returns: boolean
	*		1 = success
	*		0 = unknown tag
	*/
        //FUNCION: Cuando se encuentra un parámentro en la línea lo asigna a su  variable correspondiente
	private boolean putIntoVector (String name, String value)
	{
		if (debugFlag)
		{
			msgStr = "Put Into Vector: " + name + "   " + value;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	
		if (name.equalsIgnoreCase ("k"))
		{
			if (!m_PartialDVector.contains (value))
				m_PartialDVector.addElement (value);
		}
		else if (name.equalsIgnoreCase ("e"))
		{
			if (!m_PartialMVector.contains(value))
				m_PartialMVector.addElement (value);
		}
		else if (name.equalsIgnoreCase ("tablename"))
		{
			m_tblName = value;
		}
		else if (name.equalsIgnoreCase ("dbms"))
		{
                        //NOTA: Envia un numero y se requiere una cadena
                        //EDGAR
                        switch (Integer.parseInt(value)){
                            case 1:
                                dbmsType = "postgresql";
                                break;
                            case 2:
                                dbmsType = "sqlserver";
                                break;
                            default:
                                dbmsType = "sqlserver";
                        }
		}
		else if (name.equalsIgnoreCase ("pThreshold"))
		{
			pThreshold = Integer.parseInt (value);
		}
		else if (name.equalsIgnoreCase ("outfile"))
		{
			sqlFileName = value;
		}
		else if (name.equalsIgnoreCase ("interface"))
		{
			if (value.equalsIgnoreCase ("1"))
				userInterface = true;
			else
				userInterface = false;
		}
		else if (name.equalsIgnoreCase ("algorithm"))
		{
			algorithm = Integer.parseInt (value);
		}
		else if (name.equalsIgnoreCase ("SingleTable"))
		{
			if (Integer.parseInt (value) == 0)
				SingleTable = false;
		}
		else if (name.equalsIgnoreCase ("calcAll"))
		{
			if (Integer.parseInt (value) == 1)
				calcAll = true;
		}
		else if (name.equalsIgnoreCase ("flag"))
		{
			if (value.equalsIgnoreCase ("debug"))
				debugFlag = true;
			else if (value.equalsIgnoreCase ("nodebug"))
				debugFlag = false;
			else if (value.equalsIgnoreCase ("precompute"))
				precomputeFlag = true;
			else if (value.equalsIgnoreCase ("noprecompute"))
				precomputeFlag = false;
			else if (value.equalsIgnoreCase ("resultsum"))
				resultSumFlag = true;
			else if (value.equalsIgnoreCase ("noresultsum"))
				resultSumFlag = false;
			else if (value.equalsIgnoreCase ("connect"))
				connectFlag = true;
			else if (value.equalsIgnoreCase ("noconnect"))
				connectFlag = false;
			else if (value.equalsIgnoreCase ("checkresult"))
				checkFlag = true;
			else if (value.equalsIgnoreCase ("nocheckresult"))
				checkFlag = false;
			else if (value.equalsIgnoreCase ("noprint"))
				printFlag = false;
			else if (value.equalsIgnoreCase ("print"))
				printFlag = true;
			else if (value.equalsIgnoreCase ("savefile"))
				saveFileFlag = true;
			else if (value.equalsIgnoreCase ("nosavefile"))
				saveFileFlag = false;
		}
		else if (name.equalsIgnoreCase ("fulld"))
		{
			if (!m_FullDVector.contains (value))
				m_FullDVector.addElement (value);
		}
		else if (name.equalsIgnoreCase ("fulle"))
		{
			if (!m_FullMVector.contains (value))
				m_FullMVector.addElement (value);
		}
		else if (name.equalsIgnoreCase ("fullf"))
		{
			if (!m_FullIVector.contains (value))
				m_FullIVector.addElement (value);
		}
		else if (name.equalsIgnoreCase ("expStr"))
			expParm = value;
		else
		{
			msgStr = "ERROR: unknown tag = " + name;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
			return false;
		}
		return true;	
	}
	
	/*	Function to parse input string
	*	Parameters:
	*		String str - input string containing the identifiers and values
	*	Return: None
	*/
        //FUNCION: Divide la cadena de los parametros
	public void RunOLAPStatTest (String str) throws IOException
	{
		parameterStr = str;
		expParm = str;
		String argstr = "";
		String attrName = "";
		String tempstr = "";
		char letter;
		
			attrName = "";
			tempstr = "";
			argstr = str;
			if (debugFlag)
			{
				msgStr = "Looking at: " + argstr;
				System.out.println (msgStr);
				debugOutput.println (msgStr);
			}
				
			for (int k = 0; k < argstr.length (); ++k)
			{
				letter = argstr.charAt (k);
				if (debugFlag)
				{
					msgStr = "Looking at Letter: " + letter;
					System.out.println (msgStr);
					debugOutput.println (msgStr);
				}
				if (letter == '=')
				{
					attrName = tempstr;
					tempstr = "";
					if (debugFlag)
					{
						msgStr = "Attribute = " + attrName;
						System.out.println (msgStr);
						debugOutput.println (msgStr);
					}
				}
				else if (letter == ',')
				{
					if (debugFlag)
					{
						msgStr = "Found Comma";
						System.out.println (msgStr);
						debugOutput.println (msgStr);
					}
					if (!putIntoVector (attrName, tempstr))
					{
						InputERROR ();
						return;
					}
					tempstr = "";
				}
				else if (letter == ';')
				{
					if (debugFlag)
					{
						msgStr = "Found semiColon";
						System.out.println (msgStr);
						debugOutput.println (msgStr);
					}
					if (!putIntoVector (attrName, tempstr))
					{
						InputERROR ();
						return;
					}
						attrName = "";
						tempstr = "";
				}
				else
				{
					tempstr = tempstr + letter;
					if (debugFlag)
					{
						msgStr = tempstr;
						System.out.println (msgStr);
						debugOutput.println (msgStr);
					}
				}
			}
		if (tempstr != "")
			if (!putIntoVector (attrName, tempstr))
			{
				InputERROR ();
				return;
			}
		
		if (m_PartialDVector.size () == 0)
			putIntoVector ("d", "A");
		if (m_PartialMVector.size () == 0)
			putIntoVector ("measure", "A1");
			
		for (int i = 0; i < m_FullIVector.size (); i++)
			m_PartialIVector.addElement (m_FullIVector.elementAt (i));
		
		msgStr = "#################### OLAPSTATTEST ####################";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
	
		java.util.Collections.sort (m_PartialDVector);
		java.util.Collections.sort (m_PartialMVector);
		java.util.Collections.sort (m_PartialIVector);
		java.util.Collections.sort (m_FullDVector);
		java.util.Collections.sort (m_FullMVector);
		java.util.Collections.sort (m_FullIVector);

			dbFunctions = new DBSQLStatement (dbmsType, printFlag);
			dbConn = new DBSQLConnect (dbmsType, connectFlag, printFlag);
			dbConn.startConnection ();
                        
                        //EDGAR MOD:
                        //runUserInterfaceSelectDM();
			getTabColInfo ();
                        
                //EDGAR
		if (!userInterface)
		{
			RunGate ();
		}
		else
		{
			//dbConn.stopConnection ();
                        //EDGAR MOD: Función que llama la ventana para ejecutar el análisis
                        //Ahora se llama desde OLAPInterfaceSelect
			//runUserInterface ();
		}
	}
	
	public void RunFromInterface (Vector dimensions, Vector partialD, Vector measures, Vector partialM, Vector images, Vector partialI, String tablename, String idtable, int algor, int test, String parameter, String dbms, int pt, String outf)
	{
		userInterface = true;
		m_FullDVector = dimensions;
		java.util.Collections.sort (m_FullDVector);
		m_PartialDVector = partialD;
		java.util.Collections.sort (m_PartialDVector);
		m_FullMVector = measures;
		java.util.Collections.sort (m_FullMVector);
		m_PartialMVector = partialM;
		java.util.Collections.sort (m_PartialMVector);
		m_FullIVector = images;
		java.util.Collections.sort (m_FullIVector);
		m_PartialIVector = partialI;
		java.util.Collections.sort (m_PartialIVector);
		m_tblName = tablename;
                idTable = idtable;
		algorithm = algor;
                this.test = test;
                this.parameter = parameter;
		dbmsType = dbms;
		pThreshold = pt;
		sqlFileName = outf;

		dbFunctions = new DBSQLStatement (dbmsType, printFlag);
		dbConn = new DBSQLConnect (dbmsType, connectFlag, printFlag);
		dbConn.startConnection ();

		getTabColInfo ();
		RunGate ();
	}

	private String getTableName (String prefix, Vector dim)
	{
		if (debugFlag){
			msgStr = "START getTableName with prefix=" + prefix + "  dim=" + dim;
			System.out.println (msgStr);
			debugOutput.println (msgStr); }
		
		String ans = prefix;
		String s1, s2;
		
		ans += "D";
		for (int i = 0; i < dim.size (); i++)
		{
			s1 = (String) dim.elementAt (i);
			for (int j = 0; j < m_FullDVector.size (); j++)
			{
				s2 = (String) m_FullDVector.elementAt (j);
				if (s1.equalsIgnoreCase (s2))
				{
					if (j < 9)
						ans += "0" + new Integer (j+1).toString ();
					else
						ans += new Integer (j+1).toString ();
					break;
				}
			}
		}

		if (debugFlag) {
			msgStr = "END getTableName return " + ans;
			System.out.println (msgStr);
			debugOutput.println (msgStr);  }
		
		return ans;
	}

	/*	Function to create the tablenames (atomic and current)
	*	Parameters: None
	*	Return: None
	*/
        //FUNCION: Crea las consultas hacia la tabla TableInfoLookup para obtener dimensiones, medidas e imagenes
	private void getTabColInfo ()
	{
		if (debugFlag) {
			msgStr = "START getTabColInfo ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);  }
		
		String[] cols = new String[1];
		cols[0] = "ColName";
		String test = "";
		String[] temp;
		String singletemp;
		int index;

		
		// if vector already filled, then don't lookup
		if (m_FullDVector.size () > 0) return;
		
                m_FullCVector.clear ();
		m_FullDVector.clear ();
		m_FullMVector.clear ();
		m_FullIVector.clear ();

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		Columns.addElement ("*");
		TableName.addElement ("TableInfoLookup");
		Predicate.addElement ("TblName = '" + m_tblName + "' AND ColType = 'K'");
		
		test = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		boolean flag = dbConn.sendQuery(test);
		flag = flag && dbConn.solutionExists ();

		if (flag)
		{
			temp = dbConn.getResult (cols);
			while (temp.length != 0)
			{
				singletemp = (String)temp[0];
				index = singletemp.indexOf (' ');
				if (index > 0)
					singletemp = singletemp.substring (0, singletemp.indexOf (' '));
				m_FullDVector.addElement (singletemp);
				temp = dbConn.getResult (cols);
			}
		}
		else
		{
			msgStr = "Cannot Find Table Information...Asking user for input";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
			
			getColumnNames ();
			
			if (debugFlag)
			{
				msgStr = "END getTabColInfo ()";
				System.out.println (msgStr);
				debugOutput.println (msgStr);
			}

			return;
		}

			
			// GET MEASURES
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		Columns.addElement ("*");
		TableName.addElement ("TableInfoLookup");
		Predicate.addElement ("TblName = '" + m_tblName + "' AND ColType = 'A'");
		
		test = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		flag = dbConn.sendQuery(test);
		flag = flag && dbConn.solutionExists ();

		if (flag)
		{
			temp = dbConn.getResult (cols);
			while (temp.length != 0)
			{
				singletemp = temp[0];
				index = singletemp.indexOf (' ');
				if (index > 0)
					singletemp = singletemp.substring (0, singletemp.indexOf (' '));
				m_FullMVector.addElement (singletemp);
				temp = dbConn.getResult (cols);
			}
		}
		else
		{
			msgStr = "Cannot Find Table Measures Information...Asking user for input";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
			
			getColumnNames ();

			if (debugFlag)
			{
				msgStr = "END getTabColInfo ()";
				System.out.println (msgStr);
				debugOutput.println (msgStr);
			}
			
			return;
		}

			// GET ImageData
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		Columns.addElement ("*");
		TableName.addElement ("TableInfoLookup");
		Predicate.addElement ("TblName = '" + m_tblName + "' AND ColType = 'I'");
		
		test = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		flag = dbConn.sendQuery(test);
		flag = flag && dbConn.solutionExists ();

		if (flag)
		{
			temp = dbConn.getResult (cols);
			while (temp.length != 0)
			{
				singletemp = temp[0];
				index = singletemp.indexOf (' ');
				if (index > 0)
					singletemp = singletemp.substring (0, singletemp.indexOf (' '));
				m_FullIVector.addElement (singletemp);
				temp = dbConn.getResult (cols);
			}
		}
		else
		{
			msgStr = "Cannot Find Table Image Data Information...Asking user for input";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
			
                        
                        
			getColumnNames ();

			if (debugFlag)
			{
				msgStr = "END getTabColInfo ()";
				System.out.println (msgStr);
				debugOutput.println (msgStr);
			}
			
			return;
		}
			
		
	}
	
	
	/*	Function to ask user to input table column values
	*	Parameter: None
	*	Return: None
	*/
        //EDGAR MOD: Obtiene los nombres de las columnas a traves de la linea de comandos, se modifica para hacerlo grafico
	private void getColumnNames ()
	{
                Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		Columns.addElement ("column_name");
		TableName.addElement ("information_schema.columns");
		Predicate.addElement ("table_name = '" + m_tblName +"'");
		
                String[] tmp;
                String[] cols = new String[1];
                cols[0] = "column_name";
                String singletemp;
		int index;
                
		String test = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		dbConn.sendQuery(test);
                dbConn.solutionExists ();
                
                tmp = dbConn.getResult (cols);
			while (tmp.length != 0)
			{
				singletemp = (String)tmp[0];
				index = singletemp.indexOf (' ');
				if (index > 0)
					singletemp = singletemp.substring (0, singletemp.indexOf (' '));
				m_FullCVector.addElement (singletemp);
				tmp = dbConn.getResult (cols);
			}
                
                runUserInterfaceSelectDM();
	
		/*try {
        		java.util.Collections.sort (m_FullDVector);
                        java.util.Collections.sort (m_FullMVector);
                        java.util.Collections.sort (m_FullIVector);
			
                        initializeNewInputTable ();
		} catch (Exception e) { }*/
	}
	
	/*	Function to save new table info into lookup and create necessary tables
	*	Parameter: None
	*	Return: None
	*/
	
	public void initializeNewInputTable ()
	{
		String sqlStat = "";
		sqlFile += "/* **********INITIALIZE TABLES*********** */" + CR;
		
		if (!tableExists ("TableInfoLookup"))
		{
			TblName = "TableInfoLookup";
			ColName = new Vector ();
			ColType = new Vector ();
			PrimeKey = new Vector ();

			ColName.addElement("TblName");
			ColType.addElement("char(200)");
			ColName.addElement("ColType");
			ColType.addElement("char(1)");
			ColName.addElement("ColName");
			ColType.addElement("char(200)");
			
			PrimeKey.addElement("TblName");
			PrimeKey.addElement("ColName");
		
			sqlStat = dbFunctions.dbCreateTable (TblName, ColName, ColType, PrimeKey,-1);
			dbConn.sendQuery (sqlStat);
			sqlFile += sqlStat + ";" + CR + CR;
		}
		SelectStatement = "";
		TblName = "TableInfoLookup";
		Columns = new Vector ();
		Values = new Vector ();
		
		for (int i = 0; i < m_FullDVector.size (); i++)
		{
			Values.clear ();
			Values.addElement("'" + m_tblName + "'");
			Values.addElement("'K'");
			Values.addElement("'" + m_FullDVector.elementAt (i) + "'");
			
			sqlStat = dbFunctions.dbInsert (TblName, Columns, Values, SelectStatement, -1);
			dbConn.sendQuery (sqlStat);
			sqlFile += sqlStat + ";" + CR + CR;
		}

		for (int i = 0; i < m_FullMVector.size (); i++)
		{
			Values.clear ();
			Values.addElement("'" + m_tblName + "'");
			Values.addElement("'A'");
			Values.addElement("'" + m_FullMVector.elementAt (i) + "'");
			
			sqlStat = dbFunctions.dbInsert (TblName, Columns, Values, SelectStatement, -1);
			dbConn.sendQuery (sqlStat);
			sqlFile += sqlStat + ";" + CR + CR;
		}

		for (int i = 0; i < m_FullIVector.size (); i++)
		{
			Values.clear ();
			Values.addElement("'" + m_tblName + "'");
			Values.addElement("'I'");
			Values.addElement("'" + m_FullIVector.elementAt (i) + "'");
			
			sqlStat = dbFunctions.dbInsert (TblName, Columns, Values, SelectStatement, -1);
			dbConn.sendQuery (sqlStat);
			sqlFile += sqlStat + ";" + CR + CR;
		}

		initializeResultSummaryTables ();

	}
	
	/*	Function that creates the result and summary tables
	*	Parameter: None
	*	Return: None
	*/
	private void initializeResultSummaryTables ()
	{
		String sqlStat = "";
		sqlStat = dbFunctions.dbDropTable (m_tblName + resultTableText);
		dbConn.sendQuery (sqlStat);
		sqlFile += sqlStat + ";" + CR + CR;
		

		TblName = m_tblName + resultTableText;
		ColName = new Vector ();
		ColType = new Vector ();
		PrimeKey = new Vector ();
		
		ColName.addElement("id");
		ColType.addElement("int");
		PrimeKey.addElement("id");
		
		for (int i = 0; i < m_FullDVector.size (); i++)
		{
			ColName.addElement((String)m_FullDVector.elementAt (i));
			ColType.addElement("int");
		}
		
		ColName.addElement("N");
		ColType.addElement("float");

		for (int i = 0; i < m_FullMVector.size (); i++)
		{
			ColName.addElement("L_" + (String)m_FullMVector.elementAt (i));
			ColType.addElement("float");
			ColName.addElement("Q_" + (String)m_FullMVector.elementAt (i));
			ColType.addElement("float");
		}
		for (int i = 0; i < m_FullIVector.size (); i++)
		{
			ColName.addElement("L_" + (String)m_FullIVector.elementAt (i));
			ColType.addElement("float");
			ColName.addElement("Q_" + (String)m_FullIVector.elementAt (i));
			ColType.addElement("float");
		}

		sqlStat = dbFunctions.dbCreateTable (TblName,ColName,ColType,PrimeKey,0);
		dbConn.sendQuery (sqlStat);
		sqlFile += sqlStat + ";" + CR + CR;

		
		sqlStat = dbFunctions.dbDropTable (m_tblName + summaryTableText);
		dbConn.sendQuery (sqlStat);
		sqlFile += sqlStat + ";" + CR + CR;
		

		TblName = m_tblName + summaryTableText;
		ColName = new Vector ();
		ColType = new Vector ();
		PrimeKey = new Vector ();
		
		for (int i = 0; i < m_FullDVector.size (); i++)
		{
			ColName.addElement((String)m_FullDVector.elementAt (i));
			ColType.addElement("int");
			PrimeKey.addElement((String)m_FullDVector.elementAt (i));
		}
		ColName.addElement("NumDim");
		ColType.addElement("int");
				
		sqlStat = dbFunctions.dbCreateTable (TblName,ColName,ColType,PrimeKey,-1);
		dbConn.sendQuery (sqlStat);
		sqlFile += sqlStat + ";" + CR + CR;		
//		myPause ();
	}
	
	/*	Function to print out inputted parameters
	*	Parameters: None
	*	Return: None
	*/
	private void printParameters (Vector partialVector)
	{
		String str = "";
			str += "OLAPStatTest V 1.0" + CR;
			str += "Interface     : " + userInterface + CR;
			str += "Full d        : " + m_FullDVector + CR;
			str += "Partial d     : " + partialVector + CR;
			str += "Full measure  : " + m_FullMVector + CR;
			str += "Partial m     : " + m_PartialMVector + CR;
			str += "Tablename     : " + m_tblName + CR;
			str += "Algorithm     : " + algorithm + CR;
			str += "DBMS          : " + dbmsType + CR;
			str += "pThreshold    : " + pThreshold + CR;
			str += "outfile       : " + sqlFileName + CR;
			str += "*******OPTIMIZATION**********" + CR;
			str += "SingleTable   : " + SingleTable + CR + CR;		
		
		if (printFlag)
			System.out.println (str);
		debugOutput.println (str);
	}
	
	/*	Function acting as gate for determining which run function to do (single or all)
	*	Parameter: None
	*	Return: None
	*/
	private void RunGate ()
	{
                //EDGAR NOTA: no se creaban las tablas
                dbConn.sendQuery(dbFunctions.dbDropTable("experimentStartEnd"));
                dbConn.sendQuery(dbFunctions.dbDropTable("experimentLog"));
                dbConn.sendQuery(dbFunctions.createLog());
		if (calcAll)
		{
			dbConn.sendQuery (dbFunctions.dbDropTable (m_tblName + finalTableText) + ";" + CR);
			Vector front = new Vector ();
			Vector back = m_PartialDVector;
			dbFunctions.logStart (dbConn, technique + "Total");
			calcAllSubsets (front, back);
			dbFunctions.logUpdate (dbConn, technique + "Total", "TotalTime", expParm);
		}
		else
		{
			dbConn.sendQuery (dbFunctions.dbDropTable (m_tblName + finalTableText) + ";" + CR);
			RunGenerator (m_PartialDVector);
		}
		dbConn.stopConnection ();
		
		msgStr = "Number of subsets: " + numTables;
		System.out.println (msgStr);
		debugOutput.println (msgStr);
	}

	/*	Function used to determine analysis
	*	Parameter: None
	*	Return: None
	*/
        //EDGAR: Inicia la prueba estadística
	private void RunGenerator (Vector partialVector)
	{
		msgStr = "***************START SQL Code Generation**************";
		if (printFlag)	System.out.println (msgStr);
		debugOutput.println (msgStr);
		
		sqlFile = dbFunctions.logStart (dbConn, technique + "RUN") + ";" + CR + CR;

		printParameters (partialVector);
		
		String tempStr;
		if (algorithm == 1)
		{
			tempStr = "/* **********************************" + CR;
			tempStr += "   * File: java OLAPStatTest " + parameterStr + CR;
			tempStr += "   * Author: Zhibo Chen" + CR;
			tempStr += "   * Updated: " + new java.util.Date () + CR;
			tempStr += "   * Contents: OLAP Cube Statistical Test using PreCompute Method" + CR;
			tempStr += "********************************** */" + CR + CR + CR;
			sqlFile += tempStr;
			
			if (resultSumFlag && (!tableExists (m_tblName + resultTableText) || !tableExists (m_tblName + summaryTableText)))
				initializeResultSummaryTables ();

			PreComputeAlgorithm (partialVector);
		}
		else if (algorithm == 2)
		{
			tempStr = "/* **********************************" + CR;
			tempStr += "   * File: java OLAPStatTest " + parameterStr + CR;
			tempStr += "   * Author: Zhibo Chen" + CR;
			tempStr += "   * Updated: " + new java.util.Date () + CR;
			tempStr += "   * Contents: OLAP Cube Statistical Test using Direct Compute Method" + CR;
			tempStr += "********************************** */" + CR + CR + CR;

			sqlFile += tempStr;

			if (resultSumFlag && (!tableExists (m_tblName + resultTableText) || !tableExists (m_tblName + summaryTableText)))
				initializeResultSummaryTables ();

			DirectComputeAlgorithm (partialVector);
		}
		else
		{
			tempStr = "/* **********************************" + CR;
			tempStr += "   * File: java OLAPStatTest " + parameterStr + CR;
			tempStr += "   * Author: Zhibo Chen" + CR;
			tempStr += "   * Updated: " + new java.util.Date () + CR;
			tempStr += "   * Contents: OLAP Cube Statistical Test using Auto Compute Method" + CR;
			tempStr += "********************************** */" + CR + CR + CR;

			sqlFile += tempStr;
			
			if (resultSumFlag && (!tableExists (m_tblName + resultTableText) || !tableExists (m_tblName + summaryTableText)))
				initializeResultSummaryTables ();

			AutoComputeAlgorithm (partialVector);
		}

		msgStr = "***************END SQL Code Generation**************";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
	
		sqlFile += dbFunctions.logUpdate (dbConn, technique + "RUN", "RunTime", expParm) + ";" + CR + CR;

		numTables++;
		
		sqlOutput.println (sqlFile);
	}
	
	/*	Main Function for obtaining all subsets of a set of dimensions
	*	Parameter:
	*		Vector frontVect - contains subset of dimensions to be attached to front of new subset
	*		Vector backVect - dimensions to be combined
	*	Return: None
	*/
	private void calcAllSubsets (Vector frontVect, Vector backVect)
	{
		Vector temp = new Vector ();
		
		for (int i = 0; i < backVect.size (); i++)
		{
			frontVect.addElement (backVect.elementAt (i));
			msgStr = "Running: " + frontVect + " out of " + m_PartialDVector;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
			RunGenerator (frontVect);
			frontVect.removeElementAt (frontVect.size () - 1);
		}
		
		for (int i = 0; i < backVect.size (); i++)
		{
			frontVect.addElement (backVect.elementAt (i));
			temp.clear ();
			for (int j = i+1; j < backVect.size (); j++)
				temp.addElement (backVect.elementAt (j));
			if (temp.size () > 0)
				calcAllSubsets (frontVect, temp);
			frontVect.removeElementAt (frontVect.size () - 1);
		}	
	}	

	/*	Main Function for Direct Compute Method using Interface
	*	Parameter: None
	*	Return: None
	*/
	private void DirectComputeAlgorithm (Vector partialVector)
	{
		String sqlStat = "";
		if (debugFlag) {
			msgStr = "Start DirectComputeAlgorithm ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);  }

		msgStr = "++++++++++ Direct Compute Method ++++++++++++";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		
		String currentTableName = getTableName (m_tblName, partialVector);

		if (!checkFlag || !resultExists (currentTableName, partialVector))
		{
			msgStr = "Need to compute Table";
			if (printFlag) System.out.println (msgStr);
			debugOutput.println (msgStr);
			
			sqlFile += dbFunctions.logStart (dbConn, technique) + ";" + CR + CR;
			aggregateFromOriginal (currentTableName, m_tblName, partialVector, m_FullMVector, m_FullIVector);
			sqlFile += dbFunctions.logUpdate (dbConn, technique, "1 2 Calculate NLQ", expParm) + ";" + CR + CR;
		}
		else
		{
			msgStr = "Table already exists";
			if (printFlag) System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
		createDifferencesTable (currentTableName, partialVector);
		
		if (debugFlag)
		{
			msgStr = "END DirectComputeAlgorithm ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
	}
	
	/*	Main Function for PreCompute Method using Interface
	*	Parameter: None
	*	Return: None
	*/
        //EDGAR: Este es el método al que siempre entra
	private void PreComputeAlgorithm (Vector partialVector)
	{
		String sqlStat;
		if (debugFlag)
		{
			msgStr = "START PreComputeAlgorithm () with partialVector=" + partialVector;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		msgStr = "++++++++++ PreCompute Method ++++++++++++";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);


		String atomicTableName = getTableName (m_tblName, m_FullDVector);
		String currentTableName = getTableName (m_tblName, partialVector);
		if (precomputeFlag && !resultExists (atomicTableName, m_FullDVector))
		{
			msgStr = "Need to compute Pre-Compute Table";
			if (printFlag) System.out.println (msgStr);
			debugOutput.println (msgStr);
			
			sqlFile += dbFunctions.logStart (dbConn, technique) + ";" + CR + CR;
			aggregateFromOriginal (atomicTableName, m_tblName, m_FullDVector, m_FullMVector, m_FullIVector);
			sqlFile += dbFunctions.logUpdate (dbConn, technique, "1 Pre-Compute Table", expParm) + "; " + CR + CR;

			msgStr = "Need to compute Table from Pre-Compute";
			if (printFlag) System.out.println (msgStr);
			debugOutput.println (msgStr);
			
			sqlFile += dbFunctions.logStart (dbConn, technique) + ";" + CR + CR;
			if (!partialVector.containsAll (m_FullDVector))
				aggregateFromComputed (currentTableName, atomicTableName, partialVector, m_FullMVector, m_FullIVector);
			sqlFile += dbFunctions.logUpdate (dbConn, technique, "2 Calculate NLQ Info", expParm) + ";" + CR + CR;
		}
		else
		{
			msgStr = "PreCompute Table already exists";
			if (printFlag) System.out.println (msgStr);
			debugOutput.println (msgStr);
			
			if (!checkFlag || !resultExists (currentTableName, partialVector))
			{
				msgStr = "Need to compute Table from Pre-Compute";
				if (printFlag) System.out.println (msgStr);
				debugOutput.println (msgStr);
				
				sqlFile += dbFunctions.logStart (dbConn, technique) + ";" + CR + CR;
				if (!partialVector.containsAll (m_FullDVector))
					aggregateFromComputed (currentTableName, atomicTableName, partialVector, m_FullMVector, m_FullIVector);
				sqlFile += dbFunctions.logUpdate (dbConn, technique, "2 Calculate NLQ Info", expParm) + ";" + CR + CR;
			}
			else
			{
				msgStr = "Table already exists";
				if (printFlag) System.out.println (msgStr);
				debugOutput.println (msgStr);
			}
		}
		
		createDifferencesTable (currentTableName, partialVector);
		
		if (debugFlag)
		{
			msgStr = "END PreComputeAlgorithm ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
}
	
	/*	Main Function for AutoCompute Method using Interface.  AutoCompute will determine which of the above 2 methods to use
	*	Parameter: None
	*	Return: None
	*/
	private void AutoComputeAlgorithm (Vector partialVector)
	{
		if (debugFlag)
		{
			msgStr = "START AutoComputeAlgorithm ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		msgStr = "++++++++++ Auto Compute Method ++++++++++++";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

//		sqlFile += dbFunctions.logStart (technique);
	
		String atomicTableName = getTableName (m_tblName, m_FullDVector);
		String currentTableName = getTableName (m_tblName, partialVector);

		msgStr = "TO BE COMPLETED";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
		
		if (debugFlag)
		{
			msgStr = "END AutoComputeAlgorithm ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
	}
		
// ***************************************** HELPER FUNCTIONS *************************************
	
	/*	Function to see if a result is already calculated
	*	Parameter:
	*		String currentTableName - name of table
	*		Vector Dim - dimensions
	*	Return: Boolean
	*		True - result here
	*		False - result not found
	*/
	private boolean resultExists (String currentTableName, Vector dim)
	{
		if (debugFlag) {
			msgStr = "START resultExists with tblName=" + currentTableName + " and dim=" + dim;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
		if (SingleTable)
		{
			String sql = "";
			
			Columns = new Vector ();
			TableName = new Vector ();
			Predicate = new Vector ();
			OrderBy = new Vector ();
			GroupBy = new Vector ();
		
			Columns.addElement ("*");
			TableName.addElement (m_tblName + summaryTableText);
			
			String holder = "", tpred = "";
			boolean flag = true;
			for (int i = 0; i < m_FullDVector.size (); i++)
			{
				if (flag)
				{
					tpred += "(";
					flag = false;
				}
				else
				{	tpred += " AND (";	}
				holder = (String)m_FullDVector.elementAt (i);
				if (dim.contains (holder))
				{	tpred += holder + "=1";	}
				else
				{	tpred += holder + "=0";	}
				tpred += ")";
			}
			Predicate.addElement (tpred);
		
			sql = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
			dbConn.sendQuery (sql);
			return dbConn.solutionExists ();
		}
		else
		{
			if (debugFlag) {
				msgStr = "Looking for actual table: " + currentTableName;
				System.out.println (msgStr);
				debugOutput.println (msgStr);
			}
			return tableExists (currentTableName);		
		}
	}
	
	/*	Function to see if a table is present
	*	Parameter:
	*		String tblName - name of table to look for
	*	Return: Boolean
	*		True - table found
	*		False - table not found
	*/
	private boolean tableExists (String tblName)
	{
		if (debugFlag) {
			msgStr = "START tableExists (tblName) with tblName=" + tblName;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		String sql = "";

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		Columns.addElement("*");
		TableName.addElement(tblName);
		sql = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		
		if (debugFlag)
		{
			msgStr = "END tableExists ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
		return dbConn.sendQuery (sql);
	}
	
	/*	Function to create table from pre-computed table
	*	Parameters:
	*		String curTable - name of table to be created
	*		String fromTable - name of precomputed table
	*		Vector DimVect - set of dimensions
	*		Vector MeaVect - set of measures
	*	Return: None
	*/
	private void aggregateFromComputed (String curTable, String fromTable, Vector DimVect, Vector MeaVect, Vector ImgVect)
	{
		if (debugFlag){
			msgStr = "START aggregateFromComputed with curTable=" + curTable + "  fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		msgStr = "Creating Table from PreComputed";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		if (SingleTable)
		{
			String sqlStat = "";
			boolean flag;
			String op = "";

			sqlFile += "/* Aggregate from PreComputed SINGLE curTable=" + curTable + "  fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect + "*/" + CR;
			
			// Delete old results
			Vector Pred = new Vector ();
			
			flag = false;
			for (int i = 0; i < m_FullDVector.size (); i++)
			{
				op = (String) m_FullDVector.elementAt (i);
				flag = DimVect.contains (op);
				if (flag)
					Pred.addElement ("(" + op + "<>-1)");
				else
					Pred.addElement ("(" + op + "=-1)");
			}
			
			sqlStat = dbFunctions.dbDelete (m_tblName + resultTableText, Pred);
			sqlFile += sqlStat + ";" + CR + CR;
			dbConn.sendQuery (sqlStat);
			
			// SELECT FOR RESULT
			Columns = new Vector ();
			TableName = new Vector ();
			Predicate = new Vector ();
			OrderBy = new Vector ();
			GroupBy = new Vector ();
			
			Columns.addElement ("cnt + sum(1) OVER (order by 1 rows unbounded preceding)");
			
			flag = false;
			for (int lp1 = 0; lp1 < m_FullDVector.size (); lp1++)
			{
				op = (String) m_FullDVector.elementAt (lp1);
				flag = DimVect.contains (op);
				if (flag)
				{
					Columns.addElement(op);
					GroupBy.addElement(op);
				}
				else
					Columns.addElement("-1");
			}
			
			Columns.addElement("sum(N) as N");
			
			op = "";
			for (int i = 0; i < MeaVect.size (); i++)
			{
				op = (String) MeaVect.elementAt (i);
				Columns.addElement("sum(L_" + op + ")");
				Columns.addElement("sum(Q_" + op + ")");
			}
			
			for (int i = 0; i < ImgVect.size (); i++)
			{
				op = (String) ImgVect.elementAt (i);
				Columns.addElement("sum(L_" + op + ")");
				Columns.addElement("sum(Q_" + op + ")");
			}

			// NESTED SELECT to get Pre-Computed Table
			Vector Columns2 = new Vector ();
			Vector TableName2 = new Vector ();
			Vector Predicate2 = new Vector ();
			Vector OrderBy2 = new Vector ();
			Vector GroupBy2 = new Vector ();
			
			Columns2.addElement ("*");
			TableName2.addElement (m_tblName + resultTableText);
			
			String tpstr = "";
			flag = true;
			for (int i = 0; i < m_FullDVector.size (); i++)
			{
				tpstr = "((" + (String)m_FullDVector.elementAt (i) + "<>-1)" + " OR (" + (String)m_FullDVector.elementAt (i) + " is null))";
				Predicate2.addElement (tpstr);
			}

			TableName.addElement("(" + dbFunctions.dbSelect (Columns2,TableName2,Predicate2,OrderBy2,GroupBy2,-1) + ") as t");

			// nested select for use with cnt
			Vector Columns3 = new Vector ();
			Vector TableName3 = new Vector ();
			Vector Predicate3 = new Vector ();
			Vector OrderBy3 = new Vector ();
			Vector GroupBy3 = new Vector ();
			
			Columns3.addElement ("count(*) as cnt");
			TableName3.addElement (m_tblName + resultTableText);
			
			TableName.addElement ("(" + dbFunctions.dbSelect (Columns3,TableName3,Predicate3,OrderBy3,GroupBy3,-1) + ") as t1");
			
			// INSERT INTO RESULT
			SelectStatement = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy,0);
			TblName = m_tblName + resultTableText;
			Columns = new Vector ();
			Values = new Vector ();
			
			Columns.addElement("id");
			for (int i = 0; i < m_FullDVector.size (); i++)
				Columns.addElement((String)m_FullDVector.elementAt (i));
		
			Columns.addElement("N");

			for (int i = 0; i < m_FullMVector.size (); i++)
			{
				Columns.addElement("L_" + (String)m_FullMVector.elementAt (i));
				Columns.addElement("Q_" + (String)m_FullMVector.elementAt (i));
			}

			for (int i = 0; i < m_FullIVector.size (); i++)
			{
				Columns.addElement("L_" + (String)m_FullIVector.elementAt (i));
				Columns.addElement("Q_" + (String)m_FullIVector.elementAt (i));
			}

		
			sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, 0) + ";";
			sqlFile += sqlStat + CR + CR;
			dbConn.sendQuery (sqlStat);
			
			
			// Delete old summary
			Pred = new Vector ();
			
			flag = false;
			for (int i = 0; i < m_FullDVector.size (); i++)
			{
				op = (String) m_FullDVector.elementAt (i);
				flag = DimVect.contains (op);
				if (flag)
					Pred.addElement ("(" + op + "=1)");
				else
					Pred.addElement ("(" + op + "=0)");
			}
			
			sqlStat = dbFunctions.dbDelete (m_tblName + summaryTableText, Pred);
			sqlFile += sqlStat + ";" + CR + CR;
			dbConn.sendQuery (sqlStat);

			
			// INSERT INTO SUMMARY
			SelectStatement = "";
			TblName = m_tblName + summaryTableText;
			Columns = new Vector ();
			Values = new Vector ();
			
			int cnt = 0;
			for (int lp1 = 0; lp1 < m_FullDVector.size (); lp1++)
			{
				op = (String) m_FullDVector.elementAt (lp1);
				flag = DimVect.contains (op);
				if (flag)
				{
					Values.addElement("1");
					cnt++;
				}
				else
					Values.addElement("0");
			}
			Values.addElement(new Integer(cnt).toString ());
			
			sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1) + ";";
			sqlFile += sqlStat + CR + CR;
			dbConn.sendQuery (sqlStat);
		}
		else
		{
			int vectSize = DimVect.size ();
			int lp1;
			String sqlStat = "";
		
			sqlFile += "/* Aggregate from PreComputed MULTIPLE curTable=" + curTable + "  fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect + "*/" + CR;
		
			sqlStat = dbFunctions.dbDropTable (curTable);
			sqlFile += sqlStat;
			dbConn.sendQuery (sqlStat);
		
			Columns = new Vector ();
			TableName = new Vector ();
			Predicate = new Vector ();
			OrderBy = new Vector ();
			GroupBy = new Vector ();
		
			Columns.addElement ("sum(1) OVER (order by 1 rows unbounded preceding) as id");

			for (lp1 = 0; lp1 < vectSize; lp1++)
			{
				Columns.addElement(DimVect.elementAt (lp1));
				GroupBy.addElement(DimVect.elementAt (lp1));
			}
		
			Columns.addElement("sum(N) as N");
		
			String op = "";
			for (int i = 0; i < MeaVect.size (); i++)
			{
				op = (String) MeaVect.elementAt (i);
				Columns.addElement("max(MAX_" + op + ") as MAX_" + op);
				Columns.addElement("sum(L_" + op + ") as L_" + op);
				Columns.addElement("sum(Q_" + op + ") as Q_" + op);
			}

			for (int i = 0; i < ImgVect.size (); i++)
			{
				op = (String) ImgVect.elementAt (i);
				Columns.addElement("max(MAX_" + op + ") as MAX_" + op);
				Columns.addElement("sum(L_" + op + ") as L_" + op);
				Columns.addElement("sum(Q_" + op + ") as Q_" + op);
			}
			
			TableName.addElement(fromTable);
		
			SelectStatement = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
			TblName = curTable;
			PrimeKey = new Vector ();
		
			sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey);
			sqlFile += sqlStat + ";" + CR + CR;
			dbConn.sendQuery (sqlStat);
		}
		
		if (debugFlag){
			msgStr = "END aggregateFromComputed ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
	}
	
	/*	Function to create table from original table
	*	Parameters:
	*		String curTable - name of table to be created
	*		String fromTable - name of original table
	*		Vector DimVect - set of dimensions
	*		Vector MeaVect - set of measures
	*	Return: None
	*/
	private void aggregateFromOriginal (String curTable, String fromTable, Vector DimVect, Vector MeaVect, Vector ImgVect)
	{
		if (debugFlag) {
			msgStr = "START aggregateFromOriginal with curTable=" + curTable + "  fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		msgStr = "Creating Table from Original Dataset";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
                
		if (SingleTable)
		{
			String sqlStat = "";
			boolean flag;
			String op = "";

			sqlFile += "/* Aggregate From Original SINGLE D=" + DimVect + " M=" + MeaVect + " */" + CR;
			
			// Delete old results
			Vector Pred = new Vector ();
			
			flag = false;
			for (int i = 0; i < m_FullDVector.size (); i++)
			{
				op = (String) m_FullDVector.elementAt (i);
				flag = DimVect.contains (op);
				if (flag)
					Pred.addElement ("(" + op + "<>-1)");
				else
					Pred.addElement ("(" + op + "=-1)");
			}
			
			sqlStat = dbFunctions.dbDelete (m_tblName + resultTableText, Pred);
			sqlFile += sqlStat + ";" + CR + CR;
			dbConn.sendQuery (sqlStat);
			
			// SELECT FOR RESULT
			Columns = new Vector ();
			TableName = new Vector ();
			Predicate = new Vector ();
			OrderBy = new Vector ();
			GroupBy = new Vector ();
			
			Columns.addElement ("cnt + sum(1) OVER (order by 1 rows unbounded preceding)");
			
			flag = false;
			for (int lp1 = 0; lp1 < m_FullDVector.size (); lp1++)
			{
				op = (String) m_FullDVector.elementAt (lp1);
				flag = DimVect.contains (op);
				if (flag)
				{
					Columns.addElement(op);
					GroupBy.addElement(op);
				}
				else
					Columns.addElement("-1");
			}
			
			Columns.addElement("sum(1.0) as N");
			
			op = "";
			for (int i = 0; i < MeaVect.size (); i++)
			{
				op = (String) MeaVect.elementAt (i);
				Columns.addElement("sum(" + op + ")");
				Columns.addElement("sum(" + op + "*" + op + ")");
			}
			for (int i = 0; i < ImgVect.size (); i++)
			{
				op = (String) ImgVect.elementAt (i);
				Columns.addElement("sum(" + op + ")");
				Columns.addElement("sum(" + op + "*" + op + ")");
			}
			TableName.addElement(fromTable);

			Vector Columns2 = new Vector ();
			Vector TableName2 = new Vector ();
			Vector Predicate2 = new Vector ();
			Vector OrderBy2 = new Vector ();
			Vector GroupBy2 = new Vector ();
			
			Columns2.addElement ("count(*) as cnt");
			TableName2.addElement (m_tblName + resultTableText);
			
			TableName.addElement ("(" + dbFunctions.dbSelect (Columns2,TableName2,Predicate2,OrderBy2,GroupBy2,-1) + ") as t1");
			
			// INSERT INTO RESULT
			SelectStatement = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy,0);
			TblName = m_tblName + resultTableText;
			Columns = new Vector ();
			Values = new Vector ();
			
			Columns.addElement("id");
			for (int i = 0; i < m_FullDVector.size (); i++)
				Columns.addElement((String)m_FullDVector.elementAt (i));
		
			Columns.addElement("N");

			for (int i = 0; i < m_FullMVector.size (); i++)
			{
				Columns.addElement("L_" + (String)m_FullMVector.elementAt (i));
				Columns.addElement("Q_" + (String)m_FullMVector.elementAt (i));
			}
			System.out.println ("*****************" + m_FullIVector);
			for (int i = 0; i < m_FullIVector.size (); i++)
			{
				Columns.addElement("L_" + (String)m_FullIVector.elementAt (i));
				Columns.addElement("Q_" + (String)m_FullIVector.elementAt (i));
			}
		
			sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, 0) + ";";
			sqlFile += sqlStat + CR + CR;
			dbConn.sendQuery (sqlStat);

			
			// Delete old summary
			Pred = new Vector ();
			
			flag = false;
			for (int i = 0; i < m_FullDVector.size (); i++)
			{
				op = (String) m_FullDVector.elementAt (i);
				flag = DimVect.contains (op);
				if (flag)
					Pred.addElement ("(" + op + "=1)");
				else
					Pred.addElement ("(" + op + "=0)");
			}
			
			sqlStat = dbFunctions.dbDelete (m_tblName + summaryTableText, Pred);
			sqlFile += sqlStat + ";" + CR + CR;
			dbConn.sendQuery (sqlStat);

			
			// INSERT INTO SUMMARY
			SelectStatement = "";
			TblName = m_tblName + summaryTableText;
			Columns = new Vector ();
			Values = new Vector ();
			
			int cnt = 0;
			for (int lp1 = 0; lp1 < m_FullDVector.size (); lp1++)
			{
				op = (String) m_FullDVector.elementAt (lp1);
				flag = DimVect.contains (op);
				if (flag)
				{
					Values.addElement("1");
					cnt++;
				}
				else
					Values.addElement("0");
			}
			Values.addElement(new Integer(cnt).toString ());
			
			sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1) + ";";
			sqlFile += sqlStat + CR + CR;
			dbConn.sendQuery (sqlStat);
		}
		else
		{
			int vectSize = DimVect.size ();
			int lp1;
			String sqlStat = "";
			
			sqlFile += "/* Aggregate From Original MULTIPLE D=" + DimVect + " M=" + MeaVect + " */" + CR;
			sqlStat = dbFunctions.dbDropTable (curTable) + ";" + CR;
			dbConn.sendQuery (sqlStat);
			sqlFile += sqlStat;
			
			// Parameters for Select
			Columns = new Vector ();
			TableName = new Vector ();
			Predicate = new Vector ();
			OrderBy = new Vector ();
			GroupBy = new Vector ();
			
			Columns.addElement ("sum(1) OVER (order by 1 rows unbounded preceding) as id");

			String op = "";
			for (lp1 = 0; lp1 < vectSize; lp1++)
			{
				op = (String) DimVect.elementAt (lp1);
				Columns.addElement(op);
				GroupBy.addElement(op);
			}
			Columns.addElement("sum(1.0) as N");
			
			op = "";
			for (int i = 0; i < MeaVect.size (); i++)
			{
				op = (String) MeaVect.elementAt (i);
				Columns.addElement("max(" + op + ") as MAX_" + op);
				Columns.addElement("sum(" + op + ") as L_" + op);
				Columns.addElement("sum(" + op + "*" + op + ") as Q_" + op);
			}
			for (int i = 0; i < ImgVect.size (); i++)
			{
				op = (String) ImgVect.elementAt (i);
				Columns.addElement("max(" + op + ") as MAX_" + op);
				Columns.addElement("sum(" + op + ") as L_" + op);
				Columns.addElement("sum(" + op + "*" + op + ") as Q_" + op);
			}			
			TableName.addElement(fromTable);
			
			// Parameters for Create Table with Select
			SelectStatement = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
			TblName = curTable;
			PrimeKey = new Vector ();
			
			sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement,PrimeKey);
			sqlFile += sqlStat + ";" + CR + CR;
			dbConn.sendQuery (sqlStat);
		}
		
		if (debugFlag)
		{
			msgStr = "END aggregateFromOriginal";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
	}
	
	/*	Function to calculate the actual differences once the nlq is calculated
	*	Parameter:
	*		String currentTableName - name of the NLQ table
	*	Return: None
	*/
	
	private void createDifferencesTable (String currentTableName, Vector partialVector)
	{	
		String sqlStat;

		sqlFile += dbFunctions.logStart (dbConn, technique) + ";" + CR + CR;
                if(test == 2){
                    createMSVTableSingle (MSVTableName, currentTableName, partialVector, m_FullMVector, m_FullIVector);
                }else{
                    createMSVTable (MSVTableName + idTable, currentTableName, partialVector, m_FullMVector, m_FullIVector);
                }
		sqlFile += dbFunctions.logUpdate (dbConn, technique, "3 MSV Table", expParm) + ";" + CR + CR;

                
		sqlFile += dbFunctions.logStart (dbConn, technique) + ";" + CR + CR;
                
                if(test == 3){
                    createCompStatTable (CompareStatementsTableName, MSVTableName, partialVector, m_PartialMVector);
                }else if(test ==4){
                    createCompStatTablePP ( CompareStatementsTableName + "PP", MSVTableName , partialVector);
                }
                else if(test ==7){
                    createCompStatTableCHI ( CompareStatementsTableName + "PP" + idTable, MSVTableName + idTable, partialVector);
                }
		
		sqlFile += dbFunctions.logUpdate (dbConn, technique, "4 CompareStat Table", expParm) + ";" + CR + CR;
		
		sqlFile += dbFunctions.logStart (dbConn, technique) + ";" + CR + CR;
                if(test == 1 ){
                    insertIntoTempResultsTableSingle (MSVTableName, partialVector, m_PartialMVector);
                }
                else if(test == 2 ){
                    insertIntoTempResultsTableSingleProportion (MSVTableName, partialVector);
                }
                else if(test == 3 ){
                    insertIntoTempResultsTable (CompareStatementsTableName, partialVector, m_PartialMVector);
                }else if(test == 4){
                    insertIntoTempResultsTablePP (CompareStatementsTableName + "PP", partialVector);
                }else if(test == 5){
                    insertIntoTempResultsTableSingleVariance (MSVTableName, partialVector, m_PartialMVector);
                }else if(test ==6){
                    insertIntoTempResultsTableVariance (CompareStatementsTableName, partialVector, m_PartialMVector);
                }else if(test == 7){
                    insertIntoTempResultsTableCHI (CompareStatementsTableName + "PP" + idTable, partialVector);
                }
		sqlFile += dbFunctions.logUpdate (dbConn, technique, "5 Temp Results", expParm) + ";" + CR + CR;
		
		sqlFile += dbFunctions.logStart (dbConn, technique) + ";" + CR + CR;
                if(test == 3 || test == 1){
                    saveResultsTable (partialVector, m_PartialMVector);
                }else if(test == 2 || test == 4){
                    saveResultsTablePP (partialVector);
                }else if(test == 5 ){
                    saveResultsSingleVariance (partialVector, m_PartialMVector);
                }else if(test == 6){
                    saveResultsVariance (partialVector, m_PartialMVector);
                }else if(test == 7){
                    saveResultsTableCHI (partialVector);
                }
                
		sqlFile += dbFunctions.logUpdate (dbConn, technique, "6 Save Results", expParm) + ";" + CR + CR;
	}
	
	/*	Function to create the mean/std/variance table
	*	Parameter:
	*		String toTable - table to store the info
	*		String fromTable - table to calculate the info from
	*		Vector DimVect - dimensions
	*		Vector MeaVect - dimensions
	*	Return: None
	*/
	private void createMSVTable (String toTable, String fromTable, Vector DimVect, Vector MeaVect, Vector ImgVect)
	{
		if (debugFlag)  {
			msgStr = "START createMSVTable with toTable=" + toTable + "  fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect + "  ImgVect=" + ImgVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		msgStr = "Creating MSV Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
		
		String sqlStat = "";
		
		if (SingleTable)
		{
		
		sqlStat = dbFunctions.dbDropTable (toTable);
		sqlFile += sqlStat + ";" + CR + CR;
		dbConn.sendQuery (sqlStat);
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		Columns.addElement("N");
		for (int lp1 = 0; lp1 < DimVect.size (); lp1++)
			Columns.addElement(DimVect.elementAt (lp1));
                
                if(test == 7){
                    Columns.addElement("0 as T1");
                    Columns.addElement("0 as T2");
                }
		
		String op = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			op = (String) MeaVect.elementAt (i);
			Columns.addElement("(L_" + op + "/N) as MEAN_" + op);
			Columns.addElement("((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N)) as VARIANCE_" + op);
			Columns.addElement("sqrt(((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N))) as STD_" + op);
		}
		
		for (int i = 0; i < ImgVect.size (); i++)
		{
			op = (String) ImgVect.elementAt (i);
			Columns.addElement("(L_" + op + "/N) as MEAN_" + op);
			Columns.addElement("((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N)) as VARIANCE_" + op);
			Columns.addElement("sqrt(((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N))) as STD_" + op);
		}
                
		Vector Columns2 = new Vector ();
		Vector TableName2 = new Vector ();
		Vector Predicate2 = new Vector ();
		Vector OrderBy2 = new Vector ();
		Vector GroupBy2 = new Vector ();
		
		Columns2.addElement ("*");
		TableName2.addElement (m_tblName + resultTableText);
		
		boolean flag = true;
		String tpstr = "";
		for (int i = 0; i < m_FullDVector.size (); i++)
		{
			if (DimVect.contains ((String)m_FullDVector.elementAt (i)))
			{
				Predicate2.addElement ("(" + (String)m_FullDVector.elementAt (i) + "<>-1)");
			}
			else
			{
				Predicate.addElement ("(" + (String)m_FullDVector.elementAt (i) + "=-1)");
			}
		}

		TableName.addElement("( " + CR + dbFunctions.dbSelect (Columns2,TableName2,Predicate2,OrderBy2,GroupBy2,-1) + " ) AS t1");
		
		SelectStatement = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		TblName = toTable;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";" + CR + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		}
		else
		{
		sqlStat = dbFunctions.dbDropTable (toTable);
		sqlFile += sqlStat + ";" + CR + CR;
		dbConn.sendQuery (sqlStat);
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		Columns.addElement("N");
		for (int lp1 = 0; lp1 < DimVect.size (); lp1++)
			Columns.addElement(DimVect.elementAt (lp1));
		
		String op = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			op = (String) MeaVect.elementAt (i);
			Columns.addElement("(L_" + op + "/N) as MEAN_" + op);
			Columns.addElement("((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N)) as VARIANCE_" + op);
			Columns.addElement("sqrt(((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N))) as STD_" + op);
		}
		
		for (int i = 0; i < ImgVect.size (); i++)
		{
			op = (String) ImgVect.elementAt (i);
			Columns.addElement("(L_" + op + "/N) as MEAN_" + op);
			Columns.addElement("((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N)) as VARIANCE_" + op);
			Columns.addElement("sqrt(((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N))) as STD_" + op);
		}

		
		TableName.addElement(fromTable);
		
		SelectStatement = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		TblName = toTable;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";" + CR + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		}
                
                /************/
                
                if(test == 7){
                    Vector dim1 = new Vector();
                    Vector dim2 = new Vector();
                    
                    sqlStat = "SELECT distinct " + Columns.elementAt(1).toString() +" from " + TblName;
                    dbConn.sendQuery (sqlStat);
                    
                    String[] cols = new String[1];
                    cols[0] = Columns.elementAt(1).toString();
                    
                    String[] tmp;
                    tmp = dbConn.getResult(cols);
                    while (tmp.length != 0){
                        dim1.add(tmp[0]);
                        tmp = dbConn.getResult(cols);
                    }
                    
                    sqlStat = "SELECT distinct " + Columns.elementAt(2).toString() +" from " + TblName;
                    dbConn.sendQuery (sqlStat);
                    
                    cols = new String[1];
                    cols[0] = Columns.elementAt(2).toString();
                    
                    tmp = dbConn.getResult(cols);
                    while (tmp.length != 0){
                        dim2.add(tmp[0]);
                        tmp = dbConn.getResult(cols);
                    }
                    
                    for(int i=0; i<dim1.size(); i++){
                        for(int j=0; j<dim2.size(); j++){
                            sqlStat = "SELECT 'exist' as exist from " + TblName + " where "
                                    + Columns.elementAt(1).toString() + " =" + dim1.elementAt(i)
                                    + " AND " + Columns.elementAt(2).toString() + " =" + dim2.elementAt(j) ;
                            dbConn.sendQuery(sqlStat);
                            cols = new String[1];
                            cols[0] = "exist";
                            tmp =dbConn.getResult(cols);
                            if(tmp.length == 0){
                                sqlStat = "INSERT INTO " + TblName + " VALUES(0,"+dim1.elementAt(i) +","
                                        + dim2.elementAt(j) +")";
                                dbConn.sendQuery(sqlStat);
                            }
                                                      
                            //Actualiza totales
                            sqlStat = "UPDATE " + TblName + " SET T1 = ( SELECT count(1) FROM " + m_tblName 
                                    + " WHERE " + Columns.elementAt(1).toString() + " =" + dim1.elementAt(i) + " ) "
                                    + " WHERE " + Columns.elementAt(1).toString() + " =" + dim1.elementAt(i)
                                    + " AND " + Columns.elementAt(2).toString() + " =" + dim2.elementAt(j);
                            dbConn.sendQuery(sqlStat);
                            
                            sqlStat = "UPDATE " + TblName + " SET T2 = ( SELECT count(1) FROM " + m_tblName 
                                    + " WHERE " + Columns.elementAt(2).toString() + " =" + dim2.elementAt(j) + " ) "
                                    + " WHERE " + Columns.elementAt(1).toString() + " =" + dim1.elementAt(i)
                                    + " AND " + Columns.elementAt(2).toString() + " =" + dim2.elementAt(j);
                            dbConn.sendQuery(sqlStat);
                        }
                    }
                }
		
		if (debugFlag) {
			msgStr = "END createMSVTable";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
	}
	
        
        /*	Function to create the mean/std/variance table
	*	Parameter:
	*		String toTable - table to store the info
	*		String fromTable - table to calculate the info from
	*		Vector DimVect - dimensions
	*		Vector MeaVect - dimensions
	*	Return: None
	*/
	private void createMSVTableSingle (String toTable, String fromTable, Vector DimVect, Vector MeaVect, Vector ImgVect)
	{
		if (debugFlag)  {
			msgStr = "START createMSVTable with toTable=" + toTable + "  fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect + "  ImgVect=" + ImgVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		msgStr = "Creating MSV Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
		
		String sqlStat = "";
		
		if (SingleTable)
		{
		
		sqlStat = dbFunctions.dbDropTable (toTable);
		sqlFile += sqlStat + ";" + CR + CR;
		dbConn.sendQuery (sqlStat);
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		Columns.addElement("N");
		for (int lp1 = 0; lp1 < DimVect.size (); lp1++)
			Columns.addElement(DimVect.elementAt (lp1));
		
		String op = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			op = (String) MeaVect.elementAt (i);
			Columns.addElement("(L_" + op + "/N) as MEAN_" + op);
			Columns.addElement("((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N)) as VARIANCE_" + op);
			Columns.addElement("sqrt(((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N))) as STD_" + op);
		}
		
		for (int i = 0; i < ImgVect.size (); i++)
		{
			op = (String) ImgVect.elementAt (i);
			Columns.addElement("(L_" + op + "/N) as MEAN_" + op);
			Columns.addElement("((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N)) as VARIANCE_" + op);
			Columns.addElement("sqrt(((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N))) as STD_" + op);
		}
                
                Columns.addElement("1 as NT");
                
		Vector Columns2 = new Vector ();
		Vector TableName2 = new Vector ();
		Vector Predicate2 = new Vector ();
		Vector OrderBy2 = new Vector ();
		Vector GroupBy2 = new Vector ();
		
		Columns2.addElement ("*");
		TableName2.addElement (m_tblName + resultTableText);
		
		boolean flag = true;
		String tpstr = "";
		for (int i = 0; i < m_FullDVector.size (); i++)
		{
			if (DimVect.contains ((String)m_FullDVector.elementAt (i)))
			{
				Predicate2.addElement ("(" + (String)m_FullDVector.elementAt (i) + "<>-1)");
			}
			else
			{
				Predicate.addElement ("(" + (String)m_FullDVector.elementAt (i) + "=-1)");
			}
		}

		TableName.addElement("( " + CR + dbFunctions.dbSelect (Columns2,TableName2,Predicate2,OrderBy2,GroupBy2,-1) + " ) AS t1");
		
		SelectStatement = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		TblName = toTable;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";" + CR + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
                
                    Vector Columns = new Vector ();
                    Vector TableName = new Vector ();
                    Vector Predicate = new Vector ();
                    Vector OrderBy = new Vector ();
                    Vector GroupBy = new Vector ();
                    
                    Columns.add("sum(n) as n");
                    TableName.add(toTable);
                    
                    DBSQLConnect dbConn2 = new DBSQLConnect (dbmsType, connectFlag, printFlag);
                    dbConn2.startConnection ();
                    String test2 = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
                    dbConn2.sendQuery(test2);
                    
                    String[] cols2 = new String[1];
                    cols2[0] = "n";
                    
                    String[] tmp2;
                    tmp2 = dbConn2.getResult(cols2);
                    String nt = tmp2[0];
                    
                    //Update de los valores en la tabla
                    Vector Sets = new Vector();
                    Sets.add("nt = " + nt);

                    test2 = dbFunctions.dbUpdate(TableName, Sets, Predicate);
                    dbConn2.sendQuery(test2);
                    
                    dbConn2.stopConnection();
                    
		}
		else
		{
		sqlStat = dbFunctions.dbDropTable (toTable);
		sqlFile += sqlStat + ";" + CR + CR;
		dbConn.sendQuery (sqlStat);
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		Columns.addElement("N");
		for (int lp1 = 0; lp1 < DimVect.size (); lp1++)
			Columns.addElement(DimVect.elementAt (lp1));
		
		String op = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			op = (String) MeaVect.elementAt (i);
			Columns.addElement("(L_" + op + "/N) as MEAN_" + op);
			Columns.addElement("((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N)) as VARIANCE_" + op);
			Columns.addElement("sqrt(((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N))) as STD_" + op);
		}
		
		for (int i = 0; i < ImgVect.size (); i++)
		{
			op = (String) ImgVect.elementAt (i);
			Columns.addElement("(L_" + op + "/N) as MEAN_" + op);
			Columns.addElement("((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N)) as VARIANCE_" + op);
			Columns.addElement("sqrt(((Q_" + op + "/N) - (L_" + op + "/N)*(L_" + op + "/N))) as STD_" + op);
		}

		
		TableName.addElement(fromTable);
		
		SelectStatement = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		TblName = toTable;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";" + CR + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		}
		
		if (debugFlag) {
			msgStr = "END createMSVTable";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
	}
        
	/*	Function to create table for comparing statements
	*	Parameter:
	*		String toTable - table to store values
	*		String fromTable - table to obtain values from
	*		Vector DimVect - dimensions
	*		Vector MeaVect - measures
	*	Return: None
	*/
	private void createCompStatTable (String toTable, String fromTable, Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START createCompStatTable with toTable=" + toTable + "  fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
                
                //EDGAR
                //Obtiene los valores de cada dimension
                calcSizeDim(DimVect);

		msgStr = "Creating CompStat Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
		
		String sqlStat;

		sqlStat = dbFunctions.dbDropTable (toTable) + ";" + CR;
		dbConn.sendQuery (sqlStat);
		sqlFile += sqlStat;

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		PrimeKey = new Vector ();
		
		String op = "";
		String tp = "";
		Columns.addElement("t1.N AS N1");
		for (int i = 0; i < DimVect.size (); i++)
		{
			op = (String) DimVect.elementAt (i);
			Columns.addElement("t1." + op + " AS " + op + "1");
                        //EDGAR
                        //NOTA: con PK no se crea la tabla
			PrimeKey.addElement(op + "1");
		}
		
		for (int i = 0; i < MeaVect.size (); i++)
		{
			op = (String) MeaVect.elementAt (i);
			Columns.addElement("t1" + ".mean_" + op + " as MEAN_" + op + "1");
			Columns.addElement("t1" + ".variance_" + op + " as VARIANCE_" + op + "1");
			Columns.addElement("t1" + ".std_" + op + " as STD_" + op + "1");
		}
		
		
		Columns.addElement("t2.N AS N2");
		for (int i = 0; i < DimVect.size (); i++)
		{
			op = (String) DimVect.elementAt (i);
			Columns.addElement("t2." + op + " AS " + op + "2");
                        //EDGAR
                        //NOTA: con PK no se crea la tabla
			PrimeKey.addElement(op + "2");
		}
		
		for (int i = 0; i < MeaVect.size (); i++)
		{
			op = (String) MeaVect.elementAt (i);
			Columns.addElement("t2" + ".mean_" + op + " as MEAN_" + op + "2");
			Columns.addElement("t2" + ".variance_" + op + " as VARIANCE_" + op + "2");
			Columns.addElement("t2" + ".std_" + op + " as STD_" + op + "2");
		}
	
		TableName.addElement(fromTable + " AS t1");
		TableName.addElement(fromTable + " AS t2");
	
		String operation = "";
		tp = "";
		for (int i = 0; i < DimVect.size (); i++)
		{
			if (i == 0)
				tp += "(";
			else
				tp += " OR (";
			
                        Vector DimVectValues = (Vector)values.get(i);
                        for(int k = 0; k < DimVectValues.size(); k++){
                            if (k != 0)
                                    tp += " OR ( ";
			for (int j = 0; j < DimVect.size (); j++)
			{
                                
				if (i == j) 
					operation = "<>";
				else 
					operation = "=";
	
				if (j != 0)
					tp += " AND ";
					
				tp += "t1." + DimVect.elementAt (j) + operation + "t2." + DimVect.elementAt (j);
				if (i == j)
					tp += " AND t1." + DimVect.elementAt (j) + "=" + (String)DimVectValues.get(k) + " ";
                           
			}
			tp += ")";
                        }
		}
		Predicate.addElement(tp);

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = toTable;
	
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";" + CR + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
		
		if (debugFlag)
		{
			msgStr = "END createCompStatTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
	}
	

	/*	Function to create the empty results tables (tempz, tempt)
	*	Parameter:
	*		Vector DimVect - dimensions
	*		Vector MeaVect - measures
	*	Return: None
	*/
	private void createEmptyResultsTable (Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START createEmptyResultsTable with DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
		msgStr = "Creating Empty Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		String temp = "";
		String sqlStat = "";
		
		sqlStat = dbFunctions.dbDropTable (m_tblName + resultTempZText) + ";" + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		//Creating resultsTempZ table
		TblName = m_tblName + resultTempZText;
		ColName = new Vector ();
		ColType = new Vector ();
		PrimeKey = new Vector ();

		String tt = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tt = (String) MeaVect.elementAt (i);
			ColName.addElement("PValue_" + tt);
			ColType.addElement("float");
			ColName.addElement("ZTest_" + tt);
			ColType.addElement("float");
		}
		ColName.addElement("N1");
		ColType.addElement("int");
		
		for (int i = 0; i < DimVect.size (); i++)
		{
			ColName.addElement(DimVect.elementAt (i) + "1");
			ColType.addElement("int");
		}
		ColName.addElement("N2");
		ColType.addElement("int");

		for (int i = 0; i < DimVect.size (); i++)
		{
			ColName.addElement(DimVect.elementAt (i) + "2");
			ColType.addElement("int");
		}
		
		sqlStat = dbFunctions.dbCreateTable (TblName,ColName,ColType,PrimeKey,-1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
		
		sqlStat = dbFunctions.dbDropTable (m_tblName + resultTempTText) + ";" + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);

		// Creating resultsTempT table
		TblName = m_tblName + resultTempTText;
		ColName = new Vector ();
		ColType = new Vector ();
		PrimeKey = new Vector ();

		tt = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tt = (String) MeaVect.elementAt (i);
			ColName.addElement("PValue_" + tt);
			ColType.addElement("float");
			ColName.addElement("TTest" + tt);
			ColType.addElement("float");
			ColName.addElement("DF" + tt);
			ColType.addElement("int");
		}
		ColName.addElement("N1");
		ColType.addElement("int");
		
		for (int i = 0; i < DimVect.size (); i++)
		{
			ColName.addElement(DimVect.elementAt (i) + "1");
			ColType.addElement("int");
		}
		ColName.addElement("N2");
		ColType.addElement("int");

		for (int i = 0; i < DimVect.size (); i++)
		{
			ColName.addElement(DimVect.elementAt (i) + "2");
			ColType.addElement("int");
		}

		sqlStat = dbFunctions.dbCreateTable (TblName,ColName,ColType,PrimeKey,-1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
		
		if (debugFlag)
		{
			msgStr = "END createEmptyResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}
        private void createEmptyResultsTable (Vector DimVect)
	{
		
		msgStr = "Creating Empty Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		String temp = "";
		String sqlStat = "";
		
		sqlStat = dbFunctions.dbDropTable (m_tblName + resultTempZText + idTable) + ";" + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		//Creating resultsTempZ table
		TblName = m_tblName + resultTempZText + idTable;
		ColName = new Vector ();
		ColType = new Vector ();
		PrimeKey = new Vector ();

		String tt = "";
		
			ColName.addElement("PValue" + tt);
			ColType.addElement("float");
			ColName.addElement("ZTest" + tt);
			ColType.addElement("float");
                        
		ColName.addElement("N1");
		ColType.addElement("int");
		
		for (int i = 0; i < DimVect.size (); i++)
		{
			ColName.addElement(DimVect.elementAt (i) + "1");
			ColType.addElement("int");
		}
		ColName.addElement("N2");
		ColType.addElement("int");

		for (int i = 0; i < DimVect.size (); i++)
		{
			ColName.addElement(DimVect.elementAt (i) + "2");
			ColType.addElement("int");
		}
		
		sqlStat = dbFunctions.dbCreateTable (TblName,ColName,ColType,PrimeKey,-1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		if (debugFlag)
		{
			msgStr = "END createEmptyResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}
        
        /*	Function to insert values into the temporary results tables
	*	Parameter:
	*		String fromTable - table to extract information
	*		Vector DimVect - set of dimensions
	*		Vector MeaVect - set of measures
	*	Return: None
	*/
	private void insertIntoTempResultsTableSingle (String fromTable, Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START insertIntoTempResultsTable with fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		String sqlStat = "";
		
		//create empty results tables
		createEmptyResultsTable (DimVect, MeaVect);
		
		msgStr = "Filling Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		String tt = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tt = (String) MeaVect.elementAt (i);
			Columns.addElement("((mean_" + tt + " -" + parameter + ")/(std_" + tt + " / sqrt(N) ))");
		}
		
		Columns.addElement("N");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "");
		
		
		TableName.addElement(fromTable);
		Predicate.addElement("N>=" + pThreshold );

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + resultTempZText;
		Columns = new Vector ();
		Values = new Vector ();
		
		for (int i = 0; i < MeaVect.size (); i++)
		{
			Columns.addElement("ZTest_" + MeaVect.elementAt (i));
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");

		sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
			
		sqlFile += CR + CR;

		if (debugFlag) {
			msgStr = "END insertIntoTempResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}
        
        /*	Function to insert values into the temporary results tables
	*	Parameter:
	*		String fromTable - table to extract information
	*		Vector DimVect - set of dimensions
	*		Vector MeaVect - set of measures
	*	Return: None
	*/
	private void insertIntoTempResultsTableSingleProportion (String fromTable, Vector DimVect)
	{

		String sqlStat = "";
		
		//create empty results tables
		createEmptyResultsTable (DimVect);
		
		msgStr = "Filling Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		Columns.addElement("((N/NT) - (" + parameter +")) / sqrt((("+parameter+") * (1 - (N/NT))) / (N) )");
		
		Columns.addElement("N");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "");
		
		
		TableName.addElement(fromTable);
		Predicate.addElement("N>=" + pThreshold );

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + resultTempZText;
		Columns = new Vector ();
		Values = new Vector ();
		
		Columns.addElement("ZTest");
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");

		sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
			
		sqlFile += CR + CR;

		if (debugFlag) {
			msgStr = "END insertIntoTempResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}

	/*	Function to insert values into the temporary results tables
	*	Parameter:
	*		String fromTable - table to extract information
	*		Vector DimVect - set of dimensions
	*		Vector MeaVect - set of measures
	*	Return: None
	*/
	private void insertIntoTempResultsTableVariance (String fromTable, Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START insertIntoTempResultsTable with fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		String sqlStat = "";
		
		//create empty results tables
		createEmptyResultsTable (DimVect, MeaVect);
		
		msgStr = "Filling Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		String tt = "";
                
                String tmpStr = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tt = (String) MeaVect.elementAt (i);
                        tmpStr = "CASE WHEN (variance_" + tt + "1 > variance_" + tt + "2 ) THEN "
                                + " (variance_" + tt + "1 / variance_" + tt + "2 ) "
                                + " ELSE (variance_" + tt + "2 / variance_" + tt + "1 ) END "
                                + " AS Ztest_" + tt;
			Columns.addElement(tmpStr);
		}
		
		Columns.addElement("CASE when (variance_" + tt + "1 > variance_" + tt + "2 ) THEN N1 ELSE N2 END AS N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");
		
		TableName.addElement(fromTable);
		Predicate.addElement("N1>=" + pThreshold + " AND N2>=" + pThreshold);

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + resultTempZText;
		Columns = new Vector ();
		Values = new Vector ();
		
		for (int i = 0; i < MeaVect.size (); i++)
		{
			Columns.addElement("ZTest_" + MeaVect.elementAt (i));
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");

		sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
			
		sqlFile += CR + CR;
			
		if (debugFlag) {
			msgStr = "END insertIntoTempResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}
        
        
        private void insertIntoTempResultsTable (String fromTable, Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START insertIntoTempResultsTable with fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		String sqlStat = "";
		
		//create empty results tables
		createEmptyResultsTable (DimVect, MeaVect);
		
		msgStr = "Filling Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		String tt = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tt = (String) MeaVect.elementAt (i);
			Columns.addElement("((mean_" + tt + "1-mean_" + tt + "2)/(sqrt((variance_" + tt + "1/N1)+(variance_" + tt + "2/N2))))");
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");
		
		TableName.addElement(fromTable);
		Predicate.addElement("N1>=" + pThreshold + " AND N2>=" + pThreshold);

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + resultTempZText;
		Columns = new Vector ();
		Values = new Vector ();
		
		for (int i = 0; i < MeaVect.size (); i++)
		{
			Columns.addElement("ZTest_" + MeaVect.elementAt (i));
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");

		sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
			
		sqlFile += CR + CR;
			
		// RESULTS TEMP T
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		tt = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tt = (String) MeaVect.elementAt (i);
			Columns.addElement("((mean_" + tt + "1-mean_" + tt + "2)/(sqrt((variance_" + tt + "1/N1)+(variance_" + tt + "2/N2))))");
			Columns.addElement("(((variance_" + tt + "1/N1) + (variance_" + tt + "2/N2))*((variance_" + tt + "1/N1) + (variance_" + tt + "2/N2)))/((((variance_" + tt + "1/N1)*(variance_" + tt + "1/N1))/(N1-1)) + (((variance_" + tt + "2/N2)*(variance_" + tt + "2/N2))/(N2-1)))");
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");

			
		TableName.addElement(fromTable);
		Predicate.addElement("N1<" + pThreshold + " OR N2<" + pThreshold);

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + resultTempTText;
		Columns = new Vector ();
		Values = new Vector ();
		
		for (int i = 0; i < MeaVect.size (); i++)
			Columns.addElement("TTest" + MeaVect.elementAt (i) + ", DF" + MeaVect.elementAt (i));
			
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");

		sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1);
		sqlFile+= sqlStat + ";" + CR + CR;
		dbConn.sendQuery (sqlStat);

		if (debugFlag) {
			msgStr = "END insertIntoTempResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}
        
        private void insertIntoTempResultsTableSingleVariance (String fromTable, Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START insertIntoTempResultsTable with fromTable=" + fromTable + "  DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

		String sqlStat = "";
		
		//create empty results tables
		createEmptyResultsTable (DimVect, MeaVect);
		
		msgStr = "Filling Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		String tt = "";
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tt = (String) MeaVect.elementAt (i);
			Columns.addElement("( ((N - 1) * (variance_" + tt + ")) /" + parameter + ")");
		}
		
		Columns.addElement("N");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "");
		
		
		TableName.addElement(fromTable);
		Predicate.addElement("N>=" + pThreshold );

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + resultTempZText;
		Columns = new Vector ();
		Values = new Vector ();
		
		for (int i = 0; i < MeaVect.size (); i++)
		{
			Columns.addElement("ZTest_" + MeaVect.elementAt (i));
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");

		sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
			
		sqlFile += CR + CR;

		if (debugFlag) {
			msgStr = "END insertIntoTempResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}
		
	/*	Function to store results into final results table....will compute the pvalues
	*	Parameter:
	*		Vector DimVect - dimensions
	*		Vector MeaVect - measures
	*	Return: None
	*/
	public void saveResultsTable (Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START saveResultsTable with DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
		msgStr = "Creating Final Results Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		String sqlStat = "";
		
		sqlFile += "/* Results */" + CR + CR;
		
		sqlStat = dbFunctions.dbDropTable (m_tblName + finalTableText) + ";" + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		String temp;
		String tpStr;
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tpStr = "";
			temp = (String) MeaVect.elementAt (i);
			tpStr += "CASE ";
			for (int j = 0; j < 3; j++)
				tpStr += "WHEN (abs(ZTest_" + temp + ")<" + m_ZValues[j] + ") THEN '" + m_ZValuesString[j] + "' ";
			tpStr += "ELSE '" + m_ZValuesString[3] + "' END AS PValue_" + temp + ", ZTest_" + temp;
			Columns.addElement(tpStr);
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
		{
			Columns.addElement(DimVect.elementAt (i) + "1");
		}
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");
			
		TableName.addElement(m_tblName + resultTempZText);
		
		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + finalTableText;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
		
		if (debugFlag) {
			msgStr = "END saveResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
	}
        
        public void saveResultsTablePP (Vector DimVect)
	{		
		msgStr = "Creating Final Results Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		String sqlStat = "";
		
		sqlFile += "/* Results */" + CR + CR;
		
		sqlStat = dbFunctions.dbDropTable (m_tblName + finalTableText) + ";" + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		String temp;
		String tpStr;
		
			tpStr = "";
		
			tpStr += "CASE ";
			for (int j = 0; j < 3; j++)
				tpStr += "WHEN (abs(ZTest)<" + m_ZValues[j] + ") THEN '" + m_ZValuesString[j] + "' ";
			tpStr += "ELSE '" + m_ZValuesString[3] + "' END AS PValue, ZTest";
			Columns.addElement(tpStr);
		
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
		{
			Columns.addElement(DimVect.elementAt (i) + "1");
		}
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");
			
		TableName.addElement(m_tblName + resultTempZText);
		
		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + finalTableText;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
		
		if (debugFlag) {
			msgStr = "END saveResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
	}
        
        public void saveResultsTableCHI (Vector DimVect)
	{		
		msgStr = "Creating Final Results Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		String sqlStat = "";
		
		sqlFile += "/* Results */" + CR + CR;
		
		sqlStat = dbFunctions.dbDropTable (m_tblName + finalTableText + idTable) + ";" + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		String temp;
		String tpStr;
		
			tpStr = "";
		
			tpStr += "CASE ";
			for (int j = 0; j < 3; j++)
				tpStr += "WHEN (abs(ZTest)<" + chi_ZValues[j] + ") THEN '" + m_ZValuesString[j] + "' ";
			tpStr += "ELSE '" + m_ZValuesString[3] + "' END AS PValue, ZTest";
			Columns.addElement(tpStr);
		
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
		{
			Columns.addElement(DimVect.elementAt (i) + "1");
		}
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");
			
		TableName.addElement(m_tblName + resultTempZText + idTable);
		
		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + finalTableText + idTable;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName , SelectStatement, PrimeKey) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
		
		if (debugFlag) {
			msgStr = "END saveResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
	}
        
        public void saveResultsSingleVariance (Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START saveResultsTable with DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
		msgStr = "Creating Final Results Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		String sqlStat = "";
		
		sqlFile += "/* Results */" + CR + CR;
		
		sqlStat = dbFunctions.dbDropTable (m_tblName + finalTableText) + ";" + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		String temp;
		String tpStr;
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tpStr = "";
			temp = (String) MeaVect.elementAt (i);
			tpStr +=  "PValue_" + temp;
			Columns.addElement(tpStr);
                        tpStr = "ZTest_" + temp;
                        Columns.addElement(tpStr);
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
		{
			Columns.addElement(DimVect.elementAt (i) + "1");
		}
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");
			
		TableName.addElement(m_tblName + resultTempZText);
		
		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + finalTableText;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
                
                /** Inserta resultados verificando la hipótesis ***/               
                 String[] names = new String [Columns.size()];
                
		 String[] results = new String [Columns.size()];
		
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		
		Columns = this.Columns;
		TableName.addElement (TblName);
		
		sqlStat = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		
		dbConn.sendQuery (sqlStat);
		
                for(int i=0; i< Columns.size(); i++){
                    names[i] = (String)Columns.elementAt(i);
                }
                
		int cnt = 0;
                
		results = dbConn.getResult (names);
		cnt = 0;
		String mstr = "";

                //Recorre los resultados, hace la comparación e inserta el resultado
                
                Vector Sets = new Vector();
                Vector PredicateU = new Vector();
                String pre ="";
                Vector TableNameU = new Vector();
                TableNameU.add(TblName);
                
		while (results.length > 0)
		{
                    Sets.clear();
                    PredicateU.clear();
                    pre="";
                    
                    int gl = Integer.valueOf(results[2]) - 1;
                    double val = Double.valueOf(results[1]);
                    String p = "0";
                    
                    if( !(gl1[gl-1] < val && val < gl2[gl-1]) ){
                        p = "3";
                    }
                    
                    Sets.add(names[0] + "=" + p);
                    
                    for (int i=0; i < DimVect.size(); i++){
                        if( i == 0){
                            pre += names[2 + MeaVect.size() + i] + "=" + results[2 + MeaVect.size() + i];
                        }
                        else{
                            pre += " AND " + names[2 + MeaVect.size() + i] + "=" + results[2 + MeaVect.size() + i];
                        }
                        
                    }
                    PredicateU.add(pre);
                    
                    String test2 = dbFunctions.dbUpdate(TableNameU, Sets, PredicateU);
                    DBSQLConnect dbConn2 = new DBSQLConnect (dbmsType, connectFlag, printFlag);
                    dbConn2.startConnection ();
                    dbConn2.sendQuery(test2);
                    dbConn2.stopConnection();
                    
                    results = dbConn.getResult(names);
                }
		
		if (debugFlag) {
			msgStr = "END saveResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
	}
        
        
        public void saveResultsVariance (Vector DimVect, Vector MeaVect)
	{
		if (debugFlag) {
			msgStr = "START saveResultsTable with DimVect=" + DimVect + "  MeaVect=" + MeaVect;
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
		msgStr = "Creating Final Results Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		String sqlStat = "";
		
		sqlFile += "/* Results */" + CR + CR;
		
		sqlStat = dbFunctions.dbDropTable (m_tblName + finalTableText) + ";" + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		
		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		String temp;
		String tpStr;
		for (int i = 0; i < MeaVect.size (); i++)
		{
			tpStr = "";
			temp = (String) MeaVect.elementAt (i);
			tpStr +=  "PValue_" + temp;
			Columns.addElement(tpStr);
                        tpStr = "ZTest_" + temp;
                        Columns.addElement(tpStr);
		}
		
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
		{
			Columns.addElement(DimVect.elementAt (i) + "1");
		}
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");
			
		TableName.addElement(m_tblName + resultTempZText);
		
		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + finalTableText;
		PrimeKey = new Vector ();
		
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
                
                /** Inserta resultados verificando la hipótesis ***/               
                 String[] names = new String [Columns.size()];
                
		 String[] results = new String [Columns.size()];
		
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		
		Columns = this.Columns;
		TableName.addElement (TblName);
		
		sqlStat = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		
		dbConn.sendQuery (sqlStat);
		
                for(int i=0; i< Columns.size(); i++){
                    names[i] = (String)Columns.elementAt(i);
                }
                
		int cnt = 0;
                
		results = dbConn.getResult (names);
		cnt = 0;
		String mstr = "";

                //Recorre los resultados, hace la comparación e inserta el resultado
                
                Vector Sets = new Vector();
                Vector PredicateU = new Vector();
                String pre ="";
                Vector TableNameU = new Vector();
                TableNameU.add(TblName);
                
		while (results.length > 0)
		{
                    Sets.clear();
                    PredicateU.clear();
                    pre="";
                    
                    int gl = Integer.valueOf(results[2]) - 1;
                    int gl2 = Integer.valueOf(results[2 + MeaVect.size() + DimVect.size()]) - 1;
                    double val = Double.valueOf(results[1]);
                    String p = "0";
                    
                    double izq = gl005v[gl-1][gl2-1];
                    double der = gl995v[gl-1][gl2-1];
                    
                    if( !(izq < val && val < der) ){
                        p = "3";
                    }
                    
                    Sets.add(names[0] + "=" + p);
                    
                    for (int i=0; i < DimVect.size(); i++){
                        if( i == 0){
                            pre += names[2 + MeaVect.size() + i] + "=" + results[2 + MeaVect.size() + i];
                        }
                        else{
                            pre += " AND " + names[2 + MeaVect.size() + i] + "=" + results[2 + MeaVect.size() + i];
                        }
                        
                    }
                    
                    for (int i=0; i < DimVect.size(); i++){
                            pre += " AND " + names[3 + MeaVect.size() + DimVect.size() + i] + "=" + results[3 + MeaVect.size() + DimVect.size() + i];
                    }
                    
                    PredicateU.add(pre);
                    
                    String test2 = dbFunctions.dbUpdate(TableNameU, Sets, PredicateU);
                    DBSQLConnect dbConn2 = new DBSQLConnect (dbmsType, connectFlag, printFlag);
                    dbConn2.startConnection ();
                    dbConn2.sendQuery(test2);
                    dbConn2.stopConnection();
                    
                    results = dbConn.getResult(names);
                }
		
		if (debugFlag) {
			msgStr = "END saveResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
		
	}
        

	/*	Function called to start the userInterface
	*	Parameter: None
	*	Return: None
	*/
        //EDGAR MOD: Declaraación de la función para llamar la interfaz que ejecuta el programa
	public void runUserInterface ()
	{
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(
//							"javax.swing.plaf.metal.MetalLookAndFeel");
//                            "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                        UIManager.getCrossPlatformLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				//EDGAR:VIDEO
				m_PartialIVector.clear ();
                                m_PartialDVector.clear();
                                m_PartialMVector.clear ();
				//for (int i = 0; i < m_FullIVector.size (); i++)
				//	m_PartialIVector.addElement (m_FullIVector.elementAt (i));
                                //EDGAR MOD: Crea la nueva interfaz enviando las variables necesarias, entre ellas los vectores de medidas, dimensiones e imágenes
				new OLAPInterface(m_FullDVector, m_FullMVector, m_FullIVector, m_PartialDVector, m_PartialMVector, m_PartialIVector, m_tblName, algorithm, dbmsType, pThreshold, sqlFileName).setVisible(true);
			}
		});
	}
        
        //EDGAR MOD: Declaraación de la función para llamar la interfaz para seleccionar Medidas y Dimensiones
	private void runUserInterfaceSelectDM ()
	{
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(
//							"javax.swing.plaf.metal.MetalLookAndFeel");
//                            "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                        UIManager.getCrossPlatformLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				//EDGAR:VIDEO
				m_PartialIVector.clear ();
                                m_PartialDVector.clear();
                                m_PartialMVector.clear ();
				//for (int i = 0; i < m_FullIVector.size (); i++)
				//	m_PartialIVector.addElement (m_FullIVector.elementAt (i));
                                //EDGAR MOD: Crea la nueva interfaz enviando las variables necesarias, entre ellas los vectores de medidas, dimensiones e imágenes
				new OLAPInterfaceSelectDM(m_FullCVector, m_FullMVector, m_FullIVector, m_PartialDVector, m_PartialMVector, m_PartialIVector, m_tblName, algorithm, dbmsType, pThreshold, sqlFileName).setVisible(true);
			}
		});
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
                
                sql = dbFunctions.dbSelectSize(m_tblName,dim);
                dbConn.sendQuery( sql );
                num = dbConn.getResult(cols);
                size = 0;
                
                while(num.length != 0){
                    
                    System.out.println((String)num[0]);
                    valuesi.add((String)num[0]);
                    size ++;
                    num = dbConn.getResult(cols);
                }
                values.add((Vector)valuesi);
                valuesi = new Vector();
                
                dimSize.add(size);
            }
        }
	
	private void myPause ()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
		System.out.print ("Press Enter to Continue.....");
		String temp = br.readLine ();
		} catch (Exception e) {}	
	}
	
	public void ArgERROR ()
	{
		System.out.println (ERRORMSG);
	}
		
	private void InputERROR ()
	{
		System.out.println ("Program exited: Error in input string");
		System.out.println (ERRORMSG);
	}
		
	private void IncProgress ()
	{
		System.out.print (".");
	}

	private void ShowSuccess ()
	{
		if (printFlag) System.out.println (" SUCCESS");
	}
	
	public String getSqlFile ()
	{
		return sqlFile;
	}
        
        //EDGAR MOD: Asigna los valores del vector de dimensiones
        public void setm_FullDVector(Vector DVector){
                m_FullDVector = DVector;
        }
        
        //EDGAR MOD: Asigna los valores del vector de medidas
        public void setm_FullMVector(Vector MVector){
                m_FullMVector = MVector;
        }

    /*** PRUEBA DE HIPOTESIS PARA LA DIFERENCIA ENTRE LAS PROPORCIONES DE DOS POBLACIONES ****/
   
    /*	Function to create table for comparing statements
	*	Parameter:
	*		String toTable - table to store values
	*		String fromTable - table to obtain values from
	*		Vector DimVect - dimensions
	*		Vector MeaVect - measures
	*	Return: None
	*/
	private void createCompStatTablePP (String toTable, String fromTable, Vector DimVect )
	{
               
                //EDGAR
                //Obtiene los valores de cada dimension
                calcSizeDim(DimVect);

		msgStr = "Creating CompStatPP Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
		
		String sqlStat;

		sqlStat = dbFunctions.dbDropTable (toTable) + ";" + CR;
		dbConn.sendQuery (sqlStat);
		sqlFile += sqlStat;

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		PrimeKey = new Vector ();
		
		String op = "";
		String tp = "";
		Columns.addElement("t1.N AS N1");
		for (int i = 0; i < DimVect.size (); i++)
		{
			op = (String) DimVect.elementAt (i);
			Columns.addElement("t1." + op + " AS " + op + "1");
                        //EDGAR
                        //NOTA: con PK no se crea la tabla
			PrimeKey.addElement(op + "1");
		}
		
		Columns.addElement("t2.N AS N2");
		for (int i = 0; i < DimVect.size (); i++)
		{
			op = (String) DimVect.elementAt (i);
			Columns.addElement("t2." + op + " AS " + op + "2");
                        //EDGAR
                        //NOTA: con PK no se crea la tabla
			PrimeKey.addElement(op + "2");
		}
                
                Columns.addElement("0 as NT1");
                Columns.addElement("0 as NT2");
                
		TableName.addElement(fromTable + " AS t1");
		TableName.addElement(fromTable + " AS t2");
	
		String operation = "";
		tp = "";
		for (int i = 0; i < DimVect.size (); i++)
		{
			if (i == 0)
				tp += "(";
			else
				tp += " OR (";
			
                        Vector DimVectValues = (Vector)values.get(i);
                        for(int k = 0; k < DimVectValues.size(); k++){
                            if (k != 0)
                                    tp += " OR ( ";
			for (int j = 0; j < DimVect.size (); j++)
			{
                                
				if (i == j) 
					operation = "<>";
				else 
					operation = "=";
	
				if (j != 0)
					tp += " AND ";
					
				tp += "t1." + DimVect.elementAt (j) + operation + "t2." + DimVect.elementAt (j);
				if (i == j)
					tp += " AND t1." + DimVect.elementAt (j) + "=" + (String)DimVectValues.get(k) + " ";
                           
			}
			tp += ")";
                        }
		}
		Predicate.addElement(tp);

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = toTable;
	
		sqlStat = dbFunctions.dbCreateTableWithSelect (TblName, SelectStatement, PrimeKey) + ";" + CR + CR;
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		sqlFile += CR + CR;
		
		if (debugFlag)
		{
			msgStr = "END createCompStatTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}
                
                //Select de la tabla recien creada para actualizar los totales
                Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		PrimeKey = new Vector ();
                
                Columns.add("*");
                TableName.add(TblName);
                
                String test = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		dbConn.sendQuery(test);
                dbConn.solutionExists();
                String[] tmp;
                
                String[] cols = new String[DimVect.size()*2];
                for (int i = 0; i < DimVect.size() ; i++){
                    cols[i] = DimVect.elementAt(i) + "1";
                    cols[i + DimVect.size()] = DimVect.elementAt(i) + "2";
                }
                
                tmp = dbConn.getResult (cols);
		while (tmp.length != 0)
		{       
                    String diff = "";
                    String v1 = "";
                    String v2 = "";
                    for(int i=0; i < DimVect.size(); i++){
                        v1 = tmp[i];
                        v2 = tmp[i+DimVect.size()];
                                    
                        if(!v1.equals(v2)){
                            diff = (String)DimVect.elementAt(i);
                            i = DimVect.size();
                        }
                    }
                    
                    Vector Columns = new Vector ();
                    Vector TableName = new Vector ();
                    Vector Predicate = new Vector ();
                    Vector OrderBy = new Vector ();
                    Vector GroupBy = new Vector ();
                    
                    Columns.add("sum(n) as n");
                    TableName.add(fromTable);
                    Predicate.add(diff + " = " + v1);
                    
                    DBSQLConnect dbConn2 = new DBSQLConnect (dbmsType, connectFlag, printFlag);
                    dbConn2.startConnection ();
                    String test2 = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
                    dbConn2.sendQuery(test2);
                    
                    String[] cols2 = new String[1];
                    cols2[0] = "n";
                    
                    String[] tmp2;
                    tmp2 = dbConn2.getResult(cols2);
                    String nt1 = tmp2[0];
                    
                    //Para el segundo valor
                    Predicate.clear();
                    Predicate.add(diff + " = " + v2);
                    
                    test2 = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
                    dbConn2.sendQuery(test2);
                    
                    tmp2 = dbConn2.getResult(cols2);
                    String nt2 = tmp2[0];
                    
                    //Update de los valores en la tabla
                    Vector Sets = new Vector();
                    Sets.add("nt1 = " + nt1);
                    Sets.add("nt2 = " + nt2);
                    String pre="";
                    Predicate.clear();
                    
                    for(int i=0; i < cols.length ; i++){
                         if(i == 0){
                            pre += cols[i] + " = " + tmp[i] ;
                         }else{
                            pre += " AND " + cols[i] + " = " + tmp[i] ;
                         }
                    }
                    Predicate.add(pre);

                    test2 = dbFunctions.dbUpdate(this.TableName, Sets, Predicate);
                    dbConn2.sendQuery(test2);
                    
                    dbConn2.stopConnection();
                    
                    tmp = dbConn.getResult (cols);
		}
                
	}
        
        /*CHI CUADRADA */
        private void createCompStatTableCHI (String toTable, String fromTable, Vector DimVect )
	{       
                //EDGAR
                //Obtiene los valores de cada dimension
                calcSizeDim(DimVect);
                
                String sqlStat;
                sqlStat = dbFunctions.dbDropTable (toTable) + ";" + CR;
		dbConn.sendQuery (sqlStat);
                
                //Select de la tabla recien creada para actualizar los totales
                Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();
		
		PrimeKey = new Vector ();
                
                Columns.add("count(1) as total");
                TableName.add(m_tblName);
                
                String tp="";
                for (int i = 0; i < DimVect.size (); i++){
                    if(i>0)
                        tp += " AND ";
                    tp += DimVect.elementAt(i) + " is NOT NULL ";
                }
                Predicate.addElement(tp);
                
                String test = dbFunctions.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
		dbConn.sendQuery(test);
                dbConn.solutionExists();
                
                String[] cols = new String[1];
                cols[0] = "total";
                    
                String[] tmp;
                tmp = dbConn.getResult(cols);
                String total = tmp[0];

		msgStr = "Creating CompStatPP Table";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);
                
                /*****************************************************/
                sqlStat = "SELECT n ";
                
                for(int i=0; i<DimVect.size(); i++){
                    sqlStat += "," + DimVect.elementAt(i);
                }
                
                sqlStat += ",t1,t2,(cast(t1 as float)*cast(t2 as float))/"+total + " as fe into "+toTable+" FROM " + fromTable;
                dbConn.sendQuery(sqlStat);
		              
        }
        
        
        /*	Function to insert values into the temporary results tables
	*	Parameter:
	*		String fromTable - table to extract information
	*		Vector DimVect - set of dimensions
	*		Vector MeaVect - set of measures
	*	Return: None
	*/
	private void insertIntoTempResultsTablePP (String fromTable, Vector DimVect)
	{
		
		String sqlStat = "";
		
		//create empty results tables
		createEmptyResultsTable (DimVect);
		
		msgStr = "Filling Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		String tt = "";
                
		//Columns.addElement("(( n1/nt1) - (n2/nt2) ) / sqrt( (( (n1/nt1) * (1 - (n1/nt1))) / n1 ) + (( (n2/nt2) * (1 - (n2/nt2)))/n2))");	
                  Columns.addElement("(( n1/nt1) - (n2/nt2) ) / sqrt( ((1.0/nt1) + (1.0/nt2)) * ((n1+n2)/(nt1+nt2)) * (1.0-((n1+n2)/(nt1+nt2))) )");	  
                
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");
		
		TableName.addElement(fromTable);
		Predicate.addElement("N1>=" + pThreshold + " AND N2>=" + pThreshold);

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + resultTempZText;
		Columns = new Vector ();
		Values = new Vector ();
		
		Columns.addElement("ZTest");
			
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");

		sqlStat = dbFunctions.dbInsert (TblName,Columns,Values,SelectStatement, -1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
			
		if (debugFlag) {
			msgStr = "END insertIntoTempResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}
        
        private void insertIntoTempResultsTableCHI (String fromTable, Vector DimVect)
	{
		
		String sqlStat = "";
		
		//create empty results tables
		createEmptyResultsTable (DimVect);
		
		msgStr = "Filling Temp Results Tables";
		if (printFlag) System.out.println (msgStr);
		debugOutput.println (msgStr);

		Columns = new Vector ();
		TableName = new Vector ();
		Predicate = new Vector ();
		OrderBy = new Vector ();
		GroupBy = new Vector ();

		String tt = "";
                
		Columns.addElement("sum((n-fe)*(n-fe)/fe)");	
		
		Columns.addElement("0");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement("0");
		
		Columns.addElement("0");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement("0");
		
		TableName.addElement(fromTable);

		SelectStatement = dbFunctions.dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy, -1);
		TblName = m_tblName + resultTempZText;
		Columns = new Vector ();
		Values = new Vector ();
		
		Columns.addElement("ZTest");
			
		Columns.addElement("N1");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "1");
		
		Columns.addElement("N2");
		for (int i = 0; i < DimVect.size (); i++)
			Columns.addElement(DimVect.elementAt (i) + "2");

		sqlStat = dbFunctions.dbInsert (TblName+idTable,Columns,Values,SelectStatement, -1) + ";";
		sqlFile += sqlStat;
		dbConn.sendQuery (sqlStat);
			
		if (debugFlag) {
			msgStr = "END insertIntoTempResultsTable ()";
			System.out.println (msgStr);
			debugOutput.println (msgStr);
		}

	}
        
    public static void main(String[] args) throws IOException {

        Vector test = new Vector ();
		OLAPStatTest OLAP = new OLAPStatTest ();		
		
		if (args.length < 1)
		{
			System.out.println ("ERROR: OLAPStatTest requires at least 1 argument");
			OLAP.ArgERROR ();
			return;
		}

			OLAP.RunOLAPStatTest (args[0]);
	}
}
