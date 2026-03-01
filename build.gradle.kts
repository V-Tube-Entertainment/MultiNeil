import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.19"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

paperweight {
    upstreams.register("neil") {
        repo = github("V-Tube-Entertainment", "Neil")
        ref = providers.gradleProperty("neilCommit")

        patchFile {
            path = "neil-server/build.gradle.kts"
            outputFile = file("multineil-server/build.gradle.kts")
            patchFile = file("multineil-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "neil-api/build.gradle.kts"
            outputFile = file("multineil-api/build.gradle.kts")
            patchFile = file("multineil-api/build.gradle.kts.patch")
        }
        patchRepo("paperApi") {
            upstreamPath = "paper-api"
            patchesDir = file("multineil-api/paper-patches")
            outputDir = file("paper-api")
        }
        patchRepo("pupurApi") {
            upstreamPath = "purpur-api"
            patchesDir = file("multineil-api/purpur-patches")
            outputDir = file("purpur-api")
        }
        patchDir("neilApi") {
            upstreamPath = "neil-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch", "paper-patches", "purpur-patches")
            patchesDir = file("multineil-api/neil-patches")
            outputDir = file("neil-api")

        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://nexus.envarcade.dev/repository/envarcade/")
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.isFork = true
        options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
        options.forkOptions.memoryMaximumSize = "4g"
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven("https://nexus.envarcade.dev/repository/envarcade/") {
                name = "envarcade"
                credentials(PasswordCredentials::class)
            }
        }
    }
}

tasks.register("printMinecraftVersion") {
    doLast {
        println(providers.gradleProperty("mcVersion").get().trim())
    }
}

tasks.register("printMultiNeilVersion") {
    doLast {
        println(project.version)
    }
}
