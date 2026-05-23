package ntu.tanphat.smart_medscan.utils;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MedicineAnalyzer implements ImageAnalysis.Analyzer {

    public interface OnMedicineDetectedListener {
        void onDetected(String text);
    }

    private final OnMedicineDetectedListener listener;
    private final TextRecognizer recognizer;

    private long lastAnalyzeTime = 0;
    private static final long ANALYZE_DELAY_MS = 1500;

    public MedicineAnalyzer(OnMedicineDetectedListener listener) {
        this.listener = listener;
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    @Override
    @SuppressLint("UnsafeOptInUsageError")
    public void analyze(@NonNull ImageProxy imageProxy) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastAnalyzeTime < ANALYZE_DELAY_MS) {
            imageProxy.close();
            return;
        }

        lastAnalyzeTime = currentTime;

        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String result = extractUsefulText(visionText);

                        if (!result.isEmpty()) {
                            listener.onDetected(result);
                        }

                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private String extractUsefulText(Text visionText) {
        StringBuilder builder = new StringBuilder();

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String text = line.getText().trim();

                if (isUsefulLine(text)) {
                    builder.append(text).append("\n");
                }
            }
        }

        return builder.toString().trim();
    }

    private boolean isUsefulLine(String text) {
        if (text == null) return false;

        text = text.trim();

        if (text.length() < 3) return false;

        // Bỏ dòng chỉ toàn số, thường là mã lô / mã vạch / ngày
        if (text.matches("^[0-9\\s./:-]+$")) return false;

        // Bỏ một số dòng phụ hay gặp trên bao bì
        String lower = text.toLowerCase();

        if (lower.contains("nsx")) return false;
        if (lower.contains("hạn dùng")) return false;
        if (lower.contains("han dung")) return false;
        if (lower.contains("số lô")) return false;
        if (lower.contains("so lo")) return false;
        if (lower.contains("lot")) return false;
        if (lower.contains("exp")) return false;
        if (lower.contains("mfg")) return false;
        if (lower.contains("barcode")) return false;

        return true;
    }
}