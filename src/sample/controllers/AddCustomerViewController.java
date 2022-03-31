package sample.controllers;

import javafx.beans.Observable;
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

public class AddCustomerViewController implements Initializable {

    Stage stage;
    Parent scene;
    private ObservableList<Country> countryList = FXCollections.observableArrayList();
    private ObservableList<Division> divisionList = FXCollections.observableArrayList();
    public int userFK;

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
    void countryComboAction(ActionEvent event){

        try {
            divisionList.clear();
            Country selectedCountry = countryCombo.getSelectionModel().getSelectedItem();
            int divisionFK = selectedCountry.getId();

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

                divisionList.add(di);

            }

            stateCombo.setItems(divisionList);
        }

        catch(SQLException e){

            e.printStackTrace();

        }


    }

    @FXML
    void stateComboAction(ActionEvent event) {

        Division d = stateCombo.getSelectionModel().getSelectedItem();
        userFK = d.getId();

    }




    @FXML
    void cancelBtnPressed(ActionEvent event) throws IOException {
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/CustomersView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    @FXML
    void saveBtnPressed(ActionEvent event) throws SQLException{


        try {
            //take all information from the fields, create a new customer object and add the data to the database
            String name = nameField.getText();
            String address = addressField.getText();
            String postal = postalField.getText();
            String phone = phoneField.getText();
            int divisionID;
            divisionID = stateCombo.getSelectionModel().getSelectedItem().getId();

            if (nameField.getText().equals("") || addressField.getText().equals("") || postalField.getText().equals("") || phoneField.getText().equals("")) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Incorrect data");
                a.setContentText("Either data field(s) are missing data, or have incorrect data types.");
                a.showAndWait();
            }

            else{
            Customer c = new Customer(name, address, postal, phone, divisionID);

            //lambda expression to add customer data to database
            addCustomerInterface a = (cName, cAddress, cPostal, cPhone, cDivisionID) -> {

                Connection c1 = DBConnection.getConnection();

                String addCustomerQuery = "INSERT INTO customers(Customer_Name, Address, Postal_Code, Phone, Division_ID) VALUES (?, ?, ?, ?, ?);";
                DBQuery.setStatement(c1, addCustomerQuery);
                PreparedStatement p = DBQuery.getStatement();

                p.setString(1, name);
                p.setString(2, address);
                p.setString(3, postal);
                p.setString(4, phone);
                p.setInt(5, divisionID);
                p.executeUpdate();

                if (p.getUpdateCount() > 0) {

                    Alert a1 = new Alert(Alert.AlertType.INFORMATION);
                    a1.setTitle("New Customer added to database");
                    a1.setContentText("The new customer has been added to the database");
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
        catch (NumberFormatException e){

            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Incorrect data");
            a.setContentText("Either data field(s) are missing data, or have incorrect data types.");
            a.showAndWait();

        }

        catch (IOException e){}

        catch (NullPointerException e){

            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Incorrect data");
            a.setContentText("Either data field(s) are missing data, or have incorrect data types.");
            a.showAndWait();

        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try{
            /*Connect to the database and put available countries into an observable array list to be displayed
            in a combo box*/
            Connection c = DBConnection.getConnection();

            String getCountryInfo = "SELECT * FROM countries;";
            DBQuery.setStatement(c, getCountryInfo);
            PreparedStatement countryInfoPS = DBQuery.getStatement();
            countryInfoPS.execute();

            ResultSet r = countryInfoPS.getResultSet();

            while(r.next()){

                int id = r.getInt("Country_ID");
                String countryName = r.getString("Country");
                Date createDate = r.getDate("Create_Date");
                String createdBy = r.getString("Created_By");
                Timestamp lastUpdate = r.getTimestamp("Last_Update");
                String lastUpdatedBy = r.getString("Last_Updated_By");



                Country co = new Country(id, countryName, createDate, createdBy, lastUpdate, lastUpdatedBy);

               countryList.add(co);

            }

            countryCombo.setItems(countryList);

        }

        catch (SQLException e){
            e.printStackTrace();
        }

    }
}

