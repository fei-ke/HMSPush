package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import one.yufz.hmspush.BuildConfig
import one.yufz.hmspush.common.BridgeUri
import one.yufz.hmspush.common.HmsPushInterface
import one.yufz.hmspush.common.model.ModuleVersionModel
import one.yufz.hmspush.common.model.PrefsModel
import one.yufz.hmspush.common.model.PushHistoryModel
import one.yufz.hmspush.common.model.PushSignModel

object HmsPushService : HmsPushInterface.Stub() {
    private const val TAG = "HmsPushService"

    fun notifyPushSignChanged() {
        BridgeUri.PUSH_SIGN.notifyContentChanged(AndroidAppHelper.currentApplication())
    }

    fun notifyPushHistoryChanged() {
        BridgeUri.PUSH_HISTORY.notifyContentChanged(AndroidAppHelper.currentApplication())
    }

    override fun getModuleVersion(): ModuleVersionModel {
        return ModuleVersionModel(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
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
}