package models

import objects.HasId
import kotlinx.serialization.Serializable
import objects.State
import objects.Status

@Serializable
data class TaskModel(
    // Required core properties
    override var id: String = "",
    var title: String = "",                    // "name" renamed to "title" for clarity
    var state: String = State.NOT_STARTED,
    var status: String = Status.QUEUED,
    var createdAt: Long = System.currentTimeMillis(), // Using timestamp instead of String

    var description: String = "",           // "details" renamed to "description"
    var deadline: Long? = null,                // "dueDate" changed to nullable timestamp
    var category: String = "",
    var groupId: String = "None",               // Using ID reference instead of group name
    var progress: Float = 0.0f,                // Kept as is, represents % completion

    // Task management properties
    var isArchived: Boolean = false,           // Better than "inTrash" for recoverable items
    var lastModifiedAt: Long = System.currentTimeMillis(),
    var completedAt: Long? = null,             // New field to track completion time
    var priority: Int = PRIORITY_NORMAL,       // New field for task priority

    // Optional metadata
    var tags: List<String> = emptyList(),      // New field for flexible categorization
    var attachments: List<String> = emptyList() // New field for attachment references
) : HasId {
    companion object {
        const val PRIORITY_LOW = 0
        const val PRIORITY_NORMAL = 1
        const val PRIORITY_HIGH = 2
        const val PRIORITY_URGENT = 3

        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
        private const val WARNING_THRESHOLD = 0.10f // 10% below expected progress
    }

    /**
     * Calculates the expected progress based on elapsed time since creation
     * @return expected progress as a float between 0 and 1
     */
    private fun calculateExpectedProgress(): Float {
        val deadline = deadline ?: return 0f

        // Total duration in milliseconds
        val totalDuration = deadline - createdAt
        if (totalDuration <= 0) return 1f // Invalid duration, assume should be complete

        // Elapsed time since creation
        val elapsedTime = System.currentTimeMillis() - createdAt
        if (elapsedTime <= 0) return 0f // Just created or future creation date

        // Calculate expected progress as a ratio of elapsed time to total duration
        return (elapsedTime.toFloat() / totalDuration).coerceIn(0f, 1f)
    }

    /**
     * Determines if the task is falling behind schedule
     * @return true if progress is significantly behind expected progress
     */
    private fun isBehindSchedule(): Boolean {
        val expectedProgress = calculateExpectedProgress()
        return progress < (expectedProgress - WARNING_THRESHOLD)
    }

    /**
     * Calculates the daily progress needed to complete on time
     * @return required daily progress or null if no deadline
     */
    fun getRequiredDailyProgress(): Float? {
        val deadline = deadline ?: return null

        val totalDays = (deadline - createdAt) / MILLIS_PER_DAY
        if (totalDays <= 0) return null

        if(progress >=1f) return 0f
        return (1.0f / totalDays)
    }

    /**
     * Calculates how many days behind schedule the task is
     * @return number of days behind schedule or null if not behind or no deadline
     */
    fun getDaysBehindSchedule(): Float? {
        val deadline = deadline ?: return null
        val expectedProgress = calculateExpectedProgress()
        if (progress >= expectedProgress) return null

        val totalDays = (deadline - createdAt) / MILLIS_PER_DAY.toFloat()
        val progressDifference = expectedProgress - progress
        return (progressDifference * totalDays)
    }

    fun calculateStatus(): String {
        if (state == State.COMPLETED) {
            return Status.ON_TRACK
        }
        if(state == State.NOT_STARTED){
            return Status.QUEUED
        }

        if (state == State.ARCHIVED){
            return Status.STOPPED
        }

        val deadline = deadline ?: return Status.NO_DEADLINE

        val now = System.currentTimeMillis()
        return when {
            now > deadline -> Status.OVERDUE
            isBehindSchedule() -> Status.AT_RISK
            else -> Status.ON_TRACK
        }
    }
}