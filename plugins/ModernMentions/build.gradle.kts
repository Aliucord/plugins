import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.2"
description = "Adds customizable mention styling with optional avatars, colors, spacing and prefix"

aliucord {
    author(Authors.Ushie)

    changelog.set(
        """
    # 1.1.2
    * Fix invisible mentions when role color is enabled and the user has no role color
    # 1.1.1
    * Fix mention color when "Use role color" is disabled
    # 1.1.0
    * Add support for custom mention prefix (e.g. @)
""".trimIndent(),
    )
}
