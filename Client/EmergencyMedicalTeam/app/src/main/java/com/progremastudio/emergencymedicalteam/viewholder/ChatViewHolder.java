/*
 * Copyright (c) 2017, Progrema Studio. All rights reserved.
 */

package com.progremastudio.emergencymedicalteam.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Chat;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "chat-view-holder";

    private LinearLayout mUserLayout;

    private TextView mUserTimestamp;

    private TextView mUserMessage;

    private LinearLayout mOtherLayout;

    private TextView mOtherDisplayName;

    private TextView mOtherTimestamp;

    private TextView mOtherMessage;

    private CircleImageView mImageView;

    public ChatViewHolder(View itemView) {
        super(itemView);

        /*
        Initiate view holder
         */
        mUserLayout = (LinearLayout) itemView.findViewById(R.id.user_layout);
        mUserMessage = (TextView) itemView.findViewById(R.id.user_message_field);
        mUserTimestamp = (TextView) itemView.findViewById(R.id.user_timestamp_field);
        mOtherLayout = (LinearLayout) itemView.findViewById(R.id.other_layout);
        mOtherDisplayName = (TextView) itemView.findViewById(R.id.other_display_name_field);
        mOtherMessage = (TextView) itemView.findViewById(R.id.other_message_field);
        mOtherTimestamp = (TextView) itemView.findViewById(R.id.other_timestamp_field);
        mImageView = (CircleImageView) itemView.findViewById(R.id.profile_picture);
    }

    @SuppressLint("SetTextI18n")
    public void bindToChat(Context context, Chat chat, View.OnClickListener clickListener) {

        /*
        Hide both layout
         */
        mUserLayout.setVisibility(View.GONE);
        mOtherLayout.setVisibility(View.GONE);

        Log.d(TAG, "chat.uid = " + chat.uid);
        Log.d(TAG, "chat.displayName = " + chat.displayName);
        Log.d(TAG, "current uid = " + AppSharedPreferences.getUserId(context));

        /*
        Check which layout is used
         */
        if (chat.uid.equals(AppSharedPreferences.getUserId(context))) {

            /*
            Use user layout
             */
            mUserLayout.setVisibility(View.VISIBLE);
            mUserTimestamp.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(chat.timestamp)));
            mUserMessage.setText(chat.message);

        } else {
            /*
            Use other layout
             */
            mOtherLayout.setVisibility(View.VISIBLE);
            mOtherDisplayName.setText(chat.displayName);
            mOtherTimestamp.setText(" - " + DateUtils.getRelativeTimeSpanString(Long.parseLong(chat.timestamp)));
            mOtherMessage.setText(chat.message);

            /*
            Show profile picture if exist
            */
            String profileUrl = chat.profileUrl;
            if (!profileUrl.equals(AppSharedPreferences.NO_URL)) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileUrl);
                Glide.with(context)
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .into(mImageView);
            }

        }
    }
}
