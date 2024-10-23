package com.example.notebook.activities;

import static com.example.notebook.KnowledgeBaseFragment.REQUEST_CODE_ADD_TAG;
import static com.example.notebook.KnowledgeBaseFragment.REQUEST_CODE_SHOW_ALL_TAGS;
import static com.example.notebook.KnowledgeBaseFragment.REQUEST_CODE_UPDATE_TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notebook.MainFragment;
import com.example.notebook.R;
import com.example.notebook.db.NotesDatabases;
import com.example.notebook.entities.Note;
import com.example.notebook.entities.Tag;
import com.example.notebook.adapters.TagAdapter;
import com.example.notebook.listeners.TagsListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;

public class CreateNewNoteActivity extends AppCompatActivity implements TagsListener {

    ImageButton btnBack, btnSave;
    EditText inputTitle, inputSubTitle, inputNoteText;
    TextView textDateTime;
    View viewOfSubTitleIndicator;
    AlertDialog deleteDialog;
    private List<Tag> tagList;
    private TagAdapter tagAdapter;
    private RecyclerView shortCutTagRecyclerView;
    private TextView tagNameTextView;
    private int clickPosition;
    private ImageView noteImage, noteImageDelete;
    public static final int REDIRECTION_CODE_ALL_NOTES = 1;
    public static final int REDIRECTION_CODE_KNOWLEDGE_BASE = 3;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 4;
    public static final int REQUEST_CODE_SELECT_IMAGE = 5;

