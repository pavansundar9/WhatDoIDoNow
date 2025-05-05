package com.example.whatdoidonow;

import java.util.Objects;

public class Task {
    private String taskText;
    private boolean completed;
    private long createdAt;      // Timestamp when task was created
    private long completedAt;    // Timestamp when task was marked as completed (0 if not completed)

    public Task(String taskText, boolean completed) {
        this.taskText = taskText;
        this.completed = completed;
        this.createdAt = System.currentTimeMillis();
        this.completedAt = 0;    // Default value for uncompleted tasks
    }

    // Constructor with timestamp support for deserialization from storage
    public Task(String taskText, boolean completed, long createdAt, long completedAt) {
        this.taskText = taskText;
        this.completed = completed;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        // Update completedAt timestamp when task is completed
        if (completed && completedAt == 0) {
            this.completedAt = System.currentTimeMillis();
        } else if (!completed) {
            // Reset completion timestamp if marked as not completed
            this.completedAt = 0;
        }
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(taskText, task.taskText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskText);
    }
}