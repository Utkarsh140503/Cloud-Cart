package com.utkarsh.codelink;

import java.util.Map;

public class ReceiptHelper1 {
    String custIdNum, cartValue, date;
    Map<String,Integer> items;
    public ReceiptHelper1(String custIdNum, String cartValue, Map<String,Integer> items, String date){
        this.cartValue = cartValue;
        this.custIdNum = custIdNum;
        this.items = items;
        this.date = date;
    }

    public String getCustIdNum() {
        return custIdNum;
    }

    public void setCustIdNum(String custIdNum) {
        this.custIdNum = custIdNum;
    }

    public String getCartValue() {
        return cartValue;
    }

    public void setCartValue(String cartValue) {
        this.cartValue = cartValue;
    }

    public Map<String,Integer> getItems() { return items; }

    public void setItems(Map<String,Integer> items) { this.items = items; }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ReceiptHelper1(){

    }
}