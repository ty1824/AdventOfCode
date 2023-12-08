plugins {
    idea
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

application {
    mainClass.set("advent.AdventRunnerKt")
}
