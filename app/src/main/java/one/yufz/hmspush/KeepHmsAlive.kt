package one.yufz.hmspush

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import kotlin.math.min

class KeepHmsAlive(private val context: Context) {
    companion object {
        private const val TAG = "KeepHmsAlive"

        private const val MSG_BIND_HMS_SERVICE = 1

        private const val MIN_RETRY_TIMEOUT = 1_000L
        private const val MAX_RETRY_TIMEOUT = 30_000L
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            XLog.d(TAG, "handleMessage() called with: what = ${msg.what}")

            when (msg.what) {
                MSG_BIND_HMS_SERVICE -> {
                    connect()
                }
            }
        }
    }

    private var timeout = MIN_RETRY_TIMEOUT

    private var connected: Boolean = false

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            XLog.d(TAG, "onServiceConnected() called with: name = $name, service = $service")
            connected = true
            timeout = MIN_RETRY_TIMEOUT
        }

        override fun onServiceDisconnected(name: ComponentName) {
            XLog.d(TAG, "onServiceDisconnected() called with: name = $name")
            connected = false
            disconnect()
            scheduleReconnect()
        }

        override fun onBindingDied(name: ComponentName?) {
            XLog.d(TAG, "onBindingDied() called with: name = $name")
            connected = false
            disconnect()
            scheduleReconnect()
        }
    }

    fun start() {
        XLog.d(TAG, "start() called")
        connect()
    }

    private fun connect() {
        XLog.d(TAG, "connect() called")

        if (connected) {
            XLog.d(TAG, "connect: already connected")
            return
        }

        wakeupHms()

        val bound = context.bindService(createServiceIntent(), serviceConnection, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)

        XLog.d(TAG, "connect() result: $bound")

        if (!bound) {
            XLog.d(TAG, "connect() failed, schedule reconnect")
            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        XLog.d(TAG, "scheduleReconnect() called")

        if (!handler.hasMessages(MSG_BIND_HMS_SERVICE)) {
            timeout = min(MAX_RETRY_TIMEOUT, (timeout * 1.5).toLong())

            XLog.d(TAG, "scheduleReconnect: scheduling reconnect in $timeout ms")

            handler.sendEmptyMessageDelayed(MSG_BIND_HMS_SERVICE, timeout)
        } else {
            XLog.d(TAG, "scheduleReconnect() called already has a scheduled reconnect")
        }
    }

    private fun createServiceIntent(): Intent {
        val intent = Intent(HMS_CORE_SERVICE_ACTION).apply {
            setClassName(HMS_PACKAGE_NAME, HMS_CORE_SERVICE)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        return intent
    }

    private fun disconnect() {
        XLog.d(TAG, "disconnect() called, connected = $connected")

        if (connected) {
            context.unbindService(serviceConnection)
        }
    }

    private fun wakeupHms() {
        XLog.d(TAG, "wakeupHms() called")
        context.startService(createServiceIntent())
    }
}