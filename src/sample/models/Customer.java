package sample.models;

import com.sun.javafx.reflect.ConstructorUtil;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Customer {

    private int id;
    private String name;
    private String address;
    private String postal;
    private String phone;
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime lastUpdate;
    private String lastUpdateBy;
    private int divisionID;

    /**Default Customer constructor used for retreiveing data from add and update Customer views
    * @param name
     * @param address
     * @param postal
     * @param phone
     * @param divisionID*/
    public Customer(String name, String address, String postal, String phone, int divisionID){

        this.name = name;
        this.address = address;
        this.postal = postal;
        this.phone = phone;
        this.divisionID = divisionID;

    }

    /**Overloaded Customer constructor
     * @param id
     * @param name
     * @param address
     * @param postal
     * @param phone
     * @param divisionID*/
    public Customer(int id, String name, String address, String postal, String phone, int divisionID){

        this.id = id;
        this.name = name;
        this.address = address;
        this.postal = postal;
        this.phone = phone;
        this.divisionID = divisionID;

    }

    /**Overloaded Customer constructor having all items included in the database
     * @param id
     * @param name
     * @param address
     * @param postal
     * @param phone
     * @param createDate
     * @param createdBy
     * @param lastUpdate
     * @param lastUpdateBy
     * @param divisionID */
    public Customer(int id, String name, String address, String postal, String phone, LocalDateTime createDate, String createdBy, LocalDateTime lastUpdate, String lastUpdateBy, int divisionID){

        this.id = id;
        this.name = name;
        this.address = address;
        this.postal = postal;
        this.phone = phone;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdateBy = lastUpdateBy;
        this.divisionID = divisionID;

    }

    //Setters
    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public void setPostal(String postal){
        this.postal = postal;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public void setCreateDate(LocalDateTime createDate){
        this.createDate = createDate;
    }

    public void setCreatedBy(String createdBy){
        this.createdBy = createdBy;
    }

    public void setLastUpdate(LocalDateTime lastUpdate){
        this.lastUpdate = lastUpdate;
    }

    public void setLastUpdateBy(String lastUpdateBy){
        this.lastUpdateBy = lastUpdateBy;
    }

    public void setDivisionID(int divisionID){
        this.divisionID = divisionID;
    }

    //Getters
    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getAddress(){
        return address;
    }

    public String getPostal(){
        return postal;
    }

    public String getPhone(){
        return phone;
    }

    public LocalDateTime getCreateDate(){
        return createDate;
    }

    public String getCreatedBy(){
        return createdBy;
    }

    public LocalDateTime getLastUpdate(){
        return lastUpdate;
    }

    public String getLastUpdateBy(){
        return lastUpdateBy;
    }

    public int getDivisionID(){
        return divisionID;
    }

    /*
    *Override the default toString() method for a Customer object
    * Used for when comboboxes display Customer objects*/
    @Override
    public String toString(){

        return(getName());

    }

}
