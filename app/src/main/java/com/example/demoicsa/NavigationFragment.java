package com.example.demoicsa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NavigationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vw = inflater.inflate(R.layout.fragment_navigation, container, false);
        // Charts CardView OnClickListener
        vw.findViewById(R.id.card_charts).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent chartsIntent = new Intent(getContext(), ChartsMainActivity.class);
                startActivity(chartsIntent);
            }
        });

        // Aprobaciones CardView OnClickListener
        vw.findViewById(R.id.card_aprobar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent approvalsIntent = new Intent(getContext(), ApprovalActivity.class);
                startActivity(approvalsIntent);
            }
        });
        return vw;
    }

    public void aprobac(View view){

    }
}
