package com.utkarsh.codelink;

import androidx.annotation.NonNull;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUPI, signupIdtype, signupIdnum, signupPass, storeID;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    String name;
    String email;
    String upiid;
    String idtype;
    String idnumber;
    String password;
    String storeIDNum;

    @SuppressLint({"WrongViewCast", "CutPasteId", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUPI = findViewById(R.id.signup_upi);
        signupIdtype = findViewById(R.id.signup_idType);
        signupIdnum = findViewById(R.id.signup_idnum);
        signupPass = findViewById(R.id.signup_pass);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        storeID = findViewById(R.id.store_idnum);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(!validEmail() | !validName() | !validIDType() | !validIDNum() | !validUPI() | !validPass() | !validStoreIDNum()){

                 }
                 else{
                     setData();
                 }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void setData(){

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        name = signupName.getText().toString().trim();
        email = signupEmail.getText().toString().trim();
        upiid = signupUPI.getText().toString().trim();
        idtype = signupIdtype.getText().toString().trim();
        idnumber = signupIdnum.getText().toString().trim();
        password = signupPass.getText().toString().trim();
        storeIDNum = storeID.getText().toString().trim();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateAndTime =  sdf.format(new Date());

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("stores");
        Query checkUserDatabase1 = reference1.orderByChild("idnumber").equalTo(storeIDNum);
        checkUserDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                if(snapshot1.exists()){
                    String alldata = "Name:" + name + " Email:" + email + " UPI ID:" + upiid +
                            " IDType:" + idtype + " IDNum:" + idnumber + " Pass:" + password + "StoreID:" + storeIDNum;
                    QRGEncoder qrgEncoder = new QRGEncoder(alldata, null, QRGContents.Type.TEXT, 1000);
                    Bitmap qrBits = qrgEncoder.getBitmap();

                    HelperClass helperClass = new HelperClass(name, email, upiid, idtype, idnumber, password, storeIDNum, currentDateAndTime);
                    reference.child(idnumber).setValue(helperClass);

                    Toast.makeText(SignupActivity.this, "Welcome to the Headless Transaction Club!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                }else{
                    storeID.setError("Invalid Store ID");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean validName(){
        String val = signupName.getText().toString().trim();
        if(val.isEmpty()){
            signupName.setError("Name Cannot be Empty");
            return false;
        }
        else {
            boolean x = ((val != null) && (!val.equals(""))
                    && (val.matches("^[a-zA-Z ]*$")));
            if(x==false){
                signupName.setError("Name can only have Alphabets");
                return false;
            }
            signupName.setError(null);
            return true;
        }
    }

    public boolean validEmail(){
        String val = signupEmail.getText().toString().trim();
        if(val.isEmpty()){
            signupEmail.setError("Email cannot be empty");
            return false;
        }
        else {
            if(val.endsWith("@gmail.com")) {
                signupEmail.setError(null);
                return true;
            }
            signupEmail.setError("Email does not end with '@gmail.com'");
            return false;
        }
    }

    public boolean validUPI(){
        String val = signupUPI.getText().toString().trim();
        if(val.isEmpty()){
            signupUPI.setError("UPI ID cannot be empty");
            return false;
        }
        else {
            if(val.contains("@")){
                signupUPI.setError(null);
                return true;
            }
            signupUPI.setError("UPI does not contains '@' symbol");
            return false;
        }
    }

    public boolean validIDType(){
        String val = signupIdtype.getText().toString().trim();
        if(val.isEmpty()){
            signupIdtype.setError("ID Type cannot be empty");
            return false;
        }
        else {
            boolean x = ((val != null) && (!val.equals(""))
                    && (val.matches("^[a-zA-Z ]*$")));
            if(x==false){
                signupIdtype.setError("ID Type can only have Alphabets");
                return false;
            }
            signupIdtype.setError(null);
            return true;
        }
    }

    public boolean validIDNum(){
        String val = signupIdnum.getText().toString().trim();
        if(val.isEmpty()){
            signupIdnum.setError("ID Number cannot be empty");
            return false;
        }
        else {
            boolean x = ((val != null) && (!val.equals(""))
                    && (val.matches("^[a-zA-Z0-9]*$")));
            if(x==false){
                signupIdnum.setError("ID Number can only have Alphanumeric");
                return false;
            }
            signupIdnum.setError(null);
            return true;
        }
    }

    public boolean validPass(){
        String val = signupPass.getText().toString().trim();
        if(val.isEmpty()){
            signupPass.setError("Password cannot be empty");
            return false;
        }
        else {
            if(val.length()>=8) {
                signupPass.setError(null);
                return true;
            }
            signupPass.setError("Password should be more than or equal to 8 characters long");
            return false;
        }
    }

    public boolean validStoreIDNum(){
        String val = storeID.getText().toString().trim();
        if(val.isEmpty()){
            storeID.setError("Store ID Number cannot be empty");
            return false;
        }
        else {
            boolean x = ((val != null) && (!val.equals(""))
                    && (val.matches("^[a-zA-Z0-9]*$")));
            if(x==false){
                storeID.setError("Store ID Number can only have Alphanumeric");
                return false;
            }
            storeID.setError(null);
            return true;
        }
    }
}