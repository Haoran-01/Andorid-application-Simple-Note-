package com.example.notebook.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notebook.R;
import com.example.notebook.entities.Note;
import com.example.notebook.entities.Tag;
import com.example.notebook.listeners.NotesListener;
import com.example.notebook.listeners.TagsListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private List<Tag> tagList;
    private TagsListener tagsListener;
    private Timer timer;
    private List<Note> noteResource;

    public TagAdapter(List<Tag> tagList, TagsListener tagsListener){
        this.tagList = tagList;
        this.tagsListener = tagsListener;
    }


    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TagViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setTag(tagList.get(position));
        // Designing click events
        holder.tagLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagsListener.onTagClicked(tagList.get(position), position);
            }
        });
        holder.tagLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                tagsListener.onTagLongClicked(tagList.get(position), position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        ImageView tagImage;
        TextView tagName;
        LinearLayout tagLayout;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tagImage = itemView.findViewById(R.id.tagItemImage);
            tagName = itemView.findViewById(R.id.tagItemText);
            tagLayout = itemView.findViewById(R.id.layoutTagItem);
        }

        // Set tag style
        void setTag(Tag tag) {
            tagName.setText(tag.getName());
            GradientDrawable gradientDrawable = (GradientDrawable) tagLayout.getBackground();
            if (tag.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(tag.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
        }
    }
}
