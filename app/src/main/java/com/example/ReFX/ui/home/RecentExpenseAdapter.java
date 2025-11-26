package com.example.ReFX.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ReFX.R;
import com.example.ReFX.data.entity.Expense2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        //  제목 우선순위: name → memo → category
        String titleText;
        if (e.name != null && !e.name.trim().isEmpty()) {
            titleText = e.name;
        } else if (e.memo != null && !e.memo.trim().isEmpty()) {
            titleText = e.memo;
        } else {
            titleText = e.category;
        }
        holder.title.setText(titleText);

        // 날짜 표시
        String dateStr = (e.spendDate != null) ? e.spendDate : "";

        // 예: "2025. 11. 22 · USD 40.00"
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
