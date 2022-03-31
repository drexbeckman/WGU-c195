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

public class UpdateAppointmentViewController implements Initializable {

    Stage stage;
    Parent scene;
    private static int selectedApptID;
    private ObservableList<Contact> contactsList = FXCollections.observableArrayList();
    private ObservableList<LocalTime> timesList = FXCollections.observableArrayList();
    private ObservableList<Customer> customersList = FXCollections.observableArrayList();
    private ObservableList<String> typesList = FXCollections.observableArrayList();
    private final ZoneId localZone = ZoneId.systemDefault();
    private final ZoneId utcZone = ZoneId.of("UTC");
    private final ZoneId businessZone = ZoneId.of("US/Eastern");
    private boolean isCustomerSelected;
    private boolean isContactSelected;
    private boolean isStartTimeSelected;
    private boolean isEndTimeSelected;
    private boolean isGoodStartTime;
    private boolean isGoodEndTime;
    private boolean isDateSelected;
    private LocalDateTime apptDateTimeStart;
    private LocalDateTime apptDateTimeEnd;
    private Customer selectedCustomer;
    private Contact selectedContact;
    private LocalDate selectedDate;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;
    public static int userID;
    public static String userString;
    public static int contactID2;

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

    public static void setSelectedApptID(int i){
        selectedApptID = i;
    }
    public static int getSelectedApptID(){
        return  selectedApptID;
    }

    @FXML
    private TextField apptIDField;

    @FXML
    private ComboBox<Customer> custComboBox;


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
    void dateSelected(ActionEvent event) {
        selectedDate = startCal.getValue();
    }

    @FXML
    void contactSelected(ActionEvent event) {
        selectedContact = contactMenu.getSelectionModel().getSelectedItem();
        contactID2 = selectedContact.getId();
        isContactSelected = true;
    }

    @FXML
    void customerSelected(ActionEvent event) {
        selectedCustomer = custComboBox.getSelectionModel().getSelectedItem();
        isCustomerSelected = true;
    }

    @FXML
    void endTimeSelected(ActionEvent event) {
        selectedEndTime = endTimeMenu.getSelectionModel().getSelectedItem();
        isEndTimeSelected = true;
    }

    @FXML
    void startTimeSelected(ActionEvent event) {
        selectedStartTime = startTimeMenu.getSelectionModel().getSelectedItem();
        isStartTimeSelected = true;
    }

