package com.example.demoicsa.psmessage;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;

public class AuthInterceptor implements Interceptor {
    private String credentials;

    public AuthInterceptor(String user, String password){
        this.credentials = Credentials.basic(user, password);
    }

    @NotNull
    @Override
    public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Request authRequest = request.newBuilder()
                .header("Authorization", credentials).build();
        return chain.proceed(authRequest);
    }
}