package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

public class BillingActivity extends AppCompatActivity {

    private CodeScanner codeScanner;
    private CodeScannerView scannerView;
    private TextView codeData, cartText;
    private Button addToCartButton, receiptButton, emailButton, paymentButton;
    private EditText custIdNumber;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        // Initialize the scanner view and text view
        scannerView = findViewById(R.id.scanner_view);
        codeData = findViewById(R.id.scanner_textView);
        cartText = findViewById(R.id.cart_textView);
        addToCartButton = findViewById(R.id.button_addtocart);
        receiptButton = findViewById(R.id.button_receipt);
        emailButton = findViewById(R.id.email_receipt);
        custIdNumber = findViewById(R.id.custNumText);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Camera permission already granted
            setupScanner();
        }
    }

    private void setupScanner() {
        // Initialize the CodeScanner object with the scanner view
        codeScanner = new CodeScanner(this, scannerView);

        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setScanMode(ScanMode.CONTINUOUS);

        double[] cartValue = {0.0};

        ArrayList<String> items = new ArrayList<>();
        HashMap<String, Integer> hashMap = new HashMap<>();
        HashMap<String, Integer> hashMap1 = new HashMap<>();

        // Set the decode callback for the scanner
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update the text view with the scanned code data
                        String data = result.getText();
                        codeData.setText(data);
                        addToCartButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (data.contains("Price")) {
                                    String s = data.substring(data.indexOf(':'), data.indexOf('|'));
                                    items.add(s);
                                    if (hashMap.containsKey(s)) {
                                        hashMap.put(s, hashMap.get(s) + 1);
                                    } else {
                                        hashMap.put(s, 1);
                                    }
                                    int i = data.lastIndexOf(':');
                                    cartValue[0] = cartValue[0] + Integer.parseInt(data.substring(i + 1, data.length()));

                                    cartText.setText("Cart Value is: " + cartValue[0]);

                                    hashMap1.put(s, Integer.parseInt(data.substring(i + 1, data.length())));
                                } else {
                                    Toast.makeText(BillingActivity.this, "Invalid QR Code Scanned", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        // Start the scanner
        codeScanner.startPreview();

        receiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String IDNum = custIdNumber.getText().toString();
                if (IDNum.isEmpty()) {
                    custIdNumber.setError("ID Number cannot be empty");
                } else if (cartValue[0] == 0) {
                    Toast.makeText(BillingActivity.this, "Cart is Empty! No receipt generated", Toast.LENGTH_SHORT).show();
                } else {
                    final String[] emailFromDB = new String[1];
                    final String[] storeIDFromDB = new String[1];
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                    Query checkUserDatabase = ref.orderByChild("idnum").equalTo(IDNum);

                    checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                emailFromDB[0] = snapshot.child(IDNum).child("email").getValue(String.class);
                                storeIDFromDB[0] = snapshot.child(IDNum).child("storeID").getValue(String.class);
                                custIdNumber.setError(null);
                                String finalCartVal = cartValue[0] + "";
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("receipts");
                                ReceiptHelper receiptHelper = new ReceiptHelper(IDNum, finalCartVal, hashMap);
                                reference.child(IDNum + "_Receipt").setValue(receiptHelper);

                                Toast.makeText(BillingActivity.this, "Receipt generated successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                custIdNumber.setError("User does not exist");
                                custIdNumber.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    emailButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("receipts");
                            Query checkUserDatabase1 = reference1.orderByChild(IDNum + "_Receipt");
                            checkUserDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ArrayList<String> quantity = new ArrayList<>();
                                    ArrayList<String> items1 = new ArrayList<>();
                                    if (snapshot.exists()) {
                                        String priceFromDB = snapshot.child(IDNum + "_Receipt").child("cartValue").getValue(String.class);
                                        String itemsFromDB = "";

                                        for (Map.Entry<String, Integer> mapElement : hashMap.entrySet()) {
                                            itemsFromDB += mapElement.getKey().substring(1) + " " + mapElement.getValue() + " -> Rs." + (hashMap1.get(mapElement.getKey()) * mapElement.getValue()) + "\n";
                                            quantity.add(String.valueOf(mapElement.getValue()));
                                            items1.add(mapElement.getKey().substring(1));
                                        }

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

                                            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                                                @Override
                                                protected PasswordAuthentication getPasswordAuthentication() {
                                                    return new PasswordAuthentication(senderEmail, stringPasswordSenderEmail);
                                                }
                                            });

                                            MimeMessage mimeMessage = new MimeMessage(session);

                                            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

                                            mimeMessage.setSubject("Message from CloudCart Team");
                                            mimeMessage.setText("Hello Shopper! \nHere's your receipt\n\nFor the ID Number: " + IDNum + "\n\nItems bought are:\n" + itemsFromDB + "\nNet payable amount is: Rs." + priceFromDB + "\n\nTeam CloudCart");

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
                                            Toast.makeText(BillingActivity.this, "Receipt sent to your registered Email ID", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(BillingActivity.this, PaymenttActivity.class);
                                            intent.putExtra("ID", IDNum);

                                            String itemstr = "";
                                            for (int x = 0; x < items1.size(); x++) {
                                                itemstr += items1.get(x) + ",";
                                            }

                                            String quantitystr = "";
                                            for (int x = 0; x < quantity.size(); x++) {
                                                quantitystr += quantity.get(x) + ",";
                                            }
                                            intent.putExtra("id", IDNum);
                                            intent.putExtra("itemsbought", itemstr);
                                            intent.putExtra("quantity", quantitystr);
                                            intent.putExtra("hashMap", hashMap);
                                            intent.putExtra("storeID", storeIDFromDB[0]);

                                            startActivity(intent);

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
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasCameraPermission()) {
            codeScanner.startPreview();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    private boolean hasCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupScanner();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
