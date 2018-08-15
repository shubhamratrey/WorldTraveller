package com.example.shubh.worldtraveller.Worldtraveller.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.models.Album;
import com.example.shubh.worldtraveller.Worldtraveller.models.Comment;
import com.example.shubh.worldtraveller.Worldtraveller.models.Photo;
import com.example.shubh.worldtraveller.Worldtraveller.profile.Activity_Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class StorylineRecycleViewAdapter extends RecyclerView.Adapter<StorylineRecycleViewAdapter.StorylineRecycleViewHolder> {


    private static final String TAG = "RecyclerviewAdapter";
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Album> albumList;
    private int mAlbumPhotoCount = 0;


    private static final int ACTIVITY_NUM = 4;
    public StorylineRecycleViewAdapter(Context context, ArrayList<Album> albumList) {
        mContext = context;
        this.albumList = albumList;
        mInflater = LayoutInflater.from(context);

    }

    @Override
    public StorylineRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v = mInflater.inflate(R.layout.layout_profile_storyline, parent, false);
        return new StorylineRecycleViewHolder(v);

    }


    @Override
    public void onBindViewHolder(final StorylineRecycleViewHolder viewHolder, final int position) {

        final Album mAlbum = albumList.get(position);

        final String mRandomID = mAlbum.getAlbum_id().toString();

        Log.d(TAG, "setupGridView: Setting up image grid." + mRandomID);

        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("users")
                .child("posts")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mRandomID)
                .child("album_photos");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAlbumPhotoCount = 0;
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    try {

                        Log.d(TAG, "onDataChange: Inside setupgridview");

                        //photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                        photo.setTitle(objectMap.get(mContext.getString(R.string.field_title)).toString());
                        photo.setDescription(objectMap.get(mContext.getString(R.string.field_description)).toString());
                        photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                        photo.setPhoto_id(objectMap.get("photo_id").toString());
                        photo.setImage_path(objectMap.get("image_path").toString());

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(mContext.getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }

                        photo.setComments(comments);

                        /*List<Star> likesList = new ArrayList<Star>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Star like = new Star();
                            like.setUser_id(dSnapshot.getValue(Star.class).getUser_id());
                            likesList.add(like);
                        }
                        photo.setStars(likesList);*/
                        photos.add(photo);

                        mAlbumPhotoCount++;


                    } catch (NullPointerException e) {
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }

                Log.d(TAG, "NumberofPhotos:" + String.valueOf(mAlbumPhotoCount));

                if (mAlbumPhotoCount<3){
                    viewHolder.gridView.setNumColumns(mAlbumPhotoCount);
                }else {
                    viewHolder.gridView.setNumColumns(3);
                }


                /*int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / 3;
                viewHolder.gridView.setColumnWidth(imageWidth);
                viewHolder.gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);*/
                //viewHolder.gridView.setGravity(center);

                //Toast.makeText(mContext, "" + gridWidth, Toast.LENGTH_SHORT).show();
                ArrayList<String> imgUrls = new ArrayList<String>();

                for (int i = 0; i < photos.size(); i++) {
                    imgUrls.add(photos.get(i).getImage_path());
                }
                StroylineGridViewAdapter adapter = new StroylineGridViewAdapter(mContext, R.layout.layout_storyline_grid_imageview,
                        "", imgUrls);
                viewHolder.gridView.setAdapter(adapter);

                viewHolder.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                        
                        //mOnGridImageSelectedListener.onGridImageSelected(photos.get(p), ACTIVITY_NUM);
                        Intent intent = new Intent(mContext, Activity_Post.class);
                        intent.putExtra("PHOTO",photos.get(p));
                        intent.putExtra(mContext.getString(R.string.activity_number),ACTIVITY_NUM);
                        intent.putExtra("mRandomID",mRandomID);

                        mContext.startActivity(intent);

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }

        });

        viewHolder.mAlbumTitle.setText(mAlbum.getAlbum_title());
        viewHolder.mAlbumCategory.setText(mAlbum.getAlbum_category());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return albumList == null ? 0 : albumList.size();
    }


    public class StorylineRecycleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private GridView gridView;
        private TextView mAlbumTitle;
        private TextView mAlbumCategory;

        public StorylineRecycleViewHolder(View albumView) {
            super(albumView);
            gridView = (GridView) albumView.findViewById(R.id.profile_self_gridview);
            mAlbumCategory = (TextView) albumView.findViewById(R.id.profile_storyline_album_category);
            mAlbumTitle = (TextView) albumView.findViewById(R.id.profile_storyline_album_title);
        }


        @Override
        public void onClick(View v) {
            // int position =getLayoutPosition();

        }

    }


}
