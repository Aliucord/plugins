import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.2"
description = "Hide timestamps on messages."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.1.2
		* Fix NPE

		# 1.1.1
		* Undo message divider removal

		# 1.1.0
		* Hide message divider timestamps

		# 1.0.1
		* Fix changelog

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
