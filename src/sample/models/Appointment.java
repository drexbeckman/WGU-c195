package sample.models;

import java.time.LocalDateTime;

public class Appointment {

    private int id;
    private String title;
    private String description;
    private String location;
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int customerID;
    private int userID;
    private int contactID;

    //Appointment constructor
    /**Appointment constructor
     * @param id
     * @param  title
     * @param  description
     * @param  location
     * @param type
     * @param startTime
     * @param endTime
     * @param customerID
     * @param contactID
     * @param userID*/
    public Appointment(int id, String title, String description, String location, String type, LocalDateTime startTime, LocalDateTime endTime, int customerID, int userID, int contactID){

        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.customerID = customerID;
        this.userID = userID;
        this.contactID = contactID;

    }

    //setters and getters
    public void setId(int id){
        this.id = id;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setStartTime(LocalDateTime startTime){
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime){
        this.endTime = endTime;
    }

    public void setCustomerID(int customerID){
        this.customerID = customerID;
    }

    public void setUserID(int userID){
        this.userID = userID;
    }

    public void setContactID(int contactID){
        this.contactID = contactID;
    }

    public int getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public String getLocation(){
        return location;
    }

    public String getType(){
        return type;
    }

    public LocalDateTime getStartTime(){
        return startTime;
    }

    public LocalDateTime getEndTime(){
        return endTime;
    }

    public int getCustomerID(){
        return customerID;
    }

    public int getUserID(){
        return userID;
    }

    public int getContactID(){
        return contactID;
    }

    /*Override the default toString() method to return the appointment's
    title in any combobox containing Appointment objects */
    @Override
    public String toString(){
        return getTitle();
    }
}
