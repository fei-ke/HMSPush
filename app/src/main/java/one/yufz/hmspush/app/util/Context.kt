package one.yufz.hmspush.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Context.registerReceiverAsFlow(intentFilter: IntentFilter): Flow<Intent> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySendBlocking(intent)
        }
    }
    ContextCompat.registerReceiver(
        this@registerReceiverAsFlow,
        receiver,
        intentFilter,
        ContextCompat.RECEIVER_EXPORTED
    )
    awaitClose {
        unregisterReceiver(receiver)
    }
}

fun Context.registerPackageChangeFlow(): Flow<Intent> {
    val intentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_CHANGED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
        addDataScheme("package")
    }
    return registerReceiverAsFlow(intentFilter)
}
