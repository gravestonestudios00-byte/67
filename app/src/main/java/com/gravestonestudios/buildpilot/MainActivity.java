package com.gravestonestudios.buildpilot;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MainActivity extends Activity {
    private File saveFile;
    private LinearLayout root;
    private TextView projectTitle;
    private TextView projectGoal;
    private ProgressBar progress;
    private LinearLayout content;
    private TextView status;
    private JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saveFile = new File(getFilesDir(), "buildpilot_data.json");
        data = defaultData();
        loadData();
        buildUi();
        showTasks();
    }

    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        root.setBackgroundColor(Color.rgb(18, 24, 38));

        projectTitle = label("BuildPilot", 30, true);
        projectTitle.setGravity(Gravity.CENTER);
        root.addView(projectTitle);

        projectGoal = label("", 16, false);
        projectGoal.setGravity(Gravity.CENTER);
        root.addView(projectGoal);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(-1, 32);
        progressParams.setMargins(0, 24, 0, 24);
        root.addView(progress, progressParams);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.addView(button("Tasks", this::showTasks), new LinearLayout.LayoutParams(0, 96, 1f));
        tabs.addView(button("Bugs", this::showBugs), new LinearLayout.LayoutParams(0, 96, 1f));
        tabs.addView(button("APK", this::showChecklist), new LinearLayout.LayoutParams(0, 96, 1f));
        root.addView(tabs);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 24, 0, 24);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        status = label("Saved locally", 14, false);
        status.setGravity(Gravity.CENTER);
        root.addView(status);

        setContentView(root);
        refreshHeader();
    }

    private void refreshHeader() {
        projectTitle.setText(data.optString("project_name", "BuildPilot"));
        projectGoal.setText("Goal: " + data.optString("goal", "Set a goal"));
        JSONArray tasks = data.optJSONArray("tasks");
        if (tasks == null || tasks.length() == 0) {
            progress.setProgress(0);
            return;
        }
        int done = 0;
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task != null && task.optBoolean("done", false)) done++;
        }
        progress.setProgress(done * 100 / tasks.length());
    }

    private void showTasks() {
        content.removeAllViews();
        EditText input = input("New task");
        content.addView(input);
        content.addView(button("Add Task", () -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                JSONArray tasks = data.optJSONArray("tasks");
                if (tasks != null) tasks.put(item("title", title, "done", false));
                saveData();
                showTasks();
            }
        }));

        JSONArray tasks = data.optJSONArray("tasks");
        if (tasks == null) return;
        for (int i = 0; i < tasks.length(); i++) {
            final int index = i;
            JSONObject task = tasks.optJSONObject(i);
            if (task == null) continue;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(button(task.optBoolean("done", false) ? "✓" : "□", () -> {
                setBool(task, "done", !task.optBoolean("done", false));
                saveData();
                showTasks();
            }), new LinearLayout.LayoutParams(96, 96));
            row.addView(label(task.optString("title", "Task"), 18, false), new LinearLayout.LayoutParams(0, 96, 1f));
            row.addView(button("Del", () -> {
                removeAt(tasks, index);
                saveData();
                showTasks();
            }), new LinearLayout.LayoutParams(120, 96));
            content.addView(row);
        }
        refreshHeader();
    }

    private void showBugs() {
        content.removeAllViews();
        EditText input = input("New bug");
        content.addView(input);
        content.addView(button("Add Bug", () -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                JSONArray bugs = data.optJSONArray("bugs");
                if (bugs != null) bugs.put(item("title", title, "fixed", false));
                saveData();
                showBugs();
            }
        }));

        JSONArray bugs = data.optJSONArray("bugs");
        if (bugs == null) return;
        for (int i = 0; i < bugs.length(); i++) {
            final int index = i;
            JSONObject bug = bugs.optJSONObject(i);
            if (bug == null) continue;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(button(bug.optBoolean("fixed", false) ? "Fixed" : "Open", () -> {
                setBool(bug, "fixed", !bug.optBoolean("fixed", false));
                saveData();
                showBugs();
            }), new LinearLayout.LayoutParams(150, 96));
            row.addView(label(bug.optString("title", "Bug"), 18, false), new LinearLayout.LayoutParams(0, 96, 1f));
            row.addView(button("Del", () -> {
                removeAt(bugs, index);
                saveData();
                showBugs();
            }), new LinearLayout.LayoutParams(120, 96));
            content.addView(row);
        }
        refreshHeader();
    }

    private void showChecklist() {
        content.removeAllViews();
        content.addView(label("APK Build Checklist", 22, true));
        JSONArray list = data.optJSONArray("checklist");
        if (list == null) return;
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.optJSONObject(i);
            if (item == null) continue;
            content.addView(button((item.optBoolean("done", false) ? "✓ " : "□ ") + item.optString("title", "Item"), () -> {
                setBool(item, "done", !item.optBoolean("done", false));
                saveData();
                showChecklist();
            }));
        }
        refreshHeader();
    }

    private EditText input(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setTextColor(Color.WHITE);
        editText.setHintTextColor(Color.LTGRAY);
        return editText;
    }

    private Button button(String text, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private TextView label(String text, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    private void saveData() {
        try {
            Files.write(saveFile.toPath(), data.toString(2).getBytes(StandardCharsets.UTF_8));
            status.setText("Saved locally");
            refreshHeader();
        } catch (Exception e) {
            status.setText("Save failed");
        }
    }

    private void loadData() {
        try {
            if (saveFile.exists()) {
                String text = new String(Files.readAllBytes(saveFile.toPath()), StandardCharsets.UTF_8);
                data = new JSONObject(text);
            }
        } catch (Exception ignored) {
            data = defaultData();
        }
    }

    private void removeAt(JSONArray array, int index) {
        JSONArray copy = new JSONArray();
        for (int i = 0; i < array.length(); i++) if (i != index) copy.put(array.opt(i));
        while (array.length() > 0) array.remove(0);
        for (int i = 0; i < copy.length(); i++) copyValue(array, copy.opt(i));
    }

    private void copyValue(JSONArray array, Object value) {
        array.put(value);
    }

    private void setBool(JSONObject object, String key, boolean value) {
        try {
            object.put(key, value);
        } catch (Exception ignored) {
        }
    }

    private JSONObject item(String titleKey, String titleValue, String boolKey, boolean boolValue) {
        JSONObject object = new JSONObject();
        try {
            object.put(titleKey, titleValue);
            object.put(boolKey, boolValue);
        } catch (Exception ignored) {
        }
        return object;
    }

    private JSONObject defaultData() {
        JSONObject root = new JSONObject();
        try {
            root.put("project_name", "BuildPilot");
            root.put("goal", "Keep the next playable game-dev step visible.");
            root.put("tasks", new JSONArray()
                    .put(item("title", "Make one playable room", "done", false))
                    .put(item("title", "Add player movement", "done", false))
                    .put(item("title", "Add flashlight interaction", "done", false)));
            root.put("bugs", new JSONArray()
                    .put(item("title", "Example: player clips through wall", "fixed", false)));
            root.put("checklist", new JSONArray()
                    .put(item("title", "Project opens without errors", "done", true))
                    .put(item("title", "Main screen loads", "done", true))
                    .put(item("title", "Debug APK exported", "done", false))
                    .put(item("title", "Install APK on phone", "done", false)));
        } catch (Exception ignored) {
        }
        return root;
    }
}
