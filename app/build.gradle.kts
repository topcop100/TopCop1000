plugins { id("com.android.application") }

android {
    namespace = "com.topcop1000.standalone"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.topcop1000.standalone"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes { release { isMinifyEnabled = false } }
}

dependencies { }
