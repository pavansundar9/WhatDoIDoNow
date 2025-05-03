package com.example.whatdoidonow;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskItemClickListener listener;

    public interface TaskItemClickListener {
        void onTaskCheckChanged(int position, boolean isChecked);
    }

    public TaskAdapter(List<Task> taskList, TaskItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Set task text
        holder.taskTextView.setText(task.getTaskText());

        // Set checkbox without triggering listener
        holder.taskCheckBox.setOnCheckedChangeListener(null);
        holder.taskCheckBox.setChecked(task.isCompleted());
        holder.taskCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onTaskCheckChanged(adapterPosition, isChecked);
                }
            }
        });

        // Update UI based on completion status
        if (task.isCompleted()) {
            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskStatusChip.setText("Completed");
            holder.taskStatusChip.setChipBackgroundColorResource(R.color.colorCompleted);
            holder.taskItemCard.setRippleColorResource(R.color.colorCompleted);
        } else {
            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskStatusChip.setText("Pending");
            holder.taskStatusChip.setChipBackgroundColorResource(R.color.colorPending);
            holder.taskItemCard.setRippleColorResource(R.color.colorPending);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTextView;
        CheckBox taskCheckBox;
        Chip taskStatusChip;
        MaterialCardView taskItemCard;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskStatusChip = itemView.findViewById(R.id.taskStatusChip);
            taskItemCard = (MaterialCardView) itemView;
        }
    }
}