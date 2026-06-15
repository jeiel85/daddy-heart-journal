package com.jeiel.daddyheartjournal

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaddyHeartJournalInstrumentedTest {
  @Test
  fun packageName_matchesReleaseApplicationId() {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals("com.jeiel.daddyheartjournal", appContext.packageName)
  }
}

