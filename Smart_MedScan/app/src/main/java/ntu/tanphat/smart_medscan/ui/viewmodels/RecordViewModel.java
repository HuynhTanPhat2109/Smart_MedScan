package ntu.tanphat.smart_medscan.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.util.Locale;

import ntu.tanphat.smart_medscan.data.models.Department;
import ntu.tanphat.smart_medscan.data.models.Floor;
import ntu.tanphat.smart_medscan.data.models.Medicine;
import ntu.tanphat.smart_medscan.data.models.PatientRecord;
import ntu.tanphat.smart_medscan.data.models.Room;
import ntu.tanphat.smart_medscan.data.repository.RecordRepository;

public class RecordViewModel extends ViewModel {
    private final RecordRepository repository;
    private final MutableLiveData<List<Object>> displayList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Medicine> scanResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> allergyWarningLiveData = new MutableLiveData<>();
    
    private PatientRecord currentPatient;

    public RecordViewModel() {
        repository = new RecordRepository();
    }

    private String normalizeText(String input) {
        if (input == null) return "";

        String text = input.trim().toLowerCase(Locale.ROOT);

        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        text = text.replace("đ", "d");

        text = text.replaceAll("\\s+", " ");

        return text;
    }

    private boolean containsSearch(String source, String keyword) {
        String normalizedSource = normalizeText(source);
        String normalizedKeyword = normalizeText(keyword);

        if (normalizedKeyword.isEmpty()) return true;

        return normalizedSource.contains(normalizedKeyword);
    }

    private boolean matchDepartment(Department department, String keyword) {
        return containsSearch(department.getName(), keyword);
    }

    private boolean matchFloor(Floor floor, String keyword) {
        return containsSearch(floor.getName(), keyword);
    }

    private boolean matchRoom(Room room, String keyword) {
        return containsSearch(room.getName(), keyword);
    }

    private boolean matchPatient(PatientRecord patient, String keyword) {
        if (containsSearch(patient.getName(), keyword)) return true;
        if (containsSearch(patient.getDiagnosis(), keyword)) return true;
        if (containsSearch(patient.getBedNumber(), keyword)) return true;
        if (containsSearch(patient.getAge(), keyword)) return true;
        if (containsSearch(patient.getAllergy(), keyword)) return true;
        if (containsSearch(patient.getStatus(), keyword)) return true;

        return false;
    }

    private int calculateMedicineScore(String rawText, Medicine medicine) {
        if (medicine == null || medicine.getName() == null) return 0;

        String scanText = normalizeText(rawText);
        String medicineName = normalizeText(medicine.getName());

        if (scanText.isEmpty() || medicineName.isEmpty()) return 0;

        int score = 0;

        // Trường hợp OCR đọc được gần đúng nguyên tên thuốc
        if (scanText.contains(medicineName)) {
            score += 100;
        }

        if (medicineName.contains(scanText)) {
            score += 80;
        }

        // So từng từ trong tên thuốc
        String[] nameWords = medicineName.split(" ");
        for (String word : nameWords) {
            if (word.length() >= 3 && scanText.contains(word)) {
                score += 20;
            }
        }

        // So thêm thành phần thuốc nếu OCR đọc được hoạt chất
        if (medicine.getComponents() != null) {
            String components = normalizeText(medicine.getComponents());
            String[] componentWords = components.split("[,;\\s]+");

            for (String word : componentWords) {
                if (word.length() >= 4 && scanText.contains(word)) {
                    score += 10;
                }
            }
        }

        // So thêm chỉ định nếu cần, nhưng điểm thấp hơn để tránh nhận sai
        if (medicine.getIndications() != null) {
            String indications = normalizeText(medicine.getIndications());
            String[] indicationWords = indications.split("[,;\\s]+");

            for (String word : indicationWords) {
                if (word.length() >= 5 && scanText.contains(word)) {
                    score += 3;
                }
            }
        }

        return score;
    }

    private String buildNotFoundMessage(String rawText) {
        String shortText = rawText == null ? "" : rawText.trim();

        if (shortText.length() > 80) {
            shortText = shortText.substring(0, 80) + "...";
        }

        return "Không tìm thấy thuốc phù hợp từ nội dung quét: " + shortText;
    }

    public void scanMedicineFromText(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            errorMessage.setValue("Chưa nhận diện được chữ trên hộp thuốc.");
            return;
        }

        repository.getAllMedicines().addOnSuccessListener(snapshots -> {
            Medicine bestMedicine = null;
            int bestScore = 0;

            for (QueryDocumentSnapshot doc : snapshots) {
                Medicine medicine = doc.toObject(Medicine.class);
                int score = calculateMedicineScore(rawText, medicine);

                if (score > bestScore) {
                    bestScore = score;
                    bestMedicine = medicine;
                }
            }

            // Ngưỡng này có thể chỉnh. 40 là khá ổn cho demo.
            if (bestMedicine != null && bestScore >= 40) {
                checkAllergy(bestMedicine);
                scanResultLiveData.setValue(bestMedicine);
            } else {
                allergyWarningLiveData.setValue(null);
                errorMessage.setValue(buildNotFoundMessage(rawText));
            }

        }).addOnFailureListener(e -> {
            errorMessage.setValue("Lỗi khi tải danh sách thuốc: " + e.getMessage());
        });
    }

    public LiveData<List<Object>> getDisplayList() { return displayList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Medicine> getScanResultLiveData() { return scanResultLiveData; }
    public LiveData<String> getAllergyWarningLiveData() { return allergyWarningLiveData; }

    public void setCurrentPatient(PatientRecord patient) {
        this.currentPatient = patient;
    }

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

                if (matchDepartment(dept, text)) {
                    list.add(dept);
                }
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

                if (matchFloor(floor, text)) {
                    list.add(floor);
                }
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

                if (matchRoom(room, text)) {
                    list.add(room);
                }
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
                PatientRecord patient = doc.toObject(PatientRecord.class);
                patient.setPatientId(doc.getId());

                if (matchPatient(patient, text)) {
                    list.add(patient);
                }
            }

            displayList.setValue(list);
            isLoading.setValue(false);
        }).addOnFailureListener(e -> {
            errorMessage.setValue(e.getMessage());
            isLoading.setValue(false);
        });
    }

    public void checkMedicine(String medicineName) {
        repository.getMedicineByName(medicineName).addOnSuccessListener(snapshots -> {
            if (!snapshots.isEmpty()) {
                Medicine medicine = snapshots.getDocuments().get(0).toObject(Medicine.class);
                if (medicine != null) {
                    scanResultLiveData.setValue(medicine);
                    checkAllergy(medicine);
                }
            } else {
                errorMessage.setValue("Không tìm thấy thông tin thuốc: " + medicineName);
            }
        });
    }

    private void checkAllergy(Medicine medicine) {
        allergyWarningLiveData.setValue(null);

        if (currentPatient == null) return;
        if (currentPatient.getAllergy() == null || currentPatient.getAllergy().trim().isEmpty()) return;
        if (medicine == null || medicine.getComponents() == null) return;

        String patientAllergy = normalizeText(currentPatient.getAllergy());
        String medicineComponents = normalizeText(medicine.getComponents());

        String[] allergyWords = patientAllergy.split("[,;\\n]+");

        for (String allergy : allergyWords) {
            allergy = allergy.trim();

            if (allergy.length() >= 3 && medicineComponents.contains(allergy)) {
                allergyWarningLiveData.setValue(
                        "CẢNH BÁO: Bệnh nhân có tiền sử dị ứng với thành phần: " + allergy
                );
                return;
            }
        }
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
