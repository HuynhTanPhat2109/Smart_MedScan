package ntu.tanphat.smart_medscan.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import ntu.tanphat.smart_medscan.data.models.User;
import ntu.tanphat.smart_medscan.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<FirebaseUser> userLiveData;
    private MutableLiveData<String> errorLiveData;

    public AuthViewModel() {
        authRepository = new AuthRepository();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public LiveData<FirebaseUser> getUserLiveData() { return userLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    // Logic Đăng ký: Tạo tài khoản -> Lưu thông tin vào Firestore
    public void register(String email, String password, String fullName, String department) {
        authRepository.register(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();
                if (firebaseUser != null) {
                    User newUser = new User(firebaseUser.getUid(), fullName, email, department, "Nurse");

                    // Lưu tiếp vào Firestore
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
}
