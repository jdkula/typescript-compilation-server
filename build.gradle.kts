import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.10"
    application
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

group = "pw.jonak"
version = "1.0-SNAPSHOT"

val ktor_version = "1.0.0"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        setUrl("https://dl.bintray.com/jdkula/subprocess")
    }
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("com.beust:klaxon:3.0.1")
    compile("ch.qos.logback:logback-classic:0.9.24")
    compile("pw.jonak:subprocess:1.5-FINAL")
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}