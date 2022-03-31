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
import javafx.stage.Stage;
import sample.models.Appointment;
import sample.models.DBConnection;
import sample.models.DBQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    Stage stage;
    Parent scene;

    @FXML
    void apptsBtnPressed(ActionEvent event) throws IOException{
        //Chage to Appointments view
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/AppointmentsView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    @FXML
    void customersBtnPressed(ActionEvent event) throws IOException{
        //Change to customers view
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/CustomersView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    @FXML
    void reportsBtnPressed(ActionEvent event) throws IOException{
        //Change to reports view
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/ReportsView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();
    }

    @FXML
    void exitBtnPressed(ActionEvent event) throws IOException {
        //Go back to login
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/LogInView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    /**Method to create an alarm if there exists an appointment within 15 minutes of user's log in time*/
    public void appointmentAlert(){
        LocalDateTime currTime = LocalDateTime.now();
        ObservableList<LocalDateTime> apptTimesList = FXCollections.observableArrayList();
        ObservableList<Appointment> apptsList = FXCollections.observableArrayList();

        try{
            Connection c = DBConnection.getConnection();
            String getApptDataQuery = "SELECT * FROM appointments;";
            DBQuery.setStatement(c, getApptDataQuery);
            PreparedStatement p = DBQuery.getStatement();
            p.execute();

            ResultSet r = p.getResultSet();

            while(r.next()){
                //get all data from database
                int id = r.getInt("Appointment_ID");
                String title = r.getString("Title");
                String description = r.getString("Description");
                String location = r.getString("Location");
                String type = r.getString("Type");
                Timestamp start = r.getTimestamp("Start");
                Timestamp end = r.getTimestamp("End");
                int custID = r.getInt("Customer_ID");
                int userID = r.getInt("User_ID");
                int contactID = r.getInt("Contact_ID");

                //Convert Timestamp objects to LocalDateTime and convert Time zone
                LocalDateTime startDate = start.toLocalDateTime();
                LocalDate startDateLD = startDate.toLocalDate();
                LocalTime startDateLT = startDate.toLocalTime();
                ZonedDateTime startDateZoned = ZonedDateTime.of(startDateLD, startDateLT, ZoneId.of("UTC"));
                startDateZoned = startDateZoned.withZoneSameInstant(ZoneId.systemDefault());
                startDate = startDateZoned.toLocalDateTime();

                LocalDateTime endDate = end.toLocalDateTime();
                LocalDate endDateLD = endDate.toLocalDate();
                LocalTime endDateLT = endDate.toLocalTime();
                ZonedDateTime endDateZoned = ZonedDateTime.of(endDateLD, endDateLT, ZoneId.of("UTC"));
                endDateZoned = endDateZoned.withZoneSameInstant(ZoneId.systemDefault());
                endDate = endDateZoned.toLocalDateTime();

                //Create Appointment object to add to the ObservableList
                Appointment a = new Appointment(id, title, description, location, type, startDate, endDate, custID, userID, contactID);
                apptsList.add(a);
                apptTimesList.add(startDate);

            }

            //Search through apptTimesList to determine whether a time exists within 15 minutes of login
            for(int i = 0; i < apptTimesList.size(); i++){

                if((apptTimesList.get(i).isAfter(currTime) && apptTimesList.get(i).isBefore(currTime.plusMinutes(16)) || apptTimesList.get(i).equals(currTime))){
                    //Grab corresponding appointment
                    Appointment a = apptsList.get(i);

                    //create an alert for the appointment
                    /*Because the times are in 30 minute intervals, and because there cannot be any duplicate start
                    times, there will only ever be one possible alert generated*/

                    Alert a1 = new Alert(Alert.AlertType.INFORMATION);
                    a1.setTitle("Alert within 15 minutes");
                    a1.setContentText(a + " begins at: " + apptTimesList.get(i));
                    a1.showAndWait();

                }
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Initialize the 15 minute appointmentAlert function
        appointmentAlert();

    }
}
