import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.2"
description = "Prevents Discord from replacing emoji with images, using your system font to display them instead"

aliucord {
    author(Authors.Grzesiek11)

    changelog.set("v1.0.2:\n- Change build URL to GitHub\nv1.0.1:\n- Fix custom emoji reactions displaying as text\nv1.0.0:\n- Initial release")
}
