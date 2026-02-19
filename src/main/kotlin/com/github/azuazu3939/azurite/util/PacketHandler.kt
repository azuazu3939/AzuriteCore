package com.github.azuazu3939.azurite.util

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.network.protocol.game.ServerboundInteractPacket


class PacketHandler : ChannelDuplexHandler() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is ServerboundInteractPacket) {
            try {
                if (msg.isAttack) return
            } catch (e: Throwable) {
                if (e is VirtualMachineError) {
                    throw e
                }
                super.channelRead(ctx, msg)
            }
        }
        super.channelRead(ctx, msg)
    }
}