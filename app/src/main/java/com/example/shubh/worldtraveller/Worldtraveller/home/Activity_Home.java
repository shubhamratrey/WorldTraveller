package com.example.shubh.worldtraveller.Worldtraveller.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.BottomNavigationViewHelper;
import com.example.shubh.worldtraveller.Worldtraveller.registration.Activity_SignIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Activity_Home extends AppCompatActivity {

    private static final String TAG = "Activity_Home";
    private static final int ACTIVITY_NUM = 0;
    //The "x" and "y" position of the "Show Button" on screen.

    private Point p;
    private LinearLayout ln;
    private FrameLayout fm;

    private Context mContext = Activity_Home.this;
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting");

        ImageView notification = (ImageView) findViewById(R.id.home_notification_bell);

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,Activity_Notification.class);
                startActivity(intent);
            }
        });
        //inialize
        setupBottomNavigationView();
        setupFirebaseAuth();

        Button btn = (Button) findViewById(R.id.btnmap);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editTextId);
                String location = editText.getText().toString();
                location(location);
            }
        });


        ///////////////////////////////////////////////

        if (mAuth.getCurrentUser() != null) {


            mUserDatabase = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_users))
                    .child(getString(R.string.dbname_profile))
                    .child(mAuth.getCurrentUser().getUid());

        }


        ln = (LinearLayout) findViewById(R.id.snippet_post);
        fm = (FrameLayout) findViewById(R.id.post_frame_layout);

        final boolean[] showingFirst = {true};
        fm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showingFirst[0] == true){
                    ln.setVisibility(View.VISIBLE);
                    showingFirst[0] = false;
                }else{
                    ln.setVisibility(View.GONE);
                    showingFirst[0] = true;
                }

            }
        });

        final int[] location = new int[2];
        fm.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.getLocationOnScreen(location);
                p = new Point();
                p.x = location[0];
                p.y = location[1];
                popPost(v, p);
                return true;
            }
        });

    }

    private void location(String location) {



        Geocoder gc = new Geocoder(this, Locale.getDefault());
        List<Address> list = null;
        try {
            list = gc.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address = list.get(0);
        String locality = address.getLocality();
        String Country=address.getCountryName();
        String name=address.getAdminArea();

        String cityName = list.get(0).getAddressLine(0);
        String stateName = list.get(0).getAddressLine(1);
        String countryName = list.get(0).getAddressLine(2);

        Log.d(TAG, "geoLocate: cityName "+cityName+"\nstateName "+name+"\ncountryName "+Country);
        Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();
    }


    private void popPost(View v, Point p) {
        try {
            LinearLayout viewGroup = v.findViewById(R.id.layout_popup_post);
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = layoutInflater.inflate(R.layout.fragment_post_popup, viewGroup);

            // Creating the PopupWindow
            final PopupWindow popup = new PopupWindow(mContext);
            popup.setContentView(layout);
            popup.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            popup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popup.setFocusable(true);
            popup.setTouchable(true);
            popup.setAnimationStyle(R.style.popup_anim);


            popup.setBackgroundDrawable(new BitmapDrawable());
            popup.showAtLocation(layout, Gravity.CENTER, p.x, p.y );

            ImageButton dismis = layout.findViewById(R.id.layout_post_dismis);
            dismis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //////////////////////////firebase//////////////////////////////////////////

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigation:setting up bottom navigation view");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_home);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void checkCurrentUser(FirebaseUser user) {
        if (user == null) {
            Intent intent = new Intent(mContext, Activity_SignIn.class);
            startActivity(intent);
        }
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check user if he is logged in or not
                checkCurrentUser(user);
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!= null){
            mUserDatabase.child("online").setValue(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!= null){
            mUserDatabase.child("online").setValue(1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!= null){
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
