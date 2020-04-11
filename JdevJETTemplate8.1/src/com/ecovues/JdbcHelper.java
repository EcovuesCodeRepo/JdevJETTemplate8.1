package com.ecovues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.json.JSONArray;
import org.json.JSONObject;

public class JdbcHelper {
          public static String dbLink = "@ecoui2img";//Ecovues instance
         //public static String dbLink = "@axf2img";//UL instance
	/*
	private static String _userName, _passWord, _host, _database, _dbPort;
    
    static {
        //setProxy("www-proxy.us.oracle.com", "80");
         _userName = "PREDICT";
         _passWord = "PREDICT";
         _host = "rws3270162.us.oracle.com";//"den02bpc.us.oracle.com";
         _database = "scem1226";//"PDB1.us.oracle.com"; //service name
         _dbPort = "1521";
    }
    
    public static synchronized void setProxy(String proxyHost, String proxyPort){
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);
    }

    public static synchronized Connection getJDBCConnection(boolean autoCommit){
        String connectionURL = "jdbc:oracle:thin:@//" + _host + ":" + _dbPort + "/" + _database;
        Connection conn = null;
        
        try {
           Class.forName("oracle.jdbc.driver.OracleDriver");
           conn = DriverManager.getConnection( connectionURL, _userName, _passWord);
           conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
           System.out.println("getJDBCConnection: Error creating DB connection");
           e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to register Oracle driver");
            e.printStackTrace();
        }
        return conn;
    }
    */
    
    public static synchronized Connection getJDBCConnectionFromDataSource( boolean autoCommit){
        Connection conn = null;
      //  String dbJNDI = "predict226";
        javax.naming.Context ctx;
        try {
            ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/ECOIMG");
            conn = ds.getConnection();
        } catch (NamingException e) {
        	System.out.println("getJDBCConnectionFromDataSource: NamingException while creating DB connection: " + e);
            e.printStackTrace();
        } catch (SQLException e) {
        	System.out.println("getJDBCConnectionFromDataSource: SQLException while creating DB connection: " + e);
            e.printStackTrace();
        } catch (Exception e) {
        	System.out.println("getJDBCConnectionFromDataSource: Exception while creating DB connection: " + e);
            e.printStackTrace();
        }
        
        return conn;
    }
    
    public static synchronized Connection getEcouiJDBCConn( boolean autoCommit){
        Connection conn = null;
      //  String dbJNDI = "predict226"; 
        javax.naming.Context ctx;
        try {
            ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/ECOUI");
            conn = ds.getConnection();
        } catch (NamingException e) {
                System.out.println("getJDBCConnectionFromDataSource: NamingException while creating DB connection: " + e);
            e.printStackTrace();
        } catch (SQLException e) {
                System.out.println("getJDBCConnectionFromDataSource: SQLException while creating DB connection: " + e);
            e.printStackTrace();
        } catch (Exception e) {
                System.out.println("getJDBCConnectionFromDataSource: Exception while creating DB connection: " + e);
            e.printStackTrace();
        }
        
        return conn;
    }
    public static synchronized void closeJDBCConnection(Connection conn, boolean shouldCommit) {
        if(conn != null) {
            try {
                if(shouldCommit)
                    conn.commit();
                } catch (SQLException e) {
                    System.out.println("closeJDBCConnection: Error committing on JDBC Connection");
                    e.printStackTrace();
                }
            closeJDBCConnection(conn);
        }
        else
            System.out.println("NULL Connection object passed");
    }

    public static synchronized void closeJDBCConnection(Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("closeJDBCConnection: Error closing JDBC Connection");
                e.printStackTrace();
            }
        }
        else
            System.out.println("NULL Connection object passed");
    }

    public static synchronized PreparedStatement getPreparedStatement(Connection conn , String query) {
       PreparedStatement preparedStatement=null;
       try{
           preparedStatement= conn.prepareStatement(query);
       }
       catch(Exception e) {
           e.printStackTrace();
       }
       return preparedStatement;
    }
    public static JSONObject convertToJSON(ResultSet resultSet)
            throws Exception {
        JSONArray jsonArray = new JSONArray();
        JSONObject finalobj = new JSONObject();
        while (resultSet.next()) {
            int total_rows = resultSet.getMetaData().getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 0; i < total_rows; i++) {
                
              //  System.out.println("Label: "+resultSet.getMetaData().getColumnLabel(i + 1)
               //         .toLowerCase()+" : " + resultSet.getObject(i + 1));
               
               
               if(resultSet.getObject(i + 1)==null)
               {  
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), "");
               }
                else {
                   obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
                           .toLowerCase(), resultSet.getObject(i + 1));
               }
                
            }
            jsonArray.put(obj);
        }
        return finalobj.put("items",jsonArray);
    }
    
    /**
     * Fetches a new Id from the sequence specified
     * @param dbSequenceName
     * @return
     */
    public static synchronized long getNextId(String dbSequenceName, Connection conn) {
        Statement stmt = null;
        Long seqNum = -1L;
        try {
             stmt = conn.createStatement();
             ResultSet rs =  stmt.executeQuery("SELECT " + dbSequenceName + ".NEXTVAL FROM DUAL");                
             if(rs.next())
                 seqNum = rs.getLong(1);
        } catch (SQLException e) {
             System.out.println(e);
             e.printStackTrace();
        } finally{
        }
        return seqNum;
    }
    
    public static synchronized void setColumn2Stmt(PreparedStatement pstmt, int index, String value) throws SQLException{
    	if(value!=null)
        	pstmt.setString(index, value);
        else
        	pstmt.setNull(index, Types.NVARCHAR);
    }
    public static synchronized void setColumn2Stmt(PreparedStatement pstmt, int index, Long value) throws SQLException{
    	if(value!=null && value!=0L)
        	pstmt.setLong(index, value);
        else
        	pstmt.setNull(index, Types.NUMERIC);
    }
    
   public static void main(String[] args) {
   }

}


