import models.FileListOptions

// Usage examples:
fun examples() {
    // Basic usage - just get all files in a directory
    listFilesAndDirectories("/path/to/directory")?.forEach { println(it) }

    // Get all text and PDF files, including from subdirectories
    val options = FileListOptions(
        includeSubdirectories = true,
        fileExtensions = listOf("txt", "pdf")
    )
    listFilesAndDirectories("/path/to/directory", options)?.forEach { println(it) }

    // Get all files except those matching certain patterns
    val excludeOptions = FileListOptions(
        excludePatterns = listOf(
            Regex("^\\._.*"),  // Hidden files starting with ._
            Regex(".*\\.tmp$") // Temporary files ending with .tmp
        )
    )
    listFilesAndDirectories("/path/to/directory", excludeOptions)?.forEach { println(it) }

    // Get everything including directories and subdirectories
    val allOptions = FileListOptions(
        includeSubdirectories = true,
        includeDirectories = true
    )
    listFilesAndDirectories("/path/to/directory", allOptions)?.forEach { println(it) }
}