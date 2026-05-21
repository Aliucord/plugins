import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.6"
description = "More confirmations on shit."

aliucord {
    changelog.set("""
        ## 1.0.4
        - Added confirmation for deleting a role
    """.trimIndent())
}