plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":bisheng:annotation"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Deps.kotlin_version}")
    implementation("com.squareup:javapoet:1.13.0")
}