@file:Suppress("FunctionName")

package screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.*
import contentProvider
import contentRepository
import getDateAsString
import ifNotNull
import ifNull
import kotlinx.coroutines.delay
import models.TaskModel
import models.TaskPath
import objects.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Home(modifier: Modifier = Modifier){
    var selectedTask by rememberSaveable{
        mutableStateOf<TaskModel?>(null)
    }
    var creationType by rememberSaveable{
        mutableStateOf(CreationType.CREATE)
    }

    var showDeleteTaskDialog by remember {
        mutableStateOf(false)
    }
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFDF4F5),
            Color.Blue
        )
    )
    var showSnackBar by rememberSaveable() {
        mutableStateOf(false)
    }
    var showTaskCreator by rememberSaveable() {
        mutableStateOf(false)
    }
    var info by rememberSaveable() {
        mutableStateOf("")
    }
    val scrollState = rememberScrollState()
    var filteredTasks by remember { mutableStateOf(
        contentProvider.tasks.value
    ) }
    var taskListController by rememberSaveable { mutableStateOf(0) }
    var filterText by remember { mutableStateOf("") }
    var showMenu by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(taskListController, contentProvider.tasks.value){
        filteredTasks = emptyList()
        delay(20)
        filteredTasks = contentProvider.tasks.value
    }
    LaunchedEffect(true){
        contentRepository.taskEmitter()
    }

    Box(modifier = Modifier.fillMaxSize().background(color = Color(0xffc8c8c8)).padding(13.dp)){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(YatdoDataTypes.Fibonacci.EIGHT.dp),
        ) {
            Column(
                modifier = Modifier
                    .background(gradientBackground, shape = RoundedCornerShape(YatdoDataTypes.Fibonacci.TWENTY_ONE.dp))
                    .fillMaxWidth()
                    .fillMaxHeight(0.09f)
                    .padding(YatdoDataTypes.Fibonacci.THREE.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    contentProvider.homeHeaders.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.weight(1f).padding(YatdoDataTypes.Fibonacci.FIVE.dp)
                        ) {
                            TitleText(text = item, modifier = Modifier.basicMarquee().weight(1f))
                            if (index != contentProvider.homeHeaders.lastIndex) {
                                Column(
                                    modifier.weight(0.01f).fillMaxHeight().width(3.dp)
                                        .border(width = 3.dp, color = Color.White)
                                ) { }
                            }
                        }
                    }
                }
            }

            //Should probably use a LazyColumn for this
            Column(
                modifier = Modifier.fillMaxWidth().wrapContentHeight().verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(YatdoDataTypes.Fibonacci.TWENTY_ONE.dp))
                filteredTasks.sortedWith(compareBy<TaskModel> { it.groupId }.thenBy { it.deadline }).forEach { task ->
                    if(
                        task.title.contains(filterText, ignoreCase = true) ||
                        task.description.contains(filterText, ignoreCase = true) ||
                        task.state.contains(filterText, ignoreCase = true) ||
                        task.status.contains(filterText, ignoreCase = true) ||
                        task.category.contains(filterText, ignoreCase = true) ||
                        task.deadline?.getDateAsString()?.contains(filterText, ignoreCase = true) != false ||
                        task.groupId.contains(filterText, ignoreCase = true)
                    ) {
                        TaskComponent(
                            modifier = Modifier.fillMaxWidth(),
                            task,
                            onInlineEdit = { callbackTask ->
                                callbackTask.ifNotNull { it ->
                                    val foundTask = contentProvider.tasks.value.find { tk ->
                                        tk.id == it.id
                                    }
                                    foundTask
                                        .ifNotNull { fk ->
                                            val tasks = contentProvider.tasks.value.toMutableList()
                                            val index = tasks.indexOf(fk)
                                            tasks[index] = it
                                            contentProvider.tasks.value = tasks.toList()
                                            contentRepository.updateTask(
                                                taskPath = TaskPath(
                                                    it.id,
                                                    path = Path.database
                                                ), data = it
                                            )
                                        }
                                        .ifNull {
                                            val tasks = contentProvider.tasks.value.toMutableList()
                                            tasks.add(it)
                                            contentProvider.tasks.value = tasks.toList()
                                            contentRepository.createTask(
                                                taskPath = TaskPath(
                                                    it.id,
                                                    path = Path.database
                                                ), data = it
                                            )
                                        }
                                }
                            },
                            onDelete = {
                                selectedTask = it
                                showDeleteTaskDialog = true
                            },
                            showArchivedTask = Controller.showArchivedTasks.value
                        ) {
                            creationType = CreationType.EDIT
                            selectedTask = it
                            showTaskCreator = true
                        }
                        //Spacer(modifier = Modifier.height(8.dp))
                    }
                }

            }
        }
        FloatingActionButton(onClick = {
            creationType = CreationType.CREATE
            selectedTask = null
            showTaskCreator = true
        },Modifier.align(Alignment.BottomEnd)){
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = ""
            )
        }

        SimpleAnimator(
            isVisible = showMenu,
            style = AnimationStyle.LEFT,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            NavigationMenu(
                modifier = Modifier.align(Alignment.TopStart). fillMaxWidth(0.3f).fillMaxHeight(),
            ) {
                filterText = it
            }
        }

        IconButton(
            onClick = {showMenu = !showMenu},
            modifier = Modifier.align(Alignment.TopStart).size(YatdoDataTypes.Fibonacci.FIFTY_FIVE.dp)){
            Icon(
                modifier = Modifier.size(YatdoDataTypes.Fibonacci.FIFTY_FIVE.dp)
                    .background(color = Color.Black.copy(alpha = 0.6f), shape = CircleShape),
                imageVector = Icons.Default.Menu,
                tint = Color.White,
                contentDescription = "Menu"
            )
        }

        SimpleAnimator(
            modifier = Modifier.align(Alignment.Center),
            isVisible = showTaskCreator,
            style = AnimationStyle.SCALE_IN_CENTER
        ) {
            TaskCreator(
                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                creationType = creationType,
                task = selectedTask,
                onClose = {showTaskCreator = false}
            ){ callbackTask->
                callbackTask.ifNotNull { it->
                    val foundTask = contentProvider.tasks.value.find { tk->
                        tk.id == it.id
                    }
                    foundTask
                        .ifNotNull { fk ->
                            val tasks = contentProvider.tasks.value.toMutableList()
                            val index = tasks.indexOf(fk)
                            tasks[index] = it
                            contentProvider.tasks.value = tasks.toList()
                            taskListController++
                            //contentRepository.runInit()
                        }
                        .ifNull {
                            val tasks = contentProvider.tasks.value.toMutableList()
                            tasks.add(it)
                            contentProvider.tasks.value = tasks.toList()
                            taskListController++
                            //contentRepository.runInit()
                        }
                }

            }
        }
        if(showSnackBar) {
            CustomSnackBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                info = info,
                isVisible = true
            ) {
                showSnackBar = false
            }
        }

        VerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState), modifier = Modifier.align(Alignment.CenterEnd))

        if(showDeleteTaskDialog) {
            AlertDialog(
                modifier = Modifier.align(Alignment.Center),
                onDismissRequest = {
                    selectedTask = null
                    showDeleteTaskDialog =  false
                },
                title = { Text("Cancel Task", color = Color.Red, fontSize = 30.sp) },
                text = {
                    Text(
                        "Are you sure you want to cancel this task? It will be deleted and cannot be undone.",
                        color = Color.Red,
                        fontSize = 20.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedTask.ifNotNull { task ->
                                contentRepository.deleteTask(taskPath = TaskPath(task.id, Path.database), failure = {
                                    info = "Failure: ${it.message}"
                                }){
                                    if(it){
                                        contentRepository.runInit(){
                                            taskListController++
                                        }
                                        info = "Task ${task.title} has been cancelled."
                                        showSnackBar = true
                                        selectedTask = null
                                        showDeleteTaskDialog = false
                                    }
                                }
                            }
                        }
                    ) {
                        Text("YES, delete task.", color = Color.Red, fontSize = 30.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            selectedTask = null
                            showDeleteTaskDialog = false
                        }
                    ) {
                        Text("NO.", color = Color.Blue, fontSize = 30.sp)
                    }
                }
            )
        }
    }
}