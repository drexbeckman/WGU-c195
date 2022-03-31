package sample.models;

import java.sql.*;

public class Country{

    private int id;
    private String name;
    private Date createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdateBy;

    //Default Country constructor
    /**Default Country constructor
     *@param id
     * @param name */
    public Country(int id, String name){

        this.id = id;
        this.name = name;

    }

    /**Overloaded Country constructor containing all database information corresponding to
     * a Country object
     * @param id
     * @param name
     * @param createDate
     * @param createdBy
     * @param lastUpdate
     * @param lastUpdateBy */
    public Country(int id, String name, Date createDate, String createdBy, Timestamp lastUpdate, String lastUpdateBy){

        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdateBy = lastUpdateBy;

    }

    //Getters and setters
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


    /*Override the default toString() method for Country objects.
    * Used to display Country object's name in comboboxes*/
    @Override
    public String toString(){

        return(getName());

    }

}
