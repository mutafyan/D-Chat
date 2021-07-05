package com.mutafyan.dchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Layout;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mutafyan.dchat.MainActivity;
import com.mutafyan.dchat.MessageActivity;
import com.mutafyan.dchat.Model.Chat;
import com.mutafyan.dchat.Model.User;
import com.mutafyan.dchat.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat){
        this.mUsers   = mUsers;
        this.mContext = mContext;
        this.ischat   = ischat;
    }


    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UserAdapter.ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if(user.getImageURL().equals("default")) {
            holder.profile_pic.setImageResource(R.drawable.ic_default_pfp);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_pic);
        }


        if(ischat) {
            setLastMessage(user.getId(), holder.last_message);
        } else {
            holder.last_message.setVisibility(View.GONE);
        }

        if(ischat){
            if(user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_off.setVisibility(View.VISIBLE);
                holder.img_on.setVisibility(View.GONE);

            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chat = new Intent(mContext, MessageActivity.class);
                chat.putExtra("userid", user.getId());
                mContext.startActivity(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_pic;
        private ImageView img_on, img_off;
        private TextView last_message;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_pic = itemView.findViewById(R.id.profile_pic);
            img_on = itemView.findViewById(R.id.img_online);
            img_off = itemView.findViewById(R.id.img_offline);
            last_message = itemView.findViewById(R.id.last_message);


        }
    }

    // Check for last message
    private void setLastMessage(String userid, TextView last_message) {
        theLastMessage = "default";
        FirebaseUser firebaseUser   = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    assert firebaseUser != null;
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        theLastMessage = chat.getMessage();
                        if(!chat.isIsseen() && firebaseUser.getUid().equals(chat.getReceiver())
                                && chat.getSender().equals(userid)) {
                            last_message.setTypeface(null, Typeface.BOLD);
                        } else {
                            last_message.setTypeface(null, Typeface.NORMAL);
                        }
                    }

                }

                switch (theLastMessage) {
                    case "default":
                        last_message.setText("No messages yet");
                        break;

                    default:
                        last_message.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}
