import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

import com.android.build.gradle.LibraryExtension 

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.7"
description = "(A.S.S) Style Aliucord to your liking!"

aliucord {
    changelog = """
        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}

configure<LibraryExtension> {
    defaultConfig {
        minSdk = 26
    }
}