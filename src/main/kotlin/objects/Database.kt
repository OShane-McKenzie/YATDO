package objects

import contentProvider
import deleteFile
import exists
import getCurrentDateTime
import ifNotEmpty
import ifNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import listFilesAndDirectories
import models.Config
import models.FileListOptions
import models.TaskModel
import models.TaskPath
import readFile
import writeFile
import yatDoLog
import java.io.File

object Database: DataSource {
    private val tasks: MutableList<TaskModel> = emptyList<TaskModel>().toMutableList()

    fun getConfig():Result<Config>{
        return try {
            val config = readFile(Path.generalConfig)
            var configModel = Config()
            config.ifNotEmpty { json ->
                val cf = Json.decodeFromString<Config>(json)
                configModel = cf
                yatDoLog(operation = "getConfig", outcome = "Success", exitCode = "0", message = "Config loaded")
            }
            Result.success(configModel)
        }catch (e: Exception){
            yatDoLog(operation = "getConfig", outcome = "Failure", exitCode = "1", message = "Failed to load config: ${e.message}")
            Result.failure(e)
        }

    }
    override fun getAllTasks(taskPath: TaskPath): Result<MutableList<TaskModel>> {
        return try {
            val tasksNames = listFilesAndDirectories(taskPath.path, options = FileListOptions(fileExtensions = listOf("json", "JSON")))
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

    fun updateConfig(config: Config): Result<Config> {
        return try {
            writeFile(Path.generalConfig, Json.encodeToString(config), append = false)
            yatDoLog(operation = "updateConfig", outcome = "Success", exitCode = "0", message = "Config updated")
            Result.success(config)
        } catch (e: Exception) {
            yatDoLog(operation = "updateConfig", outcome = "Failure", exitCode = "1", message = e.message ?: "Unknown error")
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

    fun backupDatabase(name:String=""): Result<String> {
        return try {
            val sourceDir = File(Path.database)
            val backupDir = File(Path.backup)

            // Check if source directory exists
            if (!sourceDir.exists() || !sourceDir.isDirectory) {
                return Result.failure(Exception("Source database directory does not exist or is not a directory"))
            }

            // Create a timestamped backup subdirectory
            val timestamp = getCurrentDateTime().replace(":", "-").replace(" ", "_")
            val sanitizedName = name.replace(":", "-").replace(" ", "_")
            val timestampedBackupDir = File("${backupDir.absolutePath}/${sanitizedName}Backup_$timestamp")
            timestampedBackupDir.mkdirs()

            // Copy all files from the database directory to the timestamped backup directory
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val targetFile = File("${timestampedBackupDir.absolutePath}/${file.name}")
                    file.copyTo(targetFile, overwrite = true)
                    println("File backed up: ${file.name}")
                }
            }

            yatDoLog(
                operation = "backupDatabase",
                outcome = "Success",
                exitCode = "0",
                message = "Database backed up successfully to ${timestampedBackupDir.absolutePath}"
            )
            Result.success("Database backed up successfully to ${timestampedBackupDir.absolutePath}")
        } catch (e: Exception) {
            yatDoLog(
                operation = "backupDatabase",
                outcome = "Failure",
                exitCode = "1",
                message = e.message ?: "Unknown error during database backup"
            )
            Result.failure(e)
        }
    }

    fun restoreDatabaseFromBackup(backupName: String): Result<Boolean> {
        val backupFolder = "${Path.backup}/$backupName"
        val databaseFolder = Path.database

        return try {
            val backupDirectory = File(backupFolder)
            if (!backupDirectory.exists() || !backupDirectory.isDirectory) {
                throw IllegalArgumentException("Backup folder '$backupName' does not exist or is not a directory")
            }

            // Clear the existing database folder
            File(databaseFolder).deleteRecursively()
            File(databaseFolder).mkdirs()

            // Copy each file from the backup folder to the database folder
            backupDirectory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val destination = File(databaseFolder, file.relativeTo(backupDirectory).path)
                    destination.parentFile.mkdirs()
                    file.copyTo(destination, overwrite = true)
                }
            }

            yatDoLog(operation = "restoreDatabaseFromBackup", outcome = "Success", exitCode = "0", message = "Database restored from backup '$backupName'")
            Result.success(true)
        } catch (e: Exception) {
            yatDoLog(operation = "restoreDatabaseFromBackup", outcome = "Failure", exitCode = "1", message = e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

}