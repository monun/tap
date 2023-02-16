package io.github.monun.tap.data.meta

import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.LazyMetadataValue
import org.bukkit.metadata.LazyMetadataValue.CacheStrategy
import org.bukkit.metadata.Metadatable
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.PluginClassLoader
import java.util.concurrent.Callable
import kotlin.reflect.KProperty

/**
 *  [Metadatable] 을 확장하여 플러그인에 종속적인 메타데이터를 사용할 수 있게 해줍니다.
 *
 * @author Monun
 */
@JvmInline
value class MetadataSupport(private val metadatable: Metadatable) {
    private companion object {
        val plugin: JavaPlugin = (MetadataSupport::class.java.classLoader as PluginClassLoader).plugin
    }

    /**
     * 메타데이터를 가져옵니다.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? {
        return metadatable.getMetadata(key).find { it.owningPlugin === plugin }?.value() as T?
    }

    /**
     * 메타데이터를 설정합니다.
     */
    operator fun set(key: String, value: Any) {
        metadatable.setMetadata(key, FixedMetadataValue(plugin, value))
    }

    /**
     * Lazy 메타데이터를 설정합니다.
     */
    operator fun set(key: String, cacheStrategy: CacheStrategy, callable: Callable<Any>) {
        metadatable.setMetadata(
            key,
            LazyMetadataValue(plugin, cacheStrategy, callable)
        )
    }

    /**
     * 메타데이터를 삭제합니다.
     */
    fun remove(key: String) {
        metadatable.removeMetadata(key, plugin)
    }
}

/**
 * [Metadatable] 을 확장합니다.
 */
val Metadatable.metadata: MetadataSupport
    get() = MetadataSupport(this)

operator fun <T> MetadataSupport.getValue(thisRef: Any?, property: KProperty<*>): T? {
    return get<T>(property.name)
}
