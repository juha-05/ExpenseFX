package com.example.ReFX.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ReFX.R;
import com.example.ReFX.data.db.AppDatabase2;
import com.example.ReFX.data.entity.Expense2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class ExpenseListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<Expense2> expenseList = new ArrayList<>();

    private CheckBox cbSelectAll;
    private Button btnDate;
    private Button btnDelete;
    private Button btnModify;

    private String selectedDate;     // YYYY-MM-DD

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_expense_add_list, container, false);

        // ---------------- RecyclerView ----------------
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ExpenseListAdapter(expenseList);
        recyclerView.setAdapter(adapter);

        // ---------------- 날짜 버튼 ----------------
        btnDate = view.findViewById(R.id.btnDate);

        selectedDate = getTodayDate();
        btnDate.setText(selectedDate + " ▼");

        btnDate.setOnClickListener(v -> showDatePicker());

        // ---------------- 전체 선택 체크박스 ----------------
        cbSelectAll = view.findViewById(R.id.rbSelectAll);
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
        });

        // ---------------- 삭제 버튼 ----------------
        btnDelete = view.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> deleteSelectedExpenses());

        // ---------------- 수정 버튼 ----------------
        btnModify = view.findViewById(R.id.btnModify);
        btnModify.setOnClickListener(v -> modifySelectedExpense());

        // ---------------- DB 로드 ----------------
        loadExpensesByDate(selectedDate);

        // ---------------- 뒤로가기 ----------------
        Button backBtn = view.findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> requireActivity().finish());

        // ---------------- 새 지출 ----------------
        Button addBtn = view.findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(v -> {
            ((ExpenseEditNav) requireActivity()).showExpenseAdd();
        });

        return view;
    }

    // ---------------- 오늘 날짜 YYYY-MM-DD ----------------
    private String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        return String.format("%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    // ---------------- YYYY-MM-DD → YYYY. MM. DD 변환 ----------------
    private String convertToSpendDateFormat(String ymd) {
        String[] p = ymd.split("-");
        return String.format("%s. %s. %s", p[0], p[1], p[2]);
    }

    // ---------------- 날짜 선택 ----------------
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {

                    selectedDate = String.format("%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);

                    btnDate.setText(selectedDate + " ▼");

                    loadExpensesByDate(selectedDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // ---------------- 해당 날짜의 지출 목록 로드 ----------------
    private void loadExpensesByDate(String ymd) {

        String converted = convertToSpendDateFormat(ymd);

        new Thread(() -> {

            List<Expense2> list =
                    AppDatabase2.getInstance(requireContext())
                            .expenseDao2()
                            .getExpensesByDate(converted);

            requireActivity().runOnUiThread(() -> {
                adapter.setItems(list);
                cbSelectAll.setChecked(false);
            });

        }).start();
    }

    // ---------------- 선택된 지출 삭제 ----------------
    private void deleteSelectedExpenses() {

        new Thread(() -> {

            for (int id : adapter.getSelectedIds()) {
                AppDatabase2.getInstance(requireContext())
                        .expenseDao2()
                        .deleteById(id);
            }

            loadExpensesByDate(selectedDate);

        }).start();
    }

    // ---------------- 선택된 지출 수정 ----------------
    private void modifySelectedExpense() {

        Set<Integer> selected = adapter.getSelectedIds();

        if (selected.size() == 0) {
            Toast.makeText(getContext(), "수정할 항목을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selected.size() > 1) {
            Toast.makeText(getContext(), "수정은 1개의 항목만 선택해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 선택된 ID 추출
        int expenseId = selected.iterator().next();

        // 수정 화면(= 기존 추가 화면)을 수정 모드로 열기
        ((ExpenseEditNav) requireActivity()).showExpenseAddWithId(expenseId);
    }
}
