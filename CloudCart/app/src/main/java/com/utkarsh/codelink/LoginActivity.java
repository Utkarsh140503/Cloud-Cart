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

public class LoginActivity extends AppCompatActivity {

    EditText loginIdnumber, loginPassword;
    Button loginButton;
    TextView signupRedirectText, contactusRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginIdnumber = findViewById(R.id.login_idnum);
        loginPassword = findViewById(R.id.login_pass);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        contactusRedirectText = findViewById(R.id.contactusRedirectText);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateIdnumber() | !validatePassword()){

                }else{
                    checkUser();
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        contactusRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });

        }

    public Boolean validateIdnumber(){
        String val = loginIdnumber.getText().toString().trim();
        if(val.isEmpty()){
            loginIdnumber.setError("Username cannot be empty");
            return false;
        }
        else {
            loginIdnumber.setError(null);
            return true;
        }
    }

    public Boolean validatePassword(){
        String val = loginPassword.getText().toString().trim();
        if(val.isEmpty()){
            loginPassword.setError("Password cannot be empty");
            return false;
        }
        else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String userIdnumber = loginIdnumber.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("idnum").equalTo(userIdnumber);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    loginIdnumber.setError(null);
                    String passwordFromDB = snapshot.child(userIdnumber).child("pass").getValue(String.class);
//
                    if(passwordFromDB.equals(userPassword)){
                        String nameFromDB = snapshot.child(userIdnumber).child("name").getValue(String.class);
                        loginIdnumber.setError(null);

                        //Pass the data using intent

//                        String nameFromDB = snapshot.child(userIdnumber).child("name").getValue(String.class);
                        String emailFromDB = snapshot.child(userIdnumber).child("email").getValue(String.class);
                        String upiFromDB = snapshot.child(userIdnumber).child("upi").getValue(String.class);
                        String idtypeFromDB = snapshot.child(userIdnumber).child("idtype").getValue(String.class);
                        String idnumFromDB = snapshot.child(userIdnumber).child("idnum").getValue(String.class);

                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);

                        intent.putExtra("name", nameFromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("upi", upiFromDB);
                        intent.putExtra("idtype", idtypeFromDB);
                        intent.putExtra("idnum", idnumFromDB);
                        intent.putExtra("pass", passwordFromDB);

                        startActivity(intent);
                    }else {
                        loginPassword.setError("Invalid Credentials");
                        loginPassword.requestFocus();
                    }
                }else {
                    loginIdnumber.setError("User does not exist");
                    loginIdnumber.requestFocus();
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}