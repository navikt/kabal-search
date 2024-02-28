import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mockkVersion = "1.13.8"
val tokenValidationVersion = "3.2.0"
val logstashVersion = "7.4"
val springRetryVersion = "2.0.5"
val springMockkVersion = "4.0.2"
val springDocVersion = "2.3.0"
val testContainersVersion = "1.19.3"
val threeTenExtraVersion = "1.7.2"
val archunitVersion = "1.2.1"
val opensearchVersion = "2.11.1"
val reactorSpringVersion = "1.0.1.RELEASE"
val kodeverkVersion = "1.7.34"
val ehcacheVersion = "3.10.8"
val logbackSyslog4jVersion = "1.0.0"

java.sourceCompatibility = JavaVersion.VERSION_17

ext["elasticsearch.version"] = "7.10.2"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")
}

plugins {
    val kotlinVersion = "1.9.22"
    id("org.springframework.boot") version "3.2.1"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    idea
}

//TODO: Fjern denne når spring boot er oppdatert.
ext["spring-kafka.version"] = "3.0.11"

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
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("javax.cache:cache-api")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    //Not managed by Spring:
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
    implementation("org.springframework.retry:spring-retry:$springRetryVersion")

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
