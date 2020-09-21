/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object GitHubSupport {
    private const val GITHUB_API = "https://api.github.com"

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
     * https://github.com/로부터 다운로드 업데이트를 시도합니다.
     *
     * latest release의 업로드된 파일을 받아옵니다.
     *
     * 인수로 주어진 버전보다 낮다면 다운로드하지 않고 [UpToDateException]을 발생시킵니다.
     *
     * @exception UpToDateException
     */
    fun downloadUpdate(
        dest: File,
        owner: String,
        project: String,
        currentVersion: String,
        callback: (Result<String>.() -> Unit)? = null
    ) {
        runCatching {
            dest.parentFile?.mkdirs()
            val urlPath = generateUrlGitHubLatestRelease(owner, project)

            val json = URL(urlPath).httpRequest {
                requestMethod = "GET"
                addRequestProperty("Accept", REQUEST_ACCEPT)
                return@httpRequest inputStream.bufferedReader().use { JsonParser().parse(it) as JsonObject }
            }
            val version = json[KEY_TAG_NAME].asString
            //현재 버전이 같거나 더 높다면
            if (currentVersion.compareVersion(version) >= 0) {
                throw UpToDateException("Up to date")
            }
            //다운로드
            val downloadURL = json.find(KEY_BROWSER_DOWNLOAD_URL).first().asString
            URL(downloadURL).downloadTo(dest)
            downloadURL
        }.onSuccess {
            callback?.invoke(Result.success(it))
        }.onFailure {
            callback?.invoke(Result.failure(it))
        }
    }
}

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

private fun JsonObject.find(name: String): List<JsonElement> {
    val ret = arrayListOf<JsonElement>()

    for ((key, value) in entrySet()) {
        when {
            key == name -> ret.add(value)
            value is JsonObject -> ret.addAll(value.find(name))
            value is JsonArray -> value.forEach {
                if (it is JsonObject) {
                    ret.addAll(it.find(name))
                }
            }
        }
    }

    return ret
}

class UpToDateException(message: String) : RuntimeException(message)
