package com.gravestonestudios.buildpilot

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : Activity() {
    private val saveFile by lazy { File(filesDir, "buildpilot_data.json") }
    private lateinit var root: LinearLayout
    private lateinit var projectTitle: TextView
    private lateinit var projectGoal: TextView
    private lateinit var progress: ProgressBar
    private lateinit var content: LinearLayout
    private lateinit var status: TextView
    private var data: JSONObject = defaultData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        buildUi()
        showTasks()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(Color.rgb(18, 24, 38))
        }

        projectTitle = label("BuildPilot", 30, true)
        projectTitle.gravity = Gravity.CENTER
        root.addView(projectTitle)

        projectGoal = label("", 16, false)
        projectGoal.gravity = Gravity.CENTER
        root.addView(projectGoal)

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
        }
        root.addView(progress, LinearLayout.LayoutParams(-1, 32).apply { setMargins(0, 24, 0, 24) })

        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tabs.addView(button("Tasks") { showTasks() }, LinearLayout.LayoutParams(0, 96, 1f))
        tabs.addView(button("Bugs") { showBugs() }, LinearLayout.LayoutParams(0, 96, 1f))
        tabs.addView(button("APK") { showChecklist() }, LinearLayout.LayoutParams(0, 96, 1f))
        root.addView(tabs)

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 24, 0, 24)
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        status = label("Saved locally", 14, false)
        status.gravity = Gravity.CENTER
        root.addView(status)

        setContentView(root)
        refreshHeader()
    }

    private fun refreshHeader() {
        projectTitle.text = data.optString("project_name", "BuildPilot")
        projectGoal.text = "Goal: ${data.optString("goal", "Set a goal")}" 
        val tasks = data.optJSONArray("tasks") ?: JSONArray()
        var done = 0
        for (i in 0 until tasks.length()) {
            if (tasks.getJSONObject(i).optBoolean("done")) done++
        }
        progress.progress = if (tasks.length() == 0) 0 else (done * 100 / tasks.length())
    }

    private fun showTasks() {
        content.removeAllViews()
        val input = EditText(this).apply {
            hint = "New task"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
        }
        content.addView(input)
        content.addView(button("Add Task") {
            val title = input.text.toString().trim()
            if (title.isNotEmpty()) {
                data.getJSONArray("tasks").put(JSONObject().put("title", title).put("done", false))
                saveData()
                showTasks()
            }
        })
        val tasks = data.getJSONArray("tasks")
        for (i in 0 until tasks.length()) {
            val task = tasks.getJSONObject(i)
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(button(if (task.optBoolean("done")) "✓" else "□") {
                task.put("done", !task.optBoolean("done"))
                saveData()
                showTasks()
            }, LinearLayout.LayoutParams(96, 96))
            row.addView(label(task.optString("title"), 18, false), LinearLayout.LayoutParams(0, 96, 1f))
            row.addView(button("Del") {
                removeAt(tasks, i)
                saveData()
                showTasks()
            }, LinearLayout.LayoutParams(120, 96))
            content.addView(row)
        }
        refreshHeader()
    }

    private fun showBugs() {
        content.removeAllViews()
        val input = EditText(this).apply {
            hint = "New bug"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
        }
        content.addView(input)
        content.addView(button("Add Bug") {
            val title = input.text.toString().trim()
            if (title.isNotEmpty()) {
                data.getJSONArray("bugs").put(JSONObject().put("title", title).put("fixed", false))
                saveData()
                showBugs()
            }
        })
        val bugs = data.getJSONArray("bugs")
        for (i in 0 until bugs.length()) {
            val bug = bugs.getJSONObject(i)
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(button(if (bug.optBoolean("fixed")) "Fixed" else "Open") {
                bug.put("fixed", !bug.optBoolean("fixed"))
                saveData()
                showBugs()
            }, LinearLayout.LayoutParams(150, 96))
            row.addView(label(bug.optString("title"), 18, false), LinearLayout.LayoutParams(0, 96, 1f))
            row.addView(button("Del") {
                removeAt(bugs, i)
                saveData()
                showBugs()
            }, LinearLayout.LayoutParams(120, 96))
            content.addView(row)
        }
        refreshHeader()
    }

    private fun showChecklist() {
        content.removeAllViews()
        content.addView(label("APK Build Checklist", 22, true))
        val list = data.getJSONArray("checklist")
        for (i in 0 until list.length()) {
            val item = list.getJSONObject(i)
            content.addView(button((if (item.optBoolean("done")) "✓ " else "□ ") + item.optString("title")) {
                item.put("done", !item.optBoolean("done"))
                saveData()
                showChecklist()
            })
        }
        refreshHeader()
    }

    private fun button(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { action() }
        }
    }

    private fun label(text: String, size: Int, bold: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            if (bold) typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    private fun saveData() {
        saveFile.writeText(data.toString(2))
        status.text = "Saved locally"
        refreshHeader()
    }

    private fun loadData() {
        data = if (saveFile.exists()) JSONObject(saveFile.readText()) else defaultData()
    }

    private fun removeAt(array: JSONArray, index: Int) {
        val copy = JSONArray()
        for (i in 0 until array.length()) if (i != index) copy.put(array.get(i))
        while (array.length() > 0) array.remove(0)
        for (i in 0 until copy.length()) array.put(copy.get(i))
    }

    private fun defaultData(): JSONObject = JSONObject()
        .put("project_name", "BuildPilot")
        .put("goal", "Keep the next playable game-dev step visible.")
        .put("tasks", JSONArray()
            .put(JSONObject().put("title", "Make one playable room").put("done", false))
            .put(JSONObject().put("title", "Add player movement").put("done", false))
            .put(JSONObject().put("title", "Add flashlight interaction").put("done", false))
        )
        .put("bugs", JSONArray()
            .put(JSONObject().put("title", "Example: player clips through wall").put("fixed", false))
        )
        .put("checklist", JSONArray()
            .put(JSONObject().put("title", "Project opens without errors").put("done", true))
            .put(JSONObject().put("title", "Main screen loads").put("done", true))
            .put(JSONObject().put("title", "Debug APK exported").put("done", false))
            .put(JSONObject().put("title", "Install APK on phone").put("done", false))
        )
}
