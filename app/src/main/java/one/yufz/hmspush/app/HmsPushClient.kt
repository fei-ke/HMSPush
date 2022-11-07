package one.yufz.hmspush.app

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import one.yufz.hmspush.common.BinderCursor
import one.yufz.hmspush.common.BridgeUri
import one.yufz.hmspush.common.BridgeWrap
import one.yufz.hmspush.common.HmsPushInterface
import one.yufz.hmspush.common.IconData
import one.yufz.hmspush.common.model.IconModel
import one.yufz.hmspush.common.model.ModuleVersionModel
import one.yufz.hmspush.common.model.PrefsModel
import one.yufz.hmspush.common.model.PushHistoryModel
import one.yufz.hmspush.common.model.PushSignModel
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

fun createHmsPushServiceProxy(context: Context): HmsPushInterface = Proxy.newProxyInstance(HmsPushInterface::class.java.classLoader, arrayOf(HmsPushInterface::class.java), object : InvocationHandler {
    private lateinit var service: HmsPushInterface

    private fun getService(): HmsPushInterface {
        if (this::service.isInitialized && service.asBinder().isBinderAlive) {
            return service
        }
        BridgeWrap.query(context, BridgeUri.HMS_PUSH_SERVICE.toUri())?.use {
            service = HmsPushInterface.Stub.asInterface(BinderCursor.getBinder(it))
            return service
        }
        return HmsPushInterface.Default()
    }

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        return try {
            call(method, args)
        } catch (t: Throwable) {
            call(method, args)
        }
    }

    private fun call(method: Method, args: Array<out Any>?): Any? {
        return if (args != null) {
            method.invoke(getService(), *args)
        } else {
            method.invoke(getService())
        }
    }
}) as HmsPushInterface

object HmsPushClient : HmsPushInterface.Stub() {
    private val service = createHmsPushServiceProxy(App.instance)

    fun getPushSignFlow(): Flow<List<PushSignModel>> = callbackFlow {
        val onChange: () -> Unit = { trySendBlocking(pushSignList) }

        onChange()

        val observer = ObserverWrap(onChange)

        registerContentObserver(BridgeUri.PUSH_SIGN.toUri(), observer)

        awaitClose { unregisterContentObserver(observer) }
    }


    fun getPushHistoryFlow(): Flow<List<PushHistoryModel>> = callbackFlow {
        val onChange: () -> Unit = { trySendBlocking(pushHistoryList) }

        onChange()

        val observer = ObserverWrap(onChange)

        registerContentObserver(BridgeUri.PUSH_HISTORY.toUri(), observer)

        awaitClose {
            unregisterContentObserver(observer)
        }
    }

    private class ObserverWrap(val observer: () -> Unit) : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            observer()
        }
    }

    private fun registerContentObserver(uri: Uri, observer: ObserverWrap) {
        BridgeWrap.registerObserve(App.instance, uri, observer)
    }

    private fun unregisterContentObserver(observer: ObserverWrap) {
        BridgeWrap.unregisterObserve(App.instance, observer)
    }

    override fun getModuleVersion(): ModuleVersionModel? {
        return service.moduleVersion
    }

    override fun getPushSignList(): List<PushSignModel> {
        return service.pushSignList ?: emptyList()
    }

    override fun unregisterPush(packageName: String) {
        return service.unregisterPush(packageName)
    }

    override fun getPushHistoryList(): List<PushHistoryModel> {
        return service.pushHistoryList ?: emptyList()
    }

    override fun getPreference(): PrefsModel {
        return service.preference ?: PrefsModel()
    }

    override fun updatePreference(model: PrefsModel) {
        service.updatePreference(model)
    }

    override fun getAllIcon(): List<IconModel> {
        return service.allIcon ?: emptyList()
    }

    override fun saveIcon(iconModel: IconModel?) {
        service.saveIcon(iconModel)
    }

    override fun deleteIcon(vararg packageName: String) {
        service.deleteIcon(packageName)
    }

    fun saveIcon(iconData: IconData) {
        saveIcon(IconModel(iconData.packageName, iconData.toJson()))
    }
}