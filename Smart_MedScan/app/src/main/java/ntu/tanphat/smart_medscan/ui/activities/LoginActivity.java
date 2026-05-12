package ntu.tanphat.smart_medscan.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.ui.viewmodels.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Xử lý Padding hệ thống để không bị tràn vào Status Bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initViewModel();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void initViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Quan sát kết quả đăng nhập
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        // Quan sát lỗi
        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                // Chuyển về chữ thường để so sánh không phân biệt hoa thường
                String lowerError = error.toLowerCase();

                if (lowerError.contains("credential") || lowerError.contains("invalid")) {
                    // Đây là lỗi do sai Email HOẶC sai Mật khẩu
                    showTopError("Email hoặc mật khẩu không chính xác");
                } else if (lowerError.contains("network") || lowerError.contains("timeout")) {
                    showTopError("Network error. Please check your connection.");
                } else {
                    showTopError(error);
                }
            }
        });
    }

    private void setupClickListeners() {
        // Nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Định dạng email không hợp lệ (ví dụ: name@email.com)");
                etEmail.requestFocus();
                return;
            }
            authViewModel.login(email, password);
        });

        // Nút chuyển sang màn hình Đăng ký (Ô vuông ở dưới)
        findViewById(R.id.btnToRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Các nút phụ khác (Hỗ trợ, Hướng dẫn, Tin tức)
        findViewById(R.id.btnSupport).setOnClickListener(v ->
                Toast.makeText(this, "Đang kết nối tổng đài hỗ trợ...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnGuide).setOnClickListener(v ->
                Toast.makeText(this, "Đang tải tài liệu hướng dẫn PDF...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnNews).setOnClickListener(v ->
                Toast.makeText(this, "Đang tải trang tin tức...", Toast.LENGTH_SHORT).show());
    }
    private void showTopError(String message) {
        View errorBanner = findViewById(R.id.errorBanner);
        TextView tvErrorMessage = findViewById(R.id.tvErrorMessage);

        tvErrorMessage.setText(message);

        if (errorBanner.getVisibility() == View.GONE) {
            errorBanner.setVisibility(View.VISIBLE);

            // Hiệu ứng trượt từ trên xuống (Slide Down)
            errorBanner.setTranslationY(-200f);
            errorBanner.animate()
                    .translationY(0f)
                    .setDuration(400)
                    .setListener(null);

            // Tự động ẩn sau 3 giây
            errorBanner.postDelayed(() -> {
                errorBanner.animate()
                        .translationY(-300f)
                        .setDuration(400)
                        .withEndAction(() -> errorBanner.setVisibility(View.GONE));
            }, 3000);
        }
    }
}