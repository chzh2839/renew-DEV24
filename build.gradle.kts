plugins {
	java
	id("org.springframework.boot") version "3.5.16"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
}

group = "com.dev24"
version = "0.0.1-SNAPSHOT"
description = "DEV24 도서 쇼핑몰 현대화"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("io.nats:jnats:2.25.3")
	implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260313.1")
	implementation(platform("software.amazon.awssdk:bom:2.47.6"))
	implementation("software.amazon.awssdk:s3")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	archiveFileName.set("app.jar")
}

jacoco {
	toolVersion = "0.8.12"
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	// dependsOn(tasks.test)를 일부러 넣지 않는다 - finalizedBy와 서로 dependsOn하는 순환 관계가 되면
	// test가 실패했을 때 Gradle이 finalizedBy 자체를 건너뛰어 리포트가 아예 생성되지 않는 문제가 있다
	// (https://github.com/gradle/gradle/issues/27707). finalizedBy만으로 순서 보장은 충분하고,
	// 대신 이 태스크를 단독 실행하려면 먼저 ./gradlew test를 실행해 최신 실행 데이터를 만들어둬야 한다.
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
	// QueryDSL이 생성하는 Q-클래스는 순수 보일러플레이트라 커버리지 대상에서 제외
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) { exclude("**/Q*.class") }
		})
	)
}
