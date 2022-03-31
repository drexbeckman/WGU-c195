package sample.models;

import java.sql.Date;
import java.sql.Timestamp;

public class Division {

    private int id;
    private String name;
    private Date createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdateBy;
    private int countryID;

    /**Division constructor
     * @param id
     * @param name
     * @param createDate
     * @param createdBy
     * @param lastUpdate
     * @param lastUpdateBy
     * @param countryID foreign key from countries table in database*/
    public Division(int id, String name, Date createDate, String createdBy, Timestamp lastUpdate, String lastUpdateBy, int countryID){
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdateBy = lastUpdateBy;
        this.countryID = countryID;
    }

    //Setters
    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setCreateDate(Date createDate){
        this.createDate = createDate;
    }

    public void setCreatedBy(String createdBy){
        this.createdBy = createdBy;
    }

    public void setLastUpdate(Timestamp lastUpdate){
        this.lastUpdate = lastUpdate;
    }

    public void setLastUpdateBy(String lastUpdateBy){
        this.lastUpdateBy = lastUpdateBy;
    }

    public void setCountryID(int countryID){
        this.countryID = countryID;
    }

    //Getters
    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public Date getCreateDate(){
        return createDate;
    }

    public String getCreatedBy(){
        return createdBy;
    }

    public Timestamp getLastUpdate(){
        return lastUpdate;
    }

    public String getLastUpdateBy(){
        return lastUpdateBy;
    }

    public int getCountryID(){
        return countryID;
    }

    //override the default toString() method to return Division getName() method for readability
    @Override
    public String toString(){

        return(getName());

    }

}
