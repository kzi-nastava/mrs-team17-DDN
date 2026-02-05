package com.example.taximobile.core.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;

    public static Retrofit get(Context ctx) {
        if (retrofit != null) return retrofit;

        TokenStorage tokenStorage = new TokenStorage(ctx.getApplicationContext());

        OkHttpClient ok = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenStorage))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(ok)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}
