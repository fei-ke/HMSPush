package one.yufz.hmspush.hook

import android.content.Context
import java.util.*

sealed interface I18n {
    companion object {
        fun get(context: Context): I18n {
            return when (context.resources.configuration.locales.get(0).language) {
                Locale.CHINESE.language -> Chinese
                else -> Default
            }
        }
    }

    val hmsCoreRunning: String
    val hmsCoreRunningState: String
    val dummyFragmentDesc: String
    val tipsOptimizeBattery: String
}

object Chinese : I18n {
    override val hmsCoreRunning = "HMS Core 正在后台运行"
    override val hmsCoreRunningState = "HMS Core 运行状态"
    override val dummyFragmentDesc = "这是一个空白页面，你可以将该页面在最近任务中锁定，以帮助 HMS Core 保持后台运行"
    override val tipsOptimizeBattery = "建议对 HMS Core 关闭电池优化，以帮助 HMS Core 保持后台运行"
}

object Default : I18n {
    override val hmsCoreRunning = "HMS Core is running in the background"
    override val hmsCoreRunningState = "HMS Core Running State"
    override val dummyFragmentDesc = "This is a blank page, you can lock this page in recent tasks to help HMS Core keep running in the background"
    override val tipsOptimizeBattery = "It is recommended to turn off battery optimization for HMS Core to help keep it running in the background"
}