package com.example.asktherabbi.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.R;
import com.example.asktherabbi.TorahLesson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TorahLesson_mylesson_Adapter extends RecyclerView.Adapter<TorahLesson_mylesson_Adapter.myTorahLessonViewHolder> {
private Context mContext;
private List<TorahLesson> torahLessons;
private     MyListener listener;

    public interface MyListener {
        void onClicked(int position,View view);
    }
    public void setListener(MyListener listener) {
        this.listener = listener;
    }



    public TorahLesson_mylesson_Adapter(Context mContext, List<TorahLesson> torahLessons) {
        this.mContext = mContext;
        this.torahLessons = torahLessons;
        }


public class myTorahLessonViewHolder extends RecyclerView.ViewHolder {

    TextView rabbi_tv,subject_tv,location_tv,max_tv,current_tv,date_tv,time_tv,edit_tv;
    Button register_bt;
    ImageButton lesson_chat_bt;


    public myTorahLessonViewHolder(View itemView) {
        super(itemView);

        rabbi_tv = itemView.findViewById(R.id.rabbi);
        subject_tv = itemView.findViewById(R.id.subject);
        location_tv = itemView.findViewById(R.id.location);
        max_tv = itemView.findViewById(R.id.max_praticipants);
        current_tv = itemView.findViewById(R.id.current_praticipants);
        date_tv = itemView.findViewById(R.id.date);
        time_tv = itemView.findViewById(R.id.time);
        register_bt = itemView.findViewById(R.id.lesson_register_bt);
        edit_tv = itemView.findViewById(R.id.lesson_Edit_bt);
        lesson_chat_bt = itemView.findViewById(R.id.lesson_chat_bt);
        lesson_chat_bt.setVisibility(View.INVISIBLE);


        itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null)

                        listener.onClicked(getAdapterPosition(),view);
                }
            });


    }
}

    @NonNull
    @Override
    public TorahLesson_mylesson_Adapter.myTorahLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lesson_item,parent,false);
        TorahLesson_mylesson_Adapter.myTorahLessonViewHolder myTorahLessonViewHolder = new TorahLesson_mylesson_Adapter.myTorahLessonViewHolder(view);
        return myTorahLessonViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final TorahLesson_mylesson_Adapter.myTorahLessonViewHolder holder, final int position) {

        final TorahLesson torahLesson = torahLessons.get(position);

        final DatabaseReference torahFavRef, myUserRef;
        final String currentUserID;
        FirebaseAuth mAuth;
        FirebaseUser fuser;

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        torahFavRef = FirebaseDatabase.getInstance().getReference().child("TorahRegisterList").child(currentUserID);

        holder.rabbi_tv.setText(torahLesson.getRabbi());
        holder.subject_tv.setText(torahLesson.getTopic());
        holder.location_tv.setText(torahLesson.getLocation());
        holder.max_tv.setText(String.valueOf(torahLesson.getMax_Number_participants()));
        holder.current_tv.setText(String.valueOf(torahLesson.getCurrentNumberPerticipants()));
        holder.date_tv.setText(torahLesson.getDate());
        holder.time_tv.setText(torahLesson.getTime());

        holder.register_bt.setVisibility(View.INVISIBLE);
        holder.edit_tv.setVisibility(View.VISIBLE);



    }

    @Override
    public int getItemCount() {
        return torahLessons.size();
    }


}