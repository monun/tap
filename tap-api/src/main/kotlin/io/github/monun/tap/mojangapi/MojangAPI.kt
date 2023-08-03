/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.mojangapi

import com.destroystokyo.paper.profile.ProfileProperty
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.math.BigInteger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * https://wiki.vg/Mojang_API
 */
object MojangAPI {

    @Serializable
    data class Profile(
        val name: String,
        val id: String
    ) {
        fun uuid(): UUID = BigInteger(id, 16).let { bigInteger ->
            return UUID(bigInteger.shiftRight(64).toLong(), bigInteger.toLong())
        }
    }

    private inline fun <reified T> fetchAsync(url: String): CompletableFuture<T?> {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .build()

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply { response ->
            val body = response.body()
            if (body.isBlank()) null else Json { ignoreUnknownKeys = true }.decodeFromString<T>(body)
        }
    }

    fun fetchProfileAsync(username: String) =
        fetchAsync<Profile>("https://api.mojang.com/users/profiles/minecraft/$username")

    fun fetchProfile(username: String) = fetchProfileAsync(username).get()

    @Serializable
    data class SkinProfile(
        val id: String,
        val name: String,
        val properties: List<Property>
    ) {
        fun textureProfile() = properties.find { it.name == "textures" }?.let { textures ->
            val string = Base64.getDecoder().decode(textures.value).decodeToString()
            Json { ignoreUnknownKeys = true }.decodeFromString<TextureProfile>(string)
        }

        fun profileProperties() = properties.map { it.profileProperty() }
    }

    @Serializable
    data class Property(
        val name: String,
        val value: String,
        val signature: String? = null
    ) {
        fun profileProperty() = ProfileProperty(name, value, signature)
    }

    @Serializable
    data class TextureProfile(
        val timestamp: Long,
        val profileId: String,
        val profileName: String,
        val signatureRequired: Boolean,
        val textures: Textures
    )

    @Serializable
    data class Textures(
        val SKIN: TextureSkin,
        val CAPE: TextureCape? = null
    )

    @Serializable
    data class TextureSkin(val url: String, val metadata: TextureMetadata? = null)

    @Serializable
    data class TextureMetadata(val model: String)

    @Serializable
    data class TextureCape(val url: String)

    fun fetchSkinProfileAsync(trimmedUUID: String) =
        fetchAsync<SkinProfile>("https://sessionserver.mojang.com/session/minecraft/profile/$trimmedUUID?unsigned=false")

    fun fetchSkinProfile(trimmedUUID: String) = fetchSkinProfileAsync(trimmedUUID).get()

    fun fetchSkinProfileAsync(uuid: UUID) = fetchSkinProfileAsync(uuid.toString().replace("-", ""))

    fun fetchSkinProfile(uuid: UUID) = fetchSkinProfileAsync(uuid.toString().replace("-", "")).get()
}
