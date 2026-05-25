package ntu.tanphat.smart_medscan.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.data.models.Medicine;
import ntu.tanphat.smart_medscan.ui.viewmodels.RecordViewModel;
import ntu.tanphat.smart_medscan.utils.MedicineAnalyzer;

public class ScanFragment extends Fragment {

    private static final String TAG = "ScanFragment";
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    
    private PreviewView previewView;
    private TextView tvDetectedText;
    private RecordViewModel viewModel;
    private ExecutorService cameraExecutor;
    private View btnBackScan;
    private View scanLine;
    private View btnChooseImage;
    private View btnSelectPatient;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private boolean isProcessingResult = false;
    private boolean isNavigatingToRecords = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        
        previewView = view.findViewById(R.id.previewView);
        tvDetectedText = view.findViewById(R.id.tvDetectedText);
        scanLine = view.findViewById(R.id.scanLine);
        btnBackScan = view.findViewById(R.id.btnBackScan);
        btnChooseImage = view.findViewById(R.id.btnChooseImage);
        btnSelectPatient = view.findViewById(R.id.btnSelectPatient);

        setupImagePicker();
        setupScanActions();
        startScanLineAnimation();

        
        // Quan trọng: Sử dụng requireActivity() để dùng chung ViewModel với RecordFragment
        viewModel = new ViewModelProvider(requireActivity()).get(RecordViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();

        observeViewModel();


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }

        return view;
    }

    private void observeViewModel() {
        // Lắng nghe kết quả khi tìm thấy thuốc trong DB
        viewModel.getScanResultLiveData().observe(getViewLifecycleOwner(), medicine -> {
            if (medicine != null && isProcessingResult) {
                showMedicineInfoDialog(medicine);
            }
        });

        // Nhận thông báo lỗi (không tìm thấy thuốc)
        viewModel.getScanErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && isProcessingResult) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                tvDetectedText.setText("Đưa tên thuốc vào khung quét để thử lại...");
                tvDetectedText.postDelayed(() -> isProcessingResult = false, 1500);
            }
        });
    }

    private void showMedicineInfoDialog(Medicine medicine) {
        String allergyWarning = viewModel.getAllergyWarningLiveData().getValue();
        
        StringBuilder message = new StringBuilder();
        message.append("🧪 Thành phần: ").append(medicine.getComponents()).append("\n\n");
        message.append("📋 Chỉ định: ").append(medicine.getIndications()).append("\n\n");
        message.append("⚖️ Liều lượng: ").append(medicine.getDosage()).append("\n\n");
        message.append("💊 Cách dùng: ").append(medicine.getUsage()).append("\n\n");
        message.append("🚫 Chống chỉ định: ").append(medicine.getContraindications()).append("\n\n");
        message.append(allergyWarning != null ? "⚠️ " + allergyWarning : "✅ An toàn cho bệnh nhân.");

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("🔍 Thông tin thuốc: " + medicine.getName())
                .setMessage(message.toString())
                .setPositiveButton("Đã hiểu", (dialog, which) -> {
                    isProcessingResult = false;
                    dialog.dismiss();
                })
                .setCancelable(false);
        
        if (allergyWarning != null) {
            builder.setIcon(android.R.drawable.ic_dialog_alert);
        }

        builder.show();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new MedicineAnalyzer(text -> {
                    if (isAdded() && !isProcessingResult && !isNavigatingToRecords) {
                        requireActivity().runOnUiThread(() -> {
                            tvDetectedText.setText("Đang phân tích thuốc...\n" + text);
                            isProcessingResult = true;
                            viewModel.scanMedicineFromText(text);
                        });
                    }
                }));

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera Error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(getContext(), "Cần quyền camera để quét thuốc", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get();
            cameraProvider.unbindAll();
        } catch (Exception ignored) {
        }

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    private void setupScanActions() {
        btnBackScan.setOnClickListener(v -> {
            if (getActivity() instanceof ntu.tanphat.smart_medscan.ui.activities.MainActivity) {
                ((ntu.tanphat.smart_medscan.ui.activities.MainActivity) getActivity())
                        .selectBottomNavTab(R.id.nav_home);
            }
        });

        btnChooseImage.setOnClickListener(v -> {
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        btnSelectPatient.setOnClickListener(v -> {
            isNavigatingToRecords = true;
            isProcessingResult = false;

            Toast.makeText(
                    getContext(),
                    "Chọn bệnh nhân cần kiểm tra thuốc",
                    Toast.LENGTH_SHORT
            ).show();

            if (getActivity() instanceof ntu.tanphat.smart_medscan.ui.activities.MainActivity) {
                ((ntu.tanphat.smart_medscan.ui.activities.MainActivity) getActivity())
                        .selectBottomNavTab(R.id.nav_records);
            }
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        analyzeImageFromGallery(uri);
                    } else {
                        Toast.makeText(getContext(), "Bạn chưa chọn ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void analyzeImageFromGallery(Uri uri) {
        try {
            Toast.makeText(getContext(), "Đã chọn ảnh, đang phân tích...", Toast.LENGTH_SHORT).show();

            InputImage image = InputImage.fromFilePath(requireContext(), uri);

            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .addOnSuccessListener(visionText -> {
                        String text = visionText.getText();

                        if (text == null || text.trim().isEmpty()) {
                            Toast.makeText(getContext(), "Không nhận diện được chữ trong ảnh", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        tvDetectedText.setText("Đang phân tích ảnh...\n" + text);
                        isProcessingResult = true;
                        viewModel.scanMedicineFromText(text);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Không thể mở ảnh này", Toast.LENGTH_SHORT).show();
        }
    }

    private void startScanLineAnimation() {
        if (scanLine == null) return;

        ObjectAnimator animator = ObjectAnimator.ofFloat(scanLine, "translationY", -165f, 165f);
        animator.setDuration(1800);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();
    }
}
