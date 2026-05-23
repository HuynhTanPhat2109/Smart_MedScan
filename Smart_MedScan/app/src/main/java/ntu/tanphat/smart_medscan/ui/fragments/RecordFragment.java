package ntu.tanphat.smart_medscan.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.data.models.Department;
import ntu.tanphat.smart_medscan.data.models.Floor;
import ntu.tanphat.smart_medscan.data.models.PatientRecord;
import ntu.tanphat.smart_medscan.data.models.Room;
import ntu.tanphat.smart_medscan.ui.viewmodels.RecordViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class RecordFragment extends Fragment {

    private RecordViewModel viewModel;
    private RecyclerView rvRecords;
    private EditText etSearch;
    private ExtendedFloatingActionButton fabAdd;
    private ProgressBar pbLoading;
    private ImageButton btnBack;
    private TextView tvHeaderTitle;
    private View btnFilter;
    private RecordAdapter adapter;
    private final List<Object> displayList = new ArrayList<>();

    private String selectedDeptId, selectedDeptName;
    private String selectedFloorId, selectedFloorName;
    private String selectedRoomId, selectedRoomName;
    private String currentStatusFilter = "Tất cả";

    public enum ViewState { DEPARTMENTS, FLOORS, ROOMS, PATIENTS }
    private ViewState currentState = ViewState.DEPARTMENTS;
    private View loadingContainer;

    private TextView tvTotalRecordCount, tvTotalRecordLabel;
    private TextView tvRecordStateLabel, tvCurrentLocation, tvCurrentLocationSub, tvCurrentFilter;

    public interface OnRecordClickListener {
        void onItemClick(Object item);
        void onOptionsClick(View view, Object item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(RecordViewModel.class);

        rvRecords = view.findViewById(R.id.rvMedicalRecords);
        etSearch = view.findViewById(R.id.etSearchRecord);
        fabAdd = view.findViewById(R.id.fabAddPatient);
        pbLoading = view.findViewById(R.id.pbLoading);
        btnBack = view.findViewById(R.id.btnBack);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        btnFilter = view.findViewById(R.id.btnFilter);

        tvTotalRecordCount = view.findViewById(R.id.tvTotalRecordCount);
        tvTotalRecordLabel = view.findViewById(R.id.tvTotalRecordLabel);
        tvRecordStateLabel = view.findViewById(R.id.tvRecordStateLabel);
        tvCurrentLocation = view.findViewById(R.id.tvCurrentLocation);
        tvCurrentLocationSub = view.findViewById(R.id.tvCurrentLocationSub);
        tvCurrentFilter = view.findViewById(R.id.tvCurrentFilter);

        loadingContainer = view.findViewById(R.id.loadingContainer);

        setupRecyclerView();
        setupSearch();
        setupListeners();
        observeViewModel();

        updateViewState(ViewState.DEPARTMENTS);
        return view;
    }

    private void setupRecyclerView() {
        adapter = new RecordAdapter(displayList, new OnRecordClickListener() {
            @Override
            public void onItemClick(Object item) {
                if (item instanceof Department) {
                    Department d = (Department) item;
                    selectedDeptId = d.getId(); selectedDeptName = d.getName();
                    updateViewState(ViewState.FLOORS);
                } else if (item instanceof Floor) {
                    Floor f = (Floor) item;
                    selectedFloorId = f.getId(); selectedFloorName = f.getName();
                    updateViewState(ViewState.ROOMS);
                } else if (item instanceof Room) {
                    Room r = (Room) item;
                    selectedRoomId = r.getId(); selectedRoomName = r.getName();
                    updateViewState(ViewState.PATIENTS);
                } else if (item instanceof PatientRecord) {
                    PatientRecord p = (PatientRecord) item;
                    viewModel.setCurrentPatient(p);
                    showPatientDetailBottomSheet(p);
                }
            }

            @Override
            public void onOptionsClick(View view, Object item) {
                showPopupMenu(view, item);
            }
        });
        rvRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecords.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getDisplayList().observe(getViewLifecycleOwner(), items -> {
            displayList.clear();
            displayList.addAll(items);
            adapter.notifyDataSetChanged();
            updateOverviewCard();
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingContainer != null) {
                loadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void showPatientDetailBottomSheet(PatientRecord patient) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_patient_detail, null);
        dialog.setContentView(view);

        TextView tvInitial = view.findViewById(R.id.tvDetailInitial);
        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvAge = view.findViewById(R.id.tvDetailAge);
        TextView tvBed = view.findViewById(R.id.tvDetailBed);
        TextView tvDiagnosis = view.findViewById(R.id.tvDetailDiagnosis);
        TextView tvAllergy = view.findViewById(R.id.tvDetailAllergy);
        TextView tvRoom = view.findViewById(R.id.tvDetailRoom);
        MaterialButton btnEdit = view.findViewById(R.id.btnEditPatientDetail);
        MaterialButton btnClose = view.findViewById(R.id.btnClosePatientDetail);
        MaterialButton btnScanMedicine = view.findViewById(R.id.btnScanMedicineForPatient);

        String name = patient.getName() == null ? "Bệnh nhân" : patient.getName();
        String initial = !name.trim().isEmpty() ? name.trim().substring(0, 1).toUpperCase() : "P";

        tvInitial.setText(initial);
        tvName.setText(name);
        tvStatus.setText(patient.getStatus() == null || patient.getStatus().isEmpty()
                ? "Chưa cập nhật"
                : patient.getStatus());

        tvAge.setText(isEmpty(patient.getAge()) ? "Chưa cập nhật" : patient.getAge() + " tuổi");
        tvBed.setText(isEmpty(patient.getBedNumber()) ? "Chưa cập nhật" : "Giường " + patient.getBedNumber());
        tvDiagnosis.setText(isEmpty(patient.getDiagnosis()) ? "Chưa có chẩn đoán" : patient.getDiagnosis());
        tvAllergy.setText(isEmpty(patient.getAllergy()) ? "Không ghi nhận dị ứng" : patient.getAllergy());
        tvRoom.setText(selectedRoomName == null ? "Chưa chọn phòng" : selectedRoomName);

        int statusColor = ContextCompat.getColor(requireContext(), R.color.success);

        if ("Cấp cứu".equalsIgnoreCase(patient.getStatus())) {
            statusColor = ContextCompat.getColor(requireContext(), R.color.danger);
        } else if ("Theo dõi".equalsIgnoreCase(patient.getStatus())) {
            statusColor = ContextCompat.getColor(requireContext(), R.color.warning);
        }

        tvStatus.setTextColor(statusColor);

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            showModalDialog(patient);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnScanMedicine.setOnClickListener(v -> {
            viewModel.setCurrentPatient(patient);
            dialog.dismiss();

            Toast.makeText(
                    getContext(),
                    "Đã chọn " + patient.getName() + " để quét thuốc",
                    Toast.LENGTH_SHORT
            ).show();

            if (getActivity() instanceof ntu.tanphat.smart_medscan.ui.activities.MainActivity) {
                ((ntu.tanphat.smart_medscan.ui.activities.MainActivity) getActivity())
                        .selectBottomNavTab(R.id.placeholder);
            }
        });

        dialog.show();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showPopupMenu(View view, Object item) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add("Chỉnh sửa");
        popup.getMenu().add("Xóa");
        popup.setOnMenuItemClickListener(menuItem -> {
            if ("Chỉnh sửa".equals(menuItem.getTitle())) showModalDialog(item);
            else confirmDelete(item);
            return true;
        });
        popup.show();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                triggerSearch();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void triggerSearch() {
        String query = etSearch.getText().toString().trim();

        switch (currentState) {
            case DEPARTMENTS:
                if (query.isEmpty()) viewModel.fetchDepartments();
                else viewModel.searchDepartments(query);
                break;
            case FLOORS:
                if (query.isEmpty()) viewModel.fetchFloors(selectedDeptId);
                else viewModel.searchFloors(query, selectedDeptId);
                break;
            case ROOMS:
                if (query.isEmpty()) viewModel.fetchRooms(selectedFloorId);
                else viewModel.searchRooms(query, selectedFloorId);
                break;
            case PATIENTS:
                viewModel.searchPatients(query, selectedRoomId, currentStatusFilter);
                break;
        }
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showModalDialog(null));
        btnBack.setOnClickListener(v -> handleBackNavigation());
        
        btnFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add("Tất cả");
            popup.getMenu().add("Ổn định");
            popup.getMenu().add("Theo dõi");
            popup.getMenu().add("Cấp cứu");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle() != null) {
                    currentStatusFilter = item.getTitle().toString();
                    if (tvCurrentFilter != null) {
                        tvCurrentFilter.setText(currentStatusFilter);
                    }
                    triggerSearch();
                }
                return true;
            });
            popup.show();
        });
    }

    private void handleBackNavigation() {
        switch (currentState) {
            case FLOORS: updateViewState(ViewState.DEPARTMENTS); break;
            case ROOMS: updateViewState(ViewState.FLOORS); break;
            case PATIENTS: updateViewState(ViewState.ROOMS); break;
        }
    }

    private void updateViewState(ViewState newState) {
        this.currentState = newState;
        if (etSearch != null) etSearch.setText("");
        currentStatusFilter = "Tất cả";
        updateUIForState();
        fetchData();
    }

    private void updateOverviewCard() {
        if (tvTotalRecordCount == null) return;

        int total = displayList.size();
        tvTotalRecordCount.setText(String.valueOf(total));
        tvCurrentFilter.setText(currentStatusFilter);

        switch (currentState) {
            case DEPARTMENTS:
                tvRecordStateLabel.setText("Đang xem khoa");
                tvTotalRecordLabel.setText("khoa hiện có");
                tvCurrentLocation.setText("Toàn bệnh viện");
                tvCurrentLocationSub.setText("Chọn khoa để xem tầng");
                break;

            case FLOORS:
                tvRecordStateLabel.setText("Đang xem tầng");
                tvTotalRecordLabel.setText("tầng trong khoa");
                tvCurrentLocation.setText(selectedDeptName == null ? "Khoa" : selectedDeptName);
                tvCurrentLocationSub.setText("Chọn tầng để xem phòng");
                break;

            case ROOMS:
                tvRecordStateLabel.setText("Đang xem phòng");
                tvTotalRecordLabel.setText("phòng trong tầng");
                tvCurrentLocation.setText(selectedFloorName == null ? "Tầng" : selectedFloorName);
                tvCurrentLocationSub.setText(selectedDeptName == null ? "Chọn phòng để xem bệnh nhân" : selectedDeptName);
                break;

            case PATIENTS:
                tvRecordStateLabel.setText("Đang xem bệnh nhân");
                tvTotalRecordLabel.setText("bệnh nhân trong phòng");
                tvCurrentLocation.setText(selectedRoomName == null ? "Phòng" : selectedRoomName);
                tvCurrentLocationSub.setText("Lọc theo trạng thái bệnh án");
                break;
        }
    }

    private void updateUIForState() {
        if (btnBack != null) btnBack.setVisibility(currentState == ViewState.DEPARTMENTS ? View.GONE : View.VISIBLE);
        if (fabAdd != null) fabAdd.setText(String.format("Thêm %s", getEntityName()));
        
        if (btnFilter != null) {
            btnFilter.setVisibility(currentState == ViewState.PATIENTS ? View.VISIBLE : View.GONE);
        }

        if (tvHeaderTitle != null) {
            switch (currentState) {
                case DEPARTMENTS: 
                    tvHeaderTitle.setText("Bệnh viện"); 
                    etSearch.setHint("Tìm kiếm khoa...");
                    break;
                case FLOORS: 
                    tvHeaderTitle.setText(selectedDeptName); 
                    etSearch.setHint("Tìm kiếm tầng...");
                    break;
                case ROOMS: 
                    tvHeaderTitle.setText(selectedFloorName); 
                    etSearch.setHint("Tìm kiếm phòng...");
                    break;
                case PATIENTS: 
                    tvHeaderTitle.setText(selectedRoomName); 
                    etSearch.setHint("Tìm bệnh nhân...");
                    break;
            }
        }
        updateOverviewCard();
    }

    private void fetchData() {
        switch (currentState) {
            case DEPARTMENTS: viewModel.fetchDepartments(); break;
            case FLOORS: viewModel.fetchFloors(selectedDeptId); break;
            case ROOMS: viewModel.fetchRooms(selectedFloorId); break;
            case PATIENTS: viewModel.fetchPatients(selectedRoomId); break;
        }
    }

    private void showModalDialog(Object item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_record, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView).create();

        LinearLayout layoutPatientExtra = dialogView.findViewById(R.id.layoutPatientExtraFields);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etAge = dialogView.findViewById(R.id.etAge);
        EditText etBed = dialogView.findViewById(R.id.etBedNumber);
        EditText etDiagnosis = dialogView.findViewById(R.id.etDiagnosis);
        EditText etAllergy = dialogView.findViewById(R.id.etAllergy);
        Spinner spnStatus = dialogView.findViewById(R.id.spnStatus);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        if (layoutPatientExtra != null) {
            layoutPatientExtra.setVisibility(currentState == ViewState.PATIENTS ? View.VISIBLE : View.GONE);
        }

        if (currentState == ViewState.PATIENTS && spnStatus != null) {
            String[] statusOptions = {"Ổn định", "Theo dõi", "Cấp cứu"};
            ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, statusOptions);
            spnStatus.setAdapter(statusAdapter);
            if (item instanceof PatientRecord) {
                int pos = statusAdapter.getPosition(((PatientRecord) item).getStatus());
                if (pos >= 0) spnStatus.setSelection(pos);
            }
        }

        if (tvTitle != null) tvTitle.setText(String.format("%s %s", item == null ? "Thêm mới" : "Sửa", getEntityName()));
        
        if (item != null) {
            etName.setText(getNameFromItem(item));
            if (item instanceof PatientRecord) {
                PatientRecord p = (PatientRecord) item;
                etAge.setText(p.getAge());
                etBed.setText(p.getBedNumber());
                etDiagnosis.setText(p.getDiagnosis());
                etAllergy.setText(p.getAllergy());
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Không được để trống tên!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);

            if (currentState == ViewState.PATIENTS) {
                data.put("age", etAge.getText().toString().trim());
                data.put("bedNumber", etBed.getText().toString().trim());
                data.put("diagnosis", etDiagnosis.getText().toString().trim());
                data.put("allergy", etAllergy.getText().toString().trim());
                data.put("status", spnStatus.getSelectedItem().toString());
            }

            saveData(item, data);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void saveData(Object item, Map<String, Object> data) {
        String collection = getCollectionName();

        if (item == null) {
            if (currentState == ViewState.FLOORS) data.put("departmentId", selectedDeptId);
            else if (currentState == ViewState.ROOMS) data.put("floorId", selectedFloorId);
            else if (currentState == ViewState.PATIENTS) data.put("roomId", selectedRoomId);
            else if (currentState == ViewState.DEPARTMENTS) data.put("count", 0);

            viewModel.addRecord(collection, data, () -> {
                if (currentState == ViewState.PATIENTS) viewModel.updateDeptCount(selectedDeptId, 1);
                fetchData();
            });
        } else {
            viewModel.updateRecord(collection, getIdFromItem(item), data, this::fetchData);
        }
    }

    private void confirmDelete(Object item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (d, w) -> {
                    viewModel.deleteRecord(getCollectionName(), getIdFromItem(item), () -> {
                        if (currentState == ViewState.PATIENTS) viewModel.updateDeptCount(selectedDeptId, -1);
                        fetchData();
                    });
                }).setNegativeButton("Hủy", null).show();
    }

    private String getCollectionName() {
        switch (currentState) {
            case DEPARTMENTS: return "departments";
            case FLOORS: return "floors";
            case ROOMS: return "rooms";
            default: return "patients";
        }
    }

    private String getEntityName() {
        switch (currentState) {
            case DEPARTMENTS: return "Khoa";
            case FLOORS: return "Tầng";
            case ROOMS: return "Phòng";
            default: return "Bệnh nhân";
        }
    }

    private String getIdFromItem(Object item) {
        if (item instanceof Department) return ((Department) item).getId();
        if (item instanceof Floor) return ((Floor) item).getId();
        if (item instanceof Room) return ((Room) item).getId();
        if (item instanceof PatientRecord) return ((PatientRecord) item).getPatientId();
        return null;
    }

    private String getNameFromItem(Object item) {
        if (item instanceof Department) return ((Department) item).getName();
        if (item instanceof Floor) return ((Floor) item).getName();
        if (item instanceof Room) return ((Room) item).getName();
        if (item instanceof PatientRecord) return ((PatientRecord) item).getName();
        return "";
    }

    static class RecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Object> items;
        private final OnRecordClickListener listener;

        public RecordAdapter(List<Object> items, OnRecordClickListener listener) {
            this.items = items; this.listener = listener;
        }

        @Override public int getItemViewType(int position) { return (items.get(position) instanceof PatientRecord) ? 1 : 0; }

        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            if (viewType == 1) return new PatientViewHolder(inf.inflate(R.layout.item_patient_record, parent, false));
            return new SelectionViewHolder(inf.inflate(R.layout.item_selection, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = items.get(position);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
            if (holder instanceof SelectionViewHolder) {
                SelectionViewHolder h = (SelectionViewHolder) holder;
                h.btnOptions.setOnClickListener(v -> listener.onOptionsClick(v, item));
                if (item instanceof Department) {
                    Department d = (Department) item;
                    h.title.setText(d.getName());
                    h.sub.setText(String.format("%d bệnh nhân", d.getCount()));
                    h.icon.setImageResource(R.drawable.iconkhoa);
                } else if (item instanceof Floor) {
                    Floor f = (Floor) item;
                    h.title.setText(f.getName()); h.sub.setText("Nhấn để xem các phòng");
                    h.icon.setImageResource(R.drawable.flooricon);
                } else if (item instanceof Room) {
                    Room r = (Room) item;
                    h.title.setText(r.getName()); h.sub.setText("Nhấn để xem bệnh nhân");
                    h.icon.setImageResource(R.drawable.roomicon);
                }
            } else if (holder instanceof PatientViewHolder) {
                PatientViewHolder h = (PatientViewHolder) holder;
                PatientRecord p = (PatientRecord) item;

                h.name.setText(p.getName());
                h.diag.setText(p.getDiagnosis() == null || p.getDiagnosis().isEmpty()
                        ? "Chưa có chẩn đoán"
                        : p.getDiagnosis());

                h.bed.setText("Giường " + (p.getBedNumber() == null || p.getBedNumber().isEmpty()
                        ? "--"
                        : p.getBedNumber()));

                h.age.setText((p.getAge() == null || p.getAge().isEmpty()
                        ? "--"
                        : p.getAge()) + " tuổi");

                h.statusChip.setText(p.getStatus() == null || p.getStatus().isEmpty()
                        ? "Chưa rõ"
                        : p.getStatus());

                String nameStr = p.getName();
                String init = (nameStr != null && !nameStr.isEmpty())
                        ? nameStr.substring(0, 1).toUpperCase()
                        : "P";
                h.initial.setText(init);

                h.allergyBadge.setVisibility(
                        p.getAllergy() != null && !p.getAllergy().trim().isEmpty()
                                ? View.VISIBLE
                                : View.GONE
                );

                int color = ContextCompat.getColor(h.itemView.getContext(), R.color.success);

                if ("Cấp cứu".equalsIgnoreCase(p.getStatus())) {
                    color = ContextCompat.getColor(h.itemView.getContext(), R.color.danger);
                } else if ("Theo dõi".equalsIgnoreCase(p.getStatus())) {
                    color = ContextCompat.getColor(h.itemView.getContext(), R.color.warning);
                }

                h.statusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                h.statusChip.setTextColor(color);

                h.btnOptions.setOnClickListener(v -> listener.onOptionsClick(v, item));
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static class SelectionViewHolder extends RecyclerView.ViewHolder {
            TextView title, sub; ImageView icon; View btnOptions;
            SelectionViewHolder(View v) { super(v); 
                title = v.findViewById(R.id.tvSelectionTitle); 
                sub = v.findViewById(R.id.tvSelectionSubtitle); 
                icon = v.findViewById(R.id.ivSelectionIcon);
                btnOptions = v.findViewById(R.id.btnOptions); 
            }
        }
        static class PatientViewHolder extends RecyclerView.ViewHolder {
            TextView name, diag, bed, age, initial, allergyBadge, statusChip;
            View btnOptions, statusDot;

            PatientViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.tvPatientName);
                diag = v.findViewById(R.id.tvDiagnosis);
                bed = v.findViewById(R.id.tvBedNumber);
                age = v.findViewById(R.id.tvAge);
                initial = v.findViewById(R.id.tvPatientInitial);
                allergyBadge = v.findViewById(R.id.tvAllergyBadge);
                statusChip = v.findViewById(R.id.tvStatusChip);
                statusDot = v.findViewById(R.id.viewStatusDot);
                btnOptions = v.findViewById(R.id.btnOptions);
            }
        }
    }
}
