package objects

import contentProvider
import kotlinx.coroutines.*
import models.TaskModel
import models.TaskPath

class ContentRepository(private val database:Database) {
    val repositoryScope = CoroutineScope(SupervisorJob()+Dispatchers.IO)
    val mainScope  = CoroutineScope(Dispatchers.Main)
    init {
        getTasks {
            contentProvider.tasks.value = it
        }
    }

    fun runInit(callBack:()->Unit={}){
        getTasks {
            contentProvider.tasks.value = it
            callBack.invoke()
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
}