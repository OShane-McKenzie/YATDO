@file:Suppress("FunctionName")

package components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import objects.YatdoDataTypes

@Composable
fun TitleText(
    modifier: Modifier = Modifier,
    text:String,
    color: Color = Color.White
){
    Text(text, color = color, modifier = modifier, fontSize = YatdoDataTypes.Fibonacci.THIRTY_FOUR.sp, fontWeight = FontWeight.Bold)
}