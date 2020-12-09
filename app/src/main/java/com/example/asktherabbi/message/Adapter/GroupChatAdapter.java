package com.example.asktherabbi.message.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Activities.MapsActivity;
import com.example.asktherabbi.message.Model.GroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {

    public static  final int MSG_TYPE_LEFT_MESSAGE= 0;
    public static  final int MSG_TYPE_LEFT_IMAGE= 1;
    public static  final int MSG_TYPE_RIGHT = 2;

    private Context mContext;
    private List<GroupChat> mChat;

    FirebaseUser fuser;

    public GroupChatAdapter(Context mContext, List<GroupChat> mChat){
        this.mChat = mChat;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.groupchat_item_right, parent, false);
        } else if(viewType == MSG_TYPE_LEFT_MESSAGE){
            view = LayoutInflater.from(mContext).inflate(R.layout.groupchat_item_left, parent, false);
        }
        else if(viewType == MSG_TYPE_LEFT_IMAGE){
            view = LayoutInflater.from(mContext).inflate(R.layout.groupchat_image_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final GroupChat chat = mChat.get(position);
        final String message_type = chat.getType();
        String messageImageUrl=chat.getMessage();

        holder.show_date.setText(chat.getDate());
        holder.show_time.setText(chat.getTime());
        holder.show_name.setText(chat.getName()+":");

        if (message_type.equals("text") ||message_type.equals("file") ) {

            holder.show_message.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.GONE);

            //if pdf url link too long-> show only first 30 characters
            if (message_type.equals("file"))
            {
                holder.fileIcon.setVisibility(View.VISIBLE);

                if (chat.getMessage().length()>30)
                    holder.show_message.setText(chat.getMessage().substring(0,30)+ "...");

            }else {
                holder.show_message.setText(chat.getMessage());
            }


        }
        else if(message_type.equals("image"))//image message
        {
            holder.show_image.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
            holder.show_message.setVisibility(View.GONE);

            Picasso.get().load(messageImageUrl).placeholder(R.drawable.ic_image).into(holder.show_image);

        }
        else if(message_type.equals("location")){
            holder.show_message.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.VISIBLE);
            holder.show_image.setImageResource(R.drawable.map_img);
            holder.show_image.setClickable(true);
            holder.show_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open google maps
                    Intent location=new Intent(mContext, MapsActivity.class);
                    location.putExtra("message",chat.getMessage());
                    mContext.startActivity(location);
                }
            });
        }

        //click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message_type.equals("file")){
                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(chat.getMessage()));
                    mContext.startActivity(intent);
                }
                else
                    if (message_type.equals("image")){
                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(chat.getMessage()));
                    mContext.startActivity(intent);
                }

            }
        });


        //long click
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (message_type.equals("file")){

                }

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public TextView show_date;
        public TextView show_time;
        public TextView show_name;
        public ImageView show_image, fileIcon;
        public ImageButton download_btn;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            show_date = itemView.findViewById(R.id.show_date);
            show_time = itemView.findViewById(R.id.show_time);
            show_name = itemView.findViewById(R.id.show_name);
            show_image = itemView.findViewById(R.id.show_image);
            download_btn = itemView.findViewById(R.id.download_btn);
            fileIcon = itemView.findViewById(R.id.messageIvFile);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getUserid().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else if(mChat.get(position).getType().equals("image")||mChat.get(position).getType().equals("location")){
            return MSG_TYPE_LEFT_IMAGE;
        }
        else
            return MSG_TYPE_LEFT_MESSAGE;
    }
}
