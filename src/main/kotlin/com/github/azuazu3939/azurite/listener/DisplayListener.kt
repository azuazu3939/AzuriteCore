package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import io.lumine.mythic.core.mobs.ActiveMob
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.text.NumberFormat
import java.util.*

class DisplayListener : Listener {

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDisplay(event: MythicDamageEvent) {
        val attacker = event.caster.entity
        val victim = event.target

        if (victim.isPlayer || !victim.isLiving) return
        val mob = MythicBukkit.inst().mobManager.getActiveMob(victim.uniqueId).orElse(null)
        if (mob != null && attacker.isPlayer) {
            val d = event.damage * DamageCalculationListener.damageResistance(victim.bukkitEntity)
            displayText(attacker.bukkitEntity as Player, victim, mob, event.damageMetadata.element, d)
        }
    }

    private fun displayText(player: Player, victim: AbstractEntity, mob: ActiveMob, element: String?, damage: Double) {
        val multi = mob.type.damageModifiers.getOrDefault(element, 1.0)
        val amount = formatDamage(damage * multi)
        val loc = getNoise(victim.bukkitEntity.location)
        val comp = Component.text(getElement(element) + amount)

        sendDisplayText(player, loc, comp, Random().nextInt(Int.MAX_VALUE))
    }

    private fun sendDisplayText(player: Player, location: Location, component: Component, id: Int) {
        spawnDisplay(player, location.x, location.y, location.z, id)
        displayMeta(player, id, component)
        Azurite.runAsyncLater(runnable = { removeDisplay(player, id) }, 30)
    }

    private fun formatDamage(amount: Double): String {
        val num = NumberFormat.getInstance()
        num.maximumFractionDigits = 2
        return num.format(amount).replace(",", "")
    }

    private fun getNoise(location: Location): Location {
        val rand = Random()
        return location.add(
            rand.nextInt(5) * 0.5 - 1,
            (rand.nextInt(5) + 1) * 0.1 + 2,
            rand.nextInt(5) * 0.5 - 1)
    }


    private fun getColors(): Map<String, String?> {
        val yml = JavaPlugin.getPlugin(Azurite::class.java).config
        if (yml.getConfigurationSection("Colors") == null) return mapOf()
        return yml.getConfigurationSection("Colors")!!.getKeys(false).associateWith { yml.getString("Colors.$it") }
    }

    private fun getElement(element: String?): String {
        if (element == null) return DAMAGE_PREFIX
        return getColors()
            .entries.stream()
            .filter { element == it.key }.map { it.value }.findFirst().map {
                it?.let { it1 ->
                    DAMAGE_PREFIX.replace("§7§l", it1)
                }
            }.orElse(DAMAGE_PREFIX)
    }

    private fun spawnDisplay(player: Player, x: Double, y: Double, z: Double, id: Int) {
        val packet = ClientboundAddEntityPacket(id, UUID.randomUUID(), x, y, z, 0F, 0F, EntityType.TEXT_DISPLAY, id, Vec3.ZERO, 0.0)
        send(player, packet)
    }

    private fun displayMeta(player: Player, id: Int, text: Component) {
        val net = PaperAdventure.asVanilla(text)
        val list = arrayListOf<SynchedEntityData.DataValue<*>>()

        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.COMPONENT.createAccessor(23), net))
        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(15), 3.toByte()))
        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(26), 255.toByte()))
        val packet = ClientboundSetEntityDataPacket(id, list)
        send(player, packet)
    }

    private fun removeDisplay(player: Player, id: Int) {
        val packet = ClientboundRemoveEntitiesPacket(id)
        send(player, packet)
    }

    private fun send(player: Player, packet: Packet<*>) {
        (player as CraftPlayer).handle.connection.send(packet)
    }

    companion object {
        const val DAMAGE_PREFIX = "§7§l⚔"
    }
}
