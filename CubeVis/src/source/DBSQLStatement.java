/*
=========================================
DBSQLStatement.java
Generates SQL Commands for various DMBS
Zhibo Chen
Started: January 18, 2007
Revised:  (Revisions for EACH date are listed in order of importance)
2007-02-12 -	1) Altered SELECT Statement to create place columns in separate lines.
			2) Added default value to dbmsType in case user tpyes in wrong number (currently defaults to MySQL)
			3) Changed RETURN to CR.
2007-03-23 -	MAJOR CHANGE
			1) Changed all String[] to Vector
			2) Added AutoInc parameters for creating tables (Auto used in MySQL)
2007-03-24	1) Changed name to DBSQLStatement
=========================================
*/


//import myCommon.DBSQLConnect;

package source;
import java.util.*;

public class DBSQLStatement
{

	final static String expStartText = "experimentStartEnd";
	final static String expLogText = "experimentLog";
	final static String expResultText = "experimentResult";

	// 1 = TeraData
	// 2 = PostgreSQL
	// 3 = SQL Server
	// 4 = MySQL
	int dbms;
	
	boolean printFlag = true;
	
	String CR = "\r\n";

        //FUNCION: Determina que manejador se va a utilizar
	public DBSQLStatement (String dbmsType, boolean print)
	{

                //NOTA: Compara dbmsType con cadenas, sin embargo recibe números
		if (dbmsType.equalsIgnoreCase ("postgresql") ||
		    dbmsType.equalsIgnoreCase ("postgresql1") ||
			dbmsType.equalsIgnoreCase ("postgresql2") )
				dbms = 1;

		if (dbmsType.equalsIgnoreCase ("sqlserver") ||
		    dbmsType.equalsIgnoreCase ("sqlserver1") ||
			dbmsType.equalsIgnoreCase ("sqlserver2") )
				dbms = 2;

                	
		printFlag = print;

		switch (dbms)
		{
			case 1:
				//if (printFlag) System.out.println ("Using Teradata Commands");
                                if (printFlag) System.out.println ("Using PostgreSQL Commands");
				break;
			case 2:
				if (printFlag) System.out.println ("Using SQL Server Commands");
                                break;
			default:
				if (printFlag) System.out.println ("Unknown dbmsType, defaulting to Teradata Commands");
				dbms = 1;
				break;
		}
	}
	
	public String dbDropTable (String TblName)
	{
		String sql = "";
		
		switch (dbms)
		{
			case 1:	
                                if(TblName.contains("_Result")){
                                    //SQL Server
                                    sql += "DROP TABLE ";
                                    sql += TblName + ";";
                                    sql += "DROP SEQUENCE seq_" + TblName;
                                }else{
                                    sql += "DROP TABLE ";
                                    sql += TblName;
                                }
                                break;
			case 2:	//SQL Server
                                sql += "DROP TABLE ";
				sql += TblName;
				break;
		}
		return sql;
	}

	public String dbCreateTable (String TblName, Vector ColName, Vector ColType, Vector PrimeKey, int autoInc)
	{
		String sql = "";
		int len;
		int pklen = PrimeKey.size ();
		boolean flag;

		switch (dbms)
		{
			case 1:	//TeraData
                                if(autoInc >= 0)
                                    sql += "CREATE SEQUENCE seq_" + TblName + "; ";
				sql += "CREATE TABLE " + TblName + " (" + CR;
				len = ColName.size ();
				flag = true;
				for (int i = 0; i < len; i++)
				{
					sql += "\t";
					if (flag)
						flag = false;
					else
						sql += ", ";
					sql += (String)ColName.elementAt (i) + " " + (String)ColType.elementAt (i);
                                        if (autoInc == i)
                                            sql += " DEFAULT NEXTVAL('seq_" + TblName + "')";
					for (int j=0; j<pklen; j++)
					{
						if (((String)PrimeKey.elementAt (j)).equalsIgnoreCase ((String)ColName.elementAt (i)))
						{
							sql += " NOT NULL";
						}
					}
					sql += CR;
				}
				if (pklen == 0)
				{
					sql += ")";
				}
				else
				{
					flag = true;
					sql += ", PRIMARY KEY" + CR + "\t(";
					for (int i = 0; i < pklen; i++)
					{
						if (flag)
						{
							sql += (String)PrimeKey.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)PrimeKey.elementAt (i);
					}
					sql += "))";
				}
				break;
			case 2:	//SQL Server
				sql = "CREATE TABLE " + TblName + " (" + CR;
				len = ColName.size ();
				flag = true;
				for (int i = 0; i < len; i++)
				{
					sql += "\t";
					if (flag)
						flag = false;
					else
						sql += ", ";
					sql += (String)ColName.elementAt (i) + " " + (String)ColType.elementAt (i);
					if (autoInc == i)
						sql += " IDENTITY(1,1)";
					for (int j=0; j<pklen; j++)
					{
						if (((String)PrimeKey.elementAt (j)).equalsIgnoreCase ((String)ColName.elementAt (i)))
						{
							sql += " NOT NULL";
						}
					}
					sql += CR;
				}
				if (pklen == 0)
				{
					sql += ")";
				}
				else
				{
					flag = true;
					sql += ", PRIMARY KEY" + CR + "\t(";
					for (int i = 0; i < pklen; i++)
					{
						if (flag)
						{
							sql += (String)PrimeKey.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)PrimeKey.elementAt (i);
					}
					sql += "))";
				}
				break;
		}

	return sql;
	}

