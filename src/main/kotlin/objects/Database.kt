package objects

import deleteFile
import exists
import ifNotEmpty
import ifNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import listFiles
import models.FileListOptions
import models.TaskModel
import models.TaskPath
import readFile
import writeFile
import yatDoLog

object Database: DataSource {
    private val tasks: MutableList<TaskModel> = emptyList<TaskModel>().toMutableList()

    override fun getAllTasks(taskPath: TaskPath): Result<MutableList<TaskModel>> {
        return try {
            val tasksNames = listFiles(taskPath.path, options = FileListOptions(fileExtensions = listOf("json", "JSON")))
            tasksNames.ifNotNull {
                tasks.clear()
                it.forEach { name ->
                    val taskJson = readFile("${taskPath.path}/$name")
                    taskJson.ifNotEmpty { json ->
                        val task = Json.decodeFromString<TaskModel>(json)
                        tasks.add(task)
                    }
                }
            }
            yatDoLog(operation = "getAllTasks", outcome = "Success", exitCode = "0", message = "Retrieved all tasks")
            Result.success(tasks)
        } catch (e: Exception) {
            yatDoLog(operation = "getAllTasks", outcome = "Failure", exitCode = "1", message = e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override fun getTask(taskPath: TaskPath): Result<TaskModel?> {
        return try {
            var task: TaskModel? = null
            val taskJson = readFile("${taskPath.path}/${taskPath.id}.json")
            taskJson.ifNotEmpty { json ->
                task = Json.decodeFromString<TaskModel>(json)
            }
            yatDoLog(operation = "getTask", outcome = "Success", exitCode = "0", message = "Retrieved task with ID ${taskPath.id}")
            Result.success(task)
        } catch (e: Exception) {
            yatDoLog(operation = "getTask", outcome = "Failure", exitCode = "1", message = e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override fun updateTask(taskPath: TaskPath, data: TaskModel): Result<TaskModel> {
        return try {
            val taskJson = readFile("${taskPath.path}/${taskPath.id}.json")
            val task = if (taskJson.isNotEmpty()) Json.decodeFromString<TaskModel>(taskJson) else null
            task?.apply {
                if (this.title != data.title) this.title = data.title
                if (this.description != data.description) this.description = data.description
                if (this.deadline != data.deadline) this.deadline = data.deadline
                if (this.createdAt != data.createdAt) this.createdAt = data.createdAt
                if (this.state != data.state) this.state = data.state
                if (this.status != data.status) this.status = data.status
                if (this.category != data.category) this.category = data.category
                if (this.groupId != data.groupId) this.groupId = data.groupId
                if (this.progress != data.progress) this.progress = data.progress
                if (this.isArchived != data.isArchived) this.isArchived = data.isArchived
                if (this.completedAt != data.completedAt) this.completedAt = data.completedAt
                if (this.priority != data.priority) this.priority = data.priority
                if (this.tags != data.tags) this.tags = data.tags
                if (this.attachments != data.attachments) this.attachments = data.attachments
                this.lastModifiedAt = System.currentTimeMillis()
            }
            task?.let {
                val updatedTaskJson = Json.encodeToString(it)
                writeFile("${taskPath.path}/${taskPath.id}.json", updatedTaskJson, append = false)
                yatDoLog(operation = "updateTask", outcome = "Success", exitCode = "0", message = "Updated task with ID ${taskPath.id}")
                Result.success(it)
            } ?: Result.failure(Exception("Task not found"))
        } catch (e: Exception) {
            yatDoLog(operation = "updateTask", outcome = "Failure", exitCode = "1", message = e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override fun createTask(taskPath: TaskPath, data: TaskModel): Result<TaskModel> {
        return try {
            val taskFile = "${taskPath.path}/${taskPath.id}.json"
            if (exists(taskFile)) {
                yatDoLog(operation = "createTask", outcome = "Failure", exitCode = "1", message = "Task with ID ${taskPath.id} already exists")
                Result.failure(Exception("Task with ID ${taskPath.id} already exists"))
            } else {
                val taskToSave = data.copy()
                writeFile(taskFile, Json.encodeToString(data), append = false)
                tasks.add(taskToSave)
                yatDoLog(operation = "createTask", outcome = "Success", exitCode = "0", message = "Created task with ID ${taskPath.id}")
                Result.success(data)
            }
        } catch (e: Exception) {
            yatDoLog(operation = "createTask", outcome = "Failure", exitCode = "1", message = e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override fun deleteTask(taskPath: TaskPath): Result<Boolean> {
        return try {
            val taskFile = "${taskPath.path}/${taskPath.id}.json"
            if (exists(taskFile)) {
                deleteFile(taskFile)
                tasks.removeIf { it.id == taskPath.id }
                yatDoLog(operation = "deleteTask", outcome = "Success", exitCode = "0", message = "Deleted task with ID ${taskPath.id}")
                Result.success(true)
            } else {
                yatDoLog(operation = "deleteTask", outcome = "Failure", exitCode = "1", message = "Task with ID ${taskPath.id} not found")
                Result.failure(Exception("Task with ID ${taskPath.id} not found"))
            }
        } catch (e: Exception) {
            yatDoLog(operation = "deleteTask", outcome = "Failure", exitCode = "1", message = e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override fun getCachedDatabase(): List<TaskModel> {
        yatDoLog(operation = "getCachedDatabase", outcome = "Success", exitCode = "0", message = "Retrieved cached tasks")
        return tasks
    }
}