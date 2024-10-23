package com.example.notebook.listeners;

import com.example.notebook.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
