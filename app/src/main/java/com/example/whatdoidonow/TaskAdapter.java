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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskItemClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface TaskItemClickListener {
        void onTaskCheckChanged(int position, boolean isChecked);
        void onDeleteTask(int position);
    }

    public TaskAdapter(List<Task> taskList, TaskItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
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

        // Format and set the timestamp(s)
        String createdDateStr = dateFormat.format(new Date(task.getCreatedAt()));

        String timestampText;
        if (task.isCompleted() && task.getCompletedAt() > 0) {
            String completedDateStr = dateFormat.format(new Date(task.getCompletedAt()));
            timestampText = "Created: " + createdDateStr + "\nCompleted: " + completedDateStr;
        } else {
            timestampText = "Created: " + createdDateStr;
        }

        // Set the timestamp text if the view exists
        if (holder.taskDescriptionView != null) {
            holder.taskDescriptionView.setText(timestampText);
            holder.taskDescriptionView.setVisibility(View.VISIBLE);
        }

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

        // Set delete button listener
        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteTask(adapterPosition);
            }
        });

        // Update UI based on completion status
        if (task.isCompleted()) {
            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskStatusChip.setText("Completed");
            holder.taskStatusChip.setChipBackgroundColorResource(R.color.colorCompleted);
            holder.taskItemCard.setRippleColorResource(R.color.colorCompleted);
            holder.taskItemCard.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.green_gradient, null));
        } else {
            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskStatusChip.setText("Pending");
            holder.taskStatusChip.setChipBackgroundColorResource(R.color.colorPending);
            holder.taskItemCard.setRippleColorResource(R.color.colorPending);
            holder.taskItemCard.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.white, null)); // or your default
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTextView;
        TextView taskDescriptionView;
        CheckBox taskCheckBox;
        Chip taskStatusChip;
        MaterialCardView taskItemCard;
        MaterialButton deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskStatusChip = itemView.findViewById(R.id.taskStatusChip);
            taskItemCard = (MaterialCardView) itemView;
            deleteButton = itemView.findViewById(R.id.deleteButton);

            // Try to find taskDescriptionView (it's optional)
            taskDescriptionView = itemView.findViewById(R.id.taskDescriptionView);
        }
    }
}