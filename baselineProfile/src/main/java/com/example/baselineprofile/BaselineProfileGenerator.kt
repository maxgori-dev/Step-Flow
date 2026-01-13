package com.example.baselineprofile

import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val APP_PACKAGE = "com.example.step_flow"
private const val MAIN_ACTIVITY = "com.example.step_flow.MainActivity"

private const val TIMEOUT = 8_000L
private const val SHORT = 1_500L

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = APP_PACKAGE) {
        prepareAndLaunch()

        // 0 -> 1 (Welcome): имя -> Continue (ждём enabled)
        enterNameThenContinue()
        device.waitForIdle()

        // 1 -> 2 (Profile setup -> Home)
        requireClick(desc = "bp_continue", text = "Continue")
        device.waitForIdle()

        // 2 -> 3 -> back (Calendar)
        requireClick(desc = "bp_tile_calendar", text = "Calendar")
        pressBackStable()

        // 2 -> 4 (Profile)
        if (!clickIfExists(desc = "bp_tab_profile", text = "Profile")) {
            clickIfExists(desc = "bp_top_profile", text = "Profile")
        }
        device.waitForIdle()

        // 4 -> 5 -> back (Personal Details)
        requireClick(desc = "bp_personal_details", text = "Personal Details")
        pressBackStable()

        // 4 -> 6 -> back (Settings)
        requireClick(desc = "bp_settings", text = "Settings")
        pressBackStable()

        // 4 -> 9 -> back (Tips)
        requireClick(desc = "bp_tips", text = "Tips and Tricks")
        pressBackStable()

        // 4 -> 7 -> back (FAQ)
        requireClick(desc = "bp_faq", text = "FAQ")
        pressBackStable()

        // 4 -> 8 -> back (Contact)
        requireClick(desc = "bp_contact", text = "Contact Us")
        pressBackStable()

        // вернуться на Home (если ещё на Profile)
        pressBackStable()

        // 2 -> Permission -> Tracking
        if (clickIfExists(desc = "bp_run", text = "RUN")) {

            // PermissionScreen inside app
            if (!clickIfExists(desc = "bp_permission_allow", text = "Grant Access")) {
                clickAndroidPermissionDialogAllowIfPresent()
            }

            device.waitForIdle()

            // Tracking: stop
            clickIfExists(desc = "bp_tracking_finish", text = "STOP RUN")
            device.waitForIdle()

            pressBackStable()
        }
    }
}


private fun MacrobenchmarkScope.prepareAndLaunch() {
    device.wakeUp()
    device.pressHome()
    device.waitForIdle()

    device.executeShellCommand("am force-stop $APP_PACKAGE")

    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        setClassName(APP_PACKAGE, MAIN_ACTIVITY)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    try {
        startActivityAndWait(intent)
        device.waitForIdle()
        return
    } catch (_: Throwable) {
        device.pressHome()
        device.waitForIdle()
        device.executeShellCommand("am force-stop $APP_PACKAGE")
        startActivityAndWait(intent)
        device.waitForIdle()
    }
}

private fun MacrobenchmarkScope.pressBackStable() {
    device.pressBack()
    device.waitForIdle()
}


private fun MacrobenchmarkScope.enterNameThenContinue() {
    val onWelcome =
        device.wait(Until.hasObject(By.desc("bp_name_input")), TIMEOUT) ||
                device.wait(Until.hasObject(By.desc("bp_continue")), TIMEOUT) ||
                device.wait(Until.hasObject(By.text("Continue")), TIMEOUT)

    if (!onWelcome) return

    val nameValue = "Egor"

    val input =
        device.findObject(By.desc("bp_name_input"))
            ?: device.findObject(By.clazz("android.widget.EditText"))

    if (input != null) {
        safeSetText(input, nameValue)
    } else {
        // крайний fallback
        device.clickCenterIfPossible(By.desc("bp_continue"))
        device.executeShellCommand("input text $nameValue")
        device.waitForIdle()
    }

    // ждём enabled у кнопки (иначе она disabled из-за пустого name)
    waitUntilEnabled(By.desc("bp_continue"), TIMEOUT)

    requireClick(desc = "bp_continue", text = "Continue")
    device.waitForIdle()
}

private fun MacrobenchmarkScope.safeSetText(obj: UiObject2, value: String) {
    try {
        obj.click()
        device.waitForIdle()

        // часто работает с Compose accessibility node
        obj.text = value
        device.waitForIdle()

        // fallback если текст не применился
        if (obj.text.isNullOrBlank() || obj.text != value) {
            device.executeShellCommand("input text $value")
            device.waitForIdle()
        }
    } catch (_: Throwable) {
        try {
            device.executeShellCommand("input text $value")
            device.waitForIdle()
        } catch (_: Throwable) {
            // не валим прогон
        }
    }
}

private fun MacrobenchmarkScope.waitUntilEnabled(selector: BySelector, timeoutMs: Long) {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < timeoutMs) {
        val obj = device.findObject(selector)
        if (obj != null && obj.isEnabled) return
        device.waitForIdle()
        Thread.sleep(120)
    }
}

private fun UiDevice.clickCenterIfPossible(selector: BySelector) {
    try {
        findObject(selector)?.click()
        waitForIdle()
    } catch (_: Throwable) {}
}

private fun MacrobenchmarkScope.requireClick(desc: String, text: String) {
    if (clickIfExists(desc, text)) return
    throw AssertionError("UI element not found: desc='$desc' or text='$text'")
}

private fun MacrobenchmarkScope.clickIfExists(desc: String, text: String): Boolean {
    if (device.wait(Until.hasObject(By.desc(desc)), TIMEOUT)) {
        device.findObject(By.desc(desc))?.click()
        device.waitForIdle()
        return true
    }

    if (text.isNotBlank() && device.wait(Until.hasObject(By.text(text)), SHORT)) {
        device.findObject(By.text(text))?.click()
        device.waitForIdle()
        return true
    }

    if (text.isNotBlank() && device.wait(Until.hasObject(By.textContains(text)), SHORT)) {
        device.findObject(By.textContains(text))?.click()
        device.waitForIdle()
        return true
    }

    return false
}

private fun MacrobenchmarkScope.clickAndroidPermissionDialogAllowIfPresent() {
    val selectors = listOf(
        By.res("com.android.permissioncontroller", "permission_allow_button"),
        By.res("com.android.permissioncontroller", "permission_allow_foreground_only_button"),
        By.res("com.android.packageinstaller", "permission_allow_button"),
        By.textContains("Allow"),
        By.textContains("While using"),
        By.textContains("Разрешить"),
        By.textContains("Только во время"),
        By.textContains("Zezwól"),
        By.textContains("Podczas używania")
    )

    for (s in selectors) {
        if (device.wait(Until.hasObject(s), SHORT)) {
            device.findObject(s)?.click()
            device.waitForIdle()
            return
        }
    }
}
