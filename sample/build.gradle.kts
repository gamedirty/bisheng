plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.sovnem.bisheng.sample"
    compileSdk = Deps.compileSdk
    buildToolsVersion = Deps.buildTools

    defaultConfig {
        applicationId = "com.sovnem.bisheng.sample"
        minSdk = Deps.minSdk
        targetSdk = Deps.targetSdk
        versionCode = 1
        versionName = "1.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // KAPT 配置
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "kapt.kotlin.generated" to "${layout.buildDirectory.get()}/generated/source/kapt/$name"
                )
            }
        }
    }
    
    // 配置 KAPT 以处理 R 类问题
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xallow-unstable-dependencies"
        )
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
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.androidx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    
    implementation(project(":bisheng:library"))
    kapt(project(":bisheng:compiler"))
    
    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

