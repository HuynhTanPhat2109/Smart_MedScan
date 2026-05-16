package ntu.tanphat.smart_medscan.data.models;

import com.google.firebase.firestore.PropertyName;

public class PatientRecord {
    @com.google.firebase.firestore.DocumentId
    private String patientId;
    private String name;
    private String age;
    private String bedNumber;
    private String diagnosis; // chẩn đoán
    private String allergy; // dị ứng
    private String status;
    private String department;
    private String floor;
    private String room;

    public PatientRecord() {} // Cần cho Firebase

    public PatientRecord(String patientId, String name, String age, String bedNumber, String diagnosis, String allergy, String status, String department, String floor, String room) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.bedNumber = bedNumber;
        this.diagnosis = diagnosis;
        this.allergy = allergy;
        this.status = status;
        this.department = department;
        this.floor = floor;
        this.room = room;
    }

    @PropertyName("patientId") public String getPatientId() { return patientId; }
    @PropertyName("patientId") public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getAllergy() {
        return allergy;
    }

    public String getStatus() {
        return status;
    }

    public String getDepartment() {
        return department;
    }

    public String getFloor() {
        return floor;
    }

    public String getRoom() {
        return room;
    }
}
