package objects

object Status {
    const val ON_TRACK: String = "On Track"         // Progress is as expected
    const val AT_RISK: String = "At Risk"           // Might miss deadline
    const val OVERDUE: String = "Overdue"           // Past deadline
    const val NO_DEADLINE: String = "No Deadline"   // No deadline set
    const val QUEUED: String = "Queued"             // Waiting to be started
    const val STOPPED:String="Stopped"
}