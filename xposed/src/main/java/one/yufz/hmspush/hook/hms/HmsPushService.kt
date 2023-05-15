package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.runBlocking
import one.yufz.hmspush.common.API_VERSION
import one.yufz.hmspush.common.BridgeUri
import one.yufz.hmspush.common.HmsPushInterface
import one.yufz.hmspush.common.VERSION_CODE
import one.yufz.hmspush.common.VERSION_NAME
import one.yufz.hmspush.common.model.IconModel
import one.yufz.hmspush.common.model.ModuleVersionModel
import one.yufz.hmspush.common.model.PrefsModel
import one.yufz.hmspush.common.model.PushHistoryModel
import one.yufz.hmspush.common.model.PushSignModel
import one.yufz.hmspush.hook.hms.icon.IconManager

object HmsPushService : HmsPushInterface.Stub() {
    private const val TAG = "HmsPushService"

    fun notifyHmsPushServiceCreated() {
        BridgeUri.HMS_PUSH_SERVICE.notifyContentChanged(AndroidAppHelper.currentApplication())
    }

    fun notifyPushSignChanged() {
        BridgeUri.PUSH_SIGN.notifyContentChanged(AndroidAppHelper.currentApplication())
    }

    fun notifyPushHistoryChanged() {
        BridgeUri.PUSH_HISTORY.notifyContentChanged(AndroidAppHelper.currentApplication())
    }

    override fun getModuleVersion(): ModuleVersionModel {
        return ModuleVersionModel(VERSION_NAME, VERSION_CODE, API_VERSION)
    }

    override fun getPushSignList(): List<PushSignModel> {
        return PushSignWatcher.getRegisterPackages()
    }

    override fun unregisterPush(packageName: String) {
        PushSignWatcher.unregisterSign(packageName)
    }

    override fun getPushHistoryList(): List<PushHistoryModel> {
        return PushHistory.getAll()
    }

    override fun getPreference(): PrefsModel {
        return Prefs.prefModel
    }

    override fun updatePreference(model: PrefsModel) {
        Prefs.updatePreference(model)
    }

    override fun getAllIcon(): List<IconModel> {
        return runBlocking { IconManager.getAllIconModel() }
    }

    override fun saveIcon(iconModel: IconModel) {
        runBlocking { IconManager.saveToLocal(iconModel.packageName, iconModel.iconData!!) }
    }

    override fun deleteIcon(packageNames: Array<String>) {
        runBlocking { IconManager.deleteIcon(packageNames) }
    }

    override fun killHmsCore(): Boolean {
        Handler(Looper.getMainLooper()).post { android.os.Process.killProcess(android.os.Process.myPid()) }
        return true
    }
}