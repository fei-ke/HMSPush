package one.yufz.hmspush.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 一条完整的推送记录。与只保存「每个应用最后一次推送时间」的 [PushHistoryModel] 不同，
 * 每条推送单独占一行、永久保留。
 */
@Parcelize
data class PushRecordModel(
    val packageName: String,
    val pushTime: Long,
    val title: String? = null,
    val content: String? = null,
    val notifyId: Int = 0,
    val channelId: String? = null,
) : Parcelable
