package com.utkarsh.codelink;

public class storeHelperClass {
    String storeName, name, idnumber, email, phone, businessType, password;

    public storeHelperClass(String storeName,String name,String idnumber,String email,String phone,String businessType,String password){
        this.storeName = storeName;
        this.name = name;
        this.idnumber = idnumber;
        this.email = email;
        this.phone = phone;
        this.businessType = businessType;
        this.password = password;
    }

    public String getStoreName() { return storeName; }

    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getIdnumber() { return idnumber; }

    public void setIdnumber(String idnumber) { this.idnumber = idnumber; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getBusinessType() { return businessType; }

    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public storeHelperClass(){
    }
}
