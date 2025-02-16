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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class StoreReturnActivity extends AppCompatActivity {

    EditText regNo, nameProduct;
    Button viewReq, accept, reject;
    ImageView img;
    TextView tv;
    String email, priceFromDB, currentCredits, IDNum;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_return);

        regNo = findViewById(R.id.return_idnum);
        viewReq = findViewById(R.id.retrieve_button);
        img = findViewById(R.id.RequestImage);
        tv = findViewById(R.id.emailContent);
        accept = findViewById(R.id.accept_request);
        reject = findViewById(R.id.reject_request);
        nameProduct = findViewById(R.id.product_name);

        Intent intent = getIntent();
        IDNum = intent.getStringExtra("idnum");

        viewReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                StorageReference photoRef = storageRef.child("returnPhotos/Return_Photo_"+regNo.getText().toString().trim()+".jpg");

                final long ONE_MEGABYTE = 1024 * 1024;
                photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Decode the byte array into a Bitmap
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        // Use the Bitmap to display the image in your UI
                        img.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

                final String[] text = new String[1];

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("requests");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        text[0] = snapshot.child(regNo.getText().toString().trim()).child("emailText").getValue(String.class);
                        tv.setText(text[0]);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                StorageReference storageRef1 = FirebaseStorage.getInstance().getReference();
                StorageReference photoRef1 = storageRef.child("returnPhotos/Return_Photo_"+regNo.getText().toString().trim()+".jpg");

                final boolean[] exist = {false};

                photoRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        exist[0] = true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoreReturnActivity.this, "No Data found!", Toast.LENGTH_SHORT).show();
                        // The file does not exist or an error occurred
                    }
                });

                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("users");
                        Query checkInventoryDatabase1 = reference1.orderByChild("idnum").equalTo(regNo.getText().toString().trim());
                        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("inventory");
                        Query checkInventoryDatabase2 = reference2.orderByChild("producttName").equalTo(nameProduct.getText().toString().trim());
                        if(text[0]!=null && exist[0]==true) {
                            checkInventoryDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                    if (snapshot2.exists()) {
                                        priceFromDB = snapshot2.child(IDNum + "_" + nameProduct.getText().toString().trim())
                                                .child("productPrice").getValue(String.class);

                                        checkInventoryDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                                if (snapshot1.exists()) {
                                                    email = snapshot1.child(regNo.getText().toString().trim()).child("email").getValue(String.class);
                                                    currentCredits = snapshot1.child(regNo.getText().toString().trim())
                                                            .child("Purchase Credits").child("credits").getValue(String.class);
                                                    //
                                                    //                                Toast.makeText(StoreReturnActivity.this, email, Toast.LENGTH_SHORT).show();
                                                    try {
                                                        String senderEmail = "teamcloudcart@gmail.com";
                                                        String stringReceiverEmail = email;
                                                        String stringPasswordSenderEmail = "uzhbpbmtryaanzlt";

                                                        String stringHost = "smtp.gmail.com";

                                                        Properties properties = System.getProperties();

                                                        properties.put("mail.smtp.host", stringHost);
                                                        properties.put("mail.smtp.port", "465");
                                                        properties.put("mail.smtp.ssl.enable", "true");
                                                        properties.put("mail.smtp.auth", "true");

                                                        Session session = Session.getInstance(properties, new Authenticator() {
                                                            @Override
                                                            protected PasswordAuthentication getPasswordAuthentication() {
                                                                return new PasswordAuthentication(senderEmail, stringPasswordSenderEmail);
                                                            }
                                                        });

                                                        MimeMessage mimeMessage = new MimeMessage(session);

                                                        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

                                                        mimeMessage.setSubject("Message from CloudCart Team");
                                                        mimeMessage.setText("Hello Shopper! \nThis Mail is in regards to the Return/Refund request raised for your purchase. \nWe sincerely regret the problems faced with the product!\nWe'll try to improve our services!\n\nAs a token for your coorporation, we have credited the Credits worth the product price to your account!\n\nSee you soon!\nTeam CloudCart");

                                                        Thread thread = new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    Transport.send(mimeMessage);
                                                                } catch (MessagingException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });

                                                        thread.start();
                                                        Toast.makeText(StoreReturnActivity.this, "Information Mail sent to the user!", Toast.LENGTH_SHORT).show();

                                                        reference1.child(regNo.getText().toString().trim())
                                                                .child("Purchase Credits").child("credits")
                                                                .setValue((Double.parseDouble(priceFromDB) + Double.parseDouble(currentCredits)) + "");

                                                        reference.child(regNo.getText().toString().trim()).child("emailText").setValue(null);

                                                        photoRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // File deleted successfully
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // An error occurred while deleting the file
                                                            }
                                                        });
                                                        img.setVisibility(View.GONE);


                                                    } catch (AddressException e) {
                                                        e.printStackTrace();
                                                    } catch (MessagingException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });

                                    }else{
                                        Toast.makeText(StoreReturnActivity.this, "Wrong product name", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                        }else{
                            Toast.makeText(StoreReturnActivity.this, "No requests found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("users");
                        Query checkInventoryDatabase1 = reference1.orderByChild("idnum").equalTo(regNo.getText().toString().trim());
                        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("inventory");
                        Query checkInventoryDatabase2 = reference2.orderByChild("producttName").equalTo(nameProduct.getText().toString().trim());
                        if(text[0]!=null && exist[0]==true) {
                            checkInventoryDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                    if (snapshot1.exists()) {
                                        email = snapshot1.child(regNo.getText().toString().trim()).child("email").getValue(String.class);

                                        try {
                                            String senderEmail = "teamcloudcart@gmail.com";
                                            String stringReceiverEmail = email;
                                            String stringPasswordSenderEmail = "uzhbpbmtryaanzlt";

                                            String stringHost = "smtp.gmail.com";

                                            Properties properties = System.getProperties();

                                            properties.put("mail.smtp.host", stringHost);
                                            properties.put("mail.smtp.port", "465");
                                            properties.put("mail.smtp.ssl.enable", "true");
                                            properties.put("mail.smtp.auth", "true");

                                            Session session = Session.getInstance(properties, new Authenticator() {
                                                @Override
                                                protected PasswordAuthentication getPasswordAuthentication() {
                                                    return new PasswordAuthentication(senderEmail, stringPasswordSenderEmail);
                                                }
                                            });

                                            MimeMessage mimeMessage = new MimeMessage(session);

                                            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

                                            mimeMessage.setSubject("Message from CloudCart Team");
                                            mimeMessage.setText("Hello Shopper! \nThis Mail is in regards to the Return/Refund request raised for your purchase. \nAfter Reviewing the request, we have found no defects in the product sold.\nFor further assistance or queries, you can mail us on\n\n'teamcloudcart@gmail.com'\n\nThankyou for your coorporation!\n\nSee you soon!\nTeam CloudCart");

                                            Thread thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Transport.send(mimeMessage);
                                                    } catch (MessagingException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });

                                            thread.start();
                                            Toast.makeText(StoreReturnActivity.this, "Information Mail sent to the user!", Toast.LENGTH_SHORT).show();

                                            reference.child(regNo.getText().toString().trim()).child("emailText").setValue(null);

                                            photoRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // File deleted successfully
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // An error occurred while deleting the file
                                                }
                                            });
                                            img.setVisibility(View.GONE);


                                        } catch (AddressException e) {
                                            e.printStackTrace();
                                        } catch (MessagingException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }else{
                            Toast.makeText(StoreReturnActivity.this, "No requests found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}