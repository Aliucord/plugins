import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Disable the delete confirmation on messages."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.0.1
		* Dismiss menu after click

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
