plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.doc)
    runtimeOnly(libs.postgresql)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(project(":library-core"))

    testImplementation (platform(libs.testcontainers.bom))
    testImplementation (libs.spring.boot.starter.test)
    testRuntimeOnly    (libs.junit.platform.launcher)
}