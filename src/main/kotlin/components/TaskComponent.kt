@file:Suppress("FunctionName")

package components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import contentProvider
import getDateAsString
import getDeadlineAsString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.TaskModel
import nativeSysCalls
import objects.*
import objects.State

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskComponent(
    modifier: Modifier = Modifier,
    task:TaskModel,
    onInlineEdit:(TaskModel)->Unit={},
    onDelete:(TaskModel)->Unit ={},
    showArchivedTask:Boolean = true,
    onEdit:(TaskModel)->Unit={}
){
    val derivedTask by remember {
        derivedStateOf {
            task
        }
    }
    var progressColor by remember {
        mutableStateOf(Color.Cyan)
    }
    var taskStateChange by rememberSaveable { mutableStateOf(0) }
    var isTaskBarExpanded by rememberSaveable { mutableStateOf(false) }
    var additionalModifier by remember { mutableStateOf(Modifier.height(YatdoDataTypes.Fibonacci.EIGHTY_NINE.dp)) }
    var progressUpdate by remember { mutableStateOf("") }
    var progressPercentage by remember {
        mutableStateOf(0f)
    }
    var showSnackBar by rememberSaveable() {
        mutableStateOf(false)
    }
    var info by rememberSaveable() {
        mutableStateOf("")
    }
    val scope = rememberCoroutineScope()
    var checkForNotification by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(Unit, taskStateChange){
        if(taskStateChange == Int.MAX_VALUE-1) taskStateChange = 1
        if(derivedTask.isArchived) {
            progressColor = Color.Gray
            if(taskStateChange>0){
                onInlineEdit.invoke(derivedTask)
            }
            return@LaunchedEffect
        }
        if(derivedTask.progress >= 1f){
            derivedTask.state = State.COMPLETED
            if(derivedTask.completedAt == null){
                derivedTask.completedAt = System.currentTimeMillis()
            }
        }else if(derivedTask.state != State.NOT_STARTED && derivedTask.state != State.PAUSED && derivedTask.progress < 1f){
            derivedTask.state = State.IN_PROGRESS
        }
        if(derivedTask.state != State.PAUSED){
            derivedTask.status = derivedTask.calculateStatus()
        }


        progressColor = when (derivedTask.state) {
            State.COMPLETED -> {
                Color(0xff027002)
            }
            State.PAUSED -> {
                Color.Gray
            }
            State.NOT_STARTED-> {
                Color(0xff000000)
            }
            else -> {
                when(derivedTask.status){
                    Status.AT_RISK ->{Color(0xffda6c38)}
                    Status.OVERDUE ->{Color.Red}
                    Status.ON_TRACK ->{Color.Blue}
                    else ->{Color.Blue}
                }
            }
        }

        if(derivedTask.state != State.NOT_STARTED) {
            progressPercentage = (derivedTask.progress * 100f)
        }

        if(taskStateChange>0){
            onInlineEdit.invoke(derivedTask)
        }
        checkForNotification++
    }

    LaunchedEffect(checkForNotification){
        if(derivedTask.state == State.PAUSED){
            return@LaunchedEffect
        }
        scope.launch {
            delay(2000)

            when(derivedTask.status){
                Status.AT_RISK ->{
                    if(derivedTask.id !in contentProvider.atRiskTaskNotificationTracker){
                        nativeSysCalls.sendNotification(
                            title = "${derivedTask.title} - AT RISK",
                            body = "${derivedTask.title} is due ${derivedTask.deadline?.getDateAsString(includeTime = true)}\nand is at risk of becoming overdue at the current rate.",
                            icon = NotificationIcons.DIALOG_WARNING,
                            sound = NotificationSounds.WARNING
                        ){
                            nativeSysCalls.cleanup()
                        }
                        contentProvider.atRiskTaskNotificationTracker.add(derivedTask.id)
                    }

                }
                Status.OVERDUE ->{
                    if(derivedTask.id !in contentProvider.overdueTaskNotificationTracker) {
                        nativeSysCalls.sendNotification(
                            title = "${derivedTask.title} - OVERDUE",
                            body = "${derivedTask.title} is now overdue ${
                                derivedTask.deadline?.getDateAsString(
                                    includeTime = true
                                )
                            }\nand is ${derivedTask.getDaysBehindSchedule()} days behind schedule.",
                            icon = NotificationIcons.DIALOG_WARNING,
                            sound = NotificationSounds.WARNING
                        ) {
                            nativeSysCalls.cleanup()
                        }
                        contentProvider.overdueTaskNotificationTracker.add(derivedTask.id)
                    }
                }
            }
        }

    }
    LaunchedEffect(isTaskBarExpanded){
        additionalModifier = if(isTaskBarExpanded){
            Modifier.wrapContentHeight()
        }else{
            Modifier.height(YatdoDataTypes.Fibonacci.EIGHTY_NINE.dp)
        }
    }
    LaunchedEffect(contentProvider.taskUpdater.value){
        if(derivedTask.isArchived) {
            return@LaunchedEffect
        }else{
            taskStateChange++
        }
    }
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xffbaa4a8),
            progressColor.copy(alpha = 0.2f)
        )
    )
    @Composable
    fun taskBody(){
        Box(modifier = modifier.wrapContentHeight()) {
            Column(
                modifier = Modifier
                    .clickable {
                        isTaskBarExpanded = !isTaskBarExpanded
                    }
                    .then(additionalModifier)
                    .align(Alignment.TopCenter)
                    .animateContentSize()
                    .background(gradientBackground, shape = RoundedCornerShape(YatdoDataTypes.Fibonacci.TWENTY_ONE.dp))
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(YatdoDataTypes.Fibonacci.TWENTY_ONE.dp))
                    .padding(5.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(13.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f).padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(derivedTask.title, fontSize = 25.sp, fontWeight = FontWeight.Bold,modifier = Modifier.basicMarquee())
                        if(derivedTask.status == Status.AT_RISK && isTaskBarExpanded) {
                            Text("${derivedTask.title} is due ${derivedTask.deadline?.getDateAsString(includeTime = true)}\nand is at risk of becoming overdue at the current rate.",
                                color = progressColor, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                        if(derivedTask.status == Status.OVERDUE && isTaskBarExpanded) {
                            Text("${derivedTask.title} is now overdue ${
                                derivedTask.deadline?.getDateAsString(
                                    includeTime = true
                                )
                            }\nand is ${derivedTask.getDaysBehindSchedule()} days behind schedule.",
                                color = progressColor, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    ////////////////////////////////

                    Column(
                        modifier = Modifier.weight(1f).padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(derivedTask.category, fontSize = 21.sp, modifier = Modifier.basicMarquee())
                    }
                    /////////////////////////////////

                    Column(
                        modifier = Modifier.weight(1f).padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (!isTaskBarExpanded) {
                            Text(
                                derivedTask.description,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 18.sp
                            )
                        } else {
                            Text(
                                "Date created: ${derivedTask.createdAt.getDateAsString(includeTime = true) ?: ""}",
                                fontSize = 18.sp,
                                modifier = Modifier.basicMarquee(),
                                maxLines = 1
                            )
                            Text(
                                "Date completed: ${derivedTask.completedAt?.getDateAsString() ?: "Pending"}",
                                fontSize = 18.sp,
                                modifier = Modifier.basicMarquee(),
                                maxLines = 1
                            )
                            Text(
                                "Group: ${derivedTask.groupId}",
                                fontSize = 18.sp,
                                modifier = Modifier.basicMarquee(),
                                maxLines = 1
                            )
                            Text(
                                "Recommended Progress Rate: ${"%.2f".format((derivedTask.getRequiredDailyProgress() ?: 0f) * 100f)}% per day",
                                fontSize = 18.sp,
                                modifier = Modifier.basicMarquee(),
                                maxLines = 1
                            )
                            if ((derivedTask.status == Status.AT_RISK || derivedTask.status == Status.OVERDUE) && derivedTask.state != State.PAUSED) {
                                Text(
                                    "Days behind schedule: ${"%.2f".format(derivedTask.getDaysBehindSchedule())}".replace("-",""),
                                    fontSize = 18.sp,
                                    modifier = Modifier.basicMarquee(),
                                    maxLines = 1,
                                    color = Color.Red
                                )

                                Text(
                                    "Days overdue: ${"%.0f".format(derivedTask.getDaysOverdue()?:0f)}",
                                    fontSize = 18.sp,
                                    modifier = Modifier.basicMarquee(),
                                    maxLines = 1,
                                    color = if ((derivedTask.getDaysOverdue() ?: 0f) > 0f) Color.Red else Color.Black
                                )
                            }
                            Text("")

                            OutlinedTextField(
                                value = derivedTask.description,
                                onValueChange = {},
                                label = { Text("Description", fontSize = 21.sp) },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    /////////////////////////////////

                    Column(
                        modifier = Modifier.weight(1f).padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            derivedTask.getDeadlineAsString(includeTime = true) ?: Status.NO_DEADLINE,
                            fontSize = 21.sp,
                            modifier = Modifier.basicMarquee(),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    /////////////////////////////////

                    Column(
                        modifier = Modifier.weight(1f).padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(derivedTask.state, fontSize = 21.sp, modifier = Modifier.basicMarquee())
                    }
                    /////////////////////////////////

                    Column(
                        modifier = Modifier.weight(1f).padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            derivedTask.status,
                            fontSize = 21.sp,
                            modifier = Modifier.basicMarquee(),
                            fontStyle = if (derivedTask.status == Status.AT_RISK || derivedTask.status == Status.OVERDUE || derivedTask.state == State.COMPLETED) {
                                FontStyle.Italic
                            } else {
                                FontStyle.Normal
                            },
                            fontWeight = if (derivedTask.status == Status.AT_RISK || derivedTask.status == Status.OVERDUE || derivedTask.state == State.COMPLETED) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                            color = progressColor
                        )
                    }
                    /////////////////////////////////
                }

                if (isTaskBarExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.height(YatdoDataTypes.Fibonacci.FIFTY_FIVE.dp).fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                if (
                                    derivedTask.state != State.IN_PROGRESS &&
                                    derivedTask.state != State.PAUSED &&
                                    derivedTask.state != State.COMPLETED
                                ) {
                                    derivedTask.state = State.IN_PROGRESS
                                    derivedTask.status = derivedTask.calculateStatus()
                                    taskStateChange++

                                } else {
                                    info = "The task has already begun and is currently ${derivedTask.state}"
                                    showSnackBar = true
                                }
                            },
                            enabled = !derivedTask.isArchived
                        ) {
                            Text("Begin Task", fontSize = 21.sp)
                        }
                        TextButton(
                            onClick = {
                                onEdit.invoke(derivedTask)
                            },
                            enabled = !derivedTask.isArchived
                        ) {
                            Text("Edit task", fontSize = 21.sp)
                        }
                        TextButton(
                            onClick = {
                                if (derivedTask.state == State.IN_PROGRESS && derivedTask.progress < 1f) {
                                    derivedTask.state = State.PAUSED
                                    taskStateChange++

                                } else if (derivedTask.state == State.PAUSED && derivedTask.progress < 1f) {
                                    derivedTask.state = State.IN_PROGRESS
                                    taskStateChange++

                                } else if (derivedTask.progress >= 1f) {
                                    derivedTask.state = State.COMPLETED
                                    taskStateChange++

                                }
                            },
                            enabled = !derivedTask.isArchived
                        ) {
                            Text(
                                if (derivedTask.state == State.PAUSED) {
                                    "Resume task"
                                } else {
                                    "Pause task"
                                }, fontSize = 21.sp
                            )
                        }
                        TextButton(
                            onClick = {
                                derivedTask.isArchived = !derivedTask.isArchived
                                if (derivedTask.isArchived) {
                                    derivedTask.state = State.ARCHIVED
                                    derivedTask.status = derivedTask.calculateStatus()
                                }
                                taskStateChange++
                            }
                        ) {
                            Text(if(derivedTask.isArchived) "Unarchive Task" else "Archive task", fontSize = 21.sp)
                        }
                        OutlinedTextField(
                            value = progressUpdate,
                            onValueChange = {
                                if (it.toFloatOrNull() != null) {
                                    progressUpdate = it
                                }
                            },
                            placeholder = {
                                Text("Update progress percentage")
                            },
                            trailingIcon = {
                                TextButton(onClick = {
                                    if (progressUpdate.toFloatOrNull() != null) {
                                        val progress = (progressUpdate.toFloat() / 100f)
                                        task.progress = progress
                                        taskStateChange++
                                        progressUpdate = ""

                                    }
                                }) {
                                    Text("Update")
                                }
                            },
                            enabled = !derivedTask.isArchived
                        )
                        TextButton(
                            onClick = {
                                if (derivedTask.state != State.COMPLETED) {
                                    derivedTask.progress = 1f
                                    derivedTask.completedAt = System.currentTimeMillis()
                                    taskStateChange++
                                }
                            },
                            enabled = !derivedTask.isArchived
                        ) {
                            Text("Mark as Completed", fontSize = 21.sp)
                        }
                        TextButton(
                            onClick = {
                                onDelete.invoke(derivedTask)
                            }
                        ) {
                            Text("Cancel task", color = Color.Red, fontSize = 21.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                LinearProgressIndicator(
                    progress = if (progressPercentage <= 0f) 0f else derivedTask.progress,
                    modifier = Modifier.fillMaxWidth(0.9f).height(8.dp),
                    color = progressColor,
                    backgroundColor = Color.White.copy(alpha = 0.5f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    "${"%.2f".format(progressPercentage)}% Complete.",
                    modifier = Modifier.padding(0.dp).basicMarquee(),
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (showSnackBar) {
                CustomSnackBar(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    info = info,
                    isVisible = true
                ) {
                    showSnackBar = false
                }
            }
            Spacer(modifier = Modifier.height(YatdoDataTypes.Fibonacci.TWENTY_ONE.dp).align(Alignment.BottomCenter))
        }
    }
    if(derivedTask.isArchived) {
        SimpleAnimator(
            isVisible = showArchivedTask,
            style = AnimationStyle.DOWN
        ) {
            Column{
                taskBody()
                Spacer(modifier = Modifier.height(13.dp))
            }
        }

    }else {
        SimpleAnimator(
            style = AnimationStyle.DOWN
        ) {
            Column{
                taskBody()
                Spacer(modifier = Modifier.height(13.dp))
            }
        }
    }

}