
package com.example.shubh.worldtraveller.Worldtraveller.home;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.adapters.NotificationAdapter;
import com.example.shubh.worldtraveller.Worldtraveller.models.Messages;
import com.example.shubh.worldtraveller.Worldtraveller.models.Notification;
import com.example.shubh.worldtraveller.Worldtraveller.view.ViewProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class Activity_Notification extends AppCompatActivity {

    private static final String TAG = "Activity_Notification";
    private RecyclerView mNotificationRecycleView;
    private NotificationAdapter notificationAdapter;
    private final List<Notification> notificationList=  new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private Context mContext = Activity_Notification.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__notification);
        initImageLoader();
        loadNotifications();
        Log.d(TAG, "onCreate: " + notificationList.size());
        notificationAdapter = new NotificationAdapter(mContext,notificationList);
        mNotificationRecycleView = (RecyclerView) findViewById(R.id.notification_recycleview);

        mLinearLayout = new LinearLayoutManager(this);
        mNotificationRecycleView.setHasFixedSize(true);
        mLinearLayout.setReverseLayout(true);
        mLinearLayout.setStackFromEnd(false);
        mNotificationRecycleView.setLayoutManager(mLinearLayout);

        mNotificationRecycleView.setAdapter(notificationAdapter);


        /*notificationAdapter.SetOnItemOnNotificationProfilePhoto(new NotificationAdapter.OnProfilePhotoClickListener() {
            @Override
            public void onProfilePhotoClick(View view, int position, String id) {

                Toast.makeText(mContext, "yupie"+id, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, ViewProfileFragment.class);
                intent.putExtra(getString(R.string.field_user_id), id);
                intent.putExtra(mContext.getString(R.string.activity_number), 0);
                startActivity(intent);
            }
        });

        notificationAdapter.SetOnItemOnView(new NotificationAdapter.OnViewClickListener() {
            @Override
            public void onViewClick(View view, int position, String id) {

                Toast.makeText(mContext, "yupie view", Toast.LENGTH_SHORT).show();
               *//* Intent intent = new Intent(mContext, ViewProfileFragment.class);
                intent.putExtra(getString(R.string.field_user_id), id);
                intent.putExtra(mContext.getString(R.string.activity_number), 0);
                startActivity(intent);*//*
            }
        });
*/
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void loadNotifications() {

        //  Log.d(TAG, "loadNotifications: ");
        DatabaseReference messageRef = (DatabaseReference) FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child("notifications")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Query messageQuery = messageRef.orderByChild("time").limitToLast(15);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Notification notification = dataSnapshot.getValue(Notification.class);
                if (notification.getType().equals("request")){
                    Toast.makeText(mContext, "Request", Toast.LENGTH_SHORT).show();
                }else {
                    notificationList.add(notification);
                }

                notificationAdapter.notifyDataSetChanged();

                //Log.d(TAG, "onChildAdded: "+notification.getFrom());
                /*itemPos++;


                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }


          mMessagesList.scrollToPosition(messagesList.size() - 1);

                ///////////////////////////

                mRefreshLayout.setRefreshing(false);*/

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Notification notification = singleSnapshot.getValue(Notification.class);
                    notificationList.add(notification);
                    // notificationAdapter.notifyDataSetChanged();


                }
                Log.d(TAG, "onDataChange: here" + notificationList.size());
                notificationAdapter = new NotificationAdapter(mContext, notificationList);
                mNotificationRecycleView.setAdapter(notificationAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
*/

    }
}
