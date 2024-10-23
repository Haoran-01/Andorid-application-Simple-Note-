package com.example.notebook.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


import com.example.notebook.KnowledgeBaseFragment;
import com.example.notebook.MainFragment;
import com.example.notebook.R;
import com.example.notebook.interfaces.fOnFocusListenable;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageButton btnMenu;
    NavigationView navigationView;
    NavHostFragment navHostFragment;
    ShapeableImageView headerPicture;
    ConstraintLayout header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find the layout
        drawerLayout = findViewById(R.id.drawerLayout);

        // set the button of menu
        btnMenu = findViewById(R.id.imageButtonMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // set the navigation view
        navigationView = findViewById(R.id.navigationView);
        navigationView.setItemIconTintList(null);

        header = (ConstraintLayout) navigationView.getHeaderView(0);
        headerPicture = header.findViewById(R.id.profile_picture);
        SharedPreferences userInfo = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String picturePath = userInfo.getString("userProfilePath", null);

        if (picturePath != null){
            headerPicture.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }

        NavController navController;
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    // For jumping between activity and fragment
    protected void onResume() {
        super.onResume();
        int id = getIntent().getIntExtra("redirection", 0);
        // set header photo

        SharedPreferences userInfo = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String picturePath = userInfo.getString("userProfilePath", null);
        if (picturePath != null){
            headerPicture.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }

        if (id == 3) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragment, new KnowledgeBaseFragment())
                    .addToBackStack(null)
                    .commit();
            navigationView.setCheckedItem(R.id.knowledgeBase);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(navHostFragment instanceof fOnFocusListenable) {
            ((fOnFocusListenable) navHostFragment).onWindowFocusChanged(hasFocus);
        }
    }
}