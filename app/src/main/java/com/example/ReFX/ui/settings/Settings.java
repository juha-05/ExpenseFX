package com.example.ReFX.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ReFX.R;
import com.example.ReFX.ui.chart.ChartsNav;
import com.example.ReFX.ui.expense.ExpenseEditNav;
import com.example.ReFX.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // ------------------------------
        // 하단 BottomNavigation 설정
        // ------------------------------
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // ------------------------------
            // 홈: 무조건 Home(MainActivity)로 이동
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
            // 차트 (4장)
            // ------------------------------
            if (id == R.id.nav_charts) {
                startActivity(new Intent(this, ChartsNav.class));
                return true;
            }

            // ------------------------------
            // 설정: 현재 화면 → 아무 동작 없음
            // ------------------------------
            if (id == R.id.nav_settings) {
                return true;
            }

            return false;
        });

        // 현재 선택된 메뉴를 설정으로 표시
        bottomNav.setSelectedItemId(R.id.nav_settings);
    }
}
