package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CreditsActivity extends AppCompatActivity {

    EditText creditIdNum, creditCount;
    Button addCreditButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        creditIdNum = findViewById(R.id.creditsIDNum);
        creditCount = findViewById(R.id.creditsCount);
        addCreditButton = findViewById(R.id.addCreditsButton);

        addCreditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateIdnumber()){

                }
                else {
                    checkUser();
                }
            }
        });

    }

    public Boolean validateIdnumber(){
        String val = creditIdNum.getText().toString().trim();
        if(val.isEmpty()){
            creditIdNum.setError("ID Number cannot be empty");
            return false;
        }
        else {
            creditIdNum.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String userIdnumber = creditIdNum.getText().toString();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("idnum").equalTo(userIdnumber);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(!creditCount.getText().toString().isEmpty()){
                        creditIdNum.setError(null);
                        String credits = snapshot.child(creditIdNum.getText().toString()).child("Purchase Credits").child("credits").getValue(String.class);

                        try {
                            if (credits.isEmpty()) {
                                CreditsHelperClass creditsHelperClass = new CreditsHelperClass(creditCount.getText().toString());
                                reference.child(userIdnumber).child("Purchase Credits").setValue(creditsHelperClass);
                            } else {
                                double c = Double.parseDouble(credits);
                                double c1 = Double.parseDouble(creditCount.getText().toString());
                                double totalC = c + c1;
                                CreditsHelperClass creditsHelperClass = new CreditsHelperClass(totalC + "");
                                reference.child(userIdnumber).child("Purchase Credits").setValue(creditsHelperClass);
                            }
                        }catch(Exception e){
                            CreditsHelperClass creditsHelperClass = new CreditsHelperClass(creditCount.getText().toString());
                            reference.child(userIdnumber).child("Purchase Credits").setValue(creditsHelperClass);
                        }
//                        Query checkUserDatabase1 = reference.orderByChild("credits");



                        Toast.makeText(CreditsActivity.this, "Credits Added to Account Successfully!", Toast.LENGTH_SHORT).show();
                    }else{
                        creditCount.setError("Credit Count cannot be Empty!");
                    }

                }
                else{
                    creditIdNum.setError("User Does not Exist");
                    creditIdNum.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}