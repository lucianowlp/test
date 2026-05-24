package lucianowlp.com.simplestodo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object TaskStorage {
    private const val PREFS_NAME = "task_storage"
    private const val KEY_TASKS = "tasks_json"

    fun loadTasks(context: Context): List<TaskItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_TASKS, null) ?: return emptyList()

        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    add(
                        TaskItem(
                            id = item.getString("id"),
                            title = item.getString("title"),
                            dueDate = LocalDate.parse(item.getString("dueDate")),
                            completed = item.getBoolean("completed"),
                            completedDate = item.optString("completedDate")
                                .takeIf { it.isNotBlank() }
                                ?.let { LocalDate.parse(it) }
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    fun saveTasks(context: Context, tasks: List<TaskItem>) {
        val jsonArray = JSONArray()
        tasks.forEach { task ->
            val objectItem = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("dueDate", task.dueDate.toString())
                put("completed", task.completed)
                put("completedDate", task.completedDate?.toString().orEmpty())
            }
            jsonArray.put(objectItem)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TASKS, jsonArray.toString()).apply()
    }
}
