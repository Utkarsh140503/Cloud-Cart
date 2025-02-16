package com.utkarsh.codelink;

public class HelperClass {
    private String name;
    private String email;
    private String upiid;
    private String idtype;
    private String idnumber;
    private String password;
    private String storeID;
    private String currentDateAndTime;

    // Empty constructor required for Firebase
    public HelperClass() {
    }

    public HelperClass(String name, String email, String upiid, String idtype, String idnumber, String password, String storeID, String currentDateAndTime) {
        this.name = name;
        this.email = email;
        this.upiid = upiid;
        this.idtype = idtype;
        this.idnumber = idnumber;
        this.password = password;
        this.storeID = storeID;
        this.currentDateAndTime = currentDateAndTime;
    }

    // Getters and Setters (required for Firebase)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUpiid() {
        return upiid;
    }

    public void setUpiid(String upiid) {
        this.upiid = upiid;
    }

    public String getIdtype() {
        return idtype;
    }

    public void setIdtype(String idtype) {
        this.idtype = idtype;
    }

    public String getIdnumber() {
        return idnumber;
    }

    public void setIdnumber(String idnumber) {
        this.idnumber = idnumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getCurrentDateAndTime() {
        return currentDateAndTime;
    }

    public void setCurrentDateAndTime(String currentDateAndTime) {
        this.currentDateAndTime = currentDateAndTime;
    }
}
