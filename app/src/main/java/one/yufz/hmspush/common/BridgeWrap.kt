package one.yufz.hmspush.common

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import one.yufz.hmspush.common.content.ContentModel
import one.yufz.hmspush.common.content.toContent
import one.yufz.hmspush.common.content.toContentList
import one.yufz.hmspush.common.content.toContentSet
import one.yufz.hmspush.common.content.toContentValues
import one.yufz.hmspush.common.model.ModuleVersionModel
import one.yufz.hmspush.common.model.PrefsModel
import one.yufz.hmspush.common.model.PushHistoryModel
import one.yufz.hmspush.common.model.PushSignModel

object BridgeWrap {
    private fun query(context: Context, uri: Uri): Cursor? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }

    private fun update(context: Context, uri: Uri, values: ContentValues): Int {
        try {
            return context.contentResolver.update(uri, values, null, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
            return 0
        }
    }

    private fun delete(context: Context, uri: Uri, args: Array<String>?): Int {
        try {
            return context.contentResolver.delete(uri, null, args)
        } catch (e: SecurityException) {
            e.printStackTrace()
            return 0
        }
    }

    fun getRegistered(context: Context): Set<PushSignModel> {
        return queryModelSet(context, BridgeUri.PUSH_REGISTERED.toUri()) ?: emptySet()
    }

    fun getPushHistory(context: Context): Set<PushHistoryModel> {
        return queryModelSet(context, BridgeUri.PUSH_HISTORY.toUri()) ?: emptySet()
    }

    fun getModuleVersion(context: Context): ModuleVersionModel? {
        return queryModel(context, BridgeUri.MODULE_VERSION.toUri())
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

    fun unregisterPush(context: Context, packageName: String) {
        delete(context, BridgeUri.PUSH_REGISTERED.toUri(), arrayOf(packageName))
    }

    fun queryPreference(context: Context): PrefsModel {
        return queryModel(context, BridgeUri.PREFERENCES.toUri()) ?: PrefsModel()
    }

    private inline fun <reified T : ContentModel> queryModel(context: Context, uri: Uri): T? {
        return query(context, uri)?.use(Cursor::toContent)
    }

    private inline fun <reified T : ContentModel> queryModelList(context: Context, uri: Uri): List<T>? {
        return query(context, uri)?.use(Cursor::toContentList)
    }

    private inline fun <reified T : ContentModel> queryModelSet(context: Context, uri: Uri): Set<T>? {
        return query(context, uri)?.use(Cursor::toContentSet)
    }

    private fun <T : ContentModel> updateModel(context: Context, uri: Uri, content: T): Int {
        return update(context, uri, content.toContentValues())
    }

    fun updatePreference(context: Context, model: PrefsModel) {
        updateModel(context, BridgeUri.PREFERENCES.toUri(), model)
    }
}