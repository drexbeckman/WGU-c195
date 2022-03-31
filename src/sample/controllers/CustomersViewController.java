package sample.controllers;

import com.mysql.cj.xdevapi.Warning;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import sample.models.Customer;
import sample.models.DBConnection;
import sample.models.DBQuery;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomersViewController implements Initializable {

    Stage stage;
    Parent scene;
    private ObservableList<Customer> customersList = FXCollections.observableArrayList();
    private static int updateCustID;

    /**
     * Sets the updated customer's ID. Used to pass the customer ID into UpdateCustomerViewController
     * @param updateCustID1 sets updateCustID*/
    public static void setUpdateCustID(int updateCustID1){
        updateCustID = updateCustID1;
    }

    /**
     * Retrieves Customer ID. Used to help pass the customer ID to UpdateCustomerViewController
     * @return updateCustID*/
    public static int getUpdateCustID(){
        return updateCustID;
    }

    @FXML
    private TableView<Customer> customersTable;

    @FXML
    private TableColumn<Customer, Integer> customerIDCol;

    @FXML
    private TableColumn<Customer, String> customerNameCol;

    @FXML
    private TableColumn<Customer, String> customerAddressCol;

    @FXML
    private TableColumn<Customer, String> customerPhoneCol;

    @FXML
    private TableColumn<Customer, String> customerPostalCol;

    @FXML
    private TableColumn<Customer, Integer> customerDivisionIDCol;

    @FXML
    void addBtnPressed(ActionEvent event) throws IOException {
        //Change view to AddCustomerView
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/AddCustomerView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    @FXML
    void backBtnPressed(ActionEvent event) throws IOException{
        //Return to MainView
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/MainView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();
    }

    @FXML
    void deleteBtnPressed(ActionEvent event) throws IOException{
        //Take a selected customer, connect to database, and delete from array list and database.
        Customer deleteCust = customersTable.getSelectionModel().getSelectedItem();
        boolean confirmDelete = false;

        try{
            /*Connect to the database Appointments table, and check for any corresponding appointments
            * to satisfy referential integrity issues*/
            Connection checkAppts = DBConnection.getConnection();
            String getApptsQuery = "SELECT * FROM appointments WHERE Customer_ID = ?;";
            DBQuery.setStatement(checkAppts, getApptsQuery);
            PreparedStatement apptsPS = DBQuery.getStatement();
            apptsPS.setInt(1, deleteCust.getId());
            apptsPS.execute();

            ResultSet apptRS = apptsPS.getResultSet();
            int i = 0;
            while(apptRS.next()){
                i++;
            }

            //Ask user for confirmation if there are any existing appointments associated with the selected customer
            if(i > 0){
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setTitle("Delete Customer and appointments?");
                a.setContentText("Deleting this customer will also remove " + i +" appointments" +
                        "associated with it.");
                Optional<ButtonType> confirm = a.showAndWait();

                if(confirm.get() == ButtonType.OK){

                    //Delete associated Appointments from database
                    Connection deleteAppts = DBConnection.getConnection();
                    String deleteApptsQuery = "DELETE FROM appointments WHERE Customer_ID = ?;";
                    DBQuery.setStatement(deleteAppts, deleteApptsQuery);
                    PreparedStatement deleteApptsPS = DBQuery.getStatement();
                    deleteApptsPS.setInt(1, deleteCust.getId());
                    deleteApptsPS.executeUpdate();
                    System.out.println("Associated appointments successfully deleted.");

                    //Delete the selected customer
                    Connection c = DBConnection.getConnection();
                    String deleteCustomerQuery = "DELETE FROM customers WHERE Customer_ID = ?;";
                    DBQuery.setStatement(c, deleteCustomerQuery);
                    PreparedStatement p = DBQuery.getStatement();
                    p.setInt(1, deleteCust.getId());
                    p.executeUpdate();
                    System.out.println("Customer successfully deleted");
                    confirmDelete = true;
                }

            }
            else{
                //Confirm whether to delete customer to user in case where there are no associated appts.
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setTitle("Delete customer?");
                a.setContentText("Are you sure you want to delete this customer?");
                Optional<ButtonType> confirm = a.showAndWait();
                if(confirm.get() == ButtonType.OK){
                    //Connect to database and delete selected customer
                    Connection c = DBConnection.getConnection();
                    String deleteCustomerQuery = "DELETE FROM customers WHERE Customer_ID = ?;";
                    DBQuery.setStatement(c, deleteCustomerQuery);
                    PreparedStatement p = DBQuery.getStatement();
                    p.setInt(1, deleteCust.getId());
                    p.executeUpdate();
                    System.out.println("Customer successfully deleted");
                    confirmDelete = true;
                }
                else{
                    a.close();
                    confirmDelete = false;
                }
            }
        }
        catch(SQLException e){

            e.printStackTrace();

        }
        catch(NoSuchElementException e){}


        ObservableList<Customer> selectedCust;
        ObservableList<Customer> otherCustomers;

        otherCustomers = customersTable.getItems();
        selectedCust = customersTable.getSelectionModel().getSelectedItems();

        //Update the table view through an observable array list (otherCustomers)
        if(confirmDelete){
        for(Customer c : selectedCust){
            otherCustomers.remove(c);
        }
        }
    }

    @FXML
    void updateBtnPressed(ActionEvent event) throws IOException, SQLException{

        try{

        //Take selected customer, connect to database, and retrieve Customer_ID
        Customer updateCust = customersTable.getSelectionModel().getSelectedItem();

        Connection c = DBConnection.getConnection();
        String getCustID = "SELECT Customer_ID FROM customers WHERE Customer_Name = ?;";
        DBQuery.setStatement(c, getCustID);
        PreparedStatement p = DBQuery.getStatement();
        p.setString(1, updateCust.getName());
        p.execute();

        ResultSet r = p.getResultSet();

        while(r.next()) {
            setUpdateCustID(r.getInt("Customer_ID"));
        }
            //Call the setSelectedCustomerID from UpdateCustomerView to pass data from database from correct Customer
            UpdateCustomerViewController.setSelectedCustomerID(getUpdateCustID());

            stage = (Stage)((Button) event.getSource()).getScene().getWindow();
            scene = FXMLLoader.load(getClass().getResource("../views/UpdateCustomerView.fxml"));
            stage.setScene(new Scene(scene));
            stage.show();

        }
        catch(NullPointerException e){

            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("No customer was selected");
            a.setContentText("A customer needs to be selected first to update.");
            a.showAndWait();

        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        try{

        //Display all customers in the database
        Connection c = DBConnection.getConnection();

        String getCustomersQuery = "SELECT * FROM customers;";
        DBQuery.setStatement(c, getCustomersQuery);
        PreparedStatement p = DBQuery.getStatement();

        p.execute();

        ResultSet customerRS = p.getResultSet();

        while(customerRS.next()){
            //Populate customerList and Customer tableView
            int customerID = customerRS.getInt("Customer_ID");
            String customerName = customerRS.getString("Customer_Name");
            String customerAddress = customerRS.getString("Address");
            String customerPostal = customerRS.getString("Postal_Code");
            String customerPhone = customerRS.getString("Phone");
            int customerDivisionID = customerRS.getInt("Division_ID");

            Customer cu = new Customer(customerID, customerName, customerAddress, customerPostal, customerPhone, customerDivisionID);

            customersList.add(cu);

        }

        customersTable.setItems(customersList);
        customerIDCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        customerAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        customerPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        customerPostalCol.setCellValueFactory(new PropertyValueFactory<>("postal"));
        customerDivisionIDCol.setCellValueFactory(new PropertyValueFactory<>("divisionID"));


        }
        catch(SQLException e){

            e.printStackTrace();

        }

    }
}

