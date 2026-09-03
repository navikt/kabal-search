import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val ktlintVersion = "1.8.0"
val mockkVersion = "1.14.11"
val tokenValidationVersion = "6.0.12"
val logstashVersion = "9.0"
val springMockkVersion = "5.0.1"
val springDocVersion = "3.1.0"
val testContainersVersion = "2.0.5"
val threeTenExtraVersion = "1.10.0"
val archunitVersion = "1.5.0"
val opensearchVersion = "3.8.0"
val reactorSpringVersion = "1.0.1.RELEASE"
val kodeverkVersion = "3.3.23"
val ehcacheVersion = "3.12.0"
val logbackSyslog4jVersion = "1.0.0"
val otelVersion = "1.65.0"

java.sourceCompatibility = JavaVersion.VERSION_21

plugins {
    val kotlinVersion = "2.4.10"
    id("org.springframework.boot") version "4.1.1"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("dev.detekt") version "2.0.0-alpha.6"
    idea
}

// Temporary override: tomcat-embed-core 11.0.24 from the Spring Boot BOM has CVE-2026-65905.
// Remove when Spring Boot ships 11.0.25 or newer.
extra["tomcat.version"] = "11.0.25"

apply(plugin = "io.spring.dependency-management")

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Managed by Spring:
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("javax.cache:cache-api")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Not managed by Spring:
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.papertrailapp:logback-syslog4j:$logbackSyslog4jVersion")
    implementation("org.ehcache:ehcache:$ehcacheVersion")
    implementation("org.opensearch.client:opensearch-rest-high-level-client:$opensearchVersion")
    implementation("org.threeten:threeten-extra:$threeTenExtraVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
    implementation("no.nav.security:token-client-spring:$tokenValidationVersion")
    implementation("no.nav.klage:klage-kodeverk:$kodeverkVersion") {
        exclude(group = "jakarta.persistence")
    }
    implementation("io.opentelemetry:opentelemetry-api:$otelVersion")

    // Test
    // Managed by Spring:
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
        exclude(group = "org.mockito")
    }
    testImplementation("org.springframework.kafka:spring-kafka-test")

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-elasticsearch")
    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

ktlint {
    version.set(ktlintVersion)
    ignoreFailures.set(false)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
    filter {
        exclude { it.file.path.contains("${File.separator}build${File.separator}") }
    }
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig.set(true)
    ignoreFailures.set(false)
}

// NamedArguments implements RequiresAnalysisApi, so it only reports when detekt
// runs with a compile classpath. The plain `detekt` task has no classpath and
// would silently pass, hence the analysis aware tasks are wired into `check`
// and the plain one is disabled.
tasks.named("detekt") {
    enabled = false
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    jvmTarget.set(JvmTarget.JVM_21.target)
    reports {
        html.required.set(true)
        checkstyle.required.set(true)
        sarif.required.set(false)
        markdown.required.set(false)
    }
}

tasks.named("check") {
    dependsOn("detektMain", "detektTest")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}
