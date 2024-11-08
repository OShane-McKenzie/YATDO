package objects

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import models.Config
import models.TaskModel
import setDateFromString
import setDeadlineFromString

class ContentProvider {
    val tasks = mutableStateOf<List<TaskModel>>(listOf())
    val homeHeaders = mutableStateListOf("Title", "Category", "Description", "Deadline", "State", "Status")
    val atRiskTaskNotificationTracker = mutableStateListOf<String>()
    val overdueTaskNotificationTracker = mutableStateListOf<String>()
    val taskUpdater = mutableStateOf(0)
    val searchText = mutableStateOf("")
    val config = mutableStateOf(Config())
}
