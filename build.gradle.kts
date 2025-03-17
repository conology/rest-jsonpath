@file:Suppress("UnstableApiUsage")

import cn.lalaki.pub.BaseCentralPortalPlusExtension

plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("cn.lalaki.central") version "1.2.5" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "cn.lalaki.central")

    group = "net.conology"
    version = findProperty("version") ?: "develop"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        mavenCentral()
    }
    tasks.named<Javadoc>("javadoc") {
        isFailOnError = true
    }

    val centralPortalPublishDummyRepo = File(projectDir, "build/maven-central-dummy").toURI()

    configure<BaseCentralPortalPlusExtension> {
        url = centralPortalPublishDummyRepo
        username = findProperty("mavenCentralUsername") as String?
        password = findProperty("mavenCentralPassword") as String?
        publishingType = BaseCentralPortalPlusExtension.PublishingType.AUTOMATIC
    }

    afterEvaluate {
        publishing {
            repositories {
                maven {
                    name = "mavenCentral"
                    url = centralPortalPublishDummyRepo
                }
            }

            publications {
                findByName("lib")?.let {
                    (it as MavenPublication).apply {
                        from(components["java"])

                        pom {
                            url.set("https://github.com/conology/rest-jsonpath")

                            licenses {
                                license {
                                    name.set("The Apache License, Version 2.0")
                                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                }
                            }

                            developers {
                                developer {
                                    id.set("goatfryed")
                                    name.set("Omar Sood")
                                    email.set("omar.sood@holos-supply.de")
                                }
                            }

                            scm {
                                connection.set("scm:git:git://github.com/conology/rest-jsonpath.git")
                                developerConnection.set("scm:git:ssh://github.com/conology/rest-jsonpath.git")
                                url.set("https://github.com/conology/rest-jsonpath")
                            }

                            issueManagement {
                                system.set("GitHub")
                                url.set("https://github.com/conology/rest-jsonpath/issues")
                            }
                        }
                    }
                }
            }
        }
    }

    val signingKey = findProperty("signingPrivateKey") as String?
    val signingPassword = findProperty("signingPassword") as String?
    signing {
        if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        } else {
            logger.lifecycle("Skipping signing because signing properties are not set.")
        }
    }

    testing {
        suites {
            withType<JvmTestSuite> {
                useJUnitJupiter("5.10.0")
            }
        }
    }

    tasks.named("check") {
        dependsOn(testing.suites)
    }
}