package one.yufz.hmspush.hook.hms

import android.app.Notification
import one.yufz.hmspush.common.model.PushRecordModel
import one.yufz.hmspush.hook.XLog
import org.json.JSONObject
import java.io.File

/**
 * 完整推送历史，每条推送单独一行、永久保留、不自动清理。
 *
 * 落盘为 JSONL（一行一条 JSON），优先写外部私有目录
 * `/sdcard/Android/data/com.huawei.hwid/files/hmspush/`，用户可用文件管理器直接查看。
 * 外部存储不可用时回退到内部 filesDir。
 *
 * 注意：本类的所有公开方法都不向外抛异常。写入点在 HMS 的 notify() 调用链上，
 * 任何异常冒出去都会崩掉 HMS Core 进程（NotificationManagerEx.tryInvoke 是 catch 后 rethrow）。
 */
object PushRecordStore {
    private const val TAG = "PushRecordStore"

    private const val FILE_NAME = "push_records.jsonl"

    private val lock = Any()

    private val recordFile: File? by lazy {
        try {
            val context = StorageContext.get()
            //外部私有目录：用户可见，且不需要存储权限
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            File(baseDir, "hmspush").apply {
                if (!exists()) mkdirs()
            }.let { File(it, FILE_NAME) }
        } catch (t: Throwable) {
            XLog.e(TAG, "init record file failed", t)
            null
        }
    }

    fun record(packageName: String, id: Int, notification: Notification) {
        try {
            val extras = notification.extras
            val model = PushRecordModel(
                packageName = packageName,
                pushTime = System.currentTimeMillis(),
                title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
                content = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
                notifyId = id,
                channelId = notification.channelId,
            )
            append(model)
        } catch (t: Throwable) {
            //记录失败只写日志，绝不能影响推送本身
            XLog.e(TAG, "record failed: packageName = $packageName", t)
        }
    }

    private fun append(model: PushRecordModel) {
        val file = recordFile ?: return

        val line = JSONObject().apply {
            put("packageName", model.packageName)
            put("pushTime", model.pushTime)
            put("title", model.title)
            put("content", model.content)
            put("notifyId", model.notifyId)
            put("channelId", model.channelId)
        }.toString()

        synchronized(lock) {
            file.appendText(line + "\n")
        }

        HmsPushService.notifyPushRecordChanged()
    }

    /**
     * 倒序（最新在前）分页读取。文件可能很大，只解析需要的那一段。
     */
    fun getRecords(limit: Int, offset: Int): List<PushRecordModel> {
        if (limit <= 0) return emptyList()

        return try {
            val file = recordFile ?: return emptyList()
            if (!file.exists()) return emptyList()

            val lines = synchronized(lock) { file.readLines() }

            lines.asReversed()
                .asSequence()
                .drop(offset.coerceAtLeast(0))
                .take(limit)
                .mapNotNull { parseLine(it) }
                .toList()
        } catch (t: Throwable) {
            XLog.e(TAG, "getRecords failed", t)
            emptyList()
        }
    }

    fun getRecordCount(): Int {
        return try {
            val file = recordFile ?: return 0
            if (!file.exists()) return 0
            synchronized(lock) { file.readLines().count { it.isNotBlank() } }
        } catch (t: Throwable) {
            XLog.e(TAG, "getRecordCount failed", t)
            0
        }
    }

    fun clear() {
        try {
            synchronized(lock) {
                recordFile?.takeIf { it.exists() }?.writeText("")
            }
            HmsPushService.notifyPushRecordChanged()
        } catch (t: Throwable) {
            XLog.e(TAG, "clear failed", t)
        }
    }

    /**
     * 供界面显示实际存储位置，方便用户拿文件管理器去找。
     */
    fun getRecordFilePath(): String {
        return recordFile?.absolutePath ?: ""
    }

    //单行解析失败时跳过该行，不影响其余记录
    private fun parseLine(line: String): PushRecordModel? {
        if (line.isBlank()) return null
        return try {
            val obj = JSONObject(line)
            PushRecordModel(
                packageName = obj.getString("packageName"),
                pushTime = obj.optLong("pushTime"),
                title = obj.optStringOrNull("title"),
                content = obj.optStringOrNull("content"),
                notifyId = obj.optInt("notifyId"),
                channelId = obj.optStringOrNull("channelId"),
            )
        } catch (t: Throwable) {
            null
        }
    }

    //JSONObject.optString 对 JSON null 返回 "null" 字符串，这里统一成真正的 null
    private fun JSONObject.optStringOrNull(name: String): String? {
        if (!has(name) || isNull(name)) return null
        return optString(name).takeIf { it.isNotEmpty() }
    }
}
