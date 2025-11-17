package com.example.exchangefx.ui.expense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangefx.R;

import java.util.Arrays;
import java.util.List;

public class ExpenseList extends Fragment {

    // true  = 오늘 환율 기준
    // false = 지출 시점 환율 기준
    private boolean useToday = true;

    private TextView tvTotalAmount;
    private TextView tvMonthYear;
    private TextView tvCurrencyDropdown;
    private Button btnTodayRate;
    private Button btnTransactionRate;
    private RecyclerView rvExpenseList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_expense_list, container, false);

        // ----- View 연결 -----

        // 1) 상단 뒤로가기 버튼
        v.findViewById(R.id.btn_back).setOnClickListener(view ->
                requireActivity().onBackPressed()
        );

        // 2) 요약 영역
        tvTotalAmount = v.findViewById(R.id.tv_total_expense_list_amount);
        tvMonthYear = v.findViewById(R.id.tv_month_year_selector);
        tvCurrencyDropdown = v.findViewById(R.id.tv_currency_dropdown);

        // 3) 환율 기준 토글 버튼
        btnTodayRate = v.findViewById(R.id.btn_today_rate_list);
        btnTransactionRate = v.findViewById(R.id.btn_transaction_rate_list);

        // 4) 리스트
        rvExpenseList = v.findViewById(R.id.rv_expense_list);

        // ----- 기본 더미 값 설정 -----
        tvMonthYear.setText("2025. 11");
        tvCurrencyDropdown.setText("원화 ▾");

        // 환율 기준 버튼 클릭 리스너
        btnTodayRate.setOnClickListener(view -> {
            useToday = true;
            updateRateToggleUI();
            updateTotalAmount();
        });

        btnTransactionRate.setOnClickListener(view -> {
            useToday = false;
            updateRateToggleUI();
            updateTotalAmount();
        });

        // RecyclerView 세팅 (현재는 더미 데이터)
        rvExpenseList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpenseList.setAdapter(new RowAdapter(dummyRows()));

        // 초기 UI 상태 반영
        updateRateToggleUI();
        updateTotalAmount();

        return v;
    }

    /** 환율 기준 버튼 UI 상태(선택/비선택) 업데이트 */
    private void updateRateToggleUI() {
        btnTodayRate.setSelected(useToday);
        btnTransactionRate.setSelected(!useToday);
        // 여기서 state_selected 기반 selector(background, textColor)를 입히면
        // 눌렸을 때 색상/배경 바뀌게 만들 수 있음
    }

    /** 상단 총 지출 금액 텍스트 업데이트 (지금은 더미 값) */
    private void updateTotalAmount() {
        if (tvTotalAmount == null) return;

        // TODO: 실제 DB + Frankfurter API 결과로 계산해서 표시할 예정
        if (useToday) {
            tvTotalAmount.setText("₩ 1,234,000");   // 오늘 환율 기준 예시
        } else {
            tvTotalAmount.setText("₩ 1,210,000");   // 지출 시점 환율 기준 예시
        }
    }

    // =========================
    //   RecyclerView 내부 클래스
    // =========================

    /** 한 행에 표시할 데이터 구조 */
    private static class RowItem {
        final String date;
        final String name;
        final String amountPrimary;
        final String amountSecondary;

        RowItem(String date, String name,
                String amountPrimary, String amountSecondary) {
            this.date = date;
            this.name = name;
            this.amountPrimary = amountPrimary;
            this.amountSecondary = amountSecondary;
        }
    }

    /** 지출 내역 리스트 어댑터 */
    private static class RowAdapter extends RecyclerView.Adapter<RowAdapter.RowVH> {

        private final List<RowItem> data;

        RowAdapter(List<RowItem> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_expense_row, parent, false);
            return new RowVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RowVH holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        /** 한 행(ViewHolder) */
        static class RowVH extends RecyclerView.ViewHolder {

            private final TextView tvDate;
            private final TextView tvName;
            private final TextView tvAmountPrimary;
            private final TextView tvAmountSecondary;

            RowVH(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvName = itemView.findViewById(R.id.tv_expense_detail_name);
                tvAmountPrimary = itemView.findViewById(R.id.tv_expense_amount_primary);
                tvAmountSecondary = itemView.findViewById(R.id.tv_expense_amount_secondary);
            }

            void bind(RowItem item) {
                tvDate.setText(item.date);
                tvName.setText(item.name);
                tvAmountPrimary.setText(item.amountPrimary);
                tvAmountSecondary.setText(item.amountSecondary);
            }
        }
    }

    /** 더미 데이터 – 나중에 Room + 환율 계산 결과로 교체 */
    private static List<RowItem> dummyRows() {
        return Arrays.asList(
                new RowItem("11. 04", "파스타", "₩ 18,000", "+ ₩ 200 (환차익)"),
                new RowItem("11. 03", "운동화", "₩ 92,000", "- ₩ 800 (환차손)"),
                new RowItem("11. 02", "커피", "₩ 4,500", "+ ₩ 50 (환차익)")
        );
    }
}
