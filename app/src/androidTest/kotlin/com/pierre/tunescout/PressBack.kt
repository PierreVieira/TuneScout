package com.pierre.tunescout

import androidx.activity.ComponentActivity
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Back, sent the way the system sends it to the activity, through its back dispatcher.
 *
 * Espresso's `pressBack` first waits for the app's window to have focus, and the CI emulator's
 * launcher sometimes stops responding: its "isn't responding" dialog holds the focus over the app,
 * which is still resumed and drawn, until the wait gives up. Nothing else in the flows needs the
 * focus, since Compose sends its input to the views directly, so Back doesn't either.
 */
internal fun ComponentActivity.pressBack() {
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        onBackPressedDispatcher.onBackPressed()
    }
}
