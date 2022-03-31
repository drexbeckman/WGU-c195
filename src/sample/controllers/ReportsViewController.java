package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import sample.models.DBConnection;
import sample.models.DBQuery;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ReportsViewController {

    Stage stage;
    Parent scene;

    @FXML
    private TextArea reportField;


    @FXML
    private ToggleGroup reportsToggle;

    @FXML
    void apptsCancelled(ActionEvent event) throws FileNotFoundException {
        //Reset reportField
        reportField.clear();

        //Read from deleted_appts file. (Custom report from rubric)
        File apptFile = new File("deleted_appts.txt");
        Scanner s = new Scanner(apptFile);

        while(s.hasNext()){
            reportField.appendText(s.nextLine() + "\n");
        }
        s.close();

    }

    @FXML
    void byTypeMonth(ActionEvent event) {
        //Reset the report field textArea
        reportField.clear();

        try{
            Connection c = DBConnection.getConnection();
            //Retrieve the type of appointments and number of associated appts.
            String getTypeDataQuery = "SELECT Type, COUNT(*) FROM appointments GROUP BY Type;";
            DBQuery.setStatement(c, getTypeDataQuery);
            PreparedStatement p = DBQuery.getStatement();
            p.execute();

            ResultSet r = p.getResultSet();
            while(r.next()){
                //create a string to write to reportField
                String type = "   Meeting type: " + r.getString("Type");
                Integer count = r.getInt("COUNT(*)");
                String countString = "   Count: " + count.toString() + "\n";

                reportField.appendText(type + countString);
            }

            Connection c1 = DBConnection.getConnection();
            //Reconnect and return query looking for number appts by month
            String getMonthDataQuery = "SELECT MONTH(Start), COUNT(*) FROM appointments GROUP BY MONTH(Start);";
            DBQuery.setStatement(c1, getMonthDataQuery);
            PreparedStatement p1 = DBQuery.getStatement();
            p1.execute();

            ResultSet r1 = p1.getResultSet();

            while(r1.next()){
                //Write returned data from query to string and append into reportField
                int month = r1.getInt("MONTH(Start)");
                String monthString = "   Month: " + month;
                int count = r1.getInt("COUNT(*)");
                String countString = "   Count: " + count + "\n";

                reportField.appendText(monthString + countString);
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    @FXML
    void contactSchedule(ActionEvent event) throws IOException {
        //clear reportField
        reportField.clear();
        try{
            Connection c = DBConnection.getConnection();
            //Write a query to return appointments associated with a contact
            String getContactDataQuery = "SELECT contacts.Contact_Name, appointments.Title, appointments.Type, appointments.Description, appointments.Start, appointments.End, appointments.Appointment_ID, appointments.Customer_ID FROM contacts INNER JOIN appointments ON contacts.Contact_ID = appointments.Contact_ID;";
            DBQuery.setStatement(c, getContactDataQuery);
            PreparedStatement p = DBQuery.getStatement();
            p.execute();
            ResultSet r = p.getResultSet();

            while(r.next()){
                //Write the returned data from the above query to a set of strings
                String contactName = "Contact: " + r.getString("Contact_Name");
                String title = "   |Title: " + r.getString("Title");
                String type = "   |Type: " + r.getString("Type");
                String desc = "   |Description: " + r.getString("Description");
                String start = "   |Start: " + r.getTimestamp("Start");
                String end = "   |End: " + r.getTimestamp("End");
                String apptID = "   |Appointment ID:" + r.getInt("Appointment_ID");
                String custID = "   |Customer ID: " + r.getInt("Customer_ID");

                //Append the reportField with the new data
                reportField.appendText(contactName + title + type + desc + start + end + apptID + custID + "\n");

            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    @FXML
    void backBtnPressed(ActionEvent event) throws IOException{

        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/MainView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

}