    @FXML
    void updateBtnPressed(ActionEvent event) {

        try{
            if(isCustomerSelected && isContactSelected && isStartTimeSelected && isEndTimeSelected && isDateSelected){
                //Create a boolean to confirm there are not appointment time conflicts
                boolean noApptConflicts = false;

                //Confirm all the times are within business hours
                apptDateTimeStart = selectedStartTime.atDate(selectedDate);
                ZonedDateTime compare = apptDateTimeStart.atZone(localZone).withZoneSameInstant(businessZone);
                LocalTime compareLDT = compare.toLocalTime();
                LocalTime c2 = LocalTime.of(8, 0);

                apptDateTimeEnd = selectedEndTime.atDate(selectedDate);
                ZonedDateTime compareEnd = apptDateTimeEnd.atZone(localZone).withZoneSameInstant(businessZone);
                LocalTime compareEndLDT = compareEnd.toLocalTime();
                LocalTime c3 = LocalTime.of(22, 0);

                if ((compareLDT.isAfter(c2) || compareLDT.equals(c2)) && compareLDT.isBefore(c3) && compareLDT.isBefore(compareEndLDT)) {
                    isGoodStartTime = true;
                }
                else {
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
                    ZonedDateTime localStartDate = ZonedDateTime.of(selectedDate, selectedStartTime, localZone);
                    ZonedDateTime UTCStartZDT = localStartDate.withZoneSameInstant(utcZone);
                    LocalDateTime UTCStartDate = UTCStartZDT.toLocalDateTime();
                    Timestamp UTCStartDateTs = Timestamp.valueOf(UTCStartDate);

                    ZonedDateTime localEndDate = ZonedDateTime.of(selectedDate, selectedEndTime, localZone);
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
                            int apptID = checkTimeRS.getInt("Appointment_ID");

                            if(UTCStartDateTs.equals(startTime)){
                                if(apptID == selectedApptID){
                                    noApptConflicts = true;
                                }
                                else{
                                noApptConflicts = false;
                                Alert a = new Alert(Alert.AlertType.INFORMATION);
                                a.setTitle("Cannot create appointment (scheduling conflict)");
                                a.setContentText("Your appointment cannot be created because an appointment already exists in the time slot." +
                                        "You can create an appointment at a different time.");
                                a.showAndWait();
                                }
                            }
                            else{
                                noApptConflicts = true;
                            }
                        }

                        if(noApptConflicts) {

                            if(titleIDField.getText().equals("") || descField.getText().equals("") || locationField.getText().equals("") || typeCombo.getSelectionModel().getSelectedItem().isEmpty()){
                                Alert a = new Alert(Alert.AlertType.INFORMATION);
                                a.setTitle("Fill out required fields");
                                a.setContentText("Please ensure all fields are filled out before submitting");
                                a.showAndWait();
                            }
                            else{

                            Connection addApptConnection = DBConnection.getConnection();
                            String addApptQuery = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = ?, Start = ?, End = ?, Last_Updated_By = ?, Contact_ID = ? WHERE Appointment_ID = ?;";
                            DBQuery.setStatement(addApptConnection, addApptQuery);
                            PreparedStatement addApptPS = DBQuery.getStatement();

                            addApptPS.setString(1, titleIDField.getText());
                            addApptPS.setString(2, descField.getText());
                            addApptPS.setString(3, locationField.getText());
                            addApptPS.setString(4, typeCombo.getSelectionModel().getSelectedItem());
                            addApptPS.setTimestamp(5, UTCStartDateTs);
                            addApptPS.setTimestamp(6, UTCEndDateTs);
                            addApptPS.setString(7, getUserString());
                            addApptPS.setInt(8, contactID2);
                            addApptPS.setInt(9, getSelectedApptID());

                            addApptPS.executeUpdate();

                            Alert a = new Alert(Alert.AlertType.INFORMATION);
                            a.setTitle("Your appointment has been updated");
                            a.setContentText("Your appointment has been successfully updated.");
                            a.showAndWait();

                            stage = (Stage)((Button) event.getSource()).getScene().getWindow();
                            scene = FXMLLoader.load(getClass().getResource("../views/AppointmentsView.fxml"));
                            stage.setScene(new Scene(scene));
                            stage.show();
                            }
                        }

                    }
                } else {

                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Selected hours incompatible");
                    a.setContentText("Your selected hours do not correspond to business hours of 0800-2200 EST, or your scheduled start time occurs after your scheduled end time.");
                    a.showAndWait();
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch(NullPointerException e){
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Problem creating appointment");
            a.setContentText("There was an issue creating your appointment. Make sure all required fields are filled out correctly.");
            a.showAndWait();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void cancelBtnPressed(ActionEvent event) throws IOException {
        stage = (Stage)((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("../views/AppointmentsView.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();

    }

    /**Determines whether or not the user's selected start and end times do not overlap with exisiting appointments
     * @param start User's selected start time converted to Timestamp object
     * @param  end User's selected end time converted to Timestamp object
     * @return  isGoodApptTime*/
    public boolean confirmApptHours(Timestamp start, Timestamp end)throws SQLException{
        boolean isGoodApptTime = false;
        //Connect to database and return appointment data
        Connection c = DBConnection.getConnection();
        String compareApptTimesQuery = "SELECT * FROM appointments;";
        DBQuery.setStatement(c, compareApptTimesQuery);
        PreparedStatement p = DBQuery.getStatement();
        p.execute();

        ResultSet r = p.getResultSet();
        if(p.getUpdateCount() > 0){

            while(r.next()){
                //Return the start and end times from the database for all appts
                Timestamp timeStart = r.getTimestamp("Start");
                Timestamp timeEnd = r.getTimestamp("End");

                //In the case where start and end times the same, set boolean as true, since in this case, it is the appt being updated
                if(timeStart.equals(start) && timeEnd.equals(end)){
                    isGoodApptTime = true;
                }
                //In other cases, there is a time overlap. Return boolean as false.
                else{
                    isGoodApptTime = false;
                }
            }

        }
        //If no overlaps exist, then the time is okay to update.
        else{
            isGoodApptTime = true;
        }
        return isGoodApptTime;


    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //initialize typesList
        typesList.addAll("De-Briefing", "Planning Session", "Other");
        typeCombo.setItems(typesList);

        //set boolean values starting as false
        isCustomerSelected = true;
        isContactSelected = true;
        isStartTimeSelected = true;
        isEndTimeSelected = true;
        isDateSelected = true;
        contactID2 = 0;

        //connect to database and populate fields with correct data
        LocalTime startTime = LocalTime.of(0, 0);
        LocalTime endTime = LocalTime.of(23, 30);
        LocalDate apptDate = LocalDate.now();
        int customerID = 0;
        int contactID = 0;
        Timestamp startTimeTS;
        Timestamp endTimeTS;


        while(startTime.isBefore(endTime)){
            timesList.add(startTime);
            startTime = startTime.plusMinutes(30);
        }

        startTimeMenu.setItems(timesList);
        endTimeMenu.setItems(timesList);

        try{
            //Populate the appointment field by connecting to the database.
            Connection c = DBConnection.getConnection();
            String getApptDataQuery = "SELECT * FROM appointments WHERE Appointment_ID = ?;";
            DBQuery.setStatement(c, getApptDataQuery);
            PreparedStatement getApptDataPS = DBQuery.getStatement();
            getApptDataPS.setInt(1, getSelectedApptID());
            getApptDataPS.execute();

            ResultSet r = getApptDataPS.getResultSet();
            while(r.next()){
                /*Convert database information into their data type to populate fields in the interface*/
                customerID = r.getInt("Customer_ID");
                String title = r.getString("Title");
                String description = r.getString("Description");
                String location = r.getString("Location");
                String type = r.getString("Type");

                startTimeTS = r.getTimestamp("Start");
                endTimeTS = r.getTimestamp("End");

                //Convert the timestamps to LocalDateTime objects and convert to the default time zone
                LocalDateTime startTimeLDT= startTimeTS.toLocalDateTime();
                LocalDateTime endTimdLDT = endTimeTS.toLocalDateTime();
                LocalTime startTimeLT = startTimeLDT.toLocalTime();
                LocalDate startTimeLD = startTimeLDT.toLocalDate();
                ZonedDateTime startTimeZDT = ZonedDateTime.of(startTimeLD, startTimeLT, utcZone);
                ZonedDateTime startTimeToLocal = startTimeZDT.withZoneSameInstant(localZone);
                startTimeLT = startTimeToLocal.toLocalTime();

                LocalTime endTimeLT = endTimdLDT.toLocalTime();
                LocalDate endTimeLD = endTimdLDT.toLocalDate();
                apptDate = endTimeLD;
                ZonedDateTime endTimeZDT = ZonedDateTime.of(endTimeLD, endTimeLT, utcZone);
                ZonedDateTime endTimeToLocal = endTimeZDT.withZoneSameInstant(localZone);
                endTimeLT = endTimeToLocal.toLocalTime();

                contactID = r.getInt("Contact_ID");

                //Add the data from database onto the fields
                titleIDField.setText(title);
                descField.setText(description);
                locationField.setText(location);
                typeCombo.getSelectionModel().select(type);

                /*Search through the timesList and select the time corresponding the the startTime,
                and select from the menu so it is set to the same time when screens are switched*/
                int startIndex = 0;
                for(int i = 0; i < timesList.size(); i++){
                    LocalTime t1 = timesList.get(i);
                    if(t1.equals(startTimeLT)){
                        startIndex = i;}
                }
                startTimeMenu.getSelectionModel().select(startIndex);
                selectedStartTime = timesList.get(startIndex);

                /*Search throught the timesList and select the same end time as the one
                * from the database and select it so that the default is selected when the screens are switched*/
                int endIndex = 0;
                for(int i = 0; i < timesList.size(); i++){
                    LocalTime t1 = timesList.get(i);
                    if(t1.equals(endTimeLT)){
                        endIndex = i;
                    }
                }
                //Take the matched time from the list and select is as default
                endTimeMenu.getSelectionModel().select(endIndex);
                selectedEndTime = timesList.get(endIndex);
            }
            //Select the calendar value as the same from the database
            startCal.setValue(apptDate);
            selectedDate = apptDate;

            //Connect to the database and get the data
            Connection c1 = DBConnection.getConnection();
            String getCustomerDataQuery = "SELECT * FROM customers;";
            DBQuery.setStatement(c1, getCustomerDataQuery);
            PreparedStatement customerdataPS = DBQuery.getStatement();
            customerdataPS.execute();
            ResultSet customerRS = customerdataPS.getResultSet();

            while(customerRS.next()){
                //Retrieve the data and separate into corresponding data types
                int id = customerRS.getInt("Customer_ID");
                String name = customerRS.getString("Customer_Name");
                String address = customerRS.getString("Address");
                String postal = customerRS.getString("Postal_Code");
                String phone = customerRS.getString("Phone");
                int divisionID = customerRS.getInt("Division_ID");

                //Create a customer object and insert into the customerList
                Customer cu = new Customer(id, name, address, postal, phone, divisionID);
                customersList.add(cu);
            }
            Customer defaultCust = null;
            //Populate the comboBox with the customerList
            custComboBox.setItems(customersList);
            for(int i = 0; i < customersList.size(); i++){
                int custID = customersList.get(i).getId();
                if(custID == customerID){
                    defaultCust = customersList.get(i);
                }
            }
            custComboBox.getSelectionModel().select(defaultCust);
            selectedCustomer = defaultCust;
            //isCustomerSelected = true;

            Connection c2 = DBConnection.getConnection();
            String getContactDataQuery = "SELECT * FROM contacts;";
            DBQuery.setStatement(c2, getContactDataQuery);
            PreparedStatement contactDataPS = DBQuery.getStatement();
            //contactDataPS.setInt(1, contactID);
            contactDataPS.execute();
            ResultSet contactDataRS = contactDataPS.getResultSet();
            //Populate the contacts observable list

            while(contactDataRS.next()){
                int id = contactDataRS.getInt("Contact_ID");
                String name = contactDataRS.getString("Contact_Name");
                String email = contactDataRS.getString("Email");

                Contact co = new Contact(id, name, email);
                contactsList.add(co);
            }
            Contact defaultContact = null;
            for(int i = 0; i < contactsList.size(); i++){
                int contactID1 = contactsList.get(i).getId();
                if(contactID1 == contactID){
                    defaultContact = contactsList.get(i);
                    contactID2 = defaultContact.getId();
                }
            }
            //add the contacts list to the contact combo box
            contactMenu.setItems(contactsList);
            //Set the default contact the same as the one set in the database
            contactMenu.getSelectionModel().select(defaultContact);

        }

        catch(SQLException e){
            e.printStackTrace();
        }

    }
}
