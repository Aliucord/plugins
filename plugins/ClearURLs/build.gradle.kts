import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = """
    Automatically removes tracking elements from URLs you send.

    Uses data from the ClearURLs browser extension.
""".trimIndent()

aliucord {
    author(Authors.Ushie)
}
