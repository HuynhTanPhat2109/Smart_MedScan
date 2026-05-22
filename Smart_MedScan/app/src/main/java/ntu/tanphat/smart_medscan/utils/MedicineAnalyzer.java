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

    public MedicineAnalyzer(OnMedicineDetectedListener listener) {
        this.listener = listener;
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    @Override
    @SuppressLint("UnsafeOptInUsageError")
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            
            recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    processText(visionText);
                    imageProxy.close();
                })
                .addOnFailureListener(e -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private void processText(Text visionText) {
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String text = line.getText().trim();
                
                // Logic lọc: > 3 ký tự và có chữ viết hoa
                if (text.length() > 3 && hasUppercase(text)) {
                    listener.onDetected(text);
                }
            }
        }
    }

    private boolean hasUppercase(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
