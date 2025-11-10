package ui.chart;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.exchangefx.R;

public class ChartsNav extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts_nav);

        //  공용 툴바 제목 변경
        TextView toolbarTitle = findViewById(R.id.tvTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setText("내 지출 한눈에 보기");
        }
    }
}
