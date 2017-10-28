package de.mkammerer.unisontray

import de.mkammerer.unisontray.config.Config
import de.mkammerer.unisontray.config.ConfigManager
import de.mkammerer.unisontray.config.ConfigManagerImpl
import de.mkammerer.unisontray.tray.Tray
import de.mkammerer.unisontray.tray.TrayImpl
import de.mkammerer.unisontray.unison.ExitCode
import de.mkammerer.unisontray.unison.Profile
import de.mkammerer.unisontray.unison.Unison
import de.mkammerer.unisontray.unison.UnisonImpl
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

fun main(args: Array<String>) {
    Main.start(args)
}

object Main {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val syncInProgress = AtomicBoolean(false)

    fun start(args: Array<String>) {
        logger.info("Started")
        try {
            run()
        } catch (e: Exception) {
            logger.error("Unhandled exception occurred", e)
        }
    }

    private fun run() {
        val executorService = Executors.newSingleThreadScheduledExecutor()

        val tray: Tray = TrayImpl(executorService)
        val unison: Unison = UnisonImpl()
        val configManager: ConfigManager = ConfigManagerImpl()

        logger.info("Loading config")
        val config = configManager.load()
        logger.info("Config: {}", config)
        configManager.save(config)

        logger.info("Initializing tray icon")
        tray.init(this::onQuit, { sync(unison, config, tray) })
        tray.idle()

        logger.info("Installing shutdown hook")
        Runtime.getRuntime().addShutdownHook(Thread({
            logger.info("Stopping")
            executorService.shutdown()
            executorService.awaitTermination(10, TimeUnit.SECONDS)
            executorService.shutdownNow()

            tray.close()
            logger.info("Stopped")
        }, "tray-shutdown"))

        logger.info("Schedulung periodic sync task")
        executorService.scheduleWithFixedDelay({
            sync(unison, config, tray)
        }, 0, config.syncInterval.toLong(), TimeUnit.SECONDS)
    }

    private fun onQuit() {
        System.exit(0)
    }

    private fun sync(unison: Unison, config: Config, tray: Tray) {
        if (syncInProgress.get()) return

        try {
            logger.info("Starting sync")
            syncInProgress.set(true)

            tray.startRefresh()
            val result = try {
                unison.run(Profile(config.profile))
            } finally {
                tray.stopRefresh()
            }
            if (result.exitCode == ExitCode.FATAL) {
                logger.error("Error running unison: {}", result)
                tray.error()
            } else {
                logger.info("Sync success: {}", result)
                tray.idle()
            }

            logger.info("Sync done")
        } catch (e: Exception) {
            logger.error("Exception in sync task", e)
            tray.error()
        } finally {
            syncInProgress.set(false)
        }
    }
}