package one.yufz.hmspush.hook.fakedevice

import android.os.Build
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.findClass
import one.yufz.xposed.hookMethod
import org.json.JSONArray
import org.json.JSONObject


class DouYin : Common() {
    companion object {
        private const val TAG = "DouYin"
    }

    override fun fake(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        super.fake(lpparam)
        if (Build.DISPLAY.contains("flyme", true) || Build.USER.contains("flyme", true)) {
            fakeProperty("ro.build.display.id" to "")
            fakeProperty("ro.build.user" to "")
            fakeProperty("ro.build.flyme.version" to "")
            fakeProperty("ro.flyme.version.id" to "")
        }

        //public java.lang.String com.bytedance.common.network.DefaultNetWorkClient.post(java.lang.String,java.util.List,java.util.Map,com.bytedance.common.utility.NetworkClient$ReqContext)
        val classAppLogNetworkClient = lpparam.classLoader.findClass("com.ss.android.ugc.aweme.statistic.AppLogNetworkClient")
        val classReqContext = lpparam.classLoader.findClass("com.bytedance.common.utility.NetworkClient\$ReqContext")
        classAppLogNetworkClient.hookMethod("post", String::class.java, List::class.java, Map::class.java, classReqContext) {
            doAfter {
                val url = args[0] as String
                if (!url.contains("/cloudpush/update_sender/")) return@doAfter

                XLog.d(TAG, result.toString())

                val json = result as String
                val obj = JSONObject(json)
                val allowPushList = obj.getJSONArray("allow_push_list")
                val newArray = tryInsertHuaweiChannel(allowPushList)
                obj.put("allow_push_list", newArray)

                result = obj.toString()

                XLog.d(TAG, result.toString())
            }
        }
        return true
    }

    private fun tryInsertHuaweiChannel(originArray: JSONArray): JSONArray {
        val array = ArrayList<Int>()
        for (i in 0 until originArray.length()) {
            array.add(originArray.getInt(i))
        }
        array.remove(7)
        array.add(0, 7)
        return JSONArray(array)
    }
}