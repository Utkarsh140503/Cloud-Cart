package com.utkarsh.codelink;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GraphicalAnalysisActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference salesReference;
    private DatabaseReference usersReference;

    private PieChart pieChart;
    private BarChart barChart;

    private boolean isShowingPieChart = true;

    TextView label;
    String storeId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphical_analysis);

        storeId = getIntent().getStringExtra("storeID");

        firebaseDatabase = FirebaseDatabase.getInstance();
        salesReference = firebaseDatabase.getReference("sales_analysis").child(storeId + "_sales_analysis").child("item_quantities");
        usersReference = firebaseDatabase.getReference("users");

        pieChart = findViewById(R.id.pie_chart);
        barChart = findViewById(R.id.bar_chart);

        // Set initial chart visibility
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);

        label = findViewById(R.id.labelHeading);

        retrieveSalesFromDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graph_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pie_chart:
                showPieChart();
                return true;
            case R.id.menu_bar_chart:
                showBarChart();
                return true;
            case R.id.menu_users_chart:
                showUserChart();
                return true;
            case R.id.menu_everyday_sales:
                showEverydaySalesChart();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPieChart() {
        isShowingPieChart = true;
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);

        retrieveSalesFromDatabase();
    }

    private void showBarChart() {
        isShowingPieChart = false;
        barChart.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.GONE);

        retrieveSalesFromDatabase();
    }

    private void showEverydaySalesChart() {
        barChart.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.GONE);

//        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        DatabaseReference ref = firebaseDatabase.getReference("sales").child(storeId + "_Sales");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Integer> dailySales = new TreeMap<>(); // Use TreeMap to maintain ascending order of dates

                for (DataSnapshot saleSnapshot : dataSnapshot.getChildren()) {
                    String saleId = saleSnapshot.getKey();
                    String saleDate = saleSnapshot.child("date").getValue(String.class);

                    if (saleDate != null) {
                        //                            Date date = dateFormat.parse(saleDate);
                        String formattedDate = saleDate.substring(0,saleDate.indexOf(' '));

                        double saleAmount = Double.parseDouble(saleSnapshot.child("cartValue").getValue(String.class));

                        int totalSaleAmount = (int) (dailySales.containsKey(formattedDate)
                                                        ? dailySales.get(formattedDate) + saleAmount
                                                        : saleAmount);
                        dailySales.put(formattedDate, totalSaleAmount);
                    }else{
                        Toast.makeText(GraphicalAnalysisActivity.this, "No Sale Date found!", Toast.LENGTH_SHORT).show();
                    }
                }

                // Extract sale dates and amounts for x-axis and bar data
                List<BarEntry> barEntries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                int index = 0; // Track x-axis index

                for (Map.Entry<String, Integer> entry : dailySales.entrySet()) {
                    String saleDate = entry.getKey();
                    int saleAmount = entry.getValue();

                    labels.add(saleDate);
                    barEntries.add(new BarEntry(index, saleAmount));

                    index++; // Increment index
                }

                // Create a dataset with the sales data for bar chart
                label.setText("Bar Chart for everyday sales");
                BarDataSet barDataSet = new BarDataSet(barEntries, "Sales");
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                barDataSet.setValueTextColor(Color.WHITE);
                barDataSet.setValueTextSize(12f);

                BarData barData = new BarData(barDataSet);

                // Set the data to the BarChart
                barChart.setData(barData);
                barChart.getDescription().setText("Everyday Sales Analysis");
                barChart.getDescription().setTextSize(12f);
                barChart.animateY(1000);

                // Set custom X-axis value formatter for BarChart
                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setGranularity(1);
                xAxis.setLabelCount(labels.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUserChart() {
        barChart.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.GONE);

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Integer> joinDatesCount = new TreeMap<>(); // Use TreeMap to maintain ascending order of dates

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userID = userSnapshot.getKey();
                    String storeID = userSnapshot.child("storeID").getValue(String.class);
                    String currentDateAndTime = userSnapshot.child("currentDateAndTime").getValue(String.class);

                    if (storeID.equals(storeId)) {
                        String joinDate = currentDateAndTime.substring(0, currentDateAndTime.indexOf(' '));
                        int count = joinDatesCount.containsKey(joinDate) ? joinDatesCount.get(joinDate) : 0;
                        joinDatesCount.put(joinDate, count + 1);
                    }
                }

                // Extract join dates and counts for x-axis and bar data
                List<BarEntry> barEntries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                int index = 0; // Track x-axis index

                for (Map.Entry<String, Integer> entry : joinDatesCount.entrySet()) {
                    String joinDate = entry.getKey();
                    int count = entry.getValue();

                    labels.add(joinDate);
                    barEntries.add(new BarEntry(index, count));

                    index++; // Increment index
                }

                // Create a dataset with the user data for bar chart
                label.setText("Bar Chart for users joining the store");
                BarDataSet barDataSet = new BarDataSet(barEntries, "Users");
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                barDataSet.setValueTextColor(Color.WHITE);
                barDataSet.setValueTextSize(12f);

                BarData barData = new BarData(barDataSet);

                // Set the data to the BarChart
                barChart.setData(barData);
                barChart.getDescription().setText("User Analysis");
                barChart.getDescription().setTextSize(12f);
                barChart.animateY(1000);

                // Set custom X-axis value formatter for BarChart
                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setGranularity(1);
                xAxis.setLabelCount(labels.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveSalesFromDatabase() {
        salesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<PieEntry> pieEntries = new ArrayList<>();
                List<BarEntry> barEntries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                int index = 0; // Track x-axis index

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String itemName = itemSnapshot.getKey().substring(1);
                    int quantity = itemSnapshot.getValue(Integer.class);

                    pieEntries.add(new PieEntry(quantity, itemName));
                    barEntries.add(new BarEntry(index, quantity)); // Use index for x-axis value
                    labels.add(itemName);

                    index++; // Increment index
                }

                if (isShowingPieChart) {
                    // Create a dataset with the sales data for pie chart
                    label.setText("Pie Chart for items sold");
                    PieDataSet pieDataSet = new PieDataSet(pieEntries, "Sales");
                    pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                    pieDataSet.setValueTextColor(Color.WHITE);
                    pieDataSet.setValueTextSize(12f);

                    PieData pieData = new PieData(pieDataSet);

                    // Set the data to the PieChart
                    pieChart.setData(pieData);
                    pieChart.getDescription().setText("Sales Analysis");
                    pieChart.getDescription().setTextSize(12f);
                    pieChart.animateY(1000);

                    // Hide BarChart's data
                    barChart.setData(null);
                    barChart.invalidate();
                } else {
                    // Create a dataset with the sales data for bar chart
                    label.setText("Bar Chart for items sold");
                    BarDataSet barDataSet = new BarDataSet(barEntries, "Sales");
                    barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                    barDataSet.setValueTextColor(Color.WHITE);
                    barDataSet.setValueTextSize(12f);

                    BarData barData = new BarData(barDataSet);

                    // Set the data to the BarChart
                    barChart.setData(barData);
                    barChart.getDescription().setText("Sales Analysis");
                    barChart.getDescription().setTextSize(12f);
                    barChart.animateY(1000);

                    // Set custom X-axis value formatter for BarChart
                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setGranularity(1);
                    xAxis.setLabelCount(labels.size());

                    // Hide PieChart's data
                    pieChart.setData(null);
                    pieChart.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}