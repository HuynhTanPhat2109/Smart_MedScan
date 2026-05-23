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
        return db.collection("floors")
                .whereEqualTo("departmentId", departmentId)
                .orderBy("name")
                .get();
    }

    public Task<QuerySnapshot> getRooms(String floorId) {
        return db.collection("rooms")
                .whereEqualTo("floorId", floorId)
                .orderBy("name")
                .get();
    }

    public Task<QuerySnapshot> getPatients(String roomId) {
        return db.collection("patients")
                .whereEqualTo("roomId", roomId)
                .orderBy("name")
                .get();
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
        db.collection("departments")
                .document(deptId)
                .update("count", FieldValue.increment(change));
    }


    public Task<QuerySnapshot> searchDepartments(String searchText) {
        return getDepartments();
    }

    public Task<QuerySnapshot> searchFloors(String searchText, String deptId) {
        return getFloors(deptId);
    }

    public Task<QuerySnapshot> searchRooms(String searchText, String floorId) {
        return getRooms(floorId);
    }

    public Task<QuerySnapshot> searchPatients(String searchText, String roomId, String status) {
        Query query = db.collection("patients");

        if (roomId != null) {
            query = query.whereEqualTo("roomId", roomId);
        }

        if (status != null && !status.equals("Tất cả")) {
            query = query.whereEqualTo("status", status);
        }

        return query.orderBy("name").get();
    }

    public Task<QuerySnapshot> getMedicineByName(String name) {
        return db.collection("medicines")
                .whereEqualTo("name", name)
                .limit(1)
                .get();
    }
}