package com.example.exchangefx.ui.chart;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exchangefx.R;
import com.example.exchangefx.ui.expense.ExpenseEditNav;
import com.example.exchangefx.ui.settings.Settings;
import com.example.exchangefx.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChartsNav extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts_nav);

        // ------------------------------
        // 하단 BottomNavigation 설정
        // ------------------------------
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // ------------------------------
            // 홈: 항상 MainActivity로 이동
            // ------------------------------
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            // ------------------------------
            // 추가 (3장)
            // ------------------------------
            if (id == R.id.nav_add) {
                startActivity(new Intent(this, ExpenseEditNav.class));
                return true;
            }

            // ------------------------------
            // 차트 (현재 화면 → 아무 동작 없음)
            // ------------------------------
            if (id == R.id.nav_charts) {
                return true;
            }

            // ------------------------------
            // 설정 (5장)
            // ------------------------------
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                return true;
            }

            return false;
        });

        // 현재 화면이 "차트"이므로 선택된 상태로 표시
        bottomNav.setSelectedItemId(R.id.nav_charts);
    }
}
