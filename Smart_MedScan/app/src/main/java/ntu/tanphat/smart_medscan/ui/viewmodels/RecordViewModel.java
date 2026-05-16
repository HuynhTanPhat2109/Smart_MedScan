package ntu.tanphat.smart_medscan.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ntu.tanphat.smart_medscan.data.models.Department;
import ntu.tanphat.smart_medscan.data.models.Floor;
import ntu.tanphat.smart_medscan.data.models.PatientRecord;
import ntu.tanphat.smart_medscan.data.models.Room;
import ntu.tanphat.smart_medscan.data.repository.RecordRepository;

public class RecordViewModel extends ViewModel {
    private final RecordRepository repository;
    private final MutableLiveData<List<Object>> displayList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public RecordViewModel() {
        repository = new RecordRepository();
    }

    public LiveData<List<Object>> getDisplayList() { return displayList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void fetchDepartments() {
        isLoading.setValue(true);
        repository.getDepartments().addOnSuccessListener(snapshots -> {
            List<Object> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                Department dept = doc.toObject(Department.class);
                dept.setId(doc.getId());
                list.add(dept);
            }
            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void fetchFloors(String deptId) {
        isLoading.setValue(true);
        repository.getFloors(deptId).addOnSuccessListener(snapshots -> {
            List<Object> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                Floor floor = doc.toObject(Floor.class);
                floor.setId(doc.getId());
                list.add(floor);
            }
            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void fetchRooms(String floorId) {
        isLoading.setValue(true);
        repository.getRooms(floorId).addOnSuccessListener(snapshots -> {
            List<Object> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                Room room = doc.toObject(Room.class);
                room.setId(doc.getId());
                list.add(room);
            }
            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void fetchPatients(String roomId) {
        isLoading.setValue(true);
        repository.getPatients(roomId).addOnSuccessListener(snapshots -> {
            List<Object> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                PatientRecord p = doc.toObject(PatientRecord.class);
                p.setPatientId(doc.getId());
                list.add(p);
            }
            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void searchDepartments(String text) {
        isLoading.setValue(true);
        repository.searchDepartments(text).addOnSuccessListener(snapshots -> {
            List<Object> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                Department dept = doc.toObject(Department.class);
                dept.setId(doc.getId());
                list.add(dept);
            }
            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void searchFloors(String text, String deptId) {
        isLoading.setValue(true);
        repository.searchFloors(text, deptId).addOnSuccessListener(snapshots -> {
            List<Object> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                Floor floor = doc.toObject(Floor.class);
                floor.setId(doc.getId());
                list.add(floor);
            }
            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void searchRooms(String text, String floorId) {
        isLoading.setValue(true);
        repository.searchRooms(text, floorId).addOnSuccessListener(snapshots -> {
            List<Object> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                Room room = doc.toObject(Room.class);
                room.setId(doc.getId());
                list.add(room);
            }
            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void searchPatients(String text, String roomId, String status) {
        isLoading.setValue(true);
        repository.searchPatients(text, roomId, status).addOnSuccessListener(snapshots -> {
            List<Object> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                PatientRecord p = doc.toObject(PatientRecord.class);
                p.setPatientId(doc.getId());
                list.add(p);
            }
            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void addRecord(String collection, Map<String, Object> data, Runnable onSuccess) {
        repository.addRecord(collection, data).addOnSuccessListener(ref -> onSuccess.run())
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }

    public void updateRecord(String collection, String id, Map<String, Object> data, Runnable onSuccess) {
        repository.updateRecord(collection, id, data).addOnSuccessListener(v -> onSuccess.run())
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }

    public void deleteRecord(String collection, String id, Runnable onSuccess) {
        repository.deleteRecord(collection, id).addOnSuccessListener(v -> onSuccess.run())
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }

    public void updateDeptCount(String deptId, int change) {
        repository.updateDeptCount(deptId, change);
    }
}
