import io.izzel.taboolib.gradle.*

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "2.0.12"
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
}

taboolib {
    description {
        contributors {
            name("枫溪")
        }
    }
    env {
        install(UNIVERSAL, BUKKIT_ALL, NMS_UTIL, KETHER, UI)
    }
    version {
        taboolib = "6.1.1"
    }
    relocate("ink.ptms.um", "top.maplex.fomalhautshop.libs.um")
    relocate("net.mamoe.yamlkt", "top.maplex.fomalhautshop.libs.yamlkt")
}

repositories {
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://nexus.phoenixdevt.fr/repository/maven-public/") }
    maven { url = uri("https://jitpack.io") }
    mavenLocal()
    mavenCentral()
}

dependencies {
    taboo("ink.ptms:um:1.0.2")
    taboo("net.mamoe.yamlkt:yamlkt:0.13.0"){
        isTransitive = false
    }
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11902:11902-minimize:mapped")
    compileOnly("ink.ptms.core:v11902:11902-minimize:universal")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1")
    implementation("ink.ptms:Zaphkiel:2.0.14")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")

    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
