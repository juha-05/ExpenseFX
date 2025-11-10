package ui.expense;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.exchangefx.R;

public class ExpenseAddFragment extends Fragment {

    private EditText etAmountFrom, etAmountTo, etName, etMemo;
    private Spinner spinnerCategory, spinnerFrom, spinnerTo;
    private Button btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_add, container, false);

        //  XML과 일치하는 ID로 findViewById 수정
        spinnerFrom = view.findViewById(R.id.spinnerFrom);
        spinnerTo = view.findViewById(R.id.spinnerTo);
        etAmountFrom = view.findViewById(R.id.etAmountFrom);
        etAmountTo = view.findViewById(R.id.etAmountTo);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        etName = view.findViewById(R.id.etName);
        etMemo = view.findViewById(R.id.etMemo);
        btnSave = view.findViewById(R.id.btnSave);

        //  카테고리 Spinner 연결
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.category_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        //  저장 버튼 클릭 이벤트
        btnSave.setOnClickListener(v -> {
            Toast.makeText(getContext(), "지출이 저장되었습니다.", Toast.LENGTH_SHORT).show();

            // 화면 전환 (ExpenseListFragment로 이동)
            if (getActivity() instanceof ExpenseEditNav) {
                ((ExpenseEditNav) getActivity()).loadFragment(new ExpenseListFragment());
            }
        });

        return view;
    }
}
