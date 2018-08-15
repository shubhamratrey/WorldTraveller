package com.example.shubh.worldtraveller.Worldtraveller.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.view.Dialog_Verification;

/**
 * Created by Shubh on 07/03/2018.
 */

public class Frag_phone extends Fragment {
    Button FragVerfyBtn;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_phone,container,false);

        return view;



    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();
        FragVerfyBtn = v.findViewById(R.id.frag_verfy_btn);
        FragVerfyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Verification alert = new Dialog_Verification();
                alert.showDialog(getActivity(), "Enter your 6 digit code.");

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}