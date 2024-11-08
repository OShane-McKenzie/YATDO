package models

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    var loadArchive:Boolean = false,
    var tray:Boolean = false
)
