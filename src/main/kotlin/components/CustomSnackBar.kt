@file:Suppress("FunctionName")

package components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import objects.AnimationStyle
import kotlinx.coroutines.delay


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomSnackBar(
    modifier: Modifier = Modifier,
    info:String = "",
    containerColor: Color = Color.Black,
    textColor: Color = Color.White,
    duration:Long = 8000L,
    isVisible:Boolean = false,
    task: () -> Unit = {}
){
    var show by rememberSaveable{
        mutableStateOf(isVisible)
    }

    LaunchedEffect(Unit){
        if(!show) return@LaunchedEffect
        delay((duration*0.95).toLong())
        show = false
        delay((duration-(duration*0.95).toLong()))
        task()
    }

    SimpleAnimator(
        modifier = modifier,
        isVisible = show,
        style = AnimationStyle.DOWN
    ){
        Box(
            modifier = modifier.padding(8.dp)
        ){
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.08f)
                    .fillMaxWidth(0.98f)
                    .background(color =  containerColor, shape = RoundedCornerShape(13))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start

            ){
                Box(modifier = Modifier.fillMaxWidth()){
                    Text(
                        info,
                        color = textColor,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .basicMarquee(),
                        fontSize = 32.sp
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(
                                color = textColor.copy(alpha = 0.7f),
                                shape = CircleShape)
                            .padding(0.dp)
                            .size(21.dp)
                            .clickable {
                                show = false
                                task()
                            },
                        imageVector = Icons.Default.Close,
                        contentDescription = "close snack bar",
                        tint = Color.Black
                    )
                }
            }
        }
    }

}