	public String dbDropView (String TblName)
	{
		String sql = "";
		sql = "DROP VIEW ";
		
		switch (dbms)
		{
			case 1:	//TeraData
			case 2:	//SQL Server
				sql += TblName;
				break;
		}
		return sql;
	}
	
	String dbCreateTableWithSelect (String TblName, String SelectStatement, Vector PrimeKey)
	{
		String sql = "";
		int pklen = PrimeKey.size ();
		boolean flag;

                //EDGAR
                //NOTA: en sqlserver se hace con un INTO
		//sql = "CREATE TABLE " + TblName;

                sql = "";

		switch (dbms)
		{
			case 1:	//Teradata
			case 2:	//PostgreSQL
			case 3:	//Oracle
				//sql += " AS (" + CR;
				if (pklen == 0){
					//sql += SelectStatement + ") WITH DATA";
                                        sql += SelectStatement.replaceAll("FROM.*[(]", "INTO " + TblName + " FROM (");
                                        if(!sql.contains("INTO")){
                                            sql = sql.replaceAll("FROM", "INTO " + TblName + " FROM ");
                                        }
                            }
				else
				{
                                        sql += SelectStatement.replaceAll("FROM", "INTO " + TblName + " FROM ");
					//sql += SelectStatement;
					/*sql += ") WITH DATA" + CR + "PRIMARY INDEX" + CR + "\t(";
					flag = true;
					for (int i = 0; i < pklen; i++)
					{
						if (flag)
						{
							sql += (String)PrimeKey.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)PrimeKey.elementAt (i);
					}
					sql += ")";
                                        */
				}
					
				break;
			case 4:	//MySQL
				if (pklen == 0)
					sql += CR + SelectStatement;
				else
				{
					sql += CR + "(PRIMARY KEY" + CR + "\t(";
					flag = true;
					for (int i = 0; i < pklen; i++)
					{
						if (flag)
						{
							sql += (String)PrimeKey.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)PrimeKey.elementAt (i);
					}
					sql += "))";
					sql += CR + SelectStatement;
				}
//					sql += " " + CR + SelectStatement + "PRIMARY KEY" + CR + "\t(" + PrimeKey + ")";
				break;
		}

		return sql;
	}

	

	public String dbCreateViewWithSelect (String TblName, String SelectStatement)
	{
		String sql = "";
		boolean flag;
		
		sql = "CREATE VIEW " + TblName;
		
		switch (dbms)
		{
			case 1:	//Teradata
			case 2:	//SQL Server
				sql += " AS (" + CR;
					sql += SelectStatement;
					sql += ")";
				break;
		}
		return sql;
	}
        
        public void dbCreateSequence (String SequenceName){
            String sql="";
                sql = " CREATE SEQUENCE " + SequenceName;
                
                
        }
	
