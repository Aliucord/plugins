import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

description = "Allows you to see hidden channels in servers."
version = "1.3.0"

aliucord {
    author(Authors.Juby210)

    changelog.set("""
       Added {added marginTop} 
       ======================
       
       * added voice channels
       
       Improved {improved marginTop}
       ======================
       
       * improved hidden channels appearance
       * refactored code
    """.trimIndent())
}
