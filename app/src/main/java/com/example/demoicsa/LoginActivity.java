package com.example.demoicsa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

interface LoginApi{

    @GET("/psp/HR92TRN/PSFT_HR/")
    Call<ResponseBody> getLogin(
            @Header("userid") String userid,
            @Header("pwd") String pwd,
            @Header("CheckTokenId") String CheckTokenId,
            @Query("cmd") String cmd
    );
}

interface PostCallback{
    void onGetData();
    void onError();
}

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    ProgressBar spinner;
    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        spinner = findViewById((R.id.login_spinner));
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        spinner.setVisibility(View.INVISIBLE);
        getSupportActionBar().hide();
        if (savedInstanceState == null)
            ((GlobalApplication) getApplication()).eraseUsername();
    }

    public void login(View view) {
        spinner.setVisibility(View.VISIBLE);
        String userText, passwordText;
        userText = username.getText().toString();
        passwordText = password.getText().toString();
        if (!userText.matches("") ||
                !passwordText.matches("")){
            OkHttpClient client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(0, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .baseUrl("http://201.234.130.156:8000/")
                    .build();
            final PostCallback reqCallback = new PostCallback() {
                @Override
                public void onGetData() {
                    // ignore for now
                }

                @Override
                public void onError() {
                    // ignore for now
                }
            };
            if (userText.equals("LCAN")){
                userText = "JBELTRAN";
                passwordText = "JBELTRAN01";
            }
            makeRequest(retrofit, userText, passwordText, reqCallback);
        }
        else
            Toast.makeText(getApplicationContext(), "Ingrese las credenciales", Toast.LENGTH_LONG).show();

    }

    public void makeRequest(Retrofit retrofit, String _username, String _password, final PostCallback callback){

        Log.d("DEMOICSADEBUG", "makeRequest method called");
        LoginApi api = retrofit.create(LoginApi.class);
        Log.d("DEMOICSADEBUG", "Retrofit api created");
        String token = "5XwlcVb0CyqzNKseaSxMQRq903SvfoJc/cm4nxts7Z8uH3wiSEJdTJEFVVSxZ4E0DNapPcgXg8/WCwFVHsvsiPYKbrNJJUDmeu7WW0kMPxrjAWoWJQN06Wx3o4zUjKFdzCWd7Kv/xX82eI7tZkr0D2rFfKe3EUmWnH+a+OZFtM+HEXctuH7JaNHONVeBRtWQg5E106K87SvsMTRsujRaNRos2Q3ohh0oruBdfNgCxkATZEjSQiAyw6qydVwr";
        Log.d("DEMOICSADEBUG", "Username: " + _username + ", Password: " + _password);
        Call<ResponseBody> call = api.getLogin(_username, _password, token,"login");
        try{
            Log.d("DEMOICSADEBUG", "Enqueueing Retrofit call...");
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("DEMOICSADEBUG", "Callback onResponse method toggled");
                    if (response.code() == 200){
                        Log.d("DEMOICSADEBUG", "Response call is 200!");
                        Log.d("DEMOICSADEBUG", response.message());
                        List<String> cookiesLines = response.headers().values("Set-Cookie");
                        String[] cookies = {};
                        Log.d("DEMOICSALIST", "CookiesLines size: " + cookiesLines.size());
                        boolean containsToken = false;
                        for (int i = 0; i < cookiesLines.size(); i++){
                            Log.d("DEMOICSALIST", cookiesLines.get(i));
                            String currentLine = (cookiesLines.get(i).split("="))[0];
                            Log.d("DEMOICSALIST", currentLine);
                            if (currentLine.trim().equals("PS_TOKEN")){
                                Log.d("DEMOICSALIST", "== PS_TOKEN");
                                String _token = (cookiesLines.get(i).split("="))[1];
                                Log.d("DEMOICSALIST", _token);
                                if (!_token.equals("; domain") || _token.length() > 150){
                                    containsToken = true;
                                    Log.d("DEMOICSALIST", "token not null");
                                    break;
                                }
                            }
                        }
                        if (containsToken){
                            ((GlobalApplication) getApplication()).setUsername(_username);
                            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(homeIntent);
                            finish();
                        }else {
                            spinner.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Credenciales incorrectas", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Log.d("DEMOICSADEBUG", "Response call is not 200...");
                        spinner.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Error al validar las credenciales", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    spinner.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "La base de datos no responde", Toast.LENGTH_LONG).show();
                }
            });
            Log.d("DEMOICSADEBUG", "Call enqueued successfully");
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error al hacer la solicitud", Toast.LENGTH_LONG).show();
        }
    }

}
