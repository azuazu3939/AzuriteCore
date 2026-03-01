package com.github.azuazu3939.azurite.util

import com.github.azuazu3939.azurite.Azurite
import com.google.common.collect.Multimap
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.managers.RegionManager
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

object Util {

    private val enchantments = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)

    fun isCooldown(clazz: Class<*>, uuid: UUID, multimap: Multimap<Class<*>, UUID>): Boolean {
        return multimap.containsEntry(clazz, uuid)
    }

    fun setCooldown(clazz: Class<*>, uuid: UUID, multimap: Multimap<Class<*>, UUID>, long: Long) {
        multimap.put(clazz, uuid)
        Azurite.runLater(runnable = { multimap.remove(clazz, uuid) }, long)
    }

    fun removeAttribute(player: Player, attribute: Attribute, key: NamespacedKey) {
        val attr = player.getAttribute(attribute) ?: return
        attr.modifiers.stream().filter{ it.key == key }.forEach{ attr.removeModifier(it.key) }
    }

    fun addAttribute(player: Player, attribute: Attribute, modify: AttributeModifier) {
        val attr = player.getAttribute(attribute) ?: return
        attr.addModifier(modify)
    }

    @Suppress("unused")
    fun getAttribute(player: Player, attribute: Attribute, key: NamespacedKey) : Double {
        val attr = player.getAttribute(attribute) ?: return 0.0
        return attr.modifiers.stream().filter { it.key == key }.mapToDouble { it.amount }.sum()
    }

    @Suppress("unused")
    fun getArmorSlots(): Set<EquipmentSlot> {
        return mutableSetOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    }

    fun getAllSlots(): Set<EquipmentSlot> {
        return mutableSetOf(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    }

    private fun getItemEnchantmentLevel(item: ItemStack?, key: NamespacedKey): Int {
        if (item != null && item.hasItemMeta()) {
            return enchantments.get(key)?.let { item.getEnchantmentLevel(it) } ?: 0
        }
        return 0
    }

    fun getSlotItemEnchantmentLevel(player: Player, key: NamespacedKey, slots: Set<EquipmentSlot>): Int {
        return slots.sumOf {
            getItemEnchantmentLevel(player.inventory.getItem(it), key)
        }
    }

    private fun getItemDataContainerString(itemStack: ItemStack?, key: NamespacedKey): String? {
        if (itemStack != null && itemStack.hasItemMeta()) {
            return itemStack.itemMeta.persistentDataContainer.get(key, PersistentDataType.STRING)
        }
        return null
    }

    private fun getSlotDataContainerString(player: Player, slot: EquipmentSlot, key: NamespacedKey): String? {
        return getItemDataContainerString(player.inventory.getItem(slot), key)
    }

    fun getDataContainerDouble(player: Player, key: NamespacedKey, slots: Set<EquipmentSlot>): Double {
        return slots.sumOf {
            getSlotDataContainerString(player, it, key)?.toDoubleOrNull() ?: 0.0
        }
    }

    @Suppress("unused")
    fun getDataContainerInt(player: Player, key: NamespacedKey, slots: Set<EquipmentSlot>): Int {
        return slots.sumOf {
            getSlotDataContainerString(player, it, key)?.toIntOrNull() ?: 0
        }
    }

    fun getPlayerDataContainerDouble(player: Player, key: NamespacedKey, defaultValue: Double): Double {
        return player.persistentDataContainer.get(key, PersistentDataType.STRING)?.toDouble() ?: defaultValue
    }

    fun worldPreset(world: World, difficulty: Difficulty) {
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        world.setGameRule(GameRule.KEEP_INVENTORY, true)
        world.setGameRule(GameRule.SPAWN_RADIUS, 0)
        world.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0)
        world.setGameRule(GameRule.GLOBAL_SOUND_EVENTS, false)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.setGameRule(GameRule.MOB_GRIEFING, false)
        world.setGameRule(GameRule.DISABLE_RAIDS, true)
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false)
        world.setGameRule(GameRule.DO_TILE_DROPS, false)
        world.setGameRule(GameRule.DO_MOB_LOOT, false)
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false)
        world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 120)
        world.pvp = false
        world.voidDamageAmount = 1000F
        world.voidDamageMinBuildHeightOffset = 0.0
        world.setSpawnLocation(0, 64, 0)
        world.worldBorder.setCenter(0.0, 0.0)
        world.viewDistance = 8
        world.simulationDistance = 6
        world.difficulty = difficulty
    }

    fun getRegionManager(world: World): RegionManager? {
        return WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(world))
    }
}