package sample.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sample.models.*;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.util.ResourceBundle;

public class AddAppointmentViewController implements Initializable {
    Stage stage;
    Parent scene;
    private LocalDateTime apptDateTimeStart;
    private LocalDateTime apptDateTimeEnd;
    private ObservableList<LocalTime> startTimesList = FXCollections.observableArrayList();
    private ObservableList<LocalTime> endTimesList = FXCollections.observableArrayList();
    private final ZoneId userZone = ZoneId.systemDefault();
    private final ZoneId businessZone = ZoneId.of("US/Eastern");
    private final ZoneId utcZone = ZoneId.of("UTC");
    private LocalDate selectedDate;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;
    private Contact selectedContact;
    private ObservableList<Contact> contactsList = FXCollections.observableArrayList();
    private ObservableList<Customer> customersList = FXCollections.observableArrayList();
    private static boolean isDateSelected;
    private static boolean isStartTimeSelected;
    private static boolean isEndTimeSelected;
    private static boolean isGoodStartTime;
    private static boolean isGoodEndTime;
    private Customer selectedCustomer;
    private static int userID;
    private static String userString;
    private ObservableList<String> typesList = FXCollections.observableArrayList();

    public static void setUserID(int userID1){
        userID = userID1;
    }
    public static int getUserID(){
        return userID;
    }

    public static void setUserString(String userString1){
        userString = userString1;
    }
    public String getUserString(){
        return userString;
    }

    @FXML
    private TextField apptIDField;

    @FXML
    private ComboBox<Customer> custNameComboBox;

    @FXML
    private TextField titleIDField;

    @FXML
    private ComboBox<Contact> contactMenu;

    @FXML
    private TextField locationField;

    @FXML
    private DatePicker startCal;

    @FXML
    private ComboBox<LocalTime> startTimeMenu;

    @FXML
    private DatePicker endCal;

    @FXML
    private ComboBox<LocalTime> endTimeMenu;

    @FXML
    private TextField descField;

    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    void getSelectedCustomer(ActionEvent event) {
        selectedCustomer = custNameComboBox.getSelectionModel().getSelectedItem();
    }

    @FXML
    void getSelectedContact(ActionEvent event) {
        selectedContact = contactMenu.getSelectionModel().getSelectedItem();
    }

    @FXML
    void selectEndTime(ActionEvent event) {
        selectedEndTime = endTimeMenu.getSelectionModel().getSelectedItem();

        isEndTimeSelected = true;

    }

    @FXML
    void selectApptDate(ActionEvent event) {

        selectedDate = startCal.getValue();

        isDateSelected = true;

    }

    @FXML
    void selectStartTime(ActionEvent event) {
        isStartTimeSelected = true;

        selectedStartTime = startTimeMenu.getSelectionModel().getSelectedItem();
    }

