import enums.State
import java.util.*


fun String.toState():State{
    return when(this.lowercase(Locale.getDefault()).trim()){
        "in progress" -> State.IN_PROGRESS
        "completed" -> State.COMPLETED
        "queued" -> State.QUEUED
        "canceled" -> State.CANCELED
        else -> State.UNKNOWN
    }
}