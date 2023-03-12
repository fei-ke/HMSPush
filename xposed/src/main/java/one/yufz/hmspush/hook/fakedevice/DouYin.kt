package one.yufz.hmspush.hook.fakedevice

import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.XLog.logMethod
import one.yufz.xposed.LoadPackageParam
import one.yufz.xposed.findClass
import one.yufz.xposed.hookMethod


class DouYin : Common() {
    companion object {
        private const val TAG = "DouYin"
    }

    override fun fake(lpparam: LoadPackageParam): Boolean {
        super.fake(lpparam)
        //public java.lang.String com.bytedance.common.network.DefaultNetWorkClient.post(java.lang.String,java.util.List,java.util.Map,com.bytedance.common.utility.NetworkClient$ReqContext)
        val classAppLogNetworkClient = lpparam.classLoader.findClass("com.ss.android.ugc.aweme.statistic.AppLogNetworkClient")
        val classReqContext = lpparam.classLoader.findClass("com.bytedance.common.utility.NetworkClient\$ReqContext")
        classAppLogNetworkClient.hookMethod("post", String::class.java, List::class.java, Map::class.java, classReqContext) {
            doBefore {
                val url = args[0] as String
                if (!url.contains("/cloudpush/update_sender/")) return@doBefore

                //&rom=EMUI-EmotionUI_xxx
                if (!url.contains("rom=EMUI-")) {
                    XLog.d(TAG, "update_sender: url = $url")
                    val fakeEmuiRom = "EMUI-${Property.EMUI_VERSION.value}"
                    args[0] = url.replace("rom=[^&]*&".toRegex(), "rom=$fakeEmuiRom&")
                }
            }
            doAfter {
                val url = args[0] as String
                if (!url.contains("/cloudpush/update_sender/")) return@doAfter

                logMethod(TAG)
            }
        }
        return true
    }
}