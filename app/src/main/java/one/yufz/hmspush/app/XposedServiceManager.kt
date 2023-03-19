package one.yufz.hmspush.app

import android.util.Log
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object XposedServiceManager {
    private const val TAG = "XposedServiceManager"

    var xposedService: XposedService? = null

    private val _scopeListFlow = MutableStateFlow<List<String>>(emptyList())

    fun scopeListFlow(): Flow<List<String>> = _scopeListFlow

    private val mCallback = object : XposedService.OnScopeEventListener {
        override fun onScopeRequestPrompted(packageName: String) {
            Log.d(TAG, "onScopeRequestPrompted() called with: packageName = $packageName")
        }

        override fun onScopeRequestApproved(packageName: String) {
            Log.d(TAG, "onScopeRequestApproved() called with: packageName = $packageName")
            updateScopeList()
        }

        override fun onScopeRequestDenied(packageName: String) {
            Log.d(TAG, "onScopeRequestDenied() called with: packageName = $packageName")
        }

        override fun onScopeRequestTimeout(packageName: String) {
            Log.d(TAG, "onScopeRequestTimeout() called with: packageName = $packageName")
        }

        override fun onScopeRequestFailed(packageName: String, message: String) {
            Log.d(TAG, "onScopeRequestFailed() called with: packageName = $packageName, message = $message")
        }
    }

    private fun updateScopeList() {
        val scopeList = getScopeList()
        Log.d(TAG, "updateScopeList() called, scopeList = $scopeList")
        _scopeListFlow.value = scopeList
    }

    fun getScopeList(): List<String> {
        return xposedService?.scope ?: emptyList()
    }

    fun requestScope(packageName: String) {
        xposedService?.requestScope(packageName, mCallback)
    }

    fun removeScope(packageName: String) {
        xposedService?.removeScope(packageName)
        updateScopeList()
    }

    fun init() {
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                Log.d(TAG, "onServiceBind() called with: service = $service")
                xposedService = service
                updateScopeList()
            }

            override fun onServiceDied(service: XposedService) {
                Log.d(TAG, "onServiceDied() called with: service = $service")
            }
        })
    }
}