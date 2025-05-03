package com.example.whatdoidonow;

import java.util.Objects;

public class Task {
    private String taskText;
    private boolean completed;

    public Task(String taskText, boolean completed) {
        this.taskText = taskText;
        this.completed = completed;
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