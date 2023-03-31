import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("org.owasp.dependencycheck") version "8.1.0"
    kotlin("jvm") version "1.6.21"
}

var springVersion = "2.6.6"

val extremumVersion = "3.0.0"
val artifactVersion = extremumVersion
val extremumGroup = "io.extremum"
val artifactUrl = "github.com/smekalka/extremum-signaler-java"

group = extremumGroup
version = artifactVersion
java.sourceCompatibility = JavaVersion.VERSION_1_8

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "java")

    repositories {
        mavenCentral()
        mavenLocal()
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    configure<PublishingExtension> {
        if (project.path == ":") {
            // Do not publish "root" project
            return@configure
        }
        // Ignore projects without "artifact" property for publishing
        val projectArtifact = project.findProperty("artifact")?.let { it as String }
            ?: return@configure
        publications {
            create<MavenPublication>(project.name) {
                artifactId = projectArtifact
                version = rootProject.version.toString()
                description = project.description
                from(components["java"])

                pom {
                    project.property("artifact.name")?.let { name.set(it as String) }
                    description.set(project.description)

                    url.set("https://$artifactUrl")
                    inceptionYear.set("2022")

                    scm {
                        url.set("https://$artifactUrl")
                        connection.set("scm:https://$artifactUrl.git")
                        developerConnection.set("scm:git://$artifactUrl.git")
                    }

                    licenses {
                        license {
                            name.set("Business Source License 1.1")
                            url.set("https://$artifactUrl/blob/develop/LICENSE.md")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("urgimchak")
                            name.set("Maksim Tyutyaev")
                            email.set("maksim.tyutyaev@smekalka.com")
                        }
                    }
                }
            }

            repositories {
                maven {
                    name = "OSSRH"
                    val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    val isReleaseVersion = !(version as String).endsWith("-SNAPSHOT")
                    url = uri(if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl)
                    credentials {
                        username = System.getProperty("ossrhUsername")
                        password = System.getProperty("ossrhPassword")
                    }
                }
            }
        }
    }

    configure<SigningExtension> {
        val publishing: PublishingExtension by project
        sign(publishing.publications)
    }

    tasks.javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }
}

subprojects {
    version = artifactVersion
    group = extremumGroup

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")

    dependencies {
        implementation("io.extremum:extremum-shared-models:$extremumVersion")
        implementation("com.squareup.okhttp:okhttp:2.7.5")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.named<Jar>("jar") {
        enabled = true
    }
}

project(":signaler") {
    dependencies {
        implementation("org.springframework.boot:spring-boot:$springVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
    }
}

tasks.wrapper {
    gradleVersion = "7.5.1"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.named<Jar>("jar") {
    enabled = false
}