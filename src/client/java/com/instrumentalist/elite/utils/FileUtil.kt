package com.instrumentalist.elite.utils

import com.instrumentalist.elite.Client
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.ArrayList
import java.util.stream.Collectors

object FileUtil {

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
}