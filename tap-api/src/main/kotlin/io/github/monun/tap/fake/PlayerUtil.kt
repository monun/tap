package io.github.monun.tap.fake

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.util.*

/**
 * @author octomarine
 */
class PlayerData(val name: String, private val skin: Pair<String, String>, private var uuid: UUID = UUID.randomUUID()) {
    constructor(name: String, skin: String): this(name, fetchSkinInfo(fetchUUID(skin)))

    fun toGameProfile(): GameProfile {
        val profile = GameProfile(uuid, name)
        profile.properties.put("textures", Property("textures", skin.first, skin.second))
        return profile
    }

    fun refresh() {
        uuid = UUID.randomUUID()
    }
}

enum class PlayerInfoAction {
    ADD,
    GAME_MODE,
    LATENCY,
    DISPLAY_NAME,
    REMOVE
}

fun fetchUUID(name: String): String {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder(URI.create("https://api.mojang.com/profiles/minecraft"))
        .POST(BodyPublishers.ofString("[\"$name\"]"))
        .setHeader("Content-Type", "application/json")
        .build()
    val string = client.send(request, BodyHandlers.ofString()).body()
    return JsonParser.parseString(string).asJsonArray.first().asJsonObject.get("id").asString
}

fun fetchSkinInfo(uuid: String): Pair<String, String> {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/$uuid?unsigned=false"))
        .GET()
        .build()
    val json = client.send(request, BodyHandlers.ofString()).body()
    val properties = JsonParser.parseString(json).asJsonObject.get("properties").asJsonArray.first().asJsonObject
    return properties.get("value").asString to properties.get("signature").asString
}