package io.github.monun.tap.mojangapi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


class MojangAPITest {
    @Test
    fun testUsernameToUUID() {
        val profile = MojangAPI.fetchProfile("jeb_")!!
        assertEquals("jeb_", profile.name)
        assertEquals("853c80ef3c3749fdaa49938b674adae6", profile.id)
        assertNotNull(MojangAPI.fetchSkinProfile(profile.id))
    }
}
