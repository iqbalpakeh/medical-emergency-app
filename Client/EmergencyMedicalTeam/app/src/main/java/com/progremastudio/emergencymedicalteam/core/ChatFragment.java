package com.progremastudio.emergencymedicalteam.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.FirebasePath;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Chat;
import com.progremastudio.emergencymedicalteam.models.User;
import com.progremastudio.emergencymedicalteam.viewholder.ChatViewHolder;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private static final String TAG = "chat-fragment";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Chat, ChatViewHolder> mAdapter;

    private RecyclerView mRecyclerView;

    private EditText mMessageField;

    private CircleImageView mImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        /*
        Initiate fragment layout
         */
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        /*
        Initiate Firebase object
         */
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*
        Initialize RecyclerView
         */
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mRecyclerView.setHasFixedSize(true);

        /*
        Initiate widget
         */
        mMessageField = (EditText) rootView.findViewById(R.id.other_message_field);
        mImageView = (CircleImageView) rootView.findViewById(R.id.profile_picture_field);

        /*
        Initiate button
         */
        ImageButton sendButton = (ImageButton) rootView.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitMessage();
            }
        });

        /*
        Show profile picture if exist
         */
        String pictureUrl = AppSharedPreferences.getCurrentUserPictureUrl(getContext());
        if (!pictureUrl.equals("No picture")) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pictureUrl);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(mImageView);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
        Set up Layout Manager and reverse layout
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);

        /*
        Set up FirebaseRecyclerAdapter with the Query
         */
        Query query = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class, R.layout.item_chat, ChatViewHolder.class, query) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, final Chat chat, final int position) {

                /*
                Log post details information
                 */
                Log.d(TAG, "populateViewHolder: chat.uid = " + chat.uid);
                Log.d(TAG, "populateViewHolder: chat.displayName = " + chat.displayName);
                Log.d(TAG, "populateViewHolder: chat.timestamp = " + chat.timestamp);
                Log.d(TAG, "populateViewHolder: chat.message = " + chat.message);

                /*
                Assign post information to widget defined in item_chat.xml
                 */
                viewHolder.bindToChat(getContext(), chat, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "To be implemented...",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private Query getQuery(DatabaseReference databaseReference) {
        /*
        Last 100 posts, these are automatically the 100 most recent
        due to sorting by push() keys
        */
        return databaseReference.child(FirebasePath.CHAT).limitToFirst(100);
    }

    /**
     * Submit chat message to FB part 1
     */
    private void submitMessage() {

        /*
        Get user message and check as it's required field
         */
        final String content = mMessageField.getText().toString();
        if (TextUtils.isEmpty(content)) {
            mMessageField.setError("Required");
            return;
        }

        /*
        Shows posting message to user and progress bar
         */
        Toast.makeText(getContext(), getString(R.string.str_Posting), Toast.LENGTH_SHORT).show();

        /*
        Listen for data change under users path and submit the post
         */
        final String userId = ((BaseActivity)getActivity()).getUid();
        mDatabase.child(FirebasePath.USERS).child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*
                        Get User object from datasnapshot
                         */
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            /*
                            Show error log if User is unexpectedly NULL
                             */
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(getContext(), "Error: could not fetch user.", Toast.LENGTH_SHORT).show();

                        } else {
                            /*
                            Upload Post to Firebase and sync with other user
                             */
                            Log.d(TAG, "User:" + user.toString());
                            uploadChat(userId, content);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }

                });
    }

    /**
     * Submit chat message to FB part 2
     */
    private void uploadChat(final String userId, final String message) {

        /*
        Generate random key for addressing every new post
         */
        final String key = mDatabase.child(FirebasePath.CHAT).push().getKey();

        /*
        Prepare local data for chat object creation
         */
        String displayName = AppSharedPreferences.getCurrentUserDisplayName(getContext());
        String timestamp = ((BaseActivity)getActivity()).currentTimestamp();

        /*
        Create new chat object
         */
        Chat chat = new Chat(
                userId,
                displayName,
                timestamp,
                message
        );

        /*
        Prepare hash-map value from Chat object
         */
        Map<String, Object> chatValues = chat.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        /*
        Prepare data for "/chat/key*"
         */
        childUpdates.put("/" + FirebasePath.CHAT + "/" + key, chatValues);

        /*
        Update FB object
         */
        mDatabase.updateChildren(childUpdates);

        /*
        Clear message box
         */
        mMessageField.setText("");
    }
}
