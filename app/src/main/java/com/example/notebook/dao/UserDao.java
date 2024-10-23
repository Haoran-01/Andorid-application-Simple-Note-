package com.example.notebook.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.notebook.entities.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM user ORDER BY id DESC")
    List<User> getAllUsers();

    @Query("SELECT * FROM user WHERE userEmail = :userEmail")
    List<User> getUserByEmail (String userEmail);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Delete
    void deleteUser(User user);

    @Update
    void updateUser(User user);
}
