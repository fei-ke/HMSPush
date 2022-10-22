package one.yufz.hmspush.common

import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.os.IBinder

class BinderCursor(service: IBinder) : MatrixCursor(emptyArray()) {
    companion object {
        private const val KEY_BINDER = "binder"

        fun getBinder(cursor: Cursor): IBinder? {
            return cursor.extras.getBinder(KEY_BINDER)
        }
    }

    init {
        extras = Bundle().apply {
            putBinder(KEY_BINDER, service)
        }
    }
}