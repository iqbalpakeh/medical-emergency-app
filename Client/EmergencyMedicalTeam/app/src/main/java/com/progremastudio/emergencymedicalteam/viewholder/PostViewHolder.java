package com.progremastudio.emergencymedicalteam.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView mNameField;
    public TextView mMessageField;

    PostViewHolder(View itemView) {
        super(itemView);
        mNameField = (TextView) itemView.findViewById(R.id.display_name_field);
        mMessageField = (TextView) itemView.findViewById(R.id.message_field);
    }

    public void bindToPost(Post post, View.OnClickListener clickListener) {

        mNameField.setText(post.displayName);
        mNameField.setOnClickListener(clickListener);

        mMessageField.setText(post.message);
        mMessageField.setOnClickListener(clickListener);

    }

}
