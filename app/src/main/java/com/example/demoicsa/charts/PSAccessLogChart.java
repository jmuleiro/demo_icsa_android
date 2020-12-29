package com.example.demoicsa.charts;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;

import org.jetbrains.annotations.NotNull;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;

/*
@Root(name="PSMessage", strict = false)
class PSMessageTEST<T> extends PSMessage{
    @Element(name="MsgData")
    public MsgDataTEST<T> msgData;
}

@Root(name="MsgData", strict = false)
class MsgDataTEST<T> extends MsgData{
    @ElementList(name = "Transaction", inline = true)
    public List<T> transactions;
}*/

/*
* PeoplesoftService main interface, GET method and
* getPSMessage must be called before executing request
*/
interface PeoplesoftService{
    @GET("PSIGW/RESTListeningConnector/PSFT_HR/ANDROID_TEST_2_ECHO.v1/echo/")
    Call<PSMessage> getPSMessage();
}

/*
* GET Request Callback interface
 */
interface RequestCallback{
    void onGetXmlData(PSMessage psMessage);
    void onError();
}

/*
* Classes for toggling visibility after a given period of time
 */
class VisibilityRunnable implements Runnable{
    private ProgressBar progressBar;
    private AnyChartView anyChartView;
    private View view;
    public VisibilityRunnable(ProgressBar _spinner, AnyChartView _anyChartView, View view){
        Log.d("PSAccessLogChart", "VisibilityRunnable: constructor init");
        this.progressBar = _spinner;
        this.anyChartView = _anyChartView;
        this.view = view;
        Log.d("PSAccessLogChart", "VisibilityRunnable: constructor end");
    }

    @Override
    public void run() {
        Log.d("PSAccessLogChart", "VisibilityRunnable: run() init");
        anyChartView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        Log.d("PSAccessLogChart", "VisibilityRunnable: run() visibility set");
        ScrollView.LayoutParams params = new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        Log.d("PSAccessLogChart", "VisibilityRunnable: run() ScrollView.LayoutParams set");
    }
}

/* Transaction extended class for this specific chart */
@Root(name="Transaction", strict = false)
class AccessLogTransaction extends Transaction {
    @Path(value = "./AND_RESP_RST")
    @Element(name = "DESCR_X")
    String signonCountStr;

    @Path(value = "./AND_RESP_RST")
    @Element(name = "PT_SIGNON_TYPE")
    String signonType;

    int rCount = 0;

    void setrCount(Object r){
        if (r instanceof Integer)
            this.rCount = ((Integer) r).intValue();
        else
            throw new ClassCastException();
    }

    public int getrCount(){
        return this.rCount;
    }
}

// -----------------------------------------------------------------------------------------------

@Root(name="PSMessage", strict = false)
class PSMessage{
    @Element(name="MsgData")
    MsgData<Transaction> msgData;
}

@Root(name="MsgData", strict = false)
class MsgData<T>{
    @ElementList(name = "Transaction", inline = true)
    List<Transaction> transactions;
}

@Root(name="Transaction", strict = false)
class Transaction{
    @Path(value = "./AND_RESP_RST")
    @Element(name = "DESCR_X")
    String signonCountStr;

    @Path(value = "./AND_RESP_RST")
    @Element(name = "PT_SIGNON_TYPE")
    String signonType;

    int rCount = 0;

    void setrCount(Object r){
        if (r instanceof Integer)
            this.rCount = ((Integer) r).intValue();
        else
            throw new ClassCastException();
    }

    public int getrCount(){
        return this.rCount;
    }

}

// -----------------------------------------------------------------------------------------------
/* Authentication Interceptor */
class AuthInterceptor implements Interceptor {
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

public class PSAccessLogChart {
    /*
    * Views & Misc
     */
    private ProgressBar spinner;
    private ImageView arrow;
    private AnyChartView chartView;
    private List<DataEntry> data = new ArrayList<>();

    /*
    * PSAccessLogChart Helper Class Attributes
     */
    private Integer visibilityTimeout;
    private String baseUrl;

    /*
    * Opened or closed, default closed
     */
    public boolean isToggled = false;
    private boolean spinnerIsSet = false;
    private boolean chartIsSet = false;

