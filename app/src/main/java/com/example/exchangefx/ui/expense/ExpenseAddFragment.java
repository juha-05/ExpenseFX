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

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExpenseAddFragment extends Fragment {

    private Spinner spinnerBase, spinnerTarget;
    private EditText etBaseAmount, etTargetAmount;
    private TextView tvDate, tvAppliedRate, tvRateTime;
    private Button btnApplyRate;

    private OkHttpClient client = new OkHttpClient();
    private double latestRate = 0.0;

    // API 호출용 날짜
    private String apiDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_expense_add, container, false);

        // ===================== UI 연결 =====================
        spinnerBase = v.findViewById(R.id.spinnerFrom);
        spinnerTarget = v.findViewById(R.id.spinnerTo);

        etBaseAmount = v.findViewById(R.id.etAmountFrom);
        etTargetAmount = v.findViewById(R.id.etAmountTo);

        tvDate = v.findViewById(R.id.etDate);
        tvAppliedRate = v.findViewById(R.id.tvRate);
        tvRateTime = v.findViewById(R.id.tvRateTime);

        btnApplyRate = v.findViewById(R.id.btn_apply_rate);

        // ===================== 기본 날짜 = 오늘 날짜 =====================
        setTodayDate();

        // ===================== 날짜 클릭 → 달력 열기 =====================
        tvDate.setOnClickListener(view -> showDatePicker());

        // ===================== 뒤로가기 =====================
        Button btnBack = v.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(view ->
                    ((ExpenseEditNav) requireActivity()).showExpenseList());
        }

        // ===================== 저장 버튼 =====================
        Button btnSave = v.findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(view ->
                    ((ExpenseEditNav) requireActivity()).showExpenseList());
        }

        // ===================== 스피너 변경 → 환율 새로 불러오기 =====================
        spinnerBase.setOnItemSelectedListener(spinnerListener);
        spinnerTarget.setOnItemSelectedListener(spinnerListener);

        // ===================== 환율 적용 버튼 =====================
        btnApplyRate.setOnClickListener(view -> applyRateToAmount());

        // 첫 로딩
        loadRateBySpinner();

        return v;
    }

    // ===================== 오늘 날짜 자동 설정 =====================
    private void setTodayDate() {
        Calendar cal = Calendar.getInstance();

        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);

        // 화면 표시용
        tvDate.setText(String.format("%04d. %02d. %02d", y, m, d));

        // API 용
        apiDate = String.format("%04d-%02d-%02d", y, m, d);
    }

    // ===================== 달력 띄우기 =====================
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {

                    // 화면용 날짜
                    tvDate.setText(
                            String.format("%04d. %02d. %02d",
                                    year, (month + 1), dayOfMonth)
                    );

                    // API 날짜
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

    // ===================== 스피너 리스너 =====================
    private final AdapterView.OnItemSelectedListener spinnerListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    loadRateBySpinner();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            };

    // ===================== 스피너 값 + 날짜 기준 환율 불러오기 =====================
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

    // ===================== Frankfurter API 호출 =====================
    private void fetchRate(String date, String base, String target) {

        String url = "https://api.frankfurter.app/"
                + date + "?from=" + base + "&to=" + target;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "환율 불러오기 실패 (인터넷)", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body().string();

                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject rates = obj.getJSONObject("rates");

                    latestRate = rates.getDouble(target);
                    String time = obj.getString("date");

                    requireActivity().runOnUiThread(() -> {
                        tvAppliedRate.setText("1 " + base + " = " + target + " "
                                + String.format("%,.4f", latestRate));
                        tvRateTime.setText(time + " 기준 환율");
                    });

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "환율 데이터 파싱 오류", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    // ===================== 금액 변환 =====================
    private void applyRateToAmount() {

        String amountStr = etBaseAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "금액을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latestRate == 0.0) {
            Toast.makeText(getContext(), "환율을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        double result = amount * latestRate;

        etTargetAmount.setText(String.format("%,.2f", result));
    }
}
