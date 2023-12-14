plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.56"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    kotlin("plugin.serialization") version "1.8.22"
}

taboolib {
    install("common")
    install("common-5")
    install("module-chat")
    install("module-configuration")
    install("module-lang")
    install("module-kether")
    install("module-nms")
    install("module-nms-util")
    install("module-ui")
    install("platform-bukkit")
    install("expansion-command-helper")
    relocate("ink.ptms.um","top.maplex.fomalhautshop.um")
    classifier = null
    version = "6.0.12-40"
}

repositories {
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://nexus.phoenixdevt.fr/repository/maven-public/") }
    maven { url = uri("https://jitpack.io") }
    mavenLocal()
    mavenCentral()
}

dependencies {
    taboo("ink.ptms:um:1.0.0-beta-33")
    taboo("net.mamoe.yamlkt:yamlkt:0.13.0")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11902:11902-minimize:mapped")
    compileOnly("ink.ptms.core:v11902:11902-minimize:universal")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1")
    implementation("ink.ptms:Zaphkiel:2.0.14")

    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
