package io.github.monun.tap.data

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

/**
 * [PersistentDataSupport] 에서 사용하는 키입니다.
 * [NamespacedKey] 와 [PersistentDataType] 를 결합한 클래스입니다
 *
 * @author Monun
 */
data class PersistentDataKey<T, Z>(
    val namespacedKey: NamespacedKey,
    val dataType: PersistentDataType<T, Z>
)