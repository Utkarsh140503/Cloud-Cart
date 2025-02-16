package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class StoreLoginActivity extends AppCompatActivity {

    EditText storeLoginIdnumber, storeLoginPassword;
    Button storeLoginButton;
    TextView storeSignupRedirectText, storeContactusRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_login);

        storeLoginIdnumber = findViewById(R.id.store_login_idnum);
        storeLoginPassword = findViewById(R.id.store_login_pass);
        storeSignupRedirectText = findViewById(R.id.store_signupRedirectText);
        storeContactusRedirectText = findViewById(R.id.store_contactusRedirectText);
        storeLoginButton = findViewById(R.id.store_login_button);

        storeLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateIdnumber() | !validatePassword()){

                }else{
                    checkUser();
                }
            }
        });

        storeSignupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreLoginActivity.this, StoreSignupActivity.class);
                startActivity(intent);
            }
        });

        storeContactusRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreLoginActivity.this, StoreContactActivity.class);
                startActivity(intent);
            }
        });
    }

    public Boolean validateIdnumber(){
        String val = storeLoginIdnumber.getText().toString().trim();
        if(val.isEmpty()){
            storeLoginIdnumber.setError("Username cannot be empty");
            return false;
        }
        else {
            storeLoginIdnumber.setError(null);
            return true;
        }
    }

    public Boolean validatePassword(){
        String val = storeLoginPassword.getText().toString().trim();
        if(val.isEmpty()){
            storeLoginPassword.setError("Password cannot be empty");
            return false;
        }
        else {
            storeLoginPassword.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String storeUerIdnumber = storeLoginIdnumber.getText().toString().trim();
        String storeUserPassword = storeLoginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("stores");
        Query checkUserDatabase = reference.orderByChild("idnumber").equalTo(storeUerIdnumber);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    storeLoginIdnumber.setError(null);
                    String passwordFromDB = snapshot.child(storeUerIdnumber).child("password").getValue(String.class);
//
                    if(passwordFromDB.equals(storeUserPassword)){
                        String nameFromDB = snapshot.child(storeUerIdnumber).child("name").getValue(String.class);
                        storeLoginIdnumber.setError(null);

                        //Pass the data using intent

//                        String nameFromDB = snapshot.child(userIdnumber).child("name").getValue(String.class);
                        String emailFromDB = snapshot.child(storeUerIdnumber).child("email").getValue(String.class);
                        String businessTypeFromDB = snapshot.child(storeUerIdnumber).child("businessType").getValue(String.class);
                        String phoneFromDB = snapshot.child(storeUerIdnumber).child("phone").getValue(String.class);
                        String storeNameFromDB = snapshot.child(storeUerIdnumber).child("storeName").getValue(String.class);
                        String idnumFromDB = snapshot.child(storeUerIdnumber).child("idnumber").getValue(String.class);


                        Intent intent = new Intent(StoreLoginActivity.this, StoreProfileActivity.class);

                        intent.putExtra("name", nameFromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("businessType", businessTypeFromDB);
                        intent.putExtra("phone", phoneFromDB);
                        intent.putExtra("idnum", idnumFromDB);
                        intent.putExtra("pass", passwordFromDB);
                        intent.putExtra("storeName", storeNameFromDB);

                        startActivity(intent);
                    }else {
                        storeLoginPassword.setError("Invalid Credentials");
                        storeLoginPassword.requestFocus();
                    }
                }else {
                    storeLoginIdnumber.setError("User does not exist");
                    storeLoginIdnumber.requestFocus();
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}