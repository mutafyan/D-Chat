package com.mutafyan.dchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mutafyan.dchat.Adapter.MessageAdapter;
import com.mutafyan.dchat.Fragments.APIService;
import com.mutafyan.dchat.Model.Chat;
import com.mutafyan.dchat.Model.User;
import com.mutafyan.dchat.Notifications.Client;
import com.mutafyan.dchat.Notifications.Data;
import com.mutafyan.dchat.Notifications.MyResponse;
import com.mutafyan.dchat.Notifications.Sender;
import com.mutafyan.dchat.Notifications.Token;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "TAG";
    private static final int MAX_MESSAGE_SIZE = 150;
    CircleImageView profile_pic;
    TextView username;

    FirebaseUser fUser;
    DatabaseReference database;
    ImageButton send_button;
    MaterialEditText send_message;
    Intent intent;
    String userid;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recycler_messages;

    ValueEventListener seenListener;

    APIService apiService;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Toolbar config
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        // Recycler of messages
        recycler_messages = findViewById(R.id.recycler_messages);
        recycler_messages.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recycler_messages.setLayoutManager(linearLayoutManager);
        // assigning ids
        profile_pic = findViewById(R.id.profile_pic);
        username = findViewById(R.id.username);
        send_button = findViewById(R.id.send_button);
        send_message = findViewById(R.id.text_message);

        // Intent passed by UserAdapter -- Receiver ID
        intent = getIntent();
        userid = intent.getStringExtra("userid");

        fUser = FirebaseAuth.getInstance().getCurrentUser();


        // Check the entered message, and if ok -> send
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = send_message.getText().toString();
                if(message.trim().equals("")) {
                    send_message.setError("Message cannot be empty!");
                } else if(message.length() > MAX_MESSAGE_SIZE) {
                    send_message.setError("The message may be maximum 150 characters long");
                } else { // all ok
                    sendMessage(fUser.getUid(), userid, message);
                    send_message.setText("");
                }
            }
        });


        database = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profile_pic.setImageResource(R.drawable.ic_default_pfp);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_pic);
                }

                readMessages(fUser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        seenMessage(userid);
    }

    // Change the status of messages as "seen"
    private void seenMessage(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        chat.setIsseen(true);
                        hashMap.put("isseen", true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }


    // Send a message
    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        database.child("Chats").push().setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fUser.getUid())
                .child(userid);
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if( !snapshot.exists() ) {
                    chatsRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        // Sending notification
        final String msg = message;
        database = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(notify) {
                    assert user != null;
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    // Send a notification to receiver user when a new message is received
    private void sendNotification(String receiver, String username, String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(), R.mipmap.ic_launcher, message, username,
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        assert response.body() != null;
                                        if (response.body().success != 1) {
                                            Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.d(TAG, t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

    }

    private void readMessages(String myid, String userid, String imageURL) {
        mchat = new ArrayList<>();

        database = FirebaseDatabase.getInstance().getReference("Chats");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mchat.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        mchat.add(chat);
                    }
                }

                messageAdapter = new MessageAdapter(getApplicationContext(), mchat, imageURL);
                recycler_messages.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }


    private void status(String status) {
        database = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        database.updateChildren(hashMap);
    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.removeEventListener(seenListener);
        status("offline");
    }
}