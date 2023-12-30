/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.5/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit test framework.
    testImplementation(libs.junit)

    // This dependency is used by the application.
    implementation(libs.guava)

    // com.fasterxml.jackson.databind.ObjectMapper;
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")

    // mysql-connector-java
    implementation("com.mysql:mysql-connector-j:8.2.0")

    // aws sdk
    implementation("com.amazonaws:aws-java-sdk-sqs:1.12.625")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


// ex : ./gradlew clean build -Dprofile=test then systemProfile = test
val systemProfile = System.getProperty("profile")
if (systemProfile != null) {
    println("Using profile from system property: $systemProfile")
    project.extra["profile"] = systemProfile
} else {
    println("No profile specified, using the default profile. defauylt profile is 'dev' ")
    project.extra["profile"] = "dev"
}
tasks.processResources {
    filesMatching("application.properties") {
        filter { line ->
            line.replace(Regex("profile=(\\w+)"), "profile=${project.extra["profile"]}")
        }
    }
}
//빌드시 주석의 인코딩 에러를 방지하기 위해 추가
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "job.user.puuid.App"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

application {
    // Define the main class for the application.
    mainClass.set("job.user.puuid.App")
}