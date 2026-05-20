import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.2"
description = "Forces Discord to remain in normal audio mode, preventing the decrease of audio quality in voice calls."

aliucord {
    author(Authors.oSumAtrIX)

    changelog.set("""
    1.1.2
    ======================

    * Add compatibility mode to settings, if the plugin does not work
  
    1.0.2
    ======================

    * Fix not being able to switch between audio devices

    1.0.1
    ======================

    * Fix some bugs

    1.0.0
    ======================

    Initial release

    ======================
""".trimIndent())
}
