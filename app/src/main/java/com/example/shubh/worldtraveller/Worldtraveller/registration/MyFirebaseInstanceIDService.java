package com.example.shubh.worldtraveller.Worldtraveller.registration;

import android.util.Log;

import com.example.shubh.worldtraveller.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.Objects;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {

        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference mOnlineDatabase;
        mOnlineDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile))
                .child(mAuth.getCurrentUser().getUid());

        mOnlineDatabase.child("device_token").setValue(token);
    }
}
