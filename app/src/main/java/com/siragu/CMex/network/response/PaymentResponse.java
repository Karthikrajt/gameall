package com.siragu.CMex.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("object")
    @Expose
    private String object;

    public String getId() {
        return id;
    }

    public String getObject() {
        return object;
    }
}
