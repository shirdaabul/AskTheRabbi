package com.example.asktherabbi.Fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.Adapters.TorahLessonFavAdapter;
import com.example.asktherabbi.Adapters.TorahLesson_mylesson_Adapter;
import com.example.asktherabbi.Adapters.Torah_lesson_Adapter;
import com.example.asktherabbi.Cities;
import com.example.asktherabbi.R;
import com.example.asktherabbi.TorahLesson;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class TorahLessonFragment extends Fragment {
    RecyclerView recyclerView;
    Button tag_my_owner, tag_my_register, tag_all;
    int currentYear, currentMonth, currentDayOfMonth;
    int  currentDayOfWeek, currentHour, currentMinute;
    int chooseYear = -1, chooseMonth = -1, chooseDayOfMonth = -1, chooseHour = -1, chooseMinute = -1, chooseDayOfWeek = -1;
    Calendar beginTimeDate = Calendar.getInstance();
    Calendar c;
    DatePickerDialog dpd;
    Calendar currenttime;
    int hour,minute;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    CoordinatorLayout coordinatorLayout;
    Torah_lesson_Adapter adapter;
    TorahLessonFavAdapter favAdapter;
    TorahLesson_mylesson_Adapter mylesson_adapter;

    List<TorahLesson> allTorahLessonList = new ArrayList<>();

    List<TorahLesson> myTorahLessonList, allTorahLessonList2;

    List<TorahLesson> RegisterLessonsList=new ArrayList<>(),mLesson;
    TorahLesson torahLesson_edit,torahLesson_del;
    String lesson_ID;
    FloatingActionButton fab;
    String fullName;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener authStateListener;

    FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    //String userid= firebaseUser.getUid();
    DatabaseReference lessonIdIndex = FirebaseDatabase.getInstance().getReference("lesson_id");


    DatabaseReference lesson_torah_reference = FirebaseDatabase.getInstance().getReference("lesson_torah");
    Cities cities;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        lessonIdIndex.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lesson_ID=dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        View view = inflater.inflate(R.layout.fragment_torah_lesson, container, false);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!firebaseUser.isAnonymous()) {
                    View dialogView = getLayoutInflater().inflate(R.layout.add_lesson_dialog, null);

                    final EditText topic_t = dialogView.findViewById(R.id.topic_txt);
                    final EditText rabbi_t = dialogView.findViewById(R.id.rabbi_txt);
                    final EditText max_t = dialogView.findViewById(R.id.max_txt);
                    final TextView datetv = dialogView.findViewById(R.id.tv_date);
                    final TextView timetv = dialogView.findViewById(R.id.tv_time);

                    Button datebtn = dialogView.findViewById(R.id.btn_date);
                    datebtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            c = Calendar.getInstance();
                            final int day = c.get(Calendar.DAY_OF_MONTH);
                            int month = c.get(Calendar.MONTH);
                            int year = c.get(Calendar.YEAR);
                            dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                                    datetv.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                                    chooseYear = year;
                                    chooseMonth = month + 1;
                                    chooseDayOfMonth = dayOfMonth;

                                }
                            }, currentYear, currentMonth, currentDayOfMonth);
                            dpd.show();


                        }
                    });

                    currenttime = Calendar.getInstance();
                    hour = currenttime.get(Calendar.HOUR_OF_DAY);
                    minute = currenttime.get(Calendar.MONTH);
                    Button timebtn = dialogView.findViewById(R.id.btn_time);
                    timebtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    if (minute < 10) {
                                        timetv.setText(hourOfDay + ":" + "0" + minute);
                                    } else {
                                        timetv.setText(hourOfDay + ":" + minute);
                                    }
                                    chooseHour = hourOfDay;
                                    chooseMinute = minute;
                                    ;
                                }
                            }, currentHour, currentMinute, true);
                            timePickerDialog.show();
                        }


                    });


                    final AutoCompleteTextView et_city = dialogView.findViewById(R.id.location_txt);
                    ArrayList<String> cities1 = new ArrayList<>();
                    cities = new Cities(getContext(), cities1);
                    cities.loadCities();
                    final ArrayAdapter<String> adapter_city = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, cities.getCities());
                    et_city.setAdapter(adapter_city);


                    final AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setView(dialogView)
                            .setPositiveButton(getResources().getString(R.string.post), null) //Set to null. We override the onclick
                            .setNegativeButton(getResources().getString(R.string.cancle), null)
                            .create();

                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(DialogInterface dialogInterface) {

                            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {

                                    int max_participants;
                                    String topic = topic_t.getText().toString();
                                    String rabbi = rabbi_t.getText().toString();
                                    String city = et_city.getText().toString();
                                    if (TextUtils.isEmpty(max_t.getText().toString()))
                                        max_participants = 0;
                                    else
                                        max_participants = Integer.parseInt(max_t.getText().toString());
                                    String date = datetv.getText().toString();
                                    String time = timetv.getText().toString();


                                    if (TextUtils.isEmpty(topic) || TextUtils.isEmpty(rabbi) || TextUtils.isEmpty(city) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || max_participants == 0)
                                        Toast.makeText(getContext(), getResources().getString(R.string.all_fileds_are_required), Toast.LENGTH_SHORT).show();

                                        //Dismiss once everything is OK.
                                    else {

                                        lessonIdIndex.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                lesson_ID = dataSnapshot.getValue(String.class);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        allTorahLessonList.add(new TorahLesson(lesson_ID, firebaseUser.getUid(), topic, max_participants, 0, city, date, time, rabbi, false));
                                        adapter.notifyItemInserted(allTorahLessonList.size() - 1);

                                        //update the database
                                        lesson_torah_reference.setValue(allTorahLessonList);
                                        lesson_ID = lesson_ID + "1";
                                        lessonIdIndex.setValue(lesson_ID);
                                        Toast.makeText(getContext(), getResources().getString(R.string.lesson_added), Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();

                                    }
                                }
                            });
                        }
                    });
                    dialog.show();
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.Regist_add_lesson), Toast.LENGTH_SHORT).show();
                }
            }});


        recyclerView = view.findViewById(R.id.recycler);
        adapter = new Torah_lesson_Adapter(getContext(), allTorahLessonList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);


        if (!firebaseUser.isAnonymous()) {
            //create registered lesson list for current user
            DatabaseReference torahFavRef = FirebaseDatabase.getInstance().getReference().child("TorahRegisterList").child(firebaseUser.getUid());
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
        }

        //display all torah lessons - all users
        lesson_torah_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allTorahLessonList.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        TorahLesson torahLesson = snapshot.getValue(TorahLesson.class);
                        allTorahLessonList.add(torahLesson);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        tag_all = view.findViewById(R.id.tag_all);
        tag_my_owner = view.findViewById(R.id.tag_my_owner);
        tag_my_register = view.findViewById(R.id.tag_my_registers);

        tag_my_owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseUser.isAnonymous())
                    Toast.makeText(getContext(), getContext().getString(R.string.register_to_display), Toast.LENGTH_SHORT).show();
                else{
                    sortGroups_myLessons("שיעורים שלי");
                    fab.hide();
                }
            }
        });

        tag_my_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseUser.isAnonymous())
                    Toast.makeText(getContext(), getContext().getString(R.string.register_to_display), Toast.LENGTH_SHORT).show();
                else {
                    sortGroups_registerLesson("שיעורים שנרשמתי");
                    fab.hide();
                }

            }
        });

        tag_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.show();
                if (firebaseUser.isAnonymous()) {
                    //display all torah lessons - all users
                    lesson_torah_reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            allTorahLessonList.clear();

                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    TorahLesson torahLesson = snapshot.getValue(TorahLesson.class);
                                    allTorahLessonList.add(torahLesson);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    loadList();
                }
            }
        });


        return view;

    }

    //loading only user's lessons - created
    private void sortGroups_myLessons(final String s){


        tag_my_owner.setBackgroundResource(R.drawable.btn_shape_lesson_press);
        tag_my_register.setBackgroundResource(R.drawable.btn_shape);
        tag_all.setBackgroundResource(R.drawable.btn_shape);

        lesson_torah_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myTorahLessonList = new ArrayList<TorahLesson>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    TorahLesson g = dataSnapshot1.getValue(TorahLesson.class);
                    if(g.getUserID().equals(firebaseUser.getUid()))
                        myTorahLessonList.add(g);
                }

                mylesson_adapter = new TorahLesson_mylesson_Adapter(getContext(), myTorahLessonList);
                mylesson_adapter.setListener(new TorahLesson_mylesson_Adapter.MyListener() {
                    @Override
                    public void onClicked(final int position, View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        View dialogView  = getLayoutInflater().inflate(R.layout.add_lesson_dialog,null);

                        final EditText topic_t = dialogView.findViewById(R.id.topic_txt);
                        final EditText rabbi_t = dialogView.findViewById(R.id.rabbi_txt);
                        final EditText max_t = dialogView.findViewById(R.id.max_txt);
                        final TextView datetv = dialogView.findViewById(R.id.tv_date);
                        final TextView timetv = dialogView.findViewById(R.id.tv_time);
                        final TextView title_dia = dialogView.findViewById(R.id.title_dialog);
                        title_dia.setText(getResources().getString(R.string.edit_lesson));
                        final AutoCompleteTextView  et_city = dialogView.findViewById(R.id.location_txt);
                        ArrayList<String>cities1 = new ArrayList<>();
                        cities = new Cities(getContext(),cities1);
                        cities.loadCities();
                        final ArrayAdapter<String> adapter_city = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,cities.getCities());
                        et_city.setAdapter(adapter_city);

                        torahLesson_edit=myTorahLessonList.get(position);

                        final String lessonID=torahLesson_edit.getLessonID();

                        topic_t.setText(torahLesson_edit.getTopic());
                        rabbi_t.setText(torahLesson_edit.getRabbi());
                        et_city.setText(torahLesson_edit.getLocation());
                        max_t.setText(String.valueOf(torahLesson_edit.getMax_Number_participants()));
                        datetv.setText(torahLesson_edit.getDate());
                        timetv.setText(torahLesson_edit.getTime());



                        Button datebtn=dialogView.findViewById(R.id.btn_date);
                        datebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                c=Calendar.getInstance();
                                final int day=c.get(Calendar.DAY_OF_MONTH);
                                int month=c.get(Calendar.MONTH);
                                int year=c.get(Calendar.YEAR);
                                dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        dpd.getDatePicker().setMinDate(System.currentTimeMillis()-1000);

                                        datetv.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                                        chooseYear = year;
                                        chooseMonth = month + 1;
                                        chooseDayOfMonth = dayOfMonth;

                                    }
                                }, currentYear, currentMonth, currentDayOfMonth);
                                dpd.show();



                            }
                        });

                        currenttime=Calendar.getInstance();
                        hour=currenttime.get(Calendar.HOUR_OF_DAY);
                        minute=currenttime.get(Calendar.MONTH);
                        Button timebtn=dialogView.findViewById(R.id.btn_time);
                        timebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        if(minute < 10) {
                                            timetv.setText(hourOfDay + ":" + "0" + minute);
                                        }
                                        else{
                                            timetv.setText(hourOfDay + ":" + minute);
                                        }
                                        chooseHour = hourOfDay;
                                        chooseMinute = minute;
                                        ;
                                    }
                                }, currentHour, currentMinute, true);
                                timePickerDialog.show();
                            }


                        });





                        builder.setView(dialogView).setPositiveButton(getResources().getString(R.string.save_changes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int max_participants;
                                String topic = topic_t.getText().toString();
                                String rabbi = rabbi_t.getText().toString();
                                String city = et_city.getText().toString();
                                if(TextUtils.isEmpty(max_t.getText().toString()))
                                    max_participants=0;
                                else
                                    max_participants = Integer.parseInt(max_t.getText().toString());
                                String date = datetv.getText().toString();
                                String time = timetv.getText().toString();

                                if(TextUtils.isEmpty(topic)||TextUtils.isEmpty(rabbi)||TextUtils.isEmpty(city)||TextUtils.isEmpty(date)||TextUtils.isEmpty(time)||max_participants==0)
                                    Toast.makeText(getContext(), getResources().getString(R.string.all_fileds_are_required), Toast.LENGTH_SHORT).show();
                                else {
                                    torahLesson_edit.setTopic(topic);
                                    torahLesson_edit.setRabbi(rabbi);
                                    torahLesson_edit.setLocation(city);
                                    torahLesson_edit.setMax_Number_participants(max_participants);
                                    torahLesson_edit.setTime(time);
                                    torahLesson_edit.setDate(date);


                                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("lesson_torah");//.child(pos).child("currentNumberPerticipants");
                                    databaseRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot snapshot: dataSnapshot.getChildren())
                                            {
                                                TorahLesson currentTorahLesson=snapshot.getValue(TorahLesson.class);
                                                if(currentTorahLesson.getLessonID().equals(torahLesson_edit.getLessonID())) {
                                                    snapshot.getRef().setValue(torahLesson_edit);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    //adapter.notifyItemInserted(allTorahLessonList.size() - 1);
                                    //update the database
                                    Toast.makeText(getContext(), getResources().getString(R.string.lesson_update), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).show();
                    }
                });


                ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder dragged, RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
                        torahLesson_del=myTorahLessonList.get(viewHolder.getAdapterPosition());

                        if((direction==ItemTouchHelper.RIGHT)|(direction==ItemTouchHelper.LEFT)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage(getResources().getString(R.string.sure_delete_lesson));
                            builder.setCancelable(true);

                            builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("lesson_torah");//.child(pos).child("currentNumberPerticipants");
                                    databaseRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot snapshot: dataSnapshot.getChildren())
                                            {
                                                TorahLesson currentTorahLesson=snapshot.getValue(TorahLesson.class);
                                                if(currentTorahLesson.getLessonID().equals(torahLesson_del.getLessonID())) {
                                                    snapshot.getRef().removeValue();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });


                                    Toast.makeText(getContext(), getResources().getString(R.string.lesson_deleted), Toast.LENGTH_SHORT).show();
                                    //  manager.removeAnimal(viewHolder.getAdapterPosition());
                                    mylesson_adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                                }
                            });


                            builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mylesson_adapter.notifyDataSetChanged();
                                    dialog.cancel();
                                }
                            });

                            AlertDialog exit_dialog = builder.create();
                            exit_dialog.show();

                        }
                    }
                };
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(recyclerView);
                recyclerView.setAdapter(mylesson_adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Toast.makeText(getContext(), getResources().getString(R.string.oops), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //loading only user's lessons - register
    private void sortGroups_registerLesson(final String s){


        tag_my_owner.setBackgroundResource(R.drawable.btn_shape);
        tag_my_register.setBackgroundResource(R.drawable.btn_shape_lesson_press);
        tag_all.setBackgroundResource(R.drawable.btn_shape);

        lesson_torah_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mLesson=new ArrayList<TorahLesson>();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    TorahLesson torahLesson=snapshot.getValue(TorahLesson.class);
                    for(TorahLesson registerLesson : RegisterLessonsList ){
                        if(torahLesson.getLessonID().equals(registerLesson.getLessonID()))
                            mLesson.add(torahLesson);
                    }
                }
                favAdapter=new TorahLessonFavAdapter(getContext(),mLesson);
                recyclerView.setAdapter(favAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Toast.makeText(getContext(), getResources().getString(R.string.oops), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //loading lessons - all
    private void loadList() {

        tag_my_owner.setBackgroundResource(R.drawable.btn_shape);
        tag_my_register.setBackgroundResource(R.drawable.btn_shape);
        tag_all.setBackgroundResource(R.drawable.btn_shape_lesson_press);

        lesson_torah_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allTorahLessonList2 = new ArrayList<TorahLesson>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int flag = 0;
                    TorahLesson torahLesson = snapshot.getValue(TorahLesson.class);
                    for (TorahLesson registerLesson : RegisterLessonsList) {
                        if (torahLesson.getLessonID().equals(registerLesson.getLessonID())) {
                            flag = 1;
                        }
                    }
                    if (flag == 0)
                        allTorahLessonList2.add(torahLesson);
                }
                adapter = new Torah_lesson_Adapter(getContext(), allTorahLessonList2);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //       Toast.makeText(getContext(), getResources().getString(R.string.oops), Toast.LENGTH_SHORT).show();
            }
        });
    }}
