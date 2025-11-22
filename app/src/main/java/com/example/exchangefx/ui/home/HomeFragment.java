package com.example.exchangefx.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangefx.R;
import com.example.exchangefx.data.dao.ExpenseDao2;
import com.example.exchangefx.data.db.AppDatabase2;
import com.example.exchangefx.data.entity.Expense2;
import com.example.exchangefx.ui.expense.ExpenseList;
import com.example.exchangefx.ui.main.MainActivity;
import com.example.exchangefx.ui.chart.ChartsNav;
import com.example.exchangefx.ui.expense.ExpenseEditNav;
import com.example.exchangefx.ui.settings.Settings;
import com.example.exchangefx.utils.FrankfurterCall;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HomeFragment 최종본 (환율 비교 기능 유지)
 * - Today 기준: FrankfurterCall 사용
 * - AtSpend 기준: DB에 저장된 targetAmount 그대로 사용
 */
public class HomeFragment extends Fragment {

    private TextView tvTotalExpenseAmount;
    private TextView tvSectionDate;
    private RecyclerView recyclerRecent;
    private FloatingActionButton fabQuickAdd;

    private RadioButton rbToday, rbAtSpend;

    private RecentExpenseAdapter recentAdapter;

    private ExpenseDao2 expenseDao;
    private FrankfurterCall fxRateClient;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private enum Basis { TODAY, AT_SPEND }
    private Basis currentBasis = Basis.TODAY;

    private static final int MAX_RECENT = 5;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // View 찾기
        tvTotalExpenseAmount = v.findViewById(R.id.tv_total_expense_amount);
        tvSectionDate        = v.findViewById(R.id.tv_section_date);
        recyclerRecent       = v.findViewById(R.id.recycler_recent);
        fabQuickAdd          = v.findViewById(R.id.fab_quick_add);
        rbToday              = v.findViewById(R.id.rb_today);
        rbAtSpend            = v.findViewById(R.id.rb_at_spend);

        BottomNavigationView bottomNav = v.findViewById(R.id.bottom_navigation);
        TextView tvChevron   = v.findViewById(R.id.tv_chevron);

        // ">" → 지출내역 페이지
        tvChevron.setOnClickListener(view -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity())
                        .replace(new ExpenseList(), true);
            }
        });

        // DB 인스턴스
        expenseDao = AppDatabase2.getInstance(requireContext()).expenseDao2();
        fxRateClient = new FrankfurterCall();

        // 오늘 날짜 표시
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd", Locale.KOREA);
        tvSectionDate.setText(sdf.format(new Date()));

        // 리사이클러뷰 설정
        recentAdapter = new RecentExpenseAdapter();
        recyclerRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRecent.setAdapter(recentAdapter);

        // 환율 기준 라디오 버튼 설정
        rbToday.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                currentBasis = Basis.TODAY;
                recalcAmounts();
            }
        });
        rbAtSpend.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                currentBasis = Basis.AT_SPEND;
                recalcAmounts();
            }
        });

        rbToday.setChecked(true); // 기본값

        // FAB → 지출 추가 화면
        fabQuickAdd.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), ExpenseEditNav.class);
            intent.putExtra(ExpenseEditNav.EXTRA_OPEN_ADD, true);
            startActivity(intent);
        });

        // 하단 네비게이션
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_add) {
                Intent intent = new Intent(requireActivity(), ExpenseEditNav.class);
                intent.putExtra(ExpenseEditNav.EXTRA_OPEN_ADD, true);
                startActivity(intent);
                return true;
            }
            if (id == R.id.nav_charts) {
                startActivity(new Intent(requireActivity(), ChartsNav.class));
                return true;
            }
            if (id == R.id.nav_settings) {
                startActivity(new Intent(requireActivity(), Settings.class));
                return true;
            }
            return false;
        });

        // 첫 계산
        recalcAmounts();

        return v;
    }

    /**
     * 지출 금액 재계산:
     * - AT_SPEND: DB targetAmount 합
     * - TODAY: API 최신 환율로 baseAmount 환산
     */
    private void recalcAmounts() {
        ioExecutor.execute(() -> {

            List<Expense2> all = expenseDao.getAllExpenses();  // id DESC 정렬됨

            double total = 0.0;

            for (Expense2 e : all) {

                if (currentBasis == Basis.AT_SPEND) {
                    // 지출 시점 환율 = 저장된 targetAmount 그대로 사용
                    total += e.targetAmount;
                    continue;
                }

                // ---- TODAY 기준: API 최신 환율로 재계산 ----
                double base = e.baseAmount;
                String baseCurrency = e.baseCurrency;

                if (baseCurrency == null || baseCurrency.trim().isEmpty()) continue;

                double rate = 1.0;

                if (!"KRW".equalsIgnoreCase(baseCurrency)) {
                    try {
                        // 최신 환율
                        rate = fxRateClient.getRateToKrw("latest", baseCurrency);
                    } catch (IOException | JSONException ex) {
                        ex.printStackTrace();
                        continue;
                    }
                }

                total += base * rate;
            }

            // 최근 5개
            int max = Math.min(all.size(), MAX_RECENT);
            List<Expense2> recent = all.subList(0, max);

            double finalTotal = total;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvTotalExpenseAmount.setText(formatKrw(finalTotal));
                    recentAdapter.setItems(recent);
                });
            }
        });
    }

    private String formatKrw(double amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.KOREA);
        return nf.format(amount);
    }
}
