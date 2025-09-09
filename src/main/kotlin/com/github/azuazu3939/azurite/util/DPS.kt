package com.github.azuazu3939.azurite.util

import com.github.azuazu3939.azurite.Azurite
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimaps
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import java.text.NumberFormat
import java.util.*

object DPS {

    private val damageData = Multimaps.synchronizedListMultimap<UUID, Pair<UUID, Double>>(ArrayListMultimap.create())
    private val shows = hashSetOf<UUID>()

    fun add(player: UUID, victim: UUID, damage: Double) {
        damageData.put(player, Pair(victim, damage))
        Azurite.runLater(runnable = { damageData.remove(player, Pair(victim, damage)) }, 100)
    }

    fun clear(player: UUID) {
        damageData.removeAll(player)
    }

    fun getDamage(player: UUID, victim: UUID): String {
        if (!damageData.containsKey(player)) return "0.0"
        val d = damageData[player].stream().filter { it.first == victim }.mapToDouble { it.second }.sum()
        val num = NumberFormat.getInstance()
        num.maximumFractionDigits = 2
        return num.format(d / 5.0)
    }

    fun on(player: UUID) = shows.add(player)

    fun off(player: UUID) = shows.remove(player)

    fun isOn(player: UUID) = shows.contains(player)

    fun show(player: Player, damage: String) {
        player.sendActionBar(Component.text("DPS $damage", NamedTextColor.RED).decorate(TextDecoration.BOLD))
    }
}