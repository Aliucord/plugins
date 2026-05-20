package com.aliucord.gradle.repo

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A dummy Gradle plugin applied to Aliucord plugin buildscripts.
 * ID: `com.aliucord.plugins-repo`
 */
@Suppress("unused")
class RepoPlugin : Plugin<Project> {
    override fun apply(target: Project) {}
}
