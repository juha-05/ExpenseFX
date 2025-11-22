package com.example.exchangefx.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "fx_rate_cache")
public class FxRateCache {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // YYYY-MM-DD
    @NonNull
    public String fxDate;

    // 베이스 통화 (예: USD)
    @NonNull
    public String baseCurrency;

    // 타겟 통화 (예: KRW)
    @NonNull
    public String targetCurrency;

    // 1 USD = 1444.00 KRW 형태의 환율 숫자값
    public double rate;
}