    /* LOG TAG */
    private static final String TAG = "PSAccessLogChart";

    /*
    * Authentication variables
     */
    private String username;
    private String password;


    public PSAccessLogChart(@NonNull String baseUrl, int vTimeout, String username, String password){
        Log.d(TAG, "Constructor: init");
        this.visibilityTimeout = vTimeout;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        preloadData();
    }

    private void preloadData(){
        getXmlData(baseUrl, new RequestCallback() {
            @Override
            public void onGetXmlData(PSMessage psMessage) {
                Log.d(TAG, "preloadData: RequestCallback triggered");
                Log.d(TAG, "preloadData: Creating data entry variable from method createDataEntry");
                try {
                    data = createDataEntry(psMessage);
                }
                catch (IllegalArgumentException e){
                    Log.d(TAG, "preloadData: IllegalArgumentException: " + e.toString());
                }
            }

            @Override
            public void onError() {
                // nothing for now
            }
        });
    }

    public void toggle(View view, int height, Context appContext) throws IllegalStateException{
        if (isToggled){
            if (spinnerIsSet){
                Log.d(TAG, "toggle: Spinner is set");
                spinner.setVisibility(View.GONE);
                if (chartIsSet){
                    Log.d(TAG, "toggle: Chart is set");
                    chartView.setVisibility(View.GONE);
                }
                else throw new IllegalStateException("toggle: Chart has not been set yet");
            }
            else throw new IllegalStateException("toggle: Spinner has not been set yet");
            isToggled = false;
        }else{
            if (spinnerIsSet){ // Falta el caso de uso donde el gráfico ya fue construído
                try{
                    Log.d(TAG, "toggle: Checking if view is LinearLayout");
                    if (view instanceof LinearLayout){
                        spinner.setVisibility(View.VISIBLE);
                        // falta validar que createDataEntry no traiga null
                        Log.d(TAG, "toggle: Creating Pie");
                        AnyChartView anyChartView = new AnyChartView(appContext);
                        APIlib.getInstance().setActiveAnyChartView(anyChartView);
                        Pie pie = AnyChart.pie();
                        Log.d(TAG, "toggle: Setting pie data");
                        pie.data(data);
                        Log.d(TAG, "toggle: Creating AnyChartView & setting LayoutParams");
                        if (appContext == null)
                            Log.d(TAG, "toggle: appContext is null");

                        ScrollView.LayoutParams params = new ScrollView.LayoutParams(
                            ScrollView.LayoutParams.MATCH_PARENT,
                            ScrollView.LayoutParams.MATCH_PARENT
                        );
                        Log.d(TAG, "toggle: Setting AnyChartView layout parameters");
                        params.height = height;
                        params.setMargins(10, 10, 10, 10);
                        anyChartView.setLayoutParams(params);
                        Log.d(TAG, "toggle: Setting visibility");
                        anyChartView.setVisibility(View.INVISIBLE);
                        anyChartView.setChart(pie);
                        ((LinearLayout) view).addView(anyChartView);
                        this.chartView = anyChartView;
                        this.chartIsSet = true;
                        new Handler().postDelayed(new VisibilityRunnable(spinner, anyChartView, view),
                                visibilityTimeout);
                    }else{
                        throw new ClassCastException("'view' variable is not an instance of LinearLayout");
                    }
                }
                catch (IllegalArgumentException e){
                    Log.d(TAG, "toggle:" + e.toString());
                }
                catch (ClassCastException e){
                    Log.d(TAG, "toggle: view variable is not an instance of LinearLayout");
                }
                catch (Exception e){
                    Log.d(TAG, "toggle: Exception thrown: " + e.toString());
                }
            }
            else throw new IllegalStateException("Spinner has not been set yet");
            isToggled = true;
        }
    }

    public void setSpinner(ProgressBar progressBar){
        if (!spinnerIsSet) {
            this.spinner = progressBar;
            spinnerIsSet = true;
        }else
            this.spinner = progressBar;
    }

