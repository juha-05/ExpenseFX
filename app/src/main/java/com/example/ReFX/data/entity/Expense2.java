package com.example.ReFX.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense")
public class Expense2 {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;           // 지출 항목 이름
    public String spendDate;      // YYYY-MM-DD (지출 날짜)
    public String fxDate;         // YYYY-MM-DD (환율 적용 날짜)

    public double baseAmount;     // 외화 금액
    public double targetAmount;   // 원화 환산 금액

    public String baseCurrency;   // 예: USD
    public String targetCurrency; // 예: KRW

    public String category;       // 카테고리
    public String memo;           // 메모


    // UI에서 선택 여부 관리용 (DB에 저장되지 않음)
    @Ignore
    public boolean isSelected = false;


    // Room에서 사용할 기본 생성자
    public Expense2() {}

    // new Expense2(...) 로 생성할 때 사용하는 생성자
    @Ignore
    public Expense2(String name,
                    String spendDate,
                    String fxDate,
                    double baseAmount,
                    double targetAmount,
                    String baseCurrency,
                    String targetCurrency,
                    String category,
                    String memo) {

        this.name = name;
        this.spendDate = spendDate;
        this.fxDate = fxDate;
        this.baseAmount = baseAmount;
        this.targetAmount = targetAmount;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.category = category;
        this.memo = memo;
    }

    // ------------------ Getter ------------------
    public String getName() {
        return name;
    }

    public String getMemo() {
        return memo;
    }
}
