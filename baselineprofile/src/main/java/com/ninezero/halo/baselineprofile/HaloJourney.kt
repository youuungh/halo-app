package com.ninezero.halo.baselineprofile

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until

private const val TIMEOUT_MS = 10_000L
private const val NETWORK_TIMEOUT_MS = 20_000L
private const val IDLE_TIMEOUT_MS = 1_000L
private const val POLL_INTERVAL_MS = 200L
private const val EDIT_TEXT = "android.widget.EditText"

private val FEED_TAB = By.desc("피드")
private val LOGIN_BUTTON = By.text("로그인")
private val COMMENT_BUTTON = By.desc("댓글")

/** 플러그인이 넘겨 주는 대상 앱 패키지 */
internal val targetPackage: String
    get() = InstrumentationRegistry.getArguments().getString("targetAppId") ?: "com.ninezero.halo"

private fun argument(name: String): String = InstrumentationRegistry.getArguments().getString(name).orEmpty()

/** 홈에서 시작하는 앱 실행 */
internal fun MacrobenchmarkScope.startApp() {
    pressHome()
    // 알림 권한 창이 화면을 가리지 않게
    device.executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
    startActivityAndWait()
}

/** 필요할 때만 하는 시드 계정 로그인 */
internal fun MacrobenchmarkScope.logInIfNeeded() {
    // 하단 탭이 먼저 뜨면 이미 로그인된 상태
    val shown = waitForAny(FEED_TAB, LOGIN_BUTTON, timeoutMs = NETWORK_TIMEOUT_MS)
    if (shown == FEED_TAB) return
    checkNotNull(shown) { "시작 화면이 뜨지 않음" }

    val email = argument("loginEmail")
    val password = argument("loginPassword")
    check(email.isNotEmpty() && password.isNotEmpty()) { "local.properties에 BASELINE_LOGIN_EMAIL · PASSWORD가 없음" }

    // 시작 화면의 로그인 버튼
    if (!device.hasObject(By.clazz(EDIT_TEXT))) {
        device.findObject(LOGIN_BUTTON).click()
        device.wait(Until.hasObject(By.clazz(EDIT_TEXT)), TIMEOUT_MS)
    }
    // 입력칸 이름이 접근성에 없어 순서로 찾음
    val fields = device.findObjects(By.clazz(EDIT_TEXT))
    fields[0].text = email
    fields[1].text = password
    hideKeyboard()
    device.findObject(LOGIN_BUTTON).click()
    check(device.wait(Until.hasObject(FEED_TAB), NETWORK_TIMEOUT_MS)) { "로그인 뒤 피드가 뜨지 않음" }
}

/** 여러 화면 중 먼저 뜬 쪽 */
private fun MacrobenchmarkScope.waitForAny(vararg selectors: BySelector, timeoutMs: Long): BySelector? {
    val deadline = SystemClock.uptimeMillis() + timeoutMs
    while (SystemClock.uptimeMillis() < deadline) {
        selectors.firstOrNull { device.hasObject(it) }?.let { return it }
        SystemClock.sleep(POLL_INTERVAL_MS)
    }
    return null
}

/** 로그인 버튼을 가리는 키보드 닫기 */
private fun MacrobenchmarkScope.hideKeyboard() {
    if (device.executeShellCommand("dumpsys input_method").contains("mInputShown=true")) device.pressBack()
}

/** 피드 첫 포스트가 그려질 때까지 대기 */
internal fun MacrobenchmarkScope.waitForFeed() {
    device.wait(Until.hasObject(COMMENT_BUTTON), NETWORK_TIMEOUT_MS)
}

/** 피드 스크롤과 포스트 상세 */
internal fun MacrobenchmarkScope.browseFeed() {
    flingList(times = 3)
    // 댓글 버튼이 포스트 상세로 감
    device.findObject(COMMENT_BUTTON)?.let { comment ->
        comment.click()
        device.waitForIdle(IDLE_TIMEOUT_MS)
        device.pressBack()
        device.waitForIdle(IDLE_TIMEOUT_MS)
    }
}

/** 탐색 · 메시지 · 프로필 탭 돌기 */
internal fun MacrobenchmarkScope.visitTabs() {
    // 읽음 처리가 생기지 않게 채팅방엔 안 들어감
    for (tab in listOf("탐색", "메시지", "프로필")) {
        device.findObject(By.desc(tab))?.click() ?: continue
        device.waitForIdle(IDLE_TIMEOUT_MS)
        flingList(times = 2)
    }
    device.findObject(FEED_TAB)?.click()
    device.waitForIdle(IDLE_TIMEOUT_MS)
}

/** 보이는 목록 아래로 넘기기 */
private fun MacrobenchmarkScope.flingList(times: Int) {
    repeat(times) {
        val list = device.findObject(By.scrollable(true)) ?: return
        // 가장자리 제스처가 뒤로 가기로 잡히지 않게
        list.setGestureMargin(device.displayWidth / 5)
        try {
            list.fling(Direction.DOWN)
        } catch (_: StaleObjectException) {
            // 넘기는 사이 목록이 바뀌면 이번만 건너뜀
        }
        device.waitForIdle(IDLE_TIMEOUT_MS)
    }
}
