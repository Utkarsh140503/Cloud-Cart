package com.utkarsh.codelink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity {
    EditText desc, AccID;
    Button pay;
    TextView redirectToProfile;
    String TAG = "main";
    final int UPI_PAYMENT = 0;

    final String[] NameFromDB = new String[1];
    final String[] UPIFromDB = new String[1];
    final String[] priceFromDB = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        desc = findViewById(R.id.paymentDesc);
        AccID = findViewById(R.id.AccountIDNum);
        pay = findViewById(R.id.payment_button);
        redirectToProfile = findViewById(R.id.profileRedirectText);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("users");
                DatabaseReference reference1 = database.getReference("receipts");
                Query checkUserDatabase = reference.orderByChild("idnum").equalTo(AccID.getText().toString());

                checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Query checkUserDatabase1 = reference1.orderByChild(AccID.getText().toString() + "_Receipt");
                            checkUserDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                    if (snapshot1.exists()) {
                                        priceFromDB[0] = snapshot.child(AccID.getText().toString() + "_Receipt").child("cartValue").getValue(String.class);
//                                        AccID.setError(null);
                                        UPIFromDB[0] = snapshot.child(AccID.getText().toString()).child("upi").getValue(String.class);
                                        NameFromDB[0] = snapshot.child(AccID.getText().toString()).child("name").getValue(String.class);
                                        payUsingUPI(NameFromDB[0], UPIFromDB[0], desc.getText().toString(), priceFromDB[0]);
                                    } else {
                                        Toast.makeText(PaymentActivity.this, "No Receipt found for the given ID Number!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                void payUsingUPI(String name, String upiID, String descr, String amount) {
                                    Log.e("main", "name" + NameFromDB[0] + "--up--" + UPIFromDB[0] + "--" + desc.getText().toString() + "--" + priceFromDB[0]);
                                    Uri uri = Uri.parse("upi://pay").buildUpon()
                                            .appendQueryParameter("mid","8887854926@paytm")
                                            .appendQueryParameter("pn", "Utkarsh")
                                            .appendQueryParameter("mc", "1qsdeq")
                                            .appendQueryParameter("tr", "2dwdwd")
                                            .appendQueryParameter("tn", desc.getText().toString())
                                            .appendQueryParameter("am", "1")
                                            .appendQueryParameter("cu", "INR")
                                            .build();
                                    Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
                                    upiPayIntent.setData(uri);
                                    Intent chooser = Intent.createChooser(upiPayIntent, "Pay With");
                                    if (null != chooser.resolveActivity(getPackageManager())) {
                                        startActivityForResult(chooser, UPI_PAYMENT);
                                    } else {
                                        Toast.makeText(PaymentActivity.this, "NO UPI App Found!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                                    Log.e("main ", "response " + resultCode);
                            /*
                               E/main: response -1
                               E/UPI: onActivityResult: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
                               E/UPIPAY: upiPaymentDataOperation: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
                               E/UPI: payment successfull: 922118921612
                             */
                                    switch (requestCode) {
                                        case UPI_PAYMENT:
                                            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                                                if (data != null) {
                                                    String trxt = data.getStringExtra("response");
                                                    Log.e("UPI", "onActivityResult: " + trxt);
                                                    ArrayList<String> dataList = new ArrayList<>();
                                                    dataList.add(trxt);
                                                    upiPaymentDataOperation(dataList);
                                                } else {
                                                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                                                    ArrayList<String> dataList = new ArrayList<>();
                                                    dataList.add("nothing");
                                                    upiPaymentDataOperation(dataList);
                                                }
                                            } else {
                                                //when user simply back without payment
                                                Log.e("UPI", "onActivityResult: " + "Return data is null");
                                                ArrayList<String> dataList = new ArrayList<>();
                                                dataList.add("nothing");
                                                upiPaymentDataOperation(dataList);
                                            }
                                            break;
                                    }
                                }

                                private void upiPaymentDataOperation(ArrayList<String> data) {
                                    if (isConnectionAvailable(PaymentActivity.this)) {
                                        String str = data.get(0);
                                        Log.e("UPIPAY", "upiPaymentDataOperation: " + str);
                                        String paymentCancel = "";
                                        if (str == null) str = "discard";
                                        String status = "";
                                        String approvalRefNo = "";
                                        String response[] = str.split("&");
                                        for (int i = 0; i < response.length; i++) {
                                            String equalStr[] = response[i].split("=");
                                            if (equalStr.length >= 2) {
                                                if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                                                    status = equalStr[1].toLowerCase();
                                                } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                                                    approvalRefNo = equalStr[1];
                                                }
                                            } else {
                                                paymentCancel = "Payment cancelled by user.";
                                            }
                                        }
                                        if (status.equals("success")) {
                                            //Code to handle successful transaction here.
                                            Toast.makeText(PaymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                                            Log.e("UPI", "payment successfull: " + approvalRefNo);
                                        } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                                            Toast.makeText(PaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                                            Log.e("UPI", "Cancelled by user: " + approvalRefNo);
                                        } else {
                                            Toast.makeText(PaymentActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                                            Log.e("UPI", "failed payment: " + approvalRefNo);
                                        }
                                    } else {
                                        Log.e("UPI", "Internet issue: ");
                                        Toast.makeText(PaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                public boolean isConnectionAvailable(Context context) {
                                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                                    if (connectivityManager != null) {
                                        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                                        if (netInfo != null && netInfo.isConnected()
                                                && netInfo.isConnectedOrConnecting()
                                                && netInfo.isAvailable()) {
                                            return true;
                                        }
                                    }
                                    return false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            AccID.setError("User does not exist");
                            AccID.requestFocus();
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