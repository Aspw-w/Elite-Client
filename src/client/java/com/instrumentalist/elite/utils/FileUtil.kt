package com.instrumentalist.elite.utils

import com.instrumentalist.elite.Client
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.ArrayList
import java.util.stream.Collectors

object FileUtil {

    var isLatestClient = false
    private var onlineStrs: String? = null

    fun getModuleFiles(): List<Path?> {
        val configPath: Path = IMinecraft.mc.runDirectory.toPath().resolve(Client.configLocation).resolve("module-configs")
        var jsonFiles: List<Path?> = ArrayList()

        try {
            Files.walk(configPath).use { paths ->
                jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter { path -> path.toString().endsWith(".json") }
                    .collect(Collectors.toList())
            }
        } catch (_: IOException) {
        }

        return jsonFiles
    }

    fun getBindFiles(): List<Path?> {
        val configPath: Path = IMinecraft.mc.runDirectory.toPath().resolve(Client.configLocation).resolve("bind-configs")
        var jsonFiles: List<Path?> = ArrayList()

        try {
            Files.walk(configPath).use { paths ->
                jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter { path -> path.toString().endsWith(".json") }
                    .collect(Collectors.toList())
            }
        } catch (_: IOException) {
        }

        return jsonFiles
    }

    fun loadOnlineNow(name: String): String? {
        val client = OkHttpClient.Builder().build()
        val builder = Request.Builder().url("https://nattogreatapi.pages.dev/Elite/configs/$name.json")
        val request: Request = builder.build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful)
                return response.body!!.string()
        }
        return null
    }

    fun doCfgNetLoader() {
        val client = OkHttpClient.Builder().build()
        val request: Request = Request.Builder().url("https://nattogreatapi.pages.dev/Elite/configs/configs.txt").build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                onlineStrs = response.body?.string()
                ChatUtil.showLog("Loaded online configs")
            }
        }
    }

    fun updateCheck(): Boolean {
        val client = OkHttpClient.Builder().build()
        val request: Request = Request.Builder().url("https://nattogreatapi.pages.dev/Elite/updates/latest.txt").build()
        client.newCall(request).execute().use { response ->
            isLatestClient = response.isSuccessful && response.body?.string() == Client.clientVersion
            if (isLatestClient) {
                ChatUtil.showLog("Loaded updates (LATEST)")
                return true
            } else {
                ChatUtil.showLog("Loaded updates (OUTDATED)")
                return false
            }
        }
    }

    fun getOnlineCfgs(): List<String?> {
        return onlineStrs?.split("\n")!!
    }
}