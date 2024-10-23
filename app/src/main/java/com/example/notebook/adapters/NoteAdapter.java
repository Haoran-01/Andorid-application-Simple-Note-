package com.example.notebook.adapters;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notebook.R;
import com.example.notebook.entities.Note;
import com.example.notebook.listeners.NotesListener;
import com.ldf.calendar.model.CalendarDate;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{

    private List<Note> noteList;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> noteResource;


    // Set up a list of notes and the corresponding note listener
    public NoteAdapter(List<Note> noteList, NotesListener notesListener) {
        this.noteList = noteList;
        this.notesListener = notesListener;
        noteResource = noteList;
    }

    @NonNull
    @Override
    // Bind the corresponding item background
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setNote(noteList.get(position));
        // Designing click events
        holder.noteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClicked(noteList.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, itemSubTitle, itemDateTime, itemTag;
        LinearLayout noteLayout;
        private RoundedImageView noteImage;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemSubTitle = itemView.findViewById(R.id.itemSubItem);
            itemDateTime = itemView.findViewById(R.id.itemDateTime);
            noteLayout = itemView.findViewById(R.id.layoutNoteItem);
            noteImage = itemView.findViewById(R.id.noteImage);
        }

        void setNote(Note note) {
            itemTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                itemSubTitle.setVisibility(View.GONE);
            } else {
                itemSubTitle.setText(note.getSubtitle());
            }
            itemDateTime.setText(note.getDateTime());
            GradientDrawable gradientDrawable = (GradientDrawable) noteLayout.getBackground();
            if (note.getColor() != null) {
                // Notes have set colours
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                // Colours not set in the notes are set as default colours
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            // 在列表中展示图片
            if (note.getImagePath() != null){
                noteImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                noteImage.setVisibility(View.VISIBLE);
            } else {
                noteImage.setVisibility(View.GONE);
            }

        }
    }

    // search note function
    public void searchNotes(final String searchKeyword) {
        // Set a timer
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    noteList = noteResource;
                } else {
                    // Create a temporary note list to store the query results
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : noteResource) {
                        // Iterate through all notes and filter out all notes with titles, sub-titles and content containing the search terms
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    noteList = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    // Show all search results
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    // search note for same tag function
    public void searchNotesByTag(final String tagName) {
        // Set a timer
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (tagName.equals(null)) {
                    noteList = noteResource;
                } else {
                    // Create a temporary note list to store the query results
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : noteResource) {
                        // Iterate through all notes and filter out all notes with the specified tag
                        if (note.getTag() != null && note.getTag().toLowerCase().contains(tagName.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    noteList = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    // Show all search results
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void searchNotesByTime(final CalendarDate noteTime) {
        // Set a timer

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    String chooseTime;
                    if (noteTime.equals(null)) {
                        noteList = noteResource;
                    } else {
                        if (noteTime.day > 10 && noteTime.month > 10){
                            chooseTime = noteTime.year + "-" + noteTime.month + "-" + noteTime.day;
                        } else if (noteTime.day < 10){
                            chooseTime = noteTime.year + "-" + noteTime.month + "-0" + noteTime.day;
                        } else if (noteTime.month < 10){
                            chooseTime = noteTime.year + "-0" + noteTime.month + "-" + noteTime.day;
                        } else {
                            chooseTime = noteTime.year + "-0" + noteTime.month + "-0" + noteTime.day;
                        }

                        DateFormat fmt = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a");
                        DateFormat fmt2 = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        // Create a temporary note list to store the query results
                        ArrayList<Note> temp = new ArrayList<>();
                        for (Note note : noteResource) {
                            // Iterate through all notes and filter out all notes with the specified tag
                            Date date2 = fmt.parse(note.getDateTime());
                            Date date = fmt2.parse(chooseTime);
                            if (simpleDateFormat.format(date).equals(simpleDateFormat.format(date2))){
                                temp.add(note);
                            }
                        }
                        noteList = temp;
                    }
                } catch (Exception exception){
                    exception.printStackTrace();
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    // Show all search results
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }


    public void cancelTimer() {
        if (timer != null){
            timer.cancel();
        }
    }
}
