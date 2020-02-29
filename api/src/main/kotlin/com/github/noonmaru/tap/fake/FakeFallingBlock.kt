package com.github.noonmaru.tap.fake

import org.bukkit.entity.FallingBlock

class FakeFallingBlock internal constructor(val fallingBlock: FallingBlock) : FakeEntity(fallingBlock) {

    val blockData
        get() = fallingBlock.blockData

}