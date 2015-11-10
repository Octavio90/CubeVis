/*
=========================================
DBSQLConnect.java
Provides helper functions to connect to, query, and retrieve from dbms
Zhibo Chen
Started: February 20, 2007
Revised:  (Revisions for EACH date are listed in order of importance)
2007-03-24	1) Changed name to DBSQLConnect
=========================================
*/
package source;

import java.io.*;
import java.util.*;
import java.sql.*;

public class DBSQLConnect {

	boolean debug = true;
	boolean showqueries = true;
	boolean connect = true;
	boolean printFlag = true;
	
	int DBMS;
	
	Statement stmt = null;
	ResultSet rs = null;
	Connection conn = null;
	
	String database = "";
	String username = "";
	String password = "";
	String url = "";

        //FUNCION:Muesta el mensaje para conectarse a la base y llama función de conexión
	public DBSQLConnect (String dbmsType, boolean connectFlag, boolean print)
	{

		connect = connectFlag;
		printFlag = print;

		database = dbmsType;

                // NOTA: recibe un número y lo quiere comparar con una cadena
		if (dbmsType.equalsIgnoreCase ("postgresql") ||
		    dbmsType.equalsIgnoreCase ("postgresql1") ||
		    dbmsType.equalsIgnoreCase ("postgresql2") ||
			dbmsType.equalsIgnoreCase ("postgresql3") )
				DBMS = 1;

		if (dbmsType.equalsIgnoreCase ("sqlserver") ||
		    dbmsType.equalsIgnoreCase ("sqlserver1") ||
		    dbmsType.equalsIgnoreCase ("sqlserver2") ||
			dbmsType.equalsIgnoreCase ("sqlserver3") )
				DBMS = 2;
				
		System.out.println("Start Connect");
		switch (DBMS)
		{
			case 1:
				//if (printFlag) System.out.println ("Using Teradata JDBC...Ready");
                                if (printFlag) System.out.println ("Using PostgreSQL...Ready");
				break;
			case 2:
				if (printFlag) System.out.println ("Using SQL Server 2005...Ready");
				break;
			default:
				if (printFlag) System.out.println ("Unknown dbmsType");
				System.exit (1);
				break;
		}
		
		getLoginInformation ();
	}

        //FUNCION: Lee el archivo dbLogin.ini para obtener los datos de conexión
	private void getLoginInformation ()
	{
		boolean flag = false;
	
		try {
                        //EDGAR NOTA:Ruta para el archivo con información de conexión
			FileReader input = new FileReader("/home/scidb/CubeVis-Files/dbLogin.ini");
			BufferedReader bufRead = new BufferedReader(input);
			String line;    // String that holds current file line
			int count = 0;  // Line number of count 
			
			// Read first line
			line = bufRead.readLine();
			count++;
			
			String[] str;
			
			// Read through file one line at time. Print line # and line
			while (line != null){
				if (line.indexOf ("=") >=0)
				{
					str = line.split ("=");
					if (str[0].equals ("database"))
					{

                                            System.out.println("Found database: " + str[1] + " compare with " + database);
						if (str[1].equals (database))
							flag = true;
						else
							flag = false;
					}
					else if (flag && str[0].equals ("username"))
					{
						username = str[1];
						for (int i = 2; i < str.length; i++)
							username += "=" + str[i];
//						username = str[1];
					}
					else if (flag && str[0].equals ("password"))
					{
						password = str[1];
						for (int i = 2; i < str.length; i++)
							password += "=" + str[i];
//						password = str[1];
					}
					else if (flag && str[0].equals ("url"))
					{
						url = str[1];
						for (int i = 2; i < str.length; i++)
							url += "=" + str[i];
//						url = str[1];
					}
				}
				line = bufRead.readLine();
				count++;
			}
			
			bufRead.close();
		}
		catch (Exception e) { System.out.println ("ERROR: " + e);}
		
/*		System.out.println ("Database = " + database);
		System.out.println ("Username = " + username);
		System.out.println ("Password = " + password);
		System.out.println ("Url      = " + url);
*/
	}

