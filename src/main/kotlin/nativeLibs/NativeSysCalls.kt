package nativeLibs

import appName
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import objects.StandardLibs


class NativeSysCalls() {
    private var libNotify: LibNotify? = null

    init {
        try {
            libNotify = Native.load(StandardLibs.LIB_NOTIFY, LibNotify::class.java)
            libNotify?.notify_init(appName)
        } catch (e: Exception) {
            println("Failed to initialize notifications: ${e.message}")
        }
    }

    fun sendNotification(
        title: String,
        body: String,
        icon: String? = null,
        sound: String? = null,
        playSound: Boolean = true,
        callBack:()->Unit = {}
    ) {
        try {
            libNotify?.let { lib ->
                val notification = lib.notify_notification_new(title, body, icon)
                if (notification != Pointer.NULL) {
                    // Set sound hint if specified
                    if (sound != null) {
                        lib.notify_notification_set_hint_string(
                            notification,
                            "sound-name",
                            "sound-name='$sound'"
                        )
                    }

                    // Enable or disable sound
                    lib.notify_notification_set_hint_string(
                        notification,
                        "suppress-sound",
                        if (playSound) "false" else "true"
                    )

                    val error = PointerByReference()
                    val success = lib.notify_notification_show(notification, error)
                    if (!success) {
                        println("Failed to show notification")
                        callBack.invoke()
                    }
                } else {
                    println("Failed to create notification")
                }
            } ?: println("LibNotify not initialized")
        } catch (e: Exception) {
            println("Error sending notification: ${e.message}")
        }
    }

    fun cleanup() {
        try {
            libNotify?.notify_uninit()
            libNotify = null
        } catch (e: Exception) {
            println("Error during cleanup: ${e.message}")
        }
    }

}