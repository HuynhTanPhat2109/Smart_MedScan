package ntu.tanphat.smart_medscan.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import ntu.tanphat.smart_medscan.data.models.User;
import ntu.tanphat.smart_medscan.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<User> userInfoLiveData;
    private final MutableLiveData<Boolean> logoutLiveData;
    private final MutableLiveData<String> successLiveData;

    public AuthViewModel() {
        authRepository = new AuthRepository();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        userInfoLiveData = new MutableLiveData<>();
        logoutLiveData = new MutableLiveData<>();
        successLiveData = new MutableLiveData<>();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<User> getUserInfoLiveData() {
        return userInfoLiveData;
    }

    public LiveData<Boolean> getLogoutLiveData() {
        return logoutLiveData;
    }

    public LiveData<String> getSuccessLiveData() {
        return successLiveData;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return authRepository.getCurrentFirebaseUser();
    }

    public void register(String email, String password, String fullName, String department) {
        authRepository.register(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();

                if (firebaseUser != null) {
                    User newUser = new User(
                            firebaseUser.getUid(),
                            fullName,
                            email,
                            department,
                            "Nurse"
                    );

                    authRepository.saveUserToFirestore(newUser).addOnCompleteListener(saveTask -> {
                        if (saveTask.isSuccessful()) {
                            userLiveData.postValue(firebaseUser);
                        } else {
                            errorLiveData.postValue("Lỗi lưu thông tin: " + saveTask.getException().getMessage());
                        }
                    });
                }
            } else {
                errorLiveData.postValue("Lỗi đăng ký: " + task.getException().getMessage());
            }
        });
    }

    public void login(String email, String password) {
        authRepository.login(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.postValue(task.getResult().getUser());
            } else {
                errorLiveData.postValue("Lỗi đăng nhập: " + task.getException().getMessage());
            }
        });
    }

    public void loadCurrentUserInfo() {
        FirebaseUser firebaseUser = authRepository.getCurrentFirebaseUser();

        if (firebaseUser == null) {
            errorLiveData.postValue("Chưa có tài khoản đăng nhập");
            return;
        }

        if (authRepository.getCurrentUserInfo() == null) {
            errorLiveData.postValue("Không tìm thấy thông tin người dùng");
            return;
        }

        authRepository.getCurrentUserInfo()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);

                        if (user != null) {
                            userInfoLiveData.postValue(user);
                        }
                    } else {
                        User fallbackUser = new User(
                                firebaseUser.getUid(),
                                "Điều dưỡng",
                                firebaseUser.getEmail(),
                                "Chưa cập nhật",
                                "Nurse"
                        );
                        userInfoLiveData.postValue(fallbackUser);
                    }
                })
                .addOnFailureListener(e -> {
                    errorLiveData.postValue("Lỗi tải hồ sơ: " + e.getMessage());
                });
    }

    public void updateProfile(String fullName, String department) {
        if (authRepository.updateCurrentUserInfo(fullName, department) == null) {
            errorLiveData.postValue("Không thể cập nhật hồ sơ");
            return;
        }

        authRepository.updateCurrentUserInfo(fullName, department)
                .addOnSuccessListener(unused -> {
                    successLiveData.postValue("Cập nhật hồ sơ thành công");
                    loadCurrentUserInfo();
                })
                .addOnFailureListener(e -> {
                    errorLiveData.postValue("Lỗi cập nhật: " + e.getMessage());
                });
    }

    public void logout() {
        authRepository.logout();
        logoutLiveData.postValue(true);
    }
}