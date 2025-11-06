plugins {
  kotlin("jvm") version "2.2.20"
}

group = "com.roughlyunderscore"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.michael-bull.kotlin-itertools:kotlin-itertools:1.0.2")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}