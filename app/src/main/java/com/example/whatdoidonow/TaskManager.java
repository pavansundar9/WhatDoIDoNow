package com.example.whatdoidonow;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String PREF_NAME = "TaskGeneratorPrefs";
    private static final String KEY_TASKS = "tasks";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public TaskManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Save tasks to SharedPreferences
     */
    public void saveTasks(List<Task> tasks) {
        String tasksJson = gson.toJson(tasks);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TASKS, tasksJson);
        editor.apply();
        Log.d("TaskManager", "Saved " + tasks.size() + " tasks");
    }

    /**
     * Load tasks from SharedPreferences
     */
    public List<Task> loadTasks() {
        String tasksJson = sharedPreferences.getString(KEY_TASKS, null);
        if (tasksJson != null) {
            Type type = new TypeToken<ArrayList<Task>>() {}.getType();
            List<Task> tasks = gson.fromJson(tasksJson, type);
            Log.d("TaskManager", "Loaded " + tasks.size() + " tasks");
            return tasks;
        }
        return new ArrayList<>();
    }

    /**
     * Clear all tasks from SharedPreferences
     */
    public void clearTasks() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TASKS);
        editor.apply();
        Log.d("TaskManager", "Cleared all tasks");
    }
}