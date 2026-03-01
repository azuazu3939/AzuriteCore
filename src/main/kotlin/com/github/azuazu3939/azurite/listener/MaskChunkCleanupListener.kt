package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.mythic.mechanic.AddMask
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkUnloadEvent

class MaskChunkCleanupListener : Listener {

    @EventHandler
    fun onUnload(e: ChunkUnloadEvent) {
        val worldHash = e.world.uid.mostSignificantBits.toInt()
        val cx = e.chunk.x and 0xFFFF
        val cz = e.chunk.z and 0xFFFF

        AddMask.maskHolder.keys.removeIf {
            val w = (it shr 48).toInt()
            val x = ((it shr 32) and 0xFFFF).toInt()
            val z = ((it shr 16) and 0xFFFF).toInt()
            w == worldHash && x == cx && z == cz
        }
    }
}