    @FXML
    void addBtnPressed(ActionEvent event){
        try {
            //Confirm all of the required options are selected
            if (isDateSelected && isStartTimeSelected && isEndTimeSelected) {

                //Create a boolean to confirm there are not appointment time conflicts
                boolean noApptConflicts = false;

                //Confirm all the times are within business hours
                apptDateTimeStart = selectedStartTime.atDate(selectedDate);
                ZonedDateTime compare = apptDateTimeStart.atZone(userZone).withZoneSameInstant(businessZone);
                LocalTime compareLDT = compare.toLocalTime();
                LocalTime c2 = LocalTime.of(8, 0);

                apptDateTimeEnd = selectedEndTime.atDate(selectedDate);
                ZonedDateTime compareEnd = apptDateTimeEnd.atZone(userZone).withZoneSameInstant(businessZone);
                LocalTime compareEndLDT = compareEnd.toLocalTime();
                LocalTime c3 = LocalTime.of(22, 0);

                if ((compareLDT.isAfter(c2) || compareLDT.equals(c2)) && compareLDT.isBefore(c3) && compareLDT.isBefore(compareEndLDT)) {
                    isGoodStartTime = true;
                }
                else{
                    isGoodStartTime = false;
                }

                if (((compareEndLDT.isBefore(c3)  && compareEndLDT.isAfter(c2)) || compareEndLDT.equals(c3)) && compareEndLDT.isAfter(compareLDT)) {
                    isGoodEndTime = true;
                }
                else{
                    isGoodEndTime = false;
                }

                if (isGoodStartTime && isGoodEndTime) {

                    //Convert times into UTC and timestamp to compare to any appointments on the database.
                    ZonedDateTime localStartDate = ZonedDateTime.of(selectedDate, selectedStartTime, userZone);
                    ZonedDateTime UTCStartZDT = localStartDate.withZoneSameInstant(utcZone);
                    LocalDateTime UTCStartDate = UTCStartZDT.toLocalDateTime();
                    Timestamp UTCStartDateTs = Timestamp.valueOf(UTCStartDate);

                    ZonedDateTime localEndDate = ZonedDateTime.of(selectedDate, selectedEndTime, userZone);
                    ZonedDateTime UTCEndZDT = localEndDate.withZoneSameInstant(utcZone);
                    LocalDateTime UTCEndDate = UTCEndZDT.toLocalDateTime();
                    Timestamp UTCEndDateTs = Timestamp.valueOf(UTCEndDate);

                    boolean b = confirmApptHours(UTCStartDateTs, UTCEndDateTs);
                    if (b) {
                        Connection c = DBConnection.getConnection();
                        String checkTimeConflictsQuery = "SELECT * FROM appointments;";
                        DBQuery.setStatement(c, checkTimeConflictsQuery);
                        PreparedStatement checkTimePS = DBQuery.getStatement();
                        checkTimePS.execute();

                        ResultSet checkTimeRS = checkTimePS.getResultSet();

                        while(checkTimeRS.next()){
                            Timestamp startTime = checkTimeRS.getTimestamp("Start");
                            Timestamp endTime = checkTimeRS.getTimestamp("End");

                            if(UTCStartDateTs.equals(startTime)){
                                noApptConflicts = false;
                                Alert a = new Alert(Alert.AlertType.INFORMATION);
                                a.setTitle("Cannot create appointment (scheduling conflict)");
                                a.setContentText("Your appointment cannot be created because an appointment already exists in the time slot." +
                                        "You can create an appointment at a different time.");
                                a.showAndWait();
                            }
                            else{
                                noApptConflicts = true;
                            }
                        }

                        if(noApptConflicts) {
                            //Put new appt information into the database
                            Connection addApptConnection = DBConnection.getConnection();
                            String addApptQuery = "INSERT INTO appointments(Title, Description, Location, Type, Start, End, Created_By, Customer_ID, User_ID, Contact_ID) VALUES(?,?,?,?,?,?,?,?,?,?);";
                            DBQuery.setStatement(addApptConnection, addApptQuery);
                            PreparedStatement addApptPS = DBQuery.getStatement();

                            addApptPS.setString(1, titleIDField.getText());
                            addApptPS.setString(2, descField.getText());
                            addApptPS.setString(3, locationField.getText());
                            addApptPS.setString(4, typeCombo.getSelectionModel().getSelectedItem());
                            addApptPS.setTimestamp(5, UTCStartDateTs);
                            addApptPS.setTimestamp(6, UTCEndDateTs);
                            addApptPS.setString(7, getUserString());
                            addApptPS.setInt(8, selectedCustomer.getId());
                            addApptPS.setInt(9, getUserID());
                            addApptPS.setInt(10, selectedContact.getId());

                            addApptPS.executeUpdate();

                            Alert a = new Alert(Alert.AlertType.INFORMATION);
                            a.setTitle("Your appointment has been scheduled");
                            a.setContentText("Your appointment has been successfully scheduled.");
                            a.showAndWait();

                            stage = (Stage)((Button) event.getSource()).getScene().getWindow();
                            scene = FXMLLoader.load(getClass().getResource("../views/AppointmentsView.fxml"));
                            stage.setScene(new Scene(scene));
                            stage.show();
                        }
                    }
                } else {

                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Selected hours incompatible");
                    a.setContentText("Your selected hours do not correspond to business hours of 0800-2200 EST, or your scheduled start time occurs after your scheduled end time.");
                    a.showAndWait();
                }

            }

            else{
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("All fields must be filled out");
                a.setContentText("Please make sure all fields are filled out before submitting an appointment");
                a.showAndWait();
            }
        }
        catch (NullPointerException e){
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Problem creating appointment");
            a.setContentText("There was an issue creating your appointment. Make sure all required fields are filled out correctly.");
            a.showAndWait();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        catch (Exception e){

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Problem creating appointment");
            a.setContentText("There was an issue creating your appointment. Make sure all required fields are filled out correctly.");
            a.showAndWait();
        }
    }

    /**
     * This method confirms whether the two times, selected by the user, match the office's hours converted to EST
     * @param start Selected start date converted into Timestamp
     * @param  end Selected end date converted into Timestamp
     * @return isGoodApptTime*/
    public boolean confirmApptHours(Timestamp start, Timestamp end)throws SQLException{
        boolean isGoodApptTime = false;

            Connection c = DBConnection.getConnection();
            String compareApptTimesQuery = "SELECT * FROM appointments;";
            DBQuery.setStatement(c, compareApptTimesQuery);
            PreparedStatement p = DBQuery.getStatement();
            p.execute();

            ResultSet r = p.getResultSet();
            if(p.getUpdateCount() > 0){

                while(r.next()){
                    Timestamp timeStart = r.getTimestamp("Start");
                    Timestamp timeEnd = r.getTimestamp("End");

                    if(timeStart.equals(start) && timeEnd.equals(end)){
                        isGoodApptTime = true;
                    }
                    else{
                        isGoodApptTime = false;
                    }
                }

            }
            else{
                isGoodApptTime = true;
            }
            return isGoodApptTime;


    }

    @FXML
    void cancelBtnPressed(ActionEvent event) throws IOException{
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/AppointmentsView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //set types of meertings for the types combo box through an observable array list
        typesList.addAll("De-Briefing", "Planning Session", "Other");
        typeCombo.setItems(typesList);

        isDateSelected = false;
        isStartTimeSelected = false;
        isEndTimeSelected = false;
        isGoodStartTime = false;
        isGoodEndTime = false;

        //Set the start and end times by an observable array list. The appts will be in increments of 30 minutes
        LocalTime startTime = LocalTime.of(0, 0);
        LocalTime endTime = LocalTime.of(23, 30);

        while(startTime.isBefore(endTime)){
            startTimesList.add(startTime);
            endTimesList.add(startTime);
            startTime = startTime.plusMinutes(30);

        }

        startTimeMenu.setItems(startTimesList);
        endTimeMenu.setItems(startTimesList);

        //Retrieve contact information and put into an array list. Contact's toString method will display contact name
        try{
            Connection c = DBConnection.getConnection();
            String getContactsQuery = "SELECT * FROM contacts;";
            DBQuery.setStatement(c, getContactsQuery);
            PreparedStatement p = DBQuery.getStatement();
            p.execute();
            ResultSet r = p.getResultSet();

            while(r.next()){

                int contactID = r.getInt("Contact_ID");
                String contactName = r.getString("Contact_Name");
                String contactEmail = r.getString("Email");
                Contact c1 = new Contact(contactID, contactName, contactEmail);

                contactsList.add(c1);


            }

            contactMenu.setItems(contactsList);

            //Connect to database and load all customer data into the combo box through an observable array list.
            Connection c1 = DBConnection.getConnection();
            String getCustomerDataQuery = "SELECT * FROM customers;";
            DBQuery.setStatement(c1, getCustomerDataQuery);
            PreparedStatement p1 = DBQuery.getStatement();
            p1.execute();

            ResultSet r1 = p1.getResultSet();

            while(r1.next()){
                int customerID = r1.getInt("Customer_ID");
                String customerName = r1.getString("Customer_Name");
                String customerAddress = r1.getString("Address");
                String customerPostal = r1.getString("Postal_Code");
                String customerPhone = r1.getString("Phone");
                int customerDivisionID = r1.getInt("Division_ID");

                Customer c2 = new Customer(customerID, customerName, customerAddress, customerPostal, customerPhone, customerDivisionID);

                customersList.add(c2);

            }
            custNameComboBox.setItems(customersList);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
}
