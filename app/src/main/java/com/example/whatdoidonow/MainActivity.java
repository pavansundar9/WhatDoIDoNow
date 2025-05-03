package com.example.whatdoidonow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements TaskAdapter.TaskItemClickListener {

    private TextInputEditText taskInputEditText;
    private MaterialButton addTaskBtn, generateTaskBtn, completeTaskBtn;
    private TextView selectedTaskTextView, emptyTasksTextView;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> tasksList = new ArrayList<>();
    private Task currentSelectedTask = null;

    private TaskManager taskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize task manager for persistence
        taskManager = new TaskManager(this);

        // Load saved tasks
        tasksList = taskManager.loadTasks();

        // Initialize UI components
        taskInputEditText = findViewById(R.id.taskInputEditText);
        addTaskBtn = findViewById(R.id.addTaskBtn);
        generateTaskBtn = findViewById(R.id.generateTaskBtn);
        completeTaskBtn = findViewById(R.id.completeTaskBtn);
        selectedTaskTextView = findViewById(R.id.selectedTaskTextView);
        emptyTasksTextView = findViewById(R.id.emptyTasksTextView);
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);

        // Set up RecyclerView
        taskAdapter = new TaskAdapter(tasksList, this);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(taskAdapter);

        // Add task button click listener
        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTask();
            }
        });

        // Generate random task button click listener
        generateTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRandomTask();
            }
        });

        // Complete task button click listener
        completeTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markTaskAsCompleted();
            }
        });

        // Add long press to clear all tasks (with confirmation)
        generateTaskBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showClearAllTasksDialog();
                return true;
            }
        });

        // Update UI based on initial state
        updateEmptyState();
    }

    private void addNewTask() {
        String taskText = taskInputEditText.getText().toString().trim();

        if (taskText.isEmpty()) {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new task and add to list
        Task newTask = new Task(taskText, false);
        tasksList.add(0, newTask);
        taskAdapter.notifyItemInserted(0);

        // Save tasks to persistent storage
        taskManager.saveTasks(tasksList);

        // Clear input field
        taskInputEditText.setText("");

        // Update UI
        updateEmptyState();

        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
    }

    private void generateRandomTask() {
        List<Task> pendingTasks = new ArrayList<>();

        // Filter for pending tasks
        for (Task task : tasksList) {
            if (!task.isCompleted()) {
                pendingTasks.add(task);
            }
        }

        if (pendingTasks.isEmpty()) {
            Toast.makeText(this, "No pending tasks to choose from", Toast.LENGTH_SHORT).show();
            selectedTaskTextView.setText("Add some tasks to get started");
            currentSelectedTask = null;
            completeTaskBtn.setEnabled(false);
            return;
        }

        // Generate random task
        Random random = new Random();
        int randomIndex = random.nextInt(pendingTasks.size());
        currentSelectedTask = pendingTasks.get(randomIndex);

        // Update UI with animation
        selectedTaskTextView.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        selectedTaskTextView.setText(currentSelectedTask.getTaskText());
                        selectedTaskTextView.animate()
                                .alpha(1f)
                                .setDuration(150)
                                .start();
                    }
                }).start();

        // Enable complete button
        completeTaskBtn.setEnabled(true);
    }

    private void markTaskAsCompleted() {
        if (currentSelectedTask == null) {
            return;
        }

        // Find the task in the list
        for (int i = 0; i < tasksList.size(); i++) {
            if (tasksList.get(i).equals(currentSelectedTask)) {
                // Mark as completed
                tasksList.get(i).setCompleted(true);
                taskAdapter.notifyItemChanged(i);

                // Save tasks to persistent storage
                taskManager.saveTasks(tasksList);

                // Reset selected task
                currentSelectedTask = null;
                selectedTaskTextView.setText("Task completed! Generate another one");
                completeTaskBtn.setEnabled(false);

                Toast.makeText(this, "Task marked as completed", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void updateEmptyState() {
        if (tasksList.isEmpty()) {
            emptyTasksTextView.setVisibility(View.VISIBLE);
            tasksRecyclerView.setVisibility(View.GONE);
        } else {
            emptyTasksTextView.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show a confirmation dialog for clearing all tasks
     */
    private void showClearAllTasksDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear All Tasks")
                .setMessage("Are you sure you want to delete all tasks? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    // Clear all tasks
                    tasksList.clear();
                    taskAdapter.notifyDataSetChanged();

                    // Clear tasks from storage
                    taskManager.clearTasks();

                    // Reset selected task
                    currentSelectedTask = null;
                    selectedTaskTextView.setText("Your random task will appear here");
                    completeTaskBtn.setEnabled(false);

                    // Update UI
                    updateEmptyState();

                    Toast.makeText(MainActivity.this, "All tasks cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show confirmation dialog for deleting a single task
     */
    private void showDeleteTaskDialog(int position) {
        Task taskToDelete = tasksList.get(position);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTask(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete a task at the specified position
     */
    private void deleteTask(int position) {
        // Check if it's the currently selected task
        if (currentSelectedTask != null && currentSelectedTask.equals(tasksList.get(position))) {
            currentSelectedTask = null;
            selectedTaskTextView.setText("Your random task will appear here");
            completeTaskBtn.setEnabled(false);
        }

        // Remove from list
        tasksList.remove(position);
        taskAdapter.notifyItemRemoved(position);

        // Save updated list
        taskManager.saveTasks(tasksList);

        // Update UI
        updateEmptyState();

        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskCheckChanged(int position, boolean isChecked) {
        tasksList.get(position).setCompleted(isChecked);
        taskAdapter.notifyItemChanged(position);

        // Save tasks to persistent storage
        taskManager.saveTasks(tasksList);

        // If this is the currently selected task, reset selection
        if (currentSelectedTask != null && currentSelectedTask.equals(tasksList.get(position))) {
            currentSelectedTask = null;
            selectedTaskTextView.setText("Task completed! Generate another one");
            completeTaskBtn.setEnabled(false);
        }
    }

    @Override
    public void onDeleteTask(int position) {
        // Show confirmation dialog before deleting
        showDeleteTaskDialog(position);
    }
}