package sample.models;

import java.io.IOException;
import java.sql.SQLException;

public interface addCustomerInterface {

    /**Interface that adds customer information into the database's customers table
     * @throws  SQLException
     * @param name sets customer name in database
     * @param address sets customer address in database
     * @param postal sets customer postal code in database
     * @param phone sets customer phone number in database
     * @param divisionID sets division ID based on selected country/division (not required in Customer constructor) to the database*/
    void addCustomer(String name, String address, String postal, String phone, int divisionID) throws SQLException, IOException;

}
