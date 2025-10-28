plugins {
    alias(libs.plugins.kotlin.jvm)
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
    implementation(libs.kotlin.stdlib)
    implementation(libs.javapoet)
    
    // Testing
    testImplementation(libs.bundles.testing)
}