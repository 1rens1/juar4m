import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date

val appName = "JUAR4M"

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
}

// Safely fetch commit count (defaults to 1 if git fails)
val commitCount = providers.exec {
	commandLine("git", "rev-list", "--count", "HEAD")
}.standardOutput.asText.map { it.trim().toIntOrNull() ?: 1 }

// Safely fetch short commit hash (defaults to "dev" if git fails)
val commitHash = providers.exec {
	commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.map { it.trim().ifEmpty { "dev" } }

android {
	namespace = "com.rensdev.juar4m"
	compileSdk = 36

	defaultConfig {
		applicationId = "com.rensdev.juar4m"
		minSdk = 29
		targetSdk = 36
		versionCode = commitCount.get()
		versionName = System.getenv("VERSION_NAME")?.removePrefix("v") ?: "1.6.3-${commitHash.get()}"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
	}

	signingConfigs {
		create("release") {
			storeFile = file(System.getenv("KEYSTORE_FILE") ?: "$appName.local.jks")
			storePassword = System.getenv("KEYSTORE_PASSWORD")
			keyAlias = System.getenv("KEY_ALIAS")
			keyPassword = System.getenv("KEY_PASSWORD")
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			signingConfig = signingConfigs.getByName("release")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}
}

// Clean APK renaming using official AGP public APIs (No internal class imports)
androidComponents {
	onVariants { variant ->
		val date = SimpleDateFormat("yyyyMMdd").format(Date())
		variant.outputs.forEach { output ->
			val version = output.versionName.get()
			output.outputFileName.set("$appName-${variant.name}_${date}_v${version}.apk")
		}
	}
}

kotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_11)
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.material.icons.extended)
	implementation(libs.androidx.compose.ui.text.google.fonts)
	implementation(libs.androidx.datastore.preferences)
	implementation(libs.androidx.material3)

	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
}
