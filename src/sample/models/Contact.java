package sample.models;

public class Contact {

    private int id;
    private String name;
    private String email;

    /**Contact constructor
     * @param id
     * @param name
     * @param email*/
    public Contact(int id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }

    //getters and setters
    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    /*Override the default toString() method to display the contact name for any
    * combobox displaying Contact items*/
    @Override
    public String toString(){

        return(getName());

    }

}
