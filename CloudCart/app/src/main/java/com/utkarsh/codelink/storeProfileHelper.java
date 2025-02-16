package com.utkarsh.codelink;

public class storeProfileHelper {
    String producttName, productManufacturing, productExpiry, productQuantity, productPrice;

    public storeProfileHelper(String producttName, String productManufacturing, String productExpiry, String productQuantity, String productPrice) {
        this.producttName = producttName;
        this.productManufacturing = productManufacturing;
        this.productExpiry = productExpiry;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
    }

    public String getProducttName() {
        return producttName;
    }

    public void setProducttName(String producttName) {
        this.producttName = producttName;
    }

    public String getProductManufacturing() {
        return productManufacturing;
    }

    public void setProductManufacturing(String productManufacturing) {
        this.productManufacturing = productManufacturing;
    }

    public String getProductExpiry() {
        return productExpiry;
    }

    public void setProductExpiry(String productExpiry) {
        this.productExpiry = productExpiry;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public storeProfileHelper(){
    }
}
