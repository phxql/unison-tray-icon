package de.mkammerer.unisontray.tray

import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.Separator
import dorkbox.systemTray.SystemTray
import org.slf4j.LoggerFactory
import java.awt.Image
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.imageio.ImageIO
import kotlin.concurrent.withLock

typealias OnQuitHandler = () -> Unit
typealias OnSyncNowHandler = () -> Unit

interface Tray {
    fun init(onQuit: OnQuitHandler, onSyncNow: OnSyncNowHandler)

    fun idle()

    fun startRefresh()

    fun stopRefresh()

    fun error()

    fun close()
}

class TrayImpl(
        private val scheduler: ScheduledExecutorService
) : Tray {
    private val logger = LoggerFactory.getLogger(javaClass)

    private lateinit var systemTray: SystemTray

    private var refreshJob: ScheduledFuture<*>? = null
    private val currentRefreshImage = AtomicInteger(0)

    private val idleImage = loadImage(IDLE_IMAGE)
    private val errorImage = loadImage(ERROR_IMAGE)
    private val refreshImages = Array(REFRESH_IMAGE_MAX - REFRESH_IMAGE_MIN + 1, { num ->
        loadImage(String.format(REFRESH_IMAGES, num + REFRESH_IMAGE_MIN))
    })
    private val trayLock: Lock = ReentrantLock()

    override fun init(onQuit: OnQuitHandler, onSyncNow: OnSyncNowHandler) {
        systemTray = SystemTray.get() ?: throw IllegalStateException("Unable to initialize tray icon")
        systemTray.menu.add(MenuItem("Sync now", { onSyncNow() }).apply { shortcut = 'S' })
        systemTray.menu.add(Separator())
        systemTray.menu.add(MenuItem("Quit", { onQuit() }).apply { shortcut = 'Q' })
    }

    override fun idle() {
        logger.debug("Show idle icon")
        setTrayImage(idleImage)
        setTrayTooltip("Unison - idle")
    }

    override fun startRefresh() {
        if (refreshJob == null) {
            logger.debug("Starting refresh animation")
            setTrayTooltip("Unison - syncing")
            refreshJob = scheduler.scheduleAtFixedRate({
                val imageNumber = nextImageNumber()
                logger.trace("Showing refresh image {}", imageNumber)
                val image = refreshImages[imageNumber - 1]
                setTrayImage(image)
            }, 0, REFRESH_ROTATE_DELAY_MS, TimeUnit.MILLISECONDS)
        }
    }

    override fun stopRefresh() {
        logger.debug("Stopping refresh animation")
        refreshJob?.cancel(false)
        refreshJob = null
        currentRefreshImage.set(0)
    }

    override fun error() {
        logger.debug("Show error icon")
        setTrayTooltip("Unison - error")
        setTrayImage(errorImage)
    }

    override fun close() {
        logger.debug("Shutting down...")
        stopRefresh()
        systemTray.shutdown()
        logger.debug("Shut down")
    }

    private fun loadImage(resource: String): Image = javaClass.getResourceAsStream(resource).use { ImageIO.read(it) }

    private fun nextImageNumber(): Int {
        val image = currentRefreshImage.incrementAndGet()
        if (image > REFRESH_IMAGE_MAX) {
            currentRefreshImage.set(REFRESH_IMAGE_MIN)
            return REFRESH_IMAGE_MIN
        }

        return image
    }

    private fun setTrayTooltip(tooltip: String) {
        trayLock.withLock {
            systemTray.setTooltip(tooltip)
        }
    }

    private fun setTrayImage(image: Image) {
        trayLock.withLock {
            systemTray.setImage(image)
        }
    }

    companion object {
        const val IDLE_IMAGE = "/image/idle.png"
        const val ERROR_IMAGE = "/image/error.png"
        const val REFRESH_IMAGES = "/image/refresh_%d.png"
        const val REFRESH_IMAGE_MIN = 1
        const val REFRESH_IMAGE_MAX = 4
        const val REFRESH_ROTATE_DELAY_MS = 250L
    }
}