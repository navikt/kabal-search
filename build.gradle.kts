import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mockkVersion = "1.13.2"
val tokenValidationVersion = "2.1.4"
val logstashVersion = "7.2"
val springSleuthVersion = "3.1.4"
val unleashVersion = "4.4.1"
val problemSpringWebStartVersion = "0.27.0"
val springRetryVersion = "1.3.3"
val springMockkVersion = "3.1.1"
val springDocVersion = "1.6.11"
val testContainersVersion = "1.17.5"
val threeTenExtraVersion = "1.7.1"
val archunitVersion = "0.23.1"
val opensearchVersion = "2.3.0"
val reactorSpringVersion = "1.0.1.RELEASE"

java.sourceCompatibility = JavaVersion.VERSION_17

ext["elasticsearch.version"] = "7.10.2"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

plugins {
    id("org.springframework.boot") version "2.7.4"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
    idea
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    //Managed by Spring:
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("javax.cache:cache-api")
    implementation("org.ehcache:ehcache")
    implementation("org.jolokia:jolokia-core")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    //Not managed by Spring:
    implementation("org.opensearch.client:opensearch-rest-high-level-client:$opensearchVersion")
    implementation("org.threeten:threeten-extra:$threeTenExtraVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:$springSleuthVersion")
    implementation("org.springdoc:springdoc-openapi-ui:$springDocVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
    implementation("no.nav.security:token-client-spring:$tokenValidationVersion")
    implementation("com.github.navikt:klage-kodeverk:1.0.4") {
        exclude(group = "jakarta.persistence")
    }
    implementation("org.springframework.retry:spring-retry:$springRetryVersion")
    implementation("no.finn.unleash:unleash-client-java:$unleashVersion")
    implementation("org.zalando:problem-spring-web-starter:$problemSpringWebStartVersion")

    //Test
    //Managed by Spring:
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
        exclude(group = "org.mockito")
    }
    testImplementation("org.springframework.kafka:spring-kafka-test")

    //Not managed by Spring:
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:elasticsearch:$testContainersVersion")
    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
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
