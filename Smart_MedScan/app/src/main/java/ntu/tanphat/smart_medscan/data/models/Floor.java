package ntu.tanphat.smart_medscan.data.models;

public class Floor {
    @com.google.firebase.firestore.DocumentId
    private String id;
    private String name;
    private String departmentId;

    public Floor() {}

    public Floor(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Floor(String id, String name, String departmentId) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
}
