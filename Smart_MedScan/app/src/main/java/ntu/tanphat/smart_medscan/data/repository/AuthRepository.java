package ntu.tanphat.smart_medscan.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import ntu.tanphat.smart_medscan.data.firebase.FirebaseHelper;
import ntu.tanphat.smart_medscan.data.models.User;

public class AuthRepository {
    private final FirebaseHelper fb;

    public AuthRepository() {
        this.fb = FirebaseHelper.getInstance();
    }

    public Task<AuthResult> login(String email, String password) {
        return fb.getAuth().signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String email, String password) {
        return fb.getAuth().createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> saveUserToFirestore(User user) {
        return fb.getFirestore()
                .collection("users")
                .document(user.getUid())
                .set(user);
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return fb.getAuth().getCurrentUser();
    }

    public Task<DocumentSnapshot> getCurrentUserInfo() {
        FirebaseUser currentUser = getCurrentFirebaseUser();

        if (currentUser == null) {
            return null;
        }

        return fb.getFirestore()
                .collection("users")
                .document(currentUser.getUid())
                .get();
    }

    public Task<Void> updateCurrentUserInfo(String fullName, String department) {
        FirebaseUser currentUser = getCurrentFirebaseUser();

        if (currentUser == null) {
            return null;
        }

        return fb.getFirestore()
                .collection("users")
                .document(currentUser.getUid())
                .update(
                        "fullName", fullName,
                        "department", department
                );
    }

    public void logout() {
        fb.getAuth().signOut();
    }
}