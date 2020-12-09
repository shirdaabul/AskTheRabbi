package com.example.asktherabbi.message.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.message.Activities.GroupChatActivity;
import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Model.Group;
import com.example.asktherabbi.message.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class AllGroupAdapter extends RecyclerView.Adapter<AllGroupAdapter.ViewHolder>{

    private Context mContext;
    private List<Group> mGroups;
    User myUser;

    public AllGroupAdapter(Context mContext, List<Group> mGroups) {
        this.mContext = mContext;
        this.mGroups = mGroups;
    }




    public class ViewHolder extends  RecyclerView.ViewHolder{

        public TextView groupname;
        public ImageButton add_fav_btn, delete_fav_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupname = itemView.findViewById(R.id.groupname);
            add_fav_btn = itemView.findViewById(R.id.add_fav_btn);
            delete_fav_btn = itemView.findViewById(R.id.delete_fav_btn);

        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Group group = mGroups.get(position);
        holder.groupname.setText(group.getName());

        final DatabaseReference FavRef, GroupFavRef, myUserRef;
        final String currentUserID;

        FirebaseUser fuser;
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        if (fuser != null){
            if (!fuser.isAnonymous()) {
                currentUserID = fuser.getUid();
                FavRef = FirebaseDatabase.getInstance().getReference().child("FavoritesList").child(currentUserID);
                GroupFavRef = FirebaseDatabase.getInstance().getReference().child("GroupFavoritesList").child(group.getName()).child(currentUserID);

                myUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
                myUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);

                            if (user.getId().equals(currentUserID))
                                myUser = user;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                holder.add_fav_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage(mContext.getString(R.string.sure_add) + holder.groupname.getText() + mContext.getString(R.string.group_to_fav))
                                .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        DatabaseReference FavUserRef;
                                        FavUserRef = FavRef.child(group.getName());

                                        //create GroupFav object (for fav list)
                                        HashMap<String, Object> MessageInfoMap = new HashMap<>();
                                        MessageInfoMap.put("name", group.getName());
                                        MessageInfoMap.put("affiliation", group.getAffiliation());
                                        FavUserRef.updateChildren(MessageInfoMap);

                                        //create GroupFavChat object (for group chat)
                                        HashMap<String, Object> GroupFavInfoMap = new HashMap<>();
                                        GroupFavInfoMap.put("id", myUser.getId());
                                        GroupFavInfoMap.put("imageURL", myUser.getImageUrl());
                                        GroupFavInfoMap.put("search", myUser.getSearch());
                                        GroupFavInfoMap.put("status", myUser.getStatus());
                                        GroupFavInfoMap.put("username", myUser.getName());
                                        GroupFavRef.updateChildren(GroupFavInfoMap);

                                        holder.add_fav_btn.setVisibility(View.INVISIBLE);
                                        holder.delete_fav_btn.setVisibility(View.VISIBLE);
                                    }
                                })
                                .setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(mContext, mContext.getString(R.string.group_not_added), Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
                    }
                });

                holder.delete_fav_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage(mContext.getString(R.string.delete) + holder.groupname.getText() + mContext.getString(R.string.from_fav_group))
                                .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FavRef.child(group.getName()).removeValue();
                                        GroupFavRef.removeValue();

                                        holder.add_fav_btn.setVisibility(View.VISIBLE);
                                        holder.delete_fav_btn.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .show();
                    }
                });


            } else {
                holder.add_fav_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.regist_to_add_to_fav), Toast.LENGTH_SHORT).show();
                    }
                });
            }
    }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentGroupName = mGroups.get(position).getName();

                Intent groupChatIntent = new Intent(mContext, GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                mContext.startActivity(groupChatIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }
}