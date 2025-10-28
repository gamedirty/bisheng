plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.sovnem.bisheng"
    compileSdk = Deps.compileSdk
    buildToolsVersion = Deps.buildTools

    defaultConfig {
        minSdk = Deps.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.androidx)
    implementation(libs.material)
    
    api(project(":bisheng:annotation"))
    lintChecks(project(":bisheng:lint"))
    
    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.robolectric)
}