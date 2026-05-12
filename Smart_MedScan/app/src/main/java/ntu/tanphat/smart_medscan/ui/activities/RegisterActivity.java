package ntu.tanphat.smart_medscan.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.ui.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private EditText etEmail, etPassword, etConfirmPassword, etFullName;
    private AutoCompleteTextView actDepartment; // Chuyển sang AutoCompleteTextView cho Dropdown
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Tối ưu padding cho hệ thống (Status bar / Navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupDepartmentDropdown();
        initViewModel();

        btnRegister.setOnClickListener(v -> handleRegistration());

        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword); // Ô mới thêm
        etFullName = findViewById(R.id.etFullName);
        actDepartment = findViewById(R.id.actDepartment);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupDepartmentDropdown() {
        // Danh sách các khoa tiêu chuẩn y tế
        String[] departments = {"Khoa Nội", "Khoa Ngoại", "Khoa Sản", "Khoa Nhi", "Khoa Lồng ngực", "Khoa Cấp cứu"};

        // Sử dụng layout mặc định của Android cho item
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, departments);

        actDepartment.setAdapter(adapter);
    }

    private void initViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Lắng nghe kết quả thành công
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        // Lắng nghe thông báo lỗi từ Firebase
        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }

    private void handleRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();
        String name = etFullName.getText().toString().trim();
        String dept = actDepartment.getText().toString().trim();

        // 1. Kiểm tra không được để trống
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || dept.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        // 2. Kiểm tra định dạng Email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Định dạng email không hợp lệ (ví dụ: name@email.com)");
            etEmail.requestFocus();
            return;
        }

        // 3. Kiểm tra độ phức tạp mật khẩu
        if (!isPasswordValid(password)) {
            etPassword.setError("Mật khẩu phải từ 8 ký tự, gồm: Chữ hoa, chữ thường, chữ số và ký tự đặc biệt (!@#$...)");
            etPassword.requestFocus();
            return;
        }

        // 4. Logic kiểm tra "Nhập lại mật khẩu"
        if (!password.equals(confirmPass)) {
            etConfirmPassword.setError("Mật khẩu không trùng khớp!");
            return;
        }

        // Nếu tất cả ổn, gọi ViewModel gửi dữ liệu lên Firebase
        authViewModel.register(email, password, name, dept);
    }
    private boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }
}
