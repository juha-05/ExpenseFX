package ui.expense;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exchangefx.R;

import java.util.ArrayList;
import java.util.List;

public class ExpenseListFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button btnAdd;
    private ExpenseListAdapter adapter;
    private List<ExpenseModel> expenseList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        btnAdd = view.findViewById(R.id.btnAdd);

        expenseList = new ArrayList<>();
        adapter = new ExpenseListAdapter(expenseList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 새 지출 버튼 클릭 시 추가화면으로 이동
        btnAdd.setOnClickListener(v -> {
            ((ExpenseEditNav) getActivity()).loadFragment(new ExpenseAddFragment());
        });

        // 예시 데이터
        expenseList.add(new ExpenseModel("파스타", "€", 200, 100));
        expenseList.add(new ExpenseModel("딸기우유", "$", 100, -10000));
        expenseList.add(new ExpenseModel("신발", "€", 3000, 30000));
        adapter.notifyDataSetChanged();

        return view;
    }
}
