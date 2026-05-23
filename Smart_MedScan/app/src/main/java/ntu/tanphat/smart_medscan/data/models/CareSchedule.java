package ntu.tanphat.smart_medscan.data.models;

public class CareSchedule {
    private String id;
    private String time;
    private String scheduleDate;
    private String title;
    private String status;

    private String departmentId;
    private String departmentName;

    private String floorId;
    private String floorName;

    private String roomId;
    private String roomName;

    private String patientId;
    private String patientName;

    private long createdAt;

    public CareSchedule() {
    }

    public CareSchedule(String time,
                        String title,
                        String status,
                        String departmentId,
                        String departmentName,
                        String floorId,
                        String floorName,
                        String roomId,
                        String roomName,
                        String patientId,
                        String patientName,
                        String scheduleDate,
                        long createdAt) {
        this.time = time;
        this.title = title;
        this.status = status;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.floorId = floorId;
        this.floorName = floorName;
        this.roomId = roomId;
        this.roomName = roomName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.scheduleDate = scheduleDate;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getFloorId() { return floorId; }
    public void setFloorId(String floorId) { this.floorId = floorId; }

    public String getFloorName() { return floorName; }
    public void setFloorName(String floorName) { this.floorName = floorName; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}