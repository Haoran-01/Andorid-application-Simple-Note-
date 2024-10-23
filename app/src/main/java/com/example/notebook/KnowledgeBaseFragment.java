package com.example.notebook;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.notebook.activities.CreateNewNoteActivity;
import com.example.notebook.adapters.NoteAdapter;
import com.example.notebook.adapters.TagAdapter;
import com.example.notebook.db.NotesDatabases;
import com.example.notebook.entities.Note;
import com.example.notebook.entities.Tag;
import com.example.notebook.listeners.NotesListener;
import com.example.notebook.listeners.TagsListener;

import java.util.ArrayList;
import java.util.List;


public class KnowledgeBaseFragment extends Fragment implements TagsListener, NotesListener {

    public static final int REQUEST_CODE_ADD_TAG = 1;
    public static final int REQUEST_CODE_UPDATE_TAG = 2;
    public static final int REQUEST_CODE_SHOW_ALL_TAGS = 3;
    public static final int REQUEST_CODE_DELETE_TAG = 4;

    public static final int REQUEST_CODE_UPDATE_NOTE = 5;
    public static final int REQUEST_CODE_SHOW_ALL_NOTES = 6;

    ImageButton addTag;

    private View root;
    private View addTagView;
    private AlertDialog addTagDialog;
    private EditText inputTagName;
    private View tagImage;
    private RecyclerView tagRecyclerView, getTagRecyclerView;
    private List<Tag> tagList;
    private List<Note> noteList;
    private TagAdapter tagAdapter;
    private NoteAdapter noteAdapter;
    private int clickPosition;
    private String tagIndicatorColor;
    private Tag alreadyAvailableTag;
    private String searchTagName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_knowledge_base, container, false);
        }

        if (addTagView == null) {
            addTagView = inflater.inflate(R.layout.layout_add_tag, container, false);
        }

        tagIndicatorColor = "#333333";

        // Find the control that corresponds to the view
        inputTagName = addTagView.findViewById(R.id.inputTagName);
        tagImage = root.findViewById(R.id.tagIndicator);

        // Bind the click event for adding a Tag
        addTag = root.findViewById(R.id.addTagImageButton);
        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTagDialog();
            }
        });

        // set tag recycler view
        tagRecyclerView = root.findViewById(R.id.tagRecyclerview);
        tagRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        tagList = new ArrayList<>();
        tagAdapter = new TagAdapter(tagList, this);
        tagRecyclerView.setAdapter(tagAdapter);

        getTags(REQUEST_CODE_SHOW_ALL_TAGS);

        // Get a recycle view of all notes displaying the same tag
        getTagRecyclerView = root.findViewById(R.id.tagNoteRecyclerview);
        getTagRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        getTagRecyclerView.setAdapter(noteAdapter);

        getNotes(REQUEST_CODE_SHOW_ALL_NOTES, false);
        return root;
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
                    // Add notes
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(clickPosition);
                    if (isNoteDelete) {
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

    private void showAddTagDialog() {
        // Binding pop-up layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_add_tag, (ViewGroup) root.findViewById(R.id.layoutAddTagDialog));
        builder.setView(view);
        // Create a pop-up window
        addTagDialog = builder.create();
        tagImage = view.findViewById(R.id.tagIndicator);

        if (addTagDialog.getWindow() != null) {
            addTagDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        // set click event for delete button
        if (alreadyAvailableTag != null) {
            ((EditText)view.findViewById(R.id.inputTagName)).setText(alreadyAvailableTag.getName());
            view.findViewById(R.id.textDeleteTag).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textDeleteTag).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    class DeleteTagTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabases.getNotesDatabases(getActivity().getApplicationContext()).tagDao().deleteTag(alreadyAvailableTag);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                        }
                    }
                    getTags(REQUEST_CODE_DELETE_TAG);
                    addTagDialog.dismiss();
                    new DeleteTagTask().execute();
                }
            });
        }

        // set click event for add tag button
        view.findViewById(R.id.textAddTag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputTagName = addTagDialog.findViewById(R.id.inputTagName);
                saveTag();
                if (alreadyAvailableTag != null){
                    getTags(REQUEST_CODE_UPDATE_TAG);
                } else {
                    getTags(REQUEST_CODE_ADD_TAG);
                }
                addTagDialog.dismiss();
            }
        });

        // set click event for cancel button
        view.findViewById(R.id.textCancelAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTagDialog.dismiss();
            }
        });

        // Designing click events for the five colours
        final ImageView imageColor1 = view.findViewById(R.id.iconColor1);
        final ImageView imageColor2 = view.findViewById(R.id.iconColor2);
        final ImageView imageColor3 = view.findViewById(R.id.iconColor3);
        final ImageView imageColor4 = view.findViewById(R.id.iconColor4);
        final ImageView imageColor5 = view.findViewById(R.id.iconColor5);

        imageColor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagIndicatorColor = "#333333";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(R.drawable.ic_check);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setTagIndicatorColor();
            }
        });

        imageColor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagIndicatorColor = "#FDBE3B";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_check);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setTagIndicatorColor();
            }
        });

        imageColor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagIndicatorColor = "#FF4842";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_check);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setTagIndicatorColor();
            }
        });

        imageColor4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagIndicatorColor = "#3A52FC";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_check);
                imageColor5.setImageResource(0);
                setTagIndicatorColor();
            }
        });

        imageColor5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagIndicatorColor = "#000000";
                // Add a check icon to the click button and update the note colour
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_check);
                setTagIndicatorColor();
            }
        });

        // Sync saved note colours with the colours in the shortcut bar
        if (alreadyAvailableTag != null  && alreadyAvailableTag.getColor() != null && !alreadyAvailableTag.getColor().trim().isEmpty()){
            switch (alreadyAvailableTag.getColor()) {
                case "#FDBE3B":
                    // Call the control's click event to change the colour in the shortcut bar to that of the note
                    view.findViewById(R.id.iconColor2).performClick();
                    break;
                case "#FF4842":
                    // Call the control's click event to change the colour in the shortcut bar to that of the note
                    view.findViewById(R.id.iconColor3).performClick();
                    break;
                case "#3A52FC":
                    // Call the control's click event to change the colour in the shortcut bar to that of the note
                    view.findViewById(R.id.iconColor4).performClick();
                    break;
                case "#000000":
                    // Call the control's click event to change the colour in the shortcut bar to that of the note
                    view.findViewById(R.id.iconColor5).performClick();
                    break;
            }
        }

        addTagDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                alreadyAvailableTag = null;
            }
        });
        addTagDialog.show();
    }

    // Set note colours
    private void setTagIndicatorColor() {
        // Get the control's shape and change it
        GradientDrawable gradientDrawable = (GradientDrawable) tagImage.getBackground();
        System.out.println(tagIndicatorColor);
        System.out.println(tagImage);
        System.out.println(gradientDrawable);
        gradientDrawable.setColor(Color.parseColor(tagIndicatorColor));
    }


    private void saveTag() {
        // Determine if the input box is empty
        if (inputTagName.getText().toString().trim().isEmpty()) {
            Toast.makeText(root.getContext(), "Tag Name can't be empty!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences userInfo = root.getContext().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        // Create tag objects for storage
        final Tag tag = new Tag();
        tag.setName(inputTagName.getText().toString());
        tag.setColor(tagIndicatorColor);
        tag.setUserID(userInfo.getInt("userID",0));

        if (alreadyAvailableTag != null) {
            tag.setId(alreadyAvailableTag.getId());
        }

        class SaveTagTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabases.getNotesDatabases(getActivity().getApplicationContext()).tagDao().insertTag(tag);
                return null;
            }
        }
        new SaveTagTask().execute();
    }

    private void getTags(final int requestCode) {
        // Get all tags asynchronously
        class GetTagsTask extends AsyncTask<Void, Void, List<Tag>> {

            @Override
            protected List<Tag> doInBackground(Void... voids) {
                SharedPreferences userInfo = root.getContext().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                return NotesDatabases.getNotesDatabases(getActivity()).tagDao().getTagsByUserID(userInfo.getInt("userID", 0));
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
                    tagRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_TAG) {
                    tagList.remove(clickPosition);
                    tagList.add(clickPosition, tags.get(clickPosition));
                    tagAdapter.notifyItemChanged(clickPosition);
                } else if (requestCode == REQUEST_CODE_DELETE_TAG) {
                    tagList.remove(clickPosition);
                    tagAdapter.notifyItemRemoved(clickPosition);
                }
            }
        }
        new GetTagsTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_TAG) {
            getTags(REQUEST_CODE_ADD_TAG);
        }
    }

    // Set the click event of a clicked tag item
    @Override
    public void onTagClicked(Tag tag, int position) {
        clickPosition = position;
        alreadyAvailableTag = tagList.get(clickPosition);
        searchTagName = alreadyAvailableTag.getName();
        noteAdapter.searchNotesByTag(searchTagName);
    }

    //Set the long click event of a clicked tag item
    @Override
    public void onTagLongClicked(Tag tag, int position) {
        clickPosition = position;
        alreadyAvailableTag = tagList.get(clickPosition);
        showAddTagDialog();
    }

    //Set the click event of a clicked note item
    @Override
    public void onNoteClicked(Note note, int position) {
        clickPosition = position;
        Intent intent = new Intent(getActivity(), CreateNewNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        intent.putExtra("return", false);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }
}