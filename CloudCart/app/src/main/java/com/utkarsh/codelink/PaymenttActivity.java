package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class PaymenttActivity extends AppCompatActivity {

    TextView balance, redirectToProfile;
    Button seeBalance, seeCartBalance, Pay;
    Dialog dialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paymentt);

        balance = findViewById(R.id.BalanceText);
        redirectToProfile = findViewById(R.id.profileRedirectText);
        seeBalance = findViewById(R.id.balance_button);
        Pay = findViewById(R.id.payment_button);
        seeCartBalance = findViewById(R.id.cart_button);

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String id = getIntent().getStringExtra("ID");
        final String[] items = {getIntent().getStringExtra("itemsbought")};
        final String[] quantity = {getIntent().getStringExtra("quantity")};
        HashMap<String, Integer> hashMap = new HashMap<>();
        Intent intt = getIntent();
        hashMap = (HashMap<String, Integer>) intt.getSerializableExtra("hashMap");
        final String[] storeID = {getIntent().getStringExtra("storeID")};

        redirectToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaymenttActivity.this, BillingActivity.class);
                startActivity(intent);
            }
        });

        final String[] emailFromDB = new String[1];
        final String[] storeIDFromDB = new String[1];
        final String[] cartVal = new String[1];
        final double[] cart = {0.0};
        final double[] cred = {0.0};

        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("receipts");
        Query checkUserDatabase1 = ref1.orderByChild(id + "_Receipt");

        seeCartBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                cartVal[0] = snapshot.child(id + "_Receipt").child("cartValue").getValue(String.class);
                                Toast.makeText(PaymenttActivity.this, "Cart Value : " + cartVal[0], Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(PaymenttActivity.this, "Cart Value Not Found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(PaymenttActivity.this, "No Active Receipts Found for this ID Number.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        HashMap<String, Integer> finalHashMap = hashMap;
        seeBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id != null && !id.isEmpty()) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                    Query checkUserDatabase = ref.orderByChild("idnum").equalTo(id);
                    checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                emailFromDB[0] = snapshot.child(id).child("email").getValue(String.class);

                                checkUserDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        if (snapshot1.exists()) {
                                            try {
                                                cartVal[0] = snapshot1.child(id + "_Receipt").child("cartValue").getValue(String.class);
                                                cart[0] = Double.parseDouble(cartVal[0]);
                                                String credits = snapshot.child(id).child("Purchase Credits").child("credits").getValue(String.class);
                                                cred[0] = Double.parseDouble(credits);
                                                balance.setText("Credits Balance = " + cred[0]);
                                            } catch (Exception e) {
                                                Toast.makeText(PaymenttActivity.this, "Less Credits!", Toast.LENGTH_SHORT).show();

                                                try {
                                                    String senderEmail = "teamcloudcart@gmail.com";
                                                    String stringReceiverEmail = "teamcloudcart@gmail.com";
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
                                                    mimeMessage.setText("Hello Team CloudCart! \nCredit Refill Request \n\nFor the ID Number : " + "'" + id + "'" + " Received\n\nThank you!");

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
                                                    Toast.makeText(PaymenttActivity.this, "Add Credits Mail Sent to team CloudCart. CloudCart Team will contact you Soon!", Toast.LENGTH_SHORT).show();
                                                } catch (AddressException exp) {
                                                    exp.printStackTrace();
                                                } catch (MessagingException exp) {
                                                    exp.printStackTrace();
                                                }

                                            }
                                        } else {
                                            Toast.makeText(PaymenttActivity.this, "No Active Receipts Found for this ID Number.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                Pay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        checkUserDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    if (cart[0] > 0) {
                                                        if (cred[0] >= cart[0]) {
                                                            String newCartVal = (cred[0] - cart[0]) + "";
                                                            CreditsHelperClass creditsHelperClass = new CreditsHelperClass(newCartVal);
                                                            ref.child(id).child("Purchase Credits").setValue(creditsHelperClass);

                                                            Toast.makeText(PaymenttActivity.this, "Payment Successful! Thank you for Shopping.", Toast.LENGTH_SHORT).show();
                                                            cred[0] -= cart[0];
                                                            balance.setText("Credits Balance = " + cred[0]);
                                                            int TransID;
                                                            try {
                                                                String senderEmail = "teamcloudcart@gmail.com";
                                                                String stringReceiverEmail = emailFromDB[0];
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
                                                                TransID = (int) ((Math.random() * 100) * (Math.random() * 100));

                                                                TransIdHelper transIdHelper = new TransIdHelper(TransID + "");
                                                                ref.child(id).child("TransactionID").setValue(transIdHelper);

                                                                mimeMessage.setSubject("Message from CloudCart Team");
                                                                mimeMessage.setText("Hello Shopper! \nThank you for Shopping at CloudCart\n\nFor the ID Number : " + id + "\n\nPayment of Rs. " + cart[0] + " is Successful!\n" +
                                                                        "\n\nPlease Provide this one-time OTP at the exit to validate your payment: " + TransID + "\n\n\nWarm regards,\nTeam CloudCart");

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

                                                                DatabaseReference database1 = FirebaseDatabase.getInstance().getReference("sales").child(storeID[0] + "_Sales");
                                                                ReceiptHelper1 receiptHelper1 = new ReceiptHelper1(id, cart[0] + "", finalHashMap, dateFormat.format(new Date()));
                                                                Random r = new Random();
                                                                int xx = r.nextInt(100 - 0) + 0;

                                                                DatabaseReference newReceiptRef = ref1.child(id + "_Receipt");
                                                                newReceiptRef.child("cartValue").setValue(cart[0] + "");
                                                                newReceiptRef.child("date").setValue(dateFormat.format(new Date()));
                                                                newReceiptRef.child("items").setValue(items[0]);
                                                                newReceiptRef.child("quantity").setValue(quantity[0]);

                                                                DatabaseReference newSalesRef = database1.child("Sales ID:" + xx);
                                                                newSalesRef.setValue(receiptHelper1);

                                                                // Update inventory
                                                                DatabaseReference ref4 = FirebaseDatabase.getInstance().getReference("inventory");
                                                                StringTokenizer itemsTokenizer = new StringTokenizer(items[0], ",");
                                                                StringTokenizer quantityTokenizer = new StringTokenizer(quantity[0], ",");
                                                                while (itemsTokenizer.hasMoreTokens() && quantityTokenizer.hasMoreTokens()) {
                                                                    String currentItem = itemsTokenizer.nextToken().trim();
                                                                    String currentQuantity = quantityTokenizer.nextToken().trim();
                                                                    String inventoryKey = storeIDFromDB[0] + "_" + currentItem;
                                                                    ref4.child(inventoryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            if (snapshot.exists()) {
                                                                                int currentQuant = snapshot.child("productQuantity").getValue(Integer.class);
                                                                                int boughtt = Integer.parseInt(currentQuantity);
                                                                                int newCount = currentQuant - boughtt;
                                                                                snapshot.getRef().child("productQuantity").setValue(newCount);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                                            // Handle database error
                                                                        }
                                                                    });
                                                                }

                                                                Toast.makeText(PaymenttActivity.this, "Please Refer to the mail sent on the registered Email ID to get the validation OTP", Toast.LENGTH_SHORT).show();

                                                                dialog = new Dialog(PaymenttActivity.this);
                                                                dialog.setContentView(R.layout.custom_dialog);
                                                                dialog.show();
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

                                                                newReceiptRef.removeValue();

                                                            } catch (AddressException e) {
                                                                e.printStackTrace();
                                                            } catch (MessagingException e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else {
                                                            Toast.makeText(PaymenttActivity.this, "Less Credits! Contact the Store.", Toast.LENGTH_SHORT).show();
                                                            try {
                                                                String senderEmail = "teamcloudcart@gmail.com";
                                                                String stringReceiverEmail = "teamcloudcart@gmail.com";
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
                                                                mimeMessage.setText("Hello Team CloudCart! \nCredit Refill Request \n\nFor the ID Number : " + "'" + id + "'" + " Received\n\nThank you!");

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
                                                                Toast.makeText(PaymenttActivity.this, "Add Credits Mail Sent to team CloudCart. CloudCart Team will contact you Soon!", Toast.LENGTH_SHORT).show();
                                                            } catch (AddressException e) {
                                                                e.printStackTrace();
                                                            } catch (MessagingException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    } else {
                                                        Toast.makeText(PaymenttActivity.this, "Cart Value 0. Maybe Payment already done!", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(PaymenttActivity.this, "No Active Receipts Found for this ID Number", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                });
                            } else {
                                Toast.makeText(PaymenttActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(PaymenttActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
