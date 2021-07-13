/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

object GitHubSupport {
    private const val GITHUB_API = "https://api.github.com"

    private const val KEY_ASSETS = "assets"
    private const val KEY_NAME = "name"
    private const val KEY_TAG_NAME = "tag_name"
    private const val KEY_BROWSER_DOWNLOAD_URL = "browser_download_url"

    private const val REQUEST_ACCEPT = "application/vnd.github.v3+json"

    /**
     * [https://api.github.com/]의 release/latest url을 반환합니다.
     */
    fun generateUrlGitHubLatestRelease(owner: String, project: String): String {
        return "$GITHUB_API/repos/$owner/$project/releases/latest"
    }

    /**
     * https://github.com/로부터 latest release의 asset을 다운로드합니다.
     *
     * 인수로 주어진 버전보다 낮다면 다운로드하지 않고 [UpToDateException]을 발생시킵니다.
     *
     * 다운로드가 성공하면 callback에 url을 전달합니다.
     *
     * @see[String.compareVersion]
     *
     * @exception UpToDateException 최신버전일 경우 발생합니다.
     *
     */
    fun downloadLatestRelease(
        file: File,
        owner: String,
        project: String,
        version: String,
        asset: String,
        callback: (Result<String>.() -> Unit)? = null
    ) {
        kotlin.runCatching {
            file.parentFile?.mkdirs()

            val latestReleaseURL = URL(generateUrlGitHubLatestRelease(owner, project))
            val releaseInfo = latestReleaseURL.httpRequest {
                requestMethod = "GET"
                addRequestProperty("Accept", REQUEST_ACCEPT)
                inputStream.bufferedReader().use { JsonParser().parse(it) as JsonObject }
            }
            releaseInfo[KEY_TAG_NAME].asString.also {
                if (version.compareVersion(it) >= 0) throw UpToDateException("UP TO DATE")
            }
            val foundAsset = releaseInfo.getAsJsonArray(KEY_ASSETS)
                .filterIsInstance(JsonObject::class.java)
                .find { it[KEY_NAME].asString == asset }
                ?: throw IllegalArgumentException("Not found asset for $asset")
            val downloadURL = URL(foundAsset[KEY_BROWSER_DOWNLOAD_URL].asString)
            downloadURL.downloadTo(file)
            downloadURL.path
        }.onSuccess {
            callback?.invoke(Result.success(it))
        }.onFailure {
            callback?.invoke(Result.failure(it))
        }
    }

    // https://github.com/<user>/<repo>/releases/latest/download/<artifact>
}

class UpToDateException(message: String) : RuntimeException(message)

fun <T> URL.httpRequest(requester: (HttpURLConnection.() -> T)): T {
    return with(openConnection() as HttpURLConnection) { requester.invoke(this) }
}

fun URL.downloadTo(file: File) {
    val temp = File("${file.path}.tmp")

    openStream().buffered().use { input ->
        temp.outputStream().buffered().use { output ->
            val data = ByteArray(1024)

            while (true) {
                val count = input.read(data)
                if (count == -1) break

                output.write(data, 0, count)
            }
            output.close()
            temp.renameTo(file)
        }
    }
}

/**
 * https://github.com/로부터 latest release의 asset을 다운로드합니다.
 *
 * 현재 플러그인 버전보다 낮다면 다운로드하지 않고 [UpToDateException]을 발생시킵니다.
 *
 * 다운로드가 성공하면 callback에 url을 전달합니다.
 *
 * @see[String.compareVersion]
 *
 * @exception UpToDateException 최신버전일 경우 발생합니다.
 *
 */
fun JavaPlugin.updateFromGitHub(
    owner: String,
    project: String,
    asset: String,
    callback: (Result<String>.() -> Unit)? = null
) {
    GitHubSupport.downloadLatestRelease(updateFile, owner, project, description.version, asset, callback)
}

private val JavaPlugin.updateFile: File
    get() {
        val file = JavaPlugin::class.java.getDeclaredMethod("getFile").apply {
            isAccessible = true
        }.invoke(this) as File

        return File(file.parentFile, "update/${file.name}")
    }

/**
 * https://github.com/로부터 latest release의 asset을 ***비동기***로 다운로드합니다.
 *
 * 현재 플러그인 버전보다 낮다면 다운로드하지 않습니다.
 *
 * 다음처럼 사용 할 수 있습니다.
 *
 * `JavaPlugin.updateFromGitHubMagically(sender::sendMessage)`
 *
 * @param reciever 메시지 수신 함수
 *
 * @see[String.compareVersion]
 *
 */

@DelicateCoroutinesApi
fun JavaPlugin.updateFromGitHubMagically(
    owner: String,
    project: String,
    asset: String,
    reciever: ((String) -> Unit)? = null
): CompletableFuture<File> {
    val future = CompletableFuture<File>()
    reciever?.invoke("Attempt to update.")

    GlobalScope.launch {
        GitHubSupport.downloadLatestRelease(updateFile, owner, project, description.version, asset) {
            onSuccess { url ->
                reciever?.run {
                    invoke("Updated successfully. Applies after the server restarts.")
                    invoke(url)
                    future.complete(updateFile)
                }
            }
            onFailure { t ->
                if (t is UpToDateException) reciever?.invoke("UP TO DATE")
                else {
                    reciever?.invoke("Update failed. Check the stacktrace.")
                    t.printStackTrace()
                }
            }
        }
    }
    return future
}
