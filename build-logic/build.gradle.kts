plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.aliucord.gradle)
}

gradlePlugin {
    plugins {
        create("aliucord-plugins-repo") {
            id = "com.aliucord.plugins-repo"
            implementationClass = "com.aliucord.gradle.repo.RepoPlugin"
        }
    }
}
