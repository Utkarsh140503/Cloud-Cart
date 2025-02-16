package com.utkarsh.codelink;

import java.util.Map;

public class ReceiptHelper {
    String custIdNum, cartValue;
    Map<String,Integer> items;
    public ReceiptHelper(String custIdNum, String cartValue, Map<String,Integer> items){
        this.cartValue = cartValue;
        this.custIdNum = custIdNum;
        this.items = items;
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

    public ReceiptHelper(){

    }
}
