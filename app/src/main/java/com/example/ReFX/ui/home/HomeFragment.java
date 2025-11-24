package com.example.ReFX.ui.home;

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

import com.example.ReFX.R;
import com.example.ReFX.data.dao.ExpenseDao2;
import com.example.ReFX.data.db.AppDatabase2;
import com.example.ReFX.data.entity.Expense2;
import com.example.ReFX.ui.expense.ExpenseList;
import com.example.ReFX.ui.main.MainActivity;
import com.example.ReFX.ui.chart.ChartsNav;
import com.example.ReFX.ui.expense.ExpenseEditNav;
import com.example.ReFX.ui.settings.Settings;
import com.example.ReFX.utils.FrankfurterCall;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

        tvTotalExpenseAmount = v.findViewById(R.id.tv_total_expense_amount);
        tvSectionDate        = v.findViewById(R.id.tv_section_date);
        recyclerRecent       = v.findViewById(R.id.recycler_recent);
        fabQuickAdd          = v.findViewById(R.id.fab_quick_add);
        rbToday              = v.findViewById(R.id.rb_today);
        rbAtSpend            = v.findViewById(R.id.rb_at_spend);

        BottomNavigationView bottomNav = v.findViewById(R.id.bottom_navigation);
        TextView tvChevron   = v.findViewById(R.id.tv_chevron);


        // ------------------------------------------
        // > 버튼 → 지출 목록 화면
        // ------------------------------------------
        tvChevron.setOnClickListener(view -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity())
                        .replace(new ExpenseList(), true);
            }
        });

        // DB & 환율
        expenseDao = AppDatabase2.getInstance(requireContext()).expenseDao2();
        fxRateClient = new FrankfurterCall();

        // 오늘 날짜
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd", Locale.KOREA);
        tvSectionDate.setText(sdf.format(new Date()));

        // 최근 지출 목록
        recentAdapter = new RecentExpenseAdapter();
        recyclerRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRecent.setAdapter(recentAdapter);

        // 환율 기준 라디오
        rbToday.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                currentBasis = Basis.TODAY;
                recalcAmounts();
            }
        });

        rbAtSpend.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                currentBasis = Basis.AT_SPEND;
                recalcAmounts();
            }
        });

        rbToday.setChecked(true);


        // =====================================================
        // 우상단 FloatingActionButton (+) → 바로 추가 화면
        // =====================================================
        fabQuickAdd.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), ExpenseEditNav.class);
            intent.putExtra(ExpenseEditNav.EXTRA_OPEN_ADD, true);  // ★ AddFragment로 바로 감
            startActivity(intent);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_add) {
                Intent intent = new Intent(requireActivity(), ExpenseEditNav.class);
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

        // 첫 계산 실행
        recalcAmounts();

        return v;
    }


    /**
     * 홈 화면 상단의 총 금액을 다시 계산
     *
     * TODAY   : 각 지출의 baseCurrency → "latest" 기준 KRW 환산 후 합산
     * AT_SPEND: 각 지출의 baseCurrency → 해당 지출의 fxDate 기준 KRW 환산 후 합산
     *
     * DB에 저장된 targetAmount / targetCurrency 는 사용하지 않고,
     *    항상 baseAmount + 환율로 다시 계산
     */
    private void recalcAmounts() {
        ioExecutor.execute(() -> {

            List<Expense2> all = expenseDao.getAllExpenses();
            double total = 0.0;

            for (Expense2 e : all) {

                double baseAmount   = e.baseAmount;
                String baseCurrency = e.baseCurrency;

                if (baseCurrency == null || baseCurrency.trim().isEmpty()) {
                    continue;
                }

                double rate;

                // KRW 자체라면 환율 1.0
                if ("KRW".equalsIgnoreCase(baseCurrency)) {
                    rate = 1.0;
                } else {
                    // 기준에 따라 사용할 환율 날짜 결정
                    String fxDate;
                    if (currentBasis == Basis.AT_SPEND) {
                        // 지출 시점 환율: row 별 fxDate 사용 (없으면 latest)
                        fxDate = (e.fxDate == null || e.fxDate.trim().isEmpty())
                                ? "latest"
                                : e.fxDate;
                    } else {
                        // TODAY: 항상 latest 기준
                        fxDate = "latest";
                    }

                    try {
                        rate = fxRateClient.getRateWithCache(
                                requireContext(),
                                fxDate,
                                baseCurrency,
                                "KRW"
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // 이 건은 건너뜀
                        continue;
                    }
                }

                total += baseAmount * rate;
            }

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
