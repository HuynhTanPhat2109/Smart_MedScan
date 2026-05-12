package ntu.tanphat.smart_medscan.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import ntu.tanphat.smart_medscan.data.firebase.FirebaseHelper;
import ntu.tanphat.smart_medscan.data.models.User;

public class AuthRepository {
    private FirebaseHelper fb;

    public AuthRepository() {
        this.fb = FirebaseHelper.getInstance();
    }
    // Hàm đăng nhập
    public Task<AuthResult> login(String email, String password) {
        return fb.getAuth().signInWithEmailAndPassword(email, password);
    }
    // Hàm đăng ký
    public Task<AuthResult> register(String email, String password) {
        return fb.getAuth().createUserWithEmailAndPassword(email, password);
    }

    // Lưu thông tin User vào Firestore
    public Task<Void> saveUserToFirestore(User user) {
        return fb.getFirestore().collection("users")
                .document(user.getUid())
                .set(user);
    }

}
