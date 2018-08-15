package com.example.shubh.worldtraveller.Worldtraveller.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.gallery.Activity_Camera;
import com.example.shubh.worldtraveller.Worldtraveller.chats.Activity_Chats;
import com.example.shubh.worldtraveller.Worldtraveller.home.Activity_Home;
import com.example.shubh.worldtraveller.Worldtraveller.maps.Activity_Maps;
import com.example.shubh.worldtraveller.Worldtraveller.profile.Activity_Profile;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Created by Shubh on 16/03/2018.
 */

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";


    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(true);
    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_home:
                        Intent intent1 = new Intent(context, Activity_Home.class);//ACTIVITY_NUM = 0
                        context.startActivity(intent1);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        callingActivity.finish();
                        break;

                    case R.id.navigation_maps:
                        Intent intent2 = new Intent(context, Activity_Maps.class);//ACTIVITY_NUM = 1
                        context.startActivity(intent2);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.navigation_camera:
                        Intent intent3 = new Intent(context, Activity_Camera.class);//ACTIVITY_NUM = 2
                        context.startActivity(intent3);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.navigation_message:
                        Intent intent4 = new Intent(context, Activity_Chats.class);//ACTIVITY_NUM = 3
                        context.startActivity(intent4);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.navigation_profile:
                        Intent intent5 = new Intent(context, Activity_Profile.class);//ACTIVITY_NUM = 4
                        context.startActivity(intent5);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }
}
