import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Forces Android notifications when the client is open"

aliucord {
    author(Authors.Grzesiek11)

    changelog.set("v1.0.0\n- Initial release")
}
