package com.example.whatdoidonow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

        // Sort tasks using our enhanced sorting logic
        sortTasks();

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

    /**
     * Sort tasks with enhanced logic:
     * 1. Incomplete tasks at the top (sorted by creation date, newest first)
     * 2. Completed tasks at the bottom (sorted by completion date, newest first)
     */
    private void sortTasks() {
        Collections.sort(tasksList, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                // First, separate completed and incomplete tasks
                if (task1.isCompleted() && !task2.isCompleted()) {
                    return 1; // task1 (completed) goes after task2 (not completed)
                } else if (!task1.isCompleted() && task2.isCompleted()) {
                    return -1; // task1 (not completed) goes before task2 (completed)
                }

                // For tasks with the same completion status
                if (task1.isCompleted() && task2.isCompleted()) {
                    // Sort completed tasks by completion date (most recently completed first)
                    return Long.compare(task2.getCompletedAt(), task1.getCompletedAt());
                } else {
                    // Sort incomplete tasks by creation date (newest first)
                    return Long.compare(task2.getCreatedAt(), task1.getCreatedAt());
                }
            }
        });
    }

    private void addNewTask() {
        String taskText = taskInputEditText.getText().toString().trim();

        if (taskText.isEmpty()) {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new task - it will automatically get the current timestamp
        Task newTask = new Task(taskText, false);

        // Add to list and sort
        tasksList.add(newTask);
        sortTasks();

        // Notify adapter of data change
        taskAdapter.notifyDataSetChanged();

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
                // Mark as completed - this will automatically set the completion timestamp
                tasksList.get(i).setCompleted(true);

                // Sort tasks to move this one to the appropriate position
                sortTasks();

                // Notify adapter of potential position changes
                taskAdapter.notifyDataSetChanged();

                // Save tasks to persistent storage
                taskManager.saveTasks(tasksList);

                // Reset selected task
                currentSelectedTask = null;
                selectedTaskTextView.setText("Task completed! Generate another one");
                completeTaskBtn.setEnabled(false);
                updateBackgroundForTaskStatus();

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

    private void updateBackgroundForTaskStatus() {
        boolean hasCompletedTasks = false;

        // Check if there are any completed tasks
        for (Task task : tasksList) {
            if (task.isCompleted()) {
                hasCompletedTasks = true;
                break;
            }
        }

        // Use direct color setting with the content view
        View rootView = findViewById(android.R.id.content).getRootView();

        if (hasCompletedTasks) {
            // Debug message to check if this code path is executed
            Log.d("MainActivity", "Setting background to green");
            rootView.setBackgroundColor(getColor(R.color.green_gradient));
        } else {
            // Debug message
            Log.d("MainActivity", "Setting background to default");
            rootView.setBackgroundColor(getColor(R.color.colorBackground));
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
        // Update completion status - this will set the completion timestamp automatically
        tasksList.get(position).setCompleted(isChecked);

        // Sort tasks after completion status changes
        sortTasks();

        // Notify adapter of potential position changes
        taskAdapter.notifyDataSetChanged();

        // Save tasks to persistent storage
        taskManager.saveTasks(tasksList);

        // If this is the currently selected task, reset selection
        if (currentSelectedTask != null && currentSelectedTask.equals(tasksList.get(position))) {
            currentSelectedTask = null;
            selectedTaskTextView.setText("Task completed! Generate another one");
            completeTaskBtn.setEnabled(false);
        }

        updateBackgroundForTaskStatus();
    }

    @Override
    public void onDeleteTask(int position) {
        // Show confirmation dialog before deleting
        showDeleteTaskDialog(position);
        updateBackgroundForTaskStatus();
    }
}