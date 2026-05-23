package ntu.tanphat.smart_medscan.data.firebase;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MedicineSeeder {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void seedMedicines() {
        addMedicine(
                "Panadol Extra",
                "Paracetamol 500mg, Caffeine 65mg",
                "Giảm đau, hạ sốt",
                "Người lớn: 1-2 viên/lần, cách 4-6 giờ. Không quá 8 viên/ngày.",
                "Uống sau ăn với nước lọc.",
                "Dị ứng paracetamol, suy gan nặng, suy thận nặng."
        );

        addMedicine(
                "Efferalgan 500mg",
                "Paracetamol 500mg",
                "Giảm đau, hạ sốt",
                "Người lớn: 1 viên/lần, cách nhau ít nhất 4-6 giờ.",
                "Hòa tan viên sủi trong nước trước khi uống.",
                "Dị ứng paracetamol, bệnh gan nặng."
        );


        addMedicine(
                "Amoxicillin 500mg",
                "Amoxicillin 500mg",
                "Điều trị nhiễm khuẩn do vi khuẩn nhạy cảm.",
                "Theo chỉ định của bác sĩ.",
                "Uống trước hoặc sau ăn tùy hướng dẫn.",
                "Dị ứng nhóm penicillin, cephalosporin."
        );

        addMedicine(
                "Augmentin 625mg",
                "Amoxicillin 500mg, Clavulanic acid 125mg",
                "Điều trị nhiễm khuẩn đường hô hấp, tiết niệu, da mô mềm.",
                "Theo chỉ định của bác sĩ.",
                "Uống vào đầu bữa ăn để giảm kích ứng tiêu hóa.",
                "Dị ứng penicillin, tiền sử vàng da do amoxicillin/clavulanate."
        );

        addMedicine(
                "Cefixim 200mg",
                "Cefixim 200mg",
                "Điều trị nhiễm khuẩn hô hấp, tiết niệu, tai mũi họng.",
                "Theo chỉ định của bác sĩ.",
                "Uống với nước, có thể dùng cùng thức ăn.",
                "Dị ứng cephalosporin."
        );

        addMedicine(
                "Cefuroxim 500mg",
                "Cefuroxim axetil 500mg",
                "Điều trị nhiễm khuẩn hô hấp, tiết niệu, da mô mềm.",
                "Theo chỉ định của bác sĩ.",
                "Uống sau ăn để tăng hấp thu.",
                "Dị ứng cephalosporin."
        );

        addMedicine(
                "Azithromycin 500mg",
                "Azithromycin 500mg",
                "Điều trị nhiễm khuẩn đường hô hấp, da mô mềm.",
                "Theo chỉ định của bác sĩ.",
                "Uống trước ăn 1 giờ hoặc sau ăn 2 giờ.",
                "Dị ứng macrolid, bệnh gan nặng."
        );

        addMedicine(
                "Clarithromycin 500mg",
                "Clarithromycin 500mg",
                "Điều trị nhiễm khuẩn hô hấp, da mô mềm.",
                "Theo chỉ định của bác sĩ.",
                "Uống với nước, có thể dùng cùng thức ăn.",
                "Dị ứng macrolid."
        );

        addMedicine(
                "Metronidazole 250mg",
                "Metronidazole 250mg",
                "Điều trị nhiễm khuẩn kỵ khí, nhiễm ký sinh trùng.",
                "Theo chỉ định của bác sĩ.",
                "Uống sau ăn.",
                "Dị ứng metronidazole, không dùng rượu khi sử dụng thuốc."
        );

        addMedicine(
                "Omeprazole 20mg",
                "Omeprazole 20mg",
                "Điều trị viêm loét dạ dày, trào ngược dạ dày thực quản.",
                "Thường dùng 1 viên/ngày hoặc theo chỉ định.",
                "Uống trước ăn sáng 30 phút.",
                "Dị ứng omeprazole hoặc thuốc nhóm PPI."
        );

        addMedicine(
                "Esomeprazole 40mg",
                "Esomeprazole 40mg",
                "Điều trị trào ngược dạ dày, viêm loét dạ dày.",
                "Theo chỉ định của bác sĩ.",
                "Uống trước ăn.",
                "Dị ứng esomeprazole hoặc nhóm PPI."
        );

        addMedicine(
                "Domperidone 10mg",
                "Domperidone 10mg",
                "Giảm buồn nôn, đầy bụng, khó tiêu.",
                "Theo chỉ định của bác sĩ.",
                "Uống trước ăn 15-30 phút.",
                "Bệnh tim mạch nặng, rối loạn nhịp tim, dị ứng domperidone."
        );

        addMedicine(
                "Loperamide 2mg",
                "Loperamide 2mg",
                "Điều trị tiêu chảy cấp không do nhiễm khuẩn.",
                "Theo hướng dẫn sử dụng hoặc chỉ định bác sĩ.",
                "Uống với nước sau mỗi lần đi ngoài lỏng.",
                "Tiêu chảy do nhiễm khuẩn, sốt cao, phân có máu."
        );

        addMedicine(
                "ORS",
                "Oral Rehydration Salts",
                "Bù nước và điện giải khi tiêu chảy, nôn ói.",
                "Pha đúng lượng nước theo hướng dẫn trên gói.",
                "Uống từng ngụm nhỏ, nhiều lần.",
                "Không pha sai tỷ lệ nước, thận trọng ở bệnh nhân suy thận."
        );

        addMedicine(
                "Cetirizine 10mg",
                "Cetirizine 10mg",
                "Điều trị dị ứng, viêm mũi dị ứng, mề đay.",
                "Người lớn thường dùng 1 viên/ngày.",
                "Uống với nước, có thể gây buồn ngủ.",
                "Dị ứng cetirizine, thận trọng khi suy thận."
        );

        addMedicine(
                "Loratadine 10mg",
                "Loratadine 10mg",
                "Điều trị viêm mũi dị ứng, nổi mề đay.",
                "Người lớn thường dùng 1 viên/ngày.",
                "Uống với nước.",
                "Dị ứng loratadine."
        );

        addMedicine(
                "Salbutamol",
                "Salbutamol",
                "Giãn phế quản, hỗ trợ điều trị hen phế quản, COPD.",
                "Theo chỉ định của bác sĩ.",
                "Dùng theo dạng bào chế: viên, siro hoặc khí dung.",
                "Dị ứng salbutamol, thận trọng bệnh tim mạch."
        );

        addMedicine(
                "Amlodipine 5mg",
                "Amlodipine 5mg",
                "Điều trị tăng huyết áp, đau thắt ngực.",
                "Theo chỉ định của bác sĩ.",
                "Uống 1 lần/ngày vào cùng thời điểm.",
                "Dị ứng amlodipine, hạ huyết áp nặng."
        );

        addMedicine(
                "Losartan 50mg",
                "Losartan potassium 50mg",
                "Điều trị tăng huyết áp, bảo vệ thận ở bệnh nhân đái tháo đường.",
                "Theo chỉ định của bác sĩ.",
                "Uống với nước, có thể dùng cùng hoặc không cùng thức ăn.",
                "Phụ nữ có thai, dị ứng losartan."
        );

        addMedicine(
                "Metformin 500mg",
                "Metformin hydrochloride 500mg",
                "Điều trị đái tháo đường type 2.",
                "Theo chỉ định của bác sĩ.",
                "Uống trong hoặc sau bữa ăn.",
                "Suy thận nặng, nhiễm toan chuyển hóa, dị ứng metformin."
        );

        addMedicine(
                "Aspirin 81mg",
                "Acetylsalicylic acid 81mg",
                "Dự phòng huyết khối, hỗ trợ bệnh lý tim mạch.",
                "Theo chỉ định của bác sĩ.",
                "Uống sau ăn.",
                "Dị ứng aspirin, loét dạ dày tiến triển, nguy cơ chảy máu."
        );

        addMedicine(
                "Ibuprofen 400mg",
                "Ibuprofen 400mg",
                "Giảm đau, hạ sốt, kháng viêm.",
                "Theo hướng dẫn hoặc chỉ định bác sĩ.",
                "Uống sau ăn để giảm kích ứng dạ dày.",
                "Loét dạ dày, suy thận nặng, dị ứng NSAID."
        );

        addMedicine(
                "Diclofenac 50mg",
                "Diclofenac sodium 50mg",
                "Giảm đau, kháng viêm trong đau cơ xương khớp.",
                "Theo chỉ định của bác sĩ.",
                "Uống sau ăn.",
                "Loét dạ dày, dị ứng NSAID, suy gan thận nặng."
        );

        addMedicine(
                "Vitamin C 500mg",
                "Acid ascorbic 500mg",
                "Bổ sung vitamin C, hỗ trợ tăng sức đề kháng.",
                "Theo hướng dẫn sử dụng.",
                "Uống sau ăn.",
                "Thận trọng ở người sỏi thận, dị ứng thành phần thuốc."
        );
    }

    private void addMedicine(String name,
                             String components,
                             String indications,
                             String dosage,
                             String usage,
                             String contraindications) {

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("components", components);
        data.put("indications", indications);
        data.put("dosage", dosage);
        data.put("usage", usage);
        data.put("contraindications", contraindications);

        db.collection("medicines")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        db.collection("medicines").add(data);
                    }
                });
    }
}