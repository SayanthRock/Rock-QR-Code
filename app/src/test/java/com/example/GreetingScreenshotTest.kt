package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.QRViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GreetingScreenshotTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun mainContent_rendersScanScreen() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = QRViewModel(application)

    composeTestRule.setContent {
      MyApplicationTheme {
        MainAppContent(viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithTag("scan_screen_root").assertIsDisplayed()
  }
}
