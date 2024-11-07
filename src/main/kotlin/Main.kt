import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import nativeLibs.NativeSysCalls
import objects.*
import screens.Home


val contentProvider = ContentProvider()
val contentRepository = ContentRepository(Database)
val nativeSysCalls = NativeSysCalls()
@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }
    MaterialTheme {
        Home(modifier = Modifier.fillMaxSize())
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, state = WindowState(width = 1300.dp, height = 1000.dp)) {
        App()
    }
}
