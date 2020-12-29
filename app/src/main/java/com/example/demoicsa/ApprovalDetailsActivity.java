package com.example.demoicsa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoicsa.approval.Approval;
import com.example.demoicsa.psmessage.AuthInterceptor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/* ---------------------------------------------------------
                    API interface & classes
   --------------------------------------------------------- */
interface ApprovalActionService{
    @Headers({"Content-Type: text/xml"})
    @POST("PSIGW/RESTListeningConnector/PSFT_HR/AND_APPROVAL_ACTION.v1/POST")
    Call<ResponseBody> postAction(@Body RequestBody body);
}

public class ApprovalDetailsActivity extends AppCompatActivity {
    private static final String TAG = "ApprovalDetailsActivity";

    OkHttpClient client;
    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String cTAG = "onCreate: ";
        Log.d(TAG, cTAG + "init");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        List<Approval> approvals = null;
        if ((approvals = ((GlobalApplication) getApplication()).getDirectionApprovals()) != null){
            Log.d(TAG, cTAG + "approvals not null");
            if (((GlobalApplication) getApplication()).approvalIdToSave != -1){
                Log.d(TAG, cTAG + "approvalIdToSave not -1");
                Approval approvalItem = approvals.get(((GlobalApplication) getApplication()).approvalIdToSave);
                ((TextView) findViewById(R.id.approval_details_approval_type))
                        .setText(approvalItem.getApprovalType(getApplicationContext()));
                ((TextView) findViewById(R.id.approval_details_datecreated))
                        .setText(approvalItem.getDateCreated());
                ((TextView) findViewById(R.id.approval_details_emplid))
                        .setText(approvalItem.emplid);
                ((TextView) findViewById(R.id.approval_details_name))
                        .setText(approvalItem.getFullName());
                ((TextView) findViewById((R.id.approval_details_username)))
                        .setText(approvalItem.reqOprid);
                ((TextView) findViewById(R.id.approval_details_data_change))
                        .setText(approvalItem.getApprovalType(getApplicationContext()));
                ((TextView) findViewById(R.id.approval_details_old_data))
                        .setText(approvalItem.getAddressOld());
                ((TextView) findViewById(R.id.approval_details_new_data))
                        .setText(approvalItem.getAddressNew());
                setupHttpClient();
                setupRetrofit("http://201.234.130.156:8000/");
                ((Button) findViewById(R.id.approval_btn_approve)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String body = setupBodyContent(approvalItem.emplid, approvalItem.addressType,
                                approvalItem.effSeq, approvalItem.actionDate, "A", approvalItem.stepInstance);
                        Log.d(TAG, cTAG + body);
                        RequestBody requestBody = RequestBody.create(MediaType.parse("text/xml"), body);
                        Call<ResponseBody> call = retrofit.create(ApprovalActionService.class).postAction(requestBody);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.code() == 200){
                                    try {
                                        if (response.body().string() == "TRUE") {
                                            Toast.makeText(getApplicationContext(), "Solicitud aprobada", Toast.LENGTH_LONG)
                                                    .show();
                                            Log.d(TAG, cTAG + "response body equals TRUE");
                                            GlobalApplication app = ((GlobalApplication) getApplication());
                                            List<Approval> newApprovals = app.getDirectionApprovals();
                                            newApprovals.remove(app.approvalIdToSave);
                                            app.setDirectionApprovals(newApprovals);
                                            /*
                                            ------------------ PARCHE - MEJORAR
                                             */
                                            Intent approvalsIntent = new Intent(ApprovalDetailsActivity.this, ApprovalActivity.class);
                                            startActivity(approvalsIntent);
                                        }
                                        else if(response.body().string() == "FALSE") {
                                            Toast.makeText(getApplicationContext(), "Error al aprobar solicitud", Toast.LENGTH_LONG)
                                                    .show();
                                            Log.d(TAG, cTAG + "response body equals FALSE");
                                            GlobalApplication app = ((GlobalApplication) getApplication());
                                            List<Approval> newApprovals = app.getDirectionApprovals();
                                            newApprovals.remove(app.approvalIdToSave);
                                            app.setDirectionApprovals(newApprovals);
                                            /*
                                            ------------------ PARCHE - MEJORAR
                                             */
                                            Intent approvalsIntent = new Intent(ApprovalDetailsActivity.this, ApprovalActivity.class);
                                            startActivity(approvalsIntent);
                                        }
                                        else{
                                            GlobalApplication app = ((GlobalApplication) getApplication());
                                            List<Approval> newApprovals = app.getDirectionApprovals();
                                            newApprovals.remove(app.approvalIdToSave);
                                            app.setDirectionApprovals(newApprovals);
                                            /*
                                            ------------------ PARCHE - MEJORAR
                                             */
                                            Intent approvalsIntent = new Intent(ApprovalDetailsActivity.this, ApprovalActivity.class);
                                            startActivity(approvalsIntent);
                                        }
                                    } catch (Exception e){
                                        Toast.makeText(getApplicationContext(), "Error al aprobar la solicitud", Toast.LENGTH_LONG)
                                            .show();
                                        Log.d(TAG, cTAG + e.toString());
                                    }

                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), "El mensaje HTTP falló", Toast.LENGTH_LONG)
                                        .show();
                                Log.d(TAG, cTAG + t.getMessage());
                            }
                        });
                    }
                });
                ((Button) findViewById(R.id.approval_btn_reject)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String body = setupBodyContent(approvalItem.emplid, approvalItem.addressType,
                                approvalItem.effSeq, approvalItem.actionDate, "R", approvalItem.stepInstance);
                        Log.d(TAG, cTAG + body);
                        RequestBody requestBody = RequestBody.create(MediaType.parse("text/xml"), body);
                        Call<ResponseBody> call = retrofit.create(ApprovalActionService.class).postAction(requestBody);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.code() == 200){
                                    try{
                                        if (response.body().string() == "TRUE") {
                                            Toast.makeText(getApplicationContext(), "Solicitud rechazada", Toast.LENGTH_LONG)
                                                    .show();
                                            Log.d(TAG, cTAG + "response body equals TRUE");
                                            GlobalApplication app = ((GlobalApplication) getApplication());
                                            List<Approval> newApprovals = app.getDirectionApprovals();
                                            newApprovals.remove(app.approvalIdToSave);
                                            app.setDirectionApprovals(newApprovals);
                                            /*
                                            ------------------ PARCHE - MEJORAR
                                             */
                                            Intent approvalsIntent = new Intent(ApprovalDetailsActivity.this, ApprovalActivity.class);
                                            startActivity(approvalsIntent);
                                        }
                                        else if(response.body().string() == "FALSE") {
                                            Toast.makeText(getApplicationContext(), "Error al rechazar solicitud", Toast.LENGTH_LONG)
                                                    .show();
                                            Log.d(TAG, cTAG + "response body equals FALSE");
                                            finish();
                                            GlobalApplication app = ((GlobalApplication) getApplication());
                                            List<Approval> newApprovals = app.getDirectionApprovals();
                                            newApprovals.remove(app.approvalIdToSave);
                                            app.setDirectionApprovals(newApprovals);
                                            /*
                                            ------------------ PARCHE - MEJORAR
                                             */
                                            Intent approvalsIntent = new Intent(ApprovalDetailsActivity.this, ApprovalActivity.class);
                                            startActivity(approvalsIntent);
                                        }
                                        else{
                                            GlobalApplication app = ((GlobalApplication) getApplication());
                                            List<Approval> newApprovals = app.getDirectionApprovals();
                                            newApprovals.remove(app.approvalIdToSave);
                                            app.setDirectionApprovals(newApprovals);
                                            /*
                                            ------------------ PARCHE - MEJORAR
                                             */
                                            Intent approvalsIntent = new Intent(ApprovalDetailsActivity.this, ApprovalActivity.class);
                                            startActivity(approvalsIntent);
                                        }
                                    }catch (Exception e){
                                        Toast.makeText(getApplicationContext(), "Error al rechazar la solicitud", Toast.LENGTH_LONG)
                                            .show();
                                        Log.d(TAG, cTAG + e.toString());
                                    }

                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), "El mensaje HTTP falló", Toast.LENGTH_LONG)
                                        .show();
                                Log.d(TAG, cTAG + t.getMessage());
                            }
                        });
                    }
                });
            }
            else
                Log.d(TAG, cTAG + "approvalIdToSave is -1");
        }
        else
            Log.d(TAG, cTAG + "approvals are null");
    }

    private String setupBodyContent(String emplid, String addressType, String effSeq, String actionDate, String action, String stepInstance) {
        return "<?xml version='1.0'?>" +
                "<PSmessage>" +
                "<MsgData>" +
                "<Transaction>" +
                "<AND_APPACTN_WRK>" +
                "<EMPLID>" + emplid + "</EMPLID>" +
                "<ADDRESS_TYPE>" + addressType + "</ADDRESS_TYPE>" +
                "<EFFSEQ>" + effSeq + "</EFFSEQ>" +
                "<ACTION_DATE>" + actionDate + "</ACTION_DATE>" +
                "<ACTION>" + action + "</ACTION>" +
                "<INSTANCE>" + stepInstance + "</INSTANCE>" +
                "</AND_APPACTN_WRK>" +
                "</Transaction>" +
                "</MsgData>" +
                "</PSmessage>";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupHttpClient(){
        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(
                        new AuthInterceptor(getString(R.string.rest_login_user),
                                getString(R.string.rest_login_pwd)
                        ))
                .build();
    }

    private void setupRetrofit(String baseUrl){
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
    }
}