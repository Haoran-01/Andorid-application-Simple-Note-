package com.example.notebook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notebook.R;
import com.example.notebook.db.NotesDatabases;
import com.example.notebook.entities.User;
import com.google.android.material.navigation.NavigationView;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassword, registerRepeatPassword, registerName;
    private Button btnGoBack, btnSignUp;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerName = findViewById(R.id.inputRegisterName);
        registerEmail = findViewById(R.id.inputRegisterEmail);
        registerPassword = findViewById(R.id.inputRegisterPassword);
        registerRepeatPassword = findViewById(R.id.inputRegisterPasswordRepeat);

        btnGoBack = findViewById(R.id.registerBackButton);
        btnSignUp = findViewById(R.id.registerSignUpButton);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }


    private void register(){
        // check name
        if (registerName.getText().toString().trim().isEmpty()){
            Toast.makeText(RegisterActivity.this, "User name cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (registerEmail.getText().toString().trim().isEmpty()){
            // check email
            Toast.makeText(RegisterActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (registerPassword.getText().toString().trim().isEmpty()){
            // check password
            Toast.makeText(RegisterActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (registerPassword.getText().toString().trim().equals(registerRepeatPassword.getText().toString().trim())){
            // successful register
            final User newUser = new User();
            Toast.makeText(RegisterActivity.this, "Register successful!", Toast.LENGTH_SHORT).show();

            class SaveUserTask extends AsyncTask<Void, Void, Void> {

                @Override
                protected Void doInBackground(Void... voids) {
                    newUser.setUserName(registerName.getText().toString().trim());
                    newUser.setUserEmail(registerEmail.getText().toString().trim());
                    newUser.setUserPassword(registerPassword.getText().toString().trim());
                    NotesDatabases.getNotesDatabases(getApplicationContext()).userDao().insertUser(newUser);
                    return null;
                }

                @Override
                protected void onPostExecute(Void unused) {
                    super.onPostExecute(unused);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            new SaveUserTask().execute();

        } else {
            Toast.makeText(RegisterActivity.this, "Twice input password not equal", Toast.LENGTH_SHORT).show();
        }
    }
}