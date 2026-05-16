package ntu.tanphat.smart_medscan.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ntu.tanphat.smart_medscan.R;
import ntu.tanphat.smart_medscan.data.models.MedicalTask;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<MedicalTask> taskList;

    public TaskAdapter(List<MedicalTask> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_priority_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        MedicalTask task = taskList.get(position);
        // Cách viết String.format hoặc nối chuỗi rõ ràng
        String header = String.format("Phòng %s - %s", task.getRoom(), task.getPatientName());
        String detail = String.format("%s (%s)", task.getTaskContent(), task.getTime());

        holder.tvInfo.setText(header);
        holder.tvContent.setText(detail);
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvInfo, tvContent;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInfo = itemView.findViewById(R.id.tvTaskInfo); // Đảm bảo ID này khớp với XML item
            tvContent = itemView.findViewById(R.id.tvTaskContent);
        }
    }
}
