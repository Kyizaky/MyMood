// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
    id ("com.android.library") version "8.0.2" apply false
}
buildscript {

    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.6.0")
    }
}
