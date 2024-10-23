package com.example.notebook;


import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.notebook.activities.CreateNewNoteActivity;
import com.example.notebook.activities.NoteViewActivity;
import com.example.notebook.adapters.NoteAdapter;
import com.example.notebook.db.NotesDatabases;
import com.example.notebook.entities.Note;
import com.example.notebook.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;


public class MainFragment extends Fragment implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_ALL_NOTES = 3;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 4;
    public static final int REQUEST_CODE_SELECT_IMAGE = 5;

    private int clickPosition = -1;
    private RecyclerView notesRecyclerView;
    private EditText inputSearch;
    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private View root;

    ImageButton addNewNote;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the current fragment view
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_main, container, false);
        }

        // Bind the click event of the Add Note button
        addNewNote = root.findViewById(R.id.imageButtonAddNote);
        addNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult( new Intent(root.getContext(), CreateNewNoteActivity.class), REQUEST_CODE_ADD_NOTE);
            }
        });

        // 快捷添加笔记按钮
        root.findViewById(R.id.iconAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult( new Intent(root.getContext(), CreateNewNoteActivity.class), REQUEST_CODE_ADD_NOTE);
            }
        });

        // 快捷添加图片按钮
        root.findViewById(R.id.iconAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });

        // Using root instead of activity
        notesRecyclerView = root.findViewById(R.id.noteRecyclerview);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        notesRecyclerView.setAdapter(noteAdapter);

        getNotes(REQUEST_CODE_SHOW_ALL_NOTES, false);

        // Search Notes listener
        inputSearch = root.findViewById(R.id.inputSearchBox);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Search for note
                if (noteList.size() != 0){
                    noteAdapter.searchNotes(editable.toString());
                }
            }
        });


        // Inflate the layout for this fragment
        return root;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            } else {
                Toast.makeText(getActivity(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getActivity().getContentResolver().query(contentUri, null, null, null, null);
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

    // Get all the notes information
    private void getNotes(final int requestCode, final boolean isNoteDelete){

        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            // Get a database connection
            @Override
            protected List<Note> doInBackground(Void... voids) {
                SharedPreferences userInfo = root.getContext().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                return NotesDatabases.getNotesDatabases(getActivity()).noteDao().getNotesByUserID(userInfo.getInt("userID", 0));
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                // Show all notes
                if (requestCode == REQUEST_CODE_SHOW_ALL_NOTES) {
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                    // add note
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    Log.d("123456", "onPostExecute: " + notes.get(0).getTitle());
                    noteList.add(0, notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                    // update note
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(clickPosition);
                    if (isNoteDelete) {
                        // if delete a note, delete the note in databases
                        noteAdapter.notifyItemRemoved(clickPosition);
                    } else {
                        noteList.add(clickPosition, notes.get(clickPosition));
                        noteAdapter.notifyItemChanged(clickPosition);
                    }
                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    // Confirm whether notes are being added or updated by checking the request code
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getActivity().getApplicationContext(), CreateNewNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception exception){
                        Toast.makeText(getActivity(),exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void selectImage(){
        // 提取图片文件
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    // set note click event
    public void onNoteClicked(Note note, int position) {
        clickPosition = position;
        Intent intent = new Intent(getActivity(), CreateNewNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        intent.putExtra("return", true);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }
}