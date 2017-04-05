package com.progremastudio.emergencymedicalteam.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Chat;

public class ChatViewHolder extends RecyclerView.ViewHolder{

    private static final String TAG = "chat-view-holder";

    private TextView mDisplayName;

    private TextView mTimestamp;

    private TextView mMessage;

    public ChatViewHolder(View itemView) {

        super(itemView);

        mDisplayName = (TextView) itemView.findViewById(R.id.display_name_field);
        mMessage = (TextView) itemView.findViewById(R.id.message_field);
        mTimestamp = (TextView) itemView.findViewById(R.id.timestamp_field);
    }

    @SuppressLint("SetTextI18n")
    public void bindToChat(Context context, Chat chat, View.OnClickListener clickListener) {

        /*
        Show user display name
         */
        mDisplayName.setText(chat.displayName);

        /*
        Show timestamp
         */
        mTimestamp.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(chat.timestamp)));

        /*
        Show user message
         */
        mMessage.setText(chat.message);
    }
}
