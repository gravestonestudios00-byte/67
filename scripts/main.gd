extends Control

const SAVE_PATH := "user://buildpilot_data.json"

var data := {
	"project_name": "Dept Descent",
	"goal": "Build the first playable horror extraction loop.",
	"tasks": [
		{"title": "Make one playable room", "done": false},
		{"title": "Add player movement", "done": false},
		{"title": "Add flashlight interaction", "done": false},
		{"title": "Create extraction door", "done": false}
	],
	"bugs": [
		{"title": "Example: player clips through wall", "fixed": false}
	],
	"checklist": [
		{"title": "Project opens without errors", "done": false},
		{"title": "Main scene is assigned", "done": true},
		{"title": "Android export templates installed", "done": false},
		{"title": "Package name is set", "done": false},
		{"title": "App icon assigned", "done": true},
		{"title": "APK exported and tested", "done": false}
	]
}

@onready var project_name: Label = %ProjectName
@onready var project_goal: Label = %ProjectGoal
@onready var progress: ProgressBar = %Progress
@onready var stack: TabContainer = %Stack
@onready var status: Label = %Status

@onready var tasks_button: Button = %TasksButton
@onready var bugs_button: Button = %BugsButton
@onready var checklist_button: Button = %ChecklistButton

@onready var task_input: LineEdit = %TaskInput
@onready var add_task_button: Button = %AddTaskButton
@onready var task_list: ItemList = %TaskList
@onready var toggle_task_button: Button = %ToggleTaskButton
@onready var delete_task_button: Button = %DeleteTaskButton

@onready var bug_input: LineEdit = %BugInput
@onready var add_bug_button: Button = %AddBugButton
@onready var bug_list: ItemList = %BugList
@onready var toggle_bug_button: Button = %ToggleBugButton
@onready var delete_bug_button: Button = %DeleteBugButton

@onready var checklist_list: ItemList = %ChecklistList

func _ready() -> void:
	load_data()
	wire_buttons()
	render_all()

func wire_buttons() -> void:
	tasks_button.pressed.connect(func(): stack.current_tab = 0)
	bugs_button.pressed.connect(func(): stack.current_tab = 1)
	checklist_button.pressed.connect(func(): stack.current_tab = 2)
	add_task_button.pressed.connect(add_task)
	toggle_task_button.pressed.connect(toggle_selected_task)
	delete_task_button.pressed.connect(delete_selected_task)
	add_bug_button.pressed.connect(add_bug)
	toggle_bug_button.pressed.connect(toggle_selected_bug)
	delete_bug_button.pressed.connect(delete_selected_bug)
	checklist_list.item_activated.connect(toggle_checklist_item)

func load_data() -> void:
	if not FileAccess.file_exists(SAVE_PATH):
		save_data()
		return
	var file := FileAccess.open(SAVE_PATH, FileAccess.READ)
	if file == null:
		return
	var parsed = JSON.parse_string(file.get_as_text())
	if typeof(parsed) == TYPE_DICTIONARY:
		data = parsed

func save_data() -> void:
	var file := FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file == null:
		status.text = "Save failed."
		return
	file.store_string(JSON.stringify(data, "\t"))
	status.text = "Saved locally."

func render_all() -> void:
	project_name.text = str(data.get("project_name", "BuildPilot Project"))
	project_goal.text = "Goal: " + str(data.get("goal", "Set a goal."))
	render_tasks()
	render_bugs()
	render_checklist()
	update_progress()

func render_tasks() -> void:
	task_list.clear()
	for task in data.get("tasks", []):
		var prefix := "[x] " if bool(task.get("done", false)) else "[ ] "
		task_list.add_item(prefix + str(task.get("title", "Untitled task")))

func render_bugs() -> void:
	bug_list.clear()
	for bug in data.get("bugs", []):
		var prefix := "[fixed] " if bool(bug.get("fixed", false)) else "[open] "
		bug_list.add_item(prefix + str(bug.get("title", "Untitled bug")))

func render_checklist() -> void:
	checklist_list.clear()
	for item in data.get("checklist", []):
		var prefix := "[x] " if bool(item.get("done", false)) else "[ ] "
		checklist_list.add_item(prefix + str(item.get("title", "Checklist item")))

func update_progress() -> void:
	var tasks: Array = data.get("tasks", [])
	if tasks.is_empty():
		progress.value = 0
		return
	var done_count := 0
	for task in tasks:
		if bool(task.get("done", false)):
			done_count += 1
	progress.value = float(done_count) / float(tasks.size()) * 100.0

func add_task() -> void:
	var title := task_input.text.strip_edges()
	if title.is_empty():
		status.text = "Type a task first."
		return
	data["tasks"].append({"title": title, "done": false})
	task_input.text = ""
	save_data()
	render_all()

func selected_index(list: ItemList) -> int:
	var selected := list.get_selected_items()
	if selected.is_empty():
		return -1
	return selected[0]

func toggle_selected_task() -> void:
	var index := selected_index(task_list)
	if index < 0:
		status.text = "Select a task first."
		return
	data["tasks"][index]["done"] = not bool(data["tasks"][index].get("done", false))
	save_data()
	render_all()

func delete_selected_task() -> void:
	var index := selected_index(task_list)
	if index < 0:
		status.text = "Select a task first."
		return
	data["tasks"].remove_at(index)
	save_data()
	render_all()

func add_bug() -> void:
	var title := bug_input.text.strip_edges()
	if title.is_empty():
		status.text = "Type a bug first."
		return
	data["bugs"].append({"title": title, "fixed": false})
	bug_input.text = ""
	save_data()
	render_all()

func toggle_selected_bug() -> void:
	var index := selected_index(bug_list)
	if index < 0:
		status.text = "Select a bug first."
		return
	data["bugs"][index]["fixed"] = not bool(data["bugs"][index].get("fixed", false))
	save_data()
	render_all()

func delete_selected_bug() -> void:
	var index := selected_index(bug_list)
	if index < 0:
		status.text = "Select a bug first."
		return
	data["bugs"].remove_at(index)
	save_data()
	render_all()

func toggle_checklist_item(index: int) -> void:
	if index < 0 or index >= data["checklist"].size():
		return
	data["checklist"][index]["done"] = not bool(data["checklist"][index].get("done", false))
	save_data()
	render_all()
