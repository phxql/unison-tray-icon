package de.mkammerer.unisontray.util

import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicReference

object ProcessUtil {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun startAndReadProcess(builder: ProcessBuilder): ProcessResult {
        builder.redirectErrorStream(true)
        val output = AtomicReference<String>()
        val exception = AtomicReference<Exception>()

        logger.debug("Starting process")
        val process = builder.start()
        val thread = Thread({
            try {
                logger.debug("Reading process output")
                InputStreamReader(process.inputStream, Charsets.UTF_8).use {
                    output.set(it.readText())
                }
            } catch (e: Exception) {
                exception.set(e)
            }
        }, "process-capture")
        thread.start()

        logger.debug("Waiting for process exit")
        val exitCode = process.waitFor()
        thread.interrupt()
        thread.join()
        logger.debug("Process has exited with {}", exitCode)

        // Rethrow exception if one happened
        exception.get()?.let { throw it }

        return ProcessResult(output.get(), exitCode)
    }

    data class ProcessResult(val output: String, val exitCode: Int)
}