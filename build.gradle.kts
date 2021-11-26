plugins {
    `java-library`
    jacoco
    id("org.springframework.boot") version "2.5.6"
    id("org.barfuin.gradle.jacocolog") version "2.0.0"
    id("info.solidsoft.pitest") version "1.7.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

springBoot {
    mainClass.set("com.github.jorgebsa.spring.demo.Application")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    val testcontainersVersion: String by project
    val springdocOpenAPIVersion: String by project
    val keycloakVersion: String by project
    val testcontainersKeycloakVersion: String by project

    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation(platform("org.keycloak.bom:keycloak-adapter-bom:$keycloakVersion"))
    implementation("org.keycloak:keycloak-spring-boot-starter")

    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
    implementation("org.springdoc:springdoc-openapi-ui:$springdocOpenAPIVersion")
    implementation("org.springdoc:springdoc-openapi-data-rest:$springdocOpenAPIVersion")

    testImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.keycloak:keycloak-admin-client:${keycloakVersion}")
    testImplementation("com.github.dasniko:testcontainers-keycloak:$testcontainersKeycloakVersion")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.99".toBigDecimal()
            }
        }
    }
}

pitest {
    junit5PluginVersion.set("0.15")
    timestampedReports.set(false)
}