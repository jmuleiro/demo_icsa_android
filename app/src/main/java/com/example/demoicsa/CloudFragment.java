package com.example.demoicsa;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.demoicsa.psmessage.PSMessage;
import com.example.demoicsa.psmessage.Transaction;

import org.jetbrains.annotations.NotNull;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

/* PSoft service interface & classes */
interface PeoplesoftService{
    @GET("PSIGW/RESTListeningConnector/PSFT_HR/ANDROID_TEST_2_ECHO.v1/echo/")
    Call<PSMessage<AccessLogTransaction>> getPSMessage();
}

class AccessLogTransaction extends Transaction{
    @Path(value = "./AND_RESP_RST")
    @Element(name = "DESCR_X")
    String signonCountStr;

    @Path(value = "./AND_RESP_RST")
    @Element(name = "PT_SIGNON_TYPE")
    String signonType;
}

/* This class is not mandatory for the helper but for the method that will toggle the chart */
class VisibilityRunnable implements Runnable{
    private ProgressBar _spinner;
    private AnyChartView _chart;
    public VisibilityRunnable(ProgressBar _spinner, AnyChartView _chart){
        this._spinner = _spinner;
        this._chart = _chart;
    }

    @Override
    public void run() {
        _chart.setVisibility(View.VISIBLE);
        _spinner.setVisibility(View.INVISIBLE);
    }
}

class authInterceptor implements Interceptor{
    private String credentials;

