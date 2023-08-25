package io.github.monun.tap.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.util.Vector
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.*

/**
 * [PersistentDataSupport] 에서 사용하는 키 묶음용 클래스입니다
 *
 * 예제 코드입니다
 * ```
 * object MyKeychain : PersistentDataKeychain() {
 *   val primKey = primitive<Int>("myKey")
 *
 *   val compKey = complex<MyData>("myData") // MyData 는 kotlinx.serialization.Serializable 을 구현해야 합니다
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
    protected inline fun <reified T> primitive(name: String): PersistentDataKey<T, T> {
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
    protected inline fun <reified Z> complex(
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
    protected inline fun <reified T, reified Z> complex(
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

    // 아래는 사용빈도가 높은 기타 키 주조 함수들
    protected inline fun <reified T : Enum<T>> enum(
        name: String
    ): PersistentDataKey<String, Enum<*>> {
        val enumClass = T::class.java

        return complex(name, {
            it.name
        }, {
            enumClass.enumConstants.first { enum -> enum.name == it }
        })
    }

    protected fun uuid(name: String): PersistentDataKey<ByteArray, UUID> {
        return complex(name, {
            ByteBuffer.wrap(ByteArray(16)).apply {
                putLong(it.mostSignificantBits)
                putLong(it.leastSignificantBits)
            }.array()
        }, {
            ByteBuffer.wrap(it).run { UUID(long, long) }
        })
    }

    protected fun block(name: String): PersistentDataKey<ByteArray, Block> {
        return complex(name, {
            ByteBuffer.wrap(ByteArray(40)).apply {
                val worldUUID = it.world.uid
                putLong(worldUUID.mostSignificantBits) // 8
                putLong(worldUUID.leastSignificantBits) // 8
                putInt(it.x) // 4
                putInt(it.y) // 4
                putInt(it.z) // 4
            }.array()
        }, {
            ByteBuffer.wrap(it).run {
                val uniqueId = UUID(long, long)
                Bukkit.getWorld(uniqueId)?.getBlockAt(int, int, int) ?: error("Block not found for $uniqueId")
            }
        })
    }

    protected fun location(name: String): PersistentDataKey<ByteArray, Location> {
        return complex(name, {
            ByteBuffer.wrap(ByteArray(48)).apply {
                val worldUUID = it.world?.uid
                putLong(worldUUID?.mostSignificantBits ?: 0) // 8
                putLong(worldUUID?.leastSignificantBits ?: 0) // 8
                putDouble(it.x) // 8
                putDouble(it.y) // 8
                putDouble(it.z) // 8
                putFloat(it.yaw)// 4
                putFloat(it.pitch) // 4
            }.array()
        }, {
            ByteBuffer.wrap(it).run {
                Location(
                    Bukkit.getWorld(UUID(long, long)),
                    double,
                    double,
                    double,
                    float,
                    float
                )
            }
        })
    }

    protected fun vector(name: String): PersistentDataKey<ByteArray, Vector> {
        return complex(name, {
            ByteBuffer.wrap(ByteArray(24)).apply {
                putDouble(it.x) // 8
                putDouble(it.y) // 8
                putDouble(it.z) // 8
            }.array()
        }, {
            ByteBuffer.wrap(it).run {
                Vector(double, double, double)
            }
        })
    }

    protected fun itemStack(name: String): PersistentDataKey<ByteArray, ItemStack> {
        return complex(name, {
            it.serializeAsBytes()
        }, {
            ItemStack.deserializeBytes(it)
        })
    }

    protected fun boolean(name: String): PersistentDataKey<Byte, Boolean> {
        return complex(name, {
            if (it) 1.toByte() else 0.toByte()
        }, {
            it != 0.toByte()
        })
    }
}