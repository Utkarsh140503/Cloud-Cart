package com.utkarsh.codelink;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StartUp extends AppCompatActivity {

    ImageView admin, user;
    TextView admintext, usertext;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        admin = findViewById(R.id.startupAdmin);
        user = findViewById(R.id.startupUser);
        admintext = findViewById(R.id.startupAdminText);
        usertext = findViewById(R.id.startupUserText);

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartUp.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartUp.this, StoreSignupActivity.class);
                startActivity(intent);
            }
        });

        usertext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartUp.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        admintext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartUp.this, StoreSignupActivity.class);
                startActivity(intent);
            }
        });

    }
}