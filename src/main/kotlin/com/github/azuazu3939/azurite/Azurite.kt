package com.github.azuazu3939.azurite

import com.github.azuazu3939.azurite.command.*
import com.github.azuazu3939.azurite.database.DBCon
import com.github.azuazu3939.azurite.listener.*
import com.github.azuazu3939.azurite.util.PacketUtil
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class Azurite : JavaPlugin() {

    lateinit var mythic: MythicBukkit
        private set

    override fun onEnable() {
        mythic = MythicBukkit.inst()

        saveDefaultConfig()

        registerListeners()
        registerCommands()

        Bukkit.getOnlinePlayers().forEach {
            PacketUtil.inject(it, this)
        }

        runLater(runnable = {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a")
        }, 20)
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach {
            PacketUtil.eject(it)
        }
        DBCon.close()
    }

    private fun registerListeners() {
        val pm = server.pluginManager
        pm.registerEvents(MythicListener(this), this)
        pm.registerEvents(MythicBossListener(this), this)
        pm.registerEvents(MythicHealthListener(this), this)
        pm.registerEvents(MythicEXPListener(this), this)
        pm.registerEvents(DisplayListener(this), this)
        pm.registerEvents(EatListener(), this)
        pm.registerEvents(MeleeListener(this), this)
        pm.registerEvents(DamageCalculationListener(this), this)
        pm.registerEvents(GenericRulesListener(this), this)
        pm.registerEvents(StorageItemListener(this), this)
        pm.registerEvents(MaskChunkCleanupListener(), this)
        pm.registerEvents(PacketEntityListener(this), this)
        pm.registerEvents(CombatLogListener(), this)
        pm.registerEvents(SummonListener(this), this)
        pm.registerEvents(DPSListener(), this)
        pm.registerEvents(TrashListener(this), this)
    }

    private fun registerCommands() {
        getCommand("worldset")?.setExecutor(WorldSetCommand())
        getCommand("mode")?.setExecutor(ModeCommand())
        getCommand("combatlog")?.setExecutor(CombatLogCommand())
        getCommand("hopper")?.setExecutor(HopperCommand(this))
        getCommand("dps")?.setExecutor(DPSCommand())
        getCommand("camera")?.setExecutor(CameraCommand())
        getCommand("trash")?.setExecutor(TrashCommand())
        getCommand("fly")?.setExecutor(FlyCommand())
        getCommand("gm")?.setExecutor(GamemodeCommand())
        getCommand("flyspeed")?.setExecutor(FlySpeedCommand())
        getCommand("lock")?.setExecutor(LockCommand())
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

        fun runTimer(runnable: Runnable, delay: Long, repeat: Long) : BukkitTask {
            return Bukkit.getScheduler().runTaskTimer(getPlugin(Azurite::class.java), runnable, delay, repeat)
        }
    }
}
