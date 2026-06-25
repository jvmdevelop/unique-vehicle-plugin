plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.4.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
}

val protocolLibRuntime by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    protocolLibRuntime("net.dmulloy2:ProtocolLib:5.4.0")
    protocolLibRuntime("net.bytebuddy:byte-buddy:1.17.5")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

val protocolLibFatJar = tasks.register<Jar>("protocolLibFatJar") {
    archiveBaseName.set("ProtocolLib-runtime")
    destinationDirectory.set(layout.buildDirectory.dir("protocol-lib"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        protocolLibRuntime.resolve().map { zipTree(it) }
    })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/INDEX.LIST")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runServer {
        dependsOn(shadowJar)
        dependsOn(protocolLibFatJar)
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.4")
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        )
        jvmArgs("-Xms2G", "-Xmx2G")
        pluginJars(protocolLibFatJar.flatMap { it.archiveFile })
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
