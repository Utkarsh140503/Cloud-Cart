package com.utkarsh.codelink;

public class ReturnsHelperClass {
    String EmailText;
    public ReturnsHelperClass(String EmailText){
        this.EmailText = EmailText;
    }

    public String getEmailText() {
        return EmailText;
    }

    public void setEmailText(String emailText) {
        EmailText = emailText;
    }

    public ReturnsHelperClass(){

    }
}
