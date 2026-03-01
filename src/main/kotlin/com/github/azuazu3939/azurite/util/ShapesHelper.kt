package com.github.azuazu3939.azurite.util

import io.lumine.mythic.bukkit.utils.Schedulers
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

object ShapesHelper {

    fun generateAsync(
        origin: Location,
        generator: () -> List<Vector>,
        callback: (World, Int, Int, Int) -> Unit
    ) {

        val world = origin.world ?: return
        val ox = origin.blockX
        val oy = origin.blockY
        val oz = origin.blockZ

        Schedulers.async().run {

            val vecs = generator()

            Schedulers.sync().run {

                for (v in vecs) {
                    callback(
                        world,
                        ox + v.blockX,
                        oy + v.blockY,
                        oz + v.blockZ
                    )
                }
            }
        }
    }
}