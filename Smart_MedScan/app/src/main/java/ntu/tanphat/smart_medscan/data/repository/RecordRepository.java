package ntu.tanphat.smart_medscan.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class RecordRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<QuerySnapshot> getDepartments() {
        return db.collection("departments").orderBy("name").get();
    }

    public Task<QuerySnapshot> getFloors(String departmentId) {
        return db.collection("floors").whereEqualTo("departmentId", departmentId).orderBy("name").get();
    }

    public Task<QuerySnapshot> getRooms(String floorId) {
        return db.collection("rooms").whereEqualTo("floorId", floorId).orderBy("name").get();
    }

    public Task<QuerySnapshot> getPatients(String roomId) {
        return db.collection("patients").whereEqualTo("roomId", roomId).orderBy("name").get();
    }

    public Task<DocumentReference> addRecord(String collection, Map<String, Object> data) {
        return db.collection(collection).add(data);
    }

    public Task<Void> updateRecord(String collection, String id, Map<String, Object> data) {
        return db.collection(collection).document(id).update(data);
    }

    public Task<Void> deleteRecord(String collection, String id) {
        return db.collection(collection).document(id).delete();
    }

    public void updateDeptCount(String deptId, int change) {
        if (deptId == null) return;
        db.collection("departments").document(deptId).update("count", FieldValue.increment(change));
    }

    // Tìm kiếm Khoa
    public Task<QuerySnapshot> searchDepartments(String text) {
        return db.collection("departments")
                .orderBy("name")
                .startAt(text)
                .endAt(text + "\uf8ff")
                .get();
    }

    // Tìm kiếm Tầng trong Khoa đã chọn
    public Task<QuerySnapshot> searchFloors(String text, String deptId) {
        return db.collection("floors")
                .whereEqualTo("departmentId", deptId)
                .orderBy("name")
                .startAt(text)
                .endAt(text + "\uf8ff")
                .get();
    }

    // Tìm kiếm Phòng trong Tầng đã chọn
    public Task<QuerySnapshot> searchRooms(String text, String floorId) {
        return db.collection("rooms")
                .whereEqualTo("floorId", floorId)
                .orderBy("name")
                .startAt(text)
                .endAt(text + "\uf8ff")
                .get();
    }

    // Tìm kiếm Bệnh nhân kèm lọc trạng thái
    public Task<QuerySnapshot> searchPatients(String searchText, String roomId, String status) {
        Query query = db.collection("patients");

        if (roomId != null) {
            query = query.whereEqualTo("roomId", roomId);
        }

        if (status != null && !status.equals("Tất cả")) {
            query = query.whereEqualTo("status", status);
        }

        return query.orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get();
    }
}
