package ntu.tanphat.smart_medscan.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.data.models.CareSchedule;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    public interface OnScheduleActionListener {
        void onDoneClick(CareSchedule item);
        void onDeleteClick(CareSchedule item);
    }

    private final List<CareSchedule> items;
    private final OnScheduleActionListener listener;

    public ScheduleAdapter(List<CareSchedule> items, OnScheduleActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        CareSchedule item = items.get(position);

        holder.tvTime.setText(item.getTime());
        holder.tvTitle.setText(item.getTitle());

        String patient = item.getPatientName() == null ? "Chưa có bệnh nhân" : item.getPatientName();
        String room = item.getRoomName() == null ? "Chưa có phòng" : item.getRoomName();

        holder.tvPatient.setText(patient + " • " + room);
        holder.tvStatus.setText(item.getStatus());

        if ("Đã xong".equals(item.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
            holder.btnDone.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#104DE0"));
            holder.btnDone.setVisibility(View.VISIBLE);
        }

        holder.btnDone.setOnClickListener(v -> listener.onDoneClick(item));

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Đánh dấu đã xong");
            popup.getMenu().add("Xóa lịch");

            popup.setOnMenuItemClickListener(menuItem -> {
                String title = menuItem.getTitle().toString();

                if ("Đánh dấu đã xong".equals(title)) {
                    listener.onDoneClick(item);
                } else {
                    listener.onDeleteClick(item);
                }

                return true;
            });

            popup.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle, tvPatient, tvStatus;
        View btnDone;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvScheduleTime);
            tvTitle = itemView.findViewById(R.id.tvScheduleTitle);
            tvPatient = itemView.findViewById(R.id.tvSchedulePatient);
            tvStatus = itemView.findViewById(R.id.tvScheduleStatus);
            btnDone = itemView.findViewById(R.id.btnDoneSchedule);
        }
    }
}