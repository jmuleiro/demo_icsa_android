package com.example.demoicsa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoicsa.approval.Approval;
import com.example.demoicsa.approval.ApprovalCallback;
import com.example.demoicsa.approval.RESTApprovalHelper;

import java.util.List;

public class ApprovalActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private LinearLayout scrollerView;
    private static final String TAG = "ApprovalActivity";
    private int idToSave = 0;
    private boolean justCreated = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        scrollerView = findViewById(R.id.approval_layout);
        progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.customview_progressbar_large,
                scrollerView, false);
        scrollerView.addView(progressBar);
        progressBar.setVisibility(View.VISIBLE);
        final String cTAG = "onCreate: ";
        Log.d(TAG, cTAG + "init");
        String loggedUser = "";
        if (justCreated) {
            try {
                Log.d(TAG, cTAG + "gathering username");
                loggedUser = ((GlobalApplication) getApplication()).getUsername();
                assert loggedUser != null;
                if (!loggedUser.equals("")) {
                    Log.d(TAG, cTAG + "loggedUser found, init RESTApprovalHelper");
                    RESTApprovalHelper approvalHelper =
                            new RESTApprovalHelper("http://201.234.130.156:8000/", loggedUser,
                                    getString(R.string.rest_login_user), getString(R.string.rest_login_pwd));
                    Log.d(TAG, cTAG + "approvalHelper.get called");
                    approvalHelper.get(new ApprovalCallback() {
                        @Override
                        public void run(List<Approval> approvals) {
                            int cont = 0;
                            Log.d(TAG, cTAG + "foreach initialised");
                            scrollerView.removeAllViews();
                            for (Approval approval : approvals) {
                                if (approval.emplid != "") {
                                    Log.d(TAG, cTAG + "inflating approval item layout for item n°" + cont);
                                    LinearLayout linearLayout = (LinearLayout) getLayoutInflater()
                                            .inflate(R.layout.customview_approval_item, scrollerView, false);
                                    approval.layoutId = cont;
                                    TextView titleView = linearLayout.findViewById(R.id.customview_approval_item_text_container_layout)
                                            .findViewById(R.id.customview_approval_item_title);
                                    titleView.setText(approval.getFullName());
                                    titleView = linearLayout.findViewById(R.id.customview_approval_item_text_container_layout)
                                            .findViewById(R.id.customview_approval_item_subtitle);
                                    titleView.setText(getString(R.string.approval_type_address_change));
                                    int finalCont = cont;
                                    linearLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent approvalIntent = new Intent(ApprovalActivity.this,
                                                    ApprovalDetailsActivity.class);
                                            idToSave = finalCont;
                                            ((GlobalApplication) getApplication()).approvalIdToSave = idToSave;
                                            Log.d(TAG, cTAG + "idToSave value: " + idToSave);
                                            startActivity(approvalIntent);
                                        }
                                    });
                                    scrollerView.addView(linearLayout);
                                    progressBar.setVisibility(View.GONE);
                                } else
                                    Log.d(TAG, cTAG + "Approval n°" + cont + " has no EMPLID");
                                cont++;
                            }
                            ((GlobalApplication) getApplication()).setDirectionApprovals(approvals);
                        }
                    });

                } else {
                    Log.d(TAG, cTAG + "loggedUser variable is empty");
                    Toast.makeText(getApplicationContext(), "Error: username not found in cache",
                            Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                Log.d(TAG, cTAG + "user not found in savedInstanceState");
                Log.d(TAG, cTAG + e.toString());
                Toast.makeText(getApplicationContext(), "Error: username not found in cache",
                        Toast.LENGTH_LONG).show();
            }
        }
        justCreated = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: pausing");

    }

    @Override
    protected void onResume() {
        super.onResume();
        final String cTAG = "onResume: ";
        Log.d(TAG, cTAG + "resumed");
        List<Approval> approvals = ((GlobalApplication) getApplication()).getDirectionApprovals();
        int cont = 0;
        scrollerView.removeAllViews();
        if (approvals != null) {
            Log.d(TAG, cTAG + "GlobalApplication.getDirectionApprovals isn't null");
            for (Approval approval : approvals){
                if (approval.emplid != ""){
                    LinearLayout linearLayout = (LinearLayout) getLayoutInflater()
                            .inflate(R.layout.customview_approval_item, scrollerView, false);
                    approval.layoutId = cont;
                    TextView titleView = linearLayout.findViewById(R.id.customview_approval_item_text_container_layout)
                            .findViewById(R.id.customview_approval_item_title);
                    titleView.setText(approval.getFullName());
                    titleView = linearLayout.findViewById(R.id.customview_approval_item_text_container_layout)
                            .findViewById(R.id.customview_approval_item_subtitle);
                    titleView.setText(getString(R.string.approval_type_address_change));
                    int finalCont = cont;
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent approvalIntent = new Intent(ApprovalActivity.this,
                                    ApprovalDetailsActivity.class);
                            idToSave = finalCont;
                            ((GlobalApplication) getApplication()).approvalIdToSave = idToSave;
                            Log.d(TAG, cTAG + "idToSave value: " + idToSave);
                            startActivity(approvalIntent);
                        }
                    });
                    scrollerView.addView(linearLayout);
                    progressBar.setVisibility(View.GONE);
                }
                else {
                    Log.d(TAG, cTAG + "Approval n°" + cont + " has no EMPLID");
                }
                cont++;
                Log.d(TAG, cTAG + "For - Approval n°" + cont + " processed");
            }
        }
        else{
            if (!justCreated) {
                Log.d(TAG, cTAG + "GlobalApplication.getDirectionApprovals is null");
                TextView textView = (TextView) getLayoutInflater()
                        .inflate(R.layout.customview_no_more_approvals, scrollerView, false);
                scrollerView.addView(textView);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}