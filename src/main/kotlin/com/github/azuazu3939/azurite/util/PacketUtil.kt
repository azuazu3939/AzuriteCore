package com.github.azuazu3939.azurite.util

import com.github.azuazu3939.azurite.Azurite
import io.netty.channel.Channel
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

object PacketUtil {
    private const val NAME = "azurite"

    fun inject(player: Player) {
        val handler = PacketHandler()
        try {
            getChannel(player).pipeline().addBefore("packet_handler", NAME, handler)
        } catch (e: Exception) {
            try {
                Azurite.runLater({
                    getChannel(player).pipeline().addBefore("packet_handler", NAME, handler)
                }, 10)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    @Throws(Exception::class)
    fun eject(player: Player) {
        if (getChannel(player).pipeline().get(NAME) != null) {
            getChannel(player).pipeline().remove(NAME)
        }
    }

    fun getChannel(player: Player): Channel {
        return (player as CraftPlayer).handle.connection.connection.channel
    }
}