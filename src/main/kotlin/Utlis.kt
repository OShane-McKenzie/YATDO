
import models.FileListOptions
import models.TaskModel
import objects.DateUtils
import objects.HasId
import objects.Path
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


fun String.isValidEmail(): Boolean {
    // Regular expression pattern for validating email addresses
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
    // Return whether the email matches the pattern
    return this.matches(emailRegex)
}

/**
 * Executes the given block after any operation
 * Useful for chaining operations where you want to perform an action regardless of the result
 */
inline fun Unit.then(block: Unit.() -> Unit) {
    val result = this
    result.block()
}

/**
 * Executes the given block if the receiver is not null
 * Returns the receiver to allow chaining
 */
inline fun <T> T?.ifNotNull(block: (T) -> Unit): T? {
    if (this != null) {
        block(this)
    }
    return this
}

/**
 * Executes the given block if the receiver is not empty
 * Supports Collections, Maps, CharSequences, Arrays, and custom isEmpty implementations
 * Returns the receiver to allow chaining
 */
inline fun <T> T?.ifNotEmpty(block: (T) -> Unit): T? {
    when (this) {
        null -> return null
        is String -> if(isNotEmpty()) block(this)
        is Collection<*> -> if (isNotEmpty()) block(this)
        is Map<*, *> -> if (isNotEmpty()) block(this)
        is CharSequence -> if (isNotEmpty()) block(this)
        is Array<*> -> if (isNotEmpty()) block(this)
        else -> {
            // Support custom isEmpty implementations
            val isEmpty = runCatching {
                this::class.members.find { it.name == "isEmpty" }?.call(this) as? Boolean
            }.getOrNull()

            if (isEmpty == false) block(this)
            else block(this) // Default behavior for types without isEmpty
        }
    }
    return this
}

/**
 * Executes the given block if the receiver is empty
 * Supports Collections, Maps, CharSequences, Arrays, and custom isEmpty implementations
 * Returns the receiver to allow chaining
 */
inline fun <T> T?.ifEmpty(block: (T) -> Unit): T? {
    when (this) {
        null -> return null
        is String -> if(isEmpty()) block(this)
        is Collection<*> -> if (isEmpty()) block(this)
        is Map<*, *> -> if (isEmpty()) block(this)
        is CharSequence -> if (isEmpty()) block(this)
        is Array<*> -> if (isEmpty()) block(this)
        else -> {
            // Support custom isEmpty implementations
            val isEmpty = runCatching {
                this::class.members.find { it.name == "isEmpty" }?.call(this) as? Boolean
            }.getOrNull()

            if (isEmpty == true) block(this)
        }
    }
    return this
}

/**
 * Executes the given block if the receiver is null
 * Returns the receiver to allow chaining
 */
inline fun <T> T?.ifNull(block: () -> Unit): T? {
    if (this == null) {
        block()
    }
    return this
}

/**
 * Executes success block if receiver is not null, failure block if null
 * Returns the receiver to allow chaining
 */
inline fun <T> T?.ifNullOrNot(
    successBlock: (T) -> Unit,
    failureBlock: () -> Unit
): T? {
    if (this != null) {
        successBlock(this)
    } else {
        failureBlock()
    }
    return this
}

// Extension functions for TaskModel
fun TaskModel.setDeadlineFromString(dateStr: String?) {
    this.deadline = dateStr?.let { DateUtils.parseDeadline(it) }
}

fun TaskModel.getDeadlineAsString(includeTime: Boolean = false): String? {
    return DateUtils.formatDeadline(this.deadline, includeTime)
}

fun String.setDateFromString(dateStr: String):Long? {
    return dateStr.let { DateUtils.parseDeadline(it) }
}
fun setDateFromString(dateStr: String):Long? {
    return dateStr.let { DateUtils.parseDeadline(it) }
}
fun Long.getDateAsString(includeTime: Boolean = false): String? {
    return DateUtils.formatDeadline(this, includeTime)
}

fun getCurrentDateTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return currentDateTime.format(formatter)
}

fun yatDoLog(
    timeStamp: String = getCurrentDateTime(),
    operation: String = "",
    outcome: String = "",
    exitCode: String = "",
    message: String = ""
) {
    try {
        val logHeader = "Timestamp,Operation,Outcome,ExitCode,Message\n"
        val log = "$timeStamp,${operation.replace(",","").replace("\n", " ")},$outcome,$exitCode,${
            message.replace(",","").replace("\n", " ")}\n"

        val logFile = File(Path.logFile)

        if (!logFile.exists()) {
            logFile.writeText(logHeader)
        } else {
            logFile.appendText(log)
        }
    } catch (e: Exception) {
        System.err.println("Logging failed: ${e.message}")
    }
}

