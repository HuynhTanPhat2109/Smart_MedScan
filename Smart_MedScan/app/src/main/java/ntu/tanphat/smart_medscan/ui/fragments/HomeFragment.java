package ntu.tanphat.smart_medscan.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.data.models.MedicalTask;
import ntu.tanphat.smart_medscan.ui.adapters.TaskAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private TextView tvGreeting, tvUserName;
    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private List<MedicalTask> priorityTasks;
    private TextView tvPatientCount, tvTaskCount;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        updateGreeting();
        loadUserData();
        setupRecyclerView();
        loadDummyData();

        return view;
    }
    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);
        rvTasks = view.findViewById(R.id.rvPriorityTasks);
        tvPatientCount = view.findViewById(R.id.tvPatientCount);
        tvTaskCount = view.findViewById(R.id.tvTaskCount);
    }
    private void setupRecyclerView() {
        priorityTasks = new ArrayList<>();
        adapter = new TaskAdapter(priorityTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(adapter);

        // QUAN TRỌNG: Vô hiệu hóa nested scrolling để cuộn mượt mà hơn
        // bên trong NestedScrollView của fragment_home.xml
        rvTasks.setNestedScrollingEnabled(false);
    }
    private void loadDummyData() {
        // Giả lập dữ liệu từ Database đổ về
        priorityTasks.add(new MedicalTask("Nguyễn Văn A", "302", "Theo dõi sinh hiệu", "08:30"));
        priorityTasks.add(new MedicalTask("Trần Thị B", "105", "Tiêm kháng sinh", "09:00"));

        tvPatientCount.setText("12"); // Tổng bệnh nhân trong collection 'patients'
        tvTaskCount.setText("05");    // Tổng tasks chưa xong trong 'tasks'

        adapter.notifyDataSetChanged();
    }
    /**
     * Tự động cập nhật lời chào theo thời gian thực (Tính năng senior)
     */
    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 0 && hour < 12) {
            greeting = "Chào buổi sáng,";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Chào buổi chiều,";
        } else {
            greeting = "Chào buổi tối,";
        }
        tvGreeting.setText(greeting);
    }

    /**
     * Sau này sẽ tích hợp lấy dữ liệu từ Firestore
     */
    private void loadUserData() {
        // Giả lập dữ liệu, sau này Phát sẽ gọi FirebaseUser ở đây
        tvUserName.setText("Điều dưỡng Phát");
    }
}