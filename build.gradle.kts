import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
    application
}

repositories {
    jcenter()
}

version = "1.0.0"
group = "com.edemidov"

dependencies {
    // Kotlin
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Web
    implementation("com.sparkjava:spark-core:2.9.1")

    // DI
    implementation("com.google.inject:guice:4.2.2")

    // Json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.2")

    // DB
    implementation("ru.yandex.qatools.embed:postgresql-embedded:2.10")
    implementation("org.postgresql:postgresql:42.2.10")
    implementation("com.zaxxer:HikariCP:3.4.2")
    implementation("org.jetbrains.exposed:exposed-core:0.21.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.21.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.21.1")

    // Logging
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.0")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("com.github.kittinunf.fuel:fuel:2.2.1")
    testImplementation("com.github.kittinunf.fuel:fuel-jackson:2.2.1")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.15.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClassName = "com.edemidov.fin.AppKt"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}