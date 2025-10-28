plugins {
    id("com.android.application")
    id("kotlin-android")
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
                    "kapt.kotlin.generated" to "$buildDir/generated/source/kapt/$name"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Deps.kotlin_version}")
    implementation("androidx.core:core-ktx:${Deps.androidx_core}")
    implementation("androidx.appcompat:appcompat:${Deps.androidx_appcompat}")
    implementation("androidx.recyclerview:recyclerview:${Deps.androidx_recyclerview}")
    implementation("com.google.android.material:material:${Deps.material}")
    implementation("androidx.constraintlayout:constraintlayout:${Deps.constraintlayout}")
    
    implementation(project(":bisheng:library"))
    // 注意: sample 项目使用运行时反射，实际项目中应使用：
    // kapt(project(":bisheng:compiler"))
}

