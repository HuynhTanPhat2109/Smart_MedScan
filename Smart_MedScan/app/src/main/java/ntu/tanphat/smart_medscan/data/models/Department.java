package ntu.tanphat.smart_medscan.data.models;

import com.google.firebase.firestore.DocumentId;

public class Department {
    @DocumentId
    private String id;
    private String name;
    private long count; // Sử dụng long để khớp với FieldValue.increment

    public Department() {}

    public Department(String id, String name, long count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
