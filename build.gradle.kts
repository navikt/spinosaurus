import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val version = "1.0"
val mainClass = "no.nav.syfo.AppKt"
val group = "no.nav.syfo"

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jmailen.kotlinter")
    id("com.github.ben-manes.versions")
    jacoco
    application
}

kotlin {
    jvmToolchain(25)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.named<Test>("test") {
    include("**/*Test.class")
    exclude("**/*Spec.class")
}

tasks.register<Test>("slowTests") {
    description = "Runs slow tests"
    include("**/*Spec.class")
    exclude("**/*Test.class")
    outputs.upToDateWhen { false }
    group = "verification"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jar {
    val dependencies = configurations.runtimeClasspath.get()

    archiveBaseName.set("app")

    manifest {
        attributes["Main-Class"] = mainClass
        attributes["Class-Path"] =
            dependencies.joinToString(separator = " ") {
                it.name
            }
    }

    doLast {
        dependencies.forEach {
            val file =
                layout.buildDirectory
                    .file("libs/${it.name}")
                    .get()
                    .asFile
            if (!file.exists()) {
                it.copyTo(file)
            }
        }
    }
}

repositories {
    val githubPassword = project.property("githubPassword") as String
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven {
        setUrl("https://maven.pkg.github.com/navikt/*")
        credentials {
            username = "x-access-token"
            password = githubPassword
        }
    }
}

dependencies {
    val altinnCorrespondanceVersion = project.property("altinnCorrespondanceVersion") as String
    val altinnInntektsmeldingVersion = project.property("altinnInntektsmeldingVersion") as String
    val annotationApiVersion = project.property("annotationApiVersion") as String
    val apacheKafkaStreamsVersion = project.property("apacheKafkaStreamsVersion") as String
    val assertJVersion = project.property("assertJVersion") as String
    val bakgrunnsjobbVersion = project.property("bakgrunnsjobbVersion") as String
    val cxfVersion = project.property("cxfVersion") as String
    val flywayVersion = project.property("flywayVersion") as String
    val hagDomeneInntektsmeldingVersion = project.property("hagDomeneInntektsmeldingVersion") as String
    val hagUtilsVersion = project.property("hagUtilsVersion") as String
    val hikariVersion = project.property("hikariVersion") as String
    val imkontraktVersion = project.property("imkontraktVersion") as String
    val jacksonVersion = project.property("jacksonVersion") as String
    val jaxbRuntimeVersion = project.property("jaxbRuntimeVersion") as String
    val jaxbVersion = project.property("jaxbVersion") as String
    val joarkHendelseVersion = project.property("joarkHendelseVersion") as String
    val junitJupiterVersion = project.property("junitJupiterVersion") as String
    val kafkaVersion = project.property("kafkaVersion") as String
    val koinVersion = project.property("koinVersion") as String
    val kotlinxCoroutinesVersion = project.property("kotlinxCoroutinesVersion") as String
    val kotlinxSerializationVersion = project.property("kotlinxSerializationVersion") as String
    val ktorVersion = project.property("ktorVersion") as String
    val logbackClassicVersion = project.property("logbackClassicVersion") as String
    val logbackVersion = project.property("logbackVersion") as String
    val mockkVersion = project.property("mockkVersion") as String
    val oppgaveClientVersion = project.property("oppgaveClientVersion") as String
    val pdlClientVersion = project.property("pdlClientVersion") as String
    val postgresVersion = project.property("postgresVersion") as String
    val prometheusVersion = project.property("prometheusVersion") as String
    val slf4Version = project.property("slf4Version") as String
    val tokenSupportVersion = project.property("tokenSupportVersion") as String

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("io.confluent:kafka-avro-serializer:$kafkaVersion") { exclude("org.apache.avro:avro") }
    implementation("io.confluent:kafka-streams-avro-serde:$kafkaVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.ktor:ktor-client-apache5:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.mockk:mockk:$mockkVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("javax.annotation:javax.annotation-api:$annotationApiVersion")
    implementation("javax.xml.bind:jaxb-api:$jaxbVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackVersion")
    implementation("no.nav.helsearbeidsgiver:domene-inntektsmelding:$hagDomeneInntektsmeldingVersion")
    implementation("no.nav.helsearbeidsgiver:hag-bakgrunnsjobb:$bakgrunnsjobbVersion")
    implementation("no.nav.helsearbeidsgiver:oppgave-client:$oppgaveClientVersion")
    implementation("no.nav.helsearbeidsgiver:pdl-client:$pdlClientVersion")
    implementation("no.nav.helsearbeidsgiver:utils:$hagUtilsVersion")
    implementation("no.nav.security:token-client-core:$tokenSupportVersion")
    implementation("no.nav.security:token-validation-ktor-v3:$tokenSupportVersion")
    implementation("no.nav.sykepenger.kontrakter:inntektsmelding-kontrakt:$imkontraktVersion")
    implementation("no.nav.teamdokumenthandtering:teamdokumenthandtering-avro-schemas:$joarkHendelseVersion")
    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external-basic:$altinnCorrespondanceVersion")
    implementation("no.nav.tjenestespesifikasjoner:nav-altinn-inntektsmelding:$altinnInntektsmeldingVersion")
    implementation("org.apache.cxf:cxf-core:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-databinding-jaxb:$cxfVersion")
    implementation("org.apache.kafka:kafka-streams:$apacheKafkaStreamsVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.slf4j:slf4j-api:$slf4Version")

    runtimeOnly("ch.qos.logback:logback-classic:$logbackClassicVersion")
    runtimeOnly("com.sun.xml.bind:jaxb-impl:$jaxbRuntimeVersion")

    testImplementation(testFixtures("no.nav.helsearbeidsgiver:utils:$hagUtilsVersion"))
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
