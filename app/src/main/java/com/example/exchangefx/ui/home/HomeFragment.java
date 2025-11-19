package com.example.exchangefx.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangefx.R;
import com.example.exchangefx.ui.expense.ExpenseList;
import com.example.exchangefx.ui.expense.ExpenseAddFragment;
import com.example.exchangefx.ui.expense.ExpenseEditNav;
import com.example.exchangefx.ui.chart.ChartsNav;
import com.example.exchangefx.ui.settings.Settings;
import com.example.exchangefx.ui.home.dummy.HomeDummy;
import com.example.exchangefx.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);


        // 1) 최근 지출 RecyclerView 설정

        RecyclerView rvRecent = v.findViewById(R.id.recycler_recent);
        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecent.setAdapter(new RecentExpenseAdapter(HomeDummy.recentNames()));


        // 2) "이번 달 총 지출" 카드 → 지출 내역 Fragment 이동
        v.findViewById(R.id.card_month_total).setOnClickListener(view -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).replace(new ExpenseList(), true);
            }
        });


        // 3) 날짜 섹션 더미 텍스트
        TextView tvSectionDate = v.findViewById(R.id.tv_section_date);
        tvSectionDate.setText("2025. 11. 04");


        // 4) (+ 버튼) → ExpenseAddFragment 로 이동
        v.findViewById(R.id.fab_quick_add).setOnClickListener(btn -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity())
                        .replace(new ExpenseAddFragment(), true);
            }
        });


        // 5) BottomNavigation → Activity 연결
        BottomNavigationView bottomNav = v.findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // 홈 → 현재 화면 유지
                return true;

            } else if (id == R.id.nav_add) {
                // “추가” 탭 누를 때는 3장 Activity로 이동 유지
                startActivity(new Intent(requireActivity(), ExpenseEditNav.class));
                return true;

            } else if (id == R.id.nav_charts) {
                startActivity(new Intent(requireActivity(), ChartsNav.class));
                return true;

            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(requireActivity(), Settings.class));
                return true;
            }

            return false;
        });

        return v;
    }
}
