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

    public interface OnRecordClickListener {
        void onItemClick(Object item);
        void onOptionsClick(View view, Object item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        viewModel = new ViewModelProvider(this).get(RecordViewModel.class);

        rvRecords = view.findViewById(R.id.rvMedicalRecords);
        etSearch = view.findViewById(R.id.etSearchRecord);
        fabAdd = view.findViewById(R.id.fabAddPatient);
        pbLoading = view.findViewById(R.id.pbLoading);
        btnBack = view.findViewById(R.id.btnBack);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        btnFilter = view.findViewById(R.id.btnFilter);

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
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (pbLoading != null) pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
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

        // Search contextual logic
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
                // Search with status filter for patients
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
        // Reset search and filter when moving between levels
        etSearch.setText("");
        currentStatusFilter = "Tất cả";
        updateUIForState();
        fetchData();
    }

    private void updateUIForState() {
        if (btnBack != null) btnBack.setVisibility(currentState == ViewState.DEPARTMENTS ? View.GONE : View.VISIBLE);
        if (fabAdd != null) fabAdd.setText(String.format("Thêm %s", getEntityName()));
        
        // Show filter button ONLY in patients view
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
                    etSearch.setHint("Tìm kiếm bệnh nhân...");
                    break;
            }
        }
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
            if (etName != null) etName.setText(getNameFromItem(item));
            if (item instanceof PatientRecord) {
                PatientRecord p = (PatientRecord) item;
                if (etAge != null) etAge.setText(p.getAge());
                if (etBed != null) etBed.setText(p.getBedNumber());
                if (etDiagnosis != null) etDiagnosis.setText(p.getDiagnosis());
                if (etAllergy != null) etAllergy.setText(p.getAllergy());
            }
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = (etName != null) ? etName.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Không được để trống tên!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("name", name);

                if (currentState == ViewState.PATIENTS) {
                    if (etAge != null) data.put("age", etAge.getText().toString().trim());
                    if (etBed != null) data.put("bedNumber", etBed.getText().toString().trim());
                    if (etDiagnosis != null) data.put("diagnosis", etDiagnosis.getText().toString().trim());
                    if (etAllergy != null) data.put("allergy", etAllergy.getText().toString().trim());
                    if (spnStatus != null) data.put("status", spnStatus.getSelectedItem().toString());
                }

                saveData(item, data);
                dialog.dismiss();
            });
        }
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
                    h.sub.setText(String.format("%s BN", d.getCount()));
                    h.icon.setImageResource(R.drawable.khoaicon);
                } else if (item instanceof Floor) {
                    Floor f = (Floor) item;
                    h.title.setText(f.getName()); h.sub.setText("Nhấn để xem phòng");
                    h.icon.setImageResource(android.R.drawable.ic_menu_sort_by_size);
                } else if (item instanceof Room) {
                    Room r = (Room) item;
                    h.title.setText(r.getName()); h.sub.setText("Nhấn để xem bệnh nhân");
                    h.icon.setImageResource(android.R.drawable.ic_menu_myplaces);
                }
            } else if (holder instanceof PatientViewHolder) {
                PatientViewHolder h = (PatientViewHolder) holder;
                PatientRecord p = (PatientRecord) item;
                h.name.setText(p.getName()); h.bed.setText(String.format("Giường %s", p.getBedNumber()));
                h.diag.setText(p.getDiagnosis()); 
                h.age.setText(String.format("%s tuổi", p.getAge()));
                
                String init = (p.getName() != null && !p.getName().isEmpty()) ? p.getName().substring(0, 1).toUpperCase() : "P";
                h.initial.setText(init);
                
                h.allergyBadge.setVisibility(p.getAllergy() != null && !p.getAllergy().isEmpty() ? View.VISIBLE : View.GONE);
                
                int color = ContextCompat.getColor(h.itemView.getContext(), R.color.success);
                if ("Cấp cứu".equalsIgnoreCase(p.getStatus())) color = ContextCompat.getColor(h.itemView.getContext(), R.color.danger);
                else if ("Theo dõi".equalsIgnoreCase(p.getStatus())) color = ContextCompat.getColor(h.itemView.getContext(), R.color.warning);
                h.statusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

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
            TextView name, bed, diag, age, initial, allergyBadge; View btnOptions, statusDot;
            PatientViewHolder(View v) { super(v); 
                name = v.findViewById(R.id.tvPatientName); 
                bed = v.findViewById(R.id.tvBedNumber); 
                diag = v.findViewById(R.id.tvDiagnosis);
                age = v.findViewById(R.id.tvAge);
                initial = v.findViewById(R.id.tvPatientInitial);
                allergyBadge = v.findViewById(R.id.tvAllergyBadge);
                statusDot = v.findViewById(R.id.viewStatusDot);
                btnOptions = v.findViewById(R.id.btnOptions); 
            }
        }
    }
}
