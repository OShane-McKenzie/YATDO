@file:Suppress("FunctionName")

package components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import contentProvider
import contentRepository
import ifNotNull
import kotlinx.coroutines.launch
import listFilesAndDirectories
import models.FileListOptions
import objects.Controller
import objects.Path
import objects.YatdoDataTypes

@Composable
fun NavigationMenu(modifier:Modifier = Modifier, onSearch:(String)->Unit={}){
    val interactionSource = remember { MutableInteractionSource() }
    var showSnackBar by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf("") }
    val backups = remember { listFilesAndDirectories(Path.backup, options = FileListOptions(includeOnlyDirectories = true)) }
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
    ){
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ){
                    
                }
                .background(color = Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(
                YatdoDataTypes.Fibonacci.TWENTY_ONE.dp)
            ).fillMaxSize().padding(start = 21.dp,end = 5.dp, top = 5.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(YatdoDataTypes.Fibonacci.EIGHTY_NINE.dp))
            OutlinedTextField(
                value = contentProvider.searchText.value,
                onValueChange = {
                    contentProvider.searchText.value = it
                    onSearch.invoke(contentProvider.searchText.value)
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 25.sp
                ),
                placeholder = { Text("Search Tasks", fontSize = 25.sp, color = Color.White) },
                modifier = Modifier.fillMaxWidth(0.7f).height(60.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    modifier = Modifier.size(34.dp),
                    checked = Controller.showArchivedTasks.value,
                    onCheckedChange = {
                        Controller.showArchivedTasks.value = it
                    },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Show archived tasks", fontSize = 25.sp, color = Color.White)
            }

            Button(
                onClick = {
                    contentRepository.repositoryScope.launch {
                        contentRepository.backUpDatabase()
                            .onSuccess {
                                info = it
                                showSnackBar = true
                            }
                            .onFailure {
                                info = "Error: ${it.message}"
                                showSnackBar = true
                            }
                    }
                }
            ){
                Text(text = "Backup Database", fontSize = 25.sp, color = Color.White)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
                    .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(5)),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Startup settings", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        modifier = Modifier.size(34.dp),
                        checked = Controller.loadArchivedTasks.value,
                        onCheckedChange = { changed ->
                            Controller.loadArchivedTasks.value = changed
                            contentProvider.config.value.loadArchive = Controller.loadArchivedTasks.value
                            contentRepository.repositoryScope.launch {
                                contentRepository.updateConfig()
                                    .onFailure {
                                        info = "Error: ${it.message}"
                                        showSnackBar = true
                                    }
                                    .onSuccess {
                                        contentRepository.runInit()
                                    }
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Load archived tasks.", fontSize = 25.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        modifier = Modifier.size(34.dp),
                        checked = Controller.minimizeToSystemTray.value,
                        onCheckedChange = { changed ->
                            Controller.minimizeToSystemTray.value = changed
                            contentProvider.config.value.tray = Controller.minimizeToSystemTray.value

                            contentRepository.repositoryScope.launch {
                                contentRepository.updateConfig()
                                    .onFailure {
                                        info = "Error: ${it.message}"
                                        showSnackBar = true
                                    }
                                    .onSuccess {
                                        contentRepository.runInit()
                                    }
                            }

                        },
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Minimize to system tray", fontSize = 25.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(5))
                    .padding(13.dp)
                    .fillMaxHeight(0.25f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Restore Backup", fontWeight = FontWeight.Bold,color = Color.White, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(5.dp))
                backups.ifNotNull { dirs ->
                    dirs.sortedBy { it }.forEach{ dir ->
                        Text(text = dir, fontSize = 30.sp, color = Color.White, modifier = Modifier.clickable {
                            contentRepository.repositoryScope.launch {
                                contentRepository.restoreFromBackup(dir)
                                    .onSuccess {
                                        if(it){
                                            contentRepository.runInit(){
                                                info = "Backup $dir has been restored"
                                                showSnackBar = true
                                            }

                                        }
                                    }
                                    .onFailure {
                                        info = "Error: ${it.message}"
                                        showSnackBar = true
                                    }
                            }

                        })
                        Spacer(modifier = Modifier.height(5.dp))
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
    }
}