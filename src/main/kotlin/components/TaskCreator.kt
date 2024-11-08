@file:Suppress("FunctionName")

package components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import contentProvider
import contentRepository
import generateUniqueID
import getDateAsString
import ifNotNull
import ifNull
import models.TaskModel
import models.TaskModel.Companion.PRIORITY_NORMAL
import models.TaskPath
import objects.*
import objects.State

@Composable
fun TaskCreator(
    modifier: Modifier = Modifier,
    creationType: CreationType = CreationType.CREATE,
    task: TaskModel? = null,
    onClose:()->Unit = {},
    callBack:(TaskModel?)->Unit = {}
) {
    val scrollState = rememberScrollState()
    // List of options for the radio buttons
    val options = remember{
        listOf(
            TaskModel.PRIORITY_LOW,
            TaskModel.PRIORITY_NORMAL,
            TaskModel.PRIORITY_HIGH,
            TaskModel.PRIORITY_URGENT
        )
    }

    var showSnackBar by rememberSaveable() {
        mutableStateOf(false)
    }
    var info by rememberSaveable() {
        mutableStateOf("")
    }
    var showNoDeadlineAlert by rememberSaveable{
        mutableStateOf(false)
    }
    var localTask by remember {
        mutableStateOf(if (creationType == CreationType.CREATE) {
            TaskModel().apply {
                this.id = generateUniqueID(contentProvider.tasks.value)
            }
        } else {
            task?.copy()
        })
    }
    var selectedOption by remember { mutableStateOf(localTask?.priority?:0) }
    var deadlineString by rememberSaveable{
        mutableStateOf(localTask?.deadline?.getDateAsString(includeTime = true)?:"No Deadline")
    }
    fun taskManager(){
        if(creationType == CreationType.CREATE) {
            localTask
                .ifNotNull {
                    contentRepository.createTask(
                        taskPath = TaskPath(
                            id = it.id,
                            path = Path.database
                        ),
                        data = it,
                        failure = { e ->
                            info = "Failure: ${e.message}"
                            showSnackBar = true
                        }
                    ) {
                        callBack.invoke(localTask)
                        onClose()
                    }
                }
                .ifNull {
                    info = "Failure: Task is null"
                    showSnackBar = true
                }
        }else if(creationType == CreationType.EDIT){
            localTask
                .ifNotNull {
                    contentRepository.updateTask(
                        taskPath = TaskPath(
                            id = it.id,
                            path = Path.database
                        ),
                        data = it,
                        failure = { e ->
                            info = "Failure: ${e.message}"
                            showSnackBar = true
                        }
                    ) {
                        callBack.invoke(localTask)
                        onClose()
                    }
                }
                .ifNull {
                    info = "Failure: Task is null"
                    showSnackBar = true
                }
        }
    }
    Box(modifier = modifier.padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("ID: ${localTask?.id}", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = localTask?.title.orEmpty(),
                onValueChange = {
                    localTask = localTask?.copy(
                        title = it
                    )
                },
                label = { Text("Title", fontSize = 20.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 30.sp
                )
            )
            OutlinedTextField(
                value = localTask?.description.orEmpty(),
                onValueChange = {
                    localTask = localTask?.copy(
                        description = it
                    )
                },
                label = { Text("Description", fontSize = 20.sp) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 30.sp
                )
            )
            OutlinedTextField(
                value = localTask?.category.orEmpty(),
                onValueChange = {
                    localTask = localTask?.copy(
                        category = it
                    )
                },
                label = { Text("Category", fontSize = 20.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 30.sp
                )
            )
            OutlinedTextField(
                value = localTask?.groupId.orEmpty(),
                onValueChange = {
                    localTask = localTask?.copy(
                        groupId = it
                    )
                },
                label = { Text("Group ID", fontSize = 20.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 30.sp
                )
            )

            Column{
                Text(text = "Priority: ${
                    when(selectedOption){
                        TaskModel.PRIORITY_LOW->{"Low"}
                        TaskModel.PRIORITY_NORMAL->{"Normal"}
                        TaskModel.PRIORITY_HIGH->{"High"}
                        TaskModel.PRIORITY_URGENT->{"Urgent"}
                        else->""
                    }
                }", fontSize = 21.sp)
                Row {
                    // Display each radio button
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (option == selectedOption),
                                onClick = {
                                    selectedOption = option
                                    localTask = localTask?.copy(
                                        priority = selectedOption
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "$option", fontSize = 21.sp)
                        }
                    }
                }
            }

            Text(
                text = "Deadline: $deadlineString",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            DateTimeComponent(modifier = Modifier.fillMaxWidth(0.8f)){
                    l,s->
                if(l==null || s==null){
                    info = "Wrong date format"
                    showSnackBar = true
                }else {
                    localTask = localTask?.copy(
                        deadline = l
                    ).apply {
                        deadlineString = s
                    }

                }
            }
            Spacer(modifier = Modifier.height(YatdoDataTypes.Fibonacci.EIGHTY_NINE.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                TextButton(onClick = {
                    onClose.invoke()
                }){
                    Text(
                        text = "Cancel",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = Color.Red
                    )
                }
                TextButton(onClick = {
                    if (deadlineString.contains("Deadline", ignoreCase = true)){
                        showNoDeadlineAlert = true
                    }else{
                        taskManager()
                    }
                }){
                    Text(
                        text = "Save",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = Color.Blue
                    )

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

        SimpleAnimator(
            modifier = Modifier
                .align(Alignment.Center),
            isVisible = showNoDeadlineAlert,
            style = AnimationStyle.SCALE_IN_CENTER
        ) {
            Column (
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.5f)
                    .background(color = Color.Black, shape = RoundedCornerShape(3))
                    .padding(5.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "No deadline has been set, do you wish to create this task without a deadline?",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(13.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    Button(onClick = {
                        showNoDeadlineAlert = false
                    }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White
                    )){
                        Text(
                            text = "No",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = Color.Blue
                        )
                    }
                    Button(onClick = {
                        taskManager()
                    }, colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White
                    )){
                        Text(
                            text = "Yes, create task without a deadline.",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = Color.Red
                        )
                    }
                }
            }
        }


        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
        IconButton(
            onClick = {
            onClose.invoke()
        },modifier = Modifier.align(Alignment.TopEnd)){
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "close"
            )
        }
    }

}
