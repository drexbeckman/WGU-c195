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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import sample.models.Appointment;
import sample.models.Customer;
import sample.models.DBConnection;
import sample.models.DBQuery;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppointmentsViewController implements Initializable {

    Stage stage;
    Parent scene;
    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
    private final ZoneId utcZone = ZoneId.of("UTC");
    private final ZoneId userZone = ZoneId.systemDefault();

    @FXML
    private TableView<Appointment> apptTable;

    @FXML
    private TableColumn<Appointment, Integer> apptIDCol;

    @FXML
    private TableColumn<Appointment, String> titleCol;

    @FXML
    private TableColumn<Appointment, String> descCol;

    @FXML
    private TableColumn<Appointment, String> locationCol;

    @FXML
    private TableColumn<Appointment, Integer> contactCol;

    @FXML
    private TableColumn<Appointment, LocalDateTime> startCol;

    @FXML
    private TableColumn<Appointment, LocalDateTime> endCol;

    @FXML
    private TableColumn<Appointment, Integer> custIDCol;

    @FXML
    private TableColumn<Appointment, String> typeCol;

    @FXML
    private ToggleGroup filterBtns;

    @FXML
    private RadioButton allBtn;

    @FXML
    private RadioButton monthBtn;

    @FXML
    private RadioButton weekBtn;

    @FXML
    void monthBtnSelected(ActionEvent event) {
        //Return appointments only within 31 days of the current day (curr)
        LocalDateTime curr = LocalDateTime.now();
        LocalDate currDate = curr.toLocalDate();
        ObservableList<Appointment> monthList = FXCollections.observableArrayList();

        for(int i = 0; i < appointmentsList.size(); i++){
            Appointment a = appointmentsList.get(i);
            LocalDate aDate = a.getStartTime().toLocalDate();

            if(aDate.equals(currDate) || (aDate.isBefore(aDate.plusDays(31)) && aDate.isAfter(currDate))){
                monthList.add(a);
            }
        }

        apptTable.refresh();
        apptTable.setItems(monthList);

    }

    @FXML
    void allBtnSelected(ActionEvent event) {
        //Reset the tableview to include all appointments
        apptTable.refresh();
        apptTable.setItems(appointmentsList);
    }

    @FXML
    void weekBtnSelected(ActionEvent event) {
        //Return an observable array list including only the appointments within one week of the current day (curr)
        ObservableList<Appointment> weekApptList = FXCollections.observableArrayList();
        LocalDateTime curr = LocalDateTime.now();
        LocalDate currDate = curr.toLocalDate();
        for(int i = 0; i < appointmentsList.size(); i++){
            Appointment a = appointmentsList.get(i);
            LocalDate aDate = a.getStartTime().toLocalDate();
            if(aDate.isEqual(currDate)){
                weekApptList.add(a);
            }else if(aDate.isBefore(currDate.plusDays(6)) && aDate.isAfter(currDate)){
                weekApptList.add(a);
            }
        }
        apptTable.refresh();
        apptTable.setItems(weekApptList);

    }




    @FXML
    void addBtnPressed(ActionEvent event) throws IOException{

        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/AddAppointmentView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    @FXML
    void backBtnPressed(ActionEvent event) throws IOException {

        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/MainView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    @FXML
    void deleteBtnPressed(ActionEvent event) throws IOException{
        boolean confirmDelete = false;

        try {
            /*Confirm whether the user wants to delete a customer
            * Connect to database and remove data by Customer_ID
            * Refresh obersavel array list and tableView*/
            Appointment a = apptTable.getSelectionModel().getSelectedItem();

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Are you sure you want to delete the appointment?");
            confirm.setContentText("Do you want to remove the selected appointment?");
            Optional<ButtonType> btn = confirm.showAndWait();
            if (btn.get() == ButtonType.OK) {
                confirmDelete = true;
                try {
                    Connection c = DBConnection.getConnection();
                    String deleteApptQuery = "DELETE FROM appointments WHERE Appointment_ID = ?;";
                    DBQuery.setStatement(c, deleteApptQuery);
                    PreparedStatement deleteApptPS = DBQuery.getStatement();
                    deleteApptPS.setInt(1, a.getId());
                    deleteApptPS.executeUpdate();
                    System.out.println("Appointment successfully removed.");

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                confirmDelete = false;
                confirm.close();
            }

            if(confirmDelete){
                ObservableList<Appointment> selectedAppt;
                ObservableList<Appointment> otherAppts;

                selectedAppt = apptTable.getSelectionModel().getSelectedItems();
                otherAppts = apptTable.getItems();

                for (Appointment a1 : selectedAppt) {
                    otherAppts.remove(a1);
                }

                //Create a log for deleted appointments report
                FileWriter deletedAppts = new FileWriter("deleted_appts.txt", true);
                PrintWriter deletedApptsPW = new PrintWriter(deletedAppts);
                ZoneId utc = ZoneId.of("UTC");
                LocalDateTime deletedTime = LocalDateTime.now(utc);
                deletedApptsPW.println("Appointment " + a.getTitle() + " with ID: "+ a.getId() + " made for Customer_ID: "+ a.getCustomerID() + " deleted " + deletedTime + " (UTC)." + "\n");
                deletedAppts.close();
                System.out.println("deleted appointment log updated");
            }


        }
        catch (NoSuchElementException e){

        }
    }

    @FXML
    void updateBtnPressed(ActionEvent event) throws IOException{
        //Take the selected appointment and pass its Appointment_ID to UpdateAppointmentViewController
        Appointment a = apptTable.getSelectionModel().getSelectedItem();
        int apptID = a.getId();
        UpdateAppointmentViewController.setSelectedApptID(apptID);

        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/UpdateAppointmentView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try{
            //Connect to database and retrieve all appointment data to populate appointments observableList
            Connection c = DBConnection.getConnection();
            String getApptsQuery = "SELECT * FROM appointments;";
            DBQuery.setStatement(c, getApptsQuery);
            PreparedStatement getAppts = DBQuery.getStatement();
            getAppts.execute();
            ResultSet apptsRS = getAppts.getResultSet();

            while(apptsRS.next()){
                int id = apptsRS.getInt("Appointment_ID");
                String title = apptsRS.getString("Title");
                String description = apptsRS.getString("Description");
                String location = apptsRS.getString("Location");
                String type = apptsRS.getString("Type");
                Timestamp startTime = apptsRS.getTimestamp("Start");
                Timestamp endTime = apptsRS.getTimestamp("End");
                LocalDateTime startTimeLT = startTime.toLocalDateTime();
                LocalDateTime endTimeLT = endTime.toLocalDateTime();
                ZonedDateTime startTimeZDT = ZonedDateTime.of(startTimeLT.toLocalDate(), startTimeLT.toLocalTime(), utcZone);
                ZonedDateTime endtimeZDT = ZonedDateTime.of(endTimeLT.toLocalDate(), endTimeLT.toLocalTime(), utcZone);
                ZonedDateTime startTimeToLocal = startTimeZDT.withZoneSameInstant(userZone);
                ZonedDateTime endTimeToLocal = endtimeZDT.withZoneSameInstant(userZone);
                startTimeLT = startTimeToLocal.toLocalDateTime();
                endTimeLT = endTimeToLocal.toLocalDateTime();
                int customerID = apptsRS.getInt("Customer_ID");
                int userID = apptsRS.getInt("User_ID");
                int contactID = apptsRS.getInt("Contact_ID");

                Appointment a = new Appointment(id, title, description, location, type, startTimeLT, endTimeLT, customerID, userID, contactID);

                appointmentsList.add(a);
            }

            //Populate the default table view
            apptTable.setItems(appointmentsList);
            apptIDCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
            locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
            contactCol.setCellValueFactory(new PropertyValueFactory<>("contactID"));
            startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
            endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
            custIDCol.setCellValueFactory(new PropertyValueFactory<>("customerID"));
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }
}

