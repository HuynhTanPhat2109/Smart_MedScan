package ntu.tanphat.smart_medscan.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.data.models.User;
import ntu.tanphat.smart_medscan.ui.activities.LoginActivity;
import ntu.tanphat.smart_medscan.ui.activities.MainActivity;
import ntu.tanphat.smart_medscan.ui.viewmodels.AuthViewModel;

public class ProfileFragment extends Fragment {

    private AuthViewModel authViewModel;

    private TextView tvProfileName, tvProfileRole, tvProfileEmail, tvProfileDepartment;
    private View btnEditProfile, btnLogout;
    private View menuNurseInfo, menuScanQR, menuSettings, menuTerms, menuAppInfo;

    private User currentUserInfo;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupListeners();
        observeViewModel();

        authViewModel.loadCurrentUserInfo();

        return view;
    }

    private void initViews(View view) {
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileDepartment = view.findViewById(R.id.tvProfileDepartment);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        menuNurseInfo = view.findViewById(R.id.menuNurseInfo);
        menuScanQR = view.findViewById(R.id.menuScanQR);
        menuSettings = view.findViewById(R.id.menuSettings);
        menuTerms = view.findViewById(R.id.menuTerms);
        menuAppInfo = view.findViewById(R.id.menuAppInfo);
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        menuNurseInfo.setOnClickListener(v -> showNurseInfoDialog());

        menuScanQR.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectBottomNavTab(R.id.placeholder);
            }
        });

        menuSettings.setOnClickListener(v -> showSettingsDialog());

        menuTerms.setOnClickListener(v -> showTermsDialog());

        menuAppInfo.setOnClickListener(v -> showAppInfoDialog());

        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void observeViewModel() {
        authViewModel.getUserInfoLiveData().observe(getViewLifecycleOwner(), user -> {
            currentUserInfo = user;
            bindUserInfo(user);
        });

        authViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getSuccessLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getLogoutLiveData().observe(getViewLifecycleOwner(), isLogout -> {
            if (isLogout != null && isLogout) {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void bindUserInfo(User user) {
        if (user == null) return;

        String fullName = isEmpty(user.getFullName()) ? "Điều dưỡng" : user.getFullName();
        String email = isEmpty(user.getEmail()) ? "Chưa cập nhật" : user.getEmail();
        String department = isEmpty(user.getDepartment()) ? "Chưa cập nhật" : user.getDepartment();
        String role = user.getRole();

        tvProfileName.setText(fullName);
        tvProfileEmail.setText(email);
        tvProfileDepartment.setText("Khoa phụ trách: " + department);

        if ("Nurse".equalsIgnoreCase(role)) {
            tvProfileRole.setText("Điều dưỡng lâm sàng");
        } else {
            tvProfileRole.setText(isEmpty(role) ? "Nhân viên y tế" : role);
        }
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        EditText etFullName = dialogView.findViewById(R.id.etEditFullName);
        EditText etDepartment = dialogView.findViewById(R.id.etEditDepartment);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelEditProfile);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveEditProfile);

        if (currentUserInfo != null) {
            etFullName.setText(currentUserInfo.getFullName());
            etDepartment.setText(currentUserInfo.getDepartment());
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String department = etDepartment.getText().toString().trim();

            if (fullName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
                return;
            }

            if (department.isEmpty()) {
                department = "Chưa cập nhật";
            }

            authViewModel.updateProfile(fullName, department);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showNurseInfoDialog() {
        if (currentUserInfo == null) {
            Toast.makeText(getContext(), "Chưa tải được thông tin điều dưỡng", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = isEmpty(currentUserInfo.getFullName()) ? "Chưa cập nhật" : currentUserInfo.getFullName();
        String email = isEmpty(currentUserInfo.getEmail()) ? "Chưa cập nhật" : currentUserInfo.getEmail();
        String department = isEmpty(currentUserInfo.getDepartment()) ? "Chưa cập nhật" : currentUserInfo.getDepartment();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thông tin điều dưỡng")
                .setMessage(
                        "Họ tên: " + fullName +
                                "\nEmail: " + email +
                                "\nKhoa phụ trách: " + department +
                                "\nVai trò: Điều dưỡng lâm sàng"
                )
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showSettingsDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cài đặt")
                .setMessage("Bạn có thể cấu hình thông báo lịch chăm sóc, rung nhắc lịch và các thiết lập cá nhân khác trong các phiên bản tiếp theo.")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showTermsDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Điều khoản sử dụng")
                .setMessage("Ứng dụng Smart MedScan chỉ hỗ trợ điều dưỡng quản lý thông tin và tra cứu thuốc. Thông tin trong ứng dụng không thay thế chỉ định của bác sĩ hoặc quy trình chuyên môn của bệnh viện.")
                .setPositiveButton("Tôi đã hiểu", null)
                .show();
    }

    private void showAppInfoDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Smart MedScan")
                .setMessage("Phiên bản: 1.0.0\nĐề tài: Ứng dụng di động hỗ trợ quản lý và nhận diện thuốc lâm sàng cho điều dưỡng.\nCông nghệ: Android Java, MVVM, Firebase, ML Kit OCR.")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showLogoutConfirmDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất khỏi Smart MedScan không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> authViewModel.logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}