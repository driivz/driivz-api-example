plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = "com.driivz.example"
version = "1.0.0"
application {
    mainClass.set("com.driivz.example.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.double.receive)
    implementation(libs.ktor.serialize)
    implementation(libs.jetbrains.kotlinx.serialize)
    implementation(libs.stripe.java)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}

application {
    applicationDefaultJvmArgs = listOf(
        "--add-opens", "java.base/java.net=ALL-UNNAMED"
    )
}