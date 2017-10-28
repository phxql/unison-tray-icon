package de.mkammerer.unisontray

import de.mkammerer.unisontray.tray.Tray
import de.mkammerer.unisontray.tray.TrayImpl
import de.mkammerer.unisontray.unison.Profile
import de.mkammerer.unisontray.unison.Unison
import de.mkammerer.unisontray.unison.UnisonImpl
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
        val unison: Unison = UnisonImpl()

        tray.init()
        tray.startRefresh()

        val result = unison.run(Profile("moe"))
        logger.debug("{}", result)

        Runtime.getRuntime().addShutdownHook(object : Thread("tray-shutdown") {
            override fun run() {
                tray.close()
                logger.info("Stopped")
            }
        })
    }
}