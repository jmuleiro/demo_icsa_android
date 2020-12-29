package com.example.demoicsa.approval;

import android.util.Log;

import com.example.demoicsa.psmessage.AuthInterceptor;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
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
interface PeoplesoftService{
    @Headers({"Content-Type: text/xml"})
    @POST("PSIGW/RESTListeningConnector/PSFT_HR/AND_APPROVAL_REQUEST.v1/AND_APPROVAL_REQUEST")
    Call<PSMessage> getPSMessage(@Body RequestBody body);
}

@Root(name="PSMessage", strict = false)
class PSMessage{
    @Element(name = "MsgData")
    MsgData msgData;
}

@Root(name="MsgData", strict = false)
class MsgData{
    @ElementList(name = "Transaction", inline = true)
    List<Transaction> transactions;
}

@Root(name="Transaction", strict = false)
class Transaction{
    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "EMPLID", required = false)
    String emplid = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "EOAWREQUESTOR_ID", required = false)
    String reqOprid = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "FIRST_NAME", required = false)
    String firstName = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "MIDDLE_NAME", required = false)
    String middleName = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "LAST_NAME", required = false)
    String lastName = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "SECOND_LAST_NAME", required = false)
    String secondLastName = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "DTTM_CREATED", required = false)
    String dttmCreated = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS1", required = false)
    String address1Old = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS2", required = false)
    String address2Old = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS3", required = false)
    String address3Old = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS4", required = false)
    String address4Old = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS1_AC", required = false)
    String address1New = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS2_AC", required = false)
    String address2New = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS3_AC", required = false)
    String address3New = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS4_AC", required = false)
    String address4New = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ADDRESS_TYPE", required = false)
    String addressType = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "ACTION_DATE", required = false)
    String actionDate = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "EFFSEQ", required = false)
    String effSeq = "";

    @Path(value = "./AND_APPRESP_WRK")
    @Element(name = "EOAWUSTEP_INST_ID", required = false)
    String stepInstance;
}

/* ---------------------------------------------------------
                    Callback interface
   --------------------------------------------------------- */
interface RequestCallback{
    void onGetXmlData(PSMessage psMessage);
    void onError();
}


public class RESTApprovalHelper {
    private static final String TAG = "RESTApprovalHelper";

    /*
    * Essential Properties
     */
    private String loggedUser;
    private String baseUrl;
    private List<Approval> approvals;
    private String restUsername;
    private String restPassword;

    public RESTApprovalHelper(String baseUrl, String loggedUser, String username, String password){
        Log.d(TAG, "Constructor: init");
        this.baseUrl = baseUrl;
        this.loggedUser = loggedUser;
        this.restUsername = username;
        this.restPassword = password;
    }

