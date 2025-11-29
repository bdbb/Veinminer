package de.miraculixx.veinminerClient

import de.miraculixx.veinminerClient.config.ClientConfigManager
import de.miraculixx.veinminerClient.constants.KEY_VEINMINE
import de.miraculixx.veinminerClient.network.NetworkManager
import de.miraculixx.veinminerClient.render.BlockHighlightingRenderer
import de.miraculixx.veinminerClient.render.HUDRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object KeyBindManager {
    var lastTarget: BlockPos? = null
        private set
    var isPressed = false
        private set(value) {
            NetworkManager.sendKeyPress(value)
            field = value
        }
    var notifiedOnce = false

    // For toggle mode: tracks the toggled state
    private var toggledOn = false
    // For toggle mode: tracks if key was down last tick to detect press edge
    private var wasKeyDown = false

    fun tick() {
        val isToggleMode = ClientConfigManager.settings.toggleMode
        val keyDown = KEY_VEINMINE.isDown

        // Handle toggle mode
        if (isToggleMode) {
            // Detect key press edge (was not down, now is down)
            if (keyDown && !wasKeyDown) {
                toggledOn = !toggledOn
            }
            wasKeyDown = keyDown

            if (toggledOn) {
                // Notify user if not active
                if (!NetworkManager.isVeinminerActive) {
                    if (!notifiedOnce) {
                        notifiedOnce = true
                        val mc = Minecraft.getInstance()
                        mc.toastManager.addToast(
                            SystemToast.multiline(mc, SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                Component.translatable("veinminer.disabled.title"),
                                Component.translatable("veinminer.disabled.subtitle")
                            )
                        )
                    }
                    return
                }

                if (!isPressed) isPressed = true
                checkBlockTarget()
                scrollPattern()
            } else {
                if (isPressed) isPressed = false
                HUDRenderer.updateTarget(null)
                BlockHighlightingRenderer.setShape(emptyList())
                lastTarget = null
            }
        } else {
            // Original hold mode behavior
            if (keyDown) {
                // Notify user if not active
                if (!NetworkManager.isVeinminerActive) {
                    if (!notifiedOnce) {
                        notifiedOnce = true
                        val mc = Minecraft.getInstance()
                        mc.toastManager.addToast(
                            SystemToast.multiline(mc, SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                Component.translatable("veinminer.disabled.title"),
                                Component.translatable("veinminer.disabled.subtitle")
                            )
                        )
                    }
                    return
                }

                if (!isPressed) isPressed = true
                checkBlockTarget()
                scrollPattern()

            } else {
                if (isPressed) isPressed = false
                HUDRenderer.updateTarget(null)
                BlockHighlightingRenderer.setShape(emptyList())
                lastTarget = null
            }
        }
    }

    // Check current block target & update if necessary
    fun checkBlockTarget() {
        val instance = Minecraft.getInstance()
        val target = instance.hitResult as? BlockHitResult ?: return
        val pos = target.blockPos
        if (pos == lastTarget) return
        lastTarget = pos
        BlockHighlightingRenderer.setShape(emptyList())

        // If not targeting block, fail
        if (target.type != HitResult.Type.BLOCK) {
            HUDRenderer.updateTarget("forbidden")
            return
        }

        // Request vein for block highlighting and hud
        NetworkManager.sendBlockRequest(pos, target.direction)
    }

    // Scroll through veinmine patterns
    fun scrollPattern() {
        // TODO
    }

    fun onDisconnect() {
        notifiedOnce = false
        toggledOn = false
        wasKeyDown = false
    }
}