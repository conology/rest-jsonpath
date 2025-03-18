@file:Suppress("UnstableApiUsage")

import groovy.util.Node

plugins {
    id("antlr")
    id("io.spring.dependency-management") version "1.1.7"
}

group = "net.conology.spring"

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.commons:commons-text:1.13.0")
    implementation("org.springframework.data:spring-data-mongodb:4.4.2")
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("generateGrammarSource")
}


publishing {
    publications {
        create<MavenPublication>("lib") {
            artifactId = "rest-jsonpath-mongodb"

            pom {
                name = "Jsonpath rest filter for spring+mongodb"
                description = "Translate jsonpath rest filter queries to mongodb queries"
            }
        }
    }
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(platform("org.junit:junit-bom:5.10.0"))
                implementation(project())
                implementation("org.junit.jupiter:junit-jupiter")
                implementation("org.assertj:assertj-core:3.27.2")
            }
        }

        val test by getting(JvmTestSuite::class) {}

        val testIntegration by registering(JvmTestSuite::class) {
            dependencies {
                implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.3"))
                implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-starter-web")
                implementation("org.springframework.boot:spring-boot-testcontainers")
                implementation("org.testcontainers:junit-jupiter")
                implementation("org.testcontainers:mongodb")
            }
            targets {
                all {
                    testTask.configure {
                        systemProperty("spring.profiles.active", "test,$name")
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}