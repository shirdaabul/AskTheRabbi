package com.example.asktherabbi.message.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Adapter.User_chat_Adapter;
import com.example.asktherabbi.message.Model.Chatlist;
import com.example.asktherabbi.message.Model.User;
import com.example.asktherabbi.message.Notifications.Token;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


public class Fragment_Chat extends Fragment {

    private RecyclerView recyclerView;
    private User_chat_Adapter userAdapter;
    private List<User> mUsers;
    TextView no_chats;
    ImageView hat_no_chats;

    FirebaseUser fuser;
    DatabaseReference reference;

    private List<Chatlist> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        no_chats=view.findViewById(R.id.no_chats);
        hat_no_chats=view.findViewById(R.id.hat_no_chat);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if(fuser!=null) {
            if (!fuser.isAnonymous()) {
                usersList = new ArrayList<>();

                reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            no_chats.setVisibility(View.INVISIBLE);
                            hat_no_chats.setVisibility(View.INVISIBLE);
                            usersList.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Chatlist chatlist = snapshot.getValue(Chatlist.class);
                                usersList.add(chatlist);
                            }
                            chatList();
                        }
                        else{
                            no_chats.setVisibility(View.VISIBLE);
                            hat_no_chats.setVisibility(View.VISIBLE);}
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                updateToken(FirebaseInstanceId.getInstance().getToken());
            } else {
                view = inflater.inflate(R.layout.fragment_chat_anonymous, container, false);
            }
        }


        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void chatList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : usersList){
                        if (user.getId().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }
                userAdapter = new User_chat_Adapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


}