package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class ProfileActivity extends AppCompatActivity {
    static String idnumUser, nameUser, upiUser;
    TextView profileUpi, profilePass, profileIdtype, profileIdnum;
    TextView titleName, titleEmail, billingText, returnsText;
    ImageView qrImage, billing, returns;
    Button ProfileBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileUpi = findViewById(R.id.profileUPI);
        profilePass = findViewById(R.id.profilePass);
        profileIdtype = findViewById(R.id.profileIdType);
        profileIdnum = findViewById(R.id.profileIdNum);
        titleName = findViewById(R.id.titleName);
        titleEmail = findViewById(R.id.titleEmail);
        ProfileBack = findViewById(R.id.ProfileBack);
//        paymentRedirect = findViewById(R.id.profileMakePayment);
        qrImage = findViewById(R.id.qrimage);
        billing = findViewById(R.id.BillingPhoto);
        billingText = findViewById(R.id.BillingText);
        returns = findViewById(R.id.ReturnPhoto);
        returnsText = findViewById(R.id.ReturnText);

        showUserData();

        ProfileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                Toast.makeText(ProfileActivity.this, "Logged Out Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finishAffinity();
            }
        });

        billing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ProfileActivity.this, BillingActivity.class);
                intent.putExtra("ID", idnumUser);
                startActivity(intent);
            }
        });

        billingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ProfileActivity.this, BillingActivity.class);
                intent.putExtra("ID", idnumUser);
                startActivity(intent);
            }
        });

        returns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                final String[] storeIDFromDB = new String[1];
                DatabaseReference ref3 = FirebaseDatabase.getInstance().getReference("users");
                Query checkUserDatabase3 = ref3.orderByChild("idnum").equalTo(idnumUser);
                checkUserDatabase3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot3) {
                        if(snapshot3.exists()){
                            storeIDFromDB[0] = snapshot3.child(idnumUser).child("storeID").getValue(String.class);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                Intent intent = new Intent(ProfileActivity.this, ReturnsActivity.class);
                intent.putExtra("ID", idnumUser);
                intent.putExtra("StoreID",storeIDFromDB[0]);
                startActivity(intent);
            }
        });

        returnsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                final String[] storeIDFromDB = new String[1];
                DatabaseReference ref3 = FirebaseDatabase.getInstance().getReference("users");
                Query checkUserDatabase3 = ref3.orderByChild("idnum").equalTo(idnumUser);
                checkUserDatabase3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot3) {
                        if(snapshot3.exists()){
                            storeIDFromDB[0] = snapshot3.child(idnumUser).child("storeID").getValue(String.class);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                Intent intent = new Intent(ProfileActivity.this, ReturnsActivity.class);
                intent.putExtra("ID", idnumUser);
                intent.putExtra("StoreID",storeIDFromDB[0]);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chatbot, menu);
        return true;
    }

    public void  showUserData(){

        Intent intent = getIntent();

        nameUser = intent.getStringExtra("name");
        String emailUser = intent.getStringExtra("email");
        upiUser = intent.getStringExtra("upi");
        String idtypeUser = intent.getStringExtra("idtype");
        idnumUser = intent.getStringExtra("idnum");
        String passUser = intent.getStringExtra("pass");

        String alldata = "Name:" + nameUser + " Email:" + emailUser + " UPI ID:" + upiUser +
                " IDType:" + idtypeUser + " IDNum:" + idnumUser + " Pass:" + passUser;
        QRGEncoder qrgEncoder = new QRGEncoder(alldata, null, QRGContents.Type.TEXT, 1000);
        Bitmap qrBits = qrgEncoder.getBitmap();
        qrImage.setImageBitmap(qrBits);

        String greet="Welcome "+nameUser;
        Toast.makeText(ProfileActivity.this, greet, Toast.LENGTH_SHORT).show();

        titleName.setText(nameUser);
        titleEmail.setText(emailUser);
        profileUpi.setText(upiUser);
        profileIdtype.setText(idtypeUser);
        profileIdnum.setText(idnumUser);
        profilePass.setText(passUser);

        uploadImage(qrBits);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_chatbot) {
            // Start the ChatbotActivity when the chatbot option is clicked
            Intent chatbotIntent = new Intent(ProfileActivity.this, ChatbotActivity.class);
            startActivity(chatbotIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    void uploadImage(Bitmap bitmap) {
        // Get a reference to the Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a unique filename for the image
        String filename = "QR_" + idnumUser + ".jpg";

        // Create a reference to the image file in Firebase Storage
        StorageReference storageRef = storage.getReference().child("qrCodes/" + filename);

        // Convert the bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to Firebase Storage
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the download URL for the image
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Save the image URL to Firebase Database
//                        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//                        String key = database.child("images").push().getKey();
//                        database.child("images").child(key).setValue(uri.toString());
                    }
                });
            }
        });
    }

}