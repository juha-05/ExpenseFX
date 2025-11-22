package com.example.exchangefx.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.exchangefx.R;
import com.example.exchangefx.data.db.AppDatabase2;
import com.example.exchangefx.data.entity.Expense2;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExpenseAddFragment extends Fragment {

    private Spinner spinnerBase, spinnerTarget, spinnerCategory;
    private EditText etBaseAmount, etTargetAmount, etName, etMemo;
    private TextView tvDate, tvAppliedRate, tvRateTime;
    private Button btnApplyRate;

    private OkHttpClient client = new OkHttpClient();
    private double latestRate = 0.0;

    private String apiDate = "";

    // 수정 모드인지 확인
    private int editingId = -1;  // -1 = 신규, 1 이상 = 수정
    private Expense2 editingData = null;


    // --------------------------------------------
    // newInstance : 수정 기능을 위한 ID 전달
    // --------------------------------------------
    public static ExpenseAddFragment newInstance(int expenseId) {
        ExpenseAddFragment fragment = new ExpenseAddFragment();
        Bundle args = new Bundle();
        args.putInt("expense_id", expenseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_expense_add, container, false);

        // UI 연결
        spinnerBase = v.findViewById(R.id.spinnerFrom);
        spinnerTarget = v.findViewById(R.id.spinnerTo);
        spinnerCategory = v.findViewById(R.id.spinnerCategory);

        etBaseAmount = v.findViewById(R.id.etAmountFrom);
        etTargetAmount = v.findViewById(R.id.etAmountTo);
        etName = v.findViewById(R.id.etName);
        etMemo = v.findViewById(R.id.etMemo);

        tvDate = v.findViewById(R.id.etDate);
        tvAppliedRate = v.findViewById(R.id.tvRate);
        tvRateTime = v.findViewById(R.id.tvRateTime);

        btnApplyRate = v.findViewById(R.id.btn_apply_rate);

        // 오늘 날짜 세팅
        setTodayDate();

        // 날짜 선택
        tvDate.setOnClickListener(view -> showDatePicker());

        // 뒤로가기
        Button btnBack = v.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            if (requireActivity() instanceof ExpenseEditNav) {
                ((ExpenseEditNav) requireActivity()).showExpenseList();
            } else {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // 저장 버튼
        Button btnSave = v.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> saveExpense());

        spinnerBase.setOnItemSelectedListener(spinnerListener);
        spinnerTarget.setOnItemSelectedListener(spinnerListener);
        btnApplyRate.setOnClickListener(view -> applyRateToAmount());

        // 환율 초기 로드
        loadRateBySpinner();

        // 수정 모드라면 데이터 불러오기
        if (getArguments() != null) {
            editingId = getArguments().getInt("expense_id", -1);
            if (editingId != -1) {
                loadExistingData(editingId);
            }
        }

        return v;
    }

    // ============================================
    // 오늘 날짜
    // ============================================
    private void setTodayDate() {
        Calendar cal = Calendar.getInstance();

        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);

        tvDate.setText(String.format("%04d. %02d. %02d", y, m, d));
        apiDate = String.format("%04d-%02d-%02d", y, m, d);
    }

    // ============================================
    // 날짜 선택
    // ============================================
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {

                    tvDate.setText(String.format("%04d. %02d. %02d",
                            year, (month + 1), dayOfMonth));

                    apiDate = String.format("%04d-%02d-%02d",
                            year, (month + 1), dayOfMonth);

                    loadRateBySpinner();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // ============================================
    // 수정 데이터 불러오기
    // ============================================
    private void loadExistingData(int id) {
        new Thread(() -> {
            editingData = AppDatabase2.getInstance(requireContext())
                    .expenseDao2()
                    .getExpenseById(id);

            if (editingData != null) {
                requireActivity().runOnUiThread(() -> fillFields());
            }

        }).start();
    }

    private void fillFields() {
        etName.setText(editingData.name);
        etMemo.setText(editingData.memo);

        etBaseAmount.setText(String.valueOf(editingData.baseAmount));
        etTargetAmount.setText(String.valueOf(editingData.targetAmount));

        tvDate.setText(editingData.spendDate);
        apiDate = editingData.fxDate;

        // 스피너 선택값 설정
        setSpinnerSelection(spinnerBase, editingData.baseCurrency);
        setSpinnerSelection(spinnerTarget, editingData.targetCurrency);
        setSpinnerSelection(spinnerCategory, editingData.category);

        loadRateBySpinner();
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    // =====================================================
    // 환율 불러오기
    // =====================================================
    private final AdapterView.OnItemSelectedListener spinnerListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    loadRateBySpinner();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            };

    private void loadRateBySpinner() {

        String base = spinnerBase.getSelectedItem().toString();
        String target = spinnerTarget.getSelectedItem().toString();

        if (base.equals(target)) {
            latestRate = 1.0;
            tvAppliedRate.setText("1 " + base + " = 1 " + target);
            tvRateTime.setText(apiDate + " 기준");
            return;
        }

        fetchRate(apiDate, base, target);
    }

    private void fetchRate(String date, String base, String target) {

        String url = "https://api.frankfurter.app/" + date +
                "?from=" + base + "&to=" + target;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "환율 불러오기 실패",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body().string();

                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject rates = obj.getJSONObject("rates");

                    latestRate = rates.getDouble(target);

                    requireActivity().runOnUiThread(() -> {
                        tvAppliedRate.setText(
                                "1 " + base + " = " + target + " " +
                                        String.format("%,.4f", latestRate)
                        );
                        tvRateTime.setText(date + " 기준");
                    });

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "환율 파싱 오류",
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    // ============================================
    // 금액 계산
    // ============================================
    private void applyRateToAmount() {

        if (etBaseAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "금액을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        double baseAmount = Double.parseDouble(etBaseAmount.getText().toString());
        double result = baseAmount * latestRate;

        etTargetAmount.setText(String.format("%,.2f", result));
    }

    // ============================================
    // 저장 버튼(신규 + 수정)
    // ============================================
    private void saveExpense() {

        String name = etName.getText().toString();
        String memo = etMemo.getText().toString();
        String spendDate = tvDate.getText().toString();
        String fxDate = apiDate;

        double baseAmount = Double.parseDouble(etBaseAmount.getText().toString());
        double targetAmount = baseAmount * latestRate;

        String baseCurrency = spinnerBase.getSelectedItem().toString();
        String targetCurrency = spinnerTarget.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        new Thread(() -> {

            AppDatabase2 db = AppDatabase2.getInstance(requireContext());

            if (editingId == -1) {
                // 신규 등록
                Expense2 newItem = new Expense2(
                        name, spendDate, fxDate,
                        baseAmount, targetAmount,
                        baseCurrency, targetCurrency,
                        category, memo
                );
                db.expenseDao2().insertExpense(newItem);

            } else {
                // 수정하기
                editingData.name = name;
                editingData.spendDate = spendDate;
                editingData.fxDate = fxDate;
                editingData.baseAmount = baseAmount;
                editingData.targetAmount = targetAmount;
                editingData.baseCurrency = baseCurrency;
                editingData.targetCurrency = targetCurrency;
                editingData.category = category;
                editingData.memo = memo;

                db.expenseDao2().updateExpense(editingData);
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "저장 완료!", Toast.LENGTH_SHORT).show();
                ((ExpenseEditNav) requireActivity()).showExpenseList();
            });

        }).start();
    }
}
