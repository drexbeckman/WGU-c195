package sample.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBQuery {

    private static PreparedStatement statement;

    /**Sets the prepared statement. Used to update, delete, or select statements from teh database
     * @param c
     * @param s
     * @throws SQLException*/
    public static void setStatement(Connection c, String s) throws SQLException {

        statement = c.prepareStatement(s);

    }

    /**Returns prepared statement
     * @return  statement*/
    public static PreparedStatement getStatement(){

        return statement;

    }

}
