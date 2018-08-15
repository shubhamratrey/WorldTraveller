package com.example.shubh.worldtraveller.Worldtraveller.chats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.GetTimeAgo;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.ImageManager;
import com.example.shubh.worldtraveller.Worldtraveller.adapters.MessageAdapter;

import com.example.shubh.worldtraveller.Worldtraveller.models.Messages;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity_Conversation extends AppCompatActivity {

    private static final String TAG = "Activity_Conversation";
    private String mChatUser;
    private Toolbar mChatToolbar;

    private TextView mTitleView, mLastSeenView;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private RelativeLayout mUploadRelativeLayout;

    private Bitmap bm;
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    private ImageView mBackbButton;
    private ProgressBar mProgressBar;
    private DonutProgress mDonutProgress;
    private TextView mProgressText;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 20;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;
    private DatabaseReference mRootRef;
    private DatabaseReference mOnlineDatabase;
    private DatabaseReference mNotificationDatabase;


    //New Solution
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        mDonutProgress = (DonutProgress) findViewById(R.id.donutProgress);
        mUploadRelativeLayout = (RelativeLayout) findViewById(R.id.uploadRelativeLayout);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_users));
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_users));
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.snippet_top_chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        // ---- Custom Action bar Items ----
        mChatToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.google_red), PorterDuff.Mode.SRC_ATOP);
        mTitleView = (TextView) findViewById(R.id.conv_username);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mTitleView.setText(userName);

        mProfileImage = (ImageView) findViewById(R.id.ivEllipses);
        mChatAddBtn = (ImageButton) findViewById(R.id.conv_camera_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);


        //mProgressText= findViewById(R.id.progressText2)

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);


        mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setReverseLayout(false);
        mLinearLayout.setStackFromEnd(true);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);
        mAdapter.SetOnItemClickListener(new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String id) {

                ImageFragment fragment = new ImageFragment();
                Bundle args = new Bundle();
                args.putString(getString(R.string.photo), id);
                fragment.setArguments(args);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                transaction.replace(R.id.imageFragment, fragment);
                transaction.addToBackStack(getString(R.string.view_post_fragment));
                transaction.commit();

                // Toast.makeText(Activity_Conversation.this, "Hello"+id, Toast.LENGTH_SHORT).show();
            }
        });


        mOnlineDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile))
                .child(mAuth.getCurrentUser().getUid());


        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();


        loadMessages();

        mRootRef.child(getString(R.string.dbname_profile)).child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                // String image = dataSnapshot.child("image").getValue().toString();

                ImageView userOnlineView = (ImageView) findViewById(R.id.online_icon);


                if (online == "1") {

                    mLastSeenView.setText("Online");
                    userOnlineView.setVisibility(View.VISIBLE);
                } else {
                    userOnlineView.setVisibility(View.GONE);
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online); //String online = dataSnapshot.child("online").getValue().toString();
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child(getString(R.string.dbname_chat)).child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();


            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Toast.makeText(this, "Uploading image please wait", Toast.LENGTH_SHORT).show();

            mUploadRelativeLayout.setVisibility(View.VISIBLE);

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference filepath = mImageStorage
                    .child("message_images").child(user_id).child(push_id + ".jpg");

            UploadTask uploadTask = filepath.putFile(imageUri);
           /* //convert image url to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imageUri);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 20);

            UploadTask uploadTask = null;
            uploadTask = filepath.putBytes(bytes);
*/
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if (databaseError != null) {

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mUploadRelativeLayout.setVisibility(View.GONE);
                    Toast.makeText(Activity_Conversation.this, "yupiie uploaded", Toast.LENGTH_SHORT).show();


                }
            });
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    int myInt = (int) progress;


                    mDonutProgress.setDonut_progress(Integer.toString(myInt));

                    Log.d(TAG, "onProgress: " + myInt);
                }
            });

        }

    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if (itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

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

    }

    private void loadMessages() {


        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                ///////////////////////////

                mRefreshLayout.setRefreshing(false);

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
        mRootRef.child(getString(R.string.dbname_chat)).child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
    }


    public String getNotificationRef(){
        DatabaseReference user_message_push = mRootRef.child("messages")
                .child(mCurrentUserId).child(mChatUser).push();
        String push_id = user_message_push.getKey();
        return push_id;
    }

    private void sendMessage() {



        String message = mChatMessageView.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;
            String notification_ref ="notifications/"+mChatUser;

            /////////for message
            /*DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();
            String push_id = user_message_push.getKey();*/

            ///////for notification
            DatabaseReference newNotificationref = mRootRef.child("notifications")
                    .child(mChatUser).push();
            String newNotificationId = newNotificationref.getKey();

            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from", mCurrentUserId);
            notificationData.put("type", "request");


            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + getNotificationRef(), messageMap);
            messageUserMap.put(chat_user_ref + "/" + getNotificationRef(), messageMap);

            messageUserMap.put(notification_ref + "/" + newNotificationId, notificationData);

            mChatMessageView.setText("");


            mRootRef.child("chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);


            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

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

    }
}
