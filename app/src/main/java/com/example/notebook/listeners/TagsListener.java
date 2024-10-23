package com.example.notebook.listeners;

import com.example.notebook.entities.Tag;

public interface TagsListener {
    void onTagClicked(Tag tag, int position);
    void onTagLongClicked(Tag tag, int position);
}
