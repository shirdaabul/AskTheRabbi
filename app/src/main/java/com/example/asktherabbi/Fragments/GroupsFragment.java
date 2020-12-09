package com.example.asktherabbi.Fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Adapter.AllGroupAdapter;
import com.example.asktherabbi.message.Adapter.FavoriteGroupAdapter;
import com.example.asktherabbi.message.Model.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class GroupsFragment extends Fragment {

    DatabaseReference reference, referenceFav;
    RecyclerView recyclerView, recyclerViewFav;
    ArrayList<Group> listGroup, listFav;
    AllGroupAdapter adapter;
    FavoriteGroupAdapter adapterFav;
    EditText search_groups;
    private FirebaseAuth mAuth;
    String currentUserID;
    Button filter;
    TextView groupsName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = view.findViewById(R.id.recyclerview_groups);
        recyclerView.setLayoutManager( new LinearLayoutManager(getActivity()));

        recyclerViewFav = view.findViewById(R.id.recyclerview_fav);
        recyclerViewFav.setLayoutManager( new LinearLayoutManager(getActivity()));

        mAuth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference().child("Groups");
        if(mAuth.getCurrentUser()!=null)
        {   if(!(mAuth.getCurrentUser().isAnonymous()))
            {
            currentUserID = mAuth.getCurrentUser().getUid();
            referenceFav = FirebaseDatabase.getInstance().getReference().child("FavoritesList").child(currentUserID);
            loadFavList();
        }}

        loadList();




        search_groups = view.findViewById(R.id.search_groups);
        search_groups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchGroups(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        groupsName=view.findViewById(R.id.group_name_tv);

        filter=view.findViewById(R.id.filter_bt);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View dialogView = getLayoutInflater().inflate(R.layout.choose_category_dialog, null);

                final RadioButton torah, prayerBlassing,cosher,holidayShabat,mouring,marrigeFamily,faite,allGroups;
                Button done;

                torah=dialogView.findViewById(R.id.torahRB);
                prayerBlassing=dialogView.findViewById(R.id.prayeBlassingrRB);
                cosher=dialogView.findViewById(R.id.cosherRB);
                holidayShabat=dialogView.findViewById(R.id.holiday_shabatRB);
                mouring=dialogView.findViewById(R.id.mourningRB);
                marrigeFamily=dialogView.findViewById(R.id.marriage_familyRB);
                faite=dialogView.findViewById(R.id.faithRB);
                allGroups=dialogView.findViewById(R.id.all_groupsRB);

                done=dialogView.findViewById(R.id.done_filter_bt);

                final AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(torah.isChecked()){
                            groupsName.setText(getResources().getString(R.string.all_groups_torah));
                            sortGroups("תורה");}

                        if(prayerBlassing.isChecked()){
                            groupsName.setText(getResources().getString(R.string.all_groups_prayer_blassing));
                            sortGroups("תפילה וברכות");
                        }

                        if(cosher.isChecked()){
                            groupsName.setText(getResources().getString(R.string.all_groups_cosher));
                            sortGroups("כשרות");}

                        if(holidayShabat.isChecked()){
                            groupsName.setText(getResources().getString(R.string.all_groups_holidayShabat));
                            sortGroups("חגים ושבתות");}

                        if(mouring.isChecked()){
                            groupsName.setText(getResources().getString(R.string.all_groups_mouring));
                            sortGroups("אבלות");}

                        if(marrigeFamily.isChecked()){
                            groupsName.setText(getResources().getString(R.string.all_groups_marrigeFamily));
                            sortGroups("נישואים ומשפחה");}

                        if(faite.isChecked()){
                            groupsName.setText(getResources().getString(R.string.all_groups_faite));
                            sortGroups("אמונה");}

                        if(allGroups.isChecked()){
                            groupsName.setText(getResources().getString(R.string.all_groups));
                            loadList();}


                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        return view;
    }


    //search group by name
    private void searchGroups(String s) {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Groups").orderByChild("name")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(listGroup !=null)
                    listGroup.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Group group = snapshot.getValue(Group.class);

                    assert group != null;
                    assert fuser != null;
                    //how to skip anonymous user?

                    listGroup.add(group);
                }

                adapter = new AllGroupAdapter(getContext(), listGroup);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    //sort group by radio button
    private void sortGroups(final String s){

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listGroup = new ArrayList<Group>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    Group g = dataSnapshot1.getValue(Group.class);
                    if(g.getAffiliation().equals(s))
                        listGroup.add(g);
                }
                adapter = new AllGroupAdapter(getContext(), listGroup);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //sort group - all groups (without the favorites)
    private void loadList(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listGroup = new ArrayList<Group>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    Group g = dataSnapshot1.getValue(Group.class);
                    listGroup.add(g);
                }
                adapter = new AllGroupAdapter(getContext(), listGroup);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //display only favorite groups
    private void loadFavList(){
        referenceFav.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listFav = new ArrayList<Group>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    Group g = dataSnapshot1.getValue(Group.class);
                    listFav.add(g);

                }
                adapterFav = new FavoriteGroupAdapter(getContext(),listFav);
                recyclerViewFav.setAdapter(adapterFav);
                if(adapterFav.getItemCount()>=3){
                    ViewGroup.LayoutParams params=recyclerViewFav.getLayoutParams();
                    params.height=470;
                    recyclerViewFav.setLayoutParams(params);
                }
                else{
                    ViewGroup.LayoutParams params=recyclerViewFav.getLayoutParams();
                    params.height=MATCH_PARENT;
                    recyclerViewFav.setLayoutParams(params);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
}