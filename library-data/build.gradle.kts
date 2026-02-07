dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}