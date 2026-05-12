package ntu.tanphat.smart_medscan.data.models;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String department;
    private String role;

    public User() {
    }

    public User(String uid, String fullName, String email, String department, String role) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
