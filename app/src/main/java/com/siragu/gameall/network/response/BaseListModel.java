package com.siragu.gameall.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by a_man on 05-12-2017.
 */

public class BaseListModel<T> {
    @SerializedName("current_page")
    @Expose
    private Integer current_page;

    @SerializedName("total")
    @Expose
    private Integer total;

    @SerializedName("data")
    @Expose
    private ArrayList<T> data;

    public Integer getCurrent_page() {
        return current_page;
    }

    public ArrayList<T> getData() {
        return data;
    }
}