    public authInterceptor(String user, String password){
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

interface RequestCallback{
    void onGetXmlData(PSMessage<AccessLogTransaction> psMessage);
    void onError();
}

public class CloudFragment extends Fragment {

    private View currentView;
    private Integer appDesignerLogin;
    private Integer webLogin;

    public void getXmlData(String bUrl, final RequestCallback callback){
        PeoplesoftService service;

        Log.d("DEMOICSADEBUG", "Setting up & building OkHttpClient...");

        /* I think this is not needed anymore */
        Strategy strategy = new VisitorStrategy(new Visitor() {
            @Override
            public void read(Type type, NodeMap<InputNode> node) throws Exception {

            }
            @Override
            public void write(Type type, NodeMap<OutputNode> node) throws Exception {
                if ("AND_RESP_RST".equals(node.getName()))
                    node.remove("class");
            }
        });

        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .readTimeout(0, TimeUnit.SECONDS)
                .addInterceptor(new authInterceptor("JBELTRAN", "JBELTRAN01"))
                .build();
        Log.d("DEMOICSADEBUG", "Setting up & building retrofit client...");
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(bUrl)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        Log.d("DEMOICSADEBUG", "Creating PeoplesoftService interface object...");
        service = retrofit.create(PeoplesoftService.class);
        Call<PSMessage<AccessLogTransaction>> call = service.getPSMessage();
        try{
            Log.d("DEMOICSADEBUG", "Executing retrofit service call...");
            call.enqueue(new Callback<PSMessage<AccessLogTransaction>>(){
                @Override
                public void onResponse(Call<PSMessage<AccessLogTransaction>> call, Response<PSMessage<AccessLogTransaction>> response) {
                    if (response.code() == 200) {
                        Log.d("DEMOICSADEBUG", "Retrofit call executed successfully");
                        int[] cont = {1};
                        response.body().msgData.transactions.forEach(item -> {
                            if (item == null)
                                Log.d("DEMOICSADEBUG1",
                                        "psMessage.msgData.transaction.rowList " + cont[0]
                                                + " is null");
                            cont[0] += 1;
                        });
                        callback.onGetXmlData(response.body());
                    }else if (response.code() == 400){
                        Log.d("DEMOICSADEBUG", "Retrofit call executed, return code 400");
                    }else{
                        Log.d("DEMOICSADEBUG", "Retrofit call executed, return code: " + response.code());
                        Log.d("DEMOICSADEBUG", response.raw().headers().toString());
                    }
                }

                @Override
                public void onFailure(Call<PSMessage<AccessLogTransaction>> call, Throwable t) {
                    Log.d("DEMOICSADEBUG", "Retrofit call failed, onFailure method called");
                    Log.d("DEMOICSADEBUG", t.toString());
                    t.printStackTrace();
                }
            });
        }catch (RuntimeException e){
            Log.d("DEMOICSADEBUG", "RuntimeException thrown - Error decoding the response " +
                    "or creating the request");
        }
        catch (Exception e){
            Log.d("DEMOICSADEBUG", "Some other exception thrown");
            e.printStackTrace();
        }
        Log.d("DEMOICSADEBUG", "getXmlData returned");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View vw = inflater.inflate(R.layout.fragment_cloud, container, false);
        currentView = vw;
        int visibilityTimeout = 4000;
        ProgressBar spinner;
        spinner = (ProgressBar) vw.findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE); // hacer visible la progressbar
        String sUrl = "http://201.234.130.156:8000/";
        int height = 2170;
        if (savedInstanceState == null){
            getXmlData(sUrl, new RequestCallback() {
                @Override
                public void onGetXmlData(PSMessage<AccessLogTransaction> psMessage) {
                    Log.d("DEMOICSADEBUG", "Callback invoked");
                    List<DataEntry> data = new ArrayList<>();
                    try {
                        ConstraintLayout myLayout = vw.findViewById(R.id.myLayout);
                        if (psMessage == null)
                            Log.d("DEMOICSADEBUG", "psMessage is null");
                        else{
                            if (psMessage.msgData == null)
                                Log.d("DEMOICSADEBUG", "psMessage.msgData is null");
                            else{
                                Log.d("DEMOICSADEBUG",
                                        "psMessage.msgData is null");
                            }
                        }
                        psMessage.msgData.transactions.forEach(item ->{
                            if (item.signonCountStr != null) {
                                item.rCount = Integer.parseInt(item.signonCountStr);
                                String loginType = "";
                                switch (item.signonType) {
                                    case "0":
                                        loginType = "Application Designer login";
                                        appDesignerLogin = item.rCount;
                                        break;
                                    case "1":
                                        loginType = "Web login";
                                        webLogin = item.rCount;
                                        break;
                                }
                                final boolean add = data.add(new ValueDataEntry(loginType,
                                        item.rCount));
                                Log.d("DEMOICSADEBUG", "item.rowList not null");
                            }
                            else
                                Log.d("DEMOICSADEBUG", "item is null");
                        });
                        Pie pie = AnyChart.pie();
                        AnyChartView anyChartView = new AnyChartView(inflater.getContext());
                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        );
                        params.height = height;
                        anyChartView.setLayoutParams(params);
                        pie.data(data);
                        anyChartView.setChart(pie);
                        myLayout.addView(anyChartView);
                        new Handler().postDelayed(new VisibilityRunnable(spinner, anyChartView), visibilityTimeout);
                        Log.d("DEMOICSADEBUG", "data values set");
                        currentView = inflater.inflate(R.layout.fragment_cloud, container, false);
                        Log.d("DEMOICSADEBUG", "inflated");
                    } catch (Exception e){
                        Log.d("DEMOICSADEBUG", "Exception thrown");
                        Log.d("DEMOICSADEBUG", e.toString());
                    }

                }
                @Override
                public void onError() {
                    // i dont even care lol
                }
            });
        }else{
            List<DataEntry> data = new ArrayList<>();
            ConstraintLayout myLayout = vw.findViewById(R.id.myLayout);
            appDesignerLogin = savedInstanceState.getInt("appDesignerLogin", -1);
            webLogin = savedInstanceState.getInt("webLogin", -1);
            if (appDesignerLogin != -1 && webLogin != -1){
                Pie pie = AnyChart.pie();
                AnyChartView anyChartView = new AnyChartView(inflater.getContext());
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                params.height = height;
                anyChartView.setLayoutParams(params);
                data.add(new ValueDataEntry("Application Designer login", appDesignerLogin));
                data.add(new ValueDataEntry("Web login", webLogin));
                pie.data(data);
                anyChartView.setChart(pie);
                myLayout.addView(anyChartView);
                new Handler().postDelayed(new VisibilityRunnable(spinner, anyChartView), visibilityTimeout);
            }else{
                Toast errorToast = Toast.makeText(currentView.getContext(), "Error al recaudar datos de consulta" +
                        " REST anterior. Por favor reinicie la aplicación.", Toast.LENGTH_LONG);
                errorToast.show();
            }

        }
        return currentView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("appDesignerLogin", appDesignerLogin);
        outState.putInt("webLogin", webLogin);
    }
}