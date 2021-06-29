package com.mutafyan.dchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mutafyan.dchat.Model.Chat;
import com.mutafyan.dchat.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT= 1;
    private final Context mContext;
    private List<Chat> mChats;
    private String imageURL;

    FirebaseUser fUser;

    public MessageAdapter(Context mContext, List<Chat> mChats, String imageURL){
        this.mChats = mChats;
        this.mContext = mContext;
        this.imageURL = imageURL;
    }


    @NonNull
    @NotNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChats.get(position);
        holder.show_message.setText(chat.getMessage());
        if(imageURL.equals("default")) {
            holder.profile_pic.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageURL).into(holder.profile_pic);
        }

        if(position == mChats.size()-1) {
            if(chat.isIsseen()){
                holder.status.setText("Seen");
            } else {
                holder.status.setText("Delivered");
            }
        } else {
            holder.status.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_pic;
        public TextView status;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_pic = itemView.findViewById(R.id.profile_pic);
            status = itemView.findViewById(R.id.message_status);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        // Determine if the message is sent by current user or not
        if(mChats.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
