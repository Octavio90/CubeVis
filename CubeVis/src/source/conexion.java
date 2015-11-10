package source;

import java.sql.*;

public class conexion{
	public static void main(String[] args)
	{
		String driver = "org.postgresql.Driver";
		String connectString = "jdbc:postgresql://localhost:5432/med655";
		String user = "postgres";
		String password = "admin";
		
		try{
			Class.forName(driver);
			Connection con = DriverManager.getConnection(connectString, user , password);
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT oldyn FROM zmed655");
			
			while (rs.next()){
			System.out.println("oldyn: " + rs.getString("oldyn"));
			}
			
			stmt.close();
			con.close();			
		}catch ( Exception e ){
		System.out.println(e.getMessage());
		}
	}
}