package com.example.notebook.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.notebook.R;
import com.example.notebook.db.NotesDatabases;
import com.example.notebook.entities.User;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmail, etLoginPassword;
    private Button btnSignUp, btnSignIn;
    private String userProfilePath;
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.inputLoginEmail);
        etLoginPassword = findViewById(R.id.inputLoginPassword);
        btnSignIn = findViewById(R.id.loginSignInButton);
        btnSignUp = findViewById(R.id.loginSignUpButton);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        // 初始化
        if (getSharedPreferences("userInfo", MODE_PRIVATE) != null){
            getUserInfo();
            btnSignIn.callOnClick();
        }
    }

    private void login(){
        if (etLoginEmail.getText().toString().trim().isEmpty()){
            Toast.makeText(LoginActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (etLoginPassword.getText().toString().trim().isEmpty()){
            Toast.makeText(LoginActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
        } else {

            class LoginTask extends AsyncTask<Void, Void, Void> {
                List<User> users;

                @Override
                protected Void doInBackground(Void... voids) {
                    users = NotesDatabases.getNotesDatabases(getApplicationContext()).userDao().getUserByEmail(etLoginEmail.getText().toString().trim());
                    userProfilePath = users.get(0).getProfilePath();
                    userID = users.get(0).getId();
                    saveUserInfo();
                    return null;
                }

                @Override
                protected void onPostExecute(Void unused) {
                    super.onPostExecute(unused);
                    if (users.size() != 0 && users.get(0).getUserPassword().equals(etLoginPassword.getText().toString().trim())){
                        Toast.makeText(LoginActivity.this, "Login in successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(LoginActivity.this, "Wrong password or wrong email!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            new LoginTask().execute();
        }
    }

    private void saveUserInfo(){
        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//获取Editor
        //得到Editor后，写入需要保存的数据
        editor.putString("userEmail", etLoginEmail.getText().toString().trim());
        editor.putString("userPassword", etLoginPassword.getText().toString().trim());
        editor.putInt("userID", userID);
        if (userProfilePath != null){
            editor.putString("userProfilePath", userProfilePath);
        }
        editor.apply();
    }

    private void getUserInfo(){
        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userEmail = userInfo.getString("userEmail", null);
        String userPassword = userInfo.getString("userPassword", null);
        etLoginEmail.setText(userEmail);
        etLoginPassword.setText(userPassword);
    }
}
