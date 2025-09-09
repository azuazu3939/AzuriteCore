package com.github.azuazu3939.azurite

import com.github.azuazu3939.azurite.command.*
import com.github.azuazu3939.azurite.database.DBCon
import com.github.azuazu3939.azurite.listener.*
import com.github.azuazu3939.azurite.mana.ManaRegen
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class Azurite : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        registerListeners()
        registerCommands()
        registerDBInit()

        Bukkit.getOnlinePlayers().forEach { ManaRegen(it).start() }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a")
    }

    override fun onDisable() {
        DBCon.close()
        ManaListener.removeAll()
    }

    private fun registerListeners() {
        val pm = server.pluginManager
        pm.registerEvents(ManaListener(), this)
        pm.registerEvents(MythicListener(), this)
        pm.registerEvents(MythicBossListener(), this)
        pm.registerEvents(DisplayListener(), this)
        pm.registerEvents(EatListener(), this)
        pm.registerEvents(AttackListener(), this)
        pm.registerEvents(DamageCalculationListener(), this)
        pm.registerEvents(GenericRulesListener(), this)
        pm.registerEvents(StorageItemListener(), this)
        pm.registerEvents(CombatLogListener(), this)
        pm.registerEvents(SummonListener(), this)
        pm.registerEvents(DPSListener(), this)
        pm.registerEvents(TrashListener(), this)
    }

    private fun registerCommands() {
        getCommand("worldset")?.setExecutor(WorldSetCommand())
        getCommand("mode")?.setExecutor(ModeCommand())
        getCommand("setmana")?.setExecutor(SetManaCommand())
        getCommand("setmaxmana")?.setExecutor(SetMaxManaCommand())
        getCommand("combatlog")?.setExecutor(CombatLogCommand())
        getCommand("hopper")?.setExecutor(HopperCommand())
        getCommand("dps")?.setExecutor(DPSCommand())
        getCommand("fix")?.setExecutor(FixCommand())
        getCommand("camera")?.setExecutor(CameraCommand())
        getCommand("trash")?.setExecutor(TrashCommand())
    }

    private fun registerDBInit() {
        try {
            DBCon
        } catch (ex: Exception) {
            throw ex
        }
    }

    companion object {

        fun runAsync(runnable: Runnable) : BukkitTask {
            return Bukkit.getScheduler().runTaskAsynchronously(getPlugin(Azurite::class.java), runnable)
        }

        fun runAsyncLater(runnable: Runnable, delay: Long) : BukkitTask {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(Azurite::class.java), runnable, delay)
        }

        fun runAsyncTimer(runnable: Runnable, delay: Long, repeat: Long) : BukkitTask {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(Azurite::class.java), runnable, delay, repeat)
        }

        fun run(runnable: Runnable) : BukkitTask {
            return Bukkit.getScheduler().runTask(getPlugin(Azurite::class.java), runnable)
        }

        fun runLater(runnable: Runnable, delay: Long) : BukkitTask {
            return Bukkit.getScheduler().runTaskLater(getPlugin(Azurite::class.java), runnable, delay)
        }
    }
}
