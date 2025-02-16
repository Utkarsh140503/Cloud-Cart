package com.utkarsh.codelink;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class StoreSignupActivity extends AppCompatActivity {

    EditText storeSignupName, storeOwnerName, storeIdNum, storeEmail, storePhone, storeBusinessType, storePass;
    TextView storeLoginRedirectText;
    Button storeSignupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    String storeName, name, idnumber, email, phone, businessType, password;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_signup);

        storeSignupName = findViewById(R.id.store_signup_name);
        storeOwnerName = findViewById(R.id.store_signup_owner_name);
        storeIdNum = findViewById(R.id.store_signup_Id_Num);
        storeEmail = findViewById(R.id.store_signup_email);
        storePhone = findViewById(R.id.store_signup_phone);
        storeBusinessType = findViewById(R.id.store_signup_businesstype);
        storePass = findViewById(R.id.store_signup_pass);
        storeSignupButton = findViewById(R.id.store_signup_button);
        storeLoginRedirectText = findViewById(R.id.store_loginRedirectText);

        storeSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!validStoreName() | !validName() | !validBusinessType() | !validEmail() | !validIdNum() |!validPass() | !validPhone() | !validPass2()){

                }
                else{
                    setData();
                }

            }
        });

        storeLoginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreSignupActivity.this, StoreLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void setData(){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("stores");

        storeName = storeSignupName.getText().toString().trim();
        name = storeOwnerName.getText().toString().trim();
        idnumber = storeIdNum.getText().toString().trim();
        email = storeEmail.getText().toString().trim();
        phone = storePhone.getText().toString().trim();
        businessType = storeBusinessType.getText().toString().trim();
        password = storePass.getText().toString().trim();

        String alldata = "StoreName: " + storeName + "Name: " + name + "IDNumber: " + idnumber +
                "Email: " + email + "Phone: " + phone + "BusinessType: " + businessType +
                "Password: " + password;
        QRGEncoder qrgEncoder = new QRGEncoder(alldata, null, QRGContents.Type.TEXT, 1000);
        Bitmap qrBits = qrgEncoder.getBitmap();

        storeHelperClass storeHelperClass = new storeHelperClass(storeName, name, idnumber, email, phone, businessType, password);
        reference.child(idnumber).setValue(storeHelperClass);

        Toast.makeText(StoreSignupActivity.this, "Welcome to the Headless Transaction Club!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(StoreSignupActivity.this, StoreLoginActivity.class);
        startActivity(intent);
    }

    public boolean validStoreName(){
        String val = storeSignupName.getText().toString().trim();
        if(val.isEmpty()){
            storeSignupName.setError("Store Name Cannot be Empty");
            return false;
        }
        else {
            if(val.length()<=30) {
                storeSignupName.setError(null);
                return true;
            }
            storeSignupName.setError("Store Name Cannot be more than 30 characters long");
            return false;
        }
    }

    public boolean validName(){
        String val = storeOwnerName.getText().toString().trim();
        if(val.isEmpty()){
            storeOwnerName.setError("Name Cannot be Empty");
            return false;
        }
        else {
            boolean x = ((val != null) && (!val.equals(""))
                    && (val.matches("^[a-zA-Z ]*$")));
            if(x==false){
                storeOwnerName.setError("Name can only have Alphabets");
                return false;
            }
            storeOwnerName.setError(null);
            return true;
        }
    }

    public boolean validIdNum(){
        String val = storeIdNum.getText().toString().trim();
        if(val.isEmpty()){
            storeIdNum.setError("ID Number Cannot be Empty");
            return false;
        }
        else {
            boolean x = ((val != null) && (!val.equals(""))
                    && (val.matches("^[a-zA-Z0-9]*$")));
            if(x==false){
                storeIdNum.setError("Store ID Number can only have Alphanumeric");
                return false;
            }
            storeIdNum.setError(null);
            return true;
        }
    }

    public boolean validEmail(){
        String val =storeEmail.getText().toString().trim();
        if(val.isEmpty()){
            storeEmail.setError("Email Cannot be Empty");
            return false;
        }
        else {
            if(val.contains("@")) {
                storeEmail.setError(null);
                return true;
            }
            storeEmail.setError("Email does not contain '@' symbol");
            return false;
        }
    }

    public boolean validPhone(){
        String val = storePhone.getText().toString().trim();
        if(val.isEmpty()){
            storePhone.setError("Phone Number Cannot be Empty");
            return false;
        }
        else {
            if(val.length()==10) {
                storePhone.setError(null);
                return true;
            }
            storePhone.setError("Invalid Phone Number");
            return false;
        }
    }

    public boolean validBusinessType(){
        String val = storeBusinessType.getText().toString().trim();
        if(val.isEmpty()){
            storeBusinessType.setError("Business Type Cannot be Empty");
            return false;
        }
        else {
            storeBusinessType.setError(null);
            return true;
        }
    }

    public boolean validPass(){
        String val = storePass.getText().toString().trim();
        if(val.isEmpty()){
            storePass.setError("Password Cannot be Empty");
            return false;
        }
        else {
            if(val.length()>=8) {
                storePass.setError(null);
                return true;
            }
            storePass.setError("Password should be more than or equal to 8 characters long");
            return false;
        }
    }

    public boolean validPass2(){
        String val = storePass.getText().toString().trim();
        String regex = "^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])" + "(?=.*[@#$%^&+=])" + "(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        if (val == null) {
            return false;
        }
        Matcher m = p.matcher(val);
        if(m.matches()==false){
            storePass.setError("Password should contain a Capital Letter, Number and A Special Character!");
        }
        return m.matches();
    }
}