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

object Database:DataSource {
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
            Result.success(tasks)
        }catch (e:Exception){
            Result.failure(e)
        }
    }

    override fun getTask(taskPath: TaskPath): Result<TaskModel?> {
        return  try {
            var task:TaskModel? = null
            val taskJson = readFile("${taskPath.path}/${taskPath.id}.json")
            taskJson.ifNotEmpty { json ->
                task = Json.decodeFromString<TaskModel>(json)
            }
            Result.success(task)
        }catch (e:Exception){
            Result.failure(e)
        }
    }

    override fun updateTask(taskPath: TaskPath, data: TaskModel): Result<TaskModel> {
        return try {
            // Read existing task file
            val taskJson = readFile("${taskPath.path}/${taskPath.id}.json")
            val task = if (taskJson.isNotEmpty()) Json.decodeFromString<TaskModel>(taskJson) else null

            // Apply updates if the task is found
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
                this.lastModifiedAt = System.currentTimeMillis() // Automatically update modification time
            }

            // Save updated task back to file
            task?.let {
                val updatedTaskJson = Json.encodeToString(it)
                writeFile("${taskPath.path}/${taskPath.id}.json", updatedTaskJson, append = false)
                Result.success(it)
            } ?: Result.failure(Exception("Task not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    override fun createTask(taskPath: TaskPath, data: TaskModel): Result<TaskModel> {
        return try {
            // Check if task file already exists to avoid overwriting
            val taskFile = "${taskPath.path}/${taskPath.id}.json"
            if (exists(taskFile)) {
                Result.failure(Exception("Task with ID ${taskPath.id} already exists"))
            } else {
                val taskToSave = data.copy()
                // Serialize and save the task data to file
                writeFile(taskFile, Json.encodeToString(data), append = false)
                tasks.add(taskToSave)
                Result.success(data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun deleteTask(taskPath: TaskPath): Result<Boolean> {
        return try {
            val taskFile = "${taskPath.path}/${taskPath.id}.json"
            if (exists(taskFile)) {
                // Attempt to delete the task file
                deleteFile(taskFile)
                tasks.removeIf { it.id == taskPath.id }
                Result.success(true)
            } else {
                Result.failure(Exception("Task with ID ${taskPath.id} not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun getCachedDatabase(): List<TaskModel> {
        return tasks
    }
}