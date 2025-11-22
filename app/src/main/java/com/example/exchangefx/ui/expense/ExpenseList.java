package com.example.exchangefx.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangefx.R;
import com.example.exchangefx.data.dao.ExpenseDao2;
import com.example.exchangefx.data.db.AppDatabase2;
import com.example.exchangefx.data.entity.Expense2;
import com.example.exchangefx.utils.FrankfurterCall;

import org.json.JSONException;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseList extends Fragment {

    // 오늘 환율 true / 지출 시점 false
    private boolean useToday = true;

    private int selectedYear;
    private int selectedMonth; // 1~12

    private TextView tvTotalAmount;
    private TextView tvMonthYear;
    private Button btnTodayRate;
    private Button btnTransactionRate;
    private RecyclerView rvExpenseList;

    private ExpenseDao2 expenseDao;
    private FrankfurterCall fxClient;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private RowAdapter rowAdapter;

    public ExpenseList() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_expense_list, container, false);

        // 상단 뒤로가기
        v.findViewById(R.id.btn_back).setOnClickListener(view -> requireActivity().onBackPressed());

        tvTotalAmount      = v.findViewById(R.id.tv_total_expense_list_amount);
        tvMonthYear        = v.findViewById(R.id.tv_month_year_selector);
        btnTodayRate       = v.findViewById(R.id.btn_today_rate_list);
        btnTransactionRate = v.findViewById(R.id.btn_transaction_rate_list);
        rvExpenseList      = v.findViewById(R.id.rv_expense_list);

        expenseDao = AppDatabase2.getInstance(requireContext()).expenseDao2();
        fxClient   = new FrankfurterCall();

        // 오늘 날짜 기준 초기화
        Calendar cal = Calendar.getInstance();
        selectedYear  = cal.get(Calendar.YEAR);
        selectedMonth = cal.get(Calendar.MONTH) + 1;

        tvMonthYear.setText(String.format(Locale.KOREA, "%04d. %02d ▾", selectedYear, selectedMonth));

        tvMonthYear.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (picker, year, month, day) -> {
                        selectedYear  = year;
                        selectedMonth = month + 1;
                        tvMonthYear.setText(String.format(Locale.KOREA, "%04d. %02d ▾", year, month + 1));
                        recalcAndRender();
                    },
                    selectedYear,
                    selectedMonth - 1,
                    1
            );
            dialog.show();
        });

        // 환율 기준 버튼
        btnTodayRate.setOnClickListener(view -> {
            useToday = true;
            updateRateToggleUI();
            recalcAndRender();
        });

        btnTransactionRate.setOnClickListener(view -> {
            useToday = false;
            updateRateToggleUI();
            recalcAndRender();
        });

        // 리스트
        rowAdapter = new RowAdapter();
        rvExpenseList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpenseList.setAdapter(rowAdapter);

        updateRateToggleUI();
        recalcAndRender();

        return v;
    }

    /** 버튼 UI 변경 */
    private void updateRateToggleUI() {
        btnTodayRate.setSelected(useToday);
        btnTransactionRate.setSelected(!useToday);
    }

    /**
     * 핵심 로직:
     * 1) DB 전체 읽기
     * 2) 월·연도 기준으로 필터링
     * 3) Today vs Spend 기준 금액 계산
     * 4) 총합·리스트 표시
     */
    private void recalcAndRender() {
        ioExecutor.execute(() -> {

            List<Expense2> all = expenseDao.getAllExpenses();  // id DESC

            List<RowItem> rows = new ArrayList<>();
            double totalAmount = 0.0;

            for (Expense2 e : all) {

                // ① spendDate(YYYY. MM. DD)에서 연·월 추출
                if (e.spendDate == null || e.spendDate.length() < 10) continue;

                int year  = Integer.parseInt(e.spendDate.substring(0, 4));
                int month = Integer.parseInt(e.spendDate.substring(6, 8));

                if (year != selectedYear || month != selectedMonth) {
                    continue;
                }

                // ② Today / AtSpend 기준 금액 계산
                double mainAmount;

                if (useToday) {
                    // 최신 환율 API 호출
                    double rate = 1.0;

                    if (!"KRW".equalsIgnoreCase(e.baseCurrency)) {
                        try {
                            rate = fxClient.getRateToKrw("latest", e.baseCurrency);
                        } catch (IOException | JSONException ex) {
                            ex.printStackTrace();
                            continue;
                        }
                    }
                    mainAmount = e.baseAmount * rate;

                } else {
                    // 지출 시점 금액 = 저장된 targetAmount 그대로
                    mainAmount = e.targetAmount;
                }

                totalAmount += mainAmount;

                // 환차익/환차손 표시용
                double diff = 0.0;
                try {
                    if (!"KRW".equalsIgnoreCase(e.baseCurrency)) {
                        double todayRate = fxClient.getRateToKrw("latest", e.baseCurrency);
                        double atSpendRate = fxClient.getRateToKrw(e.fxDate, e.baseCurrency);
                        diff = (e.baseAmount * todayRate) - (e.baseAmount * atSpendRate);
                    }
                } catch (Exception ignored) {}

                String diffLabel = formatDiff(diff);

                rows.add(new RowItem(
                        e.spendDate,                       // 날짜
                        (e.memo != null && !e.memo.isEmpty()) ? e.memo : e.category, // 이름
                        formatAmount(mainAmount),          // 메인 금액
                        diffLabel                          // 환차익/환차손
                ));
            }

            double finalTotal = totalAmount;
            List<RowItem> finalRows = rows;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvTotalAmount.setText(formatAmount(finalTotal));
                    rowAdapter.setItems(finalRows);
                });
            }
        });
    }

    private String formatAmount(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);
        return "₩ " + nf.format(amount);
    }

    private String formatDiff(double diff) {
        if (Math.abs(diff) < 0.005) return "";

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        nf.setMaximumFractionDigits(2);

        if (diff > 0) return "+ ₩ " + nf.format(diff) + " (환차익)";
        else return "- ₩ " + nf.format(-diff) + " (환차손)";
    }

    // =========================
    // RecyclerView 내부 클래스
    // =========================

    private static class RowItem {
        final String date;
        final String name;
        final String amount;
        final String diff;

        RowItem(String date, String name, String amount, String diff) {
            this.date = date;
            this.name = name;
            this.amount = amount;
            this.diff = diff;
        }
    }

    private static class RowAdapter extends RecyclerView.Adapter<RowAdapter.VH> {

        private final List<RowItem> data = new ArrayList<>();

        void setItems(List<RowItem> newItems) {
            data.clear();
            if (newItems != null) data.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_expense_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {

            TextView tvDate, tvName, tvAmount, tvDiff;

            VH(@NonNull View itemView) {
                super(itemView);
                tvDate   = itemView.findViewById(R.id.tv_date);
                tvName   = itemView.findViewById(R.id.tv_expense_detail_name);
                tvAmount = itemView.findViewById(R.id.tv_expense_amount_primary);
                tvDiff   = itemView.findViewById(R.id.tv_expense_amount_secondary);
            }

            void bind(RowItem item) {
                tvDate.setText(item.date);
                tvName.setText(item.name);
                tvAmount.setText(item.amount);
                tvDiff.setText(item.diff);
            }
        }
    }
}
