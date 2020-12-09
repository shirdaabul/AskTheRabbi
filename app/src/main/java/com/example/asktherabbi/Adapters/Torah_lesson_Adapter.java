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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class Torah_lesson_Adapter extends RecyclerView.Adapter<Torah_lesson_Adapter.TorahLessonViewHolder> {
    private Context mContext;
    private List<TorahLesson> torahLessons;

    int flag=0;

    public Torah_lesson_Adapter(Context mContext, List<TorahLesson> torahLessons) {
        this.mContext = mContext;
        this.torahLessons = torahLessons;
    }
//
//    public Torah_lesson_Adapter(List<TorahLesson> missions) {
//        this.torahLessons = missions;
//        this.mContext = mContext;
//
//    }

//    interface TorahLessonListener {
//        void onMissionClicked(int position, View view);
//        void onMissionLongClicked(int position, View view);
//    }

//    private TorahLessonListener listener;

//    public void setListener(TorahLessonListener listener) {
//        this.listener = listener;
//    }

    public class TorahLessonViewHolder extends RecyclerView.ViewHolder {

        TextView rabbi_tv,subject_tv,location_tv,max_tv,current_tv,date_tv,time_tv;
        Button register_bt,unsubscribe_bt;
        ImageButton lesson_chat_bt;

        public TorahLessonViewHolder(View itemView) {
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
    public TorahLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lesson_item,parent,false);
        TorahLessonViewHolder torahLessonViewHolderViewHolder = new TorahLessonViewHolder(view);
        return torahLessonViewHolderViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final TorahLessonViewHolder holder, final int position) {

        final TorahLesson torahLesson = torahLessons.get(position);

        final DatabaseReference torahFavRef, myUserRef;
        final List<TorahLesson> RegisterLessonsList=new ArrayList<>(), mLesson = new ArrayList<TorahLesson>();
        final FirebaseAuth mAuth;
        final FirebaseUser fuser;

        mAuth = FirebaseAuth.getInstance();
        fuser = mAuth.getCurrentUser();

        if(!fuser.isAnonymous()) {
            torahFavRef = FirebaseDatabase.getInstance().getReference().child("TorahRegisterList").child(fuser.getUid());

            //current user fav lesson list
            torahFavRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    RegisterLessonsList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        TorahLesson lesson = snapshot.getValue(TorahLesson.class);
                        RegisterLessonsList.add(lesson);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.lesson_chat_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MessageActivity.class);
                    intent.putExtra("userid", torahLesson.getUserID());
                    mContext.startActivity(intent);
                }
            });
        }
        
        else
            holder.lesson_chat_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, mContext.getString(R.string.regist_to_start_chat), Toast.LENGTH_SHORT).show();
                }
            });



        holder.rabbi_tv.setText(torahLesson.getRabbi());
        holder.subject_tv.setText(torahLesson.getTopic());
        holder.location_tv.setText(torahLesson.getLocation());
        holder.max_tv.setText(String.valueOf(torahLesson.getMax_Number_participants()));
        holder.current_tv.setText(String.valueOf(torahLesson.getCurrentNumberPerticipants()));
        holder.date_tv.setText(torahLesson.getDate());
        holder.time_tv.setText(torahLesson.getTime());


        for(TorahLesson registerLesson : RegisterLessonsList ) {
            if (torahLesson.getLessonID().equals(registerLesson.getLessonID()))
                mLesson.add(torahLesson);
        }


        for(TorahLesson registerLesson : mLesson ){
            if(registerLesson.getLessonID().equals(torahLesson.getLessonID())){
                holder.register_bt.setVisibility(View.GONE);
                flag=1;
            }
        }


//        holder.register_bt.setVisibility(View.VISIBLE);
//        holder.unsubscribe_bt.setVisibility(View.INVISIBLE);

        holder.register_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!fuser.isAnonymous()){
                    int max = torahLesson.getMax_Number_participants();
                    final int count = torahLesson.getCurrentNumberPerticipants();

                    if (count < max) {
                        DatabaseReference FavUserRef;
                        FavUserRef = FirebaseDatabase.getInstance().getReference().child("TorahRegisterList").child(fuser.getUid()).child(torahLesson.getLessonID());

                        //create LessonFav object (for register list)
                        HashMap<String, Object> MessageInfoMap = new HashMap<>();
                        MessageInfoMap.put("lessonID", torahLesson.getLessonID());
                        FavUserRef.updateChildren(MessageInfoMap);

                        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("lesson_torah");//.child(pos).child("currentNumberPerticipants");
                        databaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                                {
                                    TorahLesson currentTorahLesson=snapshot.getValue(TorahLesson.class);
                                    if(currentTorahLesson.getLessonID().equals(torahLesson.getLessonID())) {
                                        snapshot.child("currentNumberPerticipants").getRef().setValue(count+1);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
//                        databaseRef.setValue(count);
//                    torahLesson.setCurrentNumberPerticipants(count);
                        holder.current_tv.setText(String.valueOf(count+1));
                        Toast.makeText(mContext, mContext.getString(R.string.Registration_done), Toast.LENGTH_SHORT).show();


                    } else
                        Toast.makeText(mContext, mContext.getString(R.string.quotafull), Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(mContext, mContext.getString(R.string.register_to_registeLesoon), Toast.LENGTH_SHORT).show();

            }
        });





    }

    @Override
    public int getItemCount() {
        return torahLessons.size();
    }
}