        //FUNCION: Establecec la conexión
	public String startConnection ()
	{
		switch (DBMS)
		{
			/*case 1:
				try {
					Class.forName ("com.ncr.teradata.TeraDriver");
					if (printFlag) System.out.print ("Starting database connection to " + database + ": ");
					conn = DriverManager.getConnection(url,username,password);
					if (printFlag) System.out.println ("SUCCESS");
					stmt = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				} catch (Exception e) {
					if (debug) System.out.println ("ERROR: " + e);
					return "Unable to get driver: " + e;
				}
				break;*/
			case 1:	//PostgreSQL
				try {
                                        Class.forName ("org.postgresql.Driver");
					if (printFlag) System.out.print ("Starting database connection to " + database + ": ");

					//conn = DriverManager.getConnection(url,username,password);
                                        //EDGAR
                                        conn = DriverManager.getConnection(url);

					if (conn == null)
						System.out.println ("Cannot Connect");
					if (printFlag) System.out.println ("SUCCESS");
					stmt = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				} catch (Exception e) {
					e.printStackTrace();
					if (debug) System.out.println ("ERROR: " + e);
					System.out.println(url);
					System.out.println(username);
					System.out.println(password);
					return "Unable to get driver: " + e;
				}
				break;
                           case 2:	//SQL Server
				try {
					Class.forName ("com.microsoft.sqlserver.jdbc.SQLServerDriver");
					if (printFlag) System.out.print ("Starting database connection to " + database + ": ");

					//conn = DriverManager.getConnection(url,username,password);
                                        //EDGAR
                                        conn = DriverManager.getConnection(url);

					if (conn == null)
						System.out.println ("Cannot Connect");
					if (printFlag) System.out.println ("SUCCESS");
					stmt = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				} catch (Exception e) {
					e.printStackTrace();
					if (debug) System.out.println ("ERROR: " + e);
					System.out.println(url);
					System.out.println(username);
					System.out.println(password);
					return "Unable to get driver: " + e;
				}
				break;
		}
		return "";
	}
	
	public boolean sendQuery (String query)
	{
		if (conn == null)
			return false;
			
		if (!connect) return false;
		
		if (showqueries)
			System.out.println ("QUERY:  " + query);
		
		switch (DBMS)
		{
			case 1:
				try {
					rs = stmt.executeQuery (query);
				} catch (SQLException e) {
					if (debug) System.out.println ("ERROR: " + e);
					return false;
				}
				break;
			case 2:
				try {
					stmt.execute (query);
					rs = stmt.getResultSet ();
				} catch (SQLException e) {
					if (debug) 
					{
						System.out.println ("ERROR: " + e);
						System.out.println (query);
					}
					return false;
				}
				break;
		}
		
		return true;
	}

        //FUNCION: Revisa si la consulta generó un resultado
	public boolean solutionExists ()
	{
		if (rs == null) return false;
		
		try {
		if (rs.first ())
		{
			rs.beforeFirst ();
			return true;
		}
		} catch (Exception e) { if (debug) System.out.println ("EXCEPTION: " + e); }
		return false;
	}
	
	public int getColumnCount ()
	{
		int cnt = 0;
		try {
			cnt = (rs.getMetaData ()).getColumnCount ();
		}
		catch (Exception e) { System.out.println ("SQL ERROR: " + e); }
		return cnt;
	}

        //FUNCION: Devuelve el nombre de la columna
	public String[] getResult (String[] colNames)
	{
		String[] results = new String[0];

		if (rs == null)
		{
			if (debug) System.out.println ("ResultSet is null");
			return results;
		}
	
		switch (DBMS)
		{
			case 1:
			case 2:
				try
				{
					if (rs.next ())
					{
						results = new String[colNames.length];
						for (int i = 0; i < colNames.length; i++)
							results[i] = rs.getString (colNames[i]);
					}
					else
					{
						if (debug) System.out.println ("No more rows");
						return results;
					}
				} catch (SQLException e) {
					if (debug) System.out.println ("ERROR: " + e);
				}
		}

		return results;
	}

        //FUNCION: Detiene la conexión a la base
	public String stopConnection ()
	{
		if (conn == null)
			return "Database not connected";

		if (printFlag) System.out.print ("Stopping database connect: ");
		
		switch (DBMS)
		{
			case 1:
			case 2:
				if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { }

				rs = null;
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException sqlEx) { }

					stmt = null;
				}
		
				try {
					conn.close ();
				} catch (SQLException e) { };
		}

		if (printFlag) System.out.println ("SUCCESS");
		
		return "";
	}
	
	
	
	public static void main (String[] args)
	{
		DBSQLConnect DC = new DBSQLConnect (args[0], true, true);

		DC.startConnection ();

		DC.sendQuery ("DROP TABLE temp");
		DC.sendQuery ("CREATE TABLE temp (a int, b int)");
		DC.sendQuery ("INSERT INTO temp VALUES (1,1)");
		DC.sendQuery ("INSERT INTO temp VALUES (2,4)");
		DC.sendQuery ("INSERT INTO temp VALUES (3,9");
		DC.sendQuery ("INSERT INTO temp VALUES (4,16)");
		DC.sendQuery ("INSERT INTO temp VALUES (5,25)");
		
		
		DC.sendQuery ("SELECT * FROM temp");

		System.out.println ("Number of Columns: " + DC.getColumnCount ());
		String[] cols = new String[2];
		cols[0] = "a";
		cols[1] = "b";
		String[] res = DC.getResult(cols);
		
		System.out.println ("a    b");
		System.out.println ("------");
		
		while (res.length > 0)
		{
			System.out.println (res[0] + "   " + res[1]);
			res = DC.getResult(cols);
		}
		
		DC.sendQuery ("DROP TABLE temp");
		
		DC.stopConnection ();

	}
}