package com.github.azuazu3939.azurite.mana

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.event.ManaModifiedEvent
import com.github.azuazu3939.azurite.event.ManaModifyEvent
import com.github.azuazu3939.azurite.util.Util
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.joml.Math

open class Mana(private val player: Player) {

    fun getMana(): Double { return Util.getPlayerDataContainerDouble(player, MANA_VALUE, 0.0) }

    fun getMaxMana(): Double { return getPlayerMaxMana() + getItemMaxMana() + getItemEnchantmentMaxMana() }

    fun setMana(value: Double) {
        Azurite.run(runnable = {
            setMana(getMana(), getManaLimit(value), getMaxMana())
        })
    }

    @Synchronized
    fun setMana(before: Double, add: Double, max: Double) {
        val event = ManaModifyEvent(player, before, add, max)
        if (!event.callEvent()) return
        player.persistentDataContainer.set(MANA_VALUE, PersistentDataType.STRING, event.getAdd().toString())
        ManaModifiedEvent(player, event.getAdd()).callEvent()
    }

    fun setMaxMana(value: Double) { player.persistentDataContainer.set(MAX_MANA, PersistentDataType.STRING, value.toString()) }

    private fun getManaLimit(value: Double): Double {
        return Math.max(0.0, Math.min(getMaxMana(), value))
    }

    private fun getPlayerMaxMana(): Double { return Util.getPlayerDataContainerDouble(player, MAX_MANA, 500.0) }

    private fun getItemMaxMana(): Double { return Util.getDataContainerDouble(player, MAX_MANA, Util.getAllSlots()) }

    private fun getItemEnchantmentMaxMana(): Double { return Util.getSlotItemEnchantmentLevel(player, MAX_MANA, Util.getAllSlots()) * 50.0 }

    companion object {
        val MANA_VALUE = NamespacedKey("az", "mana")
        val MAX_MANA = NamespacedKey("az", "max_mana")
    }
}