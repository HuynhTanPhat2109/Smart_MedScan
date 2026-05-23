package ntu.tanphat.smart_medscan.ui.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.app.AlarmManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import ntu.tanphat.smart_medscan.utils.AlarmScheduler;
import ntu.tanphat.smart_medscan.utils.NotificationHelper;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.data.models.CareSchedule;
import ntu.tanphat.smart_medscan.data.models.Department;
import ntu.tanphat.smart_medscan.data.models.Floor;
import ntu.tanphat.smart_medscan.data.models.PatientRecord;
import ntu.tanphat.smart_medscan.data.models.Room;
import ntu.tanphat.smart_medscan.ui.adapters.ScheduleAdapter;
import ntu.tanphat.smart_medscan.ui.viewmodels.ScheduleViewModel;

public class TimeFragment extends Fragment {

    private ScheduleViewModel viewModel;

    private RecyclerView rvSchedules;
    private ExtendedFloatingActionButton fabAddSchedule;

    private TextView tvTodayTaskCount, tvPendingTaskCount, tvDoneTaskCount;
    private TextView chipAll, chipPending, chipDone;

    private final List<CareSchedule> allSchedules = new ArrayList<>();
    private final List<CareSchedule> displaySchedules = new ArrayList<>();

    private ScheduleAdapter adapter;
    private String currentFilter = "Tất cả";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_time, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModel.class);

        NotificationHelper.createNotificationChannel(requireContext());
        requestNotificationPermissionIfNeeded();
        requestExactAlarmPermissionIfNeeded();

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        viewModel.fetchSchedules();

        return view;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2001);
            }
        }
    }

    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager =
                    (AlarmManager) requireContext().getSystemService(requireContext().ALARM_SERVICE);

            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Bật quyền nhắc lịch chính xác")
                        .setMessage("Để thông báo lịch chăm sóc đúng giờ hơn, bạn cần cấp quyền đặt báo thức chính xác cho ứng dụng.")
                        .setPositiveButton("Mở cài đặt", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Để sau", null)
                        .show();
            }
        }
    }

    private void initViews(View view) {
        rvSchedules = view.findViewById(R.id.rvSchedules);
        fabAddSchedule = view.findViewById(R.id.fabAddSchedule);

        tvTodayTaskCount = view.findViewById(R.id.tvTodayTaskCount);
        tvPendingTaskCount = view.findViewById(R.id.tvPendingTaskCount);
        tvDoneTaskCount = view.findViewById(R.id.tvDoneTaskCount);

        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipDone = view.findViewById(R.id.chipDone);
    }

    private void setupRecyclerView() {
        adapter = new ScheduleAdapter(displaySchedules, new ScheduleAdapter.OnScheduleActionListener() {
            @Override
            public void onDoneClick(CareSchedule item) {
                showConfirmDoneDialog(item);
            }

            @Override
            public void onDeleteClick(CareSchedule item) {
                showConfirmDeleteDialog(item);
            }
        });

        rvSchedules.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSchedules.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddSchedule.setOnClickListener(v -> showAddScheduleDialog());

        chipAll.setOnClickListener(v -> applyFilter("Tất cả"));
        chipPending.setOnClickListener(v -> applyFilter("Chờ thực hiện"));
        chipDone.setOnClickListener(v -> applyFilter("Đã xong"));
    }

    private void observeViewModel() {
        viewModel.getSchedulesLiveData().observe(getViewLifecycleOwner(), schedules -> {
            allSchedules.clear();
            if (schedules != null) {
                allSchedules.addAll(schedules);
            }
            applyFilter(currentFilter);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter(String filter) {
        currentFilter = filter;

        displaySchedules.clear();

        for (CareSchedule item : allSchedules) {
            if ("Tất cả".equals(filter) || filter.equals(item.getStatus())) {
                displaySchedules.add(item);
            }
        }

        updateStats();
        updateChipUI();
        adapter.notifyDataSetChanged();
    }

    private void updateStats() {
        int pending = 0;
        int done = 0;

        for (CareSchedule item : allSchedules) {
            if ("Đã xong".equals(item.getStatus())) {
                done++;
            } else {
                pending++;
            }
        }

        tvTodayTaskCount.setText(String.valueOf(allSchedules.size()));
        tvPendingTaskCount.setText(String.valueOf(pending));
        tvDoneTaskCount.setText(String.valueOf(done));
    }

    private void updateChipUI() {
        setChipSelected(chipAll, "Tất cả".equals(currentFilter));
        setChipSelected(chipPending, "Chờ thực hiện".equals(currentFilter));
        setChipSelected(chipDone, "Đã xong".equals(currentFilter));
    }

    private void setChipSelected(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_filter_chip_selected : R.drawable.bg_filter_chip_normal);
        chip.setTextColor(selected ? getResources().getColor(R.color.white) : android.graphics.Color.parseColor("#475569"));
    }

    private void showAddScheduleDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_schedule, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        EditText etTime = dialogView.findViewById(R.id.etScheduleTime);
        EditText etTitle = dialogView.findViewById(R.id.etScheduleTitle);

        Spinner spnDepartment = dialogView.findViewById(R.id.spnDepartment);
        Spinner spnFloor = dialogView.findViewById(R.id.spnFloor);
        Spinner spnRoom = dialogView.findViewById(R.id.spnRoom);
        Spinner spnPatient = dialogView.findViewById(R.id.spnPatient);

        TextView chipInject = dialogView.findViewById(R.id.chipInject);
        TextView chipBloodPressure = dialogView.findViewById(R.id.chipBloodPressure);
        TextView chipGlucose = dialogView.findViewById(R.id.chipGlucose);
        TextView chipVital = dialogView.findViewById(R.id.chipVital);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelSchedule);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveSchedule);

        List<Department> departmentList = new ArrayList<>();
        List<Floor> floorList = new ArrayList<>();
        List<Room> roomList = new ArrayList<>();
        List<PatientRecord> patientList = new ArrayList<>();

        etTime.setOnClickListener(v -> showTimePicker(etTime));

        chipInject.setOnClickListener(v -> etTitle.setText("Tiêm thuốc"));
        chipBloodPressure.setOnClickListener(v -> etTitle.setText("Đo huyết áp"));
        chipGlucose.setOnClickListener(v -> etTitle.setText("Kiểm tra đường huyết"));
        chipVital.setOnClickListener(v -> etTitle.setText("Theo dõi sinh hiệu"));

        setupEmptySpinner(spnDepartment, "Đang tải khoa...");
        setupEmptySpinner(spnFloor, "Chọn khoa trước");
        setupEmptySpinner(spnRoom, "Chọn tầng trước");
        setupEmptySpinner(spnPatient, "Chọn phòng trước");

        viewModel.loadDepartments(new ScheduleViewModel.DataCallback<Department>() {
            @Override
            public void onSuccess(List<Department> data) {
                departmentList.clear();
                departmentList.addAll(data);
                setSpinnerData(spnDepartment, getDepartmentNames(departmentList, "Chọn khoa"));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        spnDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position > departmentList.size()) return;

                Department department = departmentList.get(position - 1);

                floorList.clear();
                roomList.clear();
                patientList.clear();

                setupEmptySpinner(spnFloor, "Đang tải tầng...");
                setupEmptySpinner(spnRoom, "Chọn tầng trước");
                setupEmptySpinner(spnPatient, "Chọn phòng trước");

                viewModel.loadFloors(department.getId(), new ScheduleViewModel.DataCallback<Floor>() {
                    @Override
                    public void onSuccess(List<Floor> data) {
                        floorList.clear();
                        floorList.addAll(data);
                        setSpinnerData(spnFloor, getFloorNames(floorList, "Chọn tầng"));
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spnFloor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position > floorList.size()) return;

                Floor floor = floorList.get(position - 1);

                roomList.clear();
                patientList.clear();

                setupEmptySpinner(spnRoom, "Đang tải phòng...");
                setupEmptySpinner(spnPatient, "Chọn phòng trước");

                viewModel.loadRooms(floor.getId(), new ScheduleViewModel.DataCallback<Room>() {
                    @Override
                    public void onSuccess(List<Room> data) {
                        roomList.clear();
                        roomList.addAll(data);
                        setSpinnerData(spnRoom, getRoomNames(roomList, "Chọn phòng"));
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spnRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position > roomList.size()) return;

                Room room = roomList.get(position - 1);

                patientList.clear();
                setupEmptySpinner(spnPatient, "Đang tải bệnh nhân...");

                viewModel.loadPatients(room.getId(), new ScheduleViewModel.DataCallback<PatientRecord>() {
                    @Override
                    public void onSuccess(List<PatientRecord> data) {
                        patientList.clear();
                        patientList.addAll(data);
                        setSpinnerData(spnPatient, getPatientNames(patientList, "Chọn bệnh nhân"));
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String time = etTime.getText().toString().trim();
            String title = etTitle.getText().toString().trim();

            int deptPos = spnDepartment.getSelectedItemPosition();
            int floorPos = spnFloor.getSelectedItemPosition();
            int roomPos = spnRoom.getSelectedItemPosition();
            int patientPos = spnPatient.getSelectedItemPosition();

            if (time.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn thời gian", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập nội dung lịch", Toast.LENGTH_SHORT).show();
                return;
            }

            if (deptPos <= 0 || floorPos <= 0 || roomPos <= 0 || patientPos <= 0) {
                Toast.makeText(getContext(), "Vui lòng chọn đủ khoa, tầng, phòng và bệnh nhân", Toast.LENGTH_SHORT).show();
                return;
            }

            Department department = departmentList.get(deptPos - 1);
            Floor floor = floorList.get(floorPos - 1);
            Room room = roomList.get(roomPos - 1);
            PatientRecord patient = patientList.get(patientPos - 1);

            CareSchedule schedule = new CareSchedule(
                    time,
                    title,
                    "Chờ thực hiện",
                    department.getId(),
                    department.getName(),
                    floor.getId(),
                    floor.getName(),
                    room.getId(),
                    room.getName(),
                    patient.getPatientId(),
                    patient.getName(),
                    viewModel.getCurrentScheduleDate(),
                    System.currentTimeMillis()
            );

            viewModel.addSchedule(schedule);

            boolean alarmScheduled = AlarmScheduler.scheduleCareReminder(
                    requireContext(),
                    time,
                    title,
                    patient.getName(),
                    room.getName()
            );

            if (alarmScheduled) {
                Toast.makeText(
                        getContext(),
                        "Đã lưu lịch và đặt nhắc nhở chính xác lúc " + time,
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        getContext(),
                        "Đã lưu lịch. Chưa đặt được nhắc nhở chính xác, hãy bật quyền Exact Alarm hoặc chọn giờ chưa qua.",
                        Toast.LENGTH_LONG
                ).show();

                requestExactAlarmPermissionIfNeeded();
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showConfirmDoneDialog(CareSchedule item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận hoàn thành")
                .setMessage("Bạn muốn đánh dấu lịch \"" + item.getTitle() + "\" của bệnh nhân "
                        + item.getPatientName() + " là đã xong?")
                .setPositiveButton("Đã xong", (dialog, which) -> {
                    viewModel.markScheduleDone(item.getId());
                    Toast.makeText(getContext(), "Đã hoàn thành lịch chăm sóc", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showConfirmDeleteDialog(CareSchedule item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa lịch chăm sóc")
                .setMessage("Bạn có chắc muốn xóa lịch này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteSchedule(item.getId());
                    Toast.makeText(getContext(), "Đã xóa lịch", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    target.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    private void setupEmptySpinner(Spinner spinner, String text) {
        List<String> list = new ArrayList<>();
        list.add(text);
        setSpinnerData(spinner, list);
    }

    private void setSpinnerData(Spinner spinner, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                data
        );
        spinner.setAdapter(adapter);
    }

    private List<String> getDepartmentNames(List<Department> list, String firstText) {
        List<String> result = new ArrayList<>();
        result.add(firstText);
        for (Department item : list) result.add(item.getName());
        return result;
    }

    private List<String> getFloorNames(List<Floor> list, String firstText) {
        List<String> result = new ArrayList<>();
        result.add(firstText);
        for (Floor item : list) result.add(item.getName());
        return result;
    }

    private List<String> getRoomNames(List<Room> list, String firstText) {
        List<String> result = new ArrayList<>();
        result.add(firstText);
        for (Room item : list) result.add(item.getName());
        return result;
    }

    private List<String> getPatientNames(List<PatientRecord> list, String firstText) {
        List<String> result = new ArrayList<>();
        result.add(firstText);
        for (PatientRecord item : list) result.add(item.getName());
        return result;
    }
}