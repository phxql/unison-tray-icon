package de.mkammerer.unisontray

import de.mkammerer.unisontray.tray.Tray
import de.mkammerer.unisontray.tray.TrayImpl
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    Main.start(args)
}

object Main {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun start(args: Array<String>) {
        logger.info("Started")
        try {
            run()
        } catch (e: Exception) {
            logger.error("Unhandled exception occurred", e)
        }
    }

    private fun run() {
        val tray: Tray = TrayImpl()
        tray.init()
        tray.startRefresh()

        Runtime.getRuntime().addShutdownHook(object : Thread("tray-shutdown") {
            override fun run() {
                tray.close()
                logger.info("Stopped")
            }
        })
    }
}