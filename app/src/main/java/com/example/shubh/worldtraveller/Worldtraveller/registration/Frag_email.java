package com.example.shubh.worldtraveller.Worldtraveller.registration;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.FirebaseMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by shubham on 8/21/2015.
 */
public class Frag_email extends Fragment implements View.OnClickListener {

    private static final String TAG = "frag_email";
    EditText fragEmailedttxt, fragPasswordedttxt;
    Button fragSignupBtn;
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String append = "";
    private String userID;
    private FirebaseUser mUser;
    private ProgressBar progressBar;
    private String email, username, password;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_email, container, false);

        //
        progressBar = v.findViewById(R.id.progressBar);
        fragSignupBtn = v.findViewById(R.id.frag_signup_btn);
        fragEmailedttxt = v.findViewById(R.id.frag_email_edittext);
        fragPasswordedttxt = v.findViewById(R.id.frag_password_edittext);

        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        fragSignupBtn.setOnClickListener(this);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = fragEmailedttxt.getText().toString();
        if (TextUtils.isEmpty(email)) {
            fragEmailedttxt.setError("Required.");
            valid = false;
        } else {
            fragEmailedttxt.setError(null);
        }

        String password = fragPasswordedttxt.getText().toString();
        if (TextUtils.isEmpty(password)) {
            fragPasswordedttxt.setError("Required.");
            valid = false;
        } else {
            fragPasswordedttxt.setError(null);
        }
        return valid;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.frag_signup_btn) {
            LoginUser(fragEmailedttxt.getText().toString(), fragPasswordedttxt.getText().toString());
        }
    }

    private void LoginUser(String email, final String password) {
        if (!validateForm()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(),
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(getActivity(), Registration_Details.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Authentication failed#" + task.getException(),
                            Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}


