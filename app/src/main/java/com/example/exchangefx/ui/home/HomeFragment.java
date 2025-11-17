package com.example.exchangefx.ui.home;

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
import com.example.exchangefx.ui.home.dummy.HomeDummy;
import com.example.exchangefx.ui.main.MainActivity;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // 1) 최근 지출 RecyclerView - 더미 데이터 연결
        RecyclerView rvRecent = v.findViewById(R.id.recycler_recent);
        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecent.setAdapter(new RecentExpenseAdapter(HomeDummy.recentNames()));

        // 2) "이번 달 총 지출" 카드 → 지출 내역 화면(Fragment)으로 이동
        v.findViewById(R.id.card_month_total).setOnClickListener(view -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).replace(new ExpenseList(), true);
            }
        });

        // 3) 날짜 섹션 더미 텍스트 (나중에 실제 기준 날짜로 교체하면 됨)
        TextView tvSectionDate = v.findViewById(R.id.tv_section_date);
        tvSectionDate.setText("2025. 11. 04");

        // TODO:
        //  - rg_fx_basis (오늘 환율 / 지출 시점 환율 토글) 동작
        //  - fab_quick_add 클릭 시 지출 추가 화면 이동
        //  는 DB/추가 화면 만들면서 연결하면 됨

        return v;
    }
}
