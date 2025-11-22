package com.example.exchangefx.ui.expense;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

import com.example.exchangefx.R;

public class ExpenseEditNav extends AppCompatActivity {

    // 원격 코드에서 가져온 상수 (추가 자동 열기)
    public static final String EXTRA_OPEN_ADD = "open_add";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_edit_nav);

        Intent intent = getIntent();

        // 원격 코드: 추가 모드 지정
        boolean openAdd = intent.getBooleanExtra(EXTRA_OPEN_ADD, false);

        // 너의 코드: 수정 모드 지정
        int editId = intent.getIntExtra("edit_id", -1);

        // 우선순위: 수정 모드 → 추가 모드 → 목록
        if (editId != -1) {
            showExpenseAddWithId(editId);   // 수정 모드
        } else if (openAdd) {
            showExpenseAdd();               // 추가 모드
        } else {
            showExpenseList();              // 기본 목록
        }
    }

    // ----------------------------------------------------
    // Fragment 1: 지출 목록 화면
    // ----------------------------------------------------
    public void showExpenseList() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ExpenseListFragment())
                .commit();
    }

    // ----------------------------------------------------
    // Fragment 2: 지출 추가 화면
    // ----------------------------------------------------
    public void showExpenseAdd() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ExpenseAddFragment())
                .addToBackStack(null)
                .commit();
    }

    // ----------------------------------------------------
    // Fragment 3: 지출 "수정" 화면
    // ----------------------------------------------------
    public void showExpenseAddWithId(int expenseId) {

        // 기존 Add 화면을 수정 모드로 호출하는 방식
        ExpenseAddFragment fragment = ExpenseAddFragment.newInstance(expenseId);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
