import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mockkVersion = "1.12.1"
val h2Version = "1.4.200"
val tokenValidationVersion = "1.3.2"
val logstashVersion = "6.6"
val springSleuthVersion = "3.0.0"
val unleashVersion = "3.3.3"
val problemSpringWebStartVersion = "0.26.2"
val kafkaAvroVersion = "5.5.2"
val pdfboxVersion = "2.0.19"
val springRetryVersion = "1.3.1"
val springMockkVersion = "3.0.1"
val springFoxVersion = "3.0.0"
val testContainersVersion = "1.15.1"
val tikaVersion = "1.24.1"
val nimbusVersion = "8.20.1"
val threeTenExtraVersion = "1.6.0"
val shedlockVersion = "4.23.0"
val archunitVersion = "0.19.0"
val verapdfVersion = "1.18.8"

val githubUser: String by project
val githubPassword: String by project

java.sourceCompatibility = JavaVersion.VERSION_17

ext["elasticsearch.version"] = "7.10.2"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/simple-slack-poster")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

plugins {
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
    idea
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    //temporary fix:
    //Without updated ByteBuddy a lot of mockking doesnt work..
    implementation("net.bytebuddy:byte-buddy:1.12.3")
    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusVersion")
    implementation("org.threeten:threeten-extra:$threeTenExtraVersion")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("javax.cache:cache-api")
    implementation("org.ehcache:ehcache")
    //implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    // https://mvnrepository.com/artifact/org.elasticsearch.client/elasticsearch-rest-high-level-client
    implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.10.2")
    implementation("org.jolokia:jolokia-core")

    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:$springSleuthVersion")
    implementation("io.springfox:springfox-boot-starter:$springFoxVersion")

    implementation("org.projectreactor:reactor-spring:1.0.1.RELEASE")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-registry-influx")
    implementation("ch.qos.logback:logback-classic")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.kjetland:mbknor-jackson-jsonschema_2.13:1.0.39")
    implementation("com.papertrailapp:logback-syslog4j:1.0.0")

    implementation("org.redundent:kotlin-xml-builder:1.7.3")

    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
    implementation("no.nav.security:token-client-spring:$tokenValidationVersion")

    implementation("com.github.navikt:kabal-kodeverk:2021.12.17-13.45.e1c29a4e3a2e")
    implementation("no.nav.slackposter:simple-slack-poster:5")
    implementation("org.springframework.retry:spring-retry:$springRetryVersion")
    implementation("no.finn.unleash:unleash-client-java:$unleashVersion")
    implementation("org.zalando:problem-spring-web-starter:$problemSpringWebStartVersion")

    implementation("org.verapdf:validation-model:$verapdfVersion")
    implementation("org.apache.pdfbox:pdfbox:$pdfboxVersion")
    implementation("org.apache.tika:tika-core:$tikaVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
        exclude(group = "org.mockito")
    }
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:elasticsearch:$testContainersVersion")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
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