package com.example.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = APP_PACKAGE
    ) {
        startActivityAndWait()
        device.waitForIdle()

        // 0) WelcomeNameScreen -> 1) ProfileSetupScreen
        // Если welcome показан — пытаемся ввести имя, иначе просто жмём Continue если доступно.
        maybeEnterNameAndContinue()

        // 1) ProfileSetupScreen -> 2) Home
        requireClick(desc = "bp_continue", text = "Continue")
        device.waitForIdle()

        // 2) Home -> 3) Calendar -> back
        requireClick(desc = "bp_tile_calendar", text = "Calendar")
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()

        // 2) Home -> 4) Profile (через нижний таб или верхнюю кнопку)
        if (!clickIfExists(desc = "bp_tab_profile", text = "Profile")) {
            // fallback: верхняя иконка профиля
            clickIfExists(desc = "bp_top_profile", text = "Profile")
        }
        device.waitForIdle()

        // 4) Profile -> 5) Personal Details -> back
        requireClick(desc = "bp_personal_details", text = "Personal details")
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()

        // 4) Profile -> 6) Settings -> back
        requireClick(desc = "bp_settings", text = "Settings")
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()

        // 4) Profile -> 9) Tips -> back
        requireClick(desc = "bp_tips", text = "Tips")
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()

        // 4) Profile -> 7) FAQ -> back
        requireClick(desc = "bp_faq", text = "FAQ")
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()

        // 4) Profile -> 8) Contact -> back
        requireClick(desc = "bp_contact", text = "Contact")
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()

        // (опционально) Home -> 10) Run/Tracking -> back (если экран реально существует)
        device.pressBack() // вернуться на Home, если мы всё ещё на Profile
        device.waitForIdle()
        if (clickIfExists(desc = "bp_run", text = "Run")) {
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()
        }
    }
}

private const val APP_PACKAGE = "com.example.step_flow"
private const val TIMEOUT = 6_000L

private fun MacrobenchmarkScope.maybeEnterNameAndContinue() {
    // Пытаемся определить Welcome по наличию Continue
    if (!device.wait(Until.hasObject(By.text("Continue")), 1_500L) &&
        !device.wait(Until.hasObject(By.desc("bp_continue")), 1_500L)
    ) {
        return
    }

    // Пытаемся ввести имя (если поле доступно). Если имя уже сохранено — Continue может быть активна и без ввода.
    // Сначала ищем поле по подсказке/тексту, потом — первый "editable".
    val nameValue = "Egor"

    val byHint = device.findObject(By.textContains("Name"))
        ?: device.findObject(By.textContains("Your"))
        ?: device.findObject(By.textContains("Имя"))

    val editable = byHint ?: device.findObject(By.clazz("android.widget.EditText"))

    editable?.let {
        safeSetText(it, nameValue)
        device.waitForIdle()
    }

    // Continue
    requireClick(desc = "bp_continue", text = "Continue")
    device.waitForIdle()
}

private fun MacrobenchmarkScope.safeSetText(obj: UiObject2, value: String) {
    try {
        obj.click()
        device.waitForIdle()
        obj.text = value
    } catch (_: Throwable) {
        // если BasicTextField не даёт setText через UiAutomator — не валим генерацию,
        // Continue может быть уже активной из сохранённого имени
    }
}

private fun MacrobenchmarkScope.requireClick(desc: String, text: String) {
    if (clickIfExists(desc, text)) return
    throw AssertionError("UI element not found: desc='$desc' or text='$text'")
}

private fun MacrobenchmarkScope.clickIfExists(desc: String, text: String): Boolean {
    // 1) stable: contentDescription
    if (device.wait(Until.hasObject(By.desc(desc)), TIMEOUT)) {
        device.findObject(By.desc(desc))?.click()
        return true
    }
    // 2) fallback: text
    if (device.wait(Until.hasObject(By.text(text)), TIMEOUT)) {
        device.findObject(By.text(text))?.click()
        return true
    }
    // 3) fallback: contains text
    if (device.wait(Until.hasObject(By.textContains(text)), 2_000L)) {
        device.findObject(By.textContains(text))?.click()
        return true
    }
    return false
}
