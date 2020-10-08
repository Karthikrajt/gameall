package com.siragu.CMex.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.siragu.CMex.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by a_man on 05-12-2017.
 */

public class ApiUtils {
    private static Retrofit retrofit;

    private ApiUtils() {
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
