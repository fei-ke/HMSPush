package one.yufz.hmspush.app.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader


object ShellUtil {
    private const val TAG = "ShellUtil"

    class Result(val output: String, val isSuccess: Boolean)

    suspend fun executeCommand(vararg command: String): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "executeCommand() called with: command = ${command.contentToString()}")

            val process = Runtime.getRuntime().exec(command)
            val inputStream = process.inputStream
            val errorStream = process.errorStream
            val inputReader = BufferedReader(InputStreamReader(inputStream))
            val errorReader = BufferedReader(InputStreamReader(errorStream))
            val output = StringBuilder()

            var line: String?

            while (inputReader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }

            while (errorReader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }

            val exitValue = process.waitFor()

            if (exitValue == 0) {
                Result(output.toString(), true)
            } else {
                Result(output.toString(), false)
            }
        } catch (e: Exception) {
            Result(e.message ?: "Unknown error", false)
        }
    }
}