package ntu.tanphat.smart_medscan.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.data.models.CareSchedule;
import ntu.tanphat.smart_medscan.data.models.MedicalTask;
import ntu.tanphat.smart_medscan.ui.adapters.TaskAdapter;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvUserName;
    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private List<MedicalTask> priorityTasks;

    private TextView tvPatientCount, tvTaskCount;

    private View cvScan, cvRecords, cvSchedule, cvProfile;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        /*
         * Lưu ý:
         * Dòng seed thuốc chỉ nên chạy 1 lần lúc bạn cần tạo dữ liệu mẫu.
         * Nếu Firestore đã có thuốc rồi thì nên comment lại để tránh app gọi seed mỗi lần vào Home.
         */
        // new MedicineSeeder().seedMedicines();

        initViews(view);
        updateGreeting();
        setupRecyclerView();
        setupQuickActions();
        loadUserDataFromFirebase();
        loadHomeStatistics();
        animateHomeViews(view);

        return view;
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);

        rvTasks = view.findViewById(R.id.rvPriorityTasks);

        tvPatientCount = view.findViewById(R.id.tvPatientCount);
        tvTaskCount = view.findViewById(R.id.tvTaskCount);

        cvScan = view.findViewById(R.id.cvScan);
        cvRecords = view.findViewById(R.id.cvRecords);
        cvSchedule = view.findViewById(R.id.cvSchedule);
        cvProfile = view.findViewById(R.id.cvProfile);
    }

    private void setupRecyclerView() {
        priorityTasks = new ArrayList<>();
        adapter = new TaskAdapter(priorityTasks);

        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(adapter);
        rvTasks.setNestedScrollingEnabled(false);
    }

    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Chào buổi sáng,";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Chào buổi chiều,";
        } else {
            greeting = "Chào buổi tối,";
        }

        tvGreeting.setText(greeting);
    }

    private void loadUserDataFromFirebase() {
        if (auth.getCurrentUser() == null) {
            tvUserName.setText("Điều dưỡng");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");

                        if (fullName != null && !fullName.trim().isEmpty()) {
                            tvUserName.setText(fullName);
                        } else {
                            tvUserName.setText("Điều dưỡng");
                        }
                    } else {
                        tvUserName.setText("Điều dưỡng");
                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText("Điều dưỡng");
                });
    }

    private void loadHomeStatistics() {
        loadPatientCount();
        loadTodayPendingSchedules();
    }

    private void loadPatientCount() {
        db.collection("patients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalPatients = queryDocumentSnapshots.size();
                    tvPatientCount.setText(String.valueOf(totalPatients));
                })
                .addOnFailureListener(e -> {
                    tvPatientCount.setText("0");
                    Toast.makeText(getContext(), "Lỗi tải số bệnh nhân", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTodayPendingSchedules() {
        String today = getTodayDate();

        db.collection("schedules")
                .whereEqualTo("scheduleDate", today)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CareSchedule> pendingSchedules = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CareSchedule schedule = doc.toObject(CareSchedule.class);
                        schedule.setId(doc.getId());

                        if (!"Đã xong".equals(schedule.getStatus())) {
                            pendingSchedules.add(schedule);
                        }
                    }

                    Collections.sort(pendingSchedules, new Comparator<CareSchedule>() {
                        @Override
                        public int compare(CareSchedule s1, CareSchedule s2) {
                            String t1 = s1.getTime() == null ? "" : s1.getTime();
                            String t2 = s2.getTime() == null ? "" : s2.getTime();
                            return t1.compareTo(t2);
                        }
                    });

                    tvTaskCount.setText(String.valueOf(pendingSchedules.size()));
                    bindPriorityTasks(pendingSchedules);
                })
                .addOnFailureListener(e -> {
                    tvTaskCount.setText("0");
                    priorityTasks.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Lỗi tải lịch chăm sóc hôm nay", Toast.LENGTH_SHORT).show();
                });
    }

    private void bindPriorityTasks(List<CareSchedule> schedules) {
        priorityTasks.clear();

        int limit = Math.min(schedules.size(), 3);

        for (int i = 0; i < limit; i++) {
            CareSchedule schedule = schedules.get(i);

            String patientName = safeText(schedule.getPatientName(), "Bệnh nhân");
            String roomName = safeText(schedule.getRoomName(), "Chưa có phòng");
            String title = safeText(schedule.getTitle(), "Lịch chăm sóc");
            String time = safeText(schedule.getTime(), "--:--");

            priorityTasks.add(new MedicalTask(
                    patientName,
                    roomName,
                    title,
                    time
            ));
        }

        if (priorityTasks.isEmpty()) {
            priorityTasks.add(new MedicalTask(
                    "Hôm nay",
                    "Smart MedScan",
                    "Không có lịch chăm sóc đang chờ",
                    "--:--"
            ));
        }

        adapter.notifyDataSetChanged();
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private void setupQuickActions() {
        cvScan.setOnClickListener(v -> triggerTabChange(R.id.placeholder));
        cvRecords.setOnClickListener(v -> triggerTabChange(R.id.nav_records));
        cvSchedule.setOnClickListener(v -> triggerTabChange(R.id.nav_time));
        cvProfile.setOnClickListener(v -> triggerTabChange(R.id.nav_profile));
    }

    private void triggerTabChange(int menuId) {
        if (getActivity() instanceof ntu.tanphat.smart_medscan.ui.activities.MainActivity) {
            ((ntu.tanphat.smart_medscan.ui.activities.MainActivity) getActivity())
                    .selectBottomNavTab(menuId);
        }
    }

    private void animateHomeViews(View root) {
        int[] viewIds = {
                R.id.topHeader,
                R.id.mainHeroCard,
                R.id.searchCard,
                R.id.actionGrid,
                R.id.statsContainer,
                R.id.taskHeader,
                R.id.rvPriorityTasks
        };

        for (int i = 0; i < viewIds.length; i++) {
            View v = root.findViewById(viewIds[i]);

            if (v != null) {
                v.setAlpha(0f);
                v.setTranslationY(35f);
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(450)
                        .setStartDelay(i * 70L)
                        .start();
            }
        }
    }
}