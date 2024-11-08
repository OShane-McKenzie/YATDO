package objects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import contentProvider

object Controller {
    var showArchivedTasks = mutableStateOf(false)
    var loadArchivedTasks  = mutableStateOf(contentProvider.config.value.loadArchive)
    var minimizeToSystemTray = mutableStateOf(contentProvider.config.value.tray)
}