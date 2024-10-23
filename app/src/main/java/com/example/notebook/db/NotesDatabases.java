package com.example.notebook.db;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.notebook.dao.NoteDao;
import com.example.notebook.dao.TagDao;
import com.example.notebook.dao.UserDao;
import com.example.notebook.entities.Note;
import com.example.notebook.entities.Tag;
import com.example.notebook.entities.User;

// room database class
@Database(entities = {Note.class, Tag.class, User.class}, version = 5, exportSchema = false)
public abstract class NotesDatabases extends RoomDatabase{

    private static NotesDatabases notesDatabases;

    public static synchronized NotesDatabases getNotesDatabases(Context context) {
        if (notesDatabases == null) {
            notesDatabases = Room.databaseBuilder(
                    context,
                    NotesDatabases.class,
                    "notes_db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build();
        }
        return notesDatabases;
    }

    public abstract NoteDao noteDao();
    public abstract TagDao tagDao();
    public abstract UserDao userDao();

    // Database version update

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "`name` TEXT, `color` TEXT, `noteNumber` INTEGER)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN tag TEXT DEFAULT NULL");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `user` (`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "`userName` TEXT, `userEmail` TEXT, `userPassword` TEXT, `userProfile_path` TEXT)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN userID INTEGER DEFAULT NULL");
            database.execSQL("ALTER TABLE tags ADD COLUMN userID INTEGER DEFAULT NULL");
        }
    };
}


