repositories {
    mavenCentral()
}

plugins {
    java
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.dependency.management) apply false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencyLocking {
    lockAllConfigurations()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    dependencyLocking {
        lockAllConfigurations()
    }

    configurations.forEach {
        if (it.isCanBeResolved) {
            it.resolutionStrategy.activateDependencyLocking()
        }
    }

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.register("writeAllLocks") {
    group = "help"
    description = "Triggers dependency resolution and writes lockfiles for all subprojects."

    // Instead of mapping subprojects to tasks, we use a simple loop
    // to depend on the 'dependencies' task of every child.
    subprojects {
        this@register.dependsOn(tasks.named("dependencies"))
    }

    doFirst {
        if (!gradle.startParameter.isWriteDependencyLocks) {
            throw GradleException("Missing flag! Run with: ./gradlew writeAllLocks --write-locks")
        }
        println("Writing lockfiles for all modules...")
    }
}