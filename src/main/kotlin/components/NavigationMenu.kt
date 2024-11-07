@file:Suppress("FunctionName")

package components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import objects.YatdoDataTypes

@Composable
fun NavigationMenu(modifier:Modifier = Modifier, onSearch:(String)->Unit={}){
    var searchText by rememberSaveable() {
        mutableStateOf("")
    }
    val interactionSource = remember { MutableInteractionSource() }
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
                value = searchText,
                onValueChange = {
                    searchText = it
                    onSearch.invoke(searchText)
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
        }
    }
}