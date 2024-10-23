package com.example.notebook.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.notebook.entities.Tag;

import java.util.List;

@Dao
public interface TagDao {

    @Query("SELECT * FROM tags ORDER BY id DESC")
    List<Tag> getAllTags();

    @Query("SELECT * FROM tags WHERE userID =:userID")
    List<Tag> getTagsByUserID (int userID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTag(Tag tag);

    @Delete
    void deleteTag(Tag tag);
}
