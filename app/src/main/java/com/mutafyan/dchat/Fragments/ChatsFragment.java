package com.mutafyan.dchat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.mutafyan.dchat.Adapter.UserAdapter;
import com.mutafyan.dchat.Model.Chat;
import com.mutafyan.dchat.Model.Chatlist;
import com.mutafyan.dchat.Model.User;
import com.mutafyan.dchat.Notifications.Token;
import com.mutafyan.dchat.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private static final String TAG = "TAG";
    private RecyclerView chats_recycler;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    FirebaseUser fUser;
    DatabaseReference reference;
    private List<Chatlist> usersList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        chats_recycler = view.findViewById(R.id.recycler_chats);

        chats_recycler.setHasFixedSize(true);
        chats_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        // The "Chatlist" collection stores the UIDs of the users which started chatting
        // Chatlist -> activeUserID -  user 1 -> its ID
        //                         \_  user 2 -> its ID
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
//                    Toast.makeText(getContext(), chatlist.getId(), Toast.LENGTH_LONG).show();
                    usersList.add(chatlist);
                }

                listChats();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        updateToken(FirebaseMessaging.getInstance().getToken().toString()); // ??

        return view;
    }


    private void updateToken(String newToken) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(newToken);
        reference.child(fUser.getUid()).setValue(token);
    }


    private void listChats() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : usersList) {
                        assert user != null;
                        // TODO: CHECK THE STATEMENT! SOMETHING WRONG HERE
                        if(user.getId().equals(chatlist.getId()) && !user.getId().equals(fUser.getUid())) {
                            mUsers.add(user); // add to the list which will be showed in the ChatsFragment
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                chats_recycler.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

    }


}