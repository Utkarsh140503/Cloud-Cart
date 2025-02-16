package com.utkarsh.codelink;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesViewActivity extends AppCompatActivity {
    private ListView salesListView;
    private DatabaseReference salesRef;
    private List<Sale> salesList;
    private ArrayAdapter<Sale> salesAdapter;
    private boolean isSortedByCartValue = false;
    String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_view);

        // Retrieve the store ID from Intent
        storeId = getIntent().getStringExtra("idnum");

        salesListView = findViewById(R.id.salesListView);
        salesList = new ArrayList<>();
        salesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, salesList);
        salesListView.setAdapter(salesAdapter);

        // Reference to the sales data in the Firebase Realtime Database
        salesRef = FirebaseDatabase.getInstance().getReference().child("sales").child(storeId + "_Sales");

        // Listen for changes in the sales data
        salesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                salesList.clear();

                // Initialize variables to store total quantity and item names with quantities
                int totalQuantity = 0;
                Map<String, Integer> itemQuantities = new HashMap<>();

                // Iterate over the child nodes (sales)
                for (DataSnapshot saleSnapshot : dataSnapshot.getChildren()) {
                    String saleId = saleSnapshot.getKey();
                    double cartValue = Double.parseDouble(saleSnapshot.child("cartValue").getValue(String.class));
                    String custIdNum = saleSnapshot.child("custIdNum").getValue(String.class);
                    String date = saleSnapshot.child("date").getValue(String.class);
                    // Retrieve the items if available
                    List<String> items = new ArrayList<>();
                    if (saleSnapshot.hasChild("items")) {
                        for (DataSnapshot itemSnapshot : saleSnapshot.child("items").getChildren()) {
                            String itemName = itemSnapshot.getKey();
                            int itemQuan = itemSnapshot.getValue(Integer.class);

                            // Store item quantity for further analysis
                            totalQuantity += itemQuan;

                            // Check if the item already exists in "item_quantities" map
                            if (itemQuantities.containsKey(itemName)) {
                                // Increment the item count
                                int currentCount = itemQuantities.get(itemName);
                                itemQuantities.put(itemName, currentCount + itemQuan);
                            } else {
                                // Add the item to the "item_quantities" map
                                itemQuantities.put(itemName, itemQuan);
                            }

                            items.add(itemName + "-> " + itemQuan);
                        }
                    }
                    Sale sale = new Sale(saleId, cartValue, custIdNum, date, items);
                    salesList.add(sale);
                }

                // Now you can store itemQuantities and totalQuantity in the Firebase Realtime Database
                DatabaseReference analysisRef = FirebaseDatabase.getInstance().getReference().child("sales_analysis").child(storeId + "_sales_analysis");
                analysisRef.child("item_quantities").setValue(itemQuantities);
                analysisRef.child("total_quantity").setValue(totalQuantity);

                // Sort the sales list based on cartValue if previously sorted by cartValue
                if (isSortedByCartValue) {
                    sortSalesByCartValue();
                }

                // Notify the adapter that the data has changed
                salesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Set item click listener for the sales list
        salesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the selected sale
                Sale selectedSale = salesList.get(position);

                // Display sale details in a dialog box
                showSaleDetailsDialog(selectedSale);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sales_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sortByCartValue) {
            if (isSortedByCartValue) {
                // Already sorted by cartValue, reverse the order
                Collections.reverse(salesList);
            } else {
                // Sort the sales list based on cartValue (ascending order)
                sortSalesByCartValue();
            }
            // Update the sort flag
            isSortedByCartValue = !isSortedByCartValue;
            salesAdapter.notifyDataSetChanged();
            return true;
        } else if (item.getItemId() == R.id.sortByDate) {
            // Sort the sales list based on date (ascending order)
            sortSalesByDate();
            salesAdapter.notifyDataSetChanged();
            return true;
        }
        else if(item.getItemId() == R.id.graphicalAnalysis){
            Intent intent = new Intent(SalesViewActivity.this, GraphicalAnalysisActivity.class);
            intent.putExtra("storeID", storeId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortSalesByCartValue() {
        Collections.sort(salesList, new Comparator<Sale>() {
            @Override
            public int compare(Sale sale1, Sale sale2) {
                return Double.compare(sale1.getCartValue(), sale2.getCartValue());
            }
        });
    }

    private void sortSalesByDate() {
        Collections.sort(salesList, new Comparator<Sale>() {
            @Override
            public int compare(Sale sale1, Sale sale2) {
                // Assuming that the date is in the format "YYYY-MM-DD"
                String date1 = sale1.getDate();
                String date2 = sale2.getDate();

                if (date1 == null || date1.isEmpty()) {
                    return 1; // Treat null or empty date as larger, place it at the end
                } else if (date2 == null || date2.isEmpty()) {
                    return -1; // Treat null or empty date as larger, place it at the end
                } else {
                    return date1.compareTo(date2);
                }
            }
        });
    }

    private void showSaleDetailsDialog(Sale sale) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sale Details");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sale_details, null);
        builder.setView(dialogView);

        TextView cartValueTextView = dialogView.findViewById(R.id.cartValueTextView);
        TextView custIdNumTextView = dialogView.findViewById(R.id.custIdNumTextView);
        TextView dateTextView = dialogView.findViewById(R.id.dateTextView);
        TextView itemsTextView = dialogView.findViewById(R.id.itemsTextView);

        cartValueTextView.setText("Cart Value: " + sale.getCartValue());
        custIdNumTextView.setText("Customer ID: " + sale.getCustIdNum());
        dateTextView.setText("Date: " + sale.getDate());

        if (sale.getItems().isEmpty()) {
            itemsTextView.setText("Items: None");
        } else {
            String itemsText = "Items:\n";
            for (String item : sale.getItems()) {
                itemsText += "- " + item + "\n";
            }
            itemsTextView.setText(itemsText);
        }

        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}