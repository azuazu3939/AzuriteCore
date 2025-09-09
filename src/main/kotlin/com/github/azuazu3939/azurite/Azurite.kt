package com.github.azuazu3939.azurite

import com.github.azuazu3939.azurite.command.*
import com.github.azuazu3939.azurite.database.DBCon
import com.github.azuazu3939.azurite.listener.*
import com.github.azuazu3939.azurite.mana.ManaRegen
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class Azurite : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        registerListeners()
        registerCommands()

        Bukkit.getOnlinePlayers().forEach { ManaRegen(it).start() }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a")
    }

    override fun onDisable() {
        DBCon.close()
        ManaListener.removeAll()
    }

    private fun registerListeners() {
        val pm = server.pluginManager
        pm.registerSuspendingEvents(ManaListener(this), this)
        pm.registerEvents(MythicListener(), this)
        pm.registerSuspendingEvents(MythicBossListener(this), this)
        pm.registerSuspendingEvents(DisplayListener(this), this)
        pm.registerEvents(EatListener(), this)
        pm.registerEvents(AttackListener(), this)
        pm.registerEvents(DamageCalculationListener(), this)
        pm.registerSuspendingEvents(GenericRulesListener(this), this)
        pm.registerSuspendingEvents(StorageItemListener(this), this)
        pm.registerEvents(CombatLogListener(), this)
        pm.registerEvents(SummonListener(), this)
        pm.registerEvents(DPSListener(), this)
        pm.registerEvents(TrashListener(), this)
    }

    private fun registerCommands() {
        getCommand("worldset")!!.setExecutor(WorldSetCommand())
        getCommand("mode")!!.setExecutor(ModeCommand())
        getCommand("setmana")!!.setExecutor(SetManaCommand())
        getCommand("setmaxmana")!!.setExecutor(SetMaxManaCommand())
        getCommand("combatlog")!!.setExecutor(CombatLogCommand())
        getCommand("hopper")!!.setExecutor(HopperCommand())
        getCommand("dps")!!.setExecutor(DPSCommand())
        getCommand("fix")!!.setExecutor(FixCommand())
        getCommand("camera")!!.setExecutor(CameraCommand())
        getCommand("trash")!!.setExecutor(TrashCommand())
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
