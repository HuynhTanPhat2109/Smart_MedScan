package ntu.tanphat.smart_medscan.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Collections;
import java.util.Comparator;

import ntu.tanphat.smart_medscan.data.models.CareSchedule;
import ntu.tanphat.smart_medscan.data.models.Department;
import ntu.tanphat.smart_medscan.data.models.Floor;
import ntu.tanphat.smart_medscan.data.models.PatientRecord;
import ntu.tanphat.smart_medscan.data.models.Room;
import ntu.tanphat.smart_medscan.data.repository.ScheduleRepository;

public class ScheduleViewModel extends ViewModel {

    public interface DataCallback<T> {
        void onSuccess(List<T> data);
        void onError(String message);
    }

    private final ScheduleRepository repository = new ScheduleRepository();

    private final MutableLiveData<List<CareSchedule>> schedulesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<CareSchedule>> getSchedulesLiveData() {
        return schedulesLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    public String getCurrentScheduleDate() {
        return getTodayDate();
    }

    public void fetchSchedules() {
        isLoading.setValue(true);

        repository.getSchedulesByDate(getTodayDate())
                .addOnSuccessListener(snapshot -> {
                    List<CareSchedule> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        CareSchedule schedule = doc.toObject(CareSchedule.class);
                        schedule.setId(doc.getId());
                        list.add(schedule);
                    }

                    // Sắp xếp theo giờ trong Java để tránh lỗi index Firestore
                    Collections.sort(list, new Comparator<CareSchedule>() {
                        @Override
                        public int compare(CareSchedule s1, CareSchedule s2) {
                            String t1 = s1.getTime() == null ? "" : s1.getTime();
                            String t2 = s2.getTime() == null ? "" : s2.getTime();
                            return t1.compareTo(t2);
                        }
                    });

                    schedulesLiveData.setValue(list);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public void addSchedule(CareSchedule schedule) {
        isLoading.setValue(true);

        repository.addSchedule(schedule)
                .addOnSuccessListener(ref -> {
                    fetchSchedules();
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public void markScheduleDone(String scheduleId) {
        if (scheduleId == null) return;

        repository.updateScheduleStatus(scheduleId, "Đã xong")
                .addOnSuccessListener(v -> fetchSchedules())
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }

    public void deleteSchedule(String scheduleId) {
        if (scheduleId == null) return;

        repository.deleteSchedule(scheduleId)
                .addOnSuccessListener(v -> fetchSchedules())
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }

    public void loadDepartments(DataCallback<Department> callback) {
        repository.getDepartments()
                .addOnSuccessListener(snapshot -> {
                    List<Department> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Department item = doc.toObject(Department.class);
                        item.setId(doc.getId());
                        list.add(item);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loadFloors(String departmentId, DataCallback<Floor> callback) {
        repository.getFloors(departmentId)
                .addOnSuccessListener(snapshot -> {
                    List<Floor> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Floor item = doc.toObject(Floor.class);
                        item.setId(doc.getId());
                        list.add(item);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loadRooms(String floorId, DataCallback<Room> callback) {
        repository.getRooms(floorId)
                .addOnSuccessListener(snapshot -> {
                    List<Room> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Room item = doc.toObject(Room.class);
                        item.setId(doc.getId());
                        list.add(item);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loadPatients(String roomId, DataCallback<PatientRecord> callback) {
        repository.getPatients(roomId)
                .addOnSuccessListener(snapshot -> {
                    List<PatientRecord> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        PatientRecord item = doc.toObject(PatientRecord.class);
                        item.setPatientId(doc.getId());
                        list.add(item);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}