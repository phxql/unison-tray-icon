package de.mkammerer.unisontray

import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    Main.start(args)
}

object Main {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun start(args: Array<String>) {
        logger.info("Started")
        try {
            println("Hello world")
        } catch (e: Exception) {
            logger.error("Unhandled exception occurred", e)
        } finally {
            logger.info("Stopped")
        }
    }
}