/*
Class to handle JDBC connections to db on localhost
Based on examples in JDBC chapters in various O'Reilly books
DPL 11.09.14
*/

import java.sql.*;

public class Connect
{
	Connection conn = null;	
	String username = "root";
	String password = "bioinformatics";
	String host = "jdbc:mysql://localhost/chemdat";
	String driver = "org.gjt.mm.mysql.Driver";
	
	ResultSet resSet;
	
	public Connect()
	{
		connect(host);
	}	
		
	private void connect(String url)
	{
		try
		{
			Class.forName("org.gjt.mm.mysql.Driver");	
			conn = DriverManager.getConnection(url, username, password); 
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println(ex.toString());
		}
		catch(SQLException e)
		{
			System.out.println("Trying to connect to MySQL " + e);
		}		
	}
	
	// Allows user to construct prepared query
	public Connection getConnection()
	{
		return conn;	
	}
	
	// Takes PreparedStatement to run query
	public ResultSet runPreparedQuery(PreparedStatement stmt)
	{
		try 
    	{
    		resSet = stmt.executeQuery();					
		}
		catch(SQLException e)
		{
			System.out.println("Trying to make query" + e.toString());
		}
		return resSet;		
	}
	public void close()
	{
		if(conn != null)
		{
			try { conn.close();}
			catch(Exception e){System.out.println("Can't close.");}
		}		
	}

}