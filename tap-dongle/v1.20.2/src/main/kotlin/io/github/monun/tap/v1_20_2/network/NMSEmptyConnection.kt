package io.github.monun.tap.v1_20_2.network

import net.minecraft.network.Connection
import net.minecraft.network.PacketListener
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import java.lang.invoke.MethodHandles
import java.net.SocketAddress

object NMSEmptyConnection : Connection(PacketFlow.CLIENTBOUND) {
    init {
        channel = NMSEmptyChannel(null)
        address = object : SocketAddress() {
            private val serialVersionUID = 8207338859896320185L
        }
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun send(packet: Packet<*>, genericfuturelistener: PacketSendListener?) {}
    override fun setListener(pl: PacketListener) {
        try {
            val lookup = MethodHandles.lookup()

            val connectionJavaClass = Connection::class.java
            val connectionPacketListener = connectionJavaClass.getDeclaredField("q")
            val connectionDisconnectListener = connectionJavaClass.getDeclaredField("p")
            connectionPacketListener.isAccessible = true
            connectionDisconnectListener.isAccessible = true

            lookup.unreflectSetter(connectionPacketListener).invoke(this, pl)
            lookup.unreflectSetter(connectionDisconnectListener).invoke(this, null)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}