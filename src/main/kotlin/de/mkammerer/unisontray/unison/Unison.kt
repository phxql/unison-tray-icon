package de.mkammerer.unisontray.unison

import de.mkammerer.unisontray.util.startAndReadProcess
import org.slf4j.LoggerFactory

data class Profile(val name: String)

data class Result(val output: String, val exitCode: ExitCode)

enum class ExitCode(val num: Int) {
    UNKNOWN(-1), SUCCESSFUL(0), SOME_SKIPPED(1), NON_FATAL(2), FATAL(3);

    companion object {
        fun fromProcess(exitCode: Int): ExitCode = values().firstOrNull { it.num == exitCode } ?: UNKNOWN
    }
}

interface Unison {
    fun run(profile: Profile): Result
}

class UnisonImpl : Unison {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(profile: Profile): Result {
        logger.info("Starting unison with profile {}", profile)

        val builder = ProcessBuilder("unison", profile.name)
        val result = startAndReadProcess(builder)

        return Result(result.output, ExitCode.fromProcess(result.exitCode))
    }
}