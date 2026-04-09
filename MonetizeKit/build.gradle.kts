plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("maven-publish")


}

android {
    namespace = "io.monetize.kit.sdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    buildFeatures{
        compose = true
        buildConfig = true
    }
}

group = "com.github.Usman228811"
version = "3.2.9-adapter"


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.Usman228811"
                artifactId = "MonetizeKit"
                version = "3.2.9-adapter"
            }
        }
    }
}

dependencies {


    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")


    implementation("com.google.android.gms:play-services-ads:25.0.0")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")

    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("network.chaintech:sdp-ssp-compose-multiplatform:1.0.7")


    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")


    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-perf")

    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation("com.google.android.play:review-ktx:2.0.2")

    implementation("com.appsflyer:af-android-sdk:6.17.6")
    implementation("com.android.installreferrer:installreferrer:2.2")


    // mediation: meta, inmobi, liftoff(vungle), mintegral, pangle
    implementation("com.google.ads.mediation:inmobi:10.8.8.1")
    implementation("com.google.ads.mediation:vungle:7.4.2.0")
    implementation("com.google.ads.mediation:facebook:6.20.0.2")
    implementation("com.google.ads.mediation:mintegral:16.10.11.0")
    implementation("com.google.ads.mediation:pangle:7.7.0.2.0")
    implementation("net.premiumads.sdk:admob-adapter:2.2.6")

}