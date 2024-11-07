package models

/**
 * Options for listing files in a directory
 * @property includeSubdirectories Whether to include files from subdirectories
 * @property includeDirectories Whether to include directory names in the result
 * @property fileExtensions List of file extensions to filter by (e.g., ["txt", "pdf"])
 * @property excludePatterns List of regex patterns to exclude files/directories
 */
data class FileListOptions(
    val includeSubdirectories: Boolean = false,
    val includeDirectories: Boolean = false,
    val fileExtensions: List<String> = emptyList(),
    val excludePatterns: List<Regex> = emptyList()
)
