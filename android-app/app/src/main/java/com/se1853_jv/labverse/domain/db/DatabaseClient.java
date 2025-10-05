package com.se1853_jv.labverse.domain.db;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {
    // volatile: Dam bao thread-safe
    private static volatile DatabaseClient instance;
    private final AppDatabase appDB;

    private DatabaseClient(Context context) {
        appDB = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "labverse-db"
                )
                // Xóa toàn bộ bảng khi có lỗi migration
                .fallbackToDestructiveMigration(true) // xoa toan bo bang khi co loi
                .build();
    }

    public static DatabaseClient getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseClient.class) {
                if (instance == null) {
                    instance = new DatabaseClient(context);
                }
            }
        }
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDB;
    }
}
