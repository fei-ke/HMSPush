package one.yufz.hmspush.hook.fakedevice

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
                val array = ArrayList<Int>()
                for (i in 0 until allowPushList.length()) {
                    array.add(allowPushList.getInt(i))
                }
                if (!array.contains(7)) {
                    array.add(0, 7)
                }
                obj.put("allow_push_list", JSONArray(array))

                result = obj.toString()

                XLog.d(TAG, result.toString())
            }
        }
        return true
    }
}