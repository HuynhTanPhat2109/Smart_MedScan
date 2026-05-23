package ntu.tanphat.smart_medscan.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import ntu.tanphat.smart_medscan.data.models.CareSchedule;

public class ScheduleRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<QuerySnapshot> getDepartments() {
        return db.collection("departments")
                .orderBy("name")
                .get();
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

    public Task<QuerySnapshot> getSchedulesByDate(String scheduleDate) {
        return db.collection("schedules")
                .whereEqualTo("scheduleDate", scheduleDate)
                .get();
    }

    public Task<DocumentReference> addSchedule(CareSchedule schedule) {
        return db.collection("schedules")
                .add(schedule);
    }

    public Task<Void> updateScheduleStatus(String scheduleId, String status) {
        return db.collection("schedules")
                .document(scheduleId)
                .update("status", status);
    }

    public Task<Void> deleteSchedule(String scheduleId) {
        return db.collection("schedules")
                .document(scheduleId)
                .delete();
    }
}