    public void getApprovalData(final RequestCallback callback){
        final String cTAG = "getApprovalData: ";
        Log.d(TAG, cTAG + "init");
        Log.d(TAG, cTAG + "setting up OkHttpClient");

        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(restUsername, restPassword))
                .build();
        Log.d(TAG, cTAG + "OkHttp3 client set!");
        Log.d(TAG, cTAG + "setting up Retrofit");
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(this.baseUrl)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        Log.d(TAG, cTAG + "Retrofit client set!");
        Log.d(TAG, cTAG + "Creating PeopleSoft service API");
        String postContent ="<?xml version='1.0'?>"+
                "<PSmessage>" +
                "<MsgData>" +
                "<Transaction>" +
                "<APPROVAL_REQUEST>" +
                "<user>"+ loggedUser +"</user>" +
                "</APPROVAL_REQUEST>" +
                "</Transaction>" +
                "</MsgData>" +
                "</PSmessage>";
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/xml"), postContent);
        Call<PSMessage> call = retrofit.create(PeoplesoftService.class)
                .getPSMessage(requestBody);
        Log.d(TAG, cTAG + "PeopleSoft service API created");
        try{
            call.enqueue(new Callback<PSMessage>() {
                @Override
                public void onResponse(Call<PSMessage> call, Response<PSMessage> response) {
                    final String cTAG = "getApprovalData.Callback.onResponse: ";
                    if (response.code() == 200){
                        Log.d(TAG, cTAG + "Retrofit call executed successfully!");
                        if (response.body() != null &&
                                response.body().msgData != null &&
                                response.body().msgData.transactions != null){
                            Log.d(TAG, cTAG + "Response body has content!");
                            AtomicInteger cont = new AtomicInteger(1);
                            response.body().msgData.transactions.forEach(item -> {
                                if (!item.emplid.equals(""))
                                    Log.d(TAG, cTAG + "Transactions' item n°" +
                                            cont.get() + " is not null or empty");
                                else
                                    Log.d(TAG, cTAG + "Transactions' item n°" +
                                            cont.get() + " is either null or empty");
                                cont.getAndIncrement();
                            });
                            callback.onGetXmlData(response.body());
                        }
                        else if (response.body() == null)
                            Log.d(TAG, cTAG + "Response body (PSMessage) is null");
                        else if (response.body().msgData == null)
                            Log.d(TAG, cTAG + "Response msgData is null");
                        else if (response.body().msgData.transactions == null)
                            Log.d(TAG, cTAG + "Response body MsgData.transactions is null");
                    }
                    else if (response.code() == 400)
                        Log.d(TAG, cTAG + "Response code is 400");
                    else{
                        Log.d(TAG, cTAG + "Retrofit call executed. " +
                                "Response code is " + response.code());
                        Log.d(TAG, cTAG + "Headers: "
                                + response.raw().headers().toString());
                        Log.d(TAG, cTAG + "Message: " + response.message());
                        Log.d(TAG, cTAG + "Body: " + response.raw().body());
                    }
                }

                @Override
                public void onFailure(Call<PSMessage> call, Throwable t) {
                    Log.d(TAG, cTAG + "Retrofit call failed, onFailure method called");
                    Log.d(TAG, cTAG + "Throwable message: " + t.getMessage());
                }
            });
        }
        catch (RuntimeException e){
            Log.d(TAG, "getApprovalData: RuntimeException thrown - Likely error decoding " +
                    "the response or creating the request");
        }
        catch (Exception e){
            Log.d(TAG, "getApprovalData Exception: " + e.toString());
        }
        Log.d(TAG, "getApprovalData: getXmlData method finished");
    }

    public void get(ApprovalCallback callback){
        final String cTAG = "get: ";
        Log.d(TAG, cTAG + "init");
        getApprovalData(new RequestCallback() {
            @Override
            public void onGetXmlData(PSMessage psMessage) {
                Log.d(TAG, cTAG + "onGetXmlData called");
                ArrayList<Approval> approvalData = new ArrayList<>();
                int cont = 1;
                for (Transaction item : psMessage.msgData.transactions) {
                    Approval approval = new Approval();
                    approval.emplid = item.emplid;
                    approval.reqOprid = item.reqOprid;
                    approval.stepInstance = item.stepInstance;
                    approval.addressType = item.addressType;
                    approval.actionDate = item.actionDate;
                    approval.effSeq = item.effSeq;
                    approval.setAddressOld(item.address1Old, item.address2Old, item.address3Old,
                            item.address4Old);
                    approval.setAddressNew(item.address1New, item.address2New, item.address3New, item.address4New);
                    approval.setDateCreated(item.dttmCreated);
                    approval.setFullName(item.firstName, item.middleName, item.lastName, item.secondLastName);
                    approval.setApprovalType(1);
                    boolean add = approvalData.add(approval);
                    if (add)
                        Log.d(TAG, cTAG + "Approval n°" + cont +
                                " added successfully to list");
                    else
                        Log.d(TAG, cTAG + "Approval n°" + cont +
                                " couldn't be added to list");
                    cont++;
                }
                approvals = approvalData;
                callback.run(approvals);
            }

            @Override
            public void onError() {
                // ignore for now
            }
        });


    }

}
