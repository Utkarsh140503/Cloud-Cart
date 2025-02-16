package com.utkarsh.codelink;

import java.util.List;

public class Sale {
    private String saleId;
    private double cartValue;
    String custIdNum;
    String date;
    List<String> items;

    public Sale(String saleId, double cartValue) {
        this.saleId = saleId;
        this.cartValue = cartValue;
    }

    public Sale(String saleId, double cartValue, String custIdNum, String date, List<String> items) {
        this.saleId = saleId;
        this.cartValue = cartValue;
        this.custIdNum = custIdNum;
        this.date = date;
        this.items = items;
    }

    public String getSaleId() {
        return saleId;
    }

    public double getCartValue() {
        return cartValue;
    }

    @Override
    public String toString() {
        return saleId + ", Cart Value: " + cartValue;
    }

    public String getCustIdNum() {
        return custIdNum;
    }

    public String getDate() {
        return date;
    }

    public List<String> getItems() {
        return items;
    }
}
