import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Bring the /nick command to mobile."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.0.1
		* Fix changelog

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