	public String dbSample (Vector Columns, Vector TableName, Vector Predicate, int sampleNum)
	{
		String sql = "";
		int len;
		boolean flag = true;
		

		switch (dbms)
		{
			case 1:	//Teradata
				sql = "SELECT " + CR;
				
				flag = true;
				for (int i = 0; i < Columns.size (); i++)
				{
					if (flag)
					{
						sql += "\t" + (String)Columns.elementAt (i) + CR;
						flag = false;
					}
					else
						sql += "\t, " + (String)Columns.elementAt (i) + CR;
				}
				
				sql += "FROM" + CR;
				len = TableName.size ();
				flag = true;
				for (int i=0; i<len; i++)
				{
					if (flag)
					{
						sql += "\t" + (String)TableName.elementAt (i) + CR;
						flag = false;
					}
					else
						sql += "\t, " + (String)TableName.elementAt (i) + CR;
				}
			
				if (Predicate.size () > 0)
				{
					flag = true;
					sql += "WHERE" + CR + "\t";
					for (int i = 0; i < Predicate.size (); i++)
					{
						if (flag)
						{
							sql += "(" + (String)Predicate.elementAt (i) + ")";
							flag = false;
						}
						else
							sql += " AND (" + (String)Predicate.elementAt (i) + ")";
					}
					sql += CR;
				}
				sql += "SAMPLE " + sampleNum + " PERCENT" + CR;
				break;
			case 2:	//SQL Server
			// NEED TO FIX THIS LATER....this gets random rows, but goes through all the rows once
				sql = "SELECT TOP " + sampleNum + CR;
				
				flag = true;
				for (int i = 0; i < Columns.size (); i++)
				{
					if (flag)
					{
						sql += "\t" + (String)Columns.elementAt (i) + CR;
						flag = false;
					}
					else
						sql += "\t, " + (String)Columns.elementAt (i) + CR;
				}
				
				sql += "FROM" + CR;
				len = TableName.size ();
				flag = true;
				for (int i=0; i<len; i++)
				{
					if (flag)
					{
						sql += "\t" + (String)TableName.elementAt (i) + CR;
						flag = false;
					}
					else
						sql += "\t, " + (String)TableName.elementAt (i) + CR;
				}
				
				if (Predicate.size () > 0)
				{
					flag = true;
					sql += "WHERE" + CR + "\t";
					for (int i = 0; i < Predicate.size (); i++)
					{
						if (flag)
						{
							sql += "(" + (String)Predicate.elementAt (i) + ")";
							flag = false;
						}
						else
							sql += " AND (" + (String)Predicate.elementAt (i) + ")";
					}
					sql += CR;
				}

				sql += "ORDER BY newid ()" + CR;
				break;
		}
				
		return sql;
	}
	
