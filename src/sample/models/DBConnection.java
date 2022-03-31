package sample.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    //Strings concatenated into a jdbcURL to connect to the SQL database
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String ipAddress = "//wgudb.ucertify.com/WJ06AwB";
    private static final String jbdcURL = protocol + vendor + ipAddress;
    private static final String mySQLDriver = "com.mysql.jdbc.Driver";

    private static final String username = "U06AwB";
    private static final String password = "53688708556";

    private static Connection conn = null;

    /**startConnection() function called and connects to database through JDBC URL and provided
     *  username and password
     *  @return conn*/
    public static Connection startConnection(){

        try{

            Class.forName(mySQLDriver);
            conn = DriverManager.getConnection(jbdcURL, username, password);
            System.out.println("Connected successfully");

        }
        catch (ClassNotFoundException e){

            e.printStackTrace();

        }

        catch (SQLException e){

            e.printStackTrace();

        }

        return conn;

    }

    /**getConnection method to return Connection
     * @return conn*/
    public static Connection getConnection(){

        return conn;

    }

    /**Closes connection
     * @throws SQLException*/
    public static void closeConnection(){
        try{
        conn.close();
        System.out.println("Connection closed");
        }

        catch(SQLException e){

            System.out.println(e.getMessage());

        }

    }
}
