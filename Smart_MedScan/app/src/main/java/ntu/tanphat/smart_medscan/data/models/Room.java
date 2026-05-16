package ntu.tanphat.smart_medscan.data.models;

public class Room {
    @com.google.firebase.firestore.DocumentId
    private String id;
    private String name;
    private String floorId;

    public Room() {}

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Room(String id, String name, String floorId) {
        this.id = id;
        this.name = name;
        this.floorId = floorId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFloorId() { return floorId; }
    public void setFloorId(String floorId) { this.floorId = floorId; }
}
