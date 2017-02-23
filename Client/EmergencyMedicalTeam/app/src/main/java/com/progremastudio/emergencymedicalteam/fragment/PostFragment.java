package com.progremastudio.emergencymedicalteam.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.SignInActivity;
import com.progremastudio.emergencymedicalteam.models.Post;
import com.progremastudio.emergencymedicalteam.viewholder.PostViewHolder;

public class PostFragment extends Fragment {

    private static final String TAG = "PostFragment";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post post, final int position) {

                Log.d(TAG, "populateViewHolder: post.uid = " + post.uid);
                Log.d(TAG, "populateViewHolder: post.displayName = " + post.displayName);
                Log.d(TAG, "populateViewHolder: post.email = " + post.email);
                Log.d(TAG, "populateViewHolder: post.timestamp = " + post.timestamp);
                Log.d(TAG, "populateViewHolder: post.locationCoordinate = " + post.locationCoordinate);
                Log.d(TAG, "populateViewHolder: post.message = " + post.message);
                Log.d(TAG, "populateViewHolder: post.pictureUrl = " + post.pictureUrl);
                Log.d(TAG, "populateViewHolder: post.emergencyType = " + post.emergencyType);

                viewHolder.bindToPost(post, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "To be implemented...",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);

    }

    public Query getQuery(DatabaseReference databaseReference) {
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        return databaseReference.child("posts").limitToFirst(100);
    }

}
