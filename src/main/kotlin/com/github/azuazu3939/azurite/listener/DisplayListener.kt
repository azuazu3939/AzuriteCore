package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.util.PacketUtil.displayMeta
import com.github.azuazu3939.azurite.util.PacketUtil.removeEntity
import com.github.azuazu3939.azurite.util.PacketUtil.spawnDisplay
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import io.lumine.mythic.core.mobs.ActiveMob
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.EntityType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.util.Vector
import java.text.DecimalFormat
import java.util.concurrent.ThreadLocalRandom

class DisplayListener(private val plugin: Azurite) : Listener {

    companion object {
        private const val DAMAGE_PREFIX = "§7§l⚔"
        private val RANDOM = ThreadLocalRandom.current()
        private val FORMAT = DecimalFormat("#.##")
    }

    private val elementPrefix: Map<String, String> = loadColorCache()

    /* ===================================================== */

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDisplay(event: MythicDamageEvent) {

        val attacker = event.caster.entity
        if (!attacker.isPlayer) return

        val victim = event.target
        if (victim.isPlayer || !victim.isLiving) return

        val mob: ActiveMob = plugin.mythic.mobManager
            .getActiveMob(victim.uniqueId)
            .orElse(null) ?: return

        val player = attacker.bukkitEntity as Player

        val element = event.damageMetadata.element
        val multi = mob.type.damageModifiers[element] ?: 1.0
        val finalDamage = event.damage * multi

        spawnDamageDisplay(
            player,
            victim,
            element,
            finalDamage
        )
    }

    /* ===================================================== */

    private fun spawnDamageDisplay(
        player: Player,
        victim: AbstractEntity,
        element: String?,
        damage: Double
    ) {
        val prefix = elementPrefix[element] ?: DAMAGE_PREFIX
        val text = Component.text(prefix + FORMAT.format(damage))

        val base = victim.bukkitEntity.location

        val vec = Vector(
            base.x + RANDOM.nextDouble(-1.0, 1.0),
            base.y + RANDOM.nextDouble(2.1, 2.6),
            base.z + RANDOM.nextDouble(-1.0, 1.0)
        )

        val id = RANDOM.nextInt(1, Int.MAX_VALUE)

        player.spawnDisplay(EntityType.TEXT_DISPLAY, vec, id)
        player.displayMeta(id, text)

        // 30tick後削除（Coroutine無し）
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            player.removeEntity(id)
        }, 30L)
    }

    /* ===================================================== */

    private fun loadColorCache(): Map<String, String> {
        val section = plugin.config.getConfigurationSection("Colors") ?: return emptyMap()
        val keys = section.getKeys(false)

        val map = HashMap<String, String>(keys.size)
        for (key in keys) {
            val color = section.getString(key) ?: continue
            map[key] = "$color§l⚔"
        }
        return map
    }
}