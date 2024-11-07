package objects

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import models.TaskModel
import setDateFromString
import setDeadlineFromString

class ContentProvider {
    val tasks = mutableStateOf<List<TaskModel>>(listOf())
    val homeHeaders = mutableStateListOf("Title", "Category", "Description", "Deadline", "State", "Status")
}

//TaskModel().apply {
//    this.title = "New YATDO Task"
//    this.deadline = setDateFromString("2024-11-9 22:13")
//    this.description = """This is a new task to be done. It was created during yatdo test""".trimIndent()
//    this.category = "Test"
//    this.priority = TaskModel.PRIORITY_HIGH
//    this.progress = 0.23f
//    this.createdAt = setDateFromString("2024-11-2 22:13")?:0L
//    this.state = State.NOT_STARTED
//    this.id = "123456789"
//}