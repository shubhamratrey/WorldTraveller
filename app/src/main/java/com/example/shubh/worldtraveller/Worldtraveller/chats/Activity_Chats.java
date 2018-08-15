package com.example.shubh.worldtraveller.Worldtraveller.chats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shubh.worldtraveller.Worldtraveller.Utils.BottomNavigationViewHelper;
import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.GetTimeMessaged;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.RecyclerTouchListener;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.models.Conv;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.example.shubh.worldtraveller.Worldtraveller.view.ViewProfileFragment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Chats extends AppCompatActivity {

    private static final String TAG = "Activity_Chats";
    private static final int ACTIVITY_NUM = 3;


    private TextView mChatHeader;
    private EditText mSearchField;
    private ImageButton mSearchBtn;
    private RecyclerView mResultList;
    private List<UserAccountSettings> mUserList;
    private String mUserID, mUserName;
    private String searchText;
    private Point p;


    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabase;
    //chatitems var
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mUsersDatabase1;
    private DatabaseReference mOnlineDatabase;

    private RecyclerView mConvList;
    private String mCurrent_user_id;

    private Context mContext = Activity_Chats.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        Log.d(TAG, "onCreate: starting");
        setupBottomNavigationView();
        hideSoftKeyboard();
        setupFirebaseAuth();
        initImageLoader();

        mOnlineDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile))
                .child(mAuth.getCurrentUser().getUid());


        //Conservation List
        mConvList = (RecyclerView) findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_chat))
                .child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile));
        mUsersDatabase.keepSynced(true);

        mUsersDatabase1 = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile));
        mUsersDatabase1.keepSynced(true);

        mMessageDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_messages))
                .child(mCurrent_user_id);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        //search list
        mUserDatabase = FirebaseDatabase.getInstance().getReference();
        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);
        mChatHeader = (TextView) findViewById(R.id.chatheader);
        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton();
        initTextListener();
    }


    private void searchButton() {
        final Animation slide_left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        final Animation slide_right = AnimationUtils.loadAnimation(this, R.anim.slide_right);
        final Animation blink = AnimationUtils.loadAnimation(this, R.anim.blink);
        final boolean[] showingFirst = {true};
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (showingFirst[0] == true) {
                    if (mSearchField.getVisibility() == View.GONE) {
                        mSearchField.setVisibility(View.VISIBLE);
                        mSearchField.startAnimation(slide_left);
                    }
                    mChatHeader.setVisibility(View.INVISIBLE);

                    showingFirst[0] = false;
                } else {
                    if (mSearchField.getVisibility() == View.VISIBLE) {
                        mSearchField.setVisibility(View.GONE);
                        mSearchField.startAnimation(slide_right);
                    }
                    if (mChatHeader.getVisibility() == View.INVISIBLE) {
                        mChatHeader.setVisibility(View.VISIBLE);
                        mChatHeader.startAnimation(blink);
                    }

                    showingFirst[0] = true;
                }
            }
        });
    }

    private void initTextListener() {
        Log.d(TAG, "initTextListener: initializing");
        mUserList = new ArrayList<>();
        /*mResultList.setAdapter(null);*/
        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                searchText = mSearchField.getText().toString();
                if (searchText.equals("")) {
                    mResultList.setAdapter(null);
                    mConvList.setVisibility(View.VISIBLE);
                    Log.d(TAG, "afterTextChanged: when nothing is typed");
                } else {
                    firebaseUserSearch(searchText);
                    mConvList.setVisibility(View.INVISIBLE);
                }
                /*String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);*/
            }
        });

    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /*--------------------FirebaseSearchList-------------------------*/
    private void firebaseUserSearch(String searchText) {

        // Toast.makeText(Activity_Chats.this, "Started Search", Toast.LENGTH_LONG).show();
        mUserList.clear();

        Query firebaseSearchQuery = mUserDatabase.
                child(getString(R.string.dbname_users)).
                child(getString(R.string.dbname_profile)).
                orderByChild("username").startAt(searchText).endAt(searchText + "\uf8ff");

        firebaseSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(UserAccountSettings.class).toString());

                    mUserList.add(singleSnapshot.getValue(UserAccountSettings.class));
                    //update the users list view
                    Log.d(TAG, "onDataChange: username and userid" + mUserName + mUserID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        FirebaseRecyclerAdapter<UserAccountSettings, UsersViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<UserAccountSettings, UsersViewHolder>(
                        UserAccountSettings.class,
                        R.layout.chats_search_list_layout,
                        UsersViewHolder.class,
                        firebaseSearchQuery) {
                    @Override
                    protected void populateViewHolder(final UsersViewHolder viewHolder, UserAccountSettings model, int position) {
                        viewHolder.setDetails(getApplicationContext(), model.getUsername(), model.getDescription(), model.getProfile_photo(), model.getDisplay_name());

                        final String list_user_id = getRef(position).getKey();

                        mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String userName = dataSnapshot.child(getString(R.string.dbname_username)).getValue().toString();
                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Toast.makeText(Activity_Chats.this, "" + list_user_id + "   " + userName, Toast.LENGTH_SHORT).show();
                                        Intent chatIntent = new Intent(Activity_Chats.this, Activity_Conversation.class);
                                        chatIntent.putExtra("user_id", list_user_id);
                                        chatIntent.putExtra("user_name", userName);
                                        startActivity(chatIntent);
                                    }
                                });
                                try {
                                    viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            Log.d(TAG, "onLongClick: yeah" + list_user_id);
                                            Intent intent = new Intent(mContext, ViewProfileFragment.class);
                                            intent.putExtra(getString(R.string.field_user_id), list_user_id);
                                            intent.putExtra(mContext.getString(R.string.activity_number), ACTIVITY_NUM);
                                            startActivity(intent);
                                            return true;
                                        }
                                    });

                                } catch (NullPointerException e) {
                                    Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                };


        mResultList.setAdapter(firebaseRecyclerAdapter);

      /*  mResultList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                mResultList, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                //Toast.makeText(mContext, "Here is postion" + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

                Intent intent =  new Intent(mContext, ViewProfileFragment.class);
                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
                intent.putExtra(mContext.getString(R.string.activity_number), ACTIVITY_NUM);
                startActivity(intent);
                //finish();

                Log.d(TAG, "onLongClick: no");
                //Toast.makeText(mContext, "here is the " + mUserList.get(position), Toast.LENGTH_SHORT).show();
            }
        }));*/
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setDetails(Context ctx, String userName, String userStatus, String userImage, String name) {

            TextView name1 = (TextView) mView.findViewById(R.id.name_text);
            TextView user_name = (TextView) mView.findViewById(R.id.username_text);
            TextView user_status = (TextView) mView.findViewById(R.id.status_text);
            CircleImageView user_image = (CircleImageView) mView.findViewById(R.id.profile_image);

            name1.setText(name);
            user_name.setText("● " + userName);
            user_status.setText(userStatus);

            if (user_image != null) {
                UniversalImageLoader.setImage(userImage, user_image, null, "");
            } else {
                Log.d(TAG, "setDetails: user Image not loaded");
            }
        }

    }


    /*--------------------FirebaseChatList-------------------------*/
    private void chatList() {
        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new
                FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                        Conv.class,
                        R.layout.layout_chat_user_list,
                        ConvViewHolder.class,
                        conversationQuery
                ) {
                    @Override
                    protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int i) {


                        final String list_user_id = getRef(i).getKey();

                        Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                        lastMessageQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                String data = dataSnapshot.child("message").getValue().toString();
                                String time = dataSnapshot.child("time").getValue().toString();
                                String type = dataSnapshot.child("type").getValue().toString();
                                convViewHolder.setMessage(data, time, type, conv.isSeen());


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

                        mUsersDatabase1.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final String userName = dataSnapshot.child("username").getValue().toString();
                                String userThumb = dataSnapshot.child("profile_photo").getValue().toString();

                                if (dataSnapshot.hasChild("online")) {

                                    String userOnline = dataSnapshot.child("online").getValue().toString();
                                    convViewHolder.setUserOnline(userOnline);

                                }

                                convViewHolder.setName(userName);
                                convViewHolder.setUserImage(userThumb, getApplication());
                                convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        
                                        
                                        Intent chatIntent = new Intent(getApplicationContext(), Activity_Conversation.class);
                                        chatIntent.putExtra("user_id", list_user_id);
                                        chatIntent.putExtra("user_name", userName);
                                        startActivity(chatIntent);

                                    }
                                });

                                final int[] location = new int[2];
                                convViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        v.getLocationOnScreen(location);
                                        p = new Point();
                                        p.x = location[0];
                                        p.y = location[1];
                                        popStarWindow(v, p);
                                        return true;
                                    }
                                });


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                };

        mConvList.setAdapter(firebaseConvAdapter);

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;


        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, String time, String type, boolean isSeen) {

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            TextView userName = (TextView) mView.findViewById(R.id.user_single_name);
            ImageView vectorPhoto = (ImageView) mView.findViewById(R.id.user_single_photo);

            CircleImageView notifyseen = (CircleImageView) mView.findViewById(R.id.msgnotify);
            TextView mTime = (TextView) mView.findViewById(R.id.chattime);

            if (message != null) {
                if (type.equals("text")) {
                    vectorPhoto.setVisibility(View.GONE);
                    userStatusView.setText(message);
                } else {
                    vectorPhoto.setVisibility(View.VISIBLE);
                    userStatusView.setText("Photo");

                }
            }


            GetTimeMessaged getTimeAgo = new GetTimeMessaged();
            long lastTime = Long.parseLong(time);
            String lastSeenTime = getTimeAgo.getTimeMessaged(lastTime, mView.getContext());
            mTime.setText(lastSeenTime);

            if (!isSeen) {
                notifyseen.setVisibility(View.VISIBLE);
                userName.setTypeface(Typeface.DEFAULT_BOLD);
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                userStatusView.setTextColor(Color.parseColor("#212121"));

            } else {
                notifyseen.setVisibility(View.INVISIBLE);
                userName.setTypeface(Typeface.DEFAULT);
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);

            }

        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            if (thumb_image != null) {
                UniversalImageLoader.setImage(thumb_image, userImageView, null, "");
            } else {
                Log.d(TAG, "setDetails: user Image not loaded");
            }
        }

        public void setUserOnline(String online_status) {
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if (online_status != "1") {
                userOnlineView.setVisibility(View.INVISIBLE);

            } else {
                userOnlineView.setVisibility(View.VISIBLE);
            }
        }

    }



    private void popStarWindow(View v, Point p) {
        try {
            LinearLayout viewGroup = v.findViewById(R.id.layout_popup_profile);
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = layoutInflater.inflate(R.layout.layout_popup_profile, viewGroup);
            final Animation blink = AnimationUtils.loadAnimation(this, R.anim.dailog_blink_in);


            layout.startAnimation(blink);

            // Creating the PopupWindow
            final PopupWindow popup = new PopupWindow(mContext);
            popup.setAnimationStyle(R.style.popup_anim);
            popup.setContentView(layout);
            popup.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            popup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popup.setFocusable(true);
            popup.setTouchable(true);
            popup.setAnimationStyle(R.style.popup_anim);

            // Clear the default translucent background
            popup.setBackgroundDrawable(new BitmapDrawable());

            // Displaying the popup at the specified location, + offsets.

            popup.showAtLocation(layout, Gravity.CENTER,p.x,p.y);
            /*
            // Displaying the popup at the specified location, + offsets.
            popup.showAtLocation(v, Gravity.CENTER, 0, 0);*/

            /*RatingBar rb = layout.findViewById(R.id.ratingBar1);
            rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating,
                                            boolean fromUser) {
                    int in = (int) rating;
                    newRating(String.valueOf(in));
                    //Toast.makeText(mContext, "You gave " + String.valueOf(in) + " star.", Toast.LENGTH_SHORT).show();
                    popup.setAnimationStyle(R.anim.default_dialog_out);
                    popup.dismiss();

                }

            });*/
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*--------------------FirebaseAuth-------------------------*/
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigation:setting up bottom navigation view");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_home);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabase = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // user is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        chatList();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mOnlineDatabase.child("online").setValue(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mOnlineDatabase.child("online").setValue(1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mOnlineDatabase.child("online").setValue(ServerValue.TIMESTAMP);
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
