package sample.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.models.*;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UpdateCustomerViewController implements Initializable {

    Stage stage;
    Parent scene;
    private static int selectedCustomerID;
    private ObservableList<Country> countryList = FXCollections.observableArrayList();
    private ObservableList<Division> divisionList = FXCollections.observableArrayList();

    /**Sets the customer ID to pass data from main customer view into UpDateCustomerView
     * @param i integer corresponding to database's Customer_ID*/
    public static void setSelectedCustomerID(int i){
        selectedCustomerID = i;
    }

    /**Returns value of selected customer ID passed from main customer view
     * @return selectedCustomerID Returns integer value corresponding to the Customer_ID in the database*/
    public static int getSelectedCustomerID(){
        return selectedCustomerID;
    }

    @FXML
    private TextField idField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField addressField;

    @FXML
    private ComboBox<Division> stateCombo;

    @FXML
    private ComboBox<Country> countryCombo;

    @FXML
    private TextField postalField;

    @FXML
    private TextField phoneField;

    @FXML
    void cancelBtnPressed(ActionEvent event) throws IOException {

        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/CustomersView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    @FXML
    void countryComboAction(ActionEvent event) {
        try {
            //clear the division list
            divisionList.clear();
            //retrieve the selected country, and its corresponding ID
            Country selectedCountry = countryCombo.getSelectionModel().getSelectedItem();
            int divisionFK = selectedCountry.getId();

            //Get states/provinces associated with the country ID foreign key
            Connection c = DBConnection.getConnection();
            String getDivisionInfo = "SELECT * FROM first_level_divisions WHERE COUNTRY_ID = ?;";
            DBQuery.setStatement(c, getDivisionInfo);
            PreparedStatement divisionInfoPS = DBQuery.getStatement();

            divisionInfoPS.setInt(1, divisionFK);

            divisionInfoPS.execute();

            ResultSet r = divisionInfoPS.getResultSet();

            while (r.next()) {

                int id = r.getInt("Division_ID");
                String name = r.getString("Division");
                Date createDate = r.getDate("Create_Date");
                String createdBy = r.getString("Created_By");
                Timestamp lastUpdate = r.getTimestamp("Last_Update");
                String lastUpdateBy = r.getString("Last_Updated_By");
                int countryID = r.getInt("COUNTRY_ID");

                Division di = new Division(id, name, createDate, createdBy, lastUpdate, lastUpdateBy, countryID);
                //Populate the division list based in selected country
                divisionList.add(di);

            }
            //Populate the combobox with the division observable array list
            stateCombo.setItems(divisionList);
        }

        catch(SQLException e){

            e.printStackTrace();

        }
    }

    @FXML
    void saveBtnPressed(ActionEvent event) {

        try{
            //Get data from interface fields
            String name = nameField.getText();
            String address = addressField.getText();
            String postal = postalField.getText();
            String phone = phoneField.getText();
            int divisionID;
            divisionID = stateCombo.getSelectionModel().getSelectedItem().getId();
            //Throw an alert in case a value was left blank
            if (nameField.getText().equals("") || addressField.getText().equals("") || postalField.getText().equals("") || phoneField.getText().equals("")) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Incorrect data");
                a.setContentText("Either data field(s) are missing data, or have incorrect data types.");
                a.showAndWait();
            }

            else {
                //If all fields are filled out, create a new customer object.

                Customer c = new Customer(name, address, postal, phone, divisionID);

                //lambda expression to update customer data to database
                addCustomerInterface a = (cName, cAddress, cPostal, cPhone, cDivisionID) -> {

                    Connection c1 = DBConnection.getConnection();

                    String addCustomerQuery = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, Division_ID = ? WHERE Customer_ID = ?";
                    DBQuery.setStatement(c1, addCustomerQuery);
                    PreparedStatement p = DBQuery.getStatement();
                    //set the date and add update the customer table
                    p.setString(1, name);
                    p.setString(2, address);
                    p.setString(3, postal);
                    p.setString(4, phone);
                    p.setInt(5, divisionID);
                    p.setInt(6, getSelectedCustomerID());
                    p.executeUpdate();

                    if (p.getUpdateCount() > 0) {
                        //Alert the user that the customer has been updated
                        Alert a1 = new Alert(Alert.AlertType.INFORMATION);
                        a1.setTitle("Customer data updated");
                        a1.setContentText("The new data has been uploaded to the database");
                        a1.showAndWait();

                        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                        scene = FXMLLoader.load(getClass().getResource("../views/CustomersView.fxml"));
                        stage.setScene(new Scene(scene));
                        stage.show();

                    }


                };
                //Call the lambda expression method
                a.addCustomer(name, address, postal, phone, divisionID);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        catch (IOException e){
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Incorrect data");
            a.setContentText("Either data field(s) are missing data, or have incorrect data types.");
            a.showAndWait();
        }

        catch(NullPointerException e){

            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Incorrect data");
            a.setContentText("Either data field(s) are missing data, or have incorrect data types.");
            a.showAndWait();

        }
    }



    @FXML
    void stateComboAction(ActionEvent event) {

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

       try{
        /*Connect to the database and get Customer's corresponding values based on the customer ID
        * retrieved by the getSelectedCustomer() method  */
        Connection c = DBConnection.getConnection();
        String getCustomerDataQuery = "SELECT * FROM customers WHERE Customer_ID = ?;";

        DBQuery.setStatement(c, getCustomerDataQuery);
        PreparedStatement p = DBQuery.getStatement();
        p.setInt(1, getSelectedCustomerID());
        p.execute();

        ResultSet r = p.getResultSet();

        while(r.next()){
            //Retrieve the data
           String custName = r.getString("Customer_Name");
           String custAddress = r.getString("Address");
           String custPostal = r.getString("Postal_Code");
           String custPhone = r.getString("Phone");
           int customerDivisionID = r.getInt("Division_ID");

            nameField.setText(custName);
            addressField.setText(custAddress);
            postalField.setText(custPostal);
            phoneField.setText(custPhone);

        }
        //Retrieve country data
        Connection c1 = DBConnection.getConnection();
        String getCountriesQuery = "SELECT * FROM countries;";
        DBQuery.setStatement(c1, getCountriesQuery);
        PreparedStatement p1 = DBQuery.getStatement();
        p1.execute();

        ResultSet r1  = p1.getResultSet();

        while(r1.next()){

            int id = r1.getInt("Country_ID");
            String name = r1.getString("Country");
            Country co = new Country(id, name);
            //Add country objects to the observable array list
            countryList.add(co);

        }
        //Add the observable array list to the combo box
        countryCombo.setItems(countryList);


        }
        catch (SQLException e){
            e.printStackTrace();
        }

    }
}