fun exists(filePath: String): Boolean {
    return File(filePath).exists()
}

fun writeFile(filePath: String, content: String, append: Boolean = false) {
    try {
        val file = File(filePath)
        if (append) {
            file.appendText(content)
            yatDoLog(
                operation = "appending: $filePath",
                outcome = "success",
                exitCode = "0",
                message = "Content appended to file successfully"
            )
        } else {
            file.writeText(content)
            yatDoLog(
                operation = "writing: $filePath",
                outcome = "success",
                exitCode = "0",
                message = "File written successfully"
            )
        }
    } catch (e: Exception) {
        System.err.println("Error writing to file $filePath: ${e.message}")
        yatDoLog(
            operation = "writing: $filePath",
            outcome = "error",
            exitCode = "1",
            message = "File write error: ${e.message}"
        )
    }
}

fun readFile(filePath: String): String {
    return try {
        val content = File(filePath).readText()
        yatDoLog(
            operation = "reading: $filePath",
            outcome = "success",
            exitCode = "0",
            message = "File read successfully"
        )
        content
    } catch (e: Exception) {
        System.err.println("Error reading from file $filePath: ${e.message}")
        yatDoLog(
            operation = "reading: $filePath",
            outcome = "error",
            exitCode = "1",
            message = "File read error: ${e.message}"
        )
        ""
    }
}

fun deleteFile(filePath: String): Boolean {
    return try {
        val file = File(filePath)
        val deleted = file.delete()

        if (deleted) {
            yatDoLog(
                operation = "deleting: $filePath",
                outcome = "success",
                exitCode = "0",
                message = "File deleted successfully"
            )
        } else {
            System.err.println("Failed to delete file $filePath")
            yatDoLog(
                operation = "deleting: $filePath",
                outcome = "failure",
                exitCode = "1",
                message = "File deletion unsuccessful"
            )
        }

        deleted
    } catch (e: Exception) {
        System.err.println("Error deleting file $filePath: ${e.message}")
        yatDoLog(
            operation = "deleting: $filePath",
            outcome = "error",
            exitCode = "1",
            message = "File deletion error: ${e.message}"
        )
        false
    }
}
fun generateUniqueID(dataSet: List<Any>, length: Int = 8, prefix: String = "$appName-"): String {
    val random = Random()
    val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    var randomID: String
    var isUnique: Boolean

    do {
        // Generate a random ID of the specified length
        val idBuilder = StringBuilder()
        for (i in 0 until length) {
            val randomChar = chars[random.nextInt(chars.length)]
            idBuilder.append(randomChar)
        }
        randomID = "$prefix$idBuilder"

        // Check if the generated ID is unique
        isUnique = dataSet.none { if (it is HasId) {
            it.id == randomID
        } else {
            false
        } }
    } while (!isUnique)

    return randomID
}

/**
 * Lists all files in the specified directory according to the given options
 * @param directoryPath Path to the directory
 * @param options Configuration options for listing files
 * @return List of file names or null if directory doesn't exist
 * @throws IllegalArgumentException if the path exists but is not a directory
 */
fun listFiles(directoryPath: String, options: FileListOptions = FileListOptions()): List<String>? {
    /**
     * Check if a file matches the configured extension filters
     */
    fun matchesExtensions(file: File, options: FileListOptions): Boolean {
        if (options.fileExtensions.isEmpty()) {
            return true
        }
        return options.fileExtensions.any { ext ->
            file.extension.equals(ext, ignoreCase = true)
        }
    }

    /**
     * Check if a file should be excluded based on the exclude patterns
     */
    fun isExcluded(file: File, options: FileListOptions): Boolean {
        return options.excludePatterns.any { pattern ->
            pattern.matches(file.name)
        }
    }

    val directory = File(directoryPath)

    if (!directory.exists()) {
        return null
    }

    if (!directory.isDirectory) {
        throw IllegalArgumentException("Path exists but is not a directory: $directoryPath")
    }

    return directory.walkTopDown()
        .filter { file ->
            when {
                // Apply directory filter
                file.isDirectory -> options.includeDirectories && !isExcluded(file, options)

                // Apply file filters
                else -> !isExcluded(file, options) && matchesExtensions(file, options)
            }
        }
        .filter { file ->
            // Handle subdirectories
            options.includeSubdirectories || file.parent == directory.absolutePath
        }
        .map { it.name }
        .toList()
}


