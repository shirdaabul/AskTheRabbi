package com.example.asktherabbi.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.R;
import com.example.asktherabbi.TorahLesson;
import com.example.asktherabbi.message.Activities.MessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class TorahLessonFavAdapter extends RecyclerView.Adapter<TorahLessonFavAdapter.TorahFavLessonViewHolder> {
    private Context mContext;

    private List<TorahLesson> torahLessons;

    public TorahLessonFavAdapter(Context mContext, List<TorahLesson> torahLessons) {
        this.mContext = mContext;
        this.torahLessons = torahLessons;
    }


    public class TorahFavLessonViewHolder extends RecyclerView.ViewHolder {

        TextView rabbi_tv,subject_tv,location_tv,max_tv,current_tv,date_tv,time_tv;
        Button register_bt,unsubscribe_bt;
        ImageButton lesson_chat_bt;


        public TorahFavLessonViewHolder(View itemView) {
            super(itemView);

            rabbi_tv = itemView.findViewById(R.id.rabbi);
            subject_tv = itemView.findViewById(R.id.subject);
            location_tv = itemView.findViewById(R.id.location);
            max_tv = itemView.findViewById(R.id.max_praticipants);
            current_tv = itemView.findViewById(R.id.current_praticipants);
            date_tv = itemView.findViewById(R.id.date);
            time_tv = itemView.findViewById(R.id.time);
            register_bt = itemView.findViewById(R.id.lesson_register_bt);
            unsubscribe_bt = itemView.findViewById(R.id.lesson_Unsubscribe_bt);
            lesson_chat_bt = itemView.findViewById(R.id.lesson_chat_bt);


//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    listener.onMissionClicked(getAdapterPosition(),view);
//                }
//            });
//
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    listener.onMissionLongClicked(getAdapterPosition(),view);
//                    return true;
//                }
//            });

        }
    }

    @NonNull
    @Override
    public TorahFavLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lesson_item,parent,false);
        TorahFavLessonViewHolder torahFavLessonViewHolder = new TorahFavLessonViewHolder(view);
        return torahFavLessonViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final TorahFavLessonViewHolder holder, final int position) {

        final TorahLesson torahLesson = torahLessons.get(position);

        final DatabaseReference torahFavRef, myUserRef;
        final String currentUserID;
        FirebaseAuth mAuth;
        FirebaseUser fuser;

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        torahFavRef = FirebaseDatabase.getInstance().getReference().child("TorahRegisterList").child(currentUserID);


//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(mContext, MessageActivity.class);
//                intent.putExtra("userid", torahLesson.getUserID());
//                mContext.startActivity(intent);
//            }
//        });

        holder.rabbi_tv.setText(torahLesson.getRabbi());
        holder.subject_tv.setText(torahLesson.getTopic());
        holder.location_tv.setText(torahLesson.getLocation());
        holder.max_tv.setText(String.valueOf(torahLesson.getMax_Number_participants()));
        holder.current_tv.setText(String.valueOf(torahLesson.getCurrentNumberPerticipants()));
        holder.date_tv.setText(torahLesson.getDate());
        holder.time_tv.setText(torahLesson.getTime());

        holder.register_bt.setVisibility(View.INVISIBLE);
        holder.unsubscribe_bt.setVisibility(View.VISIBLE);



        holder.unsubscribe_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pos= (String.valueOf(position));

                //from firebase
                torahFavRef.child(torahLesson.getLessonID()).removeValue();

                final int count=torahLesson.getCurrentNumberPerticipants()-1;

                final DatabaseReference databaseRef=FirebaseDatabase.getInstance().getReference("lesson_torah"); //child(pos).child("currentNumberPerticipants");
                databaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren())
                        {
                            TorahLesson currentTorahLesson=snapshot.getValue(TorahLesson.class);
                            if(currentTorahLesson.getLessonID().equals(torahLesson.getLessonID())) {
                                snapshot.child("currentNumberPerticipants").getRef().setValue(count);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.current_tv.setText(String.valueOf(torahLesson.getCurrentNumberPerticipants()));
                Toast.makeText(mContext ,mContext.getString(R.string.Registration_canceled) , Toast.LENGTH_SHORT).show();

            }
        });

        holder.lesson_chat_bt.setVisibility(View.VISIBLE);
        holder.lesson_chat_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", torahLesson.getUserID());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return torahLessons.size();
    }


}
