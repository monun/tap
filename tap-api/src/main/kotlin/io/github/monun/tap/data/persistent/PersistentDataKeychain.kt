package io.github.monun.tap.data.persistent

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.PluginClassLoader

/**
 * [PersistentDataSupport] 에서 사용하는 키 묶음용 클래스입니다
 *
 * 예제 코드입니다
 * ```
 * object MyKeychain : PersistentDataKeychain() {
 *   val primKey = castPrimitive<Int>("myKey")
 *
 *   val compKey = castComplex<MyData>("myData") // MyData 는 kotlinx.serialization.Serializable 을 구현해야 합니다
 * }
 * ```
 *
 * @author Monun
 */
abstract class PersistentDataKeychain {

    /**
     * 지정된 이름과 타입으로 된 [PersistentDataKey] 를 생성합니다
     */
    protected fun <T, Z> cast(name: String, type: PersistentDataType<T, Z>): PersistentDataKey<T, Z> {
        return PersistentDataKey(NamespacedKey(PersistentDataSupport.plugin, name), type)
    }

    /**
     * 지정된 이름과 원시 타입으로 된 [PersistentDataKey] 를 생성합니다
     *
     * @param T [PersistentDataType] 의 원시 타입
     *
     * @exception IllegalArgumentException [T] 가 원시 타입이 아닐 경우
     */
    protected inline fun <reified T> castPrimitive(name: String): PersistentDataKey<T, T> {
        return cast(
            name,
            PersistentDataSupport.primitiveTypeOf(T::class.java)
                ?: error("Unsupported type: ${T::class.java}")
        )
    }

    /**
     * 지정된 이름과 복합 타입으로 된 [PersistentDataKey] 를 생성합니다
     *
     * @param Z [PersistentDataType] 의 복합 타입
     */
    @OptIn(ExperimentalSerializationApi::class)
    protected inline fun <reified Z> castComplex(
        name: String
    ): PersistentDataKey<ByteArray, Z> {
        return cast(name, object : PersistentDataType<ByteArray, Z> {
            override fun getPrimitiveType(): Class<ByteArray> {
                return ByteArray::class.java
            }

            override fun getComplexType(): Class<Z> {
                return Z::class.java
            }

            override fun toPrimitive(complex: Z & Any, context: PersistentDataAdapterContext): ByteArray {
                return ProtoBuf.encodeToByteArray(complex)
            }

            override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Z & Any {
                return ProtoBuf.decodeFromByteArray(primitive)
            }
        })
    }

    /**
     * 지정된 이름과 원시 타입과 복합 타입으로 된 [PersistentDataKey] 를 생성합니다
     *
     * 변환 함수를 통해 복합 타입과 원시 타입을 서로 변환할 수 있습니다
     *
     * @param T [PersistentDataType] 의 원시 타입
     * @param Z [PersistentDataType] 의 복합 타입
     * @param toPrimitive 복합 타입을 원시 타입으로 변환하는 함수
     * @param fromPrimitive 원시 타입을 복합 타입으로 변환하는 함수
     */
    protected inline fun <reified T, reified Z> castComplex(
        name: String,
        crossinline toPrimitive: (Z & Any) -> T & Any,
        crossinline fromPrimitive: (T & Any) -> Z & Any
    ): PersistentDataKey<T, Z> {
        return cast(name, object : PersistentDataType<T, Z> {
            override fun getPrimitiveType(): Class<T> {
                return T::class.java
            }

            override fun getComplexType(): Class<Z> {
                return Z::class.java
            }

            override fun toPrimitive(complex: Z & Any, context: PersistentDataAdapterContext): T & Any {
                return toPrimitive(complex)
            }

            override fun fromPrimitive(primitive: T & Any, context: PersistentDataAdapterContext): Z & Any {
                return fromPrimitive(primitive)
            }
        })
    }
}