package sample.models;

import java.sql.SQLException;

public interface LogInInterface {

    /**Lambda expression in LogInInterface used in LogInviewController to confirm
     * whether username and password correspond to an existing row in the
     * users table in the database
     *
     * @param user
     * @param password
     * @throws SQLException*/
    boolean verifyUserPassword(String user, String password) throws SQLException;
}
