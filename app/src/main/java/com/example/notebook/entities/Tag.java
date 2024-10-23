package com.example.notebook.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "tags")
// Objects can be serialised
public class Tag implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "color")
    private String color;

    @ColumnInfo(name = "noteNumber")
    private Integer notNumber;

    @ColumnInfo(name = "userID")
    private Integer userID;

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getNotNumber() {
        return notNumber;
    }

    public void setNotNumber(Integer notNumber) {
        this.notNumber = notNumber;
    }

    @Override
    public String toString() {
        return name + ":" + color;
    }
}