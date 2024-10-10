plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.rlrio"
version = "1.0-SNAPSHOT"

val apachePoiVersion = "5.2.3"
val slf4jVersion = "2.0.16"
val logbackVersion = "1.5.7"
val kotlinReflectVersion = "2.0.20"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi:$apachePoiVersion")
    implementation("org.apache.poi:poi-ooxml:$apachePoiVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinReflectVersion")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}