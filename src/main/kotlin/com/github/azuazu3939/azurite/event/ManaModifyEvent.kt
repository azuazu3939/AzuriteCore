package com.github.azuazu3939.azurite.event

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ManaModifyEvent(private val player: Player, private val before: Double, private var add: Double, private val max: Double) : Event(), Cancellable {

    private var cancelled = false

    fun getPlayer(): Player { return player }

    fun getBefore(): Double { return before }

    @Suppress("unused")
    fun getMax(): Double { return max }

    fun getAdd(): Double { return add }

    @Suppress("unused")
    fun setAdd(value: Double) { add = value }

    override fun getHandlers(): HandlerList { return handler }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        private val handler= HandlerList()

        @JvmStatic
        @Suppress("unused")
        fun getHandlerList(): HandlerList {return handler }
    }
}