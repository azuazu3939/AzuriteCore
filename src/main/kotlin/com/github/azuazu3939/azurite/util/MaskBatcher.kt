package com.github.azuazu3939.azurite.util

import io.lumine.mythic.api.adapters.*
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.utils.Schedulers
import java.util.concurrent.ConcurrentHashMap

object MaskBatcher {

    private val queue =
        ConcurrentHashMap<AbstractPlayer, MutableMap<AbstractLocation, AbstractBlock?>>()

    private var scheduled = false

    fun queue(
        players: Collection<AbstractPlayer>,
        blocks: Map<AbstractLocation, AbstractBlock?>
    ) {
        for (p in players) {
            val map = queue.computeIfAbsent(p){ HashMap() }
            map.putAll(blocks)
        }

        if (!scheduled) {
            scheduled = true
            Schedulers.sync().run {
                flush()
            }
        }
    }

    private fun flush() {

        val handler = MythicBukkit.inst().volatileCodeHandler.blockHandler

        for ((player, blocks) in queue) {
            handler.sendMultiBlockChange(listOf(player), blocks)
        }

        queue.clear()
        scheduled=false
    }
}