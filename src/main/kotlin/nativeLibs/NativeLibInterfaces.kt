package nativeLibs

import com.sun.jna.Library
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

interface LibNotify : Library {
    fun notify_init(appName: String): Boolean
    fun notify_notification_new(summary: String, body: String?, icon: String?): Pointer
    fun notify_notification_show(notification: Pointer, error: PointerByReference): Boolean
    fun notify_notification_set_hint_string(notification: Pointer, key: String, value: String): Boolean
    fun notify_uninit()
}