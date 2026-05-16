package ntu.tanphat.smart_medscan.ui.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.ui.fragments.HomeFragment;
import ntu.tanphat.smart_medscan.ui.fragments.ProfileFragment;
import ntu.tanphat.smart_medscan.ui.fragments.RecordFragment;
import ntu.tanphat.smart_medscan.ui.fragments.ScanFragment;
import ntu.tanphat.smart_medscan.ui.fragments.TimeFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabScan;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Tối ưu hóa không gian hiển thị (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupNavigation();

        // Màn hình mặc định khi vừa mở App: Home
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabScan = findViewById(R.id.fabScan);
        fragmentManager = getSupportFragmentManager();

        // Vô hiệu hóa item placeholder ở giữa để nhường chỗ cho nút Scan
        bottomNavigationView.getMenu().getItem(2).setEnabled(false);
    }

    private void setupNavigation() {
        // Xử lý sự kiện khi nhấn các nút trên thanh điều hướng
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_time) {
                replaceFragment(new TimeFragment());
                return true;
            } else if (itemId == R.id.nav_records) {
                replaceFragment(new RecordFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        // Sự kiện nút quét Scan (Trung tâm)
        fabScan.setOnClickListener(v -> {
            replaceFragment(new ScanFragment());
            // Bỏ chọn các item khác để làm nổi bật trạng thái Scan
            bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
        });
    }

    /**
     * Hàm dùng để thay thế Fragment hiện tại một cách mượt mà
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // Hiệu ứng chuyển cảnh mượt mà phong cách 2026
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
