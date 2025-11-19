package com.example.exchangefx.ui.expense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.exchangefx.R;

public class ExpenseListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_expense_add_list, container, false);

        // ------------------------------
        // 1) 뒤로가기 버튼 (<)
        // ------------------------------
        Button backBtn = view.findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> {
            // 현재 3장 Activity 종료
            requireActivity().finish();
        });

        // ------------------------------
        // 2) "+ 새 지출" 버튼 → ExpenseAddFragment 이동
        // ------------------------------
        Button addBtn = view.findViewById(R.id.btnAdd);
        if (addBtn != null) {
            addBtn.setOnClickListener(v -> {
                ((ExpenseEditNav) requireActivity()).showExpenseAdd();
            });
        }

        return view;
    }
}
