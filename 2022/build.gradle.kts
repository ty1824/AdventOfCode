plugins {
    idea
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}
dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
}