package com.example.ReFX.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fx_rate_cache")
public class FxRateCache {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String fxDate; //환율기준날짜

    @NonNull
    public String baseCurrency; //베이스통화(달러 등)

    @NonNull
    public String targetCurrency; //타겟통화(원 등)

    public double rate; //환율정보 (1달러=1444.00원)

    public FxRateCache(@NonNull String fxDate,
                       @NonNull String baseCurrency,
                       @NonNull String targetCurrency,
                       double rate) {
        this.fxDate = fxDate;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }
}
