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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_add_list, container, false);

        // 예시: "+ 추가" 버튼
        Button addBtn = view.findViewById(R.id.btnAdd);
        if (addBtn != null) {
            addBtn.setOnClickListener(v -> {
                // Activity의 showExpenseAdd() 실행
                ((ExpenseEditNav) requireActivity()).showExpenseAdd();
            });
        }

        return view;
    }
}
