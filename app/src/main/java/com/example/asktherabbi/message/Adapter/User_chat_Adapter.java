package com.example.asktherabbi.message.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Activities.MessageActivity;
import com.example.asktherabbi.message.Model.Chat;
import com.example.asktherabbi.message.Model.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class User_chat_Adapter extends RecyclerView.Adapter<User_chat_Adapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String theLastMessage;

    public User_chat_Adapter(Context mContext, List<User> mUsers, boolean ischat){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_chat_item, parent, false);
        return new User_chat_Adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final User user = mUsers.get(position);

        holder.username.setText(user.getName());
        if (user.getImageUrl().equals("default")){
            holder.profile_image.setImageResource(R.drawable.ic_person_profile_24dp);
        } else {
            Glide.with(mContext).load(user.getImageUrl()).into(holder.profile_image);
        }

        if (ischat){
            lastMessage(user.getId(), holder.last_msg);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (ischat){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(mUsers.get(position).getId());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user.getStatus().equals("online")){
                        holder.img_on.setVisibility(View.VISIBLE);
                        holder.img_off.setVisibility(View.GONE);
                    } else {
                        holder.img_on.setVisibility(View.GONE);
                        holder.img_off.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                intent.putExtra("username", user.getName());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
        }
    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            //new part
                            if (chat.getType().equals("text")){

                                theLastMessage = chat.getMessage();
                            }
                            else if(chat.getType().equals("image")){
                                theLastMessage=mContext.getResources().getString(R.string.sent_photo_mess);
                            }
                            else if(chat.getType().equals("file")){
                                theLastMessage=mContext.getResources().getString(R.string.sent_file_mess);
                            }
                            else
                                theLastMessage=mContext.getResources().getString(R.string.sent_location_mess);
                            //----------
                        }
                    }
                }

                switch (theLastMessage){
                    case  "default":
                        last_msg.setText(mContext.getResources().getString(R.string.no_message));
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}




