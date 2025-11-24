package com.example.ReFX.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ReFX.data.dao.ExpenseDao2;
import com.example.ReFX.data.dao.FxRateCacheDao;
import com.example.ReFX.data.entity.Expense2;
import com.example.ReFX.data.entity.FxRateCache;

@Database(
        entities = {
                Expense2.class,
                FxRateCache.class
        },
        version = 5,              //  버전 최신 유지
        exportSchema = false
)
public abstract class AppDatabase2 extends RoomDatabase {

    // ------------------------
    // DAO
    // ------------------------
    public abstract ExpenseDao2 expenseDao2();
    public abstract FxRateCacheDao fxRateCacheDao();

    // ------------------------
    // Singleton Instance
    // ------------------------
    private static volatile AppDatabase2 INSTANCE;

    public static AppDatabase2 getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase2.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase2.class,
                                    "exchangefx.db"   // ★ DB 파일 이름
                            )
                            .fallbackToDestructiveMigration() // ★ 버전 변경 시 자동 초기화(충돌 해결)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
