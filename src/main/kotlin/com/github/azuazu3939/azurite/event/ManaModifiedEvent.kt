package com.github.azuazu3939.azurite.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ManaModifiedEvent(private val player: Player, private val result: Double) : Event() {

    fun getPlayer(): Player {return player}

    @Suppress("unused")
    fun getResult(): Double {return result}

    override fun getHandlers(): HandlerList { return handle }

    companion object {
        private val handle = HandlerList()

        @JvmStatic
        @Suppress("unused")
        fun getHandlerList(): HandlerList {return handle }
    }
}