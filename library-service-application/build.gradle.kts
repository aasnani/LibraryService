plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":library-core"))

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.doc)

    runtimeOnly(libs.postgresql)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation (platform(libs.testcontainers.bom))
    testImplementation (libs.spring.boot.starter.test)
    testImplementation (libs.spring.security.test)
    testRuntimeOnly    (libs.junit.platform.launcher)
}