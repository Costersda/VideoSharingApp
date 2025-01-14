package com.example.videosharingapp.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {VideoInfo.class, ChannelInfo.class},
        version = 2,
        exportSchema = false
)

public abstract class YouTubeDatabase extends RoomDatabase {
    private static YouTubeDatabase database;
    public abstract VideoDoa getVideoDatabaseDao();
    public abstract ChannelDoa getChannelDatabaseDao();

    public static synchronized YouTubeDatabase getDBInstance(Context context){
        if (database == null){
            database = Room.databaseBuilder(
                            context.getApplicationContext(),
                            YouTubeDatabase.class,
                            "YTDatabase"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return database;
    }
}
