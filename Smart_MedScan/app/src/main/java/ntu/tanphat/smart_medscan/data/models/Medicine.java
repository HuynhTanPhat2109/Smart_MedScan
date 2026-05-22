package ntu.tanphat.smart_medscan.data.models;

import com.google.firebase.firestore.DocumentId;

public class Medicine {
    @DocumentId
    private String id;
    private String name;
    private String description;
    private String components;      // Thành phần hoạt chất
    private String indications;     // Chỉ định
    private String dosage;          // Liều lượng
    private String usage;           // Cách dùng
    private String contraindications; // Chống chỉ định

    public Medicine() {}

    public Medicine(String name, String description, String components, String indications, String dosage, String usage, String contraindications) {
        this.name = name;
        this.description = description;
        this.components = components;
        this.indications = indications;
        this.dosage = dosage;
        this.usage = usage;
        this.contraindications = contraindications;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getComponents() { return components; }
    public void setComponents(String components) { this.components = components; }
    public String getIndications() { return indications; }
    public void setIndications(String indications) { this.indications = indications; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getUsage() { return usage; }
    public void setUsage(String usage) { this.usage = usage; }
    public String getContraindications() { return contraindications; }
    public void setContraindications(String contraindications) { this.contraindications = contraindications; }
}
