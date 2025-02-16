package com.utkarsh.codelink;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;

public class StoreProfileActivity extends AppCompatActivity {

    EditText productName, manufacturingDate, expiryDate, price, quantity;
    Button generate, logout, saveDetails;
    ImageView qrImage, inventory, credits;
    TextView titleIdNumView, titleStoreNameView, inventoryText, creditsText, verifyText, returnText;
    FirebaseDatabase database;
    DatabaseReference reference;

    static String nameUser, nameStore, idnumStore, producttName, productExpiry,
            productManufacturing, productPrice, productQuantity;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_profile);

        productName = findViewById(R.id.product_name);
        manufacturingDate = findViewById(R.id.product_manufacturing);
        expiryDate = findViewById(R.id.product_expiry);
        price = findViewById(R.id.product_price);
        quantity = findViewById(R.id.product_quantity);
        generate = findViewById(R.id.generate_product_barcode_button);
        qrImage = findViewById(R.id.qrimage);
        titleIdNumView = findViewById(R.id.titleIDNum);
        titleStoreNameView = findViewById(R.id.titleStoreName);
        logout = findViewById(R.id.ProfileBack);
        saveDetails = findViewById(R.id.save_details_button);
        inventory = findViewById(R.id.InventoryPhoto);
        inventoryText = findViewById(R.id.InventoryText);
        credits = findViewById(R.id.CreditsPhoto);
        creditsText = findViewById(R.id.CreditsText);
        verifyText = findViewById(R.id.VerifyText);
        returnText = findViewById(R.id.ReturnText);

        Intent intent = getIntent();

        nameUser = intent.getStringExtra("name");
        nameStore = intent.getStringExtra("storeName");
        idnumStore = intent.getStringExtra("idnum");

        setData();

        verifyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreProfileActivity.this, VerifyActivity.class);
                startActivity(intent);
            }
        });

        returnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreProfileActivity.this, StoreReturnActivity.class);
                intent.putExtra("idnum", idnumStore);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(StoreProfileActivity.this, StoreLoginActivity.class);
                startActivity(intent);
                finishAffinity(); // Destroy all activities and return to login page
            }
        });

        inventoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(StoreProfileActivity.this, InventoryViewActivity.class);
                intent.putExtra("idnum", idnumStore);
                startActivity(intent);
            }
        });

        inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(StoreProfileActivity.this, InventoryViewActivity.class);
                intent.putExtra("idnum", idnumStore);
                startActivity(intent);
            }
        });

        creditsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(StoreProfileActivity.this, CreditsActivity.class);
                startActivity(intent);
            }
        });

        credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(StoreProfileActivity.this, CreditsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chatbot, menu);
        return true;
    }

    public void setData(){


//        Toast.makeText(this, idnumStore, Toast.LENGTH_SHORT).show();
        titleIdNumView.setText(idnumStore);
        titleStoreNameView.setText(nameStore);

        String greet="Welcome "+nameUser;
        Toast.makeText(StoreProfileActivity.this, greet, Toast.LENGTH_SHORT).show();

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(StoreProfileActivity.this, productName.getText().toString().trim(), Toast.LENGTH_SHORT).show();
                String allData = "Product Name:" + productName.getText().toString().trim() + " | MFD:" + manufacturingDate.getText().toString().trim() +
                        " | EXP:" + expiryDate.getText().toString().trim() + " | Price:" + price.getText().toString().trim();

                //        Bitmap bitmap;
                QRCodeWriter writer = new QRCodeWriter();
                try {
                    BitMatrix bitMatrix = writer.encode(allData, BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
                        }
                    }
                    qrImage.setImageBitmap(bitmap);
                    saveDetails.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            uploadData(bitmap);
                            Toast.makeText(StoreProfileActivity.this, "DATA UPLOADED SUCCESSFULLY!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_chatbot) {
            // Start the ChatbotActivity when the chatbot option is clicked
            Intent chatbotIntent = new Intent(StoreProfileActivity.this, ChatbotActivity.class);
            startActivity(chatbotIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void uploadData(Bitmap bitmap) {
        // Get a reference to the Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        producttName = productName.getText().toString().trim();
        // Create a unique filename for the image
        String filename = "BAR_" + idnumStore + producttName + ".jpg";

        // Create a reference to the image file in Firebase Storage
        StorageReference storageRef = storage.getReference().child("barCodes/" + filename);

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

        producttName = productName.getText().toString().trim();
        productManufacturing = manufacturingDate.getText().toString().trim();
        productExpiry = expiryDate.getText().toString().trim();
        productQuantity = quantity.getText().toString().trim();
        productPrice = price.getText().toString().trim();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("inventory");
        storeProfileHelper storeProfileHelper = new storeProfileHelper(producttName,productManufacturing,productExpiry, productQuantity, productPrice);
        reference.child(idnumStore+"_"+producttName).setValue(storeProfileHelper);
    }
}