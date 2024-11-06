package nativeLibs
import appName
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference


class NativeSysCalls {
    fun sendNotification(title:String, body:String, icon:String? = null){

        val libNotify = Native.load(StandardLibs.LIB_NOTIFY.libraryName, LibNotify::class.java)

        libNotify.notify_init(appName = appName)

        val notification = libNotify.notify_notification_new(title, body, icon)

        val error = PointerByReference()

        libNotify.notify_notification_show(notification, error)

        libNotify.notify_uninit()
    }
}