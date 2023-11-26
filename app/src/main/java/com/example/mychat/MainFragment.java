package com.example.mychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainFragment extends AppCompatActivity {
    FragmentTransaction ft;
    MoreActivity moreActivity;
    ContactActivity contactActivity;
    ChatActivity chatActivity;
    AnimatedBottomBar bottomBar;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fragment);

        ft = getSupportFragmentManager().beginTransaction();
        chatActivity = ChatActivity.newInstance("init");
        ft.replace(R.id.main_holder, chatActivity)
        .setReorderingAllowed(true)
        .addToBackStack("name")
        .commit();

        bottomBar=findViewById(R.id.bottom_bar);

        initBottomBar();

        applyNightMode();
    }

    public void changeFragment(Fragment selectedFragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_holder, selectedFragment)
                .setReorderingAllowed(true)
                .addToBackStack("name")
                .commit();
    }


    private void initBottomBar(){
        bottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                Fragment selectedFragment = null;


                switch (i1) {
                    case 0:
                        selectedFragment = new ChatActivity();
                        break;
                    case 1:
                        selectedFragment = new ContactActivity();
                        break;
                    case 2:
                        selectedFragment = new MoreActivity();
                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in,  // enter
                                    R.anim.fade_out,  // exit
                                    R.anim.fade_in,   // popEnter
                                    R.anim.slide_out  // popExit
                            )
                            .replace(R.id.main_holder, selectedFragment)
                            .commit();
                }
            }
            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {

            }
        });

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
