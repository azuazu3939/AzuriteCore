package com.github.azuazu3939.azurite.mana

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.util.Util
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.util.UUID

class ManaRegen(private val player: Player) : Mana(player) {

    private val regen = 10.0

    private fun getBaseRegen(): Double {return regen}

    private fun getFinalRegen(): Double {return (
            getBaseRegen() +
            Util.getDataContainerDouble(player, MANA_REGEN, Util.getAllSlots()) +
            Util.getPlayerDataContainerDouble(player, MANA_REGEN, 0.0) +
            Util.getSlotItemEnchantmentLevel(player, MANA_REGEN, Util.getAllSlots())
            ) / 2.0
    }

    fun start() {
        val taskNum = Azurite.runAsyncTimer(runnable = {
            setMana(getMaxMana().coerceAtMost(getFinalRegen() + getMana()))
        }, 10L, 10L).taskId
        tasks[player.uniqueId] = taskNum
    }

    fun stop() {
        if (tasks.containsKey(player.uniqueId)) {
            tasks[player.uniqueId]?.let { Bukkit.getScheduler().cancelTask(it)}
        }
        tasks.remove(player.uniqueId)
    }

    fun regen(percentage: Double) {
        setMana(getMaxMana().coerceAtMost(getMana() + getMaxMana() * percentage))
    }

    companion object {
        private val tasks = mutableMapOf<UUID, Int>()
        val MANA_REGEN = NamespacedKey("az", "mana_regen")
    }
}