    private void getXmlData(String baseUrl, final RequestCallback callback){
        Log.d(TAG, "getXmlData: Setting up OkHttp3 Client");
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(username, password))
                .build();
        Log.d(TAG, "getXmlData: OkHttp3 client set!");
        Log.d(TAG, "getXmlData: Setting up Retrofit client");
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        Log.d(TAG, "getXmlData: Retrofit client set!");
        Log.d(TAG, "getXmlData: Creating PSMessage Call from PeoplesoftService interface");
        Call<PSMessage> call = retrofit.create(PeoplesoftService.class).getPSMessage();
        Log.d(TAG, "getXmlData: Executing Retrofit service call...");
        try{
            call.enqueue(new Callback<PSMessage>() {
                @Override
                public void onResponse(Call<PSMessage> call, Response<PSMessage> response) {
                    if (response.code() == 200){
                        Log.d(TAG, "getXmlData: Retrofit call executed successfully!");
                        if (response.body() != null &&
                                response.body().msgData != null &&
                                response.body().msgData.transactions != null){
                            Log.d(TAG, "getXmlData: Response body has content!");
                            AtomicInteger cont = new AtomicInteger(1);
                            response.body().msgData.transactions.forEach(item -> {
                                if (!item.signonCountStr.equals(""))
                                    Log.d(TAG, "getXmlData: Transactions' item n°" +
                                            cont.get() + " is not null or empty");
                                else
                                    Log.d(TAG, "getXmlData: Transactions' item n°" +
                                            cont.get() + " is either null or empty");
                                cont.getAndIncrement();
                            });
                            callback.onGetXmlData(response.body());
                        }
                        else if (response.body() == null)
                            Log.d(TAG, "getXmlData: Response body (PSMessage) is null");
                        else if (response.body().msgData == null)
                            Log.d(TAG, "getXmlData: Response body MsgData is null");
                        else if (response.body().msgData.transactions == null)
                            Log.d(TAG, "getXmlData: Response body MsgData.transactions is null");
                    } else if (response.code() == 400)
                        Log.d(TAG, "getXmlData: Retrofit call executed, return code is 400");
                    else{
                        Log.d(TAG, "getXmlData: Retrofit call executed. " +
                                "Response code is " + response.code());
                        Log.d(TAG, "getXmlData: Headers: " +
                                response.raw().headers().toString());
                        Log.d(TAG, "getXmlData: Message: " + response.message());
                        Log.d(TAG, "getXmlData: Body: " + response.raw().body());
                    }
                }

                @Override
                public void onFailure(Call<PSMessage> call, Throwable t) {
                    Log.d(TAG, "getXmlData: Retrofit call failed, onFailure method called");
                    Log.d(TAG, "getXmlData: Throwable message: " + t.getMessage());
                }
            });
        }catch (RuntimeException e){
            Log.d(TAG, "getXmlData: RuntimeException thrown - Likely error decoding the " +
                    "response or creating the request");
        }catch (Exception e){
            Log.d(TAG, "getXmlData: Exception thrown: " + e.toString());
        }
        Log.d(TAG, "getXmlData: getXmlData method finished");
    }

    private List<DataEntry> createDataEntry(PSMessage psMessage) throws IllegalArgumentException{
        Log.d(TAG, "createDataEntry: init");
        List<DataEntry> data = new ArrayList<>();
        psMessage.msgData.transactions.forEach(item -> {
            item.rCount = Integer.parseInt(item.signonCountStr);
            final boolean add;
            switch (item.signonType){
                case "0":
                    add = data.add(new ValueDataEntry("Application Designer login", item.rCount));
                    if (add)
                        Log.d(TAG, "createDataEntry: Application Designer login data added");
                    else
                        Log.d(TAG, "createDataEntry: Error on adding Application Designer login data");
                    break;
                case "1":
                    add = data.add(new ValueDataEntry("Portal login", item.rCount));
                    if (add)
                        Log.d(TAG, "createDataEntry: Portal login data added");
                    else
                        Log.d(TAG, "createDataEntry: Error on adding Portal login data");
                    break;
                default:
                    throw new IllegalArgumentException("Unconsidered switch case possibility found");
            }
        });
        return data;
    }

}