	public String dbSelect (Vector Columns, Vector TableName, Vector Predicate, Vector OrderBy, Vector GroupBy, int autoInc)
	{
		return dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, new Vector(), autoInc);
	}	

        //FUNCION: Construye la sentencia SELECT
	public String dbSelect (Vector Columns, Vector TableName, Vector Predicate, Vector OrderBy, Vector GroupBy, Vector Having, int autoInc)
	{
		String sql = "";
		int len;
		boolean flag = true;



		switch (dbms)
		{
			/*case 1:	//Teradata
				sql = "SELECT " + CR;
				
				flag = true;
				for (int i = 0; i < Columns.size (); i++)
				{
					if (flag)
					{
						sql += "\t" + (String)Columns.elementAt (i) + CR;
						flag = false;
					}
					else
						sql += "\t, " + (String)Columns.elementAt (i) + CR;
				}
				
				sql += "FROM ";
				len = TableName.size ();
				flag = true;
				for (int i=0; i<len; i++)
				{
					if (flag)
					{
						sql += "\t" + (String)TableName.elementAt (i) + CR;
						flag = false;
					}
					else
						sql += "\t, " + (String)TableName.elementAt (i) + CR;
				}
			
				if (Predicate.size () > 0)
				{
					flag = true;
					sql += "WHERE ";
					for (int i = 0; i < Predicate.size (); i++)
					{
						if (flag)
						{
							sql += "( " + (String)Predicate.elementAt (i) + " )";
							flag = false;
						}
						else
							sql += " AND ( " + (String)Predicate.elementAt (i) + " )";
					}
					sql += CR;
				}
				
				if (OrderBy.size () > 0)
				{
					flag = true;
					sql += "ORDER BY ";
					for (int i = 0; i < OrderBy.size (); i++)
					{
						if (flag)
						{
							sql += (String)OrderBy.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)OrderBy.elementAt (i);
					}
					sql += CR;
				}
				
				if (GroupBy.size () > 0)
				{
					flag = true;
					sql += "GROUP BY ";
					for (int i = 0; i < GroupBy.size (); i++)
					{
						if (flag)
						{
							sql += (String)GroupBy.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)GroupBy.elementAt (i);
					}
					sql += CR;
				}

				if (Having.size () > 0)
				{
					flag = true;
					sql += "HAVING ";
					for (int i = 0; i < Having.size (); i++)
					{
						if (flag)
						{
							sql += "(" + (String)Having.elementAt (i) + ")";
							flag = false;
						}
						else
							sql += " AND (" + (String)Having.elementAt (i) + ")";
					}
					sql += CR;
				}
				break;
                     */
                        case 1:
			case 2:	//SQL Server
				sql = "SELECT " + CR;
				
				flag = true;
				for (int i = 0; i < Columns.size (); i++)
				{
					if (i == autoInc) continue;
					if (flag)
					{
						sql += "\t" + (String)Columns.elementAt (i) + CR;
						flag = false;
					}
					else
						sql += "\t, " + (String)Columns.elementAt (i) + CR;
				}
				
				sql += "FROM ";
				len = TableName.size ();
				flag = true;
				for (int i=0; i<len; i++)
				{
					if (flag)
					{
						sql += "\t" + (String)TableName.elementAt (i) + CR;
						flag = false;
					}
					else
						sql += "\t, " + (String)TableName.elementAt (i) + CR;
				}
			
				if (Predicate.size () > 0)
				{
					flag = true;
					sql += "WHERE ";
					for (int i = 0; i < Predicate.size (); i++)
					{
						if (flag)
						{
							sql += "( " + (String)Predicate.elementAt (i) + " )";
							flag = false;
						}
						else
							sql += " AND ( " + (String)Predicate.elementAt (i) + " )";
					}
					sql += CR;
				}
                                
                                if (GroupBy.size () > 0)
				{
					flag = true;
					sql += "GROUP BY ";
					for (int i = 0; i < GroupBy.size (); i++)
					{
						if (flag)
						{
							sql += (String)GroupBy.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)GroupBy.elementAt (i);
					}
					sql += CR;
				}
				
				if (OrderBy.size () > 0)
				{
					flag = true;
					sql += "ORDER BY ";
					for (int i = 0; i < OrderBy.size (); i++)
					{
						if (flag)
						{
							sql += (String)OrderBy.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)OrderBy.elementAt (i);
					}
					sql += CR;
				}
				
				

				if (Having.size () > 0)
				{
					flag = true;
					sql += "HAVING ";
					for (int i = 0; i < Having.size (); i++)
					{
						if (flag)
						{
							sql += "(" + (String)Having.elementAt (i) + ")";
							flag = false;
						}
						else
							sql += " AND (" + (String)Having.elementAt (i) + ")";
					}
					sql += CR;
				}
				break;
		}
				
		return sql;
	}
	
	public String dbUpdate (Vector TblNames, Vector Sets, Vector Predicates)
	{
		String sql = "UPDATE" + CR;
		boolean flag;
		
		switch (dbms)
		{
			case 1:	//Teradata
			case 2:	//SQL Server
				flag = true;
				for (int i = 0; i < TblNames.size (); i++)
				{
					if (flag)
					{
						sql += (String)TblNames.elementAt (i);
						flag = false;
					}
					else
						sql += ", " + (String)TblNames.elementAt (i);
				}
				sql += CR;
				
				if (Sets.size () > 0)
				{
					sql += "SET" + CR;
					flag = true;
					for (int i = 0; i < Sets.size (); i++)
					{
						if (flag)
						{
							sql += (String)Sets.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)Sets.elementAt (i);
					}
					sql += CR;
				}
				
				if (Predicates.size () > 0)
				{
					sql += "WHERE" + CR;
					flag = true;
					for (int i = 0; i < Predicates.size (); i++)
					{
						if (flag)
						{
							sql += (String)Predicates.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)Predicates.elementAt (i);
					}
					sql += CR;
				}
				break;
		}
		return sql;
	}
	
	public String dbInsert (String TblName, Vector Columns, Vector Values, String SelectStatement, int autoInc)
	{
		String sql = "INSERT INTO " + TblName;
		boolean flag;
		
		switch (dbms)
		{
			/*case 1:	//Teradata
				flag = true;
				if (Columns.size () > 0)
				{
					sql += CR + "(";
					for (int i = 0; i < Columns.size (); i++)
					{
						if (flag)
						{
							sql += (String)Columns.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)Columns.elementAt (i);
					}
					sql += ")";
				}
				
				flag = true;
				if (Values.size () > 0)
				{
					sql += CR + "VALUES (";
					for (int i = 0; i < Values.size (); i++)
					{
						if (flag)
						{
							sql += (String)Values.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)Values.elementAt (i);
					}
					sql += ")";
				}
				else
					sql += CR + SelectStatement;
				break;
			*/
                        case 1:
			case 2:	//SQL Server
				flag = true;
				if (Columns.size () > 0)
				{
					sql += CR + "(";
					for (int i = 0; i < Columns.size (); i++)
					{
						if (i == autoInc) continue;
						if (flag)
						{
							sql += (String)Columns.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)Columns.elementAt (i);
					}
					sql += ")";
				}
				
				flag = true;
				if (Values.size () > 0)
				{
					sql += CR + "VALUES (";
					for (int i = 0; i < Values.size (); i++)
					{
						if (flag)
						{
							sql += (String)Values.elementAt (i);
							flag = false;
						}
						else
							sql += ", " + (String)Values.elementAt (i);
					}
					sql += ")";
				}
				else
					sql += CR + SelectStatement;
				break;
		}
	
		return sql;
	}
	
	public String dbDelete (String TableName, Vector Predicate)
	{
		String sql = "DELETE FROM " + TableName + CR;
		boolean flag = true;
		for (int i = 0; i < Predicate.size (); i++)
		{
			if (flag)
			{
				sql += "WHERE (" + (String)Predicate.elementAt (i) + ")";
				flag = false;
			}
			else
				sql += " AND (" + (String)Predicate.elementAt (i) + ")";
		}
		return sql;
	}
	
	public String dbConcatStr (Vector str)
	{
		String retStr = "";
		boolean flag = true;
		
		switch (dbms)
		{
			case 1:	//Teradata
				flag = true;
				retStr = "";
				for (int i = 0; i < str.size (); i++)
					if (flag)
					{
						retStr += "cast(cast(" + (String)str.elementAt (i) + " as decimal(1,0)) as char(1))";
						flag = false;
					}
					else
						retStr += "|| cast(cast(" + (String)str.elementAt (i) + " as decimal(1,0)) as char(1))";
				break;
			case 2:	//SQL Server
				flag = true;
				retStr = "CONCAT(";
				for (int i = 0; i < str.size (); i++)
					if (flag)
					{
						retStr += (String)str.elementAt (i);
						flag = false;
					}
					else
						retStr += ", " + (String)str.elementAt (i);
				retStr += ")";
				break;
		}
		return retStr;
	}
	
	public String createLog ()
	{
		String sql = "";
	
		String TblName = expStartText;
		Vector ColName = new Vector ();
		Vector ColType = new Vector ();
		Vector PrimeKey = new Vector ();
	
		ColName.addElement ("technique");
		ColType.addElement ("char(20)");
		ColName.addElement ("startTimestamp");
		//ColType.addElement ("timestamp");
                ColType.addElement ("datetime");
		ColName.addElement ("endTimestamp");
                //EDGAR NOTA: No acepta dos columnas con timestamp
                //ColType.addElement ("timestamp");
		ColType.addElement ("datetime");
		
		PrimeKey.addElement ("technique");
		
		sql += dbCreateTable (TblName, ColName, ColType, PrimeKey, -1) + ";" + CR + CR;
		
		TblName = expLogText;
		ColName = new Vector ();
		ColType = new Vector ();
		PrimeKey = new Vector ();
		
		ColName.addElement ("experimentTS");
		ColType.addElement ("timestamp");
		ColName.addElement ("technique");
		ColType.addElement ("char(50)");
		ColName.addElement ("step");
		ColType.addElement ("char(50)");
		ColName.addElement ("elapsed");
		ColType.addElement ("float");
		ColName.addElement ("parameters");
		ColType.addElement ("char(100)");
		
		PrimeKey.addElement ("experimentTS");
		PrimeKey.addElement ("technique");
		PrimeKey.addElement ("step");
		
		sql += dbCreateTable (TblName, ColName, ColType, PrimeKey, -1);
		return sql;
	}
	
	public String logStart (DBSQLConnect dbConn, String technique)
	{
		String sql = "";
		String temp = "";
		String sqlStat;
		
		String tablename = expStartText;
		Vector Predicate = new Vector ();
		
		if (technique.length () > 20)
			temp = technique.substring (0, 20);
		else
			temp = technique;
		Predicate.addElement ("technique='" + temp + "'");
		
		sqlStat = dbDelete (tablename, Predicate) + ";" + CR;
		sql += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		String SelectStatement = "";
		String TblName = expStartText;
		Vector Columns = new Vector ();
		Vector Values = new Vector ();
		
		if (technique.length () > 20)
			temp = technique.substring (0, 20);
		else
			temp = technique;
		Values.addElement ("'" + temp + "'");
		Values.addElement ("current_timestamp");
		Values.addElement ("current_timestamp");
		
		sqlStat = dbInsert (TblName,Columns,Values,SelectStatement, -1);
		sql += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		return sql;
	}
	
	public String logUpdate (DBSQLConnect dbConn, String technique, String step, String parameters)
	{
		String sql = "";
		String sqlStat;
		String temp;
		
		// update endTimestamp in experimentStart
		Vector UpdateTblName = new Vector ();
		Vector UpdateSet = new Vector ();
		Vector UpdatePredicate = new Vector ();
		
		UpdateTblName.addElement (expStartText);
		UpdateSet.addElement ("endTimestamp = current_timestamp");
		UpdateSet.addElement ("startTimestamp = startTimestamp");
		UpdatePredicate.addElement ("technique = '" + technique + "'");
		
		sqlStat = dbUpdate (UpdateTblName, UpdateSet, UpdatePredicate);
		sql += sqlStat + ";" + CR;
		dbConn.sendQuery (sqlStat);
		
		// Insert information into experimentLog
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		Vector Having = new Vector ();

		Columns.addElement ("current_timestamp");
		
		if (technique.length () > 20)
			temp = technique.substring (0, 20);
		else
			temp = technique;
		Columns.addElement ("'" + temp + "'");
		
		if (step.length () > 20)
			temp = step.substring (0, 20);
		else
			temp = step;
			
		Columns.addElement ("'" + temp + "'");
		if (dbms == 3)
			Columns.addElement ("datediff (ms, startTimestamp, endTimestamp) / 1000.0");
		else
			Columns.addElement ("+(extract(day from endTimestamp)-extract(day from startTimestamp))*24*3600 + (extract(hour from endTimestamp)-extract(hour from startTimestamp))*3600 + (extract(minute from endTimestamp)-extract(minute from startTimestamp))*60 + (extract(second from endTimestamp)-extract(second from startTimestamp))");

		if (parameters.length () > 100)
			temp = parameters.substring (0, 100);
		else
			temp = parameters;
		Columns.addElement ("'" + temp + "'");
		
		TableName.addElement (expStartText);
		
		if (technique.length () > 20)
			temp = technique.substring (0, 20);
		else
			temp = technique;
		Predicate.addElement ("technique='" + temp + "'");
		
		String SelectStatement = dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy,Having, -1);
		String TblName = expLogText;
		Columns = new Vector ();
		Vector Values = new Vector ();
		
		sqlStat = dbInsert (TblName,Columns,Values,SelectStatement, -1);
		sql += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		return sql;
	}
	
	public String logResult (DBSQLConnect dbConn, String technique, String parameterStr, String searchStr)
	{
		String sql = "";
		String sqlStat = "";
		String temp;
		
		// Insert information into experimentLog
		Vector Columns = new Vector ();
		Vector TableName = new Vector ();
		Vector Predicate = new Vector ();
		Vector OrderBy = new Vector ();
		Vector GroupBy = new Vector ();
		Vector Having = new Vector ();

		Columns.addElement ("current_timestamp");
		Columns.addElement ("'" + technique + "'");
		Columns.addElement ("avg(elapsed)");
		Columns.addElement ("step");
		Columns.addElement ("count(*)");
		Columns.addElement ("'" + parameterStr + "'");
		
		TableName.addElement (expLogText);
		
		Predicate.addElement ("cast(experimentTS as Date) >= cast('2007-04-08' as Date)");
		Predicate.addElement ("technique like '%" + technique + "%'");
		Predicate.addElement ("parameters like '" + searchStr + "'");
		
		GroupBy.addElement ("step");
		
		String SelectStatement = dbSelect (Columns,TableName,Predicate,OrderBy,GroupBy,Having,-1);
		String TblName = expResultText;
		Columns = new Vector ();
		Vector Values = new Vector ();
		
		sqlStat = dbInsert (TblName,Columns,Values,SelectStatement, -1);
		sql += sqlStat;
		dbConn.sendQuery (sqlStat);
		
		return sql;	
	
	}

        //EDGAR
        //funcion para obtener el select 
        public String dbSelectSize(String tblName, String dimension){
            String sql = "";
            
            sql = "SELECT " + dimension + 
                    " FROM ( SELECT " + dimension + ", count(" + dimension + ") as num FROM "+tblName+" GROUP BY " + dimension + ") A "
                    + " WHERE A.num > 25 ORDER BY " + dimension;
            
            return sql;
        }
}