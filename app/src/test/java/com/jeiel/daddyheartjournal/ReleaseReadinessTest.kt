package com.jeiel.daddyheartjournal

import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessTest {
  @Test
  fun releasePackageName_isProductionNamespace() {
    assertTrue(BuildConfig.APPLICATION_ID.startsWith("com.jeiel.daddyheartjournal"))
  }
}

