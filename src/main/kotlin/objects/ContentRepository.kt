package objects

import contentProvider
import kotlinx.coroutines.*
import models.Config
import models.TaskModel
import models.TaskPath

class ContentRepository(private val database:Database) {
    val repositoryScope = CoroutineScope(SupervisorJob()+Dispatchers.IO)
    val mainScope  = CoroutineScope(Dispatchers.Main)
    init {
//        getTasks {
//            contentProvider.tasks.value = it
//        }
    }

    fun runInit(callBack:()->Unit={}){
        database.getConfig()
            .onSuccess { config ->
                contentProvider.config.value = config
                getTasks { tasks ->
                    if(!config.loadArchive){
                        contentProvider.tasks.value = tasks.filter { !it.isArchived }
                    }else{
                        contentProvider.tasks.value = tasks
                    }
                    callBack.invoke()
                }
            }
            .onFailure {
                getTasks {
                    contentProvider.tasks.value = it
                    callBack.invoke()
                }
            }
    }

    fun getTasks(failure: (Throwable) -> Unit = {}, success:(MutableList<TaskModel>) -> Unit = {}){
        repositoryScope.launch {
            val allTasks = database.getAllTasks(taskPath = TaskPath(path = Path.database))
            allTasks
                .onSuccess {
                    success(it)
                }
                .onFailure {
                    failure(it)
                }
        }
    }

    fun createTask(taskPath: TaskPath, data: TaskModel, failure: (Throwable) -> Unit = {}, success:(TaskModel) -> Unit = {}){
        repositoryScope.launch {
            database.backupDatabase()
            database.createTask(taskPath = taskPath, data = data)
                .onSuccess {
                    success.invoke(it)
                }
                .onFailure {
                    failure.invoke(it)
                }
        }
    }

    fun updateTask(taskPath: TaskPath, data: TaskModel, failure: (Throwable) -> Unit = {}, success:(TaskModel) -> Unit = {}){
        repositoryScope.launch {
            //database.backupDatabase()
            database.updateTask(taskPath = taskPath, data = data)
                .onSuccess {
                    success.invoke(it)
                }
                .onFailure {
                    failure.invoke(it)
                }
        }
    }

    fun deleteTask(
        taskPath: TaskPath,
        failure: (Throwable) -> Unit = {},
        success:(Boolean) -> Unit = {}
    ){
        repositoryScope.launch {
            database.backupDatabase()
            database.deleteTask(taskPath)
                .onSuccess {
                    success.invoke(it)
                }
                .onFailure {
                    failure.invoke(it)
                }
        }
    }

    fun taskEmitter(){
        repositoryScope.launch {
            while (true){
                mainScope.launch {
                    contentProvider.taskUpdater.value++
                }
                delay(300000)
            }
        }
    }
    fun backUpDatabase(name:String="Auto-"):Result<String> {
        return database.backupDatabase(name)
    }

    fun updateConfig():Result<Config> = database.updateConfig(contentProvider.config.value)

    fun restoreFromBackup(backUp:String):Result<Boolean>{
        return database.restoreDatabaseFromBackup(backUp)
    }
}