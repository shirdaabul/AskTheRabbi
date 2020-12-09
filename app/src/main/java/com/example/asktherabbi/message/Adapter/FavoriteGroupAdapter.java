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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.message.Activities.GroupChatActivity;
import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Model.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FavoriteGroupAdapter extends RecyclerView.Adapter<FavoriteGroupAdapter.ViewHolder>{

    private Context mContext;
    private List<Group> mGroups;

    public FavoriteGroupAdapter(Context mContext, List<Group> mGroups) {
        this.mContext = mContext;
        this.mGroups = mGroups;
    }



    public class ViewHolder extends  RecyclerView.ViewHolder{

        public TextView groupname;
        public ImageButton delete_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupname = itemView.findViewById(R.id.groupname);
            delete_btn = itemView.findViewById(R.id.in_fav_btn);
        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_favorite_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Group group = mGroups.get(position);
        holder.groupname.setText(group.getName());

        final DatabaseReference FavRef, GroupFavRef;
        String currentUserID;
        FirebaseAuth mAuth;

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        FavRef = FirebaseDatabase.getInstance().getReference().child("FavoritesList").child(currentUserID);
        GroupFavRef = FirebaseDatabase.getInstance().getReference().child("GroupFavoritesList").child(group.getName()).child(currentUserID);

        holder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(mContext.getString(R.string.delete)+holder.groupname.getText()+mContext.getString(R.string.from_fav_group))
                        .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FavRef.child(group.getName()).removeValue();
                                GroupFavRef.removeValue();

                                //update Add/Remove star in group list
                                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View groupItem =  inflater.inflate(R.layout.group_item,null);
                                ImageButton add_fav_btn=groupItem.findViewById(R.id.add_fav_btn);
                                ImageButton delete_fav_btn=groupItem.findViewById(R.id.delete_fav_btn);
                                add_fav_btn.setVisibility(View.VISIBLE);
                                delete_fav_btn.setVisibility(View.INVISIBLE);

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