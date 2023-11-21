package com.example.mychat;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;


import android.widget.AdapterView;
import android.widget.Button;

import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import nl.joery.animatedbottombar.AnimatedBottomBar;


public class ContactActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    Button btnNewContact;
    SearchView searchView;
    ListView listView;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    //
    FirebaseFirestore db;
    CollectionReference cref;
    Query query;
    //
    List<User> user=new ArrayList<>(); //tên người liên hệ
    String emailCurrentUser;
    SharedPreferences sharedPreferences;
    private MyArrayAdapter adapter;
    AnimatedBottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        searchView=findViewById(R.id.searchView);

        listView = (ListView) findViewById(R.id.listView);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(this);

        btnNewContact = (Button) findViewById(R.id.btnNewContact);
        bottomBar = findViewById(R.id.bottom_bar);

        applyNightMode();
        getListUserFromDatabase();

        searchUserWithUserName();

        bottomBar.selectTabAt(0, true);

        bottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                if(tab1.getId() == R.id.contact){
                    bottomBar.selectTabAt(i1, true);
                } else if (tab1.getId() == R.id.more) {
                    bottomBar.selectTabAt(i1, true);
                    Intent intent=new Intent(ContactActivity.this, MoreActivity.class);
                    startActivity(intent);
                } else if (tab1.getId() == R.id.chat) {
                    bottomBar.selectTabAt(i1, true);
                    Intent intent=new Intent(ContactActivity.this, ChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {

            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    private void searchUserWithUserName() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter!=null){
                    adapter.getFilter().filter(newText);

                }
                return true;
            }
        });
    }

    private void getListUserFromDatabase() {
        user = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        cref = db.collection("contact");
        DocumentReference doc = cref.document(auth.getCurrentUser().getUid().toString());
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        List<DocumentReference> docUser = (List<DocumentReference>) documentSnapshot.get("userContact");

                        final int totalUsers = docUser.size();
                        final int[] counter = {0};
                        for (DocumentReference d : docUser) {
                            d.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot userSnapshot = task.getResult();
                                        if (userSnapshot.exists()) {
                                            String username = userSnapshot.getString("username");
                                            String email = userSnapshot.getString("email");
                                            String uid=userSnapshot.getId();
                                            user.add(new User(username, "...", R.drawable.ic_avt, email,uid));
                                        }
                                    }

                                    counter[0]++;

                                    // Kiểm tra nếu tất cả các cuộc gọi đã hoàn thành
                                    if (counter[0] == totalUsers) {
                                        // Tất cả các cuộc gọi đã hoàn thành, cập nhật adapter ở đây
                                        adapter = new MyArrayAdapter(ContactActivity.this, R.layout.array_adapter, user);
                                        listView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "fail for", Toast.LENGTH_SHORT).show();
                                    counter[0]++;
                                }
                            });
                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
          User item=(User)adapterView.getItemAtPosition(i);
          Intent intent=new Intent(ContactActivity.this, ChatSreen.class);
          intent.putExtra("receiverID",item.getUid());
          startActivity(intent);
    }
    private void applyNightMode() {
        sharedPreferences=MyChat.getSharedPreferences();
        boolean nightMode=sharedPreferences.getBoolean("night",false);
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}


