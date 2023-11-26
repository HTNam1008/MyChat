package com.example.mychat;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

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


public class ChatActivity extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    ImageView imgNewChat;
    SearchView searchView;
    ListView listView;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    //
    FirebaseFirestore db;
    CollectionReference cref;
    List<User> user=new ArrayList<>(); //tên người liên hệ
    SharedPreferences sharedPreferences;
    private MyArrayAdapter adapter;
    Context context;
    MainFragment mainFragment;
    public static ChatActivity newInstance(String strArg) {
        ChatActivity fragment = new ChatActivity();
        Bundle args = new Bundle();
        args.putString("ChatActivity", strArg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
        try {
            context = getActivity(); // use this reference to invoke main callbacks
            mainFragment = (MainFragment) getActivity();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("MainFragment must implement callbacks");
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout layout_chat = (LinearLayout) inflater.inflate(R.layout.activity_chat, null);

        searchView=layout_chat.findViewById(R.id.searchView);

        listView = (ListView)layout_chat.findViewById(R.id.listView);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(this);

        imgNewChat = (ImageView) layout_chat.findViewById(R.id.imgNewChat);

        getListUserFromDatabase();

        searchUserWithUserName();
        return layout_chat;
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
        //get CONTACT collection
        cref = db.collection("contact");
        DocumentReference doc = cref.document(auth.getCurrentUser().getUid());
        //Toast.makeText(context,auth.getCurrentUser().getUid().toString(), Toast.LENGTH_SHORT).show();
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) { //get EVERY documents within the collection
                        //get the userContact array
                        List<DocumentReference> docUser = (List<DocumentReference>) documentSnapshot.get("userContact");
                        //get the total
                        final int totalUsers = docUser.size();
                        final int[] counter = {0};
                        for (DocumentReference d : docUser) { //get every user contact from array
                            d.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot userSnapshot = task.getResult();
                                        if (userSnapshot.exists()) {
                                            String username = userSnapshot.getString("username");
                                            String email = userSnapshot.getString("email");
                                            String uid=userSnapshot.getId();
                                            //now get the LATEST message

                                            user.add(new User(username, "abc", R.drawable.ic_avt, email,uid));
                                        }
                                    }
                                    counter[0]++;
                                    // Kiểm tra nếu tất cả các cuộc gọi đã hoàn thành
                                    if (counter[0] == totalUsers) {
                                        // Tất cả các cuộc gọi đã hoàn thành, cập nhật adapter ở đây
                                        adapter = new MyArrayAdapter(context, R.layout.array_adapter, user);
                                        listView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "fail for", Toast.LENGTH_SHORT).show();
                                    counter[0]++;
                                }
                            });
                        }
                    }

                } else {
                    Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        User item=(User)adapterView.getItemAtPosition(i);
        Intent intent=new Intent(context, ChatSreen.class);
        intent.putExtra("receiverID",item.getUid());
        startActivity(intent);
    }
}