    // Default values for colours
    private String noteIndicatorColor;
    private String selectImagePath;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_note);

        // Define initialisation note colours
        noteIndicatorColor = "#333333";
        selectImagePath = "";

        // Find a view by id
        inputTitle = findViewById(R.id.EditTextNoteTitle);
        inputSubTitle= findViewById(R.id.EditTextNoteSubTitle);
        inputNoteText = findViewById(R.id.EditTextInputNote);
        textDateTime = findViewById(R.id.textDateTime);
        viewOfSubTitleIndicator = findViewById(R.id.EditTextNoteSubTitleIndicator);
        tagNameTextView = findViewById(R.id.textTagName);

        noteImage = findViewById(R.id.noteImage);
        noteImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                noteImageDelete.setVisibility(View.VISIBLE);
                return true;
            }
        });

        noteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteImageDelete.setVisibility(View.GONE);
            }
        });

        noteImageDelete = findViewById(R.id.pictureDeleteButton);
        noteImageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteImage.setImageBitmap(null);
                selectImagePath = null;
                noteImageDelete.setVisibility(View.GONE);
                noteImage.setVisibility(View.GONE);
            }
        });

        // Set the tag name textview to be hidden when the tag name is empty
        if (tagNameTextView.getText().toString().isEmpty()) {
            tagNameTextView.setVisibility(View.GONE);
        } else {
            tagNameTextView.setVisibility(View.VISIBLE);
        }

        // Set the time display
        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );

        // Binding return button click events
        btnBack = findViewById(R.id.imageButtonBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When in a different fragment, return to the previous fragment
                if (getIntent().getBooleanExtra("return", true)) {
                    Intent intent = new Intent(CreateNewNoteActivity.this, MainActivity.class);
                    intent.putExtra("redirection", REDIRECTION_CODE_ALL_NOTES);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CreateNewNoteActivity.this, MainActivity.class);
                    intent.putExtra("redirection", REDIRECTION_CODE_KNOWLEDGE_BASE);
                    startActivity(intent);
                }
            }
        });

        // Binding to save keystroke events
        btnSave = findViewById(R.id.imageButtonSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        // Judgement when clicked on in the viewing or modifying state
        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            if (alreadyAvailableNote.getColor() != null){
                noteIndicatorColor = alreadyAvailableNote.getColor();
            }
            setViewOrChangeNote();
        }

        if (getIntent().getBooleanExtra("isFromQuickActions", false)){
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null){
                if (type.equals("image")) {
                    selectImagePath = getIntent().getStringExtra("imagePath");
                    noteImage.setImageBitmap(BitmapFactory.decodeFile(selectImagePath));
                    noteImage.setVisibility(View.VISIBLE);
                }
            }
        }

        // Initialise the shortcut bar inside onCreate
        initShortCutBar();
        setNoteIndicatorColor();
    }

    // Showing the contents of stored notes when viewing them
    private void setViewOrChangeNote(){
        inputTitle.setText(alreadyAvailableNote.getTitle());
        inputSubTitle.setText(alreadyAvailableNote.getSubtitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());
        tagNameTextView.setText(alreadyAvailableNote.getTag());

        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
            noteImage.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            noteImage.setVisibility(View.VISIBLE);
            selectImagePath = alreadyAvailableNote.getImagePath();
        }

        if (tagNameTextView.getText().toString().isEmpty()) {
            tagNameTextView.setVisibility(View.GONE);
        } else {
            tagNameTextView.setVisibility(View.VISIBLE);
        }
    }

    // Save entered notes
    private void saveNote() {
        // Check if there is content in the input box, the settings cannot all be empty
        if (inputTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note title can't be empty!!!", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputSubTitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);

        // Create a new note object for storage, storing the corresponding value
        final Note note = new Note();
        note.setTitle(inputTitle.getText().toString());
        note.setSubtitle(inputSubTitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setTag(tagNameTextView.getText().toString());
        note.setImagePath(selectImagePath);
        note.setColor(noteIndicatorColor);
        note.setUserID(userInfo.getInt("userID", 0));

        // Set the id of the note when it exists
        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
        }

        // Asynchronous storage of note information
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                // Putting new notes in the database
                NotesDatabases.getNotesDatabases(getApplicationContext()).noteDao().insertNote(note);
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
        new SaveNoteTask().execute();
    }

    // Initialising the bottom sidebar
    private void initShortCutBar() {
        final LinearLayout shortCutBarLayout = findViewById(R.id.shortcutLayout);
        // Use the bottom sheet to achieve the effect of a pop-up sidebar
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(shortCutBarLayout);
        shortCutBarLayout.findViewById(R.id.shortcutTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            // Set click events for the bottom sidebar
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        // Designing click events for the five colours
        final ImageView imageColor1 = shortCutBarLayout.findViewById(R.id.iconColor1);
        final ImageView imageColor2 = shortCutBarLayout.findViewById(R.id.iconColor2);
        final ImageView imageColor3 = shortCutBarLayout.findViewById(R.id.iconColor3);
        final ImageView imageColor4 = shortCutBarLayout.findViewById(R.id.iconColor4);
        final ImageView imageColor5 = shortCutBarLayout.findViewById(R.id.iconColor5);

        imageColor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteIndicatorColor = "#333333";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(R.drawable.ic_check);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteIndicatorColor();
            }
        });

        imageColor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteIndicatorColor = "#FDBE3B";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_check);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteIndicatorColor();
            }
        });

        imageColor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteIndicatorColor = "#FF4842";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_check);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteIndicatorColor();
            }
        });

        imageColor4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteIndicatorColor = "#3A52FC";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_check);
                imageColor5.setImageResource(0);
                setNoteIndicatorColor();
            }
        });

        imageColor5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteIndicatorColor = "#000000";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_check);
                setNoteIndicatorColor();
            }
        });

        shortCutBarLayout.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateNewNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });


        // Sync saved note colours with the colours in the shortcut bar
        if (alreadyAvailableNote != null  && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()) {
                case "#FDBE3B":
                    // Call the control's click event to change the colour in the shortcut bar to that of the note
                    shortCutBarLayout.findViewById(R.id.iconColor2).performClick();
                    break;
                case "#FF4842":
                    // Call the control's click event to change the colour in the shortcut bar to that of the note
                    shortCutBarLayout.findViewById(R.id.iconColor3).performClick();
                    break;
                case "#3A52FC":
                    // Call the control's click event to change the colour in the shortcut bar to that of the note
                    shortCutBarLayout.findViewById(R.id.iconColor4).performClick();
                    break;
                case "#000000":
                    // Call the control's click event to change the colour in the shortcut bar to that of the note
                    shortCutBarLayout.findViewById(R.id.iconColor5).performClick();
                    break;
            }
        }

        // Set the delete button to be visible when a note exists
        if (alreadyAvailableNote != null) {
            shortCutBarLayout.findViewById(R.id.layoutDelete).setVisibility(View.VISIBLE);
            shortCutBarLayout.findViewById(R.id.layoutDelete).setOnClickListener(new View.OnClickListener() {
                // Retract shortcut bar and pop-up window
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }

        // Set tag selection view
        shortCutTagRecyclerView = findViewById(R.id.shortCutTagRecyclerview);
        shortCutTagRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        tagList = new ArrayList<>();
        tagAdapter = new TagAdapter(tagList, this);
        shortCutTagRecyclerView.setAdapter(tagAdapter);
        getTags(REQUEST_CODE_SHOW_ALL_TAGS);
    }


    // Show delete pop-ups
    private void showDeleteNoteDialog() {
        if (deleteDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteDialog));
            builder.setView(view);
            // Create a pop-up window
            deleteDialog = builder.create();
            if (deleteDialog.getWindow() != null) {
                deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Asynchronous processing of deleted notes
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        // delete note
                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabases.getNotesDatabases(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        // Override method to add a delete mark
                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });

            // Set the click event for the cancel button
            view.findViewById(R.id.textCancelDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Close pop-ups
                    deleteDialog.dismiss();
                }
            });
        }

        deleteDialog.show();

    }

    private void getTags(final int requestCode) {
        // Get all tags asynchronously
        class GetTagsTask extends AsyncTask<Void, Void, List<Tag>> {

            @Override
            protected List<Tag> doInBackground(Void... voids) {
                SharedPreferences userInfo = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                return NotesDatabases.getNotesDatabases(getApplicationContext()).tagDao().getTagsByUserID(userInfo.getInt("userID", 0));
            }

            // Different operations according to different request codes
            @Override
            protected void onPostExecute(List<Tag> tags) {
                super.onPostExecute(tags);
                if (requestCode == REQUEST_CODE_SHOW_ALL_TAGS) {
                    tagList.addAll(tags);
                    tagAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_TAG) {
                    tagList.add(0, tags.get(0));
                    tagAdapter.notifyItemInserted(0);
                    shortCutTagRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_TAG) {
                    tagList.remove(clickPosition);
                    tagList.add(clickPosition, tags.get(clickPosition));
                    tagAdapter.notifyItemChanged(clickPosition);
                }

                if (tagList.size() != 0){
                    shortCutTagRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
        new GetTagsTask().execute();
    }

    // Set note colours
    private void setNoteIndicatorColor() {
        // Get the control's shape and change it
        GradientDrawable gradientDrawable = (GradientDrawable) viewOfSubTitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(noteIndicatorColor));
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
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        noteImage.setImageBitmap(bitmap);
                        noteImage.setVisibility(View.VISIBLE);

                        selectImagePath = getPathFromUri(selectedImageUri);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
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

    // Set tag click events
    @Override
    public void onTagClicked(Tag tag, int position) {
        clickPosition = position;
        tagNameTextView.setText(tagList.get(clickPosition).getName());
        tagNameTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTagLongClicked(Tag tag, int position) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}