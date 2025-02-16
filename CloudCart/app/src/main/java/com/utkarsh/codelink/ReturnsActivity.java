package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.FileDescriptor;
import java.io.IOException;

public class ReturnsActivity extends AppCompatActivity {

    EditText message;
    Button send, selectImageButton, takePhotoButton;
    ImageView imageView;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri selectedImageUri;
    String id;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_returns);

        message = findViewById(R.id.text_message);
        send = findViewById(R.id.send_button);
        selectImageButton = findViewById(R.id.select_image_button);
        takePhotoButton = findViewById(R.id.take_image_button);
        imageView = findViewById(R.id.image_view);

//        send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(ReturnsActivity.this, "Select Image First!", Toast.LENGTH_SHORT).show();
//            }
//        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getText().toString().isEmpty()){
                    Toast.makeText(ReturnsActivity.this, "Enter Message! Message cannot be Empty", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, "Select an Image"), REQUEST_IMAGE_PICK);
//                    id = getIntent().getStringExtra("ID");
//                    storeeId = getIntent().getStringExtra("StoreeeID");
                    Bundle bundle = getIntent().getExtras();
                    id = bundle.getString("ID");

                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String subject = "Return Request from ID Number: '"+id+"'";
                            String recipient = "teamcloudcart@gmail.com";

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("requests");
                            ReturnsHelperClass returnsHelperClass = new ReturnsHelperClass(message.getText().toString());
                            reference.child(id).setValue(returnsHelperClass);
                            Toast.makeText(ReturnsActivity.this, "Request raised successfully!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setData(Uri.parse("mailto:"));
                            intent.setType("*/*"); // set MIME type to accept any file type
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            intent.putExtra(Intent.EXTRA_TEXT, message.getText().toString().trim());
                            if (selectedImageUri != null) { // check if an image has been selected
                                intent.putExtra(Intent.EXTRA_STREAM, selectedImageUri); // attach image as a stream
                                try {
                                    startActivity(Intent.createChooser(intent, "Choose an Email Client!"));
                                } catch (Exception e) {
                                    Toast.makeText(ReturnsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(ReturnsActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
                            }

                            final String[] storeId = {""};
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                            Query checkUserDatabase1 = ref.orderByChild("idnum").equalTo(id);
                            checkUserDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    storeId[0] = snapshot.child(id).child("storeID").getValue(String.class);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                            final String[] storePhoneFromDB = {""};
                            if(ContextCompat.checkSelfPermission(ReturnsActivity.this, Manifest.permission.SEND_SMS)
                                    ==PackageManager.PERMISSION_GRANTED){
//                                Toast.makeText(ReturnsActivity.this, id, Toast.LENGTH_SHORT).show();
                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("stores");
                                Query checkUserDatabase2 = ref1.orderByValue();
                                checkUserDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        storePhoneFromDB[0] = snapshot1.child(storeId[0].trim()).child("phone").getValue(String.class);
//                                        Toast.makeText(ReturnsActivity.this, storePhoneFromDB[0], Toast.LENGTH_SHORT).show();
                                        sendSMS(storePhoneFromDB[0]);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else{
                                ActivityCompat.requestPermissions(ReturnsActivity.this, new String[]{Manifest.permission.SEND_SMS}, 100);
                            }
                        }

                        private void sendSMS(String s) {
                            // Get the default instance of SmsManager
                            final SmsManager smsManager = SmsManager.getDefault();

                            // Set the phone number and message to send
                            String message = "New Return Request from ID Number : "+id;

                            // Send the message
                            try {
                                smsManager.sendTextMessage(s, null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getText().toString().isEmpty()){
                    Toast.makeText(ReturnsActivity.this, "Enter Message! Message cannot be Empty", Toast.LENGTH_SHORT).show();
                }else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    id = getIntent().getStringExtra("ID");
                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            id = getIntent().getStringExtra("ID");
                            String subject = "Return Request from ID Number: '"+id+"'";
                            String recipient = "teamcloudcart@gmail.com";

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("requests");
                            ReturnsHelperClass returnsHelperClass = new ReturnsHelperClass(message.getText().toString());
                            reference.child(id).setValue(returnsHelperClass);
                            Toast.makeText(ReturnsActivity.this, "Request raised successfully!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setData(Uri.parse("mailto:"));
                            intent.setType("*/*"); // set MIME type to accept any file type
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            intent.putExtra(Intent.EXTRA_TEXT, message.getText().toString().trim());
                            if (imageView.getDrawable() != null) {
                                imageView.setDrawingCacheEnabled(true);
                                Bitmap bitmap = imageView.getDrawingCache();
                                Uri uri = getImageUri(getApplicationContext(), bitmap);
                                intent.putExtra(Intent.EXTRA_STREAM, uri); // attach image as a stream
                                try {
                                    startActivity(Intent.createChooser(intent, "Choose an Email Client!"));
                                } catch (Exception e) {
                                    Toast.makeText(ReturnsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else{
                                Toast.makeText(ReturnsActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
                            }

                            final String[] storeId = {""};
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                            Query checkUserDatabase1 = ref.orderByChild("idnum").equalTo(id);
                            checkUserDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    storeId[0] = snapshot.child(id).child("storeID").getValue(String.class);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                            final String[] storePhoneFromDB = {""};
                            if(ContextCompat.checkSelfPermission(ReturnsActivity.this, Manifest.permission.SEND_SMS)
                                    ==PackageManager.PERMISSION_GRANTED){
//                                Toast.makeText(ReturnsActivity.this, id, Toast.LENGTH_SHORT).show();
                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("stores");
                                Query checkUserDatabase2 = ref1.orderByValue();
                                checkUserDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        storePhoneFromDB[0] = snapshot1.child(storeId[0].trim()).child("phone").getValue(String.class);
                                        Toast.makeText(ReturnsActivity.this, storePhoneFromDB[0], Toast.LENGTH_SHORT).show();
                                        sendSMS(storePhoneFromDB[0]);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else{
                                ActivityCompat.requestPermissions(ReturnsActivity.this, new String[]{Manifest.permission.SEND_SMS}, 100);
                            }
                        }
                        private void sendSMS(String s) {
                            // Get the default instance of SmsManager
                            final SmsManager smsManager = SmsManager.getDefault();

                            // Set the phone number and message to send
                            String message = "New Return Request from ID Number : "+id;

                            // Send the message
                            try {
                                smsManager.sendTextMessage(s, null, message, null, null);
                                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        if (path != null) {
            return Uri.parse(path);
        } else {
            // Handle the case where the path is null
            // You can return null or throw an exception, depending on your requirements
            return null;
        }
    }

    // method to retrieve the image selected by the user and save its URI in a variable
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data.getData()!=null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            Bitmap image = null;
            try {
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(selectedImageUri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            String filename = "Return_Photo_" + id + ".jpg";
            StorageReference storageRef = storage.getReference().child("returnPhotos/" + filename);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data1 = baos.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data1);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get the download URL for the image
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                        }
                    });
                }
            });
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // handle image captured by camera
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            String filename = "Return_Photo_" + id + ".jpg";
            StorageReference storageRef = storage.getReference().child("returnPhotos/" + filename);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data1 = baos.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data1);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get the download URL for the image
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                        }
                    });
                }
            });
        }
    }
}