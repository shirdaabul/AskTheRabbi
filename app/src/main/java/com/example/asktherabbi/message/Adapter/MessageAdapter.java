package com.example.asktherabbi.message.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Activities.MapsActivity;
import com.example.asktherabbi.message.Model.Chat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static  final int MSG_TYPE_LEFT = 0;
    public static  final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl){
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        final Chat chat = mChat.get(position);
        final String message_type = chat.getType();
        String messageImageUrl=chat.getMessage();
        //text message
        if (message_type.equals("text") ||message_type.equals("file") ) {

            holder.show_message.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.fileIcon.setVisibility(View.GONE);

            //if pdf url link too long-> show only first 30 characters
            if (message_type.equals("file"))
            {
                holder.fileIcon.setVisibility(View.VISIBLE);

                if (chat.getMessage().length()>30)
                    holder.show_message.setText(chat.getMessage().substring(0,30)+ "...");

            }else {
                holder.show_message.setText(chat.getMessage());
            }

            if (imageurl.equals("default")) {
                holder.profile_image.setImageResource(R.drawable.ic_person_profile_24dp);
            } else {
                Glide.with(mContext).load(imageurl).into(holder.profile_image);
            }

            if (position == mChat.size() - 1) {
                if (chat.isIsseen()) {
                    holder.txt_seen.setText(mContext.getResources().getString(R.string.seen));
                } else {
                    holder.txt_seen.setText(mContext.getResources().getString(R.string.delivered));
                }
            }

        }
        //location massage
        else if(message_type.equals("location")){
            holder.show_message.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageImage.setImageResource(R.drawable.map_img);
            holder.messageImage.setClickable(true);
            holder.messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open google maps
                    /* Intent navigateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + getString(R.string.latitude) + "," + getString(R.string.longitude)));
                //  Intent navigateIntent=new Intent()
                startActivity(navigateIntent);*/
                    Intent location=new Intent(mContext, MapsActivity.class);
                    location.putExtra("message",chat.getMessage());
                    mContext.startActivity(location);
                }
            });

            if (imageurl.equals("default")) {
                holder.profile_image.setImageResource(R.drawable.ic_person_profile_24dp);
            } else {
                Glide.with(mContext).load(imageurl).into(holder.profile_image);
            }

            if (position == mChat.size() - 1) {
                if (chat.isIsseen()) {
                    holder.txt_seen.setText(mContext.getResources().getString(R.string.seen));
                } else {
                    holder.txt_seen.setText(mContext.getResources().getString(R.string.delivered));
                }
            }
        }
        else //image message
        {

            holder.show_message.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);

            //  Glide.with(mContext).load(R.drawable.ic_image_message).into(holder.messageImage);
            //  Glide.with(mContext).load(messageImageUrl).into(holder.messageImage);
            Picasso.get().load(messageImageUrl).placeholder(R.drawable.ic_image).into(holder.messageImage);

            if (imageurl.equals("default")) {
                holder.profile_image.setImageResource(R.drawable.ic_person_profile_24dp);
            } else {
                Glide.with(mContext).load(imageurl).into(holder.profile_image);
            }

            if (position == mChat.size() - 1) {
                if (chat.isIsseen()) {
                    holder.txt_seen.setText(mContext.getResources().getString(R.string.seen));
                } else {
                    holder.txt_seen.setText(mContext.getResources().getString(R.string.delivered));
                }
            }
        }


        //click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message_type.equals("file")){
                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(chat.getMessage()));
                    mContext.startActivity(intent);
                }
                else  if (message_type.equals("image")){

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
        public ImageView profile_image;
        public TextView txt_seen;
        //
        public ImageView messageImage, fileIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            fileIcon=itemView.findViewById(R.id.messageIvFile);
            messageImage=itemView.findViewById(R.id.messageIv);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}