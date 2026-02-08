dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.validation)

    runtimeOnly(libs.postgresql)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation (platform(libs.testcontainers.bom))
    testImplementation (libs.testcontainers.postgresql)
    testImplementation (libs.testcontainers.junit)
    testImplementation (libs.spring.boot.starter.test)
    testRuntimeOnly    (libs.junit.platform.launcher)
}