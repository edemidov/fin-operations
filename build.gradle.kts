import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.sparkjava:spark-core:2.9.1")
    implementation("com.google.inject:guice:4.2.2")
    implementation("com.google.code.gson:gson:2.8.6")

    // DB
    implementation("com.h2database:h2:1.4.200")
    implementation("com.zaxxer:HikariCP:3.4.2")
    implementation("org.jetbrains.exposed:exposed:0.17.7")

    // Logging
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
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