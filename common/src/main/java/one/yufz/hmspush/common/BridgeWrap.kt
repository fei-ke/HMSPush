package one.yufz.hmspush.common

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object BridgeWrap {
    fun <T> registerContentAsFlow(context: Context, uri: Uri, onChangedSendBlocking: () -> T): Flow<T> = callbackFlow {
        val onChange: () -> Unit = { trySendBlocking(onChangedSendBlocking()) }

        onChange()

        val observer = ObserverWrap(onChange)

        registerObserve(context, uri, observer)

        awaitClose {
            unregisterObserve(context, observer)
        }
    }

    private class ObserverWrap(val observer: () -> Unit) : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            observer()
        }
    }

    fun registerObserve(context: Context, uri: Uri, observer: ContentObserver) {
        try {
            context.contentResolver.registerContentObserver(uri, false, observer)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun unregisterObserve(context: Context, observer: ContentObserver) {
        try {
            context.contentResolver.unregisterContentObserver(observer)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun query(context: Context, uri: Uri): Cursor? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }

    fun isDisableSignature(context: Context): Boolean {
        query(context, BridgeUri.DISABLE_SIGNATURE.toUri())?.use {
            val indexDisableSignature = it.getColumnIndex("disableSignature")

            if (it.moveToNext()) {
                return it.getInt(indexDisableSignature) == 1
            }
        }
        return false
    }
}