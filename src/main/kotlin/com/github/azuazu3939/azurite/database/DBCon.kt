package com.github.azuazu3939.azurite.database

import com.github.azuazu3939.azurite.Azurite
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.mariadb.jdbc.Driver
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DBCon {

    private val config = JavaPlugin.getPlugin(Azurite::class.java).config
    private const val FIRST_TABLE = "az_first"
    private const val LOST_BOX_TABLE = "az_lost_box"
    private var dataSource: HikariDataSource?
    private val spawnSet: ConcurrentHashMap.KeySetView<UUID, Boolean> = ConcurrentHashMap.newKeySet()

    fun hasSpawn(uuid: UUID): Boolean {
        return spawnSet.contains(uuid)
    }

    fun loadSpawn(uuid: UUID) {
        if (hasSpawn(uuid)) return
        Azurite.runAsync(runnable = {
            try {
                runPrepareStatement("SELECT `is` FROM $FIRST_TABLE WHERE uuid = ?;".trimIndent()) {
                    it.setString(1, uuid.toString())
                    it.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            spawnSet.add(uuid)
                        }
                    }
                }
            } catch (ex: SQLException) {
                throw ex
            }
        })
    }

    fun setSpawn(uuid: UUID) {
        Azurite.runAsync(runnable = {
            try {
                runPrepareStatement("INSERT INTO $FIRST_TABLE (`uuid`, `is`) VALUES (?,?) ON DUPLICATE KEY UPDATE `is` = ?;".trimIndent()) {
                    it.setString(1, uuid.toString())
                    it.setBoolean(2, true)
                    it.setBoolean(3, true)
                    it.executeUpdate()
                    spawnSet.add(uuid)
                }
            } catch (ex: SQLException) {
                throw ex
            }
        })
    }

    fun loadBox(uuid: UUID, box: Inventory): Inventory {
        try {
            var item = MythicBukkit.inst().itemManager.getItemStack("Azuriter_ロストアイテム_未開放")
            for (ii in 0..53) {
                box.setItem(ii, item)
            }

            runPrepareStatement("SELECT `lost_num`, `count` FROM $LOST_BOX_TABLE WHERE `uuid` = ?;".trimIndent()) {
                it.setString(1, uuid.toString())
                it.executeQuery().use { resultSet ->

                    while (resultSet.next()) {
                        val lostNum = resultSet.getInt("lost_num")
                        item = MythicBukkit.inst().itemManager.getItemStack("Azuriter_ロストアイテム_$lostNum}", resultSet.getInt("count"))
                        box.setItem(lostNum, item)
                    }
                }
            }
        } catch (ex: SQLException) {
            throw ex
        }
        return box
    }

    fun addBox(uuid: UUID, slot: Int, amount: Int) {
        Azurite.runAsync(runnable = {
            try {
                runPrepareStatement("INSERT INTO $LOST_BOX_TABLE (`uuid`, `lost_num`, `count`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `count` =?;".trimIndent()) {
                    it.setString(1, uuid.toString())
                    it.setInt(2, slot)
                    it.setInt(3, amount)
                    it.executeUpdate()
                }
            } catch (ex: SQLException) {
                throw ex
            }
        })
    }

    @Throws(SQLException::class)
    fun createTables() {
        runPrepareStatement(
            """
                CREATE TABLE IF NOT EXISTS `$FIRST_TABLE` (
                `uuid` varchar(36) NOT NULL, 
                `is` tinyint(2), 
                PRIMARY KEY (`uuid`)
                )
                """.trimIndent(), PreparedStatement::execute
        )
        runPrepareStatement(
            """
                CREATE TABLE IF NOT EXISTS `$LOST_BOX_TABLE` (
                `uuid` varchar(36) NOT NULL,
                `lost_num` tinyint,
                `count` int,
                PRIMARY KEY (`uuid`)
                )
        """.trimIndent(), PreparedStatement::execute)
    }

    fun close() {
        if (!dataSource?.isClosed!! || dataSource != null) {
            dataSource?.close()
        }
    }

    private fun getDataSource(): HikariDataSource {
        return dataSource!!
    }

    @Throws(SQLException::class)
    private fun getConnection(): Connection {
        return getDataSource().connection
    }

    fun interface SQLThrowableConsumer<T> {
        @Throws(SQLException::class)
        fun accept(t: T)
    }

    @Contract(pure = true)
    @Throws(SQLException::class)
    fun use(action: SQLThrowableConsumer<Connection>) {
        getConnection().use { con ->
            action.accept(con)
        }
    }

    @Suppress("SqlSourceToSinkFlow")
    @Contract(pure = true)
    @Throws(SQLException::class)
    fun runPrepareStatement(sql: String, action: SQLThrowableConsumer<PreparedStatement>) {
        use { connection ->
            connection.prepareStatement(sql).use { preparedStatement ->
                action.accept(preparedStatement)
            }
        }
    }

    init {
        Driver()
        val hikari = HikariConfig()
        val host = config.getString("Database.host")
        val port = config.getInt("Database.port")
        val database = config.getString("Database.database")
        val username = config.getString("Database.username")
        val password = config.getString("Database.password")
        val scheme = config.getString("Database.scheme")

        hikari.jdbcUrl = "$scheme://$host:$port/$database"
        hikari.connectionTimeout = 3000
        hikari.maximumPoolSize = 12
        hikari.minimumIdle = 5
        hikari.username = username
        hikari.password = password
        hikari.connectionTimeout = 15000
        hikari.idleTimeout = 600000
        hikari.maxLifetime = 1800000
        hikari.connectionInitSql = "SET time_zone = '+09:00'"
        hikari.addDataSourceProperty("cachePrepStmts", "true")
        hikari.addDataSourceProperty("prepStmtCacheSize", "250")
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        hikari.addDataSourceProperty("useLocalSessionState", "true")
        hikari.addDataSourceProperty("useServerPrepStmts", "true")
        hikari.addDataSourceProperty("rewriteBatchedStatements", "true")
        hikari.addDataSourceProperty("cacheResultSetMetadata", "true")
        hikari.addDataSourceProperty("cacheServerConfiguration", "true")
        hikari.addDataSourceProperty("elideSetAutoCommits", "true")
        hikari.addDataSourceProperty("maintainTimeStats", "false")

        dataSource = HikariDataSource(hikari)
        createTables()
    }
}