package com.progremastudio.emergencymedicalteam.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Chat;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "chat-view-holder";

    private LinearLayout mUserLayout;

    private TextView mUserDisplayName;

    private TextView mUserTimestamp;

    private TextView mUserMessage;

    private LinearLayout mOtherLayout;

    private TextView mOtherDisplayName;

    private TextView mOtherTimestamp;

    private TextView mOtherMessage;

    public ChatViewHolder(View itemView) {

        super(itemView);

        mUserLayout = (LinearLayout) itemView.findViewById(R.id.user_layout);
        mUserDisplayName = (TextView) itemView.findViewById(R.id.user_display_name_field);
        mUserMessage = (TextView) itemView.findViewById(R.id.user_message_field);
        mUserTimestamp = (TextView) itemView.findViewById(R.id.user_timestamp_field);
        mOtherLayout = (LinearLayout) itemView.findViewById(R.id.other_layout);
        mOtherDisplayName = (TextView) itemView.findViewById(R.id.other_display_name_field);
        mOtherMessage = (TextView) itemView.findViewById(R.id.other_message_field);
        mOtherTimestamp = (TextView) itemView.findViewById(R.id.other_timestamp_field);
    }

    @SuppressLint("SetTextI18n")
    public void bindToChat(Context context, Chat chat, View.OnClickListener clickListener) {

        /*
        Hide both layout
         */
        mUserLayout.setVisibility(View.GONE);
        mOtherLayout.setVisibility(View.GONE);

        /*
        Check which layout is used
         */
        Log.d(TAG, "chat.uid = " + chat.uid);
        Log.d(TAG, "chat.displayName = " + chat.displayName);
        Log.d(TAG, "current uid = " + AppSharedPreferences.getCurrentUserId(context));

        if (chat.uid.equals(AppSharedPreferences.getCurrentUserId(context))) {
            /*
            Use user layout
             */
            mUserLayout.setVisibility(View.VISIBLE);
            mUserDisplayName.setText(chat.displayName);
            mUserTimestamp.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(chat.timestamp)));
            mUserMessage.setText(chat.message);

        } else {
            /*
            Use other layout
             */
            mOtherLayout.setVisibility(View.VISIBLE);
            mOtherDisplayName.setText(chat.displayName);
            mOtherTimestamp.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(chat.timestamp)));
            mOtherMessage.setText(chat.message);
        }
    }
}
