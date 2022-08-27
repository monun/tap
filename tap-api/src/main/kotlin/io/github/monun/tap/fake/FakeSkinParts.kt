package io.github.monun.tap.fake

import com.destroystokyo.paper.SkinParts

class FakeSkinParts(
    raw: Int = 0b1111111
) {
    companion object {
        fun from(parts: SkinParts) = FakeSkinParts(parts.raw)
    }

    var raw: Int = raw
        private set

    fun has(part: SkinPart): Boolean {
        return raw and part.bit != 0
    }

    fun enable(part: SkinPart) {
        raw = raw or part.bit
    }

    operator fun plusAssign(part: SkinPart) {
        enable(part)
    }

    fun disable(part: SkinPart) {
        raw = raw and part.bit.inv()
    }

    operator fun minusAssign(part: SkinPart) {
        disable(part)
    }

    fun enableAll() {
        raw = 0b1111111
    }

    fun disableAll() {
        raw = 0
    }
}

enum class SkinPart {
    CAPE,
    JACKET,
    LEFT_SLEEVE,
    RIGHT_SLEEVE,
    LEFT_PANTS,
    RIGHT_PANTS,
    HATS;

    val bit: Int = 1 shl ordinal
}

