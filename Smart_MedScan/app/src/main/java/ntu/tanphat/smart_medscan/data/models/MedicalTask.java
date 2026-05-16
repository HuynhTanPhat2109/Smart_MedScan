package ntu.tanphat.smart_medscan.data.models;

public class MedicalTask {
    private String id;
    private String patientName;
    private String room;
    private String taskContent;
    private String time;
    private boolean isCompleted;

    // Constructor trống cho Firebase
    public MedicalTask() {}

    public MedicalTask(String patientName, String room, String taskContent, String time) {
        this.patientName = patientName;
        this.room = room;
        this.taskContent = taskContent;
        this.time = time;
        this.isCompleted = false;
    }

    // Getter và Setter...
    public String getPatientName() { return patientName; }
    public String getRoom() { return room; }
    public String getTaskContent() { return taskContent; }
    public String getTime() { return time; }
}
