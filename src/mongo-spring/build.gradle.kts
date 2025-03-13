@file:Suppress("UnstableApiUsage")

plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.conology"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.data:spring-data-mongodb:4.4.2")
}

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter("5.10.0")
            dependencies {
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

tasks.named("check") {
    dependsOn(testing.suites)
}