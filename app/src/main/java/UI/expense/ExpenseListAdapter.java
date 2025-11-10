package ui.expense;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.exchangefx.R;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ViewHolder> {

    private final List<ExpenseModel> list;
    private final Set<Integer> selectedPositions = new HashSet<>(); //  선택된 항목 저장용

    public ExpenseListAdapter(List<ExpenseModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseModel item = list.get(position);

        holder.txtName.setText(item.getName());
        holder.txtForeign.setText(item.getCurrency() + item.getForeignAmount());
        holder.txtLocal.setText(String.format("%+d₩", item.getLocalAmount()));

        //  체크박스 상태 유지
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedPositions.contains(position));

        //  체크 변경 이벤트
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedPositions.add(position);
            else selectedPositions.remove(position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //  선택된 항목 목록 반환 (삭제 기능 등에 사용)
    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    //  선택된 항목 모두 삭제
    public void deleteSelectedItems() {
        list.removeIf(item -> selectedPositions.contains(list.indexOf(item)));
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtForeign, txtLocal;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtForeign = itemView.findViewById(R.id.txtForeign);
            txtLocal = itemView.findViewById(R.id.txtLocal);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
