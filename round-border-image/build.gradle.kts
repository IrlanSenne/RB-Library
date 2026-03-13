import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*

plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.pop.info.roundborder"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        version = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}
dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.pop.info"
            artifactId = "round-border-image"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Round Border Image")
                description.set("- - -")
                url.set("https://github.com/seuusuario/round-border-image")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("lan-sen")
                        name.set("Lan Sen")
                        email.set("moreiradeveloper2016@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/seuusuario/round-border-image.git")
                    developerConnection.set("scm:git:ssh://github.com/seuusuario/round-border-image.git")
                    url.set("https://github.com/seuusuario/round-border-image")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}