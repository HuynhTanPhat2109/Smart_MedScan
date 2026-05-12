package ntu.tanphat.smart_medscan.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mFirestore;

    // Singleton Pattern: Đảm bảo chỉ có một "người quản kho" duy nhất
    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return mAuth; }
    public FirebaseFirestore getFirestore() { return mFirestore; }
}
