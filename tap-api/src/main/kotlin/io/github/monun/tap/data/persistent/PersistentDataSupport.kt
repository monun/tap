package io.github.monun.tap.data.persistent

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.PluginClassLoader

/**
 * [PersistentDataContainer] 을 확장하여 플러그인에 종속적인 영구 데이터를 사용할 수 있게 해줍니다.
 *
 * ```
 * fun ItemStack.addCustomData() {
 *   editMeta { meta ->
 *     meta.persistentDataSupport[CustomKeychain.customOption] = 10
 *     meta.persistentDataSupport["info"] = "Hello world!"
 *   }
 *
 * object CustomKeychain : PersistentDataKeychain() {
 *    val customOption = castPrimitive<Int>("custom_option")
 * }
 *
 * ```
 *
 * @see PersistentDataKey
 * @see PersistentDataKeychain
 *
 * @author Monun
 */
@JvmInline
value class PersistentDataSupport(val container: PersistentDataContainer) {
    companion object {
        internal val plugin: JavaPlugin = (PersistentDataKeychain::class.java.classLoader as PluginClassLoader).plugin

        /**
         * 자바 타입을 [PersistentDataType] 으로 변환합니다.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> primitiveTypeOf(type: Class<T>): PersistentDataType<T, T>? {
            return when (type) {
                java.lang.Byte::class.java -> PersistentDataType.BYTE
                java.lang.Short::class.java -> PersistentDataType.SHORT
                java.lang.Integer::class.java -> PersistentDataType.INTEGER
                java.lang.Long::class.java -> PersistentDataType.LONG
                java.lang.Float::class.java -> PersistentDataType.FLOAT
                java.lang.Double::class.java -> PersistentDataType.DOUBLE
                java.lang.String::class.java -> PersistentDataType.STRING
                ByteArray::class.java -> PersistentDataType.BYTE_ARRAY
                IntArray::class.java -> PersistentDataType.INTEGER_ARRAY
                LongArray::class.java -> PersistentDataType.LONG_ARRAY
                Array<PersistentDataContainer>::class.java -> PersistentDataType.TAG_CONTAINER_ARRAY
                PersistentDataContainer::class.java -> PersistentDataType.TAG_CONTAINER
                else -> null
            } as PersistentDataType<T, T>?
        }


    }

    inline val isEmpty
        get() = container.isEmpty

    operator fun <T, Z> get(key: PersistentDataKey<T, Z>): Z? {
        return container[key.namespacedKey, key.dataType]
    }

    operator fun <T, Z> set(key: PersistentDataKey<T, Z>, value: Z & Any) {
        container[key.namespacedKey, key.dataType] = value
    }

    operator fun contains(key: PersistentDataKey<*, *>): Boolean {
        return container.has(key.namespacedKey, key.dataType)
    }

    fun remove(key: PersistentDataKey<*, *>) {
        container.remove(key.namespacedKey)
    }


    @Suppress("OPT_IN_USAGE")
    operator fun <T, Z> get(name: String, type: PersistentDataType<T, Z>): Z? {
        return container[NamespacedKey(plugin, name), type]
    }

    /**
     * 플러그인에 종속적인 영구 데이터를 가져옵니다.
     *
     * [Z] 가 [PersistentDataType] 기본 타입이 아닌 경우 [ProtoBuf] 을 사용하여 직렬화합니다.
     */
    @Suppress("OPT_IN_USAGE")
    inline operator fun <reified Z : Any> get(name: String): Z? {
        val dataType = primitiveTypeOf(Z::class.java)

        if (dataType != null) return get(name, dataType)

        val bytes = get(name, PersistentDataType.BYTE_ARRAY) ?: return null
        return ProtoBuf.decodeFromByteArray<Z>(bytes)
    }

    operator fun <T, Z> set(name: String, type: PersistentDataType<T, Z>, value: Z & Any) {
        container[NamespacedKey(plugin, name), type] = value
    }

    /**
     * 플러그인에 종속적인 영구 데이터를 저장합니다.
     *
     * [Z] 가 [PersistentDataType] 기본 타입이 아닌 경우 [ProtoBuf] 을 사용하여 직렬화합니다.
     */
    @OptIn(ExperimentalSerializationApi::class)
    inline operator fun <reified Z> set(name: String, value: Z & Any) {
        val dataType = primitiveTypeOf(Z::class.java)

        if (dataType != null) {
            set(name, dataType, value)
            return
        }

        val bytes = ProtoBuf.encodeToByteArray(value)
        set(name, PersistentDataType.BYTE_ARRAY, bytes)
    }

    fun <T, Z> contains(name: String, type: PersistentDataType<T, Z>): Boolean {
        return container.has(NamespacedKey(plugin, name), type)
    }

    inline operator fun <reified Z> contains(name: String): Boolean {
        val type = primitiveTypeOf(Z::class.java) ?: PersistentDataType.BYTE_ARRAY

        return contains(name, type)
    }

    fun remove(name: String) {
        container.remove(NamespacedKey(plugin, name))
    }

}

inline val PersistentDataHolder.persistentData
    get() = PersistentDataSupport(persistentDataContainer)
