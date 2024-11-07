package objects

object State {
    const val NOT_STARTED: String = "Not Started"    // Initial state
    const val IN_PROGRESS: String = "In Progress"    // Task is being worked on
    const val PAUSED: String = "Paused"             // Temporarily stopped
    const val ARCHIVED:String = "Archived"
    const val CANCELLED: String = "Cancelled"        // Task won't be completed
    const val COMPLETED: String = "Completed"        // Task is finished
}
