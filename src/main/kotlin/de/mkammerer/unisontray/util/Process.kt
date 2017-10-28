package de.mkammerer.unisontray.util

import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicReference

fun startAndReadProcess(builder: ProcessBuilder): ProcessResult {
    builder.redirectErrorStream(true)
    val output = AtomicReference<String>()
    val exception = AtomicReference<Exception>()

    val process = builder.start()
    val thread = Thread({
        try {
            InputStreamReader(process.inputStream, Charsets.UTF_8).use {
                output.set(it.readText())
            }
        } catch (e: Exception) {
            exception.set(e)
        }
    }, "process-capture")
    thread.start()

    val exitCode = process.waitFor()
    thread.interrupt()
    thread.join()

    // Rethrow exception if one happened
    exception.get()?.let { throw it }

    return ProcessResult(output.get(), exitCode)
}

data class ProcessResult(val output: String, val exitCode: Int)