package com.siragu.CMex.model;

/**
 * Created by laxmikant on 3/29/2018.
 */

public class StoriesModel {
    public int productImage;
    public String productName;

    public StoriesModel(String productName, int productImage) {
        this.productImage = productImage;
        this.productName = productName;
    }

    public int getProductImage() {
        return productImage;
    }

    public void setProductImage(int productImage) {
        this.productImage = productImage;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}