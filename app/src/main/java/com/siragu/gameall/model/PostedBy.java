package com.siragu.gameall.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class PostedBy {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("mobile")
    @Expose
    private String mobile;
    @SerializedName("gender")
    @Expose
    private String gender;

    public PostedBy(Integer id, String mobile, String gender) {
        this.id = id;
        this.mobile = mobile;
        this.gender = gender;
    }

    /**
     * 
     * @return
     *     The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * 
     * @param gender
     *     The gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     *
     * @return
     *     The mobile
     */
    public String getMobile() {
        return mobile;
    }

    /**
     *
     * @param mobile
     *     The mobile
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

}
