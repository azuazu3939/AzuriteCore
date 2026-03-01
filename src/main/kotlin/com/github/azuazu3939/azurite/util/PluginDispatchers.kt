package com.github.azuazu3939.azurite.util

import com.github.azuazu3939.azurite.Azurite
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.scope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.plugin.java.JavaPlugin

object PluginDispatchers {

    class PluginTaskScope(val plugin: JavaPlugin) {
        suspend fun <T> async(block: suspend () -> T): T =
            withContext(plugin.asyncDispatcher) { block() }

        suspend fun <T> sync(block: suspend () -> T): T =
            withContext(plugin.minecraftDispatcher) { block() }
    }

    fun JavaPlugin.runTask(block: suspend PluginTaskScope.() -> Unit) {
        JavaPlugin.getPlugin(Azurite::class.java).scope.launch {
            PluginTaskScope(this@runTask).block()
        }
    }
}