package objects

import models.TaskModel
import models.TaskPath

interface DataSource {
    fun getAllTasks(taskPath: TaskPath):Result<List<TaskModel>>
    fun getTask(taskPath: TaskPath):Result<TaskModel?>
    fun updateTask(taskPath: TaskPath, data:TaskModel):Result<TaskModel>
    fun createTask(taskPath: TaskPath, data:TaskModel):Result<TaskModel>
    fun deleteTask(taskPath: TaskPath):Result<Boolean>
    fun getCachedDatabase():List<TaskModel>
}