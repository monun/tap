package io.github.monun.tap.v1_20_4.network

import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl


class NMSEmptyPacketListener(
    minecraftServer: MinecraftServer,
    networkManager: Connection,
    entityPlayer: ServerPlayer,
    clc: CommonListenerCookie
) :
    ServerGamePacketListenerImpl(minecraftServer, networkManager, entityPlayer, clc) {
    override fun send(packet: Packet<*>) {}
}