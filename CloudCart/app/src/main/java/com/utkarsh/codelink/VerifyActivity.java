package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class VerifyActivity extends AppCompatActivity {

    EditText IdNum, OTP;
    Button verify;
    Dialog dialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        IdNum = findViewById(R.id.AccountIDNum);
        verify = findViewById(R.id.verify_button);
        OTP = findViewById(R.id.OTP);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                Query checkUserDatabase = reference.orderByChild("idnum").equalTo(IdNum.getText().toString());
                checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String OTPFromDB = snapshot.child(IdNum.getText().toString()).child("TransactionID").child("transID").getValue(String.class);
                            if(OTPFromDB.equals(OTP.getText().toString())){
//                                Toast.makeText(VerifyActivity.this, "Thankyou for shopping! Visit Again.", Toast.LENGTH_SHORT).show();
                                dialog = new Dialog(VerifyActivity.this);
                                dialog.setContentView(R.layout.custom_dialog1);
                                dialog.show();
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
                                }
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.setCancelable(false);
                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

                                Button okay = dialog.findViewById(R.id.btn_okay);
                                Button cancel = dialog.findViewById(R.id.btn_cancel);

                                okay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });

                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });

                                TransIdHelper transIdHelper = new TransIdHelper("");
                                reference.child(IdNum.getText().toString()).child("TransactionID").setValue(transIdHelper);
                            }else{
                                Toast.makeText(VerifyActivity.this, "OTP Not matched! Try again or contact store.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            IdNum.setError("User Not Found!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}