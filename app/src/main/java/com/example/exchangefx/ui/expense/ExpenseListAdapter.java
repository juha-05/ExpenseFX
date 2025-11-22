package com.example.exchangefx.ui.expense;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangefx.R;
import com.example.exchangefx.data.entity.Expense2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ViewHolder> {

    private List<Expense2> items;

    // ✔ 선택된 항목 id 저장 (삭제·수정 시 사용)
    private Set<Integer> selectedIds = new HashSet<>();

    public ExpenseListAdapter(List<Expense2> items) {
        this.items = items;
    }

    public void setItems(List<Expense2> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    // ===================================================
    // ✔ 전체 선택 / 전체 해제
    // ===================================================
    public void selectAll(boolean checked) {
        selectedIds.clear();
        if (checked) {
            for (Expense2 e : items) selectedIds.add(e.id);
        }
        notifyDataSetChanged();
    }

    // ===================================================
    // ✔ 선택된 항목 조회 (수정·삭제용)
    // ===================================================
    public Set<Integer> getSelectedIds() {
        return selectedIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Expense2 d = items.get(position);

        holder.txtName.setText(d.getName());
        holder.txtMemo.setText(d.getMemo());
        holder.txtCategory.setText(d.category);

        holder.txtLocal.setText(String.format("+₩%,.0f", d.targetAmount));
        holder.txtForeign.setText(d.baseCurrency + " " + String.format("%,.2f", d.baseAmount));

        // ------------------------------------------------
        // ✔ 체크박스 스크롤 버그 방지 (리스너 초기화 후 다시 등록)
        // ------------------------------------------------
        holder.checkBox.setOnCheckedChangeListener(null);

        boolean isChecked = selectedIds.contains(d.id);
        holder.checkBox.setChecked(isChecked);

        holder.checkBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean checked) -> {
            if (checked) selectedIds.add(d.id);
            else selectedIds.remove(d.id);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        TextView txtName, txtLocal, txtForeign, txtMemo, txtCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkBox);
            txtName = itemView.findViewById(R.id.txtName);
            txtLocal = itemView.findViewById(R.id.txtLocal);
            txtForeign = itemView.findViewById(R.id.txtForeign);
            txtMemo = itemView.findViewById(R.id.txtMemo);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }
    }
}
