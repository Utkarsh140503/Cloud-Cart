package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class InventoryViewActivity extends AppCompatActivity {

    EditText productName;
    TextView nameView, priceView, quantityView, mfdView, expView;
    ImageView barcodeImage;
    Button search, logout, sales;
    Bitmap bitmap;
    String IDNum;
//    FirebaseDatabase database;
//    DatabaseReference reference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_view);

        productName = findViewById(R.id.productNameSearch);
//        IDNum = findViewById(R.id.productIDNum);
        nameView = findViewById(R.id.productNameView);
        priceView = findViewById(R.id.productPriceView);
        quantityView = findViewById(R.id.productQuantityView);
        mfdView = findViewById(R.id.productMFDView);
        expView = findViewById(R.id.productEXPView);
        barcodeImage = findViewById(R.id.productBarcodeView);
        search = findViewById(R.id.productSearchButton);
        logout = findViewById(R.id.logout);
        sales = findViewById(R.id.salesButton);

        Intent intent = getIntent();
        IDNum = intent.getStringExtra("idnum");


        showUserData();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryViewActivity.this, StoreLoginActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(InventoryViewActivity.this, SalesViewActivity.class);
                intent1.putExtra("idnum", IDNum);
                startActivity(intent1);
            }
        });

    }

    String quantityFromDB, priceFromDB, expiryFromDB, manufFromDB, nameFromDB;

    public void showUserData(){
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameProduct = productName.getText().toString().trim();
                String IDProduct = IDNum;
                if(IDProduct.isEmpty()) {
//                    IDNum.setError("Field Cannot be Empty");
                    Toast.makeText(InventoryViewActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                }
//                else if (nameProduct.isEmpty()) {
//                    productName.setError("Field Cannot be Empty");
//                }
                else{
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("stores");
                    Query checkInventoryDatabase1 = reference1.orderByChild("idnumber").equalTo(IDProduct);

//                Toast.makeText(InventoryViewActivity.this, nameProduct, Toast.LENGTH_SHORT).show();

                        checkInventoryDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    if (nameProduct.isEmpty()) {
                                        productName.setError("Field Cannot be Empty");
                                    } else {
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("inventory");
                                        Query checkInventoryDatabase = reference.orderByChild("producttName").equalTo(nameProduct);

                                        checkInventoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists()) {
                                                    nameFromDB = snapshot.child(IDProduct + "_" + nameProduct).child("producttName").getValue(String.class);
                                                    Toast.makeText(InventoryViewActivity.this, nameFromDB, Toast.LENGTH_SHORT).show();
                                                    if (nameFromDB.equals(nameProduct)) {
                                                        quantityFromDB = snapshot.child(IDProduct + "_" + nameProduct).child("productQuantity").getValue(String.class);
                                                        priceFromDB = snapshot.child(IDProduct + "_" + nameProduct).child("productPrice").getValue(String.class);
                                                        expiryFromDB = snapshot.child(IDProduct + "_" + nameProduct).child("productExpiry").getValue(String.class);
                                                        manufFromDB = snapshot.child(IDProduct + "_" + nameProduct).child("productManufacturing").getValue(String.class);


                                                        // First, get a reference to the Firebase Storage instance
                                                        FirebaseStorage storage = FirebaseStorage.getInstance();

                                                        // Then, get a reference to the "barCodes" folder in the storage
                                                        StorageReference storageRef = storage.getReference().child("barCodes");

                                                        // Finally, get a reference to the specific image you want to extract
                                                        StorageReference imageRef = storageRef.child("BAR_" + IDProduct + nameProduct + ".jpg");

                                                        // Download the image as a byte array
                                                        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                            @Override
                                                            public void onSuccess(byte[] bytes) {
                                                                // Convert the byte array to a Bitmap object
                                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                                                // Use the Bitmap object however you need to (e.g. display it in an ImageView)
                                                                barcodeImage.setImageBitmap(bitmap);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                // Handle any errors that occur while downloading the image
                                                            }
                                                        });


                                                        nameView.setText(nameFromDB);
                                                        quantityView.setText(quantityFromDB);
                                                        priceView.setText(priceFromDB);
                                                        expView.setText(expiryFromDB);
                                                        mfdView.setText(manufFromDB);
                                                    }
                                                } else {
                                                    Toast.makeText(InventoryViewActivity.this, "Try Rechecking ID Number", Toast.LENGTH_SHORT).show();
                                                    productName.setError("Product does not exist");
                                                    productName.requestFocus();
                                                    nameView.setText("");
                                                    quantityView.setText("");
                                                    priceView.setText("");
                                                    expView.setText("");
                                                    mfdView.setText("");
                                                    barcodeImage.setImageBitmap(bitmap);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                    }
                                }else {
//                                    IDNum.setError("No store registered with the given ID");
                                    Toast.makeText(InventoryViewActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                }
            }
        });
    }
}