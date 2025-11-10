package UI.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import UI.home.HomeFragment;
import com.example.exchangefx.R;

public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        if (b == null) replace(new HomeFragment(), false);
    }
    public void replace(Fragment f, boolean addToBackStack) {
        var tx = getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
    }
}
