@file:Suppress("FunctionName")

package components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import setDateFromString

@Composable
fun DateTimeComponent(
    modifier: Modifier = Modifier,
    date: (Long?, String?) -> Unit = { _, _ -> }
) {
    var day by rememberSaveable { mutableStateOf("") }
    var month by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var hour by rememberSaveable { mutableStateOf("") }
    var minute by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(0.1f),
                value = day,
                onValueChange = {
                    val isInt = it.toIntOrNull()
                    if (it.length <= 2 && isInt!=null) day = it
                },
                placeholder = { Text("dd") },
                label = { Text("Day") }
            )
            Spacer(modifier = Modifier.weight(0.01f))
            OutlinedTextField(
                modifier = Modifier.weight(0.1f),
                value = month,
                onValueChange = {
                    val isInt = it.toIntOrNull()
                    if (it.length <= 2 && isInt!=null) month = it
                },
                placeholder = { Text("mm") },
                label = { Text("Month") }
            )
            Spacer(modifier = Modifier.weight(0.01f))
            OutlinedTextField(
                modifier = Modifier.weight(0.2f),
                value = year,
                onValueChange = {
                    val isInt = it.toIntOrNull()
                    if (it.length <= 4 && isInt!=null) year = it
                },
                placeholder = { Text("yyyy") },
                label = { Text("Year") }
            )
        }
        Spacer(modifier = Modifier.weight(0.1f))
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(0.1f),
                value = hour,
                onValueChange = {
                    val isInt = it.toIntOrNull()
                    if (it.length <= 2 && isInt!=null) hour = it
                },
                placeholder = { Text("hh") },
                label = { Text("Hour") }
            )
            Spacer(modifier = Modifier.weight(0.01f))
            OutlinedTextField(
                modifier = Modifier.weight(0.1f),
                value = minute,
                onValueChange = {
                    val isInt = it.toIntOrNull()
                    if (it.length <= 2 && isInt!=null) minute = it
                },
                placeholder = { Text("mm") },
                label = { Text("Minute") }
            )
            Spacer(modifier = Modifier.weight(0.01f))
            Button(
                modifier = Modifier.weight(0.1f),
                onClick = {
                    val dayAsInt = day.toIntOrNull()
                    val monthAsInt = month.toIntOrNull()
                    val yearAsInt = year.toIntOrNull()
                    val hourAsInt = hour.toIntOrNull()
                    val minuteAsInt = minute.toIntOrNull()

                    // Validate inputs
                    if (dayAsInt in 1..31 && monthAsInt in 1..12 && yearAsInt != null &&
                        hourAsInt in 0..23 && minuteAsInt in 0..59
                    ) {
                        if(year.length==4 && month.length==2 && day.length==2 && hour.length==2 && minute.length==2) {
                            val dateString = "$year-$month-$day $hour:$minute"
                            val dateLong = setDateFromString(dateString)
                            date.invoke(dateLong, dateString)
                        }else{
                            date.invoke(null, null)
                        }
                    }else{
                        date.invoke(null, null)
                    }
                }
            ) {
                Text("Set")
            }
        }
    }
}
