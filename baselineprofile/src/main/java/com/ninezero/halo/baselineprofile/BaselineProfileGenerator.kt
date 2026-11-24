package com.ninezero.halo.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Baseline · Startup Profile 생성기 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    /** 시작 프로필에도 넣는 앱 시작 구간 */
    @Test
    fun startup() = rule.collect(packageName = targetPackage, includeInStartupProfile = true) {
        startApp()
        logInIfNeeded()
        waitForFeed()
    }

    /** 자주 지나는 화면 */
    @Test
    fun navigation() = rule.collect(packageName = targetPackage) {
        startApp()
        logInIfNeeded()
        waitForFeed()
        browseFeed()
        visitTabs()
    }
}
