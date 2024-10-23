package com.example.notebook;

import static android.app.Activity.RESULT_OK;

import static com.example.notebook.MainFragment.REQUEST_CODE_STORAGE_PERMISSION;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.room.RoomDatabase;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notebook.activities.CreateNewNoteActivity;
import com.example.notebook.activities.LoginActivity;
import com.example.notebook.db.NotesDatabases;
import com.example.notebook.entities.Note;
import com.example.notebook.entities.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class UserProfileFragment extends Fragment {
    private View root;
    private ShapeableImageView profileHeaderPicture, profilePicture;
    private EditText profileUserName, profileUserEmail;
    private String selectImagePath;
    private ConstraintLayout logoutButton;

    public static final int REQUEST_CODE_SELECT_IMAGE = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (root == null){
            root = inflater.inflate(R.layout.fragment_user_profile, container, false);
        }

        profileHeaderPicture = root.findViewById(R.id.profile_header_picture);
        profilePicture = root.findViewById(R.id.profile_user_picture);
        profileUserName = root.findViewById(R.id.profileUserName);
        profileUserEmail = root.findViewById(R.id.profileEmail);
        logoutButton = root.findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        profilePicture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        root.getContext().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
                return true;
            }
        });
        
        getUserInfo();
        return root;
    }

    private void logout(){
        SharedPreferences sharedPreferences = root.getContext().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }

    private void getUserInfo(){

        SharedPreferences userInfo = root.getContext().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String userEmail = userInfo.getString("userEmail", null);
        final String[] userProfilePath = {userInfo.getString("userProfilePath", null)};
        System.out.println(userProfilePath[0]);

        class UserInfoTask extends AsyncTask<Void, Void, Void> {
            List<User> users;

            @Override
            protected Void doInBackground(Void... voids) {
                users = NotesDatabases.getNotesDatabases(getActivity().getApplicationContext()).userDao().getUserByEmail(userEmail.trim());
                userProfilePath[0] = users.get(0).getProfilePath();
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                profileUserEmail.setText(userEmail);
                profileUserName.setText(users.get(0).getUserName().trim());
                if (userProfilePath[0] != null){
                    profilePicture.setImageBitmap(BitmapFactory.decodeFile(userProfilePath[0]));
                }
            }
        }
        new UserInfoTask().execute();
    }

    private void changeUserInfo(){
        SharedPreferences userInfo = root.getContext().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String userEmail = userInfo.getString("userEmail", null);

        class UserInfoTask extends AsyncTask<Void, Void, Void> {
            List<User> users;

            @Override
            protected Void doInBackground(Void... voids) {
                users = NotesDatabases.getNotesDatabases(getActivity().getApplicationContext()).userDao().getUserByEmail(userEmail.trim());
                users.get(0).setUserName(profileUserName.getText().toString().trim());
                users.get(0).setProfilePath(selectImagePath);
                NotesDatabases.getNotesDatabases(getActivity().getApplicationContext()).userDao().updateUser(users.get(0));
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
            }
        }
        new UserInfoTask().execute();
        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString("userProfilePath", selectImagePath);
        editor.apply();
    }

    private void selectImage(){
        // 提取图片文件
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    // 监听是否有权限读取文件
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            } else {
                Toast.makeText(getContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        InputStream inputStream = root.getContext().getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        profilePicture.setImageBitmap(bitmap);
                        profilePicture.setVisibility(View.VISIBLE);
                        selectImagePath = getPathFromUri(selectedImageUri);
                        changeUserInfo();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = root.getContext().getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
}