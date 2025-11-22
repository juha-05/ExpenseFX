package com.example.exchangefx.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangefx.R;
import com.example.exchangefx.data.entity.Expense2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 최근 지출 5개 표시용 어댑터 (Expense2 기반)
 */
public class RecentExpenseAdapter extends RecyclerView.Adapter<RecentExpenseAdapter.VH> {

    private final List<Expense2> items = new ArrayList<>();

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView sub;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tv_title);
            sub   = v.findViewById(R.id.tv_sub);
        }
    }

    public RecentExpenseAdapter() {}

    /** Expense2 리스트 입력 */
    public void setItems(List<Expense2> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_compact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Expense2 e = items.get(position);

        // 제목: 메모가 있으면 메모, 없으면 카테고리
        String titleText;
        if (e.memo != null && !e.memo.isEmpty()) {
            titleText = e.memo;
        } else {
            titleText = e.category;
        }
        holder.title.setText(titleText);

        // 날짜 그대로 사용 (YYYY. MM. DD)
        String dateStr = (e.spendDate != null) ? e.spendDate : "";

        // 예: "2025. 11. 22 · USD 40.0"
        String subText = String.format(
                Locale.getDefault(),
                "%s · %s %.2f",
                dateStr,
                e.baseCurrency,
                e.baseAmount
        );
        holder.sub.setText(